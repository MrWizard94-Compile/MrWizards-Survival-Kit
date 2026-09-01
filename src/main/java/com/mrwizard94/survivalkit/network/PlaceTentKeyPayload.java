package com.mrwizard94.survivalkit.network;

import com.mrwizard94.survivalkit.MrWizardsSurvivalKitMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Client → server: place tent via keybind.
 */
public record PlaceTentKeyPayload() implements CustomPacketPayload {

    /** Payload type. */
    public static final CustomPacketPayload.Type<PlaceTentKeyPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(MrWizardsSurvivalKitMod.MOD_ID, "place_tent_key"));

    /** Stream codec (empty payload). */
    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceTentKeyPayload> STREAM_CODEC =
            StreamCodec.unit(new PlaceTentKeyPayload());

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
