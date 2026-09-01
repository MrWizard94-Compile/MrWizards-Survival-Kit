package com.mrwizard94.survivalkit.logic;

/**
 * Hostile-mob repulsion rules for a pitched tent.
 *
 * <p>Hostiles within {@link #RADIUS_BLOCKS} of the tent origin are repelled.
 */
public final class HostileRepelRules {

    /** Repulsion radius in blocks. */
    public static final int RADIUS_BLOCKS = 16;

    private HostileRepelRules() {
    }

    /**
     * Squared radius used for distance checks without {@code Math.sqrt}.
     *
     * @return {@code RADIUS_BLOCKS * RADIUS_BLOCKS}
     */
    public static int radiusSquared() {
        return RADIUS_BLOCKS * RADIUS_BLOCKS;
    }

    /**
     * Returns whether a hostile at {@code (mx,my,mz)} is inside the repel sphere.
     *
     * @param tentX tent origin X (center-ish exterior)
     * @param tentY tent origin Y
     * @param tentZ tent origin Z
     * @param mx    mob X
     * @param my    mob Y
     * @param mz    mob Z
     * @return {@code true} when the mob should be repelled
     */
    public static boolean shouldRepel(
            final double tentX,
            final double tentY,
            final double tentZ,
            final double mx,
            final double my,
            final double mz) {
        final double dx = mx - tentX;
        final double dy = my - tentY;
        final double dz = mz - tentZ;
        return (dx * dx) + (dy * dy) + (dz * dz) <= radiusSquared();
    }

    /**
     * Unit push vector away from the tent (horizontal). Length is 0 when coincident.
     *
     * @param tentX tent X
     * @param tentZ tent Z
     * @param mx    mob X
     * @param mz    mob Z
     * @return {@code {pushX, pushZ}} or zeros when on top of the tent
     */
    public static double[] pushAway(
            final double tentX,
            final double tentZ,
            final double mx,
            final double mz) {
        final double dx = mx - tentX;
        final double dz = mz - tentZ;
        final double len = Math.sqrt((dx * dx) + (dz * dz));
        if (len < 1.0e-6) {
            return new double[]{0.0, 1.0};
        }
        return new double[]{dx / len, dz / len};
    }
}
