package com.mrwizard94.survivalkit.event;

import com.mrwizard94.survivalkit.config.SurvivalKitConfig;
import com.mrwizard94.survivalkit.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Optionally grants a camping tent once on first world join.
 */
public final class FirstJoinHandler {

    /** Persistent data flag — set after the first-join gift is granted. */
    public static final String TAG_RECEIVED_STARTER_TENT = "mwsk_received_starter_tent";

    /**
     * Player logged-in hook.
     *
     * @param event event
     */
    @SubscribeEvent
    public void onPlayerLoggedIn(@NotNull final PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!SurvivalKitConfig.get().shouldGiveTentOnFirstJoin()) {
            return;
        }
        final CompoundTag data = player.getPersistentData();
        if (data.getBoolean(TAG_RECEIVED_STARTER_TENT)) {
            return;
        }
        final ItemStack tent = new ItemStack(ModItems.CAMPING_TENT.get());
        if (!player.getInventory().add(tent)) {
            player.drop(tent, false);
        }
        data.putBoolean(TAG_RECEIVED_STARTER_TENT, true);
    }
}
