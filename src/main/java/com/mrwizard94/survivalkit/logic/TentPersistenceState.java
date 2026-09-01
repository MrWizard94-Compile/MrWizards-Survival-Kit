package com.mrwizard94.survivalkit.logic;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory / serializable registry of tent↔room bindings.
 *
 * <p>Pure logic used by {@code TentSavedData} and unit tests for persist round-trips.
 */
public final class TentPersistenceState {

    private final ConcurrentHashMap<UUID, TentRoomBinding> byTentId = new ConcurrentHashMap<>();
    private int nextRoomIndex;

    /**
     * Creates an empty state.
     */
    public TentPersistenceState() {
        this.nextRoomIndex = 0;
    }

    /**
     * Creates a state with a known next room index.
     *
     * @param nextRoomIndex next index to allocate
     */
    public TentPersistenceState(final int nextRoomIndex) {
        if (nextRoomIndex < 0) {
            throw new IllegalArgumentException("nextRoomIndex must be >= 0");
        }
        this.nextRoomIndex = nextRoomIndex;
    }

    /**
     * Allocates a new room index and stores the binding.
     *
     * @param tentId      tent id
     * @param exteriorDim exterior dimension id
     * @param x           exterior origin X
     * @param y           exterior origin Y
     * @param z           exterior origin Z
     * @param facing      facing
     * @return stored binding
     */
    public TentRoomBinding allocate(
            final UUID tentId,
            final String exteriorDim,
            final int x,
            final int y,
            final int z,
            final int facing) {
        Objects.requireNonNull(tentId, "tentId");
        final int room = nextRoomIndex++;
        final TentRoomBinding binding =
                new TentRoomBinding(tentId, room, exteriorDim, x, y, z, facing, false);
        byTentId.put(tentId, binding);
        return binding;
    }

    /**
     * Upserts a binding (used when reloading from disk).
     *
     * @param binding binding to store
     */
    public void put(final TentRoomBinding binding) {
        Objects.requireNonNull(binding, "binding");
        byTentId.put(binding.tentId(), binding);
        if (binding.roomIndex() >= nextRoomIndex) {
            nextRoomIndex = binding.roomIndex() + 1;
        }
    }

    /**
     * Looks up a binding.
     *
     * @param tentId tent id
     * @return optional binding
     */
    public Optional<TentRoomBinding> find(final UUID tentId) {
        return Optional.ofNullable(byTentId.get(tentId));
    }

    /**
     * Removes a binding when a tent is permanently destroyed without packing to item.
     *
     * @param tentId tent id
     * @return removed binding if any
     */
    public Optional<TentRoomBinding> remove(final UUID tentId) {
        return Optional.ofNullable(byTentId.remove(tentId));
    }

    /**
     * Marks a room as seeded.
     *
     * @param tentId tent id
     * @return updated binding
     */
    public Optional<TentRoomBinding> markSeeded(final UUID tentId) {
        final TentRoomBinding current = byTentId.get(tentId);
        if (current == null) {
            return Optional.empty();
        }
        final TentRoomBinding updated = current.markSeeded();
        byTentId.put(tentId, updated);
        return Optional.of(updated);
    }

    /**
     * Documented member.
     *
     * @return immutable snapshot map
     */
    public Map<UUID, TentRoomBinding> snapshot() {
        return Collections.unmodifiableMap(Map.copyOf(byTentId));
    }

    /**
     * Documented member.
     *
     * @return next room index that would be allocated
     */
    public int getNextRoomIndex() {
        return nextRoomIndex;
    }

    /**
     * Round-trip helper: rebuild state from a snapshot map + next index.
     *
     * @param bindings    bindings
     * @param nextIndex   next room index
     * @return new state
     */
    public static TentPersistenceState fromSnapshot(
            final Map<UUID, TentRoomBinding> bindings, final int nextIndex) {
        final TentPersistenceState state = new TentPersistenceState(nextIndex);
        for (final TentRoomBinding binding : bindings.values()) {
            state.put(binding);
        }
        return state;
    }
}
