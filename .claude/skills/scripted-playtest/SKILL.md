---
name: scripted-playtest
description: Drive the real Minecraft window with the desktop helper (docs/testing/desktop-helper/desk.py + scn.py) to run deterministic in-game scenarios, or to write a new one. Use to verify gameplay/GUI/rendering behaviour that the automated suites cannot (real clicks, GUIs, pixels, third-person views), on dev or production clients, 1.14.4 or 1.16.5.
---

# Scripted playtest (desk.py + scn.py)

Source of truth: `docs/testing/desktop-helper/{desk.py,scn.py,README.md}` and the scenario list in `docs/testing/README.md`. Live copies run from the untracked `prod-test/desktop/` (copy changes back to `docs/testing/desktop-helper/` and keep them identical). Windows only (ctypes + Win32).

## Running
- Start the client first (`./gradlew runClient`, or the production client - see `prod-jar-check`), wait for the title screen, then from `prod-test/desktop/`:
  `SCN_MC=1.16 python -u scn.py all` (or scenario names; `list` shows them; `new` = everything after the first four). **Always `python -u`** when redirecting output. 1.14.4 needs no `SCN_MC`. Production client: also `SCN_RUN=<abs path>/prod-test/<ver>/client-run`.
- Each run creates a fresh seed-424242 world per world kind (flat first, then the default-world scenarios), wipes between scenarios, and deletes `Scn_*` worlds at the end. Results: stdout and `prod-test/desktop/scenario_results.txt`; screenshots in `prod-test/desktop/shots/` (read them with the Read tool - they settle most doubts).
- **The user needs Minecraft in front the whole run** (~15 min for `all`). The helper pauses when focus is lost and **aborts if it happens while typing**. Ask the user for an uninterrupted window first. Stop keys: hold Ctrl+Alt+Shift, Pause, Scroll Lock, F12, or `python desk.py stop` (`resume` removes the STOP file). If the game is gone/crashed the run hangs in "paused" - check `tasklist | grep java` and `run/crash-reports/`.
- After an aborted run the game is still inside `Scn_*`: `python desk.py key escape`, `python desk.py click 426 347` (Save and Quit), then `rm -rf run/saves/Scn_*`.

## Writing a scenario (`@scenario` in scn.py)
- Everything that can be a command is a command: `g.tp`/`aim_at(g, x,y,z, frm=...)` for exact aim (reach is 5 blocks - use `frm` for far targets), `/setblock`, `/fill`, `g.slot(n, item)` (`/replaceitem` into exact hotbar slots so GUI click coordinates are constant), `dv(g, 'block x y z', 'path')` for NBT values, `block_is`/`block_isnt`/`entity_count`, `inv(g)`.
- **Negative checks must prove the command ran**: use `block_isnt` (`execute unless block` -> "Test passed"), never `not block_is(...)` (a command typed into the wrong screen "passes").
- **Every GUI open must be guarded** with `gui_open(g, w, h, 'name')` (it aborts the run on failure). Pressing Escape without a GUI opens the pause menu and later clicks hit "Save and Quit".
- GUI slot pixels: `gui_slot(w,h,sx,sy)`, `hotbar_xy(w,h,n)` (854x480 client = GUI scale 2). Commands cannot be typed while a container GUI is open - close with Escape, read state, reopen.
- Pixels: use `desk.grab('Minecraft')` + `desk.frame_px(frame,x,y)`. Thousands of `desk.pixel()` calls take minutes and look like a hang. Hide the HUD/chat with F1 while measuring.
- Measure rendering by **subtracting a reference** (same pose, plain item/armor) rather than absolute colours, and include a **control** (e.g. empty state must show nothing; the unmodified glint must differ) so a pass cannot be vacuous. After writing a visual check, **mutation-test it**: break the thing on purpose, confirm the scenario fails, restore.
- Version differences are handled in scn.py (`MC16`, `DIM` registry-string dimension tag, `generic.max_health`, world-creation clicks, `locatebiome`). Add new ones there rather than forking scenarios.
- The sound check reads `[RT-DEBUG] muted sound ...` and the Magic Hood check reads `ParticleProbe` lines (file `run/rt_probe` enables it) - dev client only.
- Add the scenario's rows to `TESTING_CHECKLIST.md` as `[verified by scripted playtest scenario <date>]` and to the scenario list in `docs/testing/README.md`.
