package com.mrwizard94.survivalkit.gametest;

import com.mrwizard94.survivalkit.dimension.CampingDimension;
import com.mrwizard94.survivalkit.dimension.CampingDimensionBootstrap;
import com.mrwizard94.survivalkit.dimension.PendingPackClears;
import com.mrwizard94.survivalkit.dimension.TentSavedData;
import com.mrwizard94.survivalkit.dimension.TentServices;
import com.mrwizard94.survivalkit.event.MobRepelHandler;
import com.mrwizard94.survivalkit.item.CampingTentItem;
import com.mrwizard94.survivalkit.logic.DoorAction;
import com.mrwizard94.survivalkit.logic.HostileRepelRules;
import com.mrwizard94.survivalkit.logic.LeaveReturnPose;
import com.mrwizard94.survivalkit.logic.TentFootprint;
import com.mrwizard94.survivalkit.logic.TentInteriorBounds;
import com.mrwizard94.survivalkit.logic.TentRoomBinding;
import com.mrwizard94.survivalkit.logic.TentTransition;
import com.mrwizard94.survivalkit.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Pocket room, leave pose, sealing, barrel, camping dim, and repel GameTests.
 */
@GameTestHolder("mrwizardssurvivalkit")
public final class TentPocketGameTest {

    private TentPocketGameTest() {
    }

