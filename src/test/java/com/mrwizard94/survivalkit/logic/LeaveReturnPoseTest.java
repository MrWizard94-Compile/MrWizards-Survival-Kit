package com.mrwizard94.survivalkit.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Leave/return pose must land outside the 3×4 exterior footprint.
 */
final class LeaveReturnPoseTest {

    @Test
    void compute_forEachFacing_isOutsideFootprint() {
        final int ox = 10;
        final int oy = 64;
        final int oz = 20;
        for (int facing = 0; facing <= 3; facing++) {
            final LeaveReturnPose.Pose pose = LeaveReturnPose.compute(ox, oy, oz, facing);
            final int bx = (int) Math.floor(pose.x());
            final int bz = (int) Math.floor(pose.z());
            assertFalse(
                    LeaveReturnPose.isInsideFootprint(bx, bz, ox, oz),
                    "facing=" + facing + " pose inside footprint at " + bx + "," + bz);
            assertEquals(oy, (int) pose.y());
            final int[] air = LeaveReturnPose.standingAirYs((int) pose.y());
            assertEquals(oy, air[0]);
            assertEquals(oy + 1, air[1]);
        }
    }

    @Test
    void isInsideFootprint_matchesThreeByFour() {
        assertTrue(LeaveReturnPose.isInsideFootprint(10, 20, 10, 20));
        assertTrue(LeaveReturnPose.isInsideFootprint(12, 23, 10, 20));
        assertFalse(LeaveReturnPose.isInsideFootprint(13, 20, 10, 20));
        assertFalse(LeaveReturnPose.isInsideFootprint(10, 24, 10, 20));
        assertFalse(LeaveReturnPose.isInsideFootprint(9, 20, 10, 20));
    }

    @Test
    void doorBlock_matchesFootprintHelper() {
        final int[] south = LeaveReturnPose.doorBlock(10, 64, 20, 0);
        assertEquals(11, south[0]);
        assertEquals(64, south[1]);
        assertEquals(20 + TentFootprint.DEPTH - 1, south[2]);
    }

    @Test
    void compute_landsOutwardAlongDoorFacing() {
        // Facing south (0): leave pose should be south of the door (higher Z).
        final int ox = 10;
        final int oy = 64;
        final int oz = 20;
        final LeaveReturnPose.Pose south = LeaveReturnPose.compute(ox, oy, oz, 0);
        final int[] door = TentFootprint.doorBlock(ox, oy, oz, 0);
        assertTrue(south.z() > door[2] + 0.5);
        assertFalse(LeaveReturnPose.isInsideFootprint(
                (int) Math.floor(south.x()), (int) Math.floor(south.z()), ox, oz));
    }

    @Test
    void compute_forActiveTent_isNearerThatDoorThanPackedNeighbor() {
        final int facing = 0;
        final int oxA = 10;
        final int ozA = 20;
        final int oxB = 20;
        final int ozB = 20;
        final LeaveReturnPose.Pose leaveB = LeaveReturnPose.compute(oxB, 64, ozB, facing);
        final int[] doorA = TentFootprint.doorBlock(oxA, 64, ozA, facing);
        final int[] doorB = TentFootprint.doorBlock(oxB, 64, ozB, facing);
        final double distB = Math.abs(leaveB.x() - (doorB[0] + 0.5))
                + Math.abs(leaveB.z() - (doorB[2] + 0.5));
        final double distA = Math.abs(leaveB.x() - (doorA[0] + 0.5))
                + Math.abs(leaveB.z() - (doorA[2] + 0.5));
        assertTrue(distB < distA);
        assertFalse(LeaveReturnPose.isInsideFootprint(
                (int) Math.floor(leaveB.x()), (int) Math.floor(leaveB.z()), oxA, ozA));
    }
}
