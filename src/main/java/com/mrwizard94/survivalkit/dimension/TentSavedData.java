package com.mrwizard94.survivalkit.dimension;

import com.mrwizard94.survivalkit.logic.TentPersistenceState;
import com.mrwizard94.survivalkit.logic.TentRoomBinding;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Overworld-attached saved data wrapping {@link TentPersistenceState}.
 */
public final class TentSavedData extends SavedData {

    private static final String DATA_NAME = "mrwizardssurvivalkit_tents";

    private final TentPersistenceState state;

    /**
     * Empty data.
     */
    public TentSavedData() {
        this.state = new TentPersistenceState();
    }

    /**
     * Documented member.
     *
     * @param state persistence state
     */
    public TentSavedData(@NotNull final TentPersistenceState state) {
        this.state = state;
    }

    /**
     * Loads or creates the tent registry on the overworld.
     *
     * @param level any server level (overworld used for storage)
     * @return data
     */
    @NotNull
    public static TentSavedData get(@NotNull final ServerLevel level) {
        final ServerLevel overworld = level.getServer().overworld();
        return overworld.getDataStorage().computeIfAbsent(
                new Factory<>(TentSavedData::new, TentSavedData::load),
                DATA_NAME);
    }

    /**
     * Documented member.
     *
     * @param tag        nbt
     * @param registries lookup
     * @return data
     */
    @NotNull
    public static TentSavedData load(
            @NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider registries) {
        final int next = tag.getInt("NextRoom");
        final TentPersistenceState state = new TentPersistenceState(next);
        final ListTag list = tag.getList("Bindings", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            final CompoundTag entry = list.getCompound(i);
            final TentRoomBinding binding = new TentRoomBinding(
                    entry.getUUID("TentId"),
                    entry.getInt("Room"),
                    entry.getString("Dim"),
                    entry.getInt("X"),
                    entry.getInt("Y"),
                    entry.getInt("Z"),
                    entry.getInt("Facing"),
                    entry.getBoolean("Seeded"));
            state.put(binding);
        }
        return new TentSavedData(state);
    }

    @Override
    @NotNull
    public CompoundTag save(@NotNull final CompoundTag tag, @NotNull final HolderLookup.Provider registries) {
        tag.putInt("NextRoom", state.getNextRoomIndex());
        final ListTag list = new ListTag();
        for (final Map.Entry<UUID, TentRoomBinding> entry : state.snapshot().entrySet()) {
            final TentRoomBinding binding = entry.getValue();
            final CompoundTag compound = new CompoundTag();
            compound.putUUID("TentId", binding.tentId());
            compound.putInt("Room", binding.roomIndex());
            compound.putString("Dim", binding.exteriorDim());
            compound.putInt("X", binding.exteriorX());
            compound.putInt("Y", binding.exteriorY());
            compound.putInt("Z", binding.exteriorZ());
            compound.putInt("Facing", binding.facing());
            compound.putBoolean("Seeded", binding.seeded());
            list.add(compound);
        }
        tag.put("Bindings", list);
        return tag;
    }

    /**
     * Documented member.
     *
     * @return mutable persistence state
     */
    @NotNull
    public TentPersistenceState state() {
        return state;
    }

    /**
     * Allocates and dirty-marks.
     *
     * @param tentId tent id
     * @param dim    exterior dim
     * @param x      x
     * @param y      y
     * @param z      z
     * @param facing facing index
     * @return binding
     */
    @NotNull
    public TentRoomBinding allocate(
            @NotNull final UUID tentId,
            @NotNull final String dim,
            final int x,
            final int y,
            final int z,
            final int facing) {
        final TentRoomBinding binding = state.allocate(tentId, dim, x, y, z, facing);
        this.setDirty();
        return binding;
    }

    /**
     * Documented member.
     *
     * @param tentId tent id
     * @return binding
     */
    @NotNull
    public Optional<TentRoomBinding> find(@NotNull final UUID tentId) {
        return state.find(tentId);
    }

    /**
     * Marks seeded.
     *
     * @param tentId tent id
     */
    public void markSeeded(@NotNull final UUID tentId) {
        state.markSeeded(tentId);
        this.setDirty();
    }

    /**
     * Updates exterior pose after re-pitch of an existing tent id.
     *
     * @param binding new binding values
     */
    public void put(@NotNull final TentRoomBinding binding) {
        state.put(binding);
        this.setDirty();
    }
}
