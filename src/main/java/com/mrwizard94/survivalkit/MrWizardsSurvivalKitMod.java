package com.mrwizard94.survivalkit;

import com.mrwizard94.survivalkit.client.ClientSetup;
import com.mrwizard94.survivalkit.config.SurvivalKitConfig;
import com.mrwizard94.survivalkit.dimension.CampingDimensionBootstrap;
import com.mrwizard94.survivalkit.event.FirstJoinHandler;
import com.mrwizard94.survivalkit.event.MobRepelHandler;
import com.mrwizard94.survivalkit.event.PackClearTicker;
import com.mrwizard94.survivalkit.event.TentInteractionHandler;
import com.mrwizard94.survivalkit.network.ModNetworking;
import com.mrwizard94.survivalkit.registry.ModBlockEntities;
import com.mrwizard94.survivalkit.registry.ModBlocks;
import com.mrwizard94.survivalkit.registry.ModCreativeTabs;
import com.mrwizard94.survivalkit.registry.ModItems;
import com.mrwizard94.survivalkit.registry.ModSounds;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * Mod entry point for MrWizards Survial Kit.
 */
@Mod(MrWizardsSurvivalKitMod.MOD_ID)
public final class MrWizardsSurvivalKitMod {

    /** Mod id (registry namespace). */
    public static final String MOD_ID = "mrwizardssurvivalkit";

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Constructs and wires registries + game events.
     *
     * @param modEventBus mod bus
     * @param container   mod container
     */
    public MrWizardsSurvivalKitMod(
            @NotNull final IEventBus modEventBus,
            @NotNull final ModContainer container) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModSounds.SOUNDS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);

        final SurvivalKitConfig config = new SurvivalKitConfig();
        container.registerConfig(ModConfig.Type.SERVER, config.spec);

        modEventBus.addListener(ModNetworking::register);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.register(modEventBus);
        }

        NeoForge.EVENT_BUS.register(new TentInteractionHandler());
        NeoForge.EVENT_BUS.register(new MobRepelHandler());
        NeoForge.EVENT_BUS.register(new PackClearTicker());
        NeoForge.EVENT_BUS.register(new FirstJoinHandler());
        NeoForge.EVENT_BUS.addListener(CampingDimensionBootstrap::onServerStarted);

        LOGGER.info("[{}] MrWizards Survial Kit initialised (container={})", MOD_ID, container.getModId());
    }
}
