package com.mrwizard94.survivalkit.config;

import com.mrwizard94.survivalkit.logic.StarterKitComposer;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Server config for first-join tent gift and barrel starter contents.
 */
public final class SurvivalKitConfig {

    /** Whether to give a camping tent on first world join. */
    public final ModConfigSpec.BooleanValue giveTentOnFirstJoin;

    /** Ordered item ids placed into the starter barrel (soft-deps gated at runtime). */
    public final ModConfigSpec.ConfigValue<List<? extends String>> barrelContents;

    /** Built spec. */
    @NotNull
    public final ModConfigSpec spec;

    private static SurvivalKitConfig instance;

    /**
     * Builds the config spec.
     */
    public SurvivalKitConfig() {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("general");
        giveTentOnFirstJoin = builder
                .comment("If true, players receive one camping tent the first time they join a world.")
                .define("giveTentOnFirstJoin", true);
        final List<String> defaults = List.copyOf(StarterKitComposer.DEFAULT_BARREL_IDS);
        barrelContents = builder
                .comment(
                        "Item ids seeded into the tent barrel. Soft-dep ids are skipped when the mod is absent.",
                        "Default: torches, MRE, iron AIOT, basic backpack.")
                .defineList(
                        "barrelContents",
                        defaults,
                        () -> "minecraft:torch",
                        obj -> obj instanceof String s && !s.isBlank());
        builder.pop();
        spec = builder.build();
        instance = this;
    }

    /**
     * Returns the active config instance after mod init.
     *
     * @return config
     */
    @NotNull
    public static SurvivalKitConfig get() {
        if (instance == null) {
            throw new IllegalStateException("SurvivalKitConfig not initialised");
        }
        return instance;
    }

    /**
     * Returns whether first-join tent gift is enabled.
     *
     * @return gift enabled
     */
    public boolean shouldGiveTentOnFirstJoin() {
        return giveTentOnFirstJoin.getAsBoolean();
    }

    /**
     * Returns configured barrel content ids.
     *
     * @return item id list
     */
    @NotNull
    public List<String> barrelContentIds() {
        return List.copyOf(barrelContents.get());
    }
}

