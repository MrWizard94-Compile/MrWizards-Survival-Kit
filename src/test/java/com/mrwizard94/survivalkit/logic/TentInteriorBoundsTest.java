package com.mrwizard94.survivalkit.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests interior 5×6×4 usable bounds and room grid.
 */
final class TentInteriorBoundsTest {

    @Test
    void usableDimensions_matchObjective() {
        assertEquals(5, TentInteriorBounds.WIDTH);
        assertEquals(6, TentInteriorBounds.DEPTH);
        assertEquals(4, TentInteriorBounds.HEIGHT);
    }

    @Test
    void shellIncludesWalls() {
        assertEquals(7, TentInteriorBounds.shellWidth());
        assertEquals(8, TentInteriorBounds.shellDepth());
        assertEquals(6, TentInteriorBounds.shellHeight());
    }

    @Test
    void isInsideUsable_trueForCenterCell() {
        assertTrue(TentInteriorBounds.isInsideUsable(3, 2, 3));
    }

    @Test
    void isInsideUsable_falseForWallAndOutside() {
        assertFalse(TentInteriorBounds.isInsideUsable(0, 2, 3));
        assertFalse(TentInteriorBounds.isInsideUsable(3, 0, 3));
        assertFalse(TentInteriorBounds.isInsideUsable(3, 2, 0));
        assertFalse(TentInteriorBounds.isInsideUsable(6, 2, 3));
        assertFalse(TentInteriorBounds.isInsideUsable(3, 5, 3));
        assertFalse(TentInteriorBounds.isInsideUsable(3, 2, 7));
    }

    @Test
    void roomOrigin_isStableAndSpaced() {
        final int[] a = TentInteriorBounds.roomOrigin(0, 16);
        final int[] b = TentInteriorBounds.roomOrigin(1, 16);
        assertEquals(64, a[1]);
        assertEquals(a[0] + TentInteriorBounds.shellWidth() + 16, b[0]);
        assertEquals(a[2], b[2]);
    }

    @Test
    void roomOrigin_rejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> TentInteriorBounds.roomOrigin(-1, 16));
    }
}
