package com.mrwizard94.survivalkit.logic;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests exterior 3×4×3 clearance rules.
 */
final class TentFootprintTest {

    @Test
    void hasClearance_acceptsFullyClearVolume() {
        assertTrue(TentFootprint.hasClearance(0, 64, 0, (x, y, z) -> true));
    }

    @Test
    void hasClearance_rejectsBlockedCellInsideFootprint() {
        final ConcurrentHashMap<String, Boolean> blocked = new ConcurrentHashMap<>();
        blocked.put(key(1, 65, 2), Boolean.TRUE);
        final boolean clear = TentFootprint.hasClearance(0, 64, 0, (x, y, z) -> !blocked.containsKey(key(x, y, z)));
        assertFalse(clear);
    }

    @Test
    void hasClearance_ignoresBlocksOutsideFootprint() {
        final ConcurrentHashMap<String, Boolean> blocked = new ConcurrentHashMap<>();
        blocked.put(key(3, 64, 0), Boolean.TRUE); // outside width
        blocked.put(key(0, 64, 4), Boolean.TRUE); // outside depth
        blocked.put(key(0, 67, 0), Boolean.TRUE); // outside height
        assertTrue(TentFootprint.hasClearance(0, 64, 0, (x, y, z) -> !blocked.containsKey(key(x, y, z))));
    }

    @Test
    void dimensions_matchObjective() {
        assertEquals(3, TentFootprint.WIDTH);
        assertEquals(4, TentFootprint.DEPTH);
        assertEquals(3, TentFootprint.HEIGHT);
    }

    @Test
    void originForDoorPlacement_southFacing_extendsNorthBehindDoor() {
        // Door on south edge facing the player; footprint extends north (decreasing Z).
        final int[] origin = TentFootprint.originForDoorPlacement(10, 20, 0);
        assertEquals(9, origin[0]);
        assertEquals(20 - (TentFootprint.DEPTH - 1), origin[1]);
        final int[] door = TentFootprint.doorBlock(origin[0], 64, origin[1], 0);
        assertEquals(10, door[0]);
        assertEquals(20, door[2]);
    }

    private static String key(final int x, final int y, final int z) {
        return x + "," + y + "," + z;
    }
}
