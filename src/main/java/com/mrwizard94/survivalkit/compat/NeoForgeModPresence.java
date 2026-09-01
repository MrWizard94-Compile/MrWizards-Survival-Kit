package com.mrwizard94.survivalkit.compat;

import com.mrwizard94.survivalkit.logic.ModPresenceProbe;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

/**
 * Real NeoForge {@link ModPresenceProbe} backed by {@link ModList}.
 */
public final class NeoForgeModPresence implements ModPresenceProbe {

    /** Shared instance. */
    public static final NeoForgeModPresence INSTANCE = new NeoForgeModPresence();

    private NeoForgeModPresence() {
    }

    @Override
    public boolean isLoaded(@NotNull final String modId) {
        return ModList.get().isLoaded(modId);
    }
}
