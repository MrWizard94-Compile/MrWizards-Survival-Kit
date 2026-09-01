package com.mrwizard94.survivalkit.compat;

import com.mrwizard94.survivalkit.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.Map;
import java.util.Optional;

/**
 * Soft Curios access for an equipped camping tent.
 *
 * <p>Compile-only against Curios; callers must guard with {@link #isCuriosLoaded()}.
 */
public final class CuriosTentAccess {

    private CuriosTentAccess() {
    }

    /**
     * Reports whether the Curios mod is loaded in this game instance.
     *
     * @return {@code true} when Curios is present
     */
    public static boolean isCuriosLoaded() {
        return ModList.get().isLoaded("curios");
    }

    /**
     * Finds a camping tent in Curios slots, or empty if absent/unavailable.
     *
     * @param player player
     * @return tent stack or empty
     */
    @NotNull
    public static ItemStack findEquippedTent(@NotNull final Player player) {
        if (!isCuriosLoaded()) {
            return ItemStack.EMPTY;
        }
        final Optional<ICuriosItemHandler> curios = CuriosApi.getCuriosInventory((LivingEntity) player);
        if (curios.isEmpty()) {
            return ItemStack.EMPTY;
        }
        final Map<String, ICurioStacksHandler> map = curios.get().getCurios();
        for (final ICurioStacksHandler handler : map.values()) {
            final IDynamicStackHandler stacks = handler.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                final ItemStack stack = stacks.getStackInSlot(i);
                if (stack.is(ModItems.CAMPING_TENT.get())) {
                    return stack;
                }
            }
        }
        return ItemStack.EMPTY;
    }
}
