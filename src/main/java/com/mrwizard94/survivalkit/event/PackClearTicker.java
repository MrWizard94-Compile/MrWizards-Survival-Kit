package com.mrwizard94.survivalkit.event;

import com.mrwizard94.survivalkit.dimension.PendingPackClears;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Drives {@link PendingPackClears} so pack-up clears survive controller BE churn.
 */
public final class PackClearTicker {

    /**
     * Level tick.
     *
     * @param event event
     */
    @SubscribeEvent
    public void onLevelTick(@NotNull final LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            PendingPackClears.tickLevel(level);
        }
    }
}
