package com.mrwizard94.survivalkit.registry;

import com.mrwizard94.survivalkit.MrWizardsSurvivalKitMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Sound event registrations.
 */
public final class ModSounds {

    /** Deferred sound register. */
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, MrWizardsSurvivalKitMod.MOD_ID);

    /** Zipper / fabric shuffle used on enter, leave, pitch, and pack. */
    public static final DeferredHolder<SoundEvent, SoundEvent> TENT_ZIPPER = SOUNDS.register(
            "tent_zipper",
            () -> SoundEvent.createVariableRangeEvent(
                    ResourceLocation.fromNamespaceAndPath(MrWizardsSurvivalKitMod.MOD_ID, "tent_zipper")));

    private ModSounds() {
    }
}
