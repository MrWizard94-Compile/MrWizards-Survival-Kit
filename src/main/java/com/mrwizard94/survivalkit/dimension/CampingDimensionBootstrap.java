package com.mrwizard94.survivalkit.dimension;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.progress.LoggerChunkProgressListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executor;

/**
 * Ensures {@link CampingDimension#LEVEL_KEY} exists as a live {@link ServerLevel}.
 *
 * <p>Default world presets often omit datapack {@code dimension/} stems, so this bootstrap
 * force-creates the camping level after the server starts when it is missing.
 */
public final class CampingDimensionBootstrap {

    private static final Logger LOGGER = LogUtils.getLogger();

    private CampingDimensionBootstrap() {
    }

    /**
     * Server-started hook: create the camping dimension if absent.
     *
     * @param event started event
     */
    public static void onServerStarted(@NotNull final ServerStartedEvent event) {
        ensureCampingLevel(event.getServer());
    }

    /**
     * Returns the camping level, creating it if needed.
     *
     * @param server server
     * @return camping ServerLevel
     */
    @NotNull
    public static ServerLevel ensureCampingLevel(@NotNull final MinecraftServer server) {
        final ServerLevel existing = server.getLevel(CampingDimension.LEVEL_KEY);
        if (existing != null) {
            return existing;
        }
        synchronized (CampingDimensionBootstrap.class) {
            final ServerLevel again = server.getLevel(CampingDimension.LEVEL_KEY);
            if (again != null) {
                return again;
            }
            return createCampingLevel(server);
        }
    }

    @NotNull
    private static ServerLevel createCampingLevel(final MinecraftServer server) {
        try {
            final Holder.Reference<DimensionType> typeHolder = server.registryAccess()
                    .lookupOrThrow(Registries.DIMENSION_TYPE)
                    .getOrThrow(CampingDimension.TYPE_KEY);

            final Holder<Biome> plains = server.registryAccess()
                    .lookupOrThrow(Registries.BIOME)
                    .getOrThrow(Biomes.PLAINS);

            final FlatLevelGeneratorSettings settings = new FlatLevelGeneratorSettings(
                    Optional.empty(), plains, List.of());
            settings.getLayersInfo().add(new FlatLayerInfo(1, Blocks.BARRIER));
            settings.getLayersInfo().add(new FlatLayerInfo(63, Blocks.AIR));
            settings.updateLayers();

            final ChunkGenerator generator = new FlatLevelSource(settings);
            final LevelStem stem = new LevelStem(typeHolder, generator);

            final WorldData worldData = server.getWorldData();
            final ServerLevelData overworldData = worldData.overworldData();
            final DerivedLevelData derived = new DerivedLevelData(worldData, overworldData);

            final Field executorField = MinecraftServer.class.getDeclaredField("executor");
            executorField.setAccessible(true);
            final Executor executor = (Executor) executorField.get(server);

            final Field storageField = MinecraftServer.class.getDeclaredField("storageSource");
            storageField.setAccessible(true);
            final Object storageSource = storageField.get(server);

            final ChunkProgressListener listener = LoggerChunkProgressListener.createFromGameruleRadius(0);
            final long seed = worldData.worldGenOptions().seed();
            final long biomeZoom = net.minecraft.world.level.biome.BiomeManager.obfuscateSeed(seed);

            final ServerLevel camping = new ServerLevel(
                    server,
                    executor,
                    (net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess) storageSource,
                    derived,
                    CampingDimension.LEVEL_KEY,
                    stem,
                    listener,
                    worldData.isDebugWorld(),
                    biomeZoom,
                    ImmutableList.of(),
                    false,
                    server.overworld().getRandomSequences());

            final Field levelsField = MinecraftServer.class.getDeclaredField("levels");
            levelsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            final Map<ResourceKey<Level>, ServerLevel> levels =
                    (Map<ResourceKey<Level>, ServerLevel>) levelsField.get(server);
            levels.put(CampingDimension.LEVEL_KEY, camping);

            server.overworld()
                    .getWorldBorder()
                    .addListener(
                            new net.minecraft.world.level.border.BorderChangeListener
                                    .DelegateBorderChangeListener(camping.getWorldBorder()));
            NeoForge.EVENT_BUS.post(new LevelEvent.Load(camping));

            LOGGER.info(
                    "[mrwizardssurvivalkit] Force-created camping dimension {}",
                    CampingDimension.LEVEL_KEY.location());
            return camping;
        } catch (ReflectiveOperationException | ClassCastException | IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Failed to create camping dimension " + CampingDimension.LEVEL_KEY.location(), ex);
        }
    }
}
