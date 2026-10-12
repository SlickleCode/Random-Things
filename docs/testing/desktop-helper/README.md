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
