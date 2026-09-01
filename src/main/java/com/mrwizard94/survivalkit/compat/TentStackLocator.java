package com.mrwizard94.survivalkit.compat;

import com.mrwizard94.survivalkit.registry.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

/**
 * Locates a camping tent in inventory or (when present) Curios slots.
 */
public final class TentStackLocator {

    private TentStackLocator() {
    }

    /**
     * Prefers hotbar/inventory, then Curios if loaded.
     *
     * @param player player
     * @return tent stack or empty
     */
    @NotNull
    public static ItemStack findTent(@NotNull final Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            final ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(ModItems.CAMPING_TENT.get())) {
                return stack;
            }
        }
        if (ModList.get().isLoaded("curios")) {
            return CuriosTentAccess.findEquippedTent(player);
        }
        return ItemStack.EMPTY;
    }
}
