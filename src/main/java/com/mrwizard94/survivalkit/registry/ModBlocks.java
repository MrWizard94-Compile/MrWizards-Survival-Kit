package com.mrwizard94.survivalkit.registry;

import com.mrwizard94.survivalkit.MrWizardsSurvivalKitMod;
import com.mrwizard94.survivalkit.block.CampingTentBlock;
import com.mrwizard94.survivalkit.block.TentEntranceBlock;
import com.mrwizard94.survivalkit.block.TentFabricBlock;
import com.mrwizard94.survivalkit.block.TentShellBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Block registrations.
 */
public final class ModBlocks {

    /** Deferred block register. */
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(MrWizardsSurvivalKitMod.MOD_ID);

    private static final float UNBREAKABLE = -1.0F;
    private static final float BLAST_PROOF = 3_600_000.0F;

    /** Primary tent door / controller block (pack-up only). */
    public static final DeferredBlock<CampingTentBlock> CAMPING_TENT = BLOCKS.register(
            "camping_tent",
            () -> new CampingTentBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(UNBREAKABLE, BLAST_PROOF)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)
                    .isRedstoneConductor((state, level, pos) -> false)));

    /** Multiblock fabric filler part (pack-up only). */
    public static final DeferredBlock<TentFabricBlock> TENT_FABRIC = BLOCKS.register(
            "tent_fabric",
            () -> new TentFabricBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(UNBREAKABLE, BLAST_PROOF)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)
                    .isRedstoneConductor((state, level, pos) -> false)));

    /** Unbreakable interior shell. */
    public static final DeferredBlock<TentShellBlock> TENT_SHELL = BLOCKS.register(
            "tent_shell",
            () -> new TentShellBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN)
                    .strength(UNBREAKABLE, BLAST_PROOF)
                    .sound(SoundType.WOOL)
                    .pushReaction(PushReaction.BLOCK)));

    /** Custom interior leave entrance. */
    public static final DeferredBlock<TentEntranceBlock> TENT_ENTRANCE = BLOCKS.register(
            "tent_entrance",
            () -> new TentEntranceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BROWN)
                    .strength(UNBREAKABLE, BLAST_PROOF)
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .pushReaction(PushReaction.BLOCK)
                    .isRedstoneConductor((state, level, pos) -> false)));

    private ModBlocks() {
    }
}
