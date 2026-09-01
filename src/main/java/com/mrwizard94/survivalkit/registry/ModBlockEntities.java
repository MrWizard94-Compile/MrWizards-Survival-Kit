package com.mrwizard94.survivalkit.registry;

import com.mrwizard94.survivalkit.MrWizardsSurvivalKitMod;
import com.mrwizard94.survivalkit.blockentity.CampingTentBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Block entity registrations.
 */
public final class ModBlockEntities {

    /** Deferred BE register. */
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MrWizardsSurvivalKitMod.MOD_ID);

    /** Camping tent controller BE. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CampingTentBlockEntity>> CAMPING_TENT =
            BLOCK_ENTITIES.register(
                    "camping_tent",
                    () -> BlockEntityType.Builder.of(
                                    CampingTentBlockEntity::new,
                                    ModBlocks.CAMPING_TENT.get(),
                                    ModBlocks.TENT_FABRIC.get())
                            .build(null));

    private ModBlockEntities() {
    }
}
