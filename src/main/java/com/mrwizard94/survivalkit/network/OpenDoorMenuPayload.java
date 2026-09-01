package com.mrwizard94.survivalkit.network;

import com.mrwizard94.survivalkit.MrWizardsSurvivalKitMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Server → client: open enter/pack door menu.
 *
 * @param doorPos controller position
 * @param tentId  tent UUID
 */
public record OpenDoorMenuPayload(BlockPos doorPos, UUID tentId) implements CustomPacketPayload {

    /** Payload type. */
    public static final CustomPacketPayload.Type<OpenDoorMenuPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(MrWizardsSurvivalKitMod.MOD_ID, "open_door_menu"));

    /** Stream codec. */
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenDoorMenuPayload> STREAM_CODEC =
            StreamCodec.of(OpenDoorMenuPayload::write, OpenDoorMenuPayload::read);

    private static void write(final RegistryFriendlyByteBuf buf, final OpenDoorMenuPayload payload) {
        buf.writeBlockPos(payload.doorPos);
        buf.writeUUID(payload.tentId);
    }

    private static OpenDoorMenuPayload read(final RegistryFriendlyByteBuf buf) {
        return new OpenDoorMenuPayload(buf.readBlockPos(), buf.readUUID());
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
