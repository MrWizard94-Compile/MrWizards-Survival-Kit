package com.mrwizard94.survivalkit.item;

import com.mrwizard94.survivalkit.block.CampingTentBlock;
import com.mrwizard94.survivalkit.dimension.TentServices;
import com.mrwizard94.survivalkit.logic.TentFootprint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Item that pitches the exterior tent multiblock when clearance allows.
 */
public final class CampingTentItem extends BlockItem {

    /** NBT key for persisted tent UUID on the item. */
    public static final String TAG_TENT_ID = "TentId";

    /**
     * Documented member.
     *
     * @param block      camping tent block
     * @param properties item properties
     */
    public CampingTentItem(final Block block, final Properties properties) {
        super(block, properties);
    }

    @Override
    @NotNull
    public InteractionResult useOn(@NotNull final UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.FAIL;
        }

        final BlockPos clicked = context.getClickedPos();
        final Direction face = context.getClickedFace();
        final BlockPos placePos = face == Direction.UP ? clicked.above() : clicked.relative(face);
        final boolean placed = tryPitch(
                serverLevel,
                player,
                context.getItemInHand(),
                placePos,
                context.getHorizontalDirection());
        return placed ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    /**
     * Shared pitch entry used by right-click and Curios/keybind placement.
     *
     * @param level      server level
     * @param player     player
     * @param stack      tent stack
     * @param doorPos    door/entrance ground cell
     * @param playerLook player horizontal look direction
     * @return {@code true} if pitched
     */
    public static boolean tryPitch(
            @NotNull final ServerLevel level,
            @NotNull final ServerPlayer player,
            @NotNull final ItemStack stack,
            @NotNull final BlockPos doorPos,
            @NotNull final Direction playerLook) {
        final Direction facing = playerLook.getOpposite(); // opens toward the player
        final int facingIndex = facing.get2DDataValue();
        final int[] origin = TentFootprint.originForDoorPlacement(
                doorPos.getX(), doorPos.getZ(), facingIndex);
        final int originX = origin[0];
        final int originY = doorPos.getY();
        final int originZ = origin[1];

        final boolean clear = TentFootprint.hasClearance(
                originX,
                originY,
                originZ,
                (x, y, z) -> level.getBlockState(new BlockPos(x, y, z)).canBeReplaced());
        if (!clear) {
            player.displayClientMessage(
                    Component.translatable("message.mrwizardssurvivalkit.no_clearance"), true);
            return false;
        }

        final UUID tentId = readOrCreateTentId(stack);
        final BlockPos resolvedDoor = computeDoorPos(originX, originY, originZ, facing);
        TentServices.pitchTent(level, player, tentId, originX, originY, originZ, facing, resolvedDoor);

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        level.playSound(
                null,
                resolvedDoor,
                com.mrwizard94.survivalkit.registry.ModSounds.TENT_ZIPPER.value(),
                SoundSource.BLOCKS,
                1.0F,
                1.0F);
        return true;
    }

    /**
     * Pitches in front of the player when no block target is available (keybind).
     *
     * @param level  level
     * @param player player
     * @param stack  tent stack
     * @return {@code true} if pitched
     */
    public static boolean tryPitchInFrontOfPlayer(
            @NotNull final ServerLevel level,
            @NotNull final ServerPlayer player,
            @NotNull final ItemStack stack) {
        final Direction look = player.getDirection();
        final BlockPos feet = player.blockPosition();
        final BlockPos doorPos = feet.relative(look, 2);
        return tryPitch(level, player, stack, doorPos, look);
    }

    /**
     * Reads tent id from item custom data or creates a new one.
     *
     * @param stack item stack
     * @return tent UUID
     */
    @NotNull
    public static UUID readOrCreateTentId(@NotNull final ItemStack stack) {
        final CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            final CompoundTag tag = data.copyTag();
            if (tag.hasUUID(TAG_TENT_ID)) {
                return tag.getUUID(TAG_TENT_ID);
            }
        }
        final UUID id = UUID.randomUUID();
        writeTentId(stack, id);
        return id;
    }

    /**
     * Writes tent id onto the item stack.
     *
     * @param stack item
     * @param id    tent id
     */
    public static void writeTentId(@NotNull final ItemStack stack, @NotNull final UUID id) {
        final CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putUUID(TAG_TENT_ID, id);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    /**
     * Door sits on the entrance face of the footprint.
     *
     * @param originX origin X
     * @param originY origin Y
     * @param originZ origin Z
     * @param facing  facing toward the player
     * @return door block pos
     */
    @NotNull
    public static BlockPos computeDoorPos(
            final int originX, final int originY, final int originZ, @NotNull final Direction facing) {
        final int[] door = TentFootprint.doorBlock(originX, originY, originZ, facing.get2DDataValue());
        return new BlockPos(door[0], door[1], door[2]);
    }

    /**
     * Helper for tests / services: default block state with facing.
     *
     * @param facing facing
     * @return state
     */
    @NotNull
    public static BlockState controllerState(@NotNull final Direction facing) {
        return com.mrwizard94.survivalkit.registry.ModBlocks.CAMPING_TENT.get()
                .defaultBlockState()
                .setValue(CampingTentBlock.FACING, facing)
                .setValue(CampingTentBlock.ANIM_STAGE, 0);
    }

    /**
     * Finds a tent stack in inventory (and optionally a supplied curios stack).
     *
     * @param player      player
     * @param curiosStack optional curios-equipped tent
     * @return tent stack or empty
     */
    @NotNull
    public static ItemStack findTentStack(
            @NotNull final ServerPlayer player, @Nullable final ItemStack curiosStack) {
        if (curiosStack != null && curiosStack.is(com.mrwizard94.survivalkit.registry.ModItems.CAMPING_TENT.get())) {
            return curiosStack;
        }
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            final ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(com.mrwizard94.survivalkit.registry.ModItems.CAMPING_TENT.get())) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * Client/server-safe check used by keybind payload handling.
     *
     * @param level level
     * @return whether this is a server level
     */
    public static boolean isServer(@NotNull final Level level) {
        return !level.isClientSide && level instanceof ServerLevel;
    }
}
