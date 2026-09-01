package com.mrwizard94.survivalkit.block;

import com.mojang.serialization.MapCodec;
import com.mrwizard94.survivalkit.blockentity.CampingTentBlockEntity;
import com.mrwizard94.survivalkit.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Exterior tent door / controller block.
 */
public final class CampingTentBlock extends BaseEntityBlock {

    /** Facing property. */
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    /** Pitch/pack animation stage 0..3 (0=folded … 3=fully pitched). */
    public static final IntegerProperty ANIM_STAGE = IntegerProperty.create("anim_stage", 0, 3);

    private static final MapCodec<CampingTentBlock> CODEC = simpleCodec(CampingTentBlock::new);

    /**
     * Documented member.
     *
     * @param properties block properties
     */
    public CampingTentBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ANIM_STAGE, 3));
    }

    @Override
    @NotNull
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ANIM_STAGE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(ANIM_STAGE, 0);
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
        // Placed tent is pack-up only.
        return 0.0F;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull final BlockPos pos, @NotNull final BlockState state) {
        return new CampingTentBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            @NotNull final Level level,
            @NotNull final BlockState state,
            @NotNull final BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.CAMPING_TENT.get(), CampingTentBlockEntity::tick);
    }
}
