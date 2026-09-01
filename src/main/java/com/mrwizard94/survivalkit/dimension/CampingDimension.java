package com.mrwizard94.survivalkit.dimension;

import com.mrwizard94.survivalkit.MrWizardsSurvivalKitMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Camping pocket-dimension keys and level resolution.
 */
public final class CampingDimension {

    /** Dimension id. */
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(MrWizardsSurvivalKitMod.MOD_ID, "camping");

    /** Level key. */
    public static final ResourceKey<Level> LEVEL_KEY = ResourceKey.create(Registries.DIMENSION, ID);

    /** Dimension type key. */
    public static final ResourceKey<DimensionType> TYPE_KEY =
            ResourceKey.create(Registries.DIMENSION_TYPE, ID);

    /** Gap between room shells. */
    public static final int ROOM_GAP = 16;

    private CampingDimension() {
    }

    /**
     * Resolves the camping host level, force-creating it when absent.
     *
     * @param server server
     * @return camping ServerLevel
     */
    @NotNull
    public static ServerLevel resolveHost(@NotNull final MinecraftServer server) {
        return CampingDimensionBootstrap.ensureCampingLevel(server);
    }

    /**
     * Returns whether the level is the camping pocket dimension.
     *
     * @param level candidate level
     * @return {@code true} when this is the camping dimension
     */
    public static boolean isCampingHost(@Nullable final Level level) {
        return level != null && level.dimension().equals(LEVEL_KEY);
    }
}
