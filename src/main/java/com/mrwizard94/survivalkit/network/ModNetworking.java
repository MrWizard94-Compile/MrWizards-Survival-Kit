package com.mrwizard94.survivalkit.network;

import com.mrwizard94.survivalkit.compat.TentStackLocator;
import com.mrwizard94.survivalkit.dimension.TentServices;
import com.mrwizard94.survivalkit.item.CampingTentItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

/**
 * Registers custom payloads. Client handlers are installed from {@code ClientSetup}
 * so dedicated servers never class-load client-only GUI code.
 */
public final class ModNetworking {

    private static BiConsumer<OpenDoorMenuPayload, IPayloadContext> openDoorHandler = (payload, context) -> {
    };
    private static BiConsumer<TentTransitionPayload, IPayloadContext> transitionHandler = (payload, context) -> {
    };

    private ModNetworking() {
    }

    /**
     * Installs client-side payload consumers. Call only from client setup.
     *
     * @param openDoor    open-door handler
     * @param transition  transition handler
     */
    public static void installClientHandlers(
            @NotNull final BiConsumer<OpenDoorMenuPayload, IPayloadContext> openDoor,
            @NotNull final BiConsumer<TentTransitionPayload, IPayloadContext> transition) {
        openDoorHandler = openDoor;
        transitionHandler = transition;
    }

    /**
     * Documented member.
     *
     * @param event register event
     */
    public static void register(@NotNull final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                OpenDoorMenuPayload.TYPE,
                OpenDoorMenuPayload.STREAM_CODEC,
                (payload, context) -> openDoorHandler.accept(payload, context));
        registrar.playToClient(
                TentTransitionPayload.TYPE,
                TentTransitionPayload.STREAM_CODEC,
                (payload, context) -> transitionHandler.accept(payload, context));
        registrar.playToServer(
                DoorActionPayload.TYPE,
                DoorActionPayload.STREAM_CODEC,
                ModNetworking::handleDoorAction);
        registrar.playToServer(
                PlaceTentKeyPayload.TYPE,
                PlaceTentKeyPayload.STREAM_CODEC,
                ModNetworking::handlePlaceTentKey);
    }

    private static void handleDoorAction(
            final DoorActionPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                TentServices.handleDoorAction(
                        serverPlayer, payload.doorPos(), payload.tentId(), payload.action());
            }
        });
    }

    private static void handlePlaceTentKey(
            final PlaceTentKeyPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer serverPlayer)) {
                return;
            }
            if (!(serverPlayer.level() instanceof ServerLevel serverLevel)) {
                return;
            }
            final ItemStack tent = TentStackLocator.findTent(serverPlayer);
            if (tent.isEmpty()) {
                serverPlayer.displayClientMessage(
                        Component.translatable("message.mrwizardssurvivalkit.no_tent_to_place"), true);
                return;
            }
            CampingTentItem.tryPitchInFrontOfPlayer(serverLevel, serverPlayer, tent);
        });
    }
}
