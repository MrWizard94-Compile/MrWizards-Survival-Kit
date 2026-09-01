package com.mrwizard94.survivalkit.compat;

import com.mrwizard94.survivalkit.logic.KitItemRef;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Resolves {@link KitItemRef} into real {@link ItemStack}s via registry lookup.
 */
public final class KitItemResolver {

    private KitItemResolver() {
    }

    /**
     * Resolves a kit ref. Missing soft-dep items yield empty.
     *
     * @param ref   kit ref
     * @param level level (for enchantment lookup)
     * @return optional stack
     */
    @NotNull
    public static Optional<ItemStack> resolve(@NotNull final KitItemRef ref, @NotNull final Level level) {
        final ResourceLocation itemId = ResourceLocation.tryParse(ref.itemId());
        if (itemId == null || !BuiltInRegistries.ITEM.containsKey(itemId)) {
            return Optional.empty();
        }
        final Item item = BuiltInRegistries.ITEM.get(itemId);
        final ItemStack stack = new ItemStack(item, ref.count());
        if (ref.enchant() != null) {
            final ResourceLocation enchantId = ResourceLocation.tryParse(ref.enchant());
            if (enchantId != null) {
                final Optional<Holder.Reference<Enchantment>> enchant = level.registryAccess()
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .get(ResourceKey.create(Registries.ENCHANTMENT, enchantId));
                enchant.ifPresent(holder -> stack.enchant(holder, ref.enchantLevel()));
            }
        }
        return Optional.of(stack);
    }
}
