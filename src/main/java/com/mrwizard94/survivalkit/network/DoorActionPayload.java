package com.mrwizard94.survivalkit.network;

import com.mrwizard94.survivalkit.MrWizardsSurvivalKitMod;
import com.mrwizard94.survivalkit.logic.DoorAction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Client → server: chosen door action.
 *
 * @param doorPos controller
 * @param tentId  tent id
 * @param action  enter or pack
 */
public record DoorActionPayload(BlockPos doorPos, UUID tentId, DoorAction action)
        implements CustomPacketPayload {

    /** Payload type. */
    public static final CustomPacketPayload.Type<DoorActionPayload> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(MrWizardsSurvivalKitMod.MOD_ID, "door_action"));

    /** Stream codec. */
    public static final StreamCodec<RegistryFriendlyByteBuf, DoorActionPayload> STREAM_CODEC =
            StreamCodec.of(DoorActionPayload::write, DoorActionPayload::read);

    private static void write(final RegistryFriendlyByteBuf buf, final DoorActionPayload payload) {
        buf.writeBlockPos(payload.doorPos);
        buf.writeUUID(payload.tentId);
        buf.writeEnum(payload.action);
    }

    private static DoorActionPayload read(final RegistryFriendlyByteBuf buf) {
        return new DoorActionPayload(buf.readBlockPos(), buf.readUUID(), buf.readEnum(DoorAction.class));
    }

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
