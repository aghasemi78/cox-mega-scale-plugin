package net.runelite.client.plugins.coxmegascale.events;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Event indicating that the raid party size has changed.
 */
@RequiredArgsConstructor
@Getter
public class PartySizeChanged
{
    private final int newPartySize;
}