    /**
     * Room seeds furniture, barrel (not chest), usable bed, custom entrance, unbreakable shell.
     *
     * @param helper helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 120)
    public static void roomSeedHasInteriorSizeAndFurniture(@NotNull final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        CampingDimensionBootstrap.ensureCampingLevel(level.getServer());
        final UUID tentId = UUID.randomUUID();
        final BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        clearVolume(level, origin);
        final Direction facing = Direction.SOUTH;
        final BlockPos door = CampingTentItem.computeDoorPos(
                origin.getX(), origin.getY(), origin.getZ(), facing);
        TentServices.pitchTent(
                level, tentId, origin.getX(), origin.getY(), origin.getZ(), facing, door);

        final TentRoomBinding binding = TentSavedData.get(level).find(tentId).orElse(null);
        if (binding == null || !binding.seeded()) {
            helper.fail("Binding missing or unseeded");
            return;
        }
        if (TentInteriorBounds.WIDTH != 5 || TentInteriorBounds.DEPTH != 6 || TentInteriorBounds.HEIGHT != 4) {
            helper.fail("Interior bounds mismatch");
            return;
        }

        final ServerLevel camping = CampingDimension.resolveHost(level.getServer());
        if (!camping.dimension().equals(CampingDimension.LEVEL_KEY)) {
            helper.fail("Camping host must be mrwizardssurvivalkit:camping");
            return;
        }
        final int[] roomOrigin = TentServices.roomOriginFor(camping, binding.roomIndex());
        // Wall layout: furnace/craft on −X wall; barrel on +X facing WEST (center).
        final BlockPos furnace = new BlockPos(roomOrigin[0] + 1, roomOrigin[1] + 1, roomOrigin[2] + 2);
        if (!camping.getBlockState(furnace).is(Blocks.FURNACE)) {
            helper.fail("Furnace missing on -X wall");
            return;
        }
        if (camping.getBlockState(furnace).getValue(
                net.minecraft.world.level.block.FurnaceBlock.FACING) != Direction.EAST) {
            helper.fail("Furnace must face room center (EAST)");
            return;
        }
        final BlockPos craft = new BlockPos(roomOrigin[0] + 1, roomOrigin[1] + 1, roomOrigin[2] + 3);
        if (!camping.getBlockState(craft).is(Blocks.CRAFTING_TABLE)) {
            helper.fail("Crafting table missing on -X wall");
            return;
        }
        final BlockPos barrel = new BlockPos(roomOrigin[0] + 5, roomOrigin[1] + 1, roomOrigin[2] + 3);
        if (!camping.getBlockState(barrel).is(Blocks.BARREL)) {
            helper.fail("Barrel missing (chest must not be used)");
            return;
        }
        if (camping.getBlockState(barrel).getValue(
                net.minecraft.world.level.block.BarrelBlock.FACING) != Direction.WEST) {
            helper.fail("Barrel must face room center (WEST)");
            return;
        }
        if (camping.getBlockState(barrel).is(Blocks.CHEST)) {
            helper.fail("Chest must not be seeded");
            return;
        }
        final BlockPos bedFoot = new BlockPos(roomOrigin[0] + 3, roomOrigin[1] + 1, roomOrigin[2] + 5);
        if (!TentServices.isUsableBed(camping, bedFoot)) {
            helper.fail("Seeded bed must be usable FOOT+HEAD");
            return;
        }
        final int sw = TentInteriorBounds.shellWidth();
        final BlockPos entrance = new BlockPos(roomOrigin[0] + sw / 2, roomOrigin[1] + 1, roomOrigin[2]);
        if (!camping.getBlockState(entrance).is(ModBlocks.TENT_ENTRANCE.get())) {
            helper.fail("Interior exit must be custom tent entrance, not spruce door");
            return;
        }
        final BlockPos entranceAbove = entrance.above();
        if (camping.getBlockState(entranceAbove).isAir()
                || camping.getBlockState(entranceAbove).canBeReplaced()) {
            helper.fail("Block above tent entrance must not be air/traversable (void seal)");
            return;
        }
        if (!camping.getBlockState(entranceAbove).is(ModBlocks.TENT_ENTRANCE.get())
                && !camping.getBlockState(entranceAbove).is(ModBlocks.TENT_SHELL.get())) {
            helper.fail("Block above tent entrance must be unbreakable entrance/shell");
            return;
        }
        final BlockPos shell = new BlockPos(roomOrigin[0], roomOrigin[1] + 2, roomOrigin[2] + 3);
        if (!camping.getBlockState(shell).is(ModBlocks.TENT_SHELL.get())) {
            helper.fail("Interior walls must be tent shell");
            return;
        }
        final Player mock = helper.makeMockPlayer(GameType.SURVIVAL);
        if (camping.getBlockState(shell).getDestroyProgress(mock, camping, shell) > 0.0F) {
            helper.fail("Interior shell must be unbreakable in survival progress");
            return;
        }
        if (camping.getBlockState(entranceAbove).getDestroyProgress(mock, camping, entranceAbove) > 0.0F) {
            helper.fail("Block above entrance must be unbreakable");
            return;
        }
        helper.succeed();
    }

    /**
     * Drives shipped enter/leave/pack paths: pack via {@link DoorAction#PACK_UP} must not black-screen;
     * {@link TentServices#leaveTent} with stale A return coords while inside tent B lands at B's door
     * after A was packed.
     *
     * @param helper helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 240)
    @SuppressWarnings("removal") // GameTestHelper.makeMockServerPlayerInLevel is the only ServerPlayer factory here
    public static void leaveTentIgnoresStaleCoordsAfterNeighborPacked(
            @NotNull final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        PendingPackClears.clearAllForTests();
        TentServices.resetBlackScreenProbeForTest();
        CampingDimensionBootstrap.ensureCampingLevel(level.getServer());
        final Direction facing = Direction.SOUTH;
        final BlockPos originA = helper.absolutePos(new BlockPos(1, 2, 1));
        final BlockPos originB = helper.absolutePos(new BlockPos(5, 2, 1));
        clearVolume(level, originA);
        clearVolume(level, originB);
        final UUID idA = UUID.randomUUID();
        final UUID idB = UUID.randomUUID();
        final BlockPos doorA = CampingTentItem.computeDoorPos(
                originA.getX(), originA.getY(), originA.getZ(), facing);
        final BlockPos doorB = CampingTentItem.computeDoorPos(
                originB.getX(), originB.getY(), originB.getZ(), facing);
        TentServices.pitchTent(
                level, idA, originA.getX(), originA.getY(), originA.getZ(), facing, doorA);
        TentServices.pitchTent(
                level, idB, originB.getX(), originB.getY(), originB.getZ(), facing, doorB);

        final ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        // GameTestServer mock connections do not negotiate play-to-client payloads; probe still records.
        TentServices.installBlackScreenDispatcherForTest(ignored -> {
        });
        try {
            // Pack A on the exterior DoorAction path (same as TentDoorScreen); must not black-screen.
            TentServices.resetBlackScreenProbeForTest();
            TentServices.handleDoorAction(player, doorA, idA, DoorAction.PACK_UP);
            if (TentServices.lastBlackScreenTicksSentForTest() != -1) {
                helper.fail("DoorAction.PACK_UP / packUp(player) must not call playTransition");
                return;
            }
            for (int i = 0; i <= TentTransition.PACK_ANIM_TICKS + 2; i++) {
                PendingPackClears.tickLevel(level);
            }

            // Enter remaining tent B, then poison cached return coords with packed tent A's pose.
            TentServices.resetBlackScreenProbeForTest();
            TentServices.enterTent(player, idB);
            if (TentServices.lastBlackScreenTicksSentForTest() != TentTransition.BLACK_SCREEN_TICKS) {
                helper.fail("enterTent must request black-screen via playTransition");
                return;
            }
            if (!player.getPersistentData().getBoolean("mwsk_inside")
                    || !idB.equals(player.getPersistentData().getUUID("mwsk_tent"))) {
                helper.fail("enterTent must mark player inside tent B");
                return;
            }
            final LeaveReturnPose.Pose staleA = LeaveReturnPose.compute(
                    originA.getX(), originA.getY(), originA.getZ(), facing.get2DDataValue());
            player.getPersistentData().putDouble("mwsk_ret_x", staleA.x());
            player.getPersistentData().putDouble("mwsk_ret_y", staleA.y());
            player.getPersistentData().putDouble("mwsk_ret_z", staleA.z());

            TentServices.resetBlackScreenProbeForTest();
            TentServices.leaveTent(player);
            if (TentServices.lastBlackScreenTicksSentForTest() != TentTransition.BLACK_SCREEN_TICKS) {
                helper.fail("leaveTent must request black-screen via playTransition");
                return;
            }
            if (player.getPersistentData().getBoolean("mwsk_inside")) {
                helper.fail("leaveTent must clear mwsk_inside");
                return;
            }

            final double px = player.getX();
            final double pz = player.getZ();
            final int bx = (int) Math.floor(px);
            final int bz = (int) Math.floor(pz);
            if (LeaveReturnPose.isInsideFootprint(bx, bz, originA.getX(), originA.getZ())) {
                helper.fail("leaveTent landed in packed tent A footprint (stale cache used): "
                        + px + "," + pz);
                return;
            }
            if (LeaveReturnPose.isInsideFootprint(bx, bz, originB.getX(), originB.getZ())) {
                helper.fail("leaveTent must land outside active tent B footprint");
                return;
            }
            final double distB = Math.abs(px - (doorB.getX() + 0.5))
                    + Math.abs(pz - (doorB.getZ() + 0.5));
            final double distA = Math.abs(px - (doorA.getX() + 0.5))
                    + Math.abs(pz - (doorA.getZ() + 0.5));
            if (distB >= distA) {
                helper.fail("leaveTent must land nearer door B than packed door A (got distB="
                        + distB + " distA=" + distA + ")");
                return;
            }
            helper.succeed();
        } finally {
            TentServices.installBlackScreenDispatcherForTest(null);
        }
    }

    /**
     * Leave pose stored on enter is outside the exterior footprint.
     *
     * @param helper helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 80)
    public static void enterStoresLeavePoseOutsideFootprint(@NotNull final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        CampingDimensionBootstrap.ensureCampingLevel(level.getServer());
        final BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        clearVolume(level, origin);
        final UUID tentId = UUID.randomUUID();
        final Direction facing = Direction.SOUTH;
        final BlockPos door = CampingTentItem.computeDoorPos(
                origin.getX(), origin.getY(), origin.getZ(), facing);
        TentServices.pitchTent(
                level, tentId, origin.getX(), origin.getY(), origin.getZ(), facing, door);
        final LeaveReturnPose.Pose pose = LeaveReturnPose.compute(
                origin.getX(), origin.getY(), origin.getZ(), facing.get2DDataValue());
        final int bx = (int) Math.floor(pose.x());
        final int bz = (int) Math.floor(pose.z());
        if (LeaveReturnPose.isInsideFootprint(bx, bz, origin.getX(), origin.getZ())) {
            helper.fail("Leave pose inside footprint: " + pose);
            return;
        }
        helper.succeed();
    }

    /**
     * Exterior tent destroy progress is zero (pack-up only).
     *
     * @param helper helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 60)
    public static void exteriorTentUnbreakableInSurvival(@NotNull final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        clearVolume(level, origin);
        final UUID tentId = UUID.randomUUID();
        final Direction facing = Direction.SOUTH;
        final BlockPos door = CampingTentItem.computeDoorPos(
                origin.getX(), origin.getY(), origin.getZ(), facing);
        TentServices.pitchTent(
                level, tentId, origin.getX(), origin.getY(), origin.getZ(), facing, door);
        final Player mock = helper.makeMockPlayer(GameType.SURVIVAL);
        if (level.getBlockState(door).getDestroyProgress(mock, level, door) > 0.0F) {
            helper.fail("Controller must be unbreakable");
            return;
        }
        final BlockPos fabric = origin.equals(door) ? origin.offset(0, 1, 1) : origin;
        if (level.getBlockState(fabric).is(ModBlocks.TENT_FABRIC.get())
                && level.getBlockState(fabric).getDestroyProgress(mock, level, fabric) > 0.0F) {
            helper.fail("Fabric must be unbreakable");
            return;
        }
        helper.succeed();
    }

    /**
     * Force-created camping dimension is available.
     *
     * @param helper helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void campingDimensionIsLoaded(@NotNull final GameTestHelper helper) {
        final ServerLevel camping = CampingDimensionBootstrap.ensureCampingLevel(helper.getLevel().getServer());
        if (!camping.dimension().equals(CampingDimension.LEVEL_KEY)) {
            helper.fail("Expected camping dimension key");
            return;
        }
        helper.succeed();
    }

    /**
     * Shipped MobRepelHandler path pushes hostiles near an off-lattice pitched door.
     *
     * @param helper helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 80)
    public static void mobRepelHandlerPushesHostileNearOffLatticeDoor(
            @NotNull final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final BlockPos origin = helper.absolutePos(new BlockPos(2, 2, 3));
        clearVolume(level, origin);
        final UUID tentId = UUID.randomUUID();
        final Direction facing = Direction.SOUTH;
        final BlockPos door = CampingTentItem.computeDoorPos(
                origin.getX(), origin.getY(), origin.getZ(), facing);
        TentServices.pitchTent(
                level, tentId, origin.getX(), origin.getY(), origin.getZ(), facing, door);
        final BlockPos relativeZombie = helper.relativePos(door.offset(3, 0, 0));
        final Zombie zombie = helper.spawn(EntityType.ZOMBIE, relativeZombie);
        final Vec3 before = zombie.position();
        if (!HostileRepelRules.shouldRepel(
                door.getX() + 0.5, door.getY() + 0.5, door.getZ() + 0.5,
                before.x, before.y, before.z)) {
            helper.fail("Zombie should be inside 16-block radius");
            return;
        }
        MobRepelHandler.repelKnownControllers(level);
        final Vec3 delta = zombie.getDeltaMovement();
        if (Math.abs(delta.x) <= 0.01 && Math.abs(delta.z) <= 0.01) {
            helper.fail("MobRepelHandler did not push zombie");
            return;
        }
        helper.succeed();
    }

    /**
     * Re-pitch of same tent id keeps the same room index.
     *
     * @param helper helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 120)
    public static void rePitchReusesRoomIndex(@NotNull final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        CampingDimensionBootstrap.ensureCampingLevel(level.getServer());
        final UUID tentId = UUID.randomUUID();
        final BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        clearVolume(level, origin);
        final Direction facing = Direction.SOUTH;
        final BlockPos door = CampingTentItem.computeDoorPos(
                origin.getX(), origin.getY(), origin.getZ(), facing);
        TentServices.pitchTent(
                level, tentId, origin.getX(), origin.getY(), origin.getZ(), facing, door);
        final TentRoomBinding first = TentSavedData.get(level).find(tentId).orElse(null);
        if (first == null) {
            helper.fail("First binding missing");
            return;
        }
        final int room = first.roomIndex();
        TentServices.packUp(level, door, tentId);
        TentServices.finishPackClear(level, origin.getX(), origin.getY(), origin.getZ());
        TentServices.pitchTent(
                level, tentId, origin.getX(), origin.getY(), origin.getZ(), facing, door);
        final TentRoomBinding second = TentSavedData.get(level).find(tentId).orElse(null);
        if (second == null || second.roomIndex() != room) {
            helper.fail("Room index should persist across pack/pitch");
            return;
        }
        helper.succeed();
    }

    private static void clearVolume(final ServerLevel level, final BlockPos origin) {
        for (int y = 0; y < TentFootprint.HEIGHT; y++) {
            for (int x = 0; x < TentFootprint.WIDTH; x++) {
                for (int z = 0; z < TentFootprint.DEPTH; z++) {
                    level.setBlock(origin.offset(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
    }
}
