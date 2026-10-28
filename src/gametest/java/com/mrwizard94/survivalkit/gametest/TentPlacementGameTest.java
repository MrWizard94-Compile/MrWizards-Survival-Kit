package com.mrwizard94.survivalkit.gametest;

import com.mrwizard94.survivalkit.block.CampingTentBlock;
import com.mrwizard94.survivalkit.block.TentFabricBlock;
import com.mrwizard94.survivalkit.blockentity.CampingTentBlockEntity;
import com.mrwizard94.survivalkit.dimension.PendingPackClears;
import com.mrwizard94.survivalkit.dimension.TentSavedData;
import com.mrwizard94.survivalkit.dimension.TentServices;
import com.mrwizard94.survivalkit.item.CampingTentItem;
import com.mrwizard94.survivalkit.logic.TentFootprint;
import com.mrwizard94.survivalkit.logic.TentTransition;
import com.mrwizard94.survivalkit.registry.ModBlocks;
import com.mrwizard94.survivalkit.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Placement clearance + pack-up structure GameTests.
 */
@GameTestHolder("mrwizardssurvivalkit")
public final class TentPlacementGameTest {

    private TentPlacementGameTest() {
    }

    /**
     * Clear 3×4×3 volume accepts pitch.
     *
     * @param helper helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 60)
    public static void pitchSucceedsWhenClear(@NotNull final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        clearVolume(level, origin);
        final UUID tentId = UUID.randomUUID();
        final Direction facing = Direction.SOUTH;
        final BlockPos door = CampingTentItem.computeDoorPos(
                origin.getX(), origin.getY(), origin.getZ(), facing);
        TentServices.pitchTent(
                level, tentId, origin.getX(), origin.getY(), origin.getZ(), facing, door);
        if (!level.getBlockState(door).is(ModBlocks.CAMPING_TENT.get())) {
            helper.fail("Door controller missing");
            return;
        }
        if (TentSavedData.get(level).find(tentId).isEmpty()) {
            helper.fail("Binding missing");
            return;
        }
        helper.succeed();
    }

    /**
     * Occupied volume rejects clearance probe used by placement.
     *
     * @param helper helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 40)
    public static void clearanceRejectsBlockedCell(@NotNull final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        clearVolume(level, origin);
        level.setBlock(origin.offset(1, 1, 1), Blocks.STONE.defaultBlockState(), 3);
        final boolean clear = TentFootprint.hasClearance(
                origin.getX(),
                origin.getY(),
                origin.getZ(),
                (x, y, z) -> level.getBlockState(new BlockPos(x, y, z)).canBeReplaced());
        if (clear) {
            helper.fail("Expected clearance failure");
            return;
        }
        helper.succeed();
    }

    /**
     * Pack-up returns the tent item immediately, but clears exterior only after pack animation.
     *
     * @param helper helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 120)
    public static void packUpRemovesExteriorAfterAnim(@NotNull final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        clearVolume(level, origin);
        final UUID tentId = UUID.randomUUID();
        final Direction facing = Direction.SOUTH;
        final BlockPos door = CampingTentItem.computeDoorPos(
                origin.getX(), origin.getY(), origin.getZ(), facing);
        TentServices.pitchTent(
                level, tentId, origin.getX(), origin.getY(), origin.getZ(), facing, door);
        final ItemStack stacked = TentServices.packUp(level, door, tentId);
        if (!stacked.is(ModItems.CAMPING_TENT.get())) {
            helper.fail("Tent item not returned");
            return;
        }
        if (level.getBlockState(door).isAir()) {
            helper.fail("Exterior cleared before pack animation finished");
            return;
        }
        final BlockEntity be = level.getBlockEntity(door);
        if (!(be instanceof CampingTentBlockEntity tent) || !tent.isPacking()) {
            helper.fail("Controller should be packing");
            return;
        }
        for (int i = 0; i <= TentTransition.PACK_ANIM_TICKS + 2; i++) {
            final BlockEntity current = level.getBlockEntity(door);
            if (current instanceof CampingTentBlockEntity controller) {
                CampingTentBlockEntity.tick(level, door, level.getBlockState(door), controller);
            }
            PendingPackClears.tickLevel(level);
        }
        if (!level.getBlockState(door).isAir()) {
            helper.fail("Door still present after pack animation");
            return;
        }
        helper.succeed();
    }

    /**
     * Four sequential pitch→pack cycles each fully clear their footprint after anim ticks.
     *
     * @param helper helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 400)
    public static void multiTentPackClearsEachFootprint(@NotNull final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        PendingPackClears.clearAllForTests();
        final Direction facing = Direction.SOUTH;
        final BlockPos[] origins = new BlockPos[]{
                helper.absolutePos(new BlockPos(1, 2, 1)),
                helper.absolutePos(new BlockPos(5, 2, 1)),
                helper.absolutePos(new BlockPos(1, 2, 6)),
                helper.absolutePos(new BlockPos(5, 2, 6))
        };
        final BlockPos[] doors = new BlockPos[origins.length];
        final UUID[] ids = new UUID[origins.length];
        for (int t = 0; t < origins.length; t++) {
            clearVolume(level, origins[t]);
            ids[t] = UUID.randomUUID();
            doors[t] = CampingTentItem.computeDoorPos(
                    origins[t].getX(), origins[t].getY(), origins[t].getZ(), facing);
            TentServices.pitchTent(
                    level,
                    ids[t],
                    origins[t].getX(),
                    origins[t].getY(),
                    origins[t].getZ(),
                    facing,
                    doors[t]);
        }
        for (int t = 0; t < origins.length; t++) {
            final ItemStack stacked = TentServices.packUp(level, doors[t], ids[t]);
            if (!stacked.is(ModItems.CAMPING_TENT.get())) {
                helper.fail("Pack " + t + " did not return tent item");
                return;
            }
            for (int i = 0; i <= TentTransition.PACK_ANIM_TICKS + 2; i++) {
                final BlockEntity current = level.getBlockEntity(doors[t]);
                if (current instanceof CampingTentBlockEntity controller) {
                    CampingTentBlockEntity.tick(
                            level, doors[t], level.getBlockState(doors[t]), controller);
                }
                PendingPackClears.tickLevel(level);
            }
            if (!footprintIsAir(level, origins[t])) {
                helper.fail("Footprint " + t + " still has tent blocks after pack anim");
                return;
            }
        }
        helper.succeed();
    }

    private static boolean footprintIsAir(final ServerLevel level, final BlockPos origin) {
        for (int y = 0; y < TentFootprint.HEIGHT; y++) {
            for (int x = 0; x < TentFootprint.WIDTH; x++) {
                for (int z = 0; z < TentFootprint.DEPTH; z++) {
                    final var state = level.getBlockState(origin.offset(x, y, z));
                    if (state.is(ModBlocks.CAMPING_TENT.get()) || state.is(ModBlocks.TENT_FABRIC.get())) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Exterior entrance is two blocks tall (controller + upper ENTRANCE fabric).
     *
     * @param helper helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 60)
    public static void exteriorEntranceIsFullHeight(@NotNull final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        clearVolume(level, origin);
        final UUID tentId = UUID.randomUUID();
        final Direction facing = Direction.SOUTH;
        final BlockPos door = CampingTentItem.computeDoorPos(
                origin.getX(), origin.getY(), origin.getZ(), facing);
        TentServices.pitchTent(
                level, tentId, origin.getX(), origin.getY(), origin.getZ(), facing, door);
        if (!level.getBlockState(door).is(ModBlocks.CAMPING_TENT.get())) {
            helper.fail("Lower entrance controller missing");
            return;
        }
        final BlockPos upper = door.above();
        if (!level.getBlockState(upper).is(ModBlocks.TENT_FABRIC.get())) {
            helper.fail("Upper entrance fabric missing");
            return;
        }
        if (level.getBlockState(upper).getValue(TentFabricBlock.ROLE)
                != com.mrwizard94.survivalkit.block.TentFabricRole.ENTRANCE) {
            helper.fail("Upper cell must be ENTRANCE role");
            return;
        }
        helper.succeed();
    }

    /**
     * Fabric parts receive ANIM_STAGE updates from the controller during pitch.
     *
     * @param helper helper
     */
    @GameTest(template = "empty_platform", timeoutTicks = 120)
    public static void fabricAnimStagesSyncFromController(@NotNull final GameTestHelper helper) {
        final ServerLevel level = helper.getLevel();
        final BlockPos origin = helper.absolutePos(new BlockPos(1, 2, 1));
        clearVolume(level, origin);
        final UUID tentId = UUID.randomUUID();
        final Direction facing = Direction.SOUTH;
        final BlockPos door = CampingTentItem.computeDoorPos(
                origin.getX(), origin.getY(), origin.getZ(), facing);
        TentServices.pitchTent(
                level, tentId, origin.getX(), origin.getY(), origin.getZ(), facing, door);
        final BlockPos fabricPos = origin.offset(0, 0, 0).equals(door)
                ? origin.offset(0, 1, 0)
                : origin;
        final BlockEntity be = level.getBlockEntity(door);
        if (!(be instanceof CampingTentBlockEntity controller)) {
            helper.fail("Missing controller");
            return;
        }
        for (int i = 0; i <= TentTransition.PITCH_ANIM_TICKS + 2; i++) {
            CampingTentBlockEntity.tick(level, door, level.getBlockState(door), controller);
        }
        final var fabricState = level.getBlockState(fabricPos);
        if (!fabricState.is(ModBlocks.TENT_FABRIC.get())) {
            helper.fail("Expected fabric at " + fabricPos);
            return;
        }
        if (fabricState.getValue(TentFabricBlock.ANIM_STAGE) != 3) {
            helper.fail("Fabric anim_stage should be 3 after pitch, was "
                    + fabricState.getValue(TentFabricBlock.ANIM_STAGE));
            return;
        }
        if (level.getBlockState(door).getValue(CampingTentBlock.ANIM_STAGE) != 3) {
            helper.fail("Controller anim_stage should be 3 after pitch");
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
