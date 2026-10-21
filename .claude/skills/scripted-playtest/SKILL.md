---
name: scripted-playtest
description: Drive the real Minecraft window with the desktop helper (docs/testing/desktop-helper/desk.py + scn.py) to run deterministic in-game scenarios, or to write a new one. Use to verify gameplay/GUI/rendering behaviour that the automated suites cannot (real clicks, GUIs, pixels, third-person views). Works on 1.14.4, 1.16.5 and 1.20.6 (SCN_MC=1.18).
---

# Scripted playtest (desk.py + scn.py)

> **Version notes (1.20.6):** `SCN_MC=1.20.6` selects the 1.20.6 helper paths (`sdata`/`stack_nbt` for data-component commands, `EFFECTS_PATH`, `FLUID_WATER`, MC205 flag; pause-menu Save and Quit at y=383; chat commands are limited to 256 chars). All 28 scenarios pass on the dev client and the real install. Needs an unlocked desktop with Minecraft in front; ask for an uninterrupted window first.

Source of truth: `docs/testing/desktop-helper/{desk.py,scn.py,README.md}` and the scenario list in `docs/testing/README.md`. Live copies run from the untracked `prod-test/desktop/` (copy changes back to `docs/testing/desktop-helper/` and keep them identical). Windows only (ctypes + Win32).

## 1.18.2 status: adapted, all 26 scenarios pass (2026-10-15, dev client)
`SCN_MC=1.18` sets `MC18` (and, like 1.16, `MC16`: registry-string dimensions, `generic.max_health`, `/locatebiome`). What differs from 1.16 and is handled in scn.py:
- **`/replaceitem` no longer exists** (removed in 1.17): `item_cmd(slot, item)` emits `/item replace entity @s <slot> with <item>`.
- **Superflat ground is at y -61** (world bottom -64) while every scenario assumes ground at y 3: `controlled_setup` lays a 180x180 grass floor at y 3 in flat worlds.
- **Biome name** `minecraft:snowy_plains` (was `snowy_tundra`).
- Menus (title, world list, create-world, More World Options) are pixel-identical to 1.16, so the click coordinates did not change. The window title is `Minecraft* 1.18.2` (the helper matches on "Minecraft").
- Window runs: start the dev client first (`./gradlew runClient` without autotest flags), wait for the title screen; start a fresh client when the mod code changed.
What the playtest found on 1.18.2 that no suite had: the Item renderer coremod targeted the wrong class (dye/glint tint never applied), dye colour values (`DyeColor.getColorValue` has no 1.18 equivalent, see `DyeUtil.rgb`), and the particle probe's reflection filter.

## Running
- Start the client first (`./gradlew runClient`, or the production client - see `prod-jar-check`), wait for the title screen, then from `prod-test/desktop/`: `SCN_MC=<ver> python -u scn.py all` (or scenario names; `list` shows them). **Always `python -u`** when redirecting output. Production client: also `SCN_RUN=<abs path>/prod-test/<ver>/client-run`.
- Each run creates a fresh seed-424242 world per world kind, wipes between scenarios and deletes `Scn_*` worlds at the end. Results: stdout and `prod-test/desktop/scenario_results.txt`; screenshots in `prod-test/desktop/shots/` (read them with the Read tool).
- **The user needs Minecraft in front the whole run** (~15 min for `all`). The helper pauses when focus is lost and **aborts if it happens while typing**. Ask for an uninterrupted window first. Stop keys: Ctrl+Alt+Shift, Pause, Scroll Lock, F12, or `python desk.py stop` (`resume` removes the STOP file). If the game crashed the run hangs in "paused" - check `tasklist | grep java` and `run/crash-reports/`.
- After an aborted run the game is still inside `Scn_*`: `python desk.py key escape`, `python desk.py click 426 347` (Save and Quit), then `rm -rf run/saves/Scn_*`.

## Writing a scenario (`@scenario` in scn.py)
- Everything that can be a command is a command: `g.tp`/`aim_at(g, x,y,z, frm=...)`, `/setblock`, `/fill`, `g.slot(n, item)` into exact hotbar slots, `dv(g, 'block x y z', 'path')` for NBT values, `block_is`/`block_isnt`/`entity_count`, `inv(g)`.
- **Negative checks must prove the command ran**: `block_isnt`, never `not block_is(...)`.
- **Every GUI open must be guarded** with `gui_open(g, w, h, 'name')`; Escape without a GUI opens the pause menu and later clicks hit "Save and Quit".
- GUI slot pixels: `gui_slot(w,h,sx,sy)`, `hotbar_xy(w,h,n)` (854x480 client = GUI scale 2). Commands cannot be typed while a container GUI is open.
- Pixels: `desk.grab('Minecraft')` + `desk.frame_px(frame,x,y)`; thousands of `desk.pixel()` calls look like a hang. Hide the HUD with F1 while measuring.
- Measure rendering by **subtracting a reference** and include a **control**; **mutation-test** every visual check (break the thing, confirm the scenario fails, restore).
- Version differences go into scn.py flags, not forked scenarios.
- The sound check reads `[RT-DEBUG] muted sound ...` and the Magic Hood check reads `ParticleProbe` lines - dev client only.
- Add the scenario's rows to `TESTING_CHECKLIST.md` as `[verified by scripted playtest scenario <date>]` and to `docs/testing/README.md`.
