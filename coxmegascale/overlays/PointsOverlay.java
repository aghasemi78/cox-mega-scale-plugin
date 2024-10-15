// File: PointsOverlay.java
package net.runelite.client.plugins.coxmegascale.overlays;

import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import lombok.extern.slf4j.Slf4j;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import net.runelite.client.plugins.coxmegascale.CoxMegaScaleConfig;
import net.runelite.client.plugins.coxmegascale.CoxMegaScalePlugin;

@Slf4j
public class PointsOverlay extends OverlayPanel {
    private final CoxMegaScaleConfig config;
    private final CoxMegaScalePlugin plugin;

    @Inject
    public PointsOverlay(CoxMegaScaleConfig config, CoxMegaScalePlugin plugin) {
        this.config = config;
        this.plugin = plugin;
        setPosition(OverlayPosition.TOP_RIGHT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.LOW); // Optional: Set overlay priority if needed
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (!config.enablePointsOverlay()) {
            return null;
        }

        try {
            panelComponent.getChildren().clear();

            // Create and configure the TitleComponent
            TitleComponent title = TitleComponent.builder()
                    .text("Raid Points")
                    .color(Color.WHITE)
                    .build();
            panelComponent.getChildren().add(title);

            // Fetch total points from the plugin
            int totalPoints = plugin.getTotalPoints();

            LineComponent totalPointsLine = LineComponent.builder()
                    .left("Total Points:")
                    .right(String.valueOf(totalPoints))
                    .rightColor(Color.GREEN)
                    .build();
            panelComponent.getChildren().add(totalPointsLine);

            // Render the panel
            return super.render(graphics);
        } catch (Exception e) {
            log.error("Error rendering PointsOverlay: ", e);
            return null;
        }
    }
}
