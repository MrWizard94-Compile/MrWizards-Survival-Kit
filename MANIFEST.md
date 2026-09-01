# MANIFEST — MrWizards Survial Kit 1.0.0

## What / why

NeoForge 1.21.1 companion mod for Legends Beyond: door-anchored pitchable camping tent with full-height entrance, private pocket campsite, wall furniture, Curios/keybind soft place path, Continuity CTM assets, mob repel, starter kit (MRE), and soft VanillaAIOTs / Sophisticated Backpacks extras.

## Changed / added paths (this delivery)

- Placement: `TentFootprint`, `CampingTentItem`, `LeaveReturnPose`, `TentServices`
- Curios soft compat: `CuriosTentAccess`, `TentStackLocator`, `data/curios/tags/item/charm.json`
- Keybind: `PlaceTentKeyPayload`, `PlaceTentKeyHandler`, `ModNetworking`, `ClientSetup`
- Assets: `blockstates/tent_shell.json`, `camping_tent_upper` entrance models, `optifine/ctm/**`
- Tests: `TentFootprintPlacementTest`, `TentFootprintTest`, `LeaveReturnPoseTest`, GameTests (11)
- Docs: `README.md`, `MANIFEST.md`, `mods.toml` (optional curios + continuity)

## How to verify

```powershell
.\gradlew.bat check --no-daemon --warning-mode all --console=plain
.\gradlew.bat build --no-daemon --warning-mode all --console=plain
```

Expected: exit `0`, Checkstyle clean, unit tests pass, GameTest server reports `All 11 required tests passed`, jar at `build/libs/mrwizards-survival-kit-1.0.0.jar`.

Verification evidence under `{SCRATCH}` = `%LOCALAPPDATA%\Temp\grok-goal-c3ec48c3d805\implementer`:

| File | Content |
|------|---------|
| `tests-place-face.log` | Placement/facing unit tests (`TentFootprint*` / `LeaveReturnPose*`), `EXIT_CODE=0` |
| `tests-interior-assets.log` | GameTest run incl. wall furniture / shell / full-height entrance; `All 11 required tests passed` |
| `recipes-assets.txt` | `tent_shell` blockstate + model → `tent_canvas` texture path |
| `curios-ctm.txt` | Curios charm tag/keybind wiring + Continuity CTM property paths |
| `gradle-check-1.log` / `gradle-check-2.log` | `check` ×2, exit 0, 11/11 GameTests |
| `gradle-build-1.log` / `gradle-build-2.log` | `build` ×2, exit 0 |
| `pass1/` / `pass2/` | Copied jars from each build pass |

## Section 0 self-audit (CONST-GATE-001)

1 Completeness PASS · 2 Deps PASS (curios compileOnly; runtime optional) · 3 Zero warnings PASS · 4 Tests PASS (unit + 11 GameTests ×2) · 5 Docs PASS · 6 Security N/A-reviewed (no secrets; placement validated server-side) · 7 Perf PASS · 8 Stack fidelity PASS (1.21.1 / 21.1.248) · 9 Package PASS · 10 Resource/cache PASS · 11 Repro PASS (clean check/build ×2) · 12 IP N/A · 13 Multi-agent N/A · 14 Review packaging PASS · 15 Self-audit PASS

Residual: Continuity CTM activates only with Continuity/OptiFine client-side; tiles are compact duplicates of `tent_canvas` (seamless base). In-game visual CTM not verified in this environment.

## Suggested commit

`feat: door-anchored tent pitch, Curios keybind place, Continuity CTM, wall furniture`

## Next actions for human

1. Drop `build/libs/mrwizards-survival-kit-1.0.0.jar` into Legends Beyond `mods\` and playtest: door faces you, full-height entrance, shell texture, wall furniture, **V** keybind place (inventory and Curios charm).
2. Optional: install Continuity (client) to confirm connected canvas/shell.
3. Optional: `git add` / commit / push when ready.
