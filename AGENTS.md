# AGENTS — Project Entry Point (Pointer)

**Project:** MrWizards Survial Kit  
**Pack:** AGENTS Constitution (universal)  
**Pack path (relative to this file):** `../../../../AGENTS Constitution/`  

This file is **Level 4 project law entry only**. It does not duplicate the constitution.

---

## Binding pack

→ **Constitution (SOUL):** [`../../../../AGENTS Constitution/AGENTS.md`](../../../../AGENTS%20Constitution/AGENTS.md)  

→ **Process (SOP):** [`../../../../AGENTS Constitution/SOP.md`](../../../../AGENTS%20Constitution/SOP.md)  

→ **Adopt / move guide:** [`../../../../AGENTS Constitution/ADOPT.md`](../../../../AGENTS%20Constitution/ADOPT.md)  

→ **Pack identity:** [`../../../../AGENTS Constitution/PACK.md`](../../../../AGENTS%20Constitution/PACK.md)  

---

## Always load (via pack)

* Pack `AGENTS.md`  
* Pack `SOP.md`  
* Pack `constitution/03-DEFINITION-OF-DONE.md`  
* Pack `standards/ENGINEERING.md`  
* Pack `standards/TESTING.md`  
* Pack `standards/DOCUMENTATION.md`  

Then load pack modules per the applicability matrix in pack `AGENTS.md`.

---

## Project-local law (Level 4)

* Product docs: `README.md`, `docs/`
* Architecture notes: `docs/ARCHITECTURE.md`
* Stack pin: Minecraft **1.21.1**, NeoForge **21.1.248**, Java **21**
* Display name spelling **Survial** is intentional (OBJECTIVE)
* Soft deps: `vanillaaiots` / `sophisticatedbackpacks` via runtime registry lookup; `curios` via `compileOnly` + charm tag + keybind (base path works without Curios); `continuity` optional client CTM assets only

Project law may **tighten** standards. It may not weaken `CONST-*`, `ENG-WARN-001`, `TEST-BEHAVIOR-001`, or `SEC-INPUT-001` without a documented override.

---

## Verify pack (from pack root)

```powershell
pwsh -File "../../../../AGENTS Constitution/tools/verify-pack.ps1"
```

Must exit `0` after pack install, move, or update.

---

## Evolution

* Do not edit the pack by scattering project notes into it.  
* Evolve pack via complete file replacements per pack `AGENTS.md` amendment rules.  
* Evolve this pointer when the relative pack path changes.
