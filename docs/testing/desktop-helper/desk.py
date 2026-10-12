"""Small, deliberately restricted desktop helper for driving the Minecraft window (mouse, keyboard, screenshots).

Safety rules (all enforced in code):
  * Input is only ever sent while the FOREGROUND window's title contains --title (default "Minecraft") AND it belongs to a java process
    (--proc java, 'any' disables); otherwise the command
    refuses and sends nothing. Mouse targets must lie inside that window's client area.
  * Holding Ctrl+Alt+Shift together (or Pause/Break, Scroll Lock, F12) aborts the running command and every wait; a file called STOP next to
    this script (python desk.py stop) blocks all input.
  * Every action is appended to actions.log; --shot saves a screenshot of the window after the action.
  * No shell access, no other windows, no clipboard.

usage: python desk.py [--title Minecraft] [--shot] [--autofocus] <command> ...   (--autofocus re-raises the Minecraft window before each input if another window took focus)
  status                      foreground window title/rect, whether input would be allowed
  focus                       bring the target (java Minecraft) window to the foreground - the only window this tool ever raises
  shot [name]                 screenshot of the target window's client area (allowed even if not foreground)
  move X Y                    cursor to X,Y (client coordinates)
  click X Y [left|right|middle] [--double] [--shift]
  hold X Y [left|right] SECONDS   hold a mouse button (drinking/eating)
  drag X1 Y1 X2 Y2 [left|right]
  look DX DY                  relative mouse movement (in-game camera while the cursor is grabbed)
  scroll N                    wheel notches (positive = up)
  key NAME [--hold SECONDS] [--shift] [--ctrl] [--alt]    e.g. key e / key escape / key f3 / key w --hold 1.5
  text "string"               types text (chat/command box; unicode characters)
  wait SECONDS
  stop / resume               create / remove the STOP file (blocks every input command)
  batch                       read commands, one per line, from stdin (same syntax, no leading 'python desk.py')
"""
import ctypes
import ctypes.wintypes as wt
import os
import shlex
import struct
import sys
import time
import zlib

user32 = ctypes.windll.user32
gdi32 = ctypes.windll.gdi32
try:
    ctypes.windll.shcore.SetProcessDpiAwareness(2)
except Exception:
    user32.SetProcessDPIAware()

HERE = os.path.dirname(os.path.abspath(__file__))
SHOTS = os.path.join(HERE, 'shots')
LOG = os.path.join(HERE, 'actions.log')
STOP = os.path.join(HERE, 'STOP')

INPUT_MOUSE, INPUT_KEYBOARD = 0, 1
MOUSEEVENTF_MOVE, MOUSEEVENTF_LEFTDOWN, MOUSEEVENTF_LEFTUP = 0x1, 0x2, 0x4
MOUSEEVENTF_RIGHTDOWN, MOUSEEVENTF_RIGHTUP = 0x8, 0x10
MOUSEEVENTF_MIDDLEDOWN, MOUSEEVENTF_MIDDLEUP = 0x20, 0x40
MOUSEEVENTF_WHEEL = 0x800
KEYEVENTF_EXTENDEDKEY, KEYEVENTF_KEYUP, KEYEVENTF_UNICODE, KEYEVENTF_SCANCODE = 0x1, 0x2, 0x4, 0x8


class MOUSEINPUT(ctypes.Structure):
    _fields_ = [('dx', wt.LONG), ('dy', wt.LONG), ('mouseData', wt.DWORD), ('dwFlags', wt.DWORD), ('time', wt.DWORD), ('dwExtraInfo', ctypes.c_size_t)]


class KEYBDINPUT(ctypes.Structure):
    _fields_ = [('wVk', wt.WORD), ('wScan', wt.WORD), ('dwFlags', wt.DWORD), ('time', wt.DWORD), ('dwExtraInfo', ctypes.c_size_t)]


class HARDWAREINPUT(ctypes.Structure):
    _fields_ = [('uMsg', wt.DWORD), ('wParamL', wt.WORD), ('wParamH', wt.WORD)]


class _U(ctypes.Union):
    _fields_ = [('mi', MOUSEINPUT), ('ki', KEYBDINPUT), ('hi', HARDWAREINPUT)]


class INPUT(ctypes.Structure):
    _anonymous_ = ('u',)
    _fields_ = [('type', wt.DWORD), ('u', _U)]


def send(inp):
    if user32.SendInput(1, ctypes.byref(inp), ctypes.sizeof(INPUT)) != 1:
        raise RuntimeError('SendInput failed')


