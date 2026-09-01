package com.mrwizard94.survivalkit.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Decrements black-screen overlay on client ticks.
 */
public final class ClientTicker {

    /**
     * Documented member.
     *
     * @param event client tick
     */
    @SubscribeEvent
    public void onClientTick(@NotNull final ClientTickEvent.Post event) {
        BlackScreenOverlay.tick();
    }
}
