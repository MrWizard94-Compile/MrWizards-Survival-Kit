package com.mrwizard94.survivalkit.dimension;

import com.mrwizard94.survivalkit.block.CampingTentBlock;
import com.mrwizard94.survivalkit.block.TentEntranceBlock;
import com.mrwizard94.survivalkit.block.TentFabricBlock;
import com.mrwizard94.survivalkit.block.TentFabricRole;
import com.mrwizard94.survivalkit.blockentity.CampingTentBlockEntity;
import com.mrwizard94.survivalkit.compat.KitItemResolver;
import com.mrwizard94.survivalkit.compat.NeoForgeModPresence;
import com.mrwizard94.survivalkit.config.SurvivalKitConfig;
import com.mrwizard94.survivalkit.item.CampingTentItem;
import com.mrwizard94.survivalkit.logic.DoorAction;
import com.mrwizard94.survivalkit.logic.KitItemRef;
import com.mrwizard94.survivalkit.logic.LeaveReturnPose;
import com.mrwizard94.survivalkit.logic.StarterKitComposer;
import com.mrwizard94.survivalkit.logic.TentFootprint;
import com.mrwizard94.survivalkit.logic.TentInteriorBounds;
import com.mrwizard94.survivalkit.logic.TentRoomBinding;
import com.mrwizard94.survivalkit.logic.TentTransition;
import com.mrwizard94.survivalkit.network.OpenDoorMenuPayload;
import com.mrwizard94.survivalkit.network.TentTransitionPayload;
import com.mrwizard94.survivalkit.registry.ModBlocks;
import com.mrwizard94.survivalkit.registry.ModItems;
import com.mrwizard94.survivalkit.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side tent placement, room seeding, enter/leave, and pack-up.
 */
public final class TentServices {

    private TentServices() {
    }

    /**
     * Pitches the exterior multiblock and ensures a camping room binding exists.
     *
     * @param level    exterior level
     * @param player   placing player
     * @param tentId   tent UUID
     * @param originX  footprint origin X
     * @param originY  footprint origin Y
     * @param originZ  footprint origin Z
     * @param facing   door facing
     * @param doorPos  controller door position
     */
    public static void pitchTent(
            @NotNull final ServerLevel level,
            @NotNull final ServerPlayer player,
            @NotNull final UUID tentId,
            final int originX,
            final int originY,
            final int originZ,
            @NotNull final Direction facing,
            @NotNull final BlockPos doorPos) {
        pitchTent(level, tentId, originX, originY, originZ, facing, doorPos);
    }

    /**
     * Player-free pitch entry used by GameTests and internal callers.
     *
     * @param level    exterior level
     * @param tentId   tent UUID
     * @param originX  footprint origin X
     * @param originY  footprint origin Y
     * @param originZ  footprint origin Z
     * @param facing   door facing
     * @param doorPos  controller door position
     */
    public static void pitchTent(
            @NotNull final ServerLevel level,
            @NotNull final UUID tentId,
            final int originX,
            final int originY,
            final int originZ,
            @NotNull final Direction facing,
            @NotNull final BlockPos doorPos) {
        final TentSavedData data = TentSavedData.get(level);
        final Optional<TentRoomBinding> existing = data.find(tentId);
        final TentRoomBinding binding;
        if (existing.isPresent()) {
            binding = new TentRoomBinding(
                    tentId,
                    existing.get().roomIndex(),
                    level.dimension().location().toString(),
                    originX,
                    originY,
                    originZ,
                    facing.get2DDataValue(),
                    existing.get().seeded());
            data.put(binding);
        } else {
            binding = data.allocate(
                    tentId,
                    level.dimension().location().toString(),
                    originX,
                    originY,
                    originZ,
                    facing.get2DDataValue());
        }

        placeExterior(level, originX, originY, originZ, facing, doorPos, tentId);
        ensureRoom(level, binding);
    }

