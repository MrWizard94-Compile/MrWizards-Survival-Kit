package com.mrwizard94.survivalkit.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Unbreakable interior camping-room shell (walls/floor/ceiling).
 */
public final class TentShellBlock extends Block {

    private static final MapCodec<TentShellBlock> CODEC = simpleCodec(TentShellBlock::new);

    /**
     * Documented member.
     *
     * @param properties block properties
     */
    public TentShellBlock(final Properties properties) {
        super(properties);
    }

    @Override
    @NotNull
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public float getDestroyProgress(
            @NotNull final BlockState state,
            @NotNull final Player player,
            @NotNull final BlockGetter level,
            @NotNull final BlockPos pos) {
        return 0.0F;
    }
}
