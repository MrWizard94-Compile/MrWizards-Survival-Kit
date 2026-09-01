package com.mrwizard94.survivalkit.registry;

import com.mrwizard94.survivalkit.MrWizardsSurvivalKitMod;
import com.mrwizard94.survivalkit.item.CampingTentItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item registrations.
 */
public final class ModItems {

    /** Deferred item register. */
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(MrWizardsSurvivalKitMod.MOD_ID);

    /** Pitchable camping tent item. */
    public static final DeferredItem<CampingTentItem> CAMPING_TENT = ITEMS.register(
            "camping_tent",
            () -> new CampingTentItem(ModBlocks.CAMPING_TENT.get(), new Item.Properties().stacksTo(1)));

    /** Tent fabric block item. */
    public static final DeferredItem<BlockItem> TENT_FABRIC = ITEMS.register(
            "tent_fabric",
            () -> new BlockItem(ModBlocks.TENT_FABRIC.get(), new Item.Properties()));

    /** Interior shell (creative). */
    public static final DeferredItem<BlockItem> TENT_SHELL = ITEMS.register(
            "tent_shell",
            () -> new BlockItem(ModBlocks.TENT_SHELL.get(), new Item.Properties()));

    /** Interior entrance (creative). */
    public static final DeferredItem<BlockItem> TENT_ENTRANCE = ITEMS.register(
            "tent_entrance",
            () -> new BlockItem(ModBlocks.TENT_ENTRANCE.get(), new Item.Properties()));

    /**
     * MRE food — low-to-moderate nutrition.
     * Nutrition 6 / saturation 0.6 ≈ cooked beef-lite trail ration.
     */
    public static final DeferredItem<Item> MRE = ITEMS.register(
            "mre",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .food(new FoodProperties.Builder()
                            .nutrition(6)
                            .saturationModifier(0.6F)
                            .build())));

    private ModItems() {
    }
}
