package com.mrwizard94.survivalkit.blockentity;

import com.mrwizard94.survivalkit.block.CampingTentBlock;
import com.mrwizard94.survivalkit.dimension.TentSavedData;
import com.mrwizard94.survivalkit.dimension.TentServices;
import com.mrwizard94.survivalkit.logic.TentRoomBinding;
import com.mrwizard94.survivalkit.logic.TentTransition;
import com.mrwizard94.survivalkit.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Stores tent id, controller flag, and pitch/pack animation progress.
 */
public final class CampingTentBlockEntity extends BlockEntity {

    private UUID tentId = UUID.randomUUID();
    private boolean controller;
    private BlockPos controllerPos = BlockPos.ZERO;
    private int animTicks;
    private boolean packing;
    private boolean pitching;
    private int packOriginX;
    private int packOriginY;
    private int packOriginZ;
    private boolean hasPackOrigin;

    /**
     * Documented member.
     *
     * @param pos   block pos
     * @param state block state
     */
    public CampingTentBlockEntity(final BlockPos pos, final BlockState state) {
        super(ModBlockEntities.CAMPING_TENT.get(), pos, state);
        this.controller = state.getBlock() instanceof CampingTentBlock;
        this.controllerPos = pos;
    }

    /**
     * Server tick for pitch/pack animation.
     *
     * @param level level
     * @param pos   pos
     * @param state state
     * @param be    block entity
     */
    public static void tick(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final CampingTentBlockEntity be) {
        if (level.isClientSide || !be.controller) {
            return;
        }
        if (!be.pitching && !be.packing) {
            return;
        }
        be.animTicks++;
        final int total = be.packing ? TentTransition.PACK_ANIM_TICKS : TentTransition.PITCH_ANIM_TICKS;
        final int stage = TentTransition.animationStage(be.animTicks, total, 4);
        if (state.getValue(CampingTentBlock.ANIM_STAGE) != stage) {
            level.setBlock(pos, state.setValue(CampingTentBlock.ANIM_STAGE, stage), 3);
        }
        syncFabric(level, pos, state, be, stage);

        if (be.animTicks >= total) {
            if (be.packing) {
                be.packing = false;
                be.animTicks = 0;
                if (level instanceof ServerLevel serverLevel && be.hasPackOrigin) {
                    TentServices.finishPackClear(
                            serverLevel, be.packOriginX, be.packOriginY, be.packOriginZ);
                    be.hasPackOrigin = false;
                }
                be.setChanged();
            } else {
                be.pitching = false;
                be.animTicks = 0;
                level.setBlock(pos, state.setValue(CampingTentBlock.ANIM_STAGE, 3), 3);
                syncFabric(level, pos, state, be, 3);
                be.setChanged();
            }
        }
    }

    private static void syncFabric(
            final Level level,
            final BlockPos pos,
            final BlockState state,
            final CampingTentBlockEntity be,
            final int stage) {
        final Direction facing = state.getValue(CampingTentBlock.FACING);
        int originX = be.packOriginX;
        int originY = be.packOriginY;
        int originZ = be.packOriginZ;
        boolean haveOrigin = be.hasPackOrigin;
        if (!haveOrigin && level instanceof ServerLevel serverLevel) {
            final Optional<TentRoomBinding> binding = TentSavedData.get(serverLevel).find(be.tentId);
            if (binding.isPresent()) {
                originX = binding.get().exteriorX();
                originY = binding.get().exteriorY();
                originZ = binding.get().exteriorZ();
                haveOrigin = true;
            }
        }
        if (haveOrigin) {
            TentServices.syncFabricAnimStages(level, originX, originY, originZ, pos, facing, stage);
        }
    }

    /**
     * Documented member.
     *
     * @return tent UUID
     */
    @NotNull
    public UUID getTentId() {
        return tentId;
    }

    /**
     * Documented member.
     *
     * @param tentId tent UUID
     */
    public void setTentId(@NotNull final UUID tentId) {
        this.tentId = tentId;
        this.setChanged();
    }

    /**
     * Documented member.
     *
     * @return whether this BE is the door controller
     */
    public boolean isController() {
        return controller;
    }

    /**
     * Marks this BE as a fabric satellite.
     *
     * @param controller controller position
     * @param tentId     shared tent id
     */
    public void configureAsPart(@NotNull final BlockPos controller, @NotNull final UUID tentId) {
        this.controller = false;
        this.controllerPos = controller.immutable();
        this.tentId = tentId;
        this.setChanged();
    }

    /**
     * Marks this BE as controller and starts pitch animation.
     *
     * @param tentId tent id
     */
    public void configureAsController(@NotNull final UUID tentId) {
        this.controller = true;
        this.controllerPos = this.worldPosition.immutable();
        this.tentId = tentId;
        this.pitching = true;
        this.packing = false;
        this.animTicks = 0;
        this.setChanged();
    }

    /**
     * Documented member.
     *
     * @return controller block position
     */
    @NotNull
    public BlockPos getControllerPos() {
        return controllerPos;
    }

    /**
     * Starts pack-up animation and records the footprint to clear when finished.
     *
     * @param originX footprint origin X
     * @param originY footprint origin Y
     * @param originZ footprint origin Z
     */
    public void beginPacking(final int originX, final int originY, final int originZ) {
        this.packing = true;
        this.pitching = false;
        this.animTicks = 0;
        this.packOriginX = originX;
        this.packOriginY = originY;
        this.packOriginZ = originZ;
        this.hasPackOrigin = true;
        this.setChanged();
    }

    /**
     * Documented member.
     *
     * @return {@code true} when pack animation has finished
     */
    public boolean isPackAnimationComplete() {
        return !packing && animTicks == 0 && getBlockState().getValue(CampingTentBlock.ANIM_STAGE) == 0;
    }

    /**
     * Documented member.
     *
     * @return whether currently packing
     */
    public boolean isPacking() {
        return packing;
    }

    /**
     * Clears local flags without dropping an item (orphan fabric cleanup).
     */
    public void clearWithoutPack() {
        this.setChanged();
    }

    @Override
    protected void saveAdditional(@NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putUUID("TentId", tentId);
        tag.putBoolean("Controller", controller);
        tag.putInt("CtrlX", controllerPos.getX());
        tag.putInt("CtrlY", controllerPos.getY());
        tag.putInt("CtrlZ", controllerPos.getZ());
        tag.putInt("AnimTicks", animTicks);
        tag.putBoolean("Packing", packing);
        tag.putBoolean("Pitching", pitching);
        tag.putBoolean("HasPackOrigin", hasPackOrigin);
        tag.putInt("PackOX", packOriginX);
        tag.putInt("PackOY", packOriginY);
        tag.putInt("PackOZ", packOriginZ);
    }

    @Override
    protected void loadAdditional(@NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.hasUUID("TentId")) {
            tentId = tag.getUUID("TentId");
        }
        controller = tag.getBoolean("Controller");
        controllerPos = new BlockPos(tag.getInt("CtrlX"), tag.getInt("CtrlY"), tag.getInt("CtrlZ"));
        animTicks = tag.getInt("AnimTicks");
        packing = tag.getBoolean("Packing");
        pitching = tag.getBoolean("Pitching");
        hasPackOrigin = tag.getBoolean("HasPackOrigin");
        packOriginX = tag.getInt("PackOX");
        packOriginY = tag.getInt("PackOY");
        packOriginZ = tag.getInt("PackOZ");
    }

    @Override
    @NotNull
    public CompoundTag getUpdateTag(@NotNull final HolderLookup.Provider registries) {
        final CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
