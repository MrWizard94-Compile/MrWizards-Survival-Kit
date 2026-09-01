# Architecture — MrWizards Survial Kit

## Layers

1. **Pure logic** (`com.mrwizard94.survivalkit.logic`) — footprint clearance, interior bounds, mob-repel math, starter-kit composition, persistence state, transition timing. Unit-tested without Minecraft bootstrap.
2. **Registry / content** — blocks, items, block entities, sounds, creative tab.
3. **Pocket realm** — datapack `camping` dimension type/stem shipped; `CampingDimensionBootstrap` force-creates the camping `ServerLevel` at server start when the stem was omitted by the world preset. `TentSavedData` mirrors `TentPersistenceState`.
4. **Gameplay services** — placement, door actions, enter/leave teleport, room seeding, mob repel tick.
5. **Network + client** — door choice screen, black-screen overlay, pitch/pack animation stages, zipper sound.

## Soft dependencies

Resolved at runtime via `ModList` presence + `BuiltInRegistries.ITEM` lookup by id. Base jar never requires VanillaAIOTs or Sophisticated Backpacks.

## Persistence

Each pitched tent gets a stable `UUID`. Binding maps tent id → camping room index + exterior pose. Interior blocks live in the camping dimension chunks and persist with the world. Packing stores the tent id on the returned item so re-pitch rebinds the same room.
