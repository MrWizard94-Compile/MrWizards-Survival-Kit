package com.mrwizard94.survivalkit.dimension;

import com.mrwizard94.survivalkit.logic.TentTransition;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Server-side scheduled exterior clears after pack animation.
 *
 * <p>Independent of controller block-entity survival across {@code setBlock} anim updates,
 * so multi-tent pack-up always finishes clearing each footprint.
 */
public final class PendingPackClears {

    private static final List<Entry> PENDING = new ArrayList<>();

    private PendingPackClears() {
    }

    /**
     * Schedules a footprint clear after {@link TentTransition#PACK_ANIM_TICKS} level ticks.
     *
     * @param level exterior level
     * @param ox    footprint origin X
     * @param oy    footprint origin Y
     * @param oz    footprint origin Z
     */
    public static void schedule(
            @NotNull final ServerLevel level, final int ox, final int oy, final int oz) {
        PENDING.add(new Entry(level.dimension(), ox, oy, oz, TentTransition.PACK_ANIM_TICKS));
    }

    /**
     * Decrements countdowns for this level and clears due footprints.
     *
     * @param level server level
     */
    public static void tickLevel(@NotNull final ServerLevel level) {
        if (PENDING.isEmpty()) {
            return;
        }
        final ResourceKey<Level> dim = level.dimension();
        final Iterator<Entry> it = PENDING.iterator();
        while (it.hasNext()) {
            final Entry entry = it.next();
            if (!Objects.equals(entry.dimension(), dim)) {
                continue;
            }
            final int left = entry.ticksRemaining() - 1;
            if (left > 0) {
                entry.ticksRemaining(left);
                continue;
            }
            TentServices.finishPackClear(level, entry.ox(), entry.oy(), entry.oz());
            it.remove();
        }
    }

    /**
     * Drops pending entries whose dimension is gone (server stop / test teardown).
     *
     * @param server server
     */
    public static void purgeMissingDimensions(@NotNull final MinecraftServer server) {
        PENDING.removeIf(entry -> server.getLevel(entry.dimension()) == null);
    }

    /**
     * Reports how many pack clears are waiting across all dimensions.
     *
     * @return pending clear count (all dimensions)
     */
    public static int pendingCount() {
        return PENDING.size();
    }

    /**
     * Clears all pending work (GameTest isolation).
     */
    public static void clearAllForTests() {
        PENDING.clear();
    }

    private static final class Entry {
        private final ResourceKey<Level> dimension;
        private final int ox;
        private final int oy;
        private final int oz;
        private int ticksRemaining;

        private Entry(
                final ResourceKey<Level> dimension,
                final int ox,
                final int oy,
                final int oz,
                final int ticksRemaining) {
            this.dimension = dimension;
            this.ox = ox;
            this.oy = oy;
            this.oz = oz;
            this.ticksRemaining = ticksRemaining;
        }

        private ResourceKey<Level> dimension() {
            return dimension;
        }

        private int ox() {
            return ox;
        }

        private int oy() {
            return oy;
        }

        private int oz() {
            return oz;
        }

        private int ticksRemaining() {
            return ticksRemaining;
        }

        private void ticksRemaining(final int value) {
            this.ticksRemaining = value;
        }
    }
}