def mouse(flags, dx=0, dy=0, data=0):
    i = INPUT(type=INPUT_MOUSE)
    i.mi = MOUSEINPUT(dx, dy, data, flags, 0, 0)
    send(i)


VK = {'enter': 0x0D, 'return': 0x0D, 'escape': 0x1B, 'esc': 0x1B, 'tab': 0x09, 'space': 0x20, 'backspace': 0x08, 'delete': 0x2E,
      'shift': 0x10, 'ctrl': 0x11, 'alt': 0x12, 'up': 0x26, 'down': 0x28, 'left': 0x25, 'right': 0x27, 'home': 0x24, 'end': 0x23,
      'pageup': 0x21, 'pagedown': 0x22, 'slash': 0xBF, '/': 0xBF, 'period': 0xBE, 'comma': 0xBC, 'minus': 0xBD, 'equals': 0xBB}
for n in range(1, 13):
    VK['f%d' % n] = 0x6F + n
for c in 'abcdefghijklmnopqrstuvwxyz':
    VK[c] = ord(c.upper())
for d in '0123456789':
    VK[d] = ord(d)
EXTENDED = {0x26, 0x28, 0x25, 0x27, 0x24, 0x23, 0x21, 0x22, 0x2E}


def key_event(vk, up=False):
    scan = user32.MapVirtualKeyW(vk, 0)
    flags = KEYEVENTF_SCANCODE | (KEYEVENTF_KEYUP if up else 0) | (KEYEVENTF_EXTENDEDKEY if vk in EXTENDED else 0)
    i = INPUT(type=INPUT_KEYBOARD)
    i.ki = KEYBDINPUT(0, scan, flags, 0, 0)
    send(i)


def unicode_event(ch, up=False):
    i = INPUT(type=INPUT_KEYBOARD)
    i.ki = KEYBDINPUT(0, ord(ch), KEYEVENTF_UNICODE | (KEYEVENTF_KEYUP if up else 0), 0, 0)
    send(i)


# ---------------------------------------------------------------- window helpers
def fg_hwnd():
    return user32.GetForegroundWindow()


def title_of(hwnd):
    n = user32.GetWindowTextLengthW(hwnd)
    buf = ctypes.create_unicode_buffer(n + 1)
    user32.GetWindowTextW(hwnd, buf, n + 1)
    return buf.value


PROC = 'java'  # the window must belong to a process whose image name contains this ('any' disables the check)


def proc_name(hwnd):
    pid = wt.DWORD()
    user32.GetWindowThreadProcessId(hwnd, ctypes.byref(pid))
    h = ctypes.windll.kernel32.OpenProcess(0x1000, False, pid.value)  # PROCESS_QUERY_LIMITED_INFORMATION
    if not h:
        return ''
    buf = ctypes.create_unicode_buffer(520)
    size = wt.DWORD(520)
    ctypes.windll.kernel32.QueryFullProcessImageNameW(h, 0, buf, ctypes.byref(size))
    ctypes.windll.kernel32.CloseHandle(h)
    return os.path.basename(buf.value).lower()


def proc_ok(hwnd):
    return PROC == 'any' or PROC in proc_name(hwnd)


def find_window(substr):
    found = []

    def cb(h, _):
        if user32.IsWindowVisible(h) and substr.lower() in title_of(h).lower() and proc_ok(h):
            found.append(h)
        return True

    user32.EnumWindows(ctypes.WINFUNCTYPE(wt.BOOL, wt.HWND, wt.LPARAM)(cb), 0)
    return found[0] if found else None


def client_rect(hwnd):
    r = wt.RECT()
    user32.GetClientRect(hwnd, ctypes.byref(r))
    p = wt.POINT(0, 0)
    user32.ClientToScreen(hwnd, ctypes.byref(p))
    return p.x, p.y, r.right - r.left, r.bottom - r.top


class Abort(Exception):
    pass


def _down(vk):
    return bool(user32.GetAsyncKeyState(vk) & 0x8000)


def check_abort():
    """Abort triggers: Ctrl+Alt+Shift held together, Pause/Break, Scroll Lock, F12, or a file named STOP next to this script. The message names which fired."""
    if _down(0x11) and _down(0x12) and _down(0x10):
        raise Abort('aborted: Ctrl+Alt+Shift was held (manual stop)')
    for vk, name in ((0x13, 'Pause/Break'), (0x91, 'Scroll Lock'), (0x7B, 'F12')):
        if _down(vk):
            raise Abort('aborted: %s was held' % name)
    if os.path.exists(STOP):
        raise Abort('aborted: STOP file present (python desk.py resume removes it)')


