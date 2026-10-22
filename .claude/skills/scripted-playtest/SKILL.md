---
name: scripted-playtest
description: Drive the real Minecraft window with the desktop helper (docs/testing/desktop-helper/desk.py + scn.py) to run deterministic in-game scenarios, or to write a new one. Use to verify gameplay/GUI/rendering behaviour that the automated suites cannot (real clicks, GUIs, pixels, third-person views).
---

# Scripted playtest (desk.py + scn.py)

Source of truth: `docs/testing/desktop-helper/{desk.py,scn.py,README.md}` and the scenario list in `docs/testing/README.md`. Live copies run from the untracked `prod-test/desktop/` (copy changes back to `docs/testing/desktop-helper/` and keep them identical). Windows only (ctypes + Win32).

**Which `SCN_MC` value to use, what differs from earlier versions, and the pass status on this branch are in `docs/project/version-notes.md`, section "scripted-playtest". Read it first.**

## Rules
- Needs an unlocked desktop with Minecraft in front. **Ask the user for an uninterrupted window first**; pause rather than steal focus, and one world per session.
- Everything that can be a command is a command: teleports, `/setblock`, `/fill`, stacks into exact slots, data reads via the server. Click only what the scenario is about.
- Deterministic scripted runs: fixed seeds and coordinates; no sleeps where a state poll works.
- Chat commands are length-limited (256 chars on recent versions); the helpers shorten long item data.
- For testing by hand rather than scripts, use the manual world (`RT_AUTOTEST=true RT_MANUAL=true ./gradlew runClient`, `docs/testing/manual-test-world.md`).
