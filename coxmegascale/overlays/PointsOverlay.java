package net.runelite.client.plugins.coxmegascale.overlays;

import javax.inject.Inject;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.TextComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import java.awt.Dimension;
import java.awt.Graphics2D;

public class PointsOverlay extends Overlay
{
    private final PanelComponent panelComponent = new PanelComponent();

    @Inject
    public PointsOverlay()
    {
        setPosition(OverlayPosition.TOP_RIGHT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        // Create and configure the TextComponent
        TextComponent textComponent = new TextComponent();
        textComponent.setText("this is the points overlay");

        // Add the TextComponent to the PanelComponent
        panelComponent.getChildren().add((LayoutableRenderableEntity) textComponent);

        // Render the panel
        return panelComponent.render(graphics);
    }
}
