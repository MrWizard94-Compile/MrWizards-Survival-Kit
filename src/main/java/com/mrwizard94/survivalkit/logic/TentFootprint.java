package com.mrwizard94.survivalkit.logic;

/**
 * Exterior tent footprint clearance and door-anchored placement rules.
 *
 * <p>Outside size is a fixed {@code 3×4} block footprint and {@code 3} blocks tall.
 * The clicked cell is the door; the footprint extends away from the player; door facing
 * opens toward the player.
 */
public final class TentFootprint {

    /** Exterior footprint width (X when facing north/south). */
    public static final int WIDTH = 3;

    /** Exterior footprint depth (Z when facing north/south). */
    public static final int DEPTH = 4;

    /** Exterior height in blocks. */
    public static final int HEIGHT = 3;

    private TentFootprint() {
    }

    /**
     * Returns {@code true} when every cell in the exterior volume is clear.
     *
     * @param originX    minimum X of the footprint
     * @param originY    ground Y (lowest exterior block)
     * @param originZ    minimum Z of the footprint
     * @param clearProbe probe that returns {@code true} when a cell may be overwritten
     * @return {@code true} if the full 3×4×3 volume is clear
     */
    public static boolean hasClearance(
            final int originX,
            final int originY,
            final int originZ,
            final BlockClearanceProbe clearProbe) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                for (int z = 0; z < DEPTH; z++) {
                    if (!clearProbe.isClear(originX + x, originY + y, originZ + z)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Door facing that opens toward the player from their look direction.
     *
     * @param playerLook2d player horizontal look: 0=south, 1=west, 2=north, 3=east
     * @return facing index toward the player (opposite of look)
     */
    public static int doorFacingTowardPlayer(final int playerLook2d) {
        return (playerLook2d + 2) % 4;
    }

    /**
     * Computes the axis-aligned origin so {@code doorX,doorZ} is the entrance cell
     * and the footprint extends away from the player (behind the door).
     *
     * @param doorX  clicked / door X
     * @param doorZ  clicked / door Z
     * @param facing door facing toward the player: 0=south, 1=west, 2=north, 3=east
     * @return int pair {@code {originX, originZ}}
     */
    public static int[] originForDoorPlacement(final int doorX, final int doorZ, final int facing) {
        return switch (facing) {
            // Door on south edge; footprint extends north (decreasing Z).
            case 0 -> new int[]{doorX - 1, doorZ - (DEPTH - 1)};
            // Door on west edge; footprint extends east (increasing X).
            case 1 -> new int[]{doorX, doorZ - 1};
            // Door on north edge; footprint extends south (increasing Z).
            case 2 -> new int[]{doorX - 1, doorZ};
            // Door on east edge; footprint extends west (decreasing X).
            case 3 -> new int[]{doorX - (WIDTH - 1), doorZ - 1};
            default -> throw new IllegalArgumentException("facing must be 0..3");
        };
    }

    /**
     * Door block within a footprint for the given facing.
     *
     * @param originX origin X
     * @param originY origin Y
     * @param originZ origin Z
     * @param facing  door facing index
     * @return {@code {x,y,z}}
     */
    public static int[] doorBlock(final int originX, final int originY, final int originZ, final int facing) {
        return switch (facing) {
            case 0 -> new int[]{originX + 1, originY, originZ + DEPTH - 1}; // south edge
            case 1 -> new int[]{originX, originY, originZ + 1}; // west edge
            case 2 -> new int[]{originX + 1, originY, originZ}; // north edge
            case 3 -> new int[]{originX + WIDTH - 1, originY, originZ + 1}; // east edge
            default -> throw new IllegalArgumentException("facing must be 0..3");
        };
    }

    /**
     * Returns whether a doorway cell (lower or upper) is on the entrance column.
     *
     * @param x       cell X
     * @param y       cell Y relative to ground (0 = feet)
     * @param z       cell Z
     * @param originX footprint origin X
     * @param originY footprint ground Y
     * @param originZ footprint origin Z
     * @param facing  door facing
     * @return {@code true} if this cell is the lower or upper entrance
     */
    public static boolean isEntranceCell(
            final int x,
            final int y,
            final int z,
            final int originX,
            final int originY,
            final int originZ,
            final int facing) {
        final int[] door = doorBlock(originX, originY, originZ, facing);
        return x == door[0] && z == door[2] && (y == originY || y == originY + 1);
    }

    /**
     * Probe for whether a world cell may be replaced by tent blocks.
     */
    @FunctionalInterface
    public interface BlockClearanceProbe {
        /**
         * Documented member.
         *
         * @param x world X
         * @param y world Y
         * @param z world Z
         * @return {@code true} when the cell is clear for placement
         */
        boolean isClear(int x, int y, int z);
    }
}
