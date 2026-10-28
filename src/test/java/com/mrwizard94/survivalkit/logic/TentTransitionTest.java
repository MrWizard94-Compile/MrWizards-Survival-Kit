package com.mrwizard94.survivalkit.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Transition timing / animation stage helper tests.
 */
final class TentTransitionTest {

    @Test
    void constants_matchDesign() {
        assertEquals(20, TentTransition.BLACK_SCREEN_TICKS);
        assertEquals(40, TentTransition.PITCH_ANIM_TICKS);
        assertEquals(40, TentTransition.PACK_ANIM_TICKS);
        assertFalse(TentTransition.blackScreenOnPackUp());
        assertEquals("mrwizardssurvivalkit:tent_zipper", TentTransition.ZIPPER_SOUND_ID);
    }

    @Test
    void animationStage_clampsAndProgresses() {
        assertEquals(0, TentTransition.animationStage(0, 40, 4));
        assertEquals(1, TentTransition.animationStage(10, 40, 4));
        assertEquals(3, TentTransition.animationStage(40, 40, 4));
        assertEquals(3, TentTransition.animationStage(99, 40, 4));
    }

    @Test
    void animationStage_rejectsInvalidStages() {
        assertThrows(IllegalArgumentException.class, () -> TentTransition.animationStage(1, 10, 0));
    }
}
