package com.mrwizard94.survivalkit.logic;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests barrel kit defaults and soft-dep branching via injectable presence probe.
 */
final class StarterKitComposerTest {

    @Test
    void compose_withoutSoftDeps_onlyTorchesAndMre() {
        final List<KitItemRef> kit = StarterKitComposer.compose(id -> false);
        assertEquals(2, kit.size());
        assertTrue(StarterKitComposer.containsTorchStack(kit));
        assertTrue(StarterKitComposer.containsMreStack(kit));
        assertFalse(StarterKitComposer.containsIronAiot(kit));
        assertFalse(StarterKitComposer.containsBackpack(kit));
    }

    @Test
    void compose_withBothSoftDeps_addsAiotAndBackpack() {
        final ModPresenceProbe probe = id ->
                StarterKitComposer.VANILLA_AIOTS_MOD_ID.equals(id)
                        || StarterKitComposer.SOPHISTICATED_BACKPACKS_MOD_ID.equals(id);
        final List<KitItemRef> kit = StarterKitComposer.compose(probe);
        assertTrue(StarterKitComposer.containsTorchStack(kit));
        assertTrue(StarterKitComposer.containsMreStack(kit));
        assertTrue(StarterKitComposer.containsIronAiot(kit));
        assertTrue(StarterKitComposer.containsBackpack(kit));
        assertEquals(4, kit.size());
    }

    @Test
    void compose_withCustomList_honorsOrderAndSkipsMissingSoftDeps() {
        final ConcurrentHashMap<String, Boolean> loaded = new ConcurrentHashMap<>();
        loaded.put(StarterKitComposer.SOPHISTICATED_BACKPACKS_MOD_ID, Boolean.TRUE);
        final List<KitItemRef> kit = StarterKitComposer.compose(
                loaded::containsKey,
                List.of(
                        "minecraft:torch",
                        StarterKitComposer.MRE_ITEM_ID,
                        StarterKitComposer.IRON_AIOT_ITEM_ID,
                        StarterKitComposer.BASIC_BACKPACK_ITEM_ID));
        assertEquals(3, kit.size());
        assertTrue(StarterKitComposer.containsTorchStack(kit));
        assertTrue(StarterKitComposer.containsMreStack(kit));
        assertFalse(StarterKitComposer.containsIronAiot(kit));
        assertTrue(StarterKitComposer.containsBackpack(kit));
    }

    @Test
    void defaultBarrelIds_matchObjectiveList() {
        assertEquals("minecraft:torch", StarterKitComposer.DEFAULT_BARREL_IDS.get(0));
        assertEquals(StarterKitComposer.MRE_ITEM_ID, StarterKitComposer.DEFAULT_BARREL_IDS.get(1));
        assertEquals(StarterKitComposer.IRON_AIOT_ITEM_ID, StarterKitComposer.DEFAULT_BARREL_IDS.get(2));
        assertEquals(StarterKitComposer.BASIC_BACKPACK_ITEM_ID, StarterKitComposer.DEFAULT_BARREL_IDS.get(3));
    }
}
