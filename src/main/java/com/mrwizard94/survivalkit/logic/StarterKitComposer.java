package com.mrwizard94.survivalkit.logic;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Builds the tent barrel starter-kit item list, including soft-dependency extras.
 */
public final class StarterKitComposer {

    /** VanillaAIOTs mod id. */
    public static final String VANILLA_AIOTS_MOD_ID = "vanillaaiots";

    /** Sophisticated Backpacks mod id. */
    public static final String SOPHISTICATED_BACKPACKS_MOD_ID = "sophisticatedbackpacks";

    /** Iron AIOT item id. */
    public static final String IRON_AIOT_ITEM_ID = "vanillaaiots:iron_aiot";

    /** Basic backpack item id. */
    public static final String BASIC_BACKPACK_ITEM_ID = "sophisticatedbackpacks:backpack";

    /** Unbreaking enchantment id. */
    public static final String UNBREAKING_ENCHANT_ID = "minecraft:unbreaking";

    /** Custom MRE item id (this mod). */
    public static final String MRE_ITEM_ID = "mrwizardssurvivalkit:mre";

    /** Default barrel content ids (config may override). */
    public static final List<String> DEFAULT_BARREL_IDS = List.of(
            "minecraft:torch",
            MRE_ITEM_ID,
            IRON_AIOT_ITEM_ID,
            BASIC_BACKPACK_ITEM_ID);

    private StarterKitComposer() {
    }

    /**
     * Composes barrel contents using default ids and soft-dep gating.
     *
     * @param presence soft-dep probe
     * @return immutable ordered list of kit refs
     */
    public static List<KitItemRef> compose(final ModPresenceProbe presence) {
        return compose(presence, DEFAULT_BARREL_IDS);
    }

    /**
     * Composes barrel contents from a configured id list.
     *
     * <p>Soft-dep items are skipped when their mod is absent. Unknown/blank ids are skipped.
     * Torches and MRE default to stack size 64; other items default to 1. AIOT entries
     * receive Unbreaking III when included.
     *
     * @param presence soft-dep probe
     * @param itemIds  configured item ids (may include soft-dep ids)
     * @return immutable ordered list of kit refs
     */
    public static List<KitItemRef> compose(
            final ModPresenceProbe presence, final List<String> itemIds) {
        Objects.requireNonNull(presence, "presence");
        Objects.requireNonNull(itemIds, "itemIds");
        final CopyOnWriteArrayList<KitItemRef> items = new CopyOnWriteArrayList<>();
        for (final String rawId : itemIds) {
            if (rawId == null || rawId.isBlank()) {
                continue;
            }
            final String itemId = rawId.trim();
            if (IRON_AIOT_ITEM_ID.equals(itemId)) {
                if (presence.isLoaded(VANILLA_AIOTS_MOD_ID)) {
                    items.add(KitItemRef.enchanted(IRON_AIOT_ITEM_ID, 1, UNBREAKING_ENCHANT_ID, 3));
                }
                continue;
            }
            if (BASIC_BACKPACK_ITEM_ID.equals(itemId)) {
                if (presence.isLoaded(SOPHISTICATED_BACKPACKS_MOD_ID)) {
                    items.add(KitItemRef.of(BASIC_BACKPACK_ITEM_ID, 1));
                }
                continue;
            }
            if ("minecraft:torch".equals(itemId) || MRE_ITEM_ID.equals(itemId)) {
                items.add(KitItemRef.of(itemId, 64));
                continue;
            }
            items.add(KitItemRef.of(itemId, 1));
        }
        return Collections.unmodifiableList(items);
    }

    /**
     * Returns whether the composed kit contains the AIOT extra.
     *
     * @param kit composed kit
     * @return {@code true} if iron AIOT with Unbreaking III is present
     */
    public static boolean containsIronAiot(final List<KitItemRef> kit) {
        for (final KitItemRef ref : kit) {
            if (IRON_AIOT_ITEM_ID.equals(ref.itemId())
                    && UNBREAKING_ENCHANT_ID.equals(ref.enchant())
                    && ref.enchantLevel() == 3) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the composed kit contains the backpack extra.
     *
     * @param kit composed kit
     * @return {@code true} if basic backpack is present
     */
    public static boolean containsBackpack(final List<KitItemRef> kit) {
        for (final KitItemRef ref : kit) {
            if (BASIC_BACKPACK_ITEM_ID.equals(ref.itemId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the composed kit includes a full MRE stack.
     *
     * @param kit composed kit
     * @return {@code true} when MRE×64 is present
     */
    public static boolean containsMreStack(final List<KitItemRef> kit) {
        for (final KitItemRef ref : kit) {
            if (MRE_ITEM_ID.equals(ref.itemId()) && ref.count() == 64) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the kit includes a full torch stack.
     *
     * @param kit composed kit
     * @return {@code true} when torch×64 is present
     */
    public static boolean containsTorchStack(final List<KitItemRef> kit) {
        for (final KitItemRef ref : kit) {
            if ("minecraft:torch".equals(ref.itemId()) && ref.count() == 64) {
                return true;
            }
        }
        return false;
    }
}
