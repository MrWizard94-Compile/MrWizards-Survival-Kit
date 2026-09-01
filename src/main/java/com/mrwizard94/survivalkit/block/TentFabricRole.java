package com.mrwizard94.survivalkit.block;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * Visual role of an exterior tent fabric part.
 */
public enum TentFabricRole implements StringRepresentable {
    /** Side / back wall canvas. */
    WALL("wall"),
    /** Peaked roof canvas. */
    ROOF("roof"),
    /** Ground cloth. */
    FLOOR("floor"),
    /** Visible exterior entrance panel beside the door. */
    ENTRANCE("entrance");

    private final String name;

    TentFabricRole(final String name) {
        this.name = name;
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return name;
    }
}
