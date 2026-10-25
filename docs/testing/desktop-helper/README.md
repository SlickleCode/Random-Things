# Desktop helper for driving Minecraft (untracked, dev only)

`desk.py` lets Claude (or you) click, type and take screenshots in the Minecraft window. Python 3 standard library only (ctypes + Win32).

Safety (enforced in code): input is only sent while the **foreground** window is titled "Minecraft" **and** belongs to a `java` process; otherwise the command refuses
and sends nothing. Mouse targets must be inside the window. **Hold Ctrl+Alt+Shift together** (or Pause/Break, Scroll Lock, F12; or create a file named `STOP` here with `python desk.py stop`) to abort; the message says which fired. Every action goes to `actions.log`.
`focus` is the only command that raises a window (the Minecraft one). It never touches other windows, the clipboard or a shell.

```
python desk.py status | focus | shot [name] | wait S
python desk.py [--shot] click X Y [left|right|middle] [--double] [--shift]
python desk.py [--shot] move X Y | drag X1 Y1 X2 Y2 | look DX DY | scroll N
python desk.py [--shot] key NAME [--hold S] [--shift] [--ctrl] [--alt]     (e, escape, enter, f3, slash, w --hold 1.5, ...)
python desk.py text "any text"           (typed into whatever field/chat has focus)
python desk.py batch < steps.txt         (one command per line)
```
Coordinates are client-area pixels of the game window (0,0 = top-left of the game picture). `--shot` saves `shots/HHMMSS.png` ~0.7 s after the action.
Verified on 1.14.4 (dev client): menu clicks, Ctrl+A, typing, creating a world, chat commands, inventory, Escape, camera look.
Tips: Minecraft must be visible (not minimised/covered) for screenshots; the window is 854x480 by default; use `/gamemode creative` and `/give` instead of crafting.

## Speed (what makes a playtest fast)
Measured on 1.21.11 (28 scenarios): typing every chat command key by key (~1 s per 90-character command plus a 0.5 s settle) was the biggest cost, then fixed `g.wait()` sleeps and Python pixel loops.
- **Command channel (dev client only):** `client/CommandChannel` keeps `run/rt_cmd_ready` while the dev client runs; `scn.py`'s `Game.cmd` then writes the command to `run/rt_cmd.txt`, the client sends it on the next tick exactly as the chat box would, and `cmd` returns once the chat answer has been logged (log quiet for 0.2 s). Falls back to typing when the marker is missing (production clients, older jars) or when `SCN_FASTCMD=0`. Typing is still the path for the chat-screen itself; nothing else in the scenarios tests the chat box.
- **Poll instead of sleeping:** `until(g, pred, limit, step)` waits exactly as long as a condition needs (falling blocks, grass spread, ...). A fixed wait that is "long enough" for the slow typed flow is too short when commands are fast: three scenarios flaked right after the channel went in, for that reason. Use `until` for anything that happens in game time.
- **One frame grab per measurement:** `desk.grab` + `desk.frame_px` (one screen copy), never loops of `desk.pixel` (one screen copy per pixel, ~100 s in the dyeing scenario).
- Every scenario prints `timing N s: cmd .. x .. s, input .., shot .., wait ..` at its end: the first place to look when a run gets slower.
