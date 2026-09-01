package com.mrwizard94.survivalkit.event;

import com.mrwizard94.survivalkit.blockentity.CampingTentBlockEntity;
import com.mrwizard94.survivalkit.dimension.TentSavedData;
import com.mrwizard94.survivalkit.item.CampingTentItem;
import com.mrwizard94.survivalkit.logic.HostileRepelRules;
import com.mrwizard94.survivalkit.logic.TentRoomBinding;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Periodically repels hostiles within 16 blocks of pitched tent controllers.
 *
 * <p>Controllers are discovered from {@link TentSavedData} bindings (not a coarse block lattice).
 */
public final class MobRepelHandler {

    private static final int INTERVAL = 10;

    /**
     * Level tick.
     *
     * @param event event
     */
    @SubscribeEvent
    public void onLevelTick(@NotNull final LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (level.getGameTime() % INTERVAL != 0) {
            return;
        }
        repelKnownControllers(level);
    }

    /**
     * Runs the shipped repel path for every known pitched controller in this level.
     *
     * @param level server level
     */
    public static void repelKnownControllers(@NotNull final ServerLevel level) {
        final TentSavedData data = TentSavedData.get(level);
        final String dimId = level.dimension().location().toString();
        for (final Map.Entry<UUID, TentRoomBinding> entry : data.state().snapshot().entrySet()) {
            final TentRoomBinding binding = entry.getValue();
            if (!dimId.equals(binding.exteriorDim())) {
                continue;
            }
            final Direction facing = Direction.from2DDataValue(binding.facing());
            final BlockPos door = CampingTentItem.computeDoorPos(
                    binding.exteriorX(), binding.exteriorY(), binding.exteriorZ(), facing);
            if (!level.isLoaded(door)) {
                continue;
            }
            final BlockEntity be = level.getBlockEntity(door);
            if (!(be instanceof CampingTentBlockEntity tent) || !tent.isController()) {
                continue;
            }
            repelAround(level, door);
        }
    }

    /**
     * Repels hostiles around a single pitched controller. Public for GameTests.
     *
     * @param level   level
     * @param tentPos controller door position
     */
    public static void repelAround(@NotNull final ServerLevel level, @NotNull final BlockPos tentPos) {
        final double cx = tentPos.getX() + 0.5;
        final double cy = tentPos.getY() + 0.5;
        final double cz = tentPos.getZ() + 0.5;
        final AABB box = new AABB(tentPos).inflate(HostileRepelRules.RADIUS_BLOCKS);
        final List<Mob> mobs = level.getEntitiesOfClass(Mob.class, box, mob -> mob instanceof Enemy);
        for (final Mob mob : mobs) {
            if (!HostileRepelRules.shouldRepel(cx, cy, cz, mob.getX(), mob.getY(), mob.getZ())) {
                continue;
            }
            final double[] push = HostileRepelRules.pushAway(cx, cz, mob.getX(), mob.getZ());
            mob.setDeltaMovement(mob.getDeltaMovement().add(push[0] * 0.45, 0.05, push[1] * 0.45));
            mob.hurtMarked = true;
            mob.setTarget(null);
        }
    }
}
