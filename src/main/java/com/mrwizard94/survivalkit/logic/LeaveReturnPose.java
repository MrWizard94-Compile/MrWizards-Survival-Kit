package com.mrwizard94.survivalkit.logic;

/**
 * Computes a leave/return pose outside the exterior tent footprint.
 *
 * <p>Return position is in front of the entrance, clear of the 3×4 footprint AABB,
 * with standing clearance (feet + head = two blocks of air).
 */
public final class LeaveReturnPose {

    /** Extra blocks beyond the door so the player is fully clear of the structure. */
    public static final int OUTWARD_STEPS = 2;

    private LeaveReturnPose() {
    }

    /**
     * Resulting leave pose in world coordinates.
     *
     * @param x feet X (block center)
     * @param y feet Y
     * @param z feet Z (block center)
     */
    public record Pose(double x, double y, double z) {
    }

    /**
     * Computes leave pose from exterior footprint origin and facing.
     *
     * @param originX footprint min X
     * @param originY footprint ground Y
     * @param originZ footprint min Z
     * @param facing  0=south..3=east (door facing toward the outside/player)
     * @return pose outside the footprint
     */
    public static Pose compute(final int originX, final int originY, final int originZ, final int facing) {
        if (facing < 0 || facing > 3) {
            throw new IllegalArgumentException("facing must be 0..3");
        }
        final int[] door = TentFootprint.doorBlock(originX, originY, originZ, facing);
        // Outward = door facing direction (toward the player / outside).
        final int[] step = switch (facing) {
            case 1 -> new int[]{-1, 0};
            case 2 -> new int[]{0, -1};
            case 3 -> new int[]{1, 0};
            default -> new int[]{0, 1};
        };
        final int bx = door[0] + step[0] * OUTWARD_STEPS;
        final int bz = door[2] + step[1] * OUTWARD_STEPS;
        if (isInsideFootprint(bx, bz, originX, originZ)) {
            final int fx = door[0] + step[0] * (OUTWARD_STEPS + TentFootprint.DEPTH);
            final int fz = door[2] + step[1] * (OUTWARD_STEPS + TentFootprint.DEPTH);
            return new Pose(fx + 0.5, originY, fz + 0.5);
        }
        return new Pose(bx + 0.5, originY, bz + 0.5);
    }

    /**
     * Returns whether a block column at (x,z) lies inside the exterior footprint.
     *
     * @param x       block X
     * @param z       block Z
     * @param originX footprint origin X
     * @param originZ footprint origin Z
     * @return {@code true} if inside 3×4 footprint
     */
    public static boolean isInsideFootprint(
            final int x, final int z, final int originX, final int originZ) {
        return x >= originX
                && x < originX + TentFootprint.WIDTH
                && z >= originZ
                && z < originZ + TentFootprint.DEPTH;
    }

    /**
     * Standing clearance: feet at {@code y} and head at {@code y+1} must be free.
     *
     * @param y ground Y of the pose
     * @return required air Y values {@code {feetY, headY}}
     */
    public static int[] standingAirYs(final int y) {
        return new int[]{y, y + 1};
    }

    /**
     * Door block position for the footprint (matches placement).
     *
     * @param originX origin X
     * @param originY origin Y
     * @param originZ origin Z
     * @param facing  facing index
     * @return {@code {x,y,z}}
     */
    public static int[] doorBlock(final int originX, final int originY, final int originZ, final int facing) {
        return TentFootprint.doorBlock(originX, originY, originZ, facing);
    }
}
