package com.mrwizard94.survivalkit.logic;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Persist round-trip for tent↔room bindings.
 */
final class TentPersistenceStateTest {

    @Test
    void allocate_andRoundTrip_preservesBindings() {
        final TentPersistenceState original = new TentPersistenceState();
        final UUID tentA = UUID.randomUUID();
        final UUID tentB = UUID.randomUUID();
        original.allocate(tentA, "minecraft:overworld", 10, 64, 20, 0);
        original.allocate(tentB, "minecraft:overworld", 40, 70, 50, 2);
        original.markSeeded(tentA);

        final Map<UUID, TentRoomBinding> snap = original.snapshot();
        final TentPersistenceState restored =
                TentPersistenceState.fromSnapshot(snap, original.getNextRoomIndex());

        assertEquals(2, restored.getNextRoomIndex());
        assertTrue(restored.find(tentA).isPresent());
        assertTrue(restored.find(tentA).orElseThrow().seeded());
        assertEquals(1, restored.find(tentB).orElseThrow().roomIndex());
        assertEquals(10, restored.find(tentA).orElseThrow().exteriorX());
        assertEquals(2, restored.find(tentB).orElseThrow().facing());
    }
}
