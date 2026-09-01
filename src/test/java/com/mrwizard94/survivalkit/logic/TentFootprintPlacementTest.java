package com.mrwizard94.survivalkit.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Door-anchored placement: click cell is door, footprint away from player, facing toward player.
 */
final class TentFootprintPlacementTest {

    @Test
    void doorFacingTowardPlayer_isOppositeOfLook() {
        assertEquals(0, TentFootprint.doorFacingTowardPlayer(2)); // look north -> face south
        assertEquals(2, TentFootprint.doorFacingTowardPlayer(0)); // look south -> face north
        assertEquals(3, TentFootprint.doorFacingTowardPlayer(1)); // look west -> face east
        assertEquals(1, TentFootprint.doorFacingTowardPlayer(3)); // look east -> face west
    }

    @Test
    void originForDoorPlacement_doorOnClickCell_volumeAwayFromPlayer() {
        // Player looks north (2); door faces south (0) toward player; click/door at (10, 20).
        final int doorX = 10;
        final int doorZ = 20;
        final int facing = TentFootprint.doorFacingTowardPlayer(2);
        assertEquals(0, facing);
        final int[] origin = TentFootprint.originForDoorPlacement(doorX, doorZ, facing);
        final int[] door = TentFootprint.doorBlock(origin[0], 64, origin[1], facing);
        assertEquals(doorX, door[0]);
        assertEquals(doorZ, door[2]);
        // Footprint must not include the block south of the door (player side).
        assertFalse(LeaveReturnPose.isInsideFootprint(doorX, doorZ + 1, origin[0], origin[1]));
        // Footprint must extend north (away from player looking north).
        assertTrue(LeaveReturnPose.isInsideFootprint(doorX, doorZ - 1, origin[0], origin[1]));
    }

    @Test
    void isEntranceCell_marksLowerAndUpperDoorway() {
        final int facing = 0;
        final int[] origin = TentFootprint.originForDoorPlacement(10, 20, facing);
        final int[] door = TentFootprint.doorBlock(origin[0], 64, origin[1], facing);
        assertTrue(TentFootprint.isEntranceCell(
                door[0], 64, door[2], origin[0], 64, origin[1], facing));
        assertTrue(TentFootprint.isEntranceCell(
                door[0], 65, door[2], origin[0], 64, origin[1], facing));
        assertFalse(TentFootprint.isEntranceCell(
                door[0], 66, door[2], origin[0], 64, origin[1], facing));
    }

    @Test
    void eachFacing_doorMatchesOriginRoundTrip() {
        for (int facing = 0; facing <= 3; facing++) {
            final int doorX = 30 + facing;
            final int doorZ = 40 + facing;
            final int[] origin = TentFootprint.originForDoorPlacement(doorX, doorZ, facing);
            final int[] door = TentFootprint.doorBlock(origin[0], 70, origin[1], facing);
            assertEquals(doorX, door[0], "facing=" + facing);
            assertEquals(doorZ, door[2], "facing=" + facing);
        }
    }
}
