# MANIFEST — MrWizards Survial Kit 1.0.0 (pack/leave/door/CTM fix)

## What / why

Fix multi-tent pack-up that left exteriors standing, pack-up black screen, stale leave spawn after packing neighbors, missing interior `tent_entrance` blockstate (purple door), and Continuity CTM tiles that were identical no-ops.

## Changed paths (this delivery)

- `PendingPackClears` + `PackClearTicker` — reliable post-anim exterior clear
- `TentServices` — no black-screen on pack; leave pose from live binding
- `CampingTentBlockEntity` — pack clear no longer solely BE-owned
- `blockstates/tent_entrance.json` + improved entrance model
- Distinct CTM compact tiles under `optifine/ctm/tent_shell` and `tent_fabric_canvas`
- GameTests: `multiTentPackClearsEachFootprint`, `leavePoseFollowsActiveTentAfterOtherPacked`

## How to verify

```powershell
.\gradlew.bat check --no-daemon --warning-mode all --console=plain
.\gradlew.bat build --no-daemon --warning-mode all --console=plain
```

Expected: exit `0`; **All 13 required tests passed**; jar at `build/libs/mrwizards-survival-kit-1.0.0.jar`.

Scratch evidence (`%LOCALAPPDATA%\Temp\grok-goal-11f395c687e0\implementer`):

| File | Content |
|------|---------|
| `tests-multi-pack.log` | Multi-pack GameTests; EXIT_CODE=0; All 13 passed |
| `tests-pack-transition-leave.log` | `leaveTentIgnoresStaleCoordsAfterNeighborPacked` drives `handleDoorAction(PACK_UP)`, `enterTent`, `leaveTent` + black-screen probe |
| `assets-door-ctm.txt` | Entrance blockstate→model→`tent_door` + distinct CTM tiles |
| `gradle-check-1/2.log`, `gradle-build-1/2.log` | EXIT_CODE=0 |
| `pass1/`, `pass2/` | Built jars |

## Section 0 self-audit (CONST-GATE-001)

1 Completeness PASS · 2 Deps PASS · 3 Zero warnings PASS · 4 Tests PASS (13 GameTests ×2) · 5 Docs PASS · 6 Security N/A-reviewed · 7 Perf PASS · 8 Stack fidelity PASS · 9 Package PASS · 10 Resource PASS · 11 Repro PASS · 12 IP N/A · 13 Multi-agent N/A · 14 Review packaging PASS · 15 Self-audit PASS

Residual: Continuity in-client CTM look not screenshotted here; structural distinct tiles + properties shipped. Pitch/pack anim art polish remains non-goal.

## Suggested commit

`fix: reliable multi-tent pack clear, no pack blackout, entrance blockstate, CTM tiles`

## Next actions for human

1. Drop `build/libs/mrwizards-survival-kit-1.0.0.jar` into Legends Beyond and playtest 4× pitch/pack, leave after packing neighbors, interior door, Continuity CTM.
2. Optional: commit/push when ready.
