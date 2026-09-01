package com.mrwizard94.survivalkit.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests 16-block hostile repel predicate and push vector.
 */
final class HostileRepelRulesTest {

    @Test
    void radius_isSixteen() {
        assertEquals(16, HostileRepelRules.RADIUS_BLOCKS);
        assertEquals(256, HostileRepelRules.radiusSquared());
    }

    @Test
    void shouldRepel_insideRadius() {
        assertTrue(HostileRepelRules.shouldRepel(0, 64, 0, 10, 64, 0));
        assertTrue(HostileRepelRules.shouldRepel(0, 64, 0, 16, 64, 0));
    }

    @Test
    void shouldRepel_outsideRadius() {
        assertFalse(HostileRepelRules.shouldRepel(0, 64, 0, 16.1, 64, 0));
        assertFalse(HostileRepelRules.shouldRepel(0, 64, 0, 30, 64, 0));
    }

    @Test
    void pushAway_pointsOutward() {
        final double[] push = HostileRepelRules.pushAway(0, 0, 5, 0);
        assertEquals(1.0, push[0], 1.0e-9);
        assertEquals(0.0, push[1], 1.0e-9);
    }

    @Test
    void pushAway_coincidentUsesFallback() {
        final double[] push = HostileRepelRules.pushAway(1, 1, 1, 1);
        assertEquals(0.0, push[0], 1.0e-9);
        assertEquals(1.0, push[1], 1.0e-9);
    }
}
