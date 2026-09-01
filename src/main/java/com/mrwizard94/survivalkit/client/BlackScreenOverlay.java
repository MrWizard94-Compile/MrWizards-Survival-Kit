package com.mrwizard94.survivalkit.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Full-screen black fade used during tent enter/leave loading.
 */
public final class BlackScreenOverlay {

    private static int remainingTicks;

    /**
     * Starts a black screen for the given duration.
     *
     * @param ticks duration
     */
    public static void trigger(final int ticks) {
        remainingTicks = Math.max(remainingTicks, ticks);
    }

    /**
     * Client-tick decrement.
     */
    public static void tick() {
        if (remainingTicks > 0) {
            remainingTicks--;
        }
    }

    /**
     * Documented member.
     *
     * @return remaining ticks (for tests / diagnostics)
     */
    public static int remainingTicks() {
        return remainingTicks;
    }

    /**
     * Resets overlay state.
     */
    public static void reset() {
        remainingTicks = 0;
    }

    /**
     * Renders the overlay.
     *
     * @param event render event
     */
    @SubscribeEvent
    public void onRenderGui(@NotNull final RenderGuiEvent.Post event) {
        if (remainingTicks <= 0) {
            return;
        }
        final GuiGraphics graphics = event.getGuiGraphics();
        final Minecraft mc = Minecraft.getInstance();
        final int width = mc.getWindow().getGuiScaledWidth();
        final int height = mc.getWindow().getGuiScaledHeight();
        graphics.fill(0, 0, width, height, 0xFF000000);
    }
}
