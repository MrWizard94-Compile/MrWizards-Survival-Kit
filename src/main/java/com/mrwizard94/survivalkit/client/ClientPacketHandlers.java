package com.mrwizard94.survivalkit.client;

import com.mrwizard94.survivalkit.client.gui.TentDoorScreen;
import com.mrwizard94.survivalkit.network.OpenDoorMenuPayload;
import com.mrwizard94.survivalkit.network.TentTransitionPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * Client payload handlers (referenced from common networking; safe no-op on dedicated server
 * because playToClient handlers only run on the client).
 */
public final class ClientPacketHandlers {

    private ClientPacketHandlers() {
    }

    /**
     * Opens the door menu screen.
     *
     * @param payload payload
     * @param context context
     */
    public static void handleOpenDoorMenu(
            @NotNull final OpenDoorMenuPayload payload, @NotNull final IPayloadContext context) {
        context.enqueueWork(() -> {
            final Minecraft mc = Minecraft.getInstance();
            mc.setScreen(new TentDoorScreen(payload.doorPos(), payload.tentId()));
        });
    }

    /**
     * Starts black-screen transition.
     *
     * @param payload payload
     * @param context context
     */
    public static void handleTransition(
            @NotNull final TentTransitionPayload payload, @NotNull final IPayloadContext context) {
        context.enqueueWork(() -> BlackScreenOverlay.trigger(payload.blackScreenTicks()));
    }
}
