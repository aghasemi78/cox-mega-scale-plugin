package net.runelite.client.plugins.coxmegascale.overlays;

import javax.inject.Inject;

import net.runelite.client.plugins.coxmegascale.CoxMegaScaleConfig;
import net.runelite.client.plugins.coxmegascale.CoxMegaScalePlugin;
import net.runelite.client.plugins.coxmegascale.events.PartySizeChanged;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import net.runelite.client.eventbus.Subscribe;

import lombok.extern.slf4j.Slf4j;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

/**
 * Overlay class for displaying supplies calculations.
 */
@Slf4j
public class SuppliesCalculationOverlay extends OverlayPanel {
    private final CoxMegaScaleConfig config;
    private final CoxMegaScalePlugin plugin;

    @Inject
    public SuppliesCalculationOverlay(CoxMegaScaleConfig config, CoxMegaScalePlugin plugin) {
        this.config = config;
        this.plugin = plugin;

        setPosition(OverlayPosition.BOTTOM_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.LOW);
    }

    /**
     * Listen for PartySizeChanged events to log changes.
     *
     * @param event The PartySizeChanged event.
     */
    @Subscribe
    public void onPartySizeChanged(PartySizeChanged event) {
        log.debug("Received PartySizeChanged event: new party size = {}", event.getNewPartySize());
        // No need to trigger a repaint; the render method will fetch the latest data
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.enableSuppliesCalculationOverlay()) {
            return null;
        }

        try {
            panelComponent.getChildren().clear();

            // Fetch the current actual party size from the plugin
            int actualSize = plugin.getActualPartySizeValue();

            // Perform the dynamic calculations with proper ceiling for overloads
            int overloads = (int) Math.ceil(((5.0 * actualSize) + 5.0) / 4.0); // Ceiling division
            int fish = (20 * actualSize + 20);
            int golpar = 3 * overloads;

            // Define the text components
            String titleText = "Supplies Calculation";
            String actualSizeLabel = "Actual Party Size:";
            String overloadsLabel = "Overloads Needed:";
            String fishLabel = "Fish Needed:";
            String golparLabel = "Golpar Needed:";

            String actualSizeValue = String.valueOf(actualSize);
            String overloadsValue = String.valueOf(overloads);
            String fishValue = String.valueOf(fish);
            String golparValue = String.valueOf(golpar);

            // Measure the widths of the labels and values
            int titleWidth = graphics.getFontMetrics().stringWidth(titleText);
            int actualSizeLabelWidth = graphics.getFontMetrics().stringWidth(actualSizeLabel);
            int overloadsLabelWidth = graphics.getFontMetrics().stringWidth(overloadsLabel);
            int fishLabelWidth = graphics.getFontMetrics().stringWidth(fishLabel);
            int golparLabelWidth = graphics.getFontMetrics().stringWidth(golparLabel);

            int actualSizeValueWidth = graphics.getFontMetrics().stringWidth(actualSizeValue);
            int overloadsValueWidth = graphics.getFontMetrics().stringWidth(overloadsValue);
            int fishValueWidth = graphics.getFontMetrics().stringWidth(fishValue);
            int golparValueWidth = graphics.getFontMetrics().stringWidth(golparValue);

            // Determine the maximum width needed for labels and values
            int maxLabelWidth = Math.max(Math.max(actualSizeLabelWidth, overloadsLabelWidth),
                    Math.max(fishLabelWidth, golparLabelWidth));
            int maxValueWidth = Math.max(Math.max(actualSizeValueWidth, overloadsValueWidth),
                    Math.max(fishValueWidth, golparValueWidth));

            // Calculate the total width needed for the overlay
            int padding = 10; // Padding around the content
            int totalWidth = Math.max(titleWidth, maxLabelWidth + 10 + maxValueWidth) + padding * 2;

            // Set the panel's preferred size based on the calculated width
            panelComponent.setPreferredSize(new Dimension(totalWidth, panelComponent.getPreferredSize().height));

            // Create and configure the TitleComponent
            TitleComponent title = TitleComponent.builder()
                    .text(titleText)
                    .color(Color.WHITE)
                    .build();
            panelComponent.getChildren().add(title);

            // Display Actual Party Size
            LineComponent actualSizeLine = LineComponent.builder()
                    .left(actualSizeLabel)
                    .right(actualSizeValue)
                    .rightColor(Color.ORANGE)
                    .leftColor(Color.WHITE)
                    .build();
            panelComponent.getChildren().add(actualSizeLine);

            // Display Overloads Needed
            LineComponent overloadsLine = LineComponent.builder()
                    .left(overloadsLabel)
                    .right(overloadsValue)
                    .rightColor(Color.ORANGE)
                    .leftColor(Color.WHITE)
                    .build();
            panelComponent.getChildren().add(overloadsLine);

            // Display Fish Needed
            LineComponent fishLine = LineComponent.builder()
                    .left(fishLabel)
                    .right(fishValue)
                    .rightColor(Color.ORANGE)
                    .leftColor(Color.WHITE)
                    .build();
            panelComponent.getChildren().add(fishLine);

            // Display Golpar Needed
            LineComponent golparLine = LineComponent.builder()
                    .left(golparLabel)
                    .right(golparValue)
                    .rightColor(Color.ORANGE)
                    .leftColor(Color.WHITE)
                    .build();
            panelComponent.getChildren().add(golparLine);

            // Render the panel
            return super.render(graphics);
        } catch (Exception e) {
            log.error("Error rendering SuppliesCalculationOverlay: ", e);
            return null;
        }
    }
}
