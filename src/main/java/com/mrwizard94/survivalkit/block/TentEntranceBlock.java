package com.mrwizard94.survivalkit.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Custom interior tent entrance used to leave the camping pocket.
 *
 * <p>Unbreakable shell piece with a visible doorway opening.
 */
public final class TentEntranceBlock extends Block {

    /** Facing of the entrance opening. */
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final MapCodec<TentEntranceBlock> CODEC = simpleCodec(TentEntranceBlock::new);

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 16, 2),
            Block.box(0, 0, 14, 16, 16, 16),
            Block.box(0, 0, 0, 2, 16, 16),
            Block.box(14, 0, 0, 16, 16, 16),
            Block.box(0, 14, 0, 16, 16, 16));

    /**
     * Documented member.
     *
     * @param properties block properties
     */
    public TentEntranceBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    @NotNull
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    @NotNull
    protected RenderShape getRenderShape(@NotNull final BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @NotNull
    protected VoxelShape getShape(
            @NotNull final BlockState state,
            @NotNull final BlockGetter level,
            @NotNull final BlockPos pos,
            @NotNull final CollisionContext context) {
        return Shapes.block();
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
