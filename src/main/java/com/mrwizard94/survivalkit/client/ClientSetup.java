package com.mrwizard94.survivalkit.client;

import com.mrwizard94.survivalkit.network.ModNetworking;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

/**
 * Client-only registration hook.
 */
public final class ClientSetup {

    private ClientSetup() {
    }

    /**
     * Documented member.
     *
     * @param modBus mod event bus
     */
    public static void register(@NotNull final IEventBus modBus) {
        ModNetworking.installClientHandlers(
                ClientPacketHandlers::handleOpenDoorMenu,
                ClientPacketHandlers::handleTransition);
        modBus.addListener(PlaceTentKeyHandler::onRegisterKeyMappings);
        NeoForge.EVENT_BUS.register(new BlackScreenOverlay());
        NeoForge.EVENT_BUS.register(new ClientTicker());
        NeoForge.EVENT_BUS.register(new PlaceTentKeyHandler());
    }
}
