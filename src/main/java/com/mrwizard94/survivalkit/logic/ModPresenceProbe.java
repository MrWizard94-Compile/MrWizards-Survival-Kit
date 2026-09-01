package com.mrwizard94.survivalkit.logic;

/**
 * Soft-dependency presence probe used by starter-kit composition.
 */
@FunctionalInterface
public interface ModPresenceProbe {

    /**
     * Documented member.
     *
     * @param modId NeoForge / FML mod id
     * @return {@code true} when the mod is loaded
     */
    boolean isLoaded(String modId);
}
