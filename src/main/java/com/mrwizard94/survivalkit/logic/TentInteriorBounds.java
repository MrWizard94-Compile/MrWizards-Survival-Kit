package com.mrwizard94.survivalkit.logic;

/**
 * Interior camping-room usable bounds.
 *
 * <p>Usable interior space is {@code 5×6} footprint and {@code 4} blocks tall.
 * Walls and floor/ceiling sit outside that usable volume.
 */
public final class TentInteriorBounds {

    /** Usable interior width. */
    public static final int WIDTH = 5;

    /** Usable interior depth. */
    public static final int DEPTH = 6;

    /** Usable interior height. */
    public static final int HEIGHT = 4;

    /** Wall thickness around the usable volume. */
    public static final int WALL = 1;

    private TentInteriorBounds() {
    }

    /**
     * Full shell size including walls (floor + ceiling + side walls).
     *
     * @return shell width including walls
     */
    public static int shellWidth() {
        return WIDTH + (WALL * 2);
    }

    /**
     * Full shell depth including walls.
     *
     * @return shell depth including walls
     */
    public static int shellDepth() {
        return DEPTH + (WALL * 2);
    }

    /**
     * Full shell height including floor and ceiling.
     *
     * @return shell height including floor and ceiling
     */
    public static int shellHeight() {
        return HEIGHT + (WALL * 2);
    }

    /**
     * Returns whether a local interior coordinate is inside the usable volume.
     *
     * @param localX local X relative to room origin (0 = shell corner)
     * @param localY local Y relative to room origin
     * @param localZ local Z relative to room origin
     * @return {@code true} if inside usable 5×6×4 space
     */
    public static boolean isInsideUsable(final int localX, final int localY, final int localZ) {
        return localX >= WALL
                && localX < WALL + WIDTH
                && localY >= WALL
                && localY < WALL + HEIGHT
                && localZ >= WALL
                && localZ < WALL + DEPTH;
    }

    /**
     * Computes the world origin of room {@code roomIndex} on the camping grid.
     *
     * <p>Rooms are laid out on a sparse grid with a padding gap so shells never touch.
     *
     * @param roomIndex non-negative room id
     * @param gapBlocks empty blocks between shells
     * @return {@code {originX, originY, originZ}}
     */
    public static int[] roomOrigin(final int roomIndex, final int gapBlocks) {
        return roomOrigin(roomIndex, gapBlocks, 0, 64, 0);
    }

    /**
     * Computes the world origin of room {@code roomIndex} on a camping grid with a base offset.
     *
     * @param roomIndex non-negative room id
     * @param gapBlocks empty blocks between shells
     * @param baseX     grid origin X
     * @param baseY     floor Y
     * @param baseZ     grid origin Z
     * @return {@code {originX, originY, originZ}}
     */
    public static int[] roomOrigin(
            final int roomIndex,
            final int gapBlocks,
            final int baseX,
            final int baseY,
            final int baseZ) {
        if (roomIndex < 0) {
            throw new IllegalArgumentException("roomIndex must be >= 0");
        }
        if (gapBlocks < 0) {
            throw new IllegalArgumentException("gapBlocks must be >= 0");
        }
        final int strideX = shellWidth() + gapBlocks;
        final int strideZ = shellDepth() + gapBlocks;
        final int grid = 64;
        final int gx = roomIndex % grid;
        final int gz = roomIndex / grid;
        return new int[]{baseX + (gx * strideX), baseY, baseZ + (gz * strideZ)};
    }
}

