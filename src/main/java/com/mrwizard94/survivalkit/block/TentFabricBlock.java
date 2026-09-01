package com.mrwizard94.survivalkit.block;

import com.mojang.serialization.MapCodec;
import com.mrwizard94.survivalkit.blockentity.CampingTentBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Multiblock fabric part that forwards interaction to the controller tent block.
 */
public final class TentFabricBlock extends BaseEntityBlock {

    /** Facing mirrors the controller. */
    public static final DirectionProperty FACING = CampingTentBlock.FACING;

    /** Animation stage mirrors the controller. */
    public static final IntegerProperty ANIM_STAGE = CampingTentBlock.ANIM_STAGE;

    /** Visual role within the pitched tent silhouette. */
    public static final EnumProperty<TentFabricRole> ROLE =
            EnumProperty.create("role", TentFabricRole.class);

    private static final MapCodec<TentFabricBlock> CODEC = simpleCodec(TentFabricBlock::new);

    /**
     * Documented member.
     *
     * @param properties block properties
     */
    public TentFabricBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ANIM_STAGE, 3)
                .setValue(ROLE, TentFabricRole.WALL));
    }

    @Override
    @NotNull
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ANIM_STAGE, ROLE);
    }

    @Override
    @NotNull
    protected RenderShape getRenderShape(@NotNull final BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public float getDestroyProgress(
            @NotNull final BlockState state,
            @NotNull final Player player,
            @NotNull final BlockGetter level,
            @NotNull final BlockPos pos) {
        return 0.0F;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos pos, @NotNull final BlockState state) {
        return new CampingTentBlockEntity(pos, state);
    }

    @Override
    protected void onRemove(
            @NotNull final BlockState state,
            @NotNull final Level level,
            @NotNull final BlockPos pos,
            @NotNull final BlockState newState,
            final boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            final BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CampingTentBlockEntity tent && !tent.isController()) {
                tent.clearWithoutPack();
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