def sleep(seconds):
    """time.sleep that notices an abort within 50 ms."""
    end = time.time() + seconds
    while time.time() < end:
        check_abort()
        time.sleep(min(0.05, max(0.0, end - time.time())))


def log(msg):
    with open(LOG, 'a', encoding='utf-8') as f:
        f.write('%s  %s\n' % (time.strftime('%Y-%m-%d %H:%M:%S'), msg))


def gate(title, point=None):
    """Raise unless the target window is in the foreground (and the optional client point lies inside it)."""
    check_abort()
    fg = fg_hwnd()
    t = title_of(fg)
    if title.lower() not in t.lower() or not proc_ok(fg):
        raise Abort('refused: foreground window is %r (process %r), not a %r window of a %r process - nothing was sent' % (t, proc_name(fg), title, PROC))
    x, y, w, h = client_rect(fg)
    if point is not None and not (0 <= point[0] < w and 0 <= point[1] < h):
        raise Abort('refused: point %s is outside the window client area %dx%d' % (point, w, h))
    return fg, x, y, w, h


def pixel(title, x, y):
    """(r, g, b) of a client-area pixel of the target window (used to confirm which screen is showing before clicking)."""
    hwnd = find_window(title)
    if not hwnd:
        raise Abort('no visible window titled %r' % title)
    ox, oy, w, h = client_rect(hwnd)
    hdc = user32.GetDC(0)
    c = gdi32.GetPixel(hdc, ox + x, oy + y)
    user32.ReleaseDC(0, hdc)
    return (c & 0xff, (c >> 8) & 0xff, (c >> 16) & 0xff)


# ---------------------------------------------------------------- screenshot
def save_png(path, w, h, bgra):
    rows = bytearray()
    stride = w * 4
    for y in range(h):
        rows.append(0)
        line = bgra[y * stride:(y + 1) * stride]
        rgb = bytearray(w * 3)
        rgb[0::3] = line[2::4]
        rgb[1::3] = line[1::4]
        rgb[2::3] = line[0::4]
        rows += rgb

    def chunk(tag, data):
        c = struct.pack('>I', len(data)) + tag + data
        return c + struct.pack('>I', zlib.crc32(tag + data) & 0xffffffff)

    png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 2, 0, 0, 0)) + chunk(b'IDAT', zlib.compress(bytes(rows), 6)) + chunk(b'IEND', b'')
    open(path, 'wb').write(png)


def grab(title):
    """One fast frame grab of the client area: returns (w, h, bgra bytes); read pixels with frame_px(). Far quicker than many pixel() calls."""
    hwnd = find_window(title)
    if not hwnd:
        raise Abort('no visible window titled %r' % title)
    x, y, w, h = client_rect(hwnd)
    hdc = user32.GetDC(0)
    mem = gdi32.CreateCompatibleDC(hdc)
    bmp = gdi32.CreateCompatibleBitmap(hdc, w, h)
    gdi32.SelectObject(mem, bmp)
    gdi32.BitBlt(mem, 0, 0, w, h, hdc, x, y, 0x00CC0020)
    bi = struct.pack('<IiiHHIIiiII', 40, w, -h, 1, 32, 0, 0, 0, 0, 0, 0)
    buf = ctypes.create_string_buffer(w * h * 4)
    gdi32.GetDIBits(mem, bmp, 0, h, buf, bi, 0)
    gdi32.DeleteObject(bmp)
    gdi32.DeleteDC(mem)
    user32.ReleaseDC(0, hdc)
    return w, h, bytes(buf)


def frame_px(frame, x, y):
    w, h, buf = frame
    i = (y * w + x) * 4
    return buf[i + 2], buf[i + 1], buf[i]


def screenshot(title, name=None):
    hwnd = find_window(title)
    if not hwnd:
        raise Abort('no visible window titled %r' % title)
    x, y, w, h = client_rect(hwnd)
    hdc = user32.GetDC(0)
    mem = gdi32.CreateCompatibleDC(hdc)
    bmp = gdi32.CreateCompatibleBitmap(hdc, w, h)
    gdi32.SelectObject(mem, bmp)
    gdi32.BitBlt(mem, 0, 0, w, h, hdc, x, y, 0x00CC0020)  # SRCCOPY
    bi = struct.pack('<IiiHHIIiiII', 40, w, -h, 1, 32, 0, 0, 0, 0, 0, 0)
    buf = ctypes.create_string_buffer(w * h * 4)
    gdi32.GetDIBits(mem, bmp, 0, h, buf, bi, 0)
    gdi32.DeleteObject(bmp)
    gdi32.DeleteDC(mem)
    user32.ReleaseDC(0, hdc)
    os.makedirs(SHOTS, exist_ok=True)
    path = os.path.join(SHOTS, (name or time.strftime('%H%M%S')) + '.png')
    save_png(path, w, h, bytes(buf))
    return path


