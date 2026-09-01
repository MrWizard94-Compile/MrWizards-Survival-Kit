package com.mrwizard94.survivalkit.registry;

import com.mrwizard94.survivalkit.MrWizardsSurvivalKitMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Creative mode tabs.
 */
public final class ModCreativeTabs {

    /** Deferred tab register. */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MrWizardsSurvivalKitMod.MOD_ID);

    /** Main tab. */
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register(
            "main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mrwizardssurvivalkit"))
                    .icon(() -> new ItemStack(ModItems.CAMPING_TENT.get()))
                    .displayItems((params, output) -> {
                        output.accept(ModItems.CAMPING_TENT.get());
                        output.accept(ModItems.MRE.get());
                        output.accept(ModItems.TENT_FABRIC.get());
                        output.accept(ModItems.TENT_SHELL.get());
                        output.accept(ModItems.TENT_ENTRANCE.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