    private static void placeExterior(
            final ServerLevel level,
            final int originX,
            final int originY,
            final int originZ,
            final Direction facing,
            final BlockPos doorPos,
            final UUID tentId) {
        for (int y = 0; y < TentFootprint.HEIGHT; y++) {
            for (int x = 0; x < TentFootprint.WIDTH; x++) {
                for (int z = 0; z < TentFootprint.DEPTH; z++) {
                    final BlockPos pos = new BlockPos(originX + x, originY + y, originZ + z);
                    if (pos.equals(doorPos) && y == 0) {
                        final BlockState doorState = ModBlocks.CAMPING_TENT.get()
                                .defaultBlockState()
                                .setValue(CampingTentBlock.FACING, facing)
                                .setValue(CampingTentBlock.ANIM_STAGE, 0);
                        level.setBlock(pos, doorState, 3);
                        final BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof CampingTentBlockEntity tent) {
                            tent.configureAsController(tentId);
                        }
                    } else if (pos.equals(doorPos.above())) {
                        // Full-height exterior entrance (upper cell).
                        final BlockState upper = ModBlocks.TENT_FABRIC.get()
                                .defaultBlockState()
                                .setValue(TentFabricBlock.FACING, facing)
                                .setValue(TentFabricBlock.ANIM_STAGE, 0)
                                .setValue(TentFabricBlock.ROLE, TentFabricRole.ENTRANCE);
                        level.setBlock(pos, upper, 3);
                        final BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof CampingTentBlockEntity tent) {
                            tent.configureAsPart(doorPos, tentId);
                        }
                    } else {
                        final TentFabricRole role = fabricRole(x, y, z, doorPos, originX, originY, originZ);
                        final BlockState fabric = ModBlocks.TENT_FABRIC.get()
                                .defaultBlockState()
                                .setValue(TentFabricBlock.FACING, facing)
                                .setValue(TentFabricBlock.ANIM_STAGE, 0)
                                .setValue(TentFabricBlock.ROLE, role);
                        level.setBlock(pos, fabric, 3);
                        final BlockEntity be = level.getBlockEntity(pos);
                        if (be instanceof CampingTentBlockEntity tent) {
                            tent.configureAsPart(doorPos, tentId);
                        }
                    }
                }
            }
        }
    }

    private static TentFabricRole fabricRole(
            final int lx,
            final int ly,
            final int lz,
            final BlockPos doorPos,
            final int originX,
            final int originY,
            final int originZ) {
        if (ly == 0) {
            return TentFabricRole.FLOOR;
        }
        if (ly == TentFootprint.HEIGHT - 1) {
            return TentFabricRole.ROOF;
        }
        final BlockPos pos = new BlockPos(originX + lx, originY + ly, originZ + lz);
        if (pos.distManhattan(doorPos) == 1 && ly == 0) {
            return TentFabricRole.ENTRANCE;
        }
        if (pos.getX() == doorPos.getX() && pos.getZ() == doorPos.getZ()) {
            return TentFabricRole.ENTRANCE;
        }
        return TentFabricRole.WALL;
    }

    /**
     * Ensures the camping room shell exists and seeds furniture/kit once.
     *
     * @param exteriorLevel any server level
     * @param binding       binding
     */
    public static void ensureRoom(
            @NotNull final ServerLevel exteriorLevel, @NotNull final TentRoomBinding binding) {
        final ServerLevel camping = CampingDimension.resolveHost(exteriorLevel.getServer());
        final int[] origin = roomOriginFor(camping, binding.roomIndex());
        buildShell(camping, origin[0], origin[1], origin[2]);
        if (!binding.seeded()) {
            seedFurnitureAndKit(camping, origin[0], origin[1], origin[2]);
            TentSavedData.get(exteriorLevel).markSeeded(binding.tentId());
        }
    }

    /**
     * Computes room origin for the camping host level.
     *
     * @param host      camping host level
     * @param roomIndex room index
     * @return origin xyz
     */
    @NotNull
    public static int[] roomOriginFor(@NotNull final ServerLevel host, final int roomIndex) {
        return TentInteriorBounds.roomOrigin(roomIndex, CampingDimension.ROOM_GAP);
    }

    private static void buildShell(final ServerLevel level, final int ox, final int oy, final int oz) {
        final int sw = TentInteriorBounds.shellWidth();
        final int sd = TentInteriorBounds.shellDepth();
        final int sh = TentInteriorBounds.shellHeight();
        final BlockState shell = ModBlocks.TENT_SHELL.get().defaultBlockState();
        for (int y = 0; y < sh; y++) {
            for (int x = 0; x < sw; x++) {
                for (int z = 0; z < sd; z++) {
                    final BlockPos pos = new BlockPos(ox + x, oy + y, oz + z);
                    final boolean wall = x == 0 || x == sw - 1 || z == 0 || z == sd - 1 || y == 0 || y == sh - 1;
                    if (wall) {
                        level.setBlock(pos, shell, 2);
                    } else if (level.getBlockState(pos).isAir()) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                    }
                }
            }
        }
        // Custom tent entrance on -Z wall — two tall unbreakable blocks (no air gap into the void).
        final BlockPos leaveDoor = new BlockPos(ox + sw / 2, oy + 1, oz);
        final BlockState entrance = ModBlocks.TENT_ENTRANCE.get()
                .defaultBlockState()
                .setValue(TentEntranceBlock.FACING, Direction.SOUTH);
        level.setBlock(leaveDoor, entrance, 3);
        level.setBlock(leaveDoor.above(), entrance, 3);
    }

    private static void seedFurnitureAndKit(final ServerLevel level, final int ox, final int oy, final int oz) {
        final int floorY = oy + 1;
        // Along the -X wall (local x=1), not room center.
        final BlockPos furnace = new BlockPos(ox + 1, floorY, oz + 2);
        final BlockPos craft = new BlockPos(ox + 1, floorY, oz + 3);
        final BlockPos bed = new BlockPos(ox + 3, floorY, oz + 5);
        // Along the +X wall; open face toward room center (−X / WEST).
        final BlockPos barrel = new BlockPos(ox + 5, floorY, oz + 3);

        level.setBlock(
                furnace,
                Blocks.FURNACE.defaultBlockState().setValue(
                        net.minecraft.world.level.block.FurnaceBlock.FACING, Direction.EAST),
                3);
        level.setBlock(craft, Blocks.CRAFTING_TABLE.defaultBlockState(), 3);
        placeBed(level, bed, Direction.SOUTH);
        level.setBlock(
                barrel,
                Blocks.BARREL.defaultBlockState().setValue(
                        net.minecraft.world.level.block.BarrelBlock.FACING, Direction.WEST),
                3);

        level.setBlock(new BlockPos(ox + 2, floorY + 2, oz + 1), Blocks.TORCH.defaultBlockState(), 3);

        final BlockEntity be = level.getBlockEntity(barrel);
        if (be instanceof BaseContainerBlockEntity container) {
            final List<KitItemRef> kit = StarterKitComposer.compose(
                    NeoForgeModPresence.INSTANCE, SurvivalKitConfig.get().barrelContentIds());
            int slot = 0;
            for (final KitItemRef ref : kit) {
                final Optional<ItemStack> stack = KitItemResolver.resolve(ref, level);
                if (stack.isPresent() && slot < container.getContainerSize()) {
                    container.setItem(slot++, stack.get());
                }
            }
        }
    }

    /**
     * Places a usable two-block bed (FOOT + HEAD).
     *
     * @param level  level
     * @param foot   foot position
     * @param facing bed facing (toward head)
     */
    public static void placeBed(
            @NotNull final ServerLevel level,
            @NotNull final BlockPos foot,
            @NotNull final Direction facing) {
        final BlockState footState = Blocks.WHITE_BED.defaultBlockState()
                .setValue(BedBlock.FACING, facing)
                .setValue(BedBlock.PART, BedPart.FOOT);
        final BlockPos head = foot.relative(facing);
        final BlockState headState = footState.setValue(BedBlock.PART, BedPart.HEAD);
        level.setBlock(foot, footState, 3);
        level.setBlock(head, headState, 3);
    }

    /**
     * Returns whether a bed at foot is a complete usable FOOT+HEAD pair.
     *
     * @param level level
     * @param foot  foot pos
     * @return {@code true} when both halves exist
     */
    public static boolean isUsableBed(@NotNull final ServerLevel level, @NotNull final BlockPos foot) {
        final BlockState footState = level.getBlockState(foot);
        if (!footState.is(Blocks.WHITE_BED) || footState.getValue(BedBlock.PART) != BedPart.FOOT) {
            return false;
        }
        final Direction facing = footState.getValue(BedBlock.FACING);
        final BlockState headState = level.getBlockState(foot.relative(facing));
        return headState.is(Blocks.WHITE_BED) && headState.getValue(BedBlock.PART) == BedPart.HEAD;
    }

    /**
     * Opens the door choice UI for a player.
     *
     * @param player player
     * @param doorPos controller pos
     * @param tentId tent id
     */
    public static void openDoorMenu(
            @NotNull final ServerPlayer player,
            @NotNull final BlockPos doorPos,
            @NotNull final UUID tentId) {
        PacketDistributor.sendToPlayer(player, new OpenDoorMenuPayload(doorPos, tentId));
    }

    /**
     * Handles a door action from the client.
     *
     * @param player player
     * @param doorPos door
     * @param tentId tent
     * @param action action
     */
    public static void handleDoorAction(
            @NotNull final ServerPlayer player,
            @NotNull final BlockPos doorPos,
            @NotNull final UUID tentId,
            @NotNull final DoorAction action) {
        final ServerLevel level = player.serverLevel();
        if (action == DoorAction.ENTER) {
            enterTent(player, tentId);
        } else {
            packUp(player, level, doorPos, tentId);
        }
    }

    /**
     * Teleports the player into the camping room.
     *
     * @param player player
     * @param tentId tent id
     */
    public static void enterTent(@NotNull final ServerPlayer player, @NotNull final UUID tentId) {
        final ServerLevel exterior = player.serverLevel();
        final TentSavedData data = TentSavedData.get(exterior);
        final Optional<TentRoomBinding> bindingOpt = data.find(tentId);
        if (bindingOpt.isEmpty()) {
            return;
        }
        final TentRoomBinding binding = bindingOpt.get();
        ensureRoom(exterior, binding);
        final ServerLevel camping = CampingDimension.resolveHost(exterior.getServer());
        final int[] origin = roomOriginFor(camping, binding.roomIndex());
        final double tx = origin[0] + TentInteriorBounds.shellWidth() / 2.0 + 0.5;
        final double ty = origin[1] + 1.1;
        final double tz = origin[2] + 2.5;

        playTransition(player);
        player.teleportTo(camping, tx, ty, tz, Set.of(), player.getYRot(), player.getXRot());
        final LeaveReturnPose.Pose leave = LeaveReturnPose.compute(
                binding.exteriorX(), binding.exteriorY(), binding.exteriorZ(), binding.facing());
        final var tag = player.getPersistentData();
        tag.putBoolean("mwsk_inside", true);
        tag.putUUID("mwsk_tent", tentId);
        tag.putString("mwsk_ret_dim", exterior.dimension().location().toString());
        tag.putDouble("mwsk_ret_x", leave.x());
        tag.putDouble("mwsk_ret_y", leave.y());
        tag.putDouble("mwsk_ret_z", leave.z());
    }

    /**
     * Leaves the camping dimension back to the bound exterior.
     *
     * @param player player
     */
    public static void leaveTent(@NotNull final ServerPlayer player) {
        final var tag = player.getPersistentData();
        if (!tag.getBoolean("mwsk_inside")) {
            // Fallback: leave to overworld spawn-ish if somehow inside without tag
            return;
        }
        final String dim = tag.getString("mwsk_ret_dim");
        final double x = tag.getDouble("mwsk_ret_x");
        final double y = tag.getDouble("mwsk_ret_y");
        final double z = tag.getDouble("mwsk_ret_z");
        final ResourceLocation dimId = ResourceLocation.tryParse(dim);
        if (dimId == null) {
            return;
        }
        final ResourceKey<Level> key = ResourceKey.create(
                net.minecraft.core.registries.Registries.DIMENSION, dimId);
        final ServerLevel dest = player.server.getLevel(key);
        if (dest == null) {
            return;
        }
        playTransition(player);
        player.teleportTo(dest, x, y, z, Set.of(), player.getYRot(), player.getXRot());
        tag.putBoolean("mwsk_inside", false);
    }

    /**
     * Packs the exterior tent into an item (room contents remain bound to tent id).
     *
     * @param player  player
     * @param level   exterior level
     * @param doorPos controller
     * @param tentId  tent id
     */
    public static void packUp(
            @NotNull final ServerPlayer player,
            @NotNull final ServerLevel level,
            @NotNull final BlockPos doorPos,
            @NotNull final UUID tentId) {
        final ItemStack stack = packUp(level, doorPos, tentId);
        if (stack.isEmpty()) {
            return;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        playTransition(player);
    }

    /**
     * Packs the exterior tent and returns the tent item (empty if binding missing).
     *
     * @param level   exterior level
     * @param doorPos controller
     * @param tentId  tent id
     * @return tent item with id, or empty
     */
    @NotNull
    public static ItemStack packUp(
            @NotNull final ServerLevel level,
            @NotNull final BlockPos doorPos,
            @NotNull final UUID tentId) {
        final TentSavedData data = TentSavedData.get(level);
        final Optional<TentRoomBinding> bindingOpt = data.find(tentId);
        if (bindingOpt.isEmpty()) {
            return ItemStack.EMPTY;
        }
        final TentRoomBinding binding = bindingOpt.get();

        final BlockEntity be = level.getBlockEntity(doorPos);
        if (be instanceof CampingTentBlockEntity tent) {
            tent.beginPacking(binding.exteriorX(), binding.exteriorY(), binding.exteriorZ());
        } else {
            // No controller BE: clear immediately as a safe fallback.
            clearExterior(level, binding.exteriorX(), binding.exteriorY(), binding.exteriorZ());
        }

        final ItemStack stack = new ItemStack(ModItems.CAMPING_TENT.get());
        CampingTentItem.writeTentId(stack, tentId);
        level.playSound(null, doorPos, ModSounds.TENT_ZIPPER.value(), SoundSource.BLOCKS, 1.0F, 0.9F);
        return stack;
    }

    /**
     * Clears the exterior footprint after pack animation completes.
     *
     * @param level level
     * @param ox    origin X
     * @param oy    origin Y
     * @param oz    origin Z
     */
    public static void finishPackClear(
            @NotNull final ServerLevel level, final int ox, final int oy, final int oz) {
        clearExterior(level, ox, oy, oz);
    }

    /**
     * Propagates animation stage from the controller to all fabric parts in the footprint.
     *
     * @param level     level
     * @param originX   footprint origin X
     * @param originY   footprint origin Y
     * @param originZ   footprint origin Z
     * @param doorPos   controller pos
     * @param facing    facing
     * @param animStage stage 0..3
     */
    public static void syncFabricAnimStages(
            @NotNull final Level level,
            final int originX,
            final int originY,
            final int originZ,
            @NotNull final BlockPos doorPos,
            @NotNull final Direction facing,
            final int animStage) {
        for (int y = 0; y < TentFootprint.HEIGHT; y++) {
            for (int x = 0; x < TentFootprint.WIDTH; x++) {
                for (int z = 0; z < TentFootprint.DEPTH; z++) {
                    final BlockPos pos = new BlockPos(originX + x, originY + y, originZ + z);
                    if (pos.equals(doorPos)) {
                        continue;
                    }
                    final BlockState state = level.getBlockState(pos);
                    if (!state.is(ModBlocks.TENT_FABRIC.get())) {
                        continue;
                    }
                    if (state.getValue(TentFabricBlock.ANIM_STAGE) != animStage
                            || state.getValue(TentFabricBlock.FACING) != facing) {
                        level.setBlock(
                                pos,
                                state.setValue(TentFabricBlock.FACING, facing)
                                        .setValue(TentFabricBlock.ANIM_STAGE, animStage)
                                        .setValue(TentFabricBlock.ROLE, state.getValue(TentFabricBlock.ROLE)),
                                3);
                    }
                }
            }
        }
    }

    private static void clearExterior(final ServerLevel level, final int ox, final int oy, final int oz) {
        for (int y = 0; y < TentFootprint.HEIGHT; y++) {
            for (int x = 0; x < TentFootprint.WIDTH; x++) {
                for (int z = 0; z < TentFootprint.DEPTH; z++) {
                    final BlockPos pos = new BlockPos(ox + x, oy + y, oz + z);
                    final BlockState state = level.getBlockState(pos);
                    if (state.is(ModBlocks.CAMPING_TENT.get()) || state.is(ModBlocks.TENT_FABRIC.get())) {
                        level.removeBlock(pos, false);
                    }
                }
            }
        }
    }

    private static void playTransition(final ServerPlayer player) {
        PacketDistributor.sendToPlayer(
                player, new TentTransitionPayload(TentTransition.BLACK_SCREEN_TICKS));
        player.level().playSound(
                null,
                player.blockPosition(),
                ModSounds.TENT_ZIPPER.value(),
                SoundSource.PLAYERS,
                1.0F,
                1.0F);
    }

    /**
     * Spawn position inside a room for GameTests.
     *
     * @param roomIndex room index
     * @return block pos on the floor
     */
    @NotNull
    public static BlockPos interiorSpawnPos(final int roomIndex) {
        final int[] origin = TentInteriorBounds.roomOrigin(roomIndex, CampingDimension.ROOM_GAP);
        return new BlockPos(
                origin[0] + TentInteriorBounds.shellWidth() / 2,
                origin[1] + 1,
                origin[2] + 2);
    }
}

