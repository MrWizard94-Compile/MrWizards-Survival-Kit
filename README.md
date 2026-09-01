# MrWizards Survial Kit

Companion NeoForge **1.21.1** mod for the **Legends Beyond** CurseForge instance.

Pitch a compact camping tent that opens into a private pocket campsite (Compact Machines–style, camping theme). Hostile mobs are kept away from the exterior, and the interior keeps your placed blocks and working stations.

## Features

- **Pitchable tent** with pitch/pack animation
- Exterior footprint **3×4** and **3** blocks tall (placement requires clear space)
- Placement is **door-anchored**: the click cell is the entrance; the tent volume extends away from you; the door faces toward you
- Full-height exterior entrance (lower controller + upper entrance fabric)
- Interior usable space **5×6** footprint and **4** blocks tall
- Interior layout: furnace + crafting table on a side wall; barrel on the opposite wall facing the room center
- Door options: **Enter** or **Pack up**
- Enter/leave: brief black-screen transition + zipper/shuffling sound; leave pose is outside the exterior footprint
- Hostile mob repel within **16** blocks of the pitched tent
- Starter kit: furnace, crafting table, bed, torch stack, **barrel** storage, **MRE** food stack
- Soft compatibility (optional mods; base path works without them):
  - **VanillaAIOTs** → iron AIOT with Unbreaking III (barrel contents)
  - **Sophisticated Backpacks** → basic backpack (barrel contents)
  - **Curios** → camping tent is a charm-slot item; **Place Camping Tent** keybind (default **V**) pitches from inventory or Curios
  - **Continuity** / OptiFine → connected textures for tent shell and canvas (`optifine/ctm/`)
- Server config (`mrwizardssurvivalkit-server.toml`):
  - `giveTentOnFirstJoin` (default **true**)
  - `barrelContents` (default torches / MRE / AIOT / backpack ids)
- Placed tent is **pack-up only**; exterior tent and interior shell are unbreakable

## Stack

| Pin | Value |
|-----|-------|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.248 |
| Java | 21 |
| Mod id | `mrwizardssurvivalkit` |
| Version | 1.0.0 |

## Build

```powershell
.\gradlew.bat check --no-daemon --warning-mode all --console=plain
.\gradlew.bat build --no-daemon --warning-mode all --console=plain
```

Jar output: `build/libs/`.

## GameTests

```powershell
.\gradlew.bat runGameTestServer --no-daemon --console=plain
```

## Repository

https://github.com/MrWizard94-Compile/MrWizards-Survival-Kit.git

## License

MIT
