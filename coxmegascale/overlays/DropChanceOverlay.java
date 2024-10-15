// File: DropChanceOverlay.java
package net.runelite.client.plugins.coxmegascale.overlays;

import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayManager;
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

@Slf4j
public class DropChanceOverlay extends OverlayPanel
{
    private final CoxMegaScaleConfig config;

    @Inject
    public DropChanceOverlay(CoxMegaScaleConfig config)
    {
        this.config = config;
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.LOW); // Optional: Set overlay priority if needed
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.enableDropChanceOverlay())
        {
            return null;
        }

        try {
            panelComponent.getChildren().clear();

            // Create and configure the TitleComponent
            TitleComponent title = TitleComponent.builder()
                    .text("Drop Chances")
                    .color(Color.WHITE)
                    .build();
            panelComponent.getChildren().add(title);

            // Example LineComponents with sample data
            LineComponent uniqueChanceLine = LineComponent.builder()
                    .left("Unique Chance:")
                    .right("20%")
                    .rightColor(Color.ORANGE)
                    .build();
            panelComponent.getChildren().add(uniqueChanceLine);

            LineComponent sampleChanceLine = LineComponent.builder()
                    .left("Sample Chance:")
                    .right("10%")
                    .rightColor(Color.ORANGE)
                    .build();
            panelComponent.getChildren().add(sampleChanceLine);

            // Add more LineComponents as needed for dynamic data

            // Render the panel
            return super.render(graphics);
        } catch (Exception e) {
            log.error("Error rendering DropChanceOverlay: ", e);
            return null;
        }
    }
}
