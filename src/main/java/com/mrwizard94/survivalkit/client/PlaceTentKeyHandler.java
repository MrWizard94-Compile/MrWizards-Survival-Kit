package com.mrwizard94.survivalkit.client;

import com.mrwizard94.survivalkit.MrWizardsSurvivalKitMod;
import com.mrwizard94.survivalkit.network.PlaceTentKeyPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

/**
 * Client keybind that asks the server to pitch a tent from inventory or Curios.
 */
public final class PlaceTentKeyHandler {

    private static final String CATEGORY = "key.categories." + MrWizardsSurvivalKitMod.MOD_ID;

    /** Default: V — place camping tent in front of the player. */
    public static final KeyMapping PLACE_TENT = new KeyMapping(
            "key.mrwizardssurvivalkit.place_tent",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            CATEGORY);

    /** Package-visible for {@link ClientSetup} registration. */
    PlaceTentKeyHandler() {
    }

    /**
     * Registers the key mapping on the mod bus.
     *
     * @param event register event
     */
    public static void onRegisterKeyMappings(@NotNull final RegisterKeyMappingsEvent event) {
        event.register(PLACE_TENT);
    }

    /**
     * Sends place-tent payload when the key is pressed.
     *
     * @param event client tick
     */
    @SubscribeEvent
    public void onClientTick(@NotNull final ClientTickEvent.Post event) {
        while (PLACE_TENT.consumeClick()) {
            PacketDistributor.sendToServer(new PlaceTentKeyPayload());
        }
    }
}
