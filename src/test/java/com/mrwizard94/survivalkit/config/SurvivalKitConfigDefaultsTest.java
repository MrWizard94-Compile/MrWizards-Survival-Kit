package com.mrwizard94.survivalkit.config;

import com.mrwizard94.survivalkit.logic.StarterKitComposer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Drives the real config constructor defaults (no Minecraft bootstrap required).
 */
final class SurvivalKitConfigDefaultsTest {

    @Test
    void defaults_giveTentOnAndBarrelListMatchesObjective() {
        final SurvivalKitConfig config = new SurvivalKitConfig();
        // Spec values are not loaded from disk in plain unit tests; assert declared defaults.
        assertTrue(config.giveTentOnFirstJoin.getDefault());
        assertEquals(
                StarterKitComposer.DEFAULT_BARREL_IDS,
                java.util.List.copyOf(config.barrelContents.getDefault()));
    }
}
