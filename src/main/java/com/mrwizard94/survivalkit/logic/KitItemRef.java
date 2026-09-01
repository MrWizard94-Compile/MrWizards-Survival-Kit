package com.mrwizard94.survivalkit.logic;

import java.util.Objects;

/**
 * Soft-string item reference used by starter kits (no Minecraft types).
 *
 * @param itemId  registry id like {@code minecraft:furnace}
 * @param count   stack count
 * @param enchant optional enchant id (may be {@code null}); level applied when non-null
 * @param enchantLevel enchantment level when {@code enchant} is set
 */
public record KitItemRef(String itemId, int count, String enchant, int enchantLevel) {

    /**
     * Creates a plain stack reference.
     *
     * @param itemId registry id
     * @param count  count
     * @return ref
     */
    public static KitItemRef of(final String itemId, final int count) {
        return new KitItemRef(itemId, count, null, 0);
    }

    /**
     * Creates an enchanted stack reference.
     *
     * @param itemId       registry id
     * @param count        count
     * @param enchant      enchantment id
     * @param enchantLevel level
     * @return ref
     */
    public static KitItemRef enchanted(
            final String itemId,
            final int count,
            final String enchant,
            final int enchantLevel) {
        return new KitItemRef(itemId, count, enchant, enchantLevel);
    }

    /**
     * Compact constructor validation.
     */
    public KitItemRef {
        Objects.requireNonNull(itemId, "itemId");
        if (itemId.isBlank()) {
            throw new IllegalArgumentException("itemId must not be blank");
        }
        if (count < 1) {
            throw new IllegalArgumentException("count must be >= 1");
        }
        if (enchant != null && enchantLevel < 1) {
            throw new IllegalArgumentException("enchantLevel must be >= 1 when enchant is set");
        }
    }
}
