package net.runelite.client.plugins.coxmegascale;

import net.runelite.client.config.*;

@ConfigGroup("coxmegascale")
public interface CoxMegaScaleConfig extends Config
{
    @ConfigSection(
            name = "Scouting",
            description = "Settings related to raid scouting",
            position = 0
    )
    String scoutingSection = "scouting";

    @ConfigItem(
            keyName = "raidStartOrder",
            name = "Raid Start Order",
            description = "Select the desired raid start order code",
            position = 1,
            section = scoutingSection
    )
    default RaidStartOrder raidStartOrder()
    {
        return RaidStartOrder.ANY;
    }

    // Include options for selecting desired rooms
    @ConfigItem(
            keyName = "includeMystics",
            name = "Include Mystics",
            description = "Include Mystics in desired rooms",
            position = 2,
            section = scoutingSection
    )
    default boolean includeMystics()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeShamans",
            name = "Include Shamans",
            description = "Include Shamans in desired rooms",
            position = 3,
            section = scoutingSection
    )
    default boolean includeShamans()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeVasa",
            name = "Include Vasa",
            description = "Include Vasa in desired rooms",
            position = 4,
            section = scoutingSection
    )
    default boolean includeVasa()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeTightrope",
            name = "Include Tightrope",
            description = "Include Tightrope in desired rooms",
            position = 5,
            section = scoutingSection
    )
    default boolean includeTightrope()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeThieving",
            name = "Include Thieving",
            description = "Include Thieving in desired rooms",
            position = 6,
            section = scoutingSection
    )
    default boolean includeThieving()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeIceDemon",
            name = "Include Ice Demon",
            description = "Include Ice Demon in desired rooms",
            position = 7,
            section = scoutingSection
    )
    default boolean includeIceDemon()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeGuardians",
            name = "Include Guardians",
            description = "Include Guardians in desired rooms",
            position = 8,
            section = scoutingSection
    )
    default boolean includeGuardians()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeVespula",
            name = "Include Vespula",
            description = "Include Vespula in desired rooms",
            position = 9,
            section = scoutingSection
    )
    default boolean includeVespula()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeTekton",
            name = "Include Tekton",
            description = "Include Tekton in desired rooms",
            position = 10,
            section = scoutingSection
    )
    default boolean includeTekton()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeVanguards",
            name = "Include Vanguards",
            description = "Include Vanguards in desired rooms",
            position = 11,
            section = scoutingSection
    )
    default boolean includeVanguards()
    {
        return false;
    }

    @ConfigItem(
            keyName = "includeMuttadiles",
            name = "Include Muttadiles",
            description = "Include Muttadiles in desired rooms",
            position = 12,
            section = scoutingSection
    )
    default boolean includeMuttadiles()
    {
        return false;
    }

    @ConfigItem(
            keyName = "roomCount",
            name = "Room Count",
            description = "Select the desired total number of rooms",
            position = 13,
            section = scoutingSection
    )
    default RoomCount roomCount()
    {
        return RoomCount.ANY;
    }

    enum RoomCount
    {
        ANY("Any"),
        FIVE("5"),
        SIX("6");

        private final String name;

        RoomCount(String name)
        {
            this.name = name;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }

    @ConfigItem(
            keyName = "enableSuppliesCalculationOverlay",
            name = "Enable Supplies Calculation Overlay",
            description = "Show Supplies Calculation Overlay with Overloads, Fish, and Golpar counts."
    )
    default boolean enableSuppliesCalculationOverlay()
    {
        return true;
    }

    @ConfigItem(
            keyName = "enablePointsOverlay",
            name = "Enable Points Overlay",
            description = "Show Points Overlay with Total Points and Lost Points."
    )
    default boolean enablePointsOverlay()
    {
        return true;
    }

    @ConfigItem(
            keyName = "enableDropChanceOverlay",
            name = "Enable Drop Chance Overlay",
            description = "Show Drop Chance Overlay with Unique Chance and Fixed Chance."
    )
    default boolean enableDropChanceOverlay()
    {
        return true;
    }
}
