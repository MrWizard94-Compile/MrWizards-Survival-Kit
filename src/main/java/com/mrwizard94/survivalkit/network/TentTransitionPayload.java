package com.mrwizard94.survivalkit.network;

import com.mrwizard94.survivalkit.MrWizardsSurvivalKitMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Server → client: play black-screen loading transition.
 *
 * @param blackScreenTicks duration in ticks
 */
public record TentTransitionPayload(int blackScreenTicks) implements CustomPacketPayload {

    /** Payload type. */
    public static final CustomPacketPayload.Type<TentTransitionPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(MrWizardsSurvivalKitMod.MOD_ID, "tent_transition"));

    /** Stream codec. */
    public static final StreamCodec<RegistryFriendlyByteBuf, TentTransitionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT,
                    TentTransitionPayload::blackScreenTicks,
                    TentTransitionPayload::new);

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
