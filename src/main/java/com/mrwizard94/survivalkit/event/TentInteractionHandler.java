package com.mrwizard94.survivalkit.event;

import com.mrwizard94.survivalkit.block.CampingTentBlock;
import com.mrwizard94.survivalkit.block.TentEntranceBlock;
import com.mrwizard94.survivalkit.block.TentFabricBlock;
import com.mrwizard94.survivalkit.blockentity.CampingTentBlockEntity;
import com.mrwizard94.survivalkit.dimension.CampingDimension;
import com.mrwizard94.survivalkit.dimension.TentServices;
import com.mrwizard94.survivalkit.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Door interaction: exterior menu, interior leave via custom entrance.
 */
public final class TentInteractionHandler {

    /**
     * Right-click block handler.
     *
     * @param event event
     */
    @SubscribeEvent
    public void onRightClickBlock(@NotNull final PlayerInteractEvent.RightClickBlock event) {
        final Level level = event.getLevel();
        if (level.isClientSide || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        final BlockPos pos = event.getPos();
        final BlockState state = level.getBlockState(pos);

        if (CampingDimension.isCampingHost(level)
                && state.is(ModBlocks.TENT_ENTRANCE.get())
                && player.getPersistentData().getBoolean("mwsk_inside")) {
            TentServices.leaveTent(player);
            event.setCanceled(true);
            return;
        }

        if (state.getBlock() instanceof CampingTentBlock
                || state.getBlock() instanceof TentFabricBlock
                || state.getBlock() instanceof TentEntranceBlock) {
            if (state.getBlock() instanceof TentEntranceBlock) {
                return;
            }
            final BlockEntity be = level.getBlockEntity(pos);
            if (!(be instanceof CampingTentBlockEntity tent)) {
                return;
            }
            final BlockPos controllerPos = tent.isController() ? pos : tent.getControllerPos();
            final BlockEntity controllerBe = level.getBlockEntity(controllerPos);
            if (!(controllerBe instanceof CampingTentBlockEntity controller)) {
                return;
            }
            TentServices.openDoorMenu(player, controllerPos, controller.getTentId());
            event.setCanceled(true);
        }
    }
}
