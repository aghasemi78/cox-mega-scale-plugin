package net.runelite.client.plugins.coxmegascale;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.Varbits;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.VarbitChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.coxmegascale.events.PartySizeChanged;
import net.runelite.client.plugins.coxmegascale.overlays.DropChanceOverlay;
import net.runelite.client.plugins.coxmegascale.overlays.PointsOverlay;
import net.runelite.client.plugins.coxmegascale.overlays.SuppliesCalculationOverlay;
import net.runelite.client.plugins.coxmegascale.CoxMegaScaleConfig.RoomCount;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import net.runelite.client.plugins.raids.Raid;
import net.runelite.client.plugins.raids.RaidsPlugin;
import net.runelite.client.plugins.raids.RaidRoom;
import net.runelite.client.plugins.raids.events.RaidReset;
import net.runelite.client.plugins.raids.events.RaidScouted;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
        name = "Cox Mega Scale",
        description = "Enhances scouting with points tracking, drop chances, and supplies calculations.",
        tags = {"cox", "scouting", "points", "drop", "supplies"},
        enabledByDefault = false
)
public class CoxMegaScalePlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private EventBus eventBus;

    @Inject
    private CoxMegaScaleConfig config;

    @Inject
    private ClientThread clientThread;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private PointsOverlay pointsOverlay;

    @Inject
    private DropChanceOverlay dropChanceOverlay;

    @Inject
    private SuppliesCalculationOverlay suppliesCalculationOverlay;

    @Getter
    private Raid currentRaid;

    @Getter
    private int scaledPartySize = 1; // Default value for virtual players (if needed)

    @Getter
    private int actualPartySize = 1; // Default value for total party size (real + virtual players)

    @Getter
    private int totalPoints = 0;

    @Getter
    private int lostPoints = 0;

    @Getter
    private double uniqueChance = 0.0;

    private boolean inRaidChambers = false;

    private boolean desirableRaidFound = false;

    @Getter
    private boolean isInRaid = false;

    // Correct Varbit IDs based on user description
    private static final int IN_RAID_VARBIT_ID = 5432; // Verify if this is correct
    private static final int RAID_PARTY_SIZE_VARBIT_ID = 9539; // Scaled party size varbit (if needed)
    private static final int ACTUAL_PARTY_SIZE_VARBIT_ID = 9540; // Actual party size varbit
    private static final int RAID_PARTY_SIZE_SCALING_VARBIT_ID = 9541; // Additional scaled party size varbit (if needed)
    private static final int TOTAL_POINTS_VARBIT_ID = Varbits.TOTAL_POINTS; // Total raid points varbit

    @Provides
    CoxMegaScaleConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(CoxMegaScaleConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        log.info("Cox Mega Scale plugin started!");
        // Do not register overlays here. They will be managed based on raid status.

        // Check if the player is already in a raid at startup
        clientThread.invokeLater(() -> {
            try {
                int inRaidValue = client.getVarbitValue(IN_RAID_VARBIT_ID);
                log.debug("startUp - IN_RAID Varbit (ID {}): {}", IN_RAID_VARBIT_ID, inRaidValue);
                if (inRaidValue == 1 && !inRaidChambers) {
                    inRaidChambers = true;
                    isInRaid = true;
                    actualPartySize = calculateActualPartySize();
                    totalPoints = client.getVar(TOTAL_POINTS_VARBIT_ID);
                    log.info("Player is already in a raid. Total Points: {}", totalPoints);
                    eventBus.post(new PartySizeChanged(actualPartySize));

                    // Add overlays if enabled in config
                    addOverlays();
                }
            } catch (Exception e) {
                log.error("Error during startUp Varbit check: ", e);
            }
        });
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Cox Mega Scale plugin stopped!");

        // Remove Overlays
        removeOverlays();

        // Reset variables
        currentRaid = null;
        inRaidChambers = false;
        desirableRaidFound = false;
        isInRaid = false;
        scaledPartySize = 1;
        actualPartySize = 1;
        totalPoints = 0;
        lostPoints = 0;
        uniqueChance = 0.0;
    }

    @Subscribe
    public void onVarbitChanged(VarbitChanged event) {
        int varbitId = event.getVarbitId();
        int varbitValue = event.getValue();

        if (varbitId == IN_RAID_VARBIT_ID) {
            boolean currentlyInRaid = varbitValue == 1;
            log.debug("VarbitChanged - IN_RAID_VARBIT_ID ({}): {}", IN_RAID_VARBIT_ID, currentlyInRaid ? "In Raid" : "Not in Raid");

            if (currentlyInRaid && !isInRaid) {
                // Player has entered a raid
                isInRaid = true;
                inRaidChambers = true;
                actualPartySize = calculateActualPartySize();
                totalPoints = client.getVar(TOTAL_POINTS_VARBIT_ID);
                log.info("Raid Entry Detected. Total Points: {}", totalPoints);
                eventBus.post(new PartySizeChanged(actualPartySize));

                // Add overlays if enabled in config
                addOverlays();
            } else if (!currentlyInRaid && isInRaid) {
                // Player has exited the raid
                isInRaid = false;
                inRaidChambers = false;
                scaledPartySize = 1; // Reset to default if needed
                actualPartySize = 1; // Reset to default
                totalPoints = 0;
                lostPoints = 0;
                uniqueChance = 0.0;
                log.info("Raid Exit Detected. Total Points Reset.");
                eventBus.post(new PartySizeChanged(actualPartySize));

                // Remove overlays
                removeOverlays();
            }
        }

        // Monitor ACTUAL_PARTY_SIZE_VARBIT_ID changes
        if (varbitId == ACTUAL_PARTY_SIZE_VARBIT_ID) {
            if (isInRaid) {
                int newActualSize = calculateActualPartySize();
                if (newActualSize != actualPartySize) {
                    actualPartySize = newActualSize;
                    log.info("Actual Party Size Updated: {}", actualPartySize);
                    eventBus.post(new PartySizeChanged(actualPartySize));
                }
            }
        }

        // Update totalPoints when TOTAL_POINTS_VARBIT_ID changes
        if (varbitId == TOTAL_POINTS_VARBIT_ID) {
            if (isInRaid) {
                int newTotalPoints = client.getVar(TOTAL_POINTS_VARBIT_ID);
                if (newTotalPoints != totalPoints) {
                    totalPoints = newTotalPoints;
                    log.info("Total Raid Points updated to: {}", totalPoints);
                }
            }
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        if (isInRaid) {
            // Update total points on every game tick
            updateTotalPoints();

            int currentActualSize = calculateActualPartySize();
            if (currentActualSize != actualPartySize) {
                actualPartySize = currentActualSize;
                eventBus.post(new PartySizeChanged(actualPartySize));
            }
        }
    }

    /**
     * Fetches and updates the total raid points from varbits.
     */
    private void updateTotalPoints() {
        try {
            int newTotalPoints = client.getVar(TOTAL_POINTS_VARBIT_ID);
            if (newTotalPoints != totalPoints) {
                totalPoints = newTotalPoints;
                log.info("Total Raid Points updated to: {}", totalPoints);
            }
        } catch (Exception e) {
            log.error("Error updating total raid points: ", e);
        }
    }

    /**
     * Fetches the actual party size by reading the relevant varbit.
     *
     * @return The actual party size.
     */
    private int calculateActualPartySize() {
        try {
            int actualSize = client.getVarbitValue(ACTUAL_PARTY_SIZE_VARBIT_ID); // Varbit 9540
            log.debug("Fetched Actual Party Size - Varbit {}: {}", ACTUAL_PARTY_SIZE_VARBIT_ID, actualSize);
            return Math.max(actualSize, 1); // Ensure party size is at least 1
        } catch (Exception e) {
            log.error("Error fetching actual party size: ", e);
            return actualPartySize; // Fallback to last known value
        }
    }

    /**
     * Adds the necessary overlays based on the configuration.
     */
    private void addOverlays() {
        if (config.enablePointsOverlay()) {
            overlayManager.add(pointsOverlay);
            log.debug("PointsOverlay added to OverlayManager.");
        }

        if (config.enableDropChanceOverlay()) {
            overlayManager.add(dropChanceOverlay);
            log.debug("DropChanceOverlay added to OverlayManager.");
        }

        if (config.enableSuppliesCalculationOverlay()) {
            overlayManager.add(suppliesCalculationOverlay);
            log.debug("SuppliesCalculationOverlay added to OverlayManager.");
        }
    }

    /**
     * Removes the overlays from the OverlayManager.
     */
    private void removeOverlays() {
        if (config.enablePointsOverlay()) {
            overlayManager.remove(pointsOverlay);
            log.debug("PointsOverlay removed from OverlayManager.");
        }

        if (config.enableDropChanceOverlay()) {
            overlayManager.remove(dropChanceOverlay);
            log.debug("DropChanceOverlay removed from OverlayManager.");
        }

        if (config.enableSuppliesCalculationOverlay()) {
            overlayManager.remove(suppliesCalculationOverlay);
            log.debug("SuppliesCalculationOverlay removed from OverlayManager.");
        }
    }

    /**
     * Returns the current scaled party size.
     *
     * @return The scaled party size.
     */
    public int getScaledPartySize() {
        return this.scaledPartySize;
    }

    /**
     * Returns the actual party size.
     *
     * @return The actual party size.
     */
    public int getActualPartySizeValue() {
        return this.actualPartySize;
    }

    /**
     * Returns the total points.
     *
     * @return The total points.
     */
    public int getTotalPoints() {
        return this.totalPoints;
    }

    /**
     * Returns the lost points.
     *
     * @return The lost points.
     */
    public int getLostPoints() {
        return this.lostPoints;
    }

    /**
     * Returns the unique drop chance.
     *
     * @return The unique drop chance.
     */
    public double getUniqueChance() {
        return this.uniqueChance;
    }

    /**
     * Example method to update total points.
     * Replace with actual logic to calculate points.
     *
     * @param newTotalPoints The new total points.
     */
    public void updateTotalPoints(int newTotalPoints) {
        this.totalPoints = newTotalPoints;
        log.debug("Total Points updated to: {}", this.totalPoints);
    }

    /**
     * Example method to update lost points.
     * Replace with actual logic to calculate lost points.
     *
     * @param newLostPoints The new lost points.
     */
    public void updateLostPoints(int newLostPoints) {
        this.lostPoints = newLostPoints;
        log.debug("Lost Points updated to: {}", this.lostPoints);
    }

    /**
     * Example method to update unique drop chance.
     * Replace with actual logic to calculate unique drop chance.
     *
     * @param newUniqueChance The new unique drop chance.
     */
    public void updateUniqueChance(double newUniqueChance) {
        this.uniqueChance = newUniqueChance;
        log.debug("Unique Chance updated to: {}%", this.uniqueChance);
    }

    @Subscribe
    public void onRaidScouted(RaidScouted event) {
        this.currentRaid = event.getRaid();
        log.debug("RaidScouted event received: {}", event.getRaid());

        // Access the raid layout code
        String layoutCode = currentRaid.getLayout().toCodeString();
        log.debug("Raid Layout Code: {}", layoutCode);

        // Get the first 3 letters of the layout code
        String raidStartOrderCode = layoutCode.length() >= 3 ? layoutCode.substring(0, 3).toUpperCase() : layoutCode.toUpperCase();
        log.debug("Raid Start Order Code: {}", raidStartOrderCode);

        // Get the selected raid start order from config
        RaidStartOrder selectedStartOrder = config.raidStartOrder();
        log.debug("Selected Raid Start Order: {}", selectedStartOrder);

        // Check if the selected start order matches the raid's start order code
        boolean startOrderMatches = selectedStartOrder == RaidStartOrder.ANY ||
                raidStartOrderCode.equals(selectedStartOrder.toString());
        log.debug("Start Order Matches: {}", startOrderMatches);

        // List of room names to ignore (case-insensitive)
        List<String> roomsToIgnore = Arrays.asList("farming", "scavengers", "end", "start", "empty");
        log.debug("Rooms to Ignore: {}", roomsToIgnore);

        // Build the room order string, ignoring specified rooms
        List<String> roomNames = Arrays.stream(currentRaid.getRooms())
                .filter(room -> room != null)
                .filter(room -> !roomsToIgnore.contains(room.getName().toLowerCase()))
                .map(RaidRoom::getName)
                .collect(Collectors.toList());
        log.debug("Filtered Raid Rooms: {}", roomNames);

        // Count the total number of rooms (excluding ignored ones)
        int totalRooms = roomNames.size();
        log.debug("Total Rooms (after filtering): {}", totalRooms);

        // Collect the rooms selected by the user
        List<String> selectedRooms = getSelectedRooms();
        log.debug("Selected Rooms from Config: {}", selectedRooms);

        // Check if all selected rooms are present in the raid layout
        Set<String> raidRoomSet = roomNames.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        Set<String> selectedRoomSet = selectedRooms.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        boolean roomsMatch = raidRoomSet.containsAll(selectedRoomSet);
        log.debug("Rooms Match: {}", roomsMatch);

        // Check if the total number of rooms matches the user's selection
        boolean roomCountMatches = doesRoomCountMatch(totalRooms);
        log.debug("Room Count Matches: {}", roomCountMatches);

        // Determine if the raid is desirable
        if (!selectedRooms.isEmpty() && roomsMatch && roomCountMatches && startOrderMatches) {
            desirableRaidFound = true;
            log.info("Desirable raid found based on selected criteria.");
        } else if (!selectedRooms.isEmpty()) {
            desirableRaidFound = false;
            log.info("Raid is not desirable based on selected criteria.");
        } else {
            desirableRaidFound = false;
            log.info("No selected rooms specified. Raid is not marked as desirable.");
        }

        // Update actual party size from Raid object if available
        if (currentRaid != null) {
            try {
                // Assuming Raid object has methods to get actual party size
                // Replace these with actual methods if available
                int actualSize = calculateActualPartySize();
                log.debug("Actual Party Size from Raid object: {}", actualSize);

                if (actualSize > 0 && actualSize != actualPartySize) {
                    actualPartySize = Math.max(actualSize, 1);
                    log.info("Actual Party Size updated to: {}", actualPartySize);
                    eventBus.post(new PartySizeChanged(actualPartySize));
                }
            } catch (NoSuchMethodError | UnsupportedOperationException e) {
                // If the method does not exist, fallback to Varbit
                log.warn("Actual party size methods not found in Raid object. Falling back to Varbit.");
                actualPartySize = calculateActualPartySize();
                log.info("Actual Party Size updated via Varbit: {}", actualPartySize);
                eventBus.post(new PartySizeChanged(actualPartySize));
            }
        }
    }

    /**
     * Collects the selected rooms from the configuration.
     *
     * @return A list of selected room names.
     */
    private List<String> getSelectedRooms() {
        // Collect selected rooms from the config
        List<String> selectedRooms = new ArrayList<>();

        if (config.includeMystics()) {
            selectedRooms.add("Mystics");
        }
        if (config.includeShamans()) {
            selectedRooms.add("Shamans");
        }
        if (config.includeVasa()) {
            selectedRooms.add("Vasa");
        }
        if (config.includeTightrope()) {
            selectedRooms.add("Tightrope");
        }
        if (config.includeThieving()) {
            selectedRooms.add("Thieving");
        }
        if (config.includeIceDemon()) {
            selectedRooms.add("Ice Demon");
        }
        if (config.includeGuardians()) {
            selectedRooms.add("Guardians");
        }
        if (config.includeVespula()) {
            selectedRooms.add("Vespula");
        }
        if (config.includeTekton()) {
            selectedRooms.add("Tekton");
        }
        if (config.includeVanguards()) {
            selectedRooms.add("Vanguards");
        }
        if (config.includeMuttadiles()) {
            selectedRooms.add("Muttadiles");
        }

        log.debug("Collected Selected Rooms: {}", selectedRooms);
        return selectedRooms;
    }

    /**
     * Checks if the total number of rooms matches the user's selected criteria.
     *
     * @param totalRooms The total number of rooms in the raid.
     * @return True if the room count matches; otherwise, false.
     */
    private boolean doesRoomCountMatch(int totalRooms) {
        RoomCount selectedRoomCount = config.roomCount();
        log.debug("Selected Room Count Criteria: {}", selectedRoomCount);
        if (selectedRoomCount == RoomCount.ANY) {
            return true;
        } else if (selectedRoomCount == RoomCount.FIVE) {
            return totalRooms == 5;
        } else if (selectedRoomCount == RoomCount.SIX) {
            return totalRooms == 6;
        }
        return false;
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event) {
        if (!inRaidChambers) {
            return;
        }

        MenuEntry entry = event.getMenuEntry();
        String option = Text.removeTags(entry.getOption()).toLowerCase();
        String target = Text.removeTags(entry.getTarget()).toLowerCase();

        if (target.contains("steps")) {
            if (desirableRaidFound) {
                // When raid is desirable, deprioritize "Climb" and "Reload"
                if (option.equals("climb") || option.equals("reload")) {
                    entry.setDeprioritized(true);
                    log.debug("Deprioritized option '{}' for target '{}'.", option, target);
                }
            } else {
                // When raid is not desirable, prioritize "Reload" and deprioritize "Climb"
                if (option.equals("reload")) {
                    entry.setDeprioritized(false);
                    log.debug("Prioritized option 'reload' for target '{}'.", target);
                } else if (option.equals("climb")) {
                    entry.setDeprioritized(true);
                    log.debug("Deprioritized option 'climb' for target '{}'.", target);
                }
            }
        }
    }

    @Subscribe(priority = -1) // Ensure this runs after other menu modifications
    public void onMenuOpened(MenuOpened event) {
        if (!inRaidChambers) {
            return;
        }

        MenuEntry[] menuEntries = client.getMenuEntries();
        List<MenuEntry> entries = new ArrayList<>(Arrays.asList(menuEntries));
        MenuEntry reloadEntry = null;
        MenuEntry climbEntry = null;
        MenuEntry walkHereEntry = null;

        // Identify the menu entries for "Reload", "Climb", and "Walk here"
        for (MenuEntry entry : entries) {
            String option = Text.removeTags(entry.getOption()).toLowerCase();
            String target = Text.removeTags(entry.getTarget()).toLowerCase();

            if (target.contains("steps")) {
                if (option.equals("reload")) {
                    reloadEntry = entry;
                } else if (option.equals("climb")) {
                    climbEntry = entry;
                } else if (option.equals("walk here")) {
                    walkHereEntry = entry;
                }
            }
        }

        if (!desirableRaidFound) {
            // Undesirable raid: Make "Reload" the default left-click action
            if (reloadEntry != null) {
                entries.remove(reloadEntry);
                entries.add(reloadEntry); // Add to end to make it first
                log.debug("Set 'Reload' as default left-click action.");
            }
        } else {
            // Desirable raid: Make "Walk here" the default left-click action
            if (walkHereEntry != null) {
                entries.remove(walkHereEntry);
                entries.add(walkHereEntry); // Add to end to make it first
                log.debug("Set 'Walk here' as default left-click action.");
            }
        }

        // Update menu entries
        client.setMenuEntries(entries.toArray(new MenuEntry[0]));
    }

    @Subscribe
    public void onRaidReset(RaidReset event) {
        // Reset the current raid when the raid ends or the player leaves
        currentRaid = null;
        desirableRaidFound = false;
        log.info("RaidReset event received. Resetting currentRaid and desirableRaidFound.");
    }
}