# ---------------------------------------------------------------- commands
AUTOFOCUS = False


def foreground_ok(title):
    fg = fg_hwnd()
    return title.lower() in title_of(fg).lower() and proc_ok(fg)


def wait_foreground(title, limit=900):
    """Pause (do nothing, send nothing) while the game is not the foreground window, e.g. while you type in another window.
    Resumes by itself as soon as you click back into the game; gives up with an abort after `limit` seconds."""
    noted = False
    end = time.time() + limit
    while not foreground_ok(title):
        if not noted:
            print('  (paused: Minecraft is not the foreground window - click the game to resume)', flush=True)
            noted = True
        if time.time() > end:
            raise Abort('gave up waiting for the Minecraft window to come back to the foreground')
        sleep(0.3)
    if noted:
        print('  (resumed)', flush=True)
        sleep(0.5)



def do_command(args, title, want_shot):
    cmd, a = args[0], args[1:]
    if AUTOFOCUS and cmd not in ('status', 'shot', 'wait', 'focus') and not (title.lower() in title_of(fg_hwnd()).lower() and proc_ok(fg_hwnd())):
        do_command(['focus'], title, False)
    flags = {x for x in a if x.startswith('--')}
    pos = [x for x in a if not x.startswith('--')]
    out = ''

    if cmd == 'status':
        fg = fg_hwnd()
        win = find_window(title)
        allowed = title.lower() in title_of(fg).lower() and proc_ok(fg)
        out = 'foreground=%r  target_window=%s  client_rect=%s  input_allowed=%s' % (title_of(fg), 'found' if win else 'NOT FOUND', client_rect(win) if win else None, allowed)
    elif cmd == 'focus':
        check_abort()
        win = find_window(title)
        if not win:
            raise Abort('no visible window titled %r of a %r process' % (title, PROC))
        user32.ShowWindow(win, 9)  # SW_RESTORE
        fg = fg_hwnd()
        cur = ctypes.windll.kernel32.GetCurrentThreadId()
        fg_thread = user32.GetWindowThreadProcessId(fg, None)
        user32.AttachThreadInput(cur, fg_thread, True)
        user32.SetForegroundWindow(win)
        user32.AttachThreadInput(cur, fg_thread, False)
        time.sleep(0.4)
        out = 'foreground now %r' % title_of(fg_hwnd())
    elif cmd == 'shot':
        if title.lower() not in title_of(fg_hwnd()).lower():
            print('note: the target window is not in the foreground; the screenshot may show other windows on top of it (run: focus)')
        out = screenshot(title, pos[0] if pos else None)
    elif cmd == 'wait':
        sleep(float(pos[0]))
    elif cmd == 'stop':
        open(STOP, 'w').write('stop')
        out = 'STOP file created: all input is blocked until it is deleted (python desk.py resume)'
    elif cmd == 'resume':
        if os.path.exists(STOP):
            os.remove(STOP)
        out = 'STOP file removed'
    elif cmd == 'move':
        x, y = int(pos[0]), int(pos[1])
        _, ox, oy, _, _ = gate(title, (x, y))
        user32.SetCursorPos(ox + x, oy + y)
    elif cmd == 'click':
        x, y = int(pos[0]), int(pos[1])
        button = pos[2] if len(pos) > 2 else 'left'
        _, ox, oy, _, _ = gate(title, (x, y))
        user32.SetCursorPos(ox + x, oy + y)
        sleep(0.05)
        down, up = {'left': (MOUSEEVENTF_LEFTDOWN, MOUSEEVENTF_LEFTUP), 'right': (MOUSEEVENTF_RIGHTDOWN, MOUSEEVENTF_RIGHTUP), 'middle': (MOUSEEVENTF_MIDDLEDOWN, MOUSEEVENTF_MIDDLEUP)}[button]
        if '--shift' in flags:
            key_event(VK['shift'])
            sleep(0.25)          # sneaking is read once per game tick: let it register before the click
        for _ in range(2 if '--double' in flags else 1):
            mouse(down)
            sleep(0.04)
            mouse(up)
            sleep(0.06)
        if '--shift' in flags:
            key_event(VK['shift'], up=True)
            sleep(0.15)
    elif cmd == 'hold':
        # hold a mouse button (e.g. to drink or eat): hold X Y [left|right] SECONDS
        x, y = int(pos[0]), int(pos[1])
        button = pos[2] if len(pos) > 3 else 'left'
        seconds = float(pos[-1])
        _, ox, oy, _, _ = gate(title, (x, y))
        down, up = (MOUSEEVENTF_LEFTDOWN, MOUSEEVENTF_LEFTUP) if button == 'left' else (MOUSEEVENTF_RIGHTDOWN, MOUSEEVENTF_RIGHTUP)
        user32.SetCursorPos(ox + x, oy + y)
        mouse(down)
        end = time.time() + seconds
        try:
            while time.time() < end:
                check_abort()
                time.sleep(0.02)
        finally:
            mouse(up)
    elif cmd == 'drag':
        x1, y1, x2, y2 = (int(v) for v in pos[:4])
        button = pos[4] if len(pos) > 4 else 'left'
        _, ox, oy, _, _ = gate(title, (x1, y1))
        gate(title, (x2, y2))
        down, up = (MOUSEEVENTF_LEFTDOWN, MOUSEEVENTF_LEFTUP) if button == 'left' else (MOUSEEVENTF_RIGHTDOWN, MOUSEEVENTF_RIGHTUP)
        user32.SetCursorPos(ox + x1, oy + y1)
        time.sleep(0.05)
        mouse(down)
        for step in range(1, 11):
            user32.SetCursorPos(ox + x1 + (x2 - x1) * step // 10, oy + y1 + (y2 - y1) * step // 10)
            time.sleep(0.02)
        mouse(up)
    elif cmd == 'look':
        gate(title)
        mouse(MOUSEEVENTF_MOVE, int(pos[0]), int(pos[1]))
    elif cmd == 'scroll':
        gate(title)
        mouse(MOUSEEVENTF_WHEEL, 0, 0, int(pos[0]) * 120 & 0xffffffff)
    elif cmd == 'key':
        name = pos[0].lower()
        if name not in VK:
            raise Abort('unknown key %r' % name)
        gate(title)
        hold = 0.05
        if '--hold' in a:
            hold = float(a[a.index('--hold') + 1])
            pos = [p for p in pos if p != str(hold) and p != a[a.index('--hold') + 1]]
        mods = [m for m in ('shift', 'ctrl', 'alt') if '--' + m in flags]
        for m in mods:
            key_event(VK[m])
        key_event(VK[name])
        end = time.time() + hold
        while time.time() < end:
            check_abort()
            time.sleep(0.02)
        key_event(VK[name], up=True)
        for m in reversed(mods):
            key_event(VK[m], up=True)
    elif cmd == 'text':
        gate(title)
        for ch in ' '.join(pos):
            check_abort()
            if not foreground_ok(title):
                raise Abort('stopped typing: Minecraft lost the foreground (nothing further was typed)')
            unicode_event(ch)
            unicode_event(ch, up=True)
            time.sleep(0.012)
    else:
        raise Abort('unknown command %r' % cmd)

    if want_shot and cmd not in ('status', 'shot', 'wait', 'focus'):
        time.sleep(0.7)  # let the game process the input (camera moves land a frame or two later)
        out = (out + ' ' if out else '') + screenshot(title)
    return out


def main(argv):
    title = 'Minecraft'
    want_shot = False
    argv = list(argv)
    if '--title' in argv:
        i = argv.index('--title')
        title = argv[i + 1]
        del argv[i:i + 2]
    if '--proc' in argv:
        global PROC
        i = argv.index('--proc')
        PROC = argv[i + 1].lower()
        del argv[i:i + 2]
    if '--autofocus' in argv:
        global AUTOFOCUS
        AUTOFOCUS = True
        argv.remove('--autofocus')
    if '--shot' in argv:
        want_shot = True
        argv.remove('--shot')
    if not argv:
        print(__doc__)
        return 2
    try:
        if argv[0] == 'batch':
            for line in sys.stdin:
                line = line.strip()
                if not line or line.startswith('#'):
                    continue
                parts = shlex.split(line)
                log(' '.join(parts))
                res = do_command(parts, title, want_shot)
                print(res if res else 'ok: ' + line)
        else:
            log(' '.join(argv))
            res = do_command(argv, title, want_shot)
            print(res if res else 'ok')
        return 0
    except Abort as e:
        log(str(e))
        print(str(e))
        return 3


if __name__ == '__main__':
    sys.exit(main(sys.argv[1:]))
