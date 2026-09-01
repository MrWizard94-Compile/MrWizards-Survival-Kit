package com.mrwizard94.survivalkit.logic;

/**
 * Shared enter/leave transition parameters (server-authoritative timing + sound keys).
 */
public final class TentTransition {

    /** Client black-screen duration in ticks. */
    public static final int BLACK_SCREEN_TICKS = 20;

    /** Sound event id for zipper / shuffling. */
    public static final String ZIPPER_SOUND_ID = "mrwizardssurvivalkit:tent_zipper";

    /** Pitch animation duration in ticks. */
    public static final int PITCH_ANIM_TICKS = 40;

    /** Pack animation duration in ticks. */
    public static final int PACK_ANIM_TICKS = 40;

    private TentTransition() {
    }

    /**
     * Maps animation progress {@code 0..total} to a discrete stage {@code 0..stages-1}.
     *
     * @param tick    current tick into the animation
     * @param total   total animation ticks
     * @param stages  number of visual stages
     * @return clamped stage index
     */
    public static int animationStage(final int tick, final int total, final int stages) {
        if (stages < 1) {
            throw new IllegalArgumentException("stages must be >= 1");
        }
        if (total < 1) {
            return stages - 1;
        }
        if (tick <= 0) {
            return 0;
        }
        if (tick >= total) {
            return stages - 1;
        }
        return Math.min(stages - 1, (tick * stages) / total);
    }
}
