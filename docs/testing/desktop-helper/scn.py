"""Deterministic scenario runner for the Minecraft playtest (builds on desk.py).

A scenario is a Python function using a Game object. Everything that can be a command is a command, so runs are repeatable:
  * placement / aim:   /fill, /setblock, /tp @s x y z yaw pitch  (no mouse-look, no aiming by eye)
  * items:             /replaceitem (1.18: /item replace entity @s hotbar.N with <item>)     (items land in exact slots, so GUI click coordinates are constant)
  * assertions:        run a command and read its [CHAT] output from run/logs/latest.log (e.g. /data get ..., /execute if ...)
  * evidence:          screenshots at named checkpoints only
GUI clicks use the fixed 854x480 client layout of the vanilla container GUIs (coordinates documented where used).

usage:  python scn.py <scenario> [...]      list scenarios: python scn.py list
"""
import os
import re
import shutil
import sys
import time

import desk

REPO = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
RUN_DIR = os.environ.get('SCN_RUN') or os.path.join(REPO, 'run')       # the game directory (set SCN_RUN to drive a production client in prod-test/<ver>/client-run)
LOG = os.path.join(RUN_DIR, 'logs', 'latest.log')
REPORT = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'scenario_results.txt')
desk.AUTOFOCUS = False      # never steal the focus back during a run: the runner pauses instead (desk.wait_foreground)


class Fail(Exception):
    pass


STATS = {}          # per scenario: seconds and call counts by kind (cmd / wait / input / shot), printed at finish()
FAST_FILE = os.path.join(RUN_DIR, 'rt_cmd.txt')
FAST_MARK = os.path.join(RUN_DIR, 'rt_cmd_ready')      # the dev client keeps this file while its command channel (client/CommandChannel) is running
FAST = os.environ.get('SCN_FASTCMD', '1') != '0'


def _tick(kind, t0):
    st = STATS.setdefault('cur', {})
    n, tot = st.get(kind, (0, 0.0))
    st[kind] = (n + 1, tot + time.time() - t0)


class Game:
    def __init__(self, name, minutes=10):
        self.name = name
        self.results = []
        self.deadline = time.time() + minutes * 60
        self.t_start = time.time()
        STATS['cur'] = {}

    # ---- low level -------------------------------------------------------------------
    def do(self, *args):
        t0 = time.time()
        if args and args[0] not in ('focus', 'status', 'shot', 'wait', 'stop', 'resume'):
            desk.wait_foreground('Minecraft')      # paused while you are in another window; resumes when you click the game
        r = desk.do_command(list(args), 'Minecraft', False)
        _tick('input', t0)
        return r

    def wait(self, s):
        if time.time() > self.deadline:
            raise desk.Abort('scenario exceeded its time limit')
        t0 = time.time()
        desk.sleep(s)
        _tick('wait', t0)

    def key(self, name, **kw):
        extra = []
        for k, v in kw.items():
            extra += ['--' + k, str(v)]
        self.do('key', name, *extra)

    def click(self, x, y, button='left', *flags):
        self.do('click', str(x), str(y), button, *flags)

    def hold(self, x, y, button, seconds):
        self.do('hold', str(x), str(y), button, str(seconds))

    def shot(self, label):
        t0 = time.time()
        path = desk.screenshot('Minecraft', '%s_%s' % (self.name, label))
        self.note('shot %s' % path)
        _tick('shot', t0)
        return path

    # ---- commands and chat -----------------------------------------------------------
    def _log_size(self):
        return os.path.getsize(LOG) if os.path.exists(LOG) else 0

    def mark(self):
        """Current size of latest.log; pass it to new_log() later to get everything logged since."""
        return self._log_size()

    def new_log(self, mark):
        with open(LOG, 'rb') as f:
            f.seek(mark)
            return f.read().decode('utf-8', errors='replace')

    def cmd(self, text, settle=0.5):
        """Run a chat command; return the [CHAT] lines it produced."""
        before = self._log_size()
        text = text.lstrip('/')
        if MC2111 and text.startswith('gamerule '):
            parts = text.split(' ')
            if parts[1] in GAMERULES:
                parts[1] = GAMERULES[parts[1]]
                if parts[1] == 'fire_spread_radius_around_player' and len(parts) > 2:
                    parts[2] = '-1' if parts[2] == 'true' else '0'      # doFireTick false (no spread) = radius 0
                if parts[1] in ('advance_time', 'advance_weather', 'spawn_mobs', 'show_advancement_messages'):
                    pass
            text = ' '.join(parts)
        t0 = time.time()
        if not (FAST and os.path.exists(FAST_MARK) and self._send_fast(text, before, settle)):
            self.do('key', 'slash')
            desk.sleep(0.15)
            self.do('text', text)
            self.do('key', 'enter')
            desk.sleep(settle)
        with open(LOG, 'rb') as f:
            f.seek(before)
            data = f.read().decode('utf-8', errors='replace')
        _tick('cmd', t0)
        return [l.split('[CHAT] ', 1)[1] for l in data.splitlines() if '[CHAT] ' in l]

    def _send_fast(self, text, before, settle):
        """Hand the command to the dev client's command channel (no key presses): written to rt_cmd.txt, consumed within a tick or two, answered in chat.
        Waits until the command's chat output has arrived (log quiet for 0.1 s after it grew), at most `settle` seconds. False = the channel did not take it."""
        tmp = FAST_FILE + '.tmp'
        with open(tmp, 'w', encoding='utf-8') as f:
            f.write(text)
        os.replace(tmp, FAST_FILE)
        end = time.time() + 3.0
        while os.path.exists(FAST_FILE) and time.time() < end:
            desk.sleep(0.02)
        if os.path.exists(FAST_FILE):
            try:
                os.remove(FAST_FILE)
            except OSError:
                pass
            return False
        t_end = time.time() + max(settle, 0.3)
        last, quiet_since = self._log_size(), None
        while time.time() < t_end:
            desk.sleep(0.03)
            size = self._log_size()
            if size != last:
                last, quiet_since = size, time.time()
            elif quiet_since is not None and time.time() - quiet_since >= 0.1 and size > before:
                break
        return True

    def tp(self, x, y, z, yaw=0, pitch=0):
        self.cmd('tp @s %s %s %s %s %s' % (x, y, z, yaw, pitch), 0.35)
        self.wait(0.25)                         # the client turns its camera when the position packet arrives: a click right after the command would still aim the old way

    def slot(self, n, item):
        """Put an item into hotbar slot n (0-8) exactly."""
        self.cmd(item_cmd('hotbar.%d' % n, item), 0.25)
        self.wait(0.2)                          # the inventory update reaches the client a tick after the chat answer; a GUI click right after the command would miss the item

    def select(self, n):
        self.key(str(n + 1))

    # ---- checks ---------------------------------------------------------------------
    def note(self, msg):
        print('  ' + msg)

    def check(self, ok, desc, evidence=''):
        self.results.append((ok, desc, evidence))
        print('  %s %s%s' % ('PASS' if ok else 'FAIL', desc, ('  [' + evidence + ']') if evidence else ''))
        return ok

    def expect_chat(self, lines, pattern, desc, _unused=None):
        text = '\n'.join(lines)
        return self.check(bool(re.search(pattern, text)), desc, text[:300])

    def finish(self):
        st = STATS.get('cur', {})
        total = time.time() - self.t_start
        print('  timing %.0f s: %s' % (total, ', '.join('%s %d x %.0f s' % (k, v[0], v[1]) for k, v in sorted(st.items()))))
        passed = sum(1 for r in self.results if r[0])
        line = '%s: %d/%d checks passed%s' % (self.name, passed, len(self.results), '' if passed == len(self.results) else '  FAILED: ' + '; '.join(r[1] for r in self.results if not r[0]))
        with open(REPORT, 'a', encoding='utf-8') as f:
            f.write(time.strftime('%Y-%m-%d %H:%M:%S ') + line + '\n')
        print(line)
        return passed == len(self.results)


def item_cmd(slot, item):
    """Put an item into a slot of the player: /replaceitem up to 1.16, /item replace (1.17+) - /replaceitem no longer exists on 1.18.2."""
    if MC18:
        return 'item replace entity @s %s with %s' % (slot, item)
    return 'replaceitem entity @s %s %s' % (slot, item)


SCENARIOS = {}
WORLD_KIND = {}
_MCV = os.environ.get('SCN_MC') or '1.14'
_VPARTS = [int(x) for x in re.findall(r'[0-9]+', _MCV)] + [0, 0, 0]
MC18 = _MCV.startswith('1.18')                    # SCN_MC=1.18 drives the 1.18.2 menus
MC16 = _MCV.startswith('1.16') or MC18            # "modern" (1.16+) command/NBT conventions: registry-string dimensions, generic.max_health, /locatebiome
DIM = '"minecraft:overworld"' if MC16 else '0'    # the dimension tag: a registry key string on 1.16+, an int id on 1.14
SNOWY_BIOME = 'minecraft:snowy_plains' if MC18 else 'minecraft:snowy_tundra'     # renamed in 1.18
SEED = '424242'      # the same seed in every session: structures, caves and biomes are identical between runs (and match realgen.py)

MC2111 = _VPARTS[:3] >= [1, 21, 11]             # 1.21.11: game rules are snake_case ids (minecraft:random_tick_speed), some were renamed
GAMERULES = {'randomTickSpeed': 'random_tick_speed', 'doDaylightCycle': 'advance_time', 'doWeatherCycle': 'advance_weather', 'doMobSpawning': 'spawn_mobs',
             'doFireTick': 'fire_spread_radius_around_player', 'announceAdvancements': 'show_advancement_messages'}
MC215 = _VPARTS[:3] >= [1, 21, 5]               # 1.21.5+: the enchantments component lost its 'levels' wrapper: enchantments={"minecraft:protection":1}

def scenario(fn=None, world='flat'):
    """Register a scenario. world='flat' (superflat, the default) or 'default' (the normal generator, for worldgen/structure scenarios)."""
    def register(f):
        SCENARIOS[f.__name__] = f
        WORLD_KIND[f.__name__] = world
        return f
    return register(fn) if fn is not None else register


# ---------------------------------------------------------------------------------------------
# Flat world facts: grass surface at y=3, so feet stand at y=4. Scenario arena: x 0..8, z 0..8.
# Aim: /tp @s 0.5 4 0.5 0 0  looks south (+z); eye height 1.62 -> eye y = 5.62.
# ---------------------------------------------------------------------------------------------
def arena(g):
    """Wipe everything a previous section left behind, so each section starts from the same state in the same world."""
    g.cmd('gamemode creative')
    g.cmd('weather clear')
    g.cmd('forceload remove all')
    g.cmd('kill @e[type=!player]')
    g.cmd('effect clear @s')
    g.cmd('clear @s')
    g.cmd('experience set @s 0 points')
    g.cmd('gamerule randomTickSpeed 3')
    g.cmd('fill -20 4 -20 20 20 20 air')
    g.tp(0.5, 4, 0.5, 0, 0)


TICKCMD = _VPARTS[:3] >= [1, 20, 3]          # 1.20.3+: /tick rate exists


def game_wait(g, seconds):
    """Wait `seconds` of GAME time (growth, burning, brewing, rain): on 1.20.3+ the tick rate is raised to 100 for the duration, so 13 s take about 3 s.
    Only for waits that are about the game's own clock; anything the client has to draw or the human eye has to see stays a plain g.wait()."""
    if TICKCMD and seconds >= 3:
        g.cmd('tick rate 100', 0.2)
        g.wait(seconds / 5.0 + 0.6)
        g.cmd('tick rate 20', 0.2)
    else:
        g.wait(seconds)


def until(g, pred, limit=30.0, step=0.4):
    """Poll pred() every `step` seconds until it holds or `limit` seconds passed; returns pred()'s last value. Replaces fixed waits: as fast as the game allows,
    as patient as needed."""
    t_end = time.time() + limit
    v = pred()
    while not v and time.time() < t_end:
        g.wait(step)
        v = pred()
    return v


@scenario
def imbuing(g):
    """#169 and #170-173: Imbuing Station recipes and the imbue potions, then drink Fire and check the effect."""
    arena(g)
    g.wait(1.0)                      # 1.19.2: without this pause the first right-click below was lost in a chat screen with a stray '/' (cause not found; the pause makes it go away)
    g.cmd('setblock 0 5 3 randomthings:imbuing_station')
    g.tp(0.5, 4, 0.5, 0, 2)          # crosshair on the station block 2 blocks ahead
    water = 'minecraft:potion{Potion:"minecraft:water"}'
    g.slot(0, 'minecraft:cobblestone')
    g.slot(1, 'minecraft:vine')
    g.slot(2, 'minecraft:bone_meal')
    g.slot(3, 'minecraft:coal')
    g.slot(4, 'minecraft:flint')
    g.slot(5, 'minecraft:blaze_powder')
    g.slot(6, water)
    g.slot(7, water)
    g.click(427, 240, 'right')       # right-click the station
    g.wait(0.9)
    g.shot('gui_open')
    g.do('move', '300', '300'); g.wait(1.2)      # the first GUI of a fresh world: park the cursor inside it and let the container contents arrive before the first click
    # Imbuing Station GUI (854x480): top (426,65) left (335,156) bottom (426,246) center (426,156) output (516,156); hotbar y=415 x=282+36*n
    hot = lambda n: (282 + 36 * n, 415)
    def station_items(label):
        """Close the GUI (commands cannot be typed inside a container screen), read the station's NBT, reopen the GUI."""
        g.key('escape'); g.wait(0.5)
        out = g.cmd('data get block 0 5 3')
        g.note('%s station NBT: %s' % (label, ' | '.join(out)[:260]))
        g.click(427, 240, 'right'); g.wait(0.8)
        return out

    # --- #169 recipe: cobblestone center + water bottle + vine + bone meal
    g.click(*hot(0)); g.click(426, 156)
    g.click(*hot(6)); g.click(426, 65)
    g.click(*hot(1)); g.click(335, 156)
    g.click(*hot(2)); g.click(426, 246)
    g.key('escape'); game_wait(g, 13)               # 200 ticks, GUI closed
    g.click(427, 240, 'right'); g.wait(0.8)
    g.shot('after_169')
    out = station_items('169')
    g.check(any('mossy_cobblestone' in l for l in out), '#169: Mossy Cobblestone is in the station output after ~200 ticks', ' | '.join(out)[:200])
    g.click(516, 156); g.click(*hot(0))           # take the result into slot 0
    # --- #170 Fire Imbue: water center + coal + flint + blaze powder
    g.click(*hot(7)); g.click(426, 156)
    g.click(*hot(3)); g.click(426, 65)
    g.click(*hot(4)); g.click(335, 156)
    g.click(*hot(5)); g.click(426, 246)
    g.key('escape'); game_wait(g, 13)
    g.click(427, 240, 'right'); g.wait(0.8)
    out = station_items('170')
    g.check(any('imbue_fire' in l for l in out), '#170: Fire Imbue potion is in the station output', ' | '.join(out)[:200])
    g.click(516, 156); g.click(*hot(1))
    g.key('escape'); g.wait(0.4)
    # --- #175 drink it: aim at the sky (nothing to right-click) and hold the use button
    g.tp(0.5, 4, 0.5, 0, -80)
    g.select(1)
    g.hold(427, 240, 'right', 2.6)
    g.wait(0.5)
    out = g.cmd('data get entity @s ActiveEffects')
    g.expect_chat(out, r'Duration: 2\d{4}', '#175: drinking Fire Imbue gives a ~20 minute effect')
    g.expect_chat(out, r'ShowIcon: 1b', '#175: the effect shows its HUD/inventory icon')
    g.expect_chat(out, r'ShowParticles: 0b', '#175: the effect shows no particles (like 1.12.2)')
    g.shot('after_drink')

def hits(g, n=6, gap=0.75):
    for _ in range(n):
        g.click(427, 240, 'left')
        g.wait(gap)


def spawn_dummy(g, mob='spider'):
    """A motionless, silent mob two blocks in front of the camera. Default spider: it does not burn in daylight (a zombie does) and drops a
    fixed 5 XP. Spiders are immune to poison, so poison tests use a pig."""
    g.cmd('kill @e[type=!player]')
    g.cmd('summon minecraft:%s 0.5 4 3.5 {NoAI:1b,Silent:1b,PersistenceRequired:1b}' % mob)
    g.tp(0.5, 4, 0.5, 0, 20)
    g.wait(0.5)                                                      # the client needs the spawn packet before a click can hit the mob


def xp_points(g, settle=7):
    """Total XP the player has collected. `experience query points` only reports the points inside the current level, so add the XP of the
    levels below (valid up to level 16: L*L + 6L) - 10 XP is level 1 + 3 points."""
    g.wait(settle)                             # orbs need a moment to reach the player
    out = g.cmd('experience query @s levels')
    out2 = g.cmd('experience query @s points')
    ml = re.search(r'has (\d+) experience levels', ' '.join(out))
    mp = re.search(r'has (\d+) experience points', ' '.join(out2))
    if not ml or not mp:
        return -1, out + out2
    level, points = int(ml.group(1)), int(mp.group(1))
    return level * level + 6 * level + points, ['level %d + %d points' % (level, points)]


@scenario
def imbue_effects(g):
    """#176-179: Fire/Poison/Wither imbue on a hit, Experience imbue on a kill (XP doubles)."""
    arena(g)
    g.slot(0, 'minecraft:diamond_sword')
    g.select(0)
    # --- baseline: a spider killed by the player gives 5 XP
    spawn_dummy(g)
    hits(g, 6)
    pts, out = xp_points(g)
    g.check(pts == 5, 'baseline: a spider killed by the player gives 5 XP', ' | '.join(out)[:120])
    # --- Fire imbue
    g.cmd('effect give @s randomthings:imbue_fire 1000 0 true')
    spawn_dummy(g, 'pig' if MC215 else 'spider')      # 1.21.5+: spiders do not catch the effect/fire of the imbues in this setup, a pig does
    hits(g, 1)
    out = g.cmd('data get entity @e[type=minecraft:%s,limit=1] Fire' % ('pig' if MC215 else 'spider'))
    g.check(any(re.search(r'(\d+)s?$', l) and int(re.findall(r'\d+', l)[-1]) > 0 for l in out), '#176: Fire Imbue: a direct hit sets the target on fire', ' | '.join(out)[:160])
    # --- Poison imbue: Poison II (effect id 19, amplifier 1)
    g.cmd('effect clear @s')
    g.cmd('effect give @s randomthings:imbue_poison 1000 0 true')
    spawn_dummy(g, 'pig')
    hits(g, 1)
    out = g.cmd('data get entity @e[type=minecraft:pig,limit=1] ActiveEffects')
    g.expect_chat(out, r'Id: 19b', '#177: Poison Imbue: a direct hit poisons the target')
    g.expect_chat(out, r'Amplifier: 1b', '#177: ... at amplifier 1 (level II)')
    # --- Wither imbue: Wither II (effect id 20)
    g.cmd('effect clear @s')
    g.cmd('effect give @s randomthings:imbue_wither 1000 0 true')
    spawn_dummy(g)
    hits(g, 1)
    out = g.cmd('data get entity @e[type=minecraft:spider,limit=1] ActiveEffects')
    g.expect_chat(out, r'Id: 20b', '#178: Wither Imbue: a direct hit withers the target')
    g.expect_chat(out, r'Amplifier: 1b', '#178: ... at amplifier 1 (level II)')
    # --- Experience imbue: double XP on a kill
    g.cmd('effect clear @s')
    g.cmd('kill @e[type=!player]')           # a mob the player hit less than 5 s ago drops XP when /kill removes it: do that before the XP effect and the reset
    g.wait(1.0)
    g.cmd('kill @e[type=minecraft:experience_orb]')
    g.cmd('effect give @s randomthings:imbue_experience 1000 0 true')
    g.cmd('experience set @s 0 levels')
    g.cmd('experience set @s 0 points')
    spawn_dummy(g)
    hits(g, 6)
    pts, out = xp_points(g)
    g.check(pts == 10, '#179: Experience Imbue doubles the XP of a kill (5 -> 10)', ' | '.join(out)[:120])
    g.shot('end')


def pig_fire(g, label):
    """A burning pig with 1000 max health (so it survives, and real fire damage shows as lost health). Returns (fire ticks left, health)
    after the rain has had time to act: rain puts the fire out and the pig keeps full health, a shielded pig keeps burning and loses health."""
    g.cmd('kill @e[type=minecraft:pig]')
    g.cmd('summon minecraft:pig 3.5 4 3.5 {NoAI:1b,Silent:1b,Fire:600s,Attributes:[{Name:"generic.%s",Base:1000d}],Health:1000f}' % ('max_health' if MC16 else 'maxHealth'))
    g.wait(6)
    out = g.cmd('data get entity @e[type=minecraft:pig,limit=1] Fire')
    m = re.search(r'(-?\d+)s?$', ' '.join(out).strip())
    ticks = int(m.group(1)) if m else -999
    out2 = g.cmd('data get entity @e[type=minecraft:pig,limit=1] Health')
    m2 = re.search(r'(-?[\d.]+)f?$', ' '.join(out2).strip())
    health = float(m2.group(1)) if m2 else -1.0
    g.note('%s: pig Fire = %s ticks, Health = %s' % (label, ticks, health))
    return ticks, health


@scenario
def rain_shield(g):
    """#258 / #259: rain puts out a burning pig (control); an unpowered Rain Shield stops that so the pig keeps burning and takes damage;
    powered it stops shielding; unpowered again it shields again."""
    arena(g)
    g.cmd('weather rain')
    game_wait(g, 8)                            # let the rain strength ramp up
    fire, health = pig_fire(g, 'control, no shield')
    g.check(fire <= 0 and health >= 999, 'control: rain extinguishes a burning pig without a shield and it takes (almost) no damage', 'fire=%s health=%s' % (fire, health))
    g.cmd('setblock 0 4 3 randomthings:rain_shield')
    g.wait(1)
    fire, health = pig_fire(g, 'unpowered shield')
    g.check(fire > 0 and health < 999, '#258: with an unpowered Rain Shield the pig keeps burning and takes fire damage', 'fire=%s health=%s' % (fire, health))
    g.cmd('setblock 1 4 3 minecraft:redstone_block')      # power it from the side
    g.wait(1)
    fire, health = pig_fire(g, 'powered shield')
    g.check(fire <= 0 and health >= 999, '#259: a powered Rain Shield stops shielding, rain puts the fire out and the pig takes (almost) no damage', 'fire=%s health=%s' % (fire, health))
    g.cmd('setblock 1 4 3 minecraft:air')
    g.wait(1)
    out = g.cmd('data get block 0 4 3')
    g.note('shield after unpowering: %s' % ' | '.join(out)[:200])
    out = g.cmd('execute if block 0 4 3 randomthings:rain_shield')
    g.check(any('Test passed' in l for l in out), 'the shield block still exists after the redstone block is removed', ' | '.join(out))
    fire, health = pig_fire(g, 'unpowered again')
    g.check(fire > 0 and health < 999, '#259: unpowered again, the shield works again (pig burns and takes damage)', 'fire=%s health=%s' % (fire, health))
    g.cmd('weather clear')


@scenario
def ender_anchor(g):
    """#356 / #357: an Ender Anchor force-loads its chunk and releases it when broken."""
    arena(g)
    g.cmd('forceload remove all')
    g.tp(5000.5, 4, 5000.5, 0, 0)                            # load a far chunk nothing else keeps loaded, place the anchor, come back
    g.wait(3)
    g.cmd('setblock 5000 5 5000 randomthings:ender_anchor')
    g.tp(0.5, 4, 0.5, 0, 0)
    g.wait(4)
    out = g.cmd('forceload query 5000 5000')
    g.expect_chat(out, r'marked for force loading', '#356: the anchor force-loads its chunk')
    g.tp(5000.5, 4, 5000.5, 0, 0)
    g.wait(3)
    g.cmd('setblock 5000 5 5000 minecraft:air')
    g.tp(0.5, 4, 0.5, 0, 0)
    g.wait(4)
    out = g.cmd('forceload query 5000 5000')
    g.expect_chat(out, r'not marked|isn.t marked', '#357: breaking the anchor releases the chunk')




# =============================================================================================
# Playtest-route scenarios (A1-A10, B). Helpers first.
# =============================================================================================
import math


def aim_at(g, bx, by, bz, frm=(0.5, 4.0, 0.5)):
    """Stand at `frm` (feet) and look at the centre of block (bx,by,bz): exact yaw/pitch, so a click hits that block."""
    ex, ey, ez = frm[0], frm[1] + 1.62, frm[2]
    dx, dy, dz = bx + 0.5 - ex, by + 0.5 - ey, bz + 0.5 - ez
    yaw = -math.degrees(math.atan2(dx, dz))
    pitch = -math.degrees(math.atan2(dy, math.hypot(dx, dz)))
    g.tp(frm[0], frm[1], frm[2], round(yaw, 2), round(pitch, 2))


def gui_slot(w, h, sx, sy):
    """Screen pixel of the centre of a container slot. Client area 854x480 = scale 2 (427x240 scaled); slot (sx,sy) is relative to the GUI's
    top-left, GUI size w x h. Matches the Imbuing Station coordinates measured earlier (hotbar x = 282 + 36n)."""
    left, top = (427 - w) // 2, (240 - h) // 2
    return 2 * (left + sx + 8), 2 * (top + sy + 8)


def hotbar_xy(w, h, n):
    return gui_slot(w, h, 8 + 18 * n, h - 24)      # vanilla layouts keep the hotbar 24 px above the bottom edge


def inv(g):
    return ' '.join(g.cmd('data get entity @s Inventory', 0.7))


def player_name(g):
    out = g.cmd('say whoami', 0.5)
    m = re.search(r'\[(.+?)\] whoami', ' '.join(out))
    return m.group(1) if m else 'Dev'


def block_is(g, x, y, z, block):
    out = g.cmd('execute if block %d %d %d %s' % (x, y, z, block), 0.4)
    return any('Test passed' in l for l in out)


def dv(g, target, path=''):
    """The value part of `data get <target> <path>` ("... has the following block data: <value>")."""
    t = ' '.join(g.cmd('data get %s %s' % (target, path), 0.5))
    return t.split('data: ', 1)[1].strip() if 'data: ' in t else t


def block_isnt(g, x, y, z, block):
    """True only when the game answered 'Test passed' to `execute unless block` - a command that never ran (wrong screen) is not a pass."""
    out = g.cmd('execute unless block %d %d %d %s' % (x, y, z, block), 0.4)
    return any('Test passed' in l for l in out)


def gui_open(g, w, h, what):
    """After a right-click: verify a grey container panel is on screen (and not the world or the pause menu); record a FAIL if not.
    Pressing Escape with no GUI open would open the pause menu, where the following clicks hit 'Save and Quit', so callers return on False."""
    left, top = (427 - w) // 2, (240 - h) // 2
    probes = ((w // 2, h - 4), (w - 10, 4), (6, h // 2))        # a few spots that are plain grey panel in the container textures used here
    px = None
    for _ in range(6):
        for sx, sy in probes:
            px = desk.pixel('Minecraft', 2 * (left + sx), 2 * (top + sy))
            if 120 <= px[0] <= 235 and abs(px[0] - px[1]) < 16 and abs(px[1] - px[2]) < 16:
                g.do('move', '300', '300')                    # the game applies a new mouse position with the next frame: park the cursor in the GUI first, or the first click lands where it was
                desk.sleep(0.4)
                desk.sleep(1.0)                               # the container contents reach the client a moment after the screen opens; an immediate click can be lost
                return True
        desk.sleep(0.4)
    g.check(False, '%s: the GUI did not open (right-click did not reach the block?)' % what, 'pixel %s' % (px,))
    g.shot('gui_missing')
    raise desk.Abort('unknown screen state after a failed GUI open: stopping the run instead of typing into it')


def slot_color(g, n):
    """Average RGB of the item drawn in hotbar slot n (0-8) of the HUD (854x480 client, GUI scale 2): a 7x7 grid inside the 32x32 item box."""
    x0, y0 = 250 + 40 * n, 442
    tot, cnt = [0, 0, 0], 0
    fr = desk.grab('Minecraft')
    for i in range(7):
        for j in range(7):
            r, gg, b = desk.frame_px(fr, x0 + 3 + i * 4, y0 + 3 + j * 4)
            tot[0] += r; tot[1] += gg; tot[2] += b; cnt += 1
    return tuple(int(t / cnt) for t in tot)


def blue_pixels(g, n):
    """Number of clearly blue pixels (blue well above red) in the item box of hotbar slot n - the item sprite only covers part of the box."""
    x0, y0 = 250 + 40 * n, 442
    cnt = 0
    fr = desk.grab('Minecraft')
    for i in range(16):
        for j in range(16):
            r, gg, b = desk.frame_px(fr, x0 + 1 + i * 2, y0 + 1 + j * 2)
            if b - r > 35:
                cnt += 1
    return cnt


def entity_count(g, selector):
    out = g.cmd('execute if entity %s' % selector, 0.4)
    m = re.search(r'count: (\d+)', ' '.join(out))
    if m:
        return int(m.group(1))
    return 1 if any('Test passed' in l for l in out) else 0


def sky(g):
    g.tp(0.5, 4, 0.5, 0, -80)         # nothing to right-click


def pick_place(g, src, dst):
    g.click(*src); g.click(*dst)


# ---- A1 -----------------------------------------------------------------------------------
@scenario
def sound_dampener(g):
    """#158 / #159 / #160: a placed Sound Dampener mutes its patterns within 20 blocks; the Portable one mutes them anywhere; removing the pattern
    un-mutes. The client logs '[RT-DEBUG] muted sound ...' for every sound it silences, which is what is asserted here."""
    arena(g)
    harp = 'minecraft:block.note_block.harp'
    pattern = '{Slot:0b,id:"randomthings:sound_pattern",Count:1b,tag:{sound:"%s"}}' % harp

    def muted(x, y, z, who):
        before = g.mark()
        g.cmd('playsound %s master @s %s %s %s 100 1 1' % (harp, x, y, z), 0.2)
        g.wait(1.2)
        return ('muted sound %s (%s' % (harp, who)) in g.new_log(before)

    g.cmd('setblock 0 4 3 randomthings:sound_dampener')
    g.check(not muted(3, 4, 3, 'placed'), '#158: an empty Sound Dampener mutes nothing (control)')
    g.cmd('data merge block 0 4 3 {inventory:{Size:9,Items:[%s]}}' % pattern)
    g.wait(0.5)
    g.check(until(g, lambda: muted(3, 4, 3, 'placed'), 10.0, 0.1), '#158: the pattern\'s sound is muted within 20 blocks of the Sound Dampener')      # the client learns the new inventory from a block entity update: poll
    g.check(not muted(60, 4, 3, 'placed'), '#158: the same sound more than 20 blocks away is audible again')
    g.cmd('data modify block 0 4 3 inventory.Items set value []')
    g.wait(0.5)
    g.check(not muted(3, 4, 3, 'placed'), '#160: with the pattern taken out the sound is audible again')
    g.cmd('data merge block 0 4 3 {inventory:{Size:9,Items:[%s]}}' % pattern)
    g.wait(0.5)
    g.cmd('setblock 0 4 3 minecraft:air')
    g.wait(0.5)
    g.check(not muted(3, 4, 3, 'placed'), '#160: breaking the Sound Dampener stops the muting')
    g.slot(0, 'randomthings:portable_sound_dampener{inventory:{Size:9,Items:[%s]}}' % pattern)
    g.wait(0.8)
    g.check(until(g, lambda: muted(60, 4, 3, 'portable'), 10.0, 0.1), '#159: the Portable Sound Dampener in the inventory mutes the sound wherever it is')
    g.cmd('clear @s randomthings:portable_sound_dampener')
    g.wait(0.5)
    g.check(not muted(60, 4, 3, 'portable'), '#159: without the Portable Sound Dampener the sound is audible again')


# ---- A3 -----------------------------------------------------------------------------------
@scenario
def fluid_display(g):
    """#234: a Fluid Display takes the fluid of the bucket it is right-clicked with; empty hand toggles still/flowing, sneak rotates."""
    arena(g)
    g.cmd('setblock 0 4 3 randomthings:fluid_display')
    g.select(0)
    for bucket, fluid in (('water_bucket', 'minecraft:water'), ('lava_bucket', 'minecraft:lava')):
        g.slot(0, 'minecraft:' + bucket)
        aim_at(g, 0, 4, 3)
        g.click(427, 240, 'right'); g.wait(0.5)
        out = g.cmd('data get block 0 4 3 fluidStack')
        g.expect_chat(out, fluid, '#234: right-clicking with a %s sets the display to %s' % (bucket, fluid))
    g.shot('lava_display')
    g.cmd('clear @s')
    before = dv(g, 'block 0 4 3', 'flowing')
    aim_at(g, 0, 4, 3)
    g.click(427, 240, 'right'); g.wait(0.5)
    after = dv(g, 'block 0 4 3', 'flowing')
    g.check(before != after, '#234: an empty-handed right-click toggles still/flowing', '%s -> %s' % (before, after))
    r0 = dv(g, 'block 0 4 3', 'rotation')
    g.click(427, 240, 'right', '--shift'); g.wait(0.5)
    r1 = dv(g, 'block 0 4 3', 'rotation')
    g.check(r0 != r1, '#234: a sneak-right-click rotates the display', '%s -> %s' % (r0, r1))


def open_crafting_table(g):
    g.cmd('setblock 0 4 3 minecraft:crafting_table')
    aim_at(g, 0, 4, 3)
    g.click(427, 240, 'right'); g.wait(0.9)
    return gui_open(g, 176, 166, 'crafting table')


CRAFT_W, CRAFT_H = 176, 166


def craft_grid(g, hot_slots):
    """With the crafting table open: move hotbar slots (list of (hotbar index, grid index)) into the 3x3 grid, take the result into hotbar 8."""
    for hb, cell in hot_slots:
        pick_place(g, hotbar_xy(CRAFT_W, CRAFT_H, hb), gui_slot(CRAFT_W, CRAFT_H, 30 + 18 * (cell % 3), 17 + 18 * (cell // 3)))
    g.wait(0.3)
    g.shot('crafting')
    pick_place(g, gui_slot(CRAFT_W, CRAFT_H, 124, 35), hotbar_xy(CRAFT_W, CRAFT_H, 8))
    g.wait(0.3)
    g.key('escape'); g.wait(0.5)


@scenario
def crafting_evil_tear_portkey(g):
    """#238 Evil Tear (skull + ghast tear + ender pearl, any arrangement) and #279 Portkey disguise (Portkey + any item in a grid, donor returned)."""
    arena(g)
    g.slot(0, 'minecraft:wither_skeleton_skull')
    g.slot(1, 'minecraft:ghast_tear')
    g.slot(2, 'minecraft:ender_pearl')
    if not open_crafting_table(g):
        return
    craft_grid(g, [(0, 4), (1, 0), (2, 8)])           # deliberately scattered: shapeless
    out = inv(g)
    g.check('randomthings:evil_tear' in out, '#238: skull + ghast tear + ender pearl craft an Evil Tear', out[:200])
    g.check('wither_skeleton_skull' not in out and 'ghast_tear' not in out, '#238: the ingredients were consumed')
    g.cmd('clear @s')
    g.slot(0, 'randomthings:portkey')
    g.slot(1, 'minecraft:diamond_pickaxe')
    if not open_crafting_table(g):
        return
    craft_grid(g, [(0, 0), (1, 1)])
    out = inv(g)
    g.check('randomthings:portkey' in out and 'camo' in out, '#279: Portkey + diamond pickaxe gives a Portkey with a camo disguise', out[:260])
    g.check('minecraft:diamond_pickaxe' in out, '#279: the donor item is returned')


@scenario
def portkey_drop(g):
    """#277: a bound Portkey on the ground counts 100 ticks (glint stops, no despawn); picking it up resets the counter."""
    arena(g)
    g.cmd('summon item 0.5 4 3.5 {Item:{id:"randomthings:portkey",Count:1b,tag:{hasTarget:1b,dimension:' + DIM + ',targetX:10,targetY:4,targetZ:10}},Motion:[0.0,0.0,0.0]}')
    g.wait(1)
    v1 = dv(g, 'entity @e[type=item,nbt={Item:{id:"randomthings:portkey"}},limit=1]', 'Item.tag.dropCounter')
    c1 = int(v1) if re.fullmatch(r'-?\d+', v1) else -1
    g.wait(5)
    v2 = dv(g, 'entity @e[type=item,nbt={Item:{id:"randomthings:portkey"}},limit=1]', 'Item.tag.dropCounter')
    c2 = int(v2) if re.fullmatch(r'-?\d+', v2) else -1
    g.check(0 < c1 < 100 and c2 >= 100, '#277: the dropped Portkey\'s counter runs up to the 100-tick priming time', 'after 1 s=%s after 6 s=%s' % (c1, c2))
    age = dv(g, 'entity @e[type=item,nbt={Item:{id:"randomthings:portkey"}},limit=1]', 'Age')
    g.check(re.fullmatch(r'-\d+s', age) is not None, '#277: ... and it no longer despawns (age pushed back to the negative no-despawn value)', age)
    g.cmd('tp @s 0.5 4 3.5')
    g.wait(2)
    pos = player_pos(g)
    g.check(pos is not None and abs(pos[0] - 10.5) < 3 and abs(pos[2] - 10.5) < 3, '#277: a primed Portkey teleports whoever picks it up to its bound position (10, 4, 10)', str(pos))


# ---- A4 -----------------------------------------------------------------------------------
@scenario
def redstone_remote(g):
    """#287: a Redstone Remote with a bound Position Filter fires a 20-tick strength-15 pulse at that spot when its button is pressed.
    The command block IS the target block (the pulse makes the block at the bound position itself 'powered') and records the pulse by placing an emerald block."""
    arena(g)
    g.cmd('setblock 10 4 10 minecraft:air')
    g.cmd('setblock 6 4 3 minecraft:command_block{Command:"setblock 10 4 10 minecraft:emerald_block"}')
    g.check(block_isnt(g, 10, 4, 10, 'minecraft:emerald_block'), 'control: the marker block is not there before the pulse')
    pos = '{Slot:0b,id:"randomthings:position_filter",Count:1b,tag:{hasPosition:1b,dimension:' + DIM + ',filterX:6,filterY:4,filterZ:3}}'
    g.slot(0, 'randomthings:redstone_remote{positions:{Size:9,Items:[%s]}}' % pos)
    g.select(0)
    sky(g)
    g.click(427, 240, 'right'); g.wait(1.0)
    if not gui_open(g, 187, 41, 'Redstone Remote'):
        return
    g.shot('use_gui')
    # Use screen: 187x41, first button at (5,17) 18x18 -> screen centre
    x, y = gui_slot(187, 41, 5, 17)
    g.click(x, y); g.wait(0.6)
    g.key('escape'); g.wait(0.6)
    g.check(block_is(g, 10, 4, 10, 'minecraft:emerald_block'), '#287: pressing the remote button powered the bound spot (the command block fired)')


@scenario
def redstone_interface(g):
    """#284: a Basic Redstone Interface mirrors its input onto its bound target position; breaking it stops that."""
    arena(g)
    g.cmd('setblock 0 4 3 randomthings:basic_redstone_interface')
    g.cmd('setblock 6 4 3 minecraft:redstone_lamp')
    g.cmd('data merge block 0 4 3 {targetX:6,targetY:4,targetZ:3}')
    g.check(block_is(g, 6, 4, 3, 'minecraft:redstone_lamp[lit=false]'), 'control: the lamp at the target is off')
    g.cmd('setblock -1 4 3 minecraft:redstone_block')
    g.wait(1.0)
    g.check(block_is(g, 6, 4, 3, 'minecraft:redstone_lamp[lit=true]'), '#284: powering the interface powers its target (the lamp at the target lights)')
    g.cmd('setblock -1 4 3 minecraft:air')
    g.wait(1.0)
    g.check(block_is(g, 6, 4, 3, 'minecraft:redstone_lamp[lit=false]'), '#284: removing the input switches the target off again')
    g.cmd('setblock -1 4 3 minecraft:redstone_block')
    g.wait(0.6)
    g.cmd('setblock 0 4 3 minecraft:air')
    g.wait(1.0)
    g.check(block_is(g, 6, 4, 3, 'minecraft:redstone_lamp[lit=false]'), '#284: breaking the interface stops the mirroring')


# ---- A5 -----------------------------------------------------------------------------------
def pulse(g, pos='2 8 5'):
    g.cmd('setblock %s minecraft:air' % pos)
    g.wait(0.3)
    g.cmd('setblock %s minecraft:redstone_block' % pos)


@scenario
def block_destabilizer(g):
    """#293 / #294: the Block Destabilizer flood-fills identical full cubes (exact state, or just the block type when Fuzzy is on, toggled in the GUI)
    and drops them as falling blocks. Walls are built above the ground so the dropped blocks visibly leave their place."""
    arena(g)
    # --- #293: 5x5 stone wall at z=6, y=5..9; one andesite block in it must stay put
    g.cmd('fill 0 5 6 4 9 6 minecraft:stone')
    g.cmd('setblock 3 7 6 minecraft:andesite')
    g.cmd('setblock 2 7 5 randomthings:block_destabilizer[facing=south]')
    pulse(g, '2 8 5')
    until(g, lambda: block_isnt(g, 0, 9, 6, 'minecraft:stone') and block_isnt(g, 4, 9, 6, 'minecraft:stone'), 30.0, 0.5)
    g.check(block_isnt(g, 0, 9, 6, 'minecraft:stone') and block_isnt(g, 4, 9, 6, 'minecraft:stone'), '#293: the stone of the wall dropped (top corners are gone)')
    g.check(block_is(g, 3, 7, 6, 'minecraft:andesite'), '#293: a block of a different state in the wall stayed where it was')
    g.check(dv(g, 'block 2 7 5', 'state') == '0', '#293: the Block Destabilizer is idle again afterwards')
    g.cmd('kill @e[type=minecraft:falling_block]')
    g.cmd('fill 0 4 6 4 9 6 minecraft:air')
    # --- #294: logs [x,y,y,y,x] in a row at y=7: exact mode takes the three y logs, fuzzy mode (toggled in the GUI) takes all five
    g.cmd('setblock 0 7 6 minecraft:oak_log[axis=x]')
    g.cmd('fill 1 7 6 3 7 6 minecraft:oak_log[axis=y]')
    g.cmd('setblock 4 7 6 minecraft:oak_log[axis=x]')
    g.cmd('setblock 2 8 5 minecraft:air')
    pulse(g, '2 8 5')
    until(g, lambda: block_isnt(g, 2, 7, 6, 'minecraft:oak_log'), 30.0, 0.5)
    g.wait(0.5)
    g.check(block_isnt(g, 2, 7, 6, 'minecraft:oak_log') and block_is(g, 0, 7, 6, 'minecraft:oak_log[axis=x]'),
            '#294: exact mode: the three matching logs dropped, the logs with another rotation stayed')
    g.cmd('kill @e[type=minecraft:falling_block]')
    g.cmd('fill 1 4 6 3 4 6 minecraft:air')
    g.cmd('setblock 2 8 5 minecraft:air')
    g.cmd('fill 1 7 6 3 7 6 minecraft:oak_log[axis=y]')
    aim_at(g, 2, 7, 5, frm=(2.5, 4.0, 2.5))      # within the 5-block reach
    g.click(427, 240, 'right'); g.wait(0.9)
    if not gui_open(g, 85, 35, 'Block Destabilizer'):
        return
    g.shot('destabilizer_gui')
    g.click(*gui_slot(85, 35, 33 + 10 - 8, 7 + 10 - 8))     # Fuzzy button centre (guiLeft+33+10, guiTop+7+10)
    g.wait(0.4)
    g.key('escape'); g.wait(0.5)
    g.check(dv(g, 'block 2 7 5', 'fuzzy') == '1b', '#294: the GUI Fuzzy button turned fuzzy mode on')
    pulse(g, '2 8 5')
    until(g, lambda: block_isnt(g, 0, 7, 6, 'minecraft:oak_log') and block_isnt(g, 4, 7, 6, 'minecraft:oak_log'), 30.0, 0.5)
    g.check(block_isnt(g, 0, 7, 6, 'minecraft:oak_log') and block_isnt(g, 4, 7, 6, 'minecraft:oak_log'),
            '#294: fuzzy mode: the logs of every rotation dropped')


# ---- A6 -----------------------------------------------------------------------------------
DYE_W, DYE_H = 176, 141


@scenario
def dyeing_machine(g):
    """#321 / #322 / #324: the Dyeing Machine tags an item with a dye colour (rtDye), or an enchant-glint colour (enchantmentColor), and drops
    unconsumed inputs at your feet when closed."""
    arena(g)
    g.slot(0, 'minecraft:iron_chestplate')
    g.slot(1, 'minecraft:red_dye')
    g.slot(2, 'minecraft:diamond')
    g.slot(4, 'minecraft:iron_helmet')
    g.slot(5, 'minecraft:blue_dye')
    g.cmd('setblock 0 4 3 randomthings:dyeing_machine')
    aim_at(g, 0, 4, 3)
    g.click(427, 240, 'right'); g.wait(0.9)
    if not gui_open(g, DYE_W, DYE_H, 'Dyeing Machine'):
        return
    g.shot('dyeing_gui')
    item_slot, dye_slot = gui_slot(DYE_W, DYE_H, 27, 22), gui_slot(DYE_W, DYE_H, 76, 22)
    res_dye, res_glint = gui_slot(DYE_W, DYE_H, 133, 22), gui_slot(DYE_W, DYE_H, 154, 22)
    pick_place(g, hotbar_xy(DYE_W, DYE_H, 0), item_slot)
    pick_place(g, hotbar_xy(DYE_W, DYE_H, 1), dye_slot)
    g.wait(0.4)
    g.shot('dyeing_preview')
    pick_place(g, res_dye, hotbar_xy(DYE_W, DYE_H, 3))
    g.wait(0.3)
    g.key('escape'); g.wait(0.6)
    out = inv(g)
    g.check('rtDye' in out and 'iron_chestplate' in out, '#321/#323: the recolor result is the iron chestplate tagged with rtDye (red)', out[:240])
    g.check(not re.search(r'id: "minecraft:red_dye"', out), '#321: one dye was consumed')
    # recolor an iron HELMET blue (first result slot), and read the colour back from the rendered hotbar slot
    g.slot(8, 'minecraft:iron_helmet')                                 # undyed reference in the hotbar
    aim_at(g, 0, 4, 3)
    g.click(427, 240, 'right'); g.wait(0.9)
    if not gui_open(g, DYE_W, DYE_H, 'Dyeing Machine (2nd)'):
        return
    pick_place(g, hotbar_xy(DYE_W, DYE_H, 4), item_slot)
    pick_place(g, hotbar_xy(DYE_W, DYE_H, 5), dye_slot)
    g.wait(0.4)
    pick_place(g, res_dye, hotbar_xy(DYE_W, DYE_H, 4))
    g.wait(0.3)
    g.key('escape'); g.wait(0.8)
    out = inv(g)
    g.check('rtDye: 3949738' in out, '#321/#323: the recolored iron helmet carries rtDye = blue (3949738)', out[:300])
    plain, dyed = blue_pixels(g, 8), blue_pixels(g, 4)
    g.shot('hotbar_dyed_helmet')
    g.check(dyed >= 8 and plain == 0, '#321: the dyed helmet is drawn blue in the hotbar, the undyed one is not', 'blue pixels: undyed %d, dyed %d' % (plain, dyed))
    # enchant-glint result: an ENCHANTED helmet + blue dye, second result slot (an unenchanted item shows no glint, so nothing would be visible)
    g.slot(6, 'minecraft:iron_helmet{Enchantments:[{id:"minecraft:protection",lvl:1}]}')
    g.slot(7, 'minecraft:blue_dye')
    aim_at(g, 0, 4, 3)
    g.click(427, 240, 'right'); g.wait(0.9)
    if not gui_open(g, DYE_W, DYE_H, 'Dyeing Machine (glint)'):
        return
    pick_place(g, hotbar_xy(DYE_W, DYE_H, 6), item_slot)
    pick_place(g, hotbar_xy(DYE_W, DYE_H, 7), dye_slot)
    g.wait(0.4)
    pick_place(g, res_glint, hotbar_xy(DYE_W, DYE_H, 6))
    g.wait(0.3)
    g.key('escape'); g.wait(0.8)
    out = inv(g)
    g.check('enchantmentColor: 3949738' in out and 'Enchantments' in out, '#322: the enchanted helmet carries enchantmentColor = blue and keeps its enchantment', out[:400])
    g.slot(7, 'minecraft:iron_helmet{Enchantments:[{id:"minecraft:protection",lvl:1}]}')       # reference: the same helmet with the vanilla (purple) glint
    g.wait(0.3)

    def sheen_ratio(n):
        """The glint is ADDED on top of the item: subtract the plain (unenchanted) helmet in slot 8 pixel by pixel and return sum(red)/sum(blue) of what
        the glint added over ~2 s (blue-dye glint ~0.35, vanilla purple ~0.6)."""
        def grab(slot):
            x0, y0 = 250 + 40 * slot, 442
            fr = desk.grab('Minecraft')           # one frame grab instead of 256 single-pixel reads (those cost ~100 s per scenario)
            return [desk.frame_px(fr, x0 + 1 + i * 2, y0 + 1 + j * 2) for i in range(16) for j in range(16)]
        base = grab(8)
        dr = db = 0
        for _ in range(10):
            for (r, gg, b), (r0, g0, b0) in zip(grab(n), base):
                if b - b0 > 6 and r - r0 >= 0:
                    dr += r - r0
                    db += b - b0
            g.wait(0.2)
        return dr / float(db) if db else -1
    tinted, vanilla = sheen_ratio(6), sheen_ratio(7)
    g.shot('hotbar_glint_helmet')
    g.check(0 < tinted < vanilla - (0.02 if MC2111 else 0.1), '#322: the blue-dyed glint is drawn bluer than the vanilla purple glint (hotbar)', 'added red/blue: blue glint %.2f vs vanilla %.2f' % (tinted, vanilla))
    # #324: close with an item still in a slot -> it drops at the feet
    aim_at(g, 0, 4, 3)
    g.click(427, 240, 'right'); g.wait(0.9)
    if not gui_open(g, DYE_W, DYE_H, 'Dyeing Machine (3rd)'):
        return
    pick_place(g, hotbar_xy(DYE_W, DYE_H, 2), item_slot)
    g.key('escape'); g.wait(0.8)
    g.check(entity_count(g, '@e[type=item,nbt={Item:{id:"minecraft:diamond"}}]') >= 1 or 'minecraft:diamond' in inv(g),
            '#324: closing with the diamond still in the slot returns it (item at your feet or in the inventory), nothing deleted')


@scenario
def worn_armor(g):
    """Dyed armor and tinted enchant glint on a player seen in third person (F5 front view): a dyed chestplate/helmet shows the dye colour on the body
    (counted in the pixels around the player), and a blue 'enchantmentColor' glint is bluer than the vanilla purple glint on the same armor."""
    arena(g)
    g.cmd('gamerule doDaylightCycle false')
    g.cmd('time set 6000')
    g.tp(0.5, 4, 0.5, 0, 0)

    def wear(chest, helmet='minecraft:iron_helmet'):
        g.cmd(item_cmd('armor.chest', chest), 0.4)
        g.cmd(item_cmd('armor.head', helmet), 0.4)
        g.wait(2.0)                                           # the equipment packet and the first frames with the new armor (a fast command channel returns before the client has drawn it)

    def scan(pred, frames=1):
        """Number of pixels around the player (client area 380..472 x 212..392) matching pred(r,g,b), summed over `frames` frames."""
        n = 0
        g.key('f1'); g.wait(0.6)                              # hide the HUD and chat so nothing covers the player
        for _ in range(frames):
            fr = desk.grab('Minecraft')
            for x in range(380, 472, 2):
                for y in range(212, 392, 2):
                    if pred(*desk.frame_px(fr, x, y)):
                        n += 1
            if frames > 1:
                g.wait(0.2)
        g.key('f1'); g.wait(0.4)
        return n

    g.key('f5'); g.key('f5'); g.wait(1.0)                    # front view: the player faces the camera
    g.shot('front_view_empty')
    wear('minecraft:iron_chestplate', 'minecraft:iron_helmet')
    red = lambda r, gg, b: r > 110 and r - gg > 55 and r - b > 55
    blue = lambda r, gg, b: r < 90 and 80 < b < 195 and b - r > 40 and b - gg > 15      # dark dye blue, not the sky
    plain_red, plain_blue = scan(red), scan(blue)
    g.shot('worn_plain')
    wear('minecraft:iron_chestplate{rtDye:11546150}', 'minecraft:iron_helmet{rtDye:3949738}')
    dyed_red, dyed_blue = scan(red), scan(blue)
    g.shot('worn_dyed')
    g.check(dyed_red >= plain_red + 25, 'a dyed (red) chestplate is drawn red on the player', 'red pixels: plain %d, dyed %d' % (plain_red, dyed_red))
    g.check(dyed_blue >= plain_blue + 6, 'a dyed (blue) helmet is drawn blue on the player', 'blue pixels: plain %d, dyed %d' % (plain_blue, dyed_blue))

    # glint: the same armor with the vanilla glint, then with a blue glint; the sheen of enchanted armor is purple unless tinted.
    ench = '{Enchantments:[{id:"minecraft:protection",lvl:1}]'
    box = [(x, y) for x in range(400, 452, 2) for y in range(262, 318, 2)]      # torso: almost all chestplate

    def frame_box():
        fr = desk.grab('Minecraft')
        return [desk.frame_px(fr, x, y) for x, y in box]

    def sheen(label, base):
        """What the glint ADDS on top of the plain armor (same pose, pixel by pixel), as sum(red)/sum(blue) over ~3 s of the animated glint:
        vanilla purple adds a lot of red (~0.6), a blue-dye glint adds little (~0.3)."""
        dr = db = 0
        g.key('f1'); g.wait(0.6)
        for _ in range(24):                                   # the glint is animated: more frames, a steadier ratio
            for (r, gg, b), (r0, g0, b0) in zip(frame_box(), base):
                if b - b0 > 3 and r - r0 >= 0:
                    dr += r - r0
                    db += b - b0
            g.wait(0.15)
        g.shot(label)
        g.key('f1'); g.wait(0.4)
        return (dr / float(db) if db else -1), db

    wear('minecraft:iron_chestplate', 'minecraft:iron_helmet')
    g.key('f1'); g.wait(0.6)
    base = frame_box()
    g.key('f1'); g.wait(0.4)
    wear('minecraft:iron_chestplate%s}' % ench, 'minecraft:iron_helmet%s}' % ench)
    vanilla, nv = sheen('worn_glint_vanilla', base)
    wear('minecraft:iron_chestplate%s,enchantmentColor:3949738}' % ench, 'minecraft:iron_helmet%s,enchantmentColor:3949738}' % ench)
    tinted, nt = sheen('worn_glint_blue', base)
    g.check(0 < tinted < vanilla - 0.02, 'the worn armor glint is bluer with enchantmentColor than the vanilla purple glint', 'added red/blue: vanilla %.2f (sum %d), blue glint %.2f (sum %d)' % (vanilla, nv, tinted, nt))
    g.key('f5'); g.wait(0.5)                                   # back to first person
    g.cmd('clear @s')
    g.cmd(item_cmd('armor.chest', 'minecraft:air'), 0.3)
    g.cmd(item_cmd('armor.head', 'minecraft:air'), 0.3)


PROBE = os.path.join(RUN_DIR, 'rt_probe')


def swirl_counts(g, seconds):
    """Potion-swirl particle counts the dev client logged (ParticleProbe, once a second, only while run/rt_probe exists) over the next `seconds`."""
    before = g.mark()
    g.wait(seconds)
    return [int(n) for n in re.findall(r'swirl particles near player: (\d+)', g.new_log(before))]


@scenario
def magic_hood(g):
    """#254: the Magic Hood hides the potion-particle swirl. The dev client counts SpellParticles within 3 blocks of the player once a second
    (client/ParticleProbe, enabled by the file run/rt_probe): many without the hood, none with it, many again after taking it off."""
    arena(g)
    open(PROBE, 'w').close()
    try:
        g.cmd('effect give @s minecraft:speed 300 0')
        g.wait(3)
        without = swirl_counts(g, 6)
        g.check(len(without) >= 4 and max(without) >= 2, 'control: with the speed effect and no hood the swirl particles exist', 'counts %s' % without)
        g.cmd(item_cmd('armor.head', 'randomthings:magic_hood'), 0.4)
        g.wait(3)                                                  # let the existing particles live out their 8-40 ticks
        hooded = swirl_counts(g, 6)
        g.check(len(hooded) >= 4 and max(hooded) == 0, '#254: wearing the Magic Hood: no swirl particles at all', 'counts %s' % hooded)
        g.cmd(item_cmd('armor.head', 'minecraft:air'), 0.4)
        g.wait(2)
        again = swirl_counts(g, 6)
        g.check(len(again) >= 4 and max(again) >= 2, 'control: taking the hood off brings the swirl back', 'counts %s' % again)
        g.key('f5'); g.wait(1.0); g.shot('hood_off_f5'); g.key('f5'); g.key('f5')
    finally:
        if os.path.exists(PROBE):
            os.remove(PROBE)
        g.cmd('effect clear @s')


# ---- tests added with the "remaining automated tests" pass -----------------------------------
@scenario
def redstone_remote_edit(g):
    """#287: bind targets through the Redstone Remote's EDIT screen (sneak-right-click: 9 Position Filter slots + 9 camo slots), then use the remote:
    each button fires only its own target. Two command blocks at the bound spots record which button was pressed."""
    arena(g)
    for p in ((10, 4, 10), (11, 4, 10)):
        g.cmd('setblock %d %d %d minecraft:air' % p)
    g.cmd('setblock 6 4 3 minecraft:command_block{Command:"setblock 10 4 10 minecraft:emerald_block"}')
    g.cmd('setblock 8 4 3 minecraft:command_block{Command:"setblock 11 4 10 minecraft:gold_block"}')
    pf = lambda x: 'randomthings:position_filter{hasPosition:1b,dimension:' + DIM + ',filterX:%d,filterY:4,filterZ:3}' % x
    g.slot(0, 'randomthings:redstone_remote')
    g.slot(1, pf(6))
    g.slot(2, pf(8))
    g.slot(3, 'minecraft:diamond')
    g.select(0)
    sky(g)
    g.click(427, 240, 'right', '--shift'); g.wait(1.0)
    if not gui_open(g, 176, 150, 'Redstone Remote edit screen'):
        return
    g.shot('edit_gui')
    W, H = 176, 150
    pick_place(g, hotbar_xy(W, H, 1), gui_slot(W, H, 8, 18))             # target 1 -> position slot 0
    pick_place(g, hotbar_xy(W, H, 2), gui_slot(W, H, 26, 18))            # target 2 -> position slot 1
    pick_place(g, hotbar_xy(W, H, 3), gui_slot(W, H, 8, 36))             # diamond -> camo slot 0 (the icon of button 1)
    g.wait(0.3)
    g.key('escape'); g.wait(0.6)
    held = ' '.join(g.cmd('data get entity @s Inventory[{Slot:0b}].tag', 0.7))
    g.check(held.count('position_filter') == 2 and 'filterX: 6' in held and 'filterX: 8' in held, "the edit screen stored both Position Filters in the remote's positions", held[:300])
    g.check('diamond' in held, 'the camo slot stored the diamond (button icon)')
    sky(g)
    g.click(427, 240, 'right'); g.wait(1.0)
    if not gui_open(g, 187, 41, 'Redstone Remote use screen'):
        return
    g.shot('use_gui_2_buttons')
    x2, y2 = gui_slot(187, 41, 5 + 20, 17)
    g.click(x2, y2); g.wait(0.6)                                         # button 2 only
    g.key('escape'); g.wait(0.6)
    g.check(block_is(g, 11, 4, 10, 'minecraft:gold_block') and block_isnt(g, 10, 4, 10, 'minecraft:emerald_block'),
            'button 2 fired only its own target (gold marker set, emerald marker not)')
    g.click(427, 240, 'right'); g.wait(1.0)
    if not gui_open(g, 187, 41, 'Redstone Remote use screen (2nd)'):
        return
    x1, y1 = gui_slot(187, 41, 5, 17)
    g.click(x1, y1); g.wait(0.6)
    g.key('escape'); g.wait(0.6)
    g.check(block_is(g, 10, 4, 10, 'minecraft:emerald_block'), 'button 1 fired target 1 (emerald marker set)')


@scenario
def block_destabilizer_lazy(g):
    """#295: with Lazy on the Block Destabilizer remembers the blocks that did NOT match (NBT invalidBlocks) and skips them on the next pulse; Reset
    forgets them; switching Lazy off forgets them too. Buttons are pressed in the GUI (Lazy / Reset)."""
    arena(g)
    g.cmd('setblock 2 7 5 randomthings:block_destabilizer[facing=south]')
    g.cmd('setblock 2 7 6 minecraft:stone')
    W, H = 85, 35
    lazy_btn, reset_btn = gui_slot(W, H, 7 + 10 - 8, 7 + 10 - 8), gui_slot(W, H, 58 + 10 - 8, 7 + 10 - 8)

    def press(btn, what):
        aim_at(g, 2, 7, 5, frm=(2.5, 4.0, 2.5))
        g.click(427, 240, 'right'); g.wait(0.9)
        if not gui_open(g, W, H, 'Block Destabilizer (%s)' % what):
            return False
        g.click(*btn); g.wait(0.4)
        g.key('escape'); g.wait(0.5)
        return True

    if not press(lazy_btn, 'Lazy on'):
        return
    g.check(dv(g, 'block 2 7 5', 'lazy') == '1b', 'the GUI Lazy button turned lazy mode on')
    pulse(g, '2 8 5'); g.wait(4)
    g.check(block_isnt(g, 2, 7, 6, 'minecraft:stone'), 'pulse 1: the single stone block dropped')
    inv1 = dv(g, 'block 2 7 5', 'invalidBlocks')
    g.check('{' in inv1, 'lazy mode remembered the non-matching neighbours (invalidBlocks written)', inv1[:160])
    g.cmd('kill @e[type=minecraft:falling_block]')
    g.cmd('fill 0 4 6 4 6 6 minecraft:air')
    # the remembered neighbour (3,7,6) becomes stone again next to a new target: lazy skips it
    g.cmd('setblock 2 7 6 minecraft:stone'); g.cmd('setblock 3 7 6 minecraft:stone')
    g.cmd('setblock 2 8 5 minecraft:air'); pulse(g, '2 8 5'); g.wait(4)
    g.check(block_isnt(g, 2, 7, 6, 'minecraft:stone') and block_is(g, 3, 7, 6, 'minecraft:stone'), 'pulse 2 (lazy): the remembered neighbour was skipped and stayed')
    g.cmd('kill @e[type=minecraft:falling_block]')
    g.cmd('fill 0 4 6 4 6 6 minecraft:air')
    if not press(reset_btn, 'Reset'):
        return
    g.check('invalidBlocks' not in ' '.join(g.cmd('data get block 2 7 5', 0.7)), 'the GUI Reset button forgot the remembered blocks')
    g.cmd('setblock 2 7 6 minecraft:stone')
    g.cmd('setblock 2 8 5 minecraft:air'); pulse(g, '2 8 5'); g.wait(5)
    g.check(block_isnt(g, 2, 7, 6, 'minecraft:stone') and block_isnt(g, 3, 7, 6, 'minecraft:stone'), 'after Reset the neighbour is visited again and dropped with the rest')
    g.cmd('kill @e[type=minecraft:falling_block]')
    if not press(lazy_btn, 'Lazy off'):
        return
    g.check(dv(g, 'block 2 7 5', 'lazy') == '0b' and 'invalidBlocks' not in ' '.join(g.cmd('data get block 2 7 5', 0.7)), 'switching Lazy off clears the memory too')


@scenario
def falling_block_obstruction(g):
    """#296: a block dropped by the Block Destabilizer that lands where it cannot be placed (a sign, a torch) vanishes WITHOUT dropping an item;
    on open ground it settles as a block again (control)."""
    arena(g)

    def run_case(obstacle, x):
        g.cmd('kill @e[type=!player]')
        g.cmd('fill %d 4 6 %d 9 6 minecraft:air' % (x, x))
        if obstacle:
            g.cmd('setblock %d 4 6 %s' % (x, obstacle))
        g.cmd('setblock %d 8 6 minecraft:stone' % x)
        g.cmd('setblock %d 8 5 randomthings:block_destabilizer[facing=south]' % x)
        g.cmd('setblock %d 9 5 minecraft:redstone_block' % x)
        g.wait(5)
        stone_item = entity_count(g, '@e[type=item,nbt={Item:{id:"minecraft:stone"}}]')
        placed = block_is(g, x, 4, 6, 'minecraft:stone') or block_is(g, x, 5, 6, 'minecraft:stone')
        gone_up = block_isnt(g, x, 8, 6, 'minecraft:stone')
        return stone_item, placed, gone_up

    item, placed, gone = run_case(None, 1)
    g.check(gone and placed and item == 0, 'control: on open ground the dropped block lands and is placed again (no item)', 'item=%s placed=%s left=%s' % (item, placed, gone))
    for label, obstacle, x in (('sign (tile entity)', 'minecraft:oak_sign', 3), ('torch', 'minecraft:torch', 5)):
        item, placed, gone = run_case(obstacle, x)
        none = block_isnt(g, x, 5, 6, 'minecraft:stone') and block_isnt(g, x, 4, 6, 'minecraft:stone')
        g.check(gone and none and item == 0, '#296: a block dropped onto a %s vanishes without an item or a placed block' % label, 'item=%s stone left in column=%s' % (item, not none))
    none_any = g.cmd('execute unless entity @e[type=item]', 0.5)
    if not any('Test passed' in l for l in none_any):
        g.note('items left: ' + ' | '.join(g.cmd('execute as @e[type=item] run data get entity @s Item.id', 0.5))[:200])
    g.check(any('Test passed' in l for l in none_any), '#296: no item entity of any kind exists afterwards')


@scenario
def rune_rendering(g):
    """#346: rune dust on Rune Bases. Seen from above in spectator mode: the right number of separate coloured shapes (same-colour neighbours join, also across
    the edge into the next base; different colours do not), the texture is noisy (not one flat colour), and the picture is static (a flicker would be a bug) - the same frame twice, and
    again after moving the camera away and back, is pixel-identical (the noise is fixed per block position, so any difference would be a flicker bug). Properties are asserted; screenshots are evidence only."""
    arena(g)
    g.cmd('gamemode spectator')
    for x in (0, 1):
        g.cmd('setblock %d 4 3 randomthings:rune_base' % x)
    RED, BLUE = 14, 11

    def runes(cells):
        flat = [-1] * 16
        for (cx, cy), col in cells.items():
            flat[cx * 4 + cy] = col
        return '{runeData:[B;%s]}' % ','.join('%db' % v for v in flat)

    cam = (1.0, 6.4, 3.5, 0, 90)
    g.tp(*cam); g.wait(1.0)

    def frame():
        return desk.grab('Minecraft')

    def mask(fr, kind):
        out = set()
        for x in range(200, 654):
            for y in range(40, 440):
                r, gg, b = desk.frame_px(fr, x, y)
                if kind == 'red' and r > 70 and r > 1.7 * gg and r > 1.7 * b:
                    out.add((x, y))
                elif kind == 'blue' and b > 60 and b > 1.4 * r and b > 1.15 * gg:
                    out.add((x, y))
        return out

    def components(px, min_size=40):
        seen, n = set(), 0
        for p in px:
            if p in seen:
                continue
            stack, size = [p], 0
            seen.add(p)
            while stack:
                x, y = stack.pop()
                size += 1
                for q in ((x + 1, y), (x - 1, y), (x, y + 1), (x, y - 1)):
                    if q in px and q not in seen:
                        seen.add(q)
                        stack.append(q)
            if size >= min_size:
                n += 1
        return n

    def hidden(fn):
        """Run fn with the HUD and chat hidden (F1) so nothing covers the picture."""
        g.key('f1'); g.wait(0.6)
        try:
            return fn()
        finally:
            g.key('f1'); g.wait(0.4)

    g.cmd('data merge block 0 4 3 %s' % runes({}), 0.5)
    g.cmd('data merge block 1 4 3 %s' % runes({}), 0.5)
    g.wait(1.0)
    f0 = hidden(frame)
    g.check(components(mask(f0, 'red')) == 0 and components(mask(f0, 'blue')) == 0, 'control: empty Rune Bases show no coloured runes')
    g.cmd('data merge block 0 4 3 %s' % runes({(0, 0): RED, (1, 0): RED, (0, 2): BLUE, (1, 2): BLUE, (3, 3): RED, (3, 1): RED, (2, 1): BLUE}), 0.5)
    g.cmd('data merge block 1 4 3 %s' % runes({(0, 3): RED}), 0.5)
    g.wait(1.0)

    def two_frames():
        fa = frame()
        g.wait(0.5)
        return fa, frame()
    fa, fb = hidden(two_frames)
    g.key('f1'); g.wait(0.5); g.shot('runes'); g.key('f1'); g.wait(0.4)
    red, blue = mask(fa, 'red'), mask(fa, 'blue')
    nr, nb = components(red), components(blue)
    g.check(nr == 3, 'red runes form 3 shapes: the joined pair, the lone one next to a blue one, and the pair joined ACROSS the base edge', 'red shapes %d (pixels %d)' % (nr, len(red)))
    g.check(nb == 2, 'blue runes form 2 shapes: the joined pair and the lone one that touches red without joining', 'blue shapes %d (pixels %d)' % (nb, len(blue)))
    vals = [desk.frame_px(fa, x, y)[0] for (x, y) in list(red)]
    mean = sum(vals) / float(len(vals)) if vals else 0
    std = (sum((v - mean) ** 2 for v in vals) / float(len(vals))) ** 0.5 if vals else 0
    g.check(std > 5, 'the rune texture is noisy, not one flat colour', 'red channel std %.1f over %d px' % (std, len(vals)))

    xs = [p[0] for p in red | blue]
    ys = [p[1] for p in red | blue]
    x0, x1, y0, y1 = min(xs) - 6, max(xs) + 6, min(ys) - 6, max(ys) + 6      # the area the runes occupy (the rest of the frame is grass)

    def differ(f1, f2):
        diff = total = 0
        for x in range(x0, x1):
            for y in range(y0, y1):
                a, b2 = desk.frame_px(f1, x, y), desk.frame_px(f2, x, y)
                total += 1
                if abs(a[0] - b2[0]) + abs(a[1] - b2[1]) + abs(a[2] - b2[2]) > 12:
                    diff += 1
        return diff / float(total)
    d_empty = differ(fa, f0)
    d_same = differ(fa, fb)
    g.tp(1.0, 6.4, 5.5, 0, 90); g.wait(1.0)
    g.tp(*cam); g.wait(1.2)
    fc = hidden(frame)
    d_moved = differ(fa, fc)
    g.check(d_empty > 0.05, 'sensitivity: the rune area of the empty bases differs clearly from the painted ones (the comparison can see runes)', '%.1f%% of the rune area differs' % (100 * d_empty))
    g.check(d_same < 0.01, 'the runes are static: two frames of the same view are identical in the rune area (no per-frame re-rolled noise)', '%.2f%% pixels differ' % (100 * d_same))
    g.check(d_moved < 0.01, 'runes are stable while the camera moves away and back (no re-rolled noise)', '%.2f%% pixels differ' % (100 * d_moved))
    g.cmd('gamemode creative')


# ---- A7 -----------------------------------------------------------------------------------
@scenario
def light_redirector(g):
    """#319: right-clicking a face toggles it open/closed (block state + NBT); the 'periscope' picture is a screenshot."""
    arena(g)
    g.cmd('setblock 0 4 3 randomthings:light_redirector')
    g.cmd('setblock 0 4 4 minecraft:diamond_block')
    aim_at(g, 0, 4, 3)
    g.shot('before_toggle')
    g.click(427, 240, 'right'); g.wait(0.5)
    g.check(dv(g, 'block 0 4 3', 'enabled_north') == '0b', '#319: right-clicking the north face closes it (NBT)')
    g.check(block_is(g, 0, 4, 3, 'randomthings:light_redirector[north=false]'), '#319: ... and the block state follows')
    g.shot('north_closed')
    g.click(427, 240, 'right'); g.wait(0.5)
    g.check(dv(g, 'block 0 4 3', 'enabled_north') == '1b', '#319: right-clicking again opens it')


# ---- Spectre Illuminator (client-side light, screenshots as evidence) --------------------------
@scenario
def spectre_illuminator(g):
    """#272-274 on the CLIENT: at midnight the floor in view is dark; a placed Spectre Illuminator lights the chunk (the screen gets clearly brighter),
    removing the orb makes the screen dark again. Screenshots: dark / lit / dark_again; the evidence is the average brightness of the floor in view."""
    arena(g)
    g.cmd('gamerule doDaylightCycle false')
    g.cmd('time set midnight')
    g.cmd('fill -10 3 -10 10 3 10 minecraft:stone')       # one even grey floor, so only the light level changes the picture
    g.tp(0.5, 4, 0.5, 0, 50)
    g.wait(2.0)

    def brightness():
        fr = desk.grab('Minecraft')
        vals = [sum(desk.frame_px(fr, x, y)) / 3.0 for x in range(240, 620, 8) for y in range(300, 440, 8)]
        return sum(vals) / len(vals)

    def settle(pred, limit, what):
        """Poll the brightness every 2 s until pred(value) holds; returns the last value."""
        t0 = time.time()
        v = brightness()
        while not pred(v) and time.time() - t0 < limit:
            g.wait(2.0)
            v = brightness()
        g.note('%s: brightness %.1f after %.0f s' % (what, v, time.time() - t0))
        return v

    dark = settle(lambda v: True, 0, 'before')
    g.shot('dark')
    g.slot(0, 'randomthings:spectre_illuminator')
    g.select(0)
    g.click(427, 240, 'right'); g.wait(1.0)
    g.check(int(' '.join(g.cmd('execute if entity @e[type=randomthings:spectre_illuminator]', 0.5)).count('Test passed')) == 1, 'the orb was placed')
    lit = settle(lambda v: v > dark + 25, 240, 'lighting up')
    g.shot('lit')
    g.check(lit > dark + 25, 'client: the chunk is clearly brighter with the Illuminator (%.1f -> %.1f)' % (dark, lit))
    g.cmd('kill @e[type=randomthings:spectre_illuminator]'); g.wait(1.0)
    again = settle(lambda v: v < dark + (lit - dark) * 0.3, 240, 'going dark')
    g.shot('dark_again')
    g.check(again < dark + (lit - dark) * 0.3, 'client: the chunk goes dark again after the orb is removed (%.1f -> %.1f, was %.1f lit)' % (lit, again, dark))
    g.cmd('gamerule doDaylightCycle false')               # the state controlled_setup() made: later scenarios (grass spread, light checks, pixel checks) need frozen daylight
    g.cmd('time set day')


# ---- A8 -----------------------------------------------------------------------------------
@scenario
def ender_mailbox(g):
    """#209-#214: place a mailbox, deliver a letter addressed to yourself (the mailbox starts glowing), reject an unknown receiver."""
    arena(g)
    name = player_name(g)
    g.note('player name: %s' % name)
    g.slot(0, 'randomthings:ender_mailbox')
    g.select(0)
    aim_at(g, 0, 3, 2)                                # look at the floor block (0,3,2): the mailbox is placed on top of it, at (0,4,2)
    g.click(427, 240, 'right'); g.wait(0.6)
    g.check(block_is(g, 0, 4, 2, 'randomthings:ender_mailbox'), '#209: the Ender Mailbox was placed')
    g.cmd('clear @s')
    letter = 'randomthings:ender_letter{receiver:"%s"}' % name
    g.slot(1, letter)
    g.select(1)
    aim_at(g, 0, 4, 2)
    g.check(block_is(g, 0, 4, 2, 'randomthings:ender_mailbox[active=false]'), 'control: the mailbox is not glowing without mail')
    g.click(427, 240, 'right', '--shift'); g.wait(0.8)
    out = inv(g)
    g.check('ender_letter' not in out, '#210: the sneak-right-clicked letter was consumed', out[:160])
    until(g, lambda: block_is(g, 0, 4, 2, 'randomthings:ender_mailbox[active=true]'), 30.0, 0.5)
    g.check(block_is(g, 0, 4, 2, 'randomthings:ender_mailbox[active=true]'), '#214: with mail waiting the mailbox glows within ~10 s')
    g.select(2)
    aim_at(g, 0, 4, 2)
    g.click(427, 240, 'right'); g.wait(1.0)
    if not gui_open(g, 176, 165, 'Ender Mailbox'):
        return
    g.shot('inbox')
    g.click(*gui_slot(176, 165, 8, 18), 'left', '--shift'); g.wait(0.4)     # shift-click slot 0 of the inbox: take the letter
    g.key('escape'); g.wait(0.6)
    out = inv(g)
    g.check('ender_letter' in out and 'received' in out, '#215: the delivered letter is in the inbox and can be taken (marked received)', out[:240])
    until(g, lambda: block_is(g, 0, 4, 2, 'randomthings:ender_mailbox[active=false]'), 30.0, 0.5)
    g.check(block_is(g, 0, 4, 2, 'randomthings:ender_mailbox[active=false]'), '#214: once everything is taken the mailbox stops glowing')
    # #211 (unknown receiver is refused) cannot be tested here: an offline dev client resolves ANY name to an offline profile, so the letter is
    # simply delivered (same code path as 1.12.2). It needs an online session with a name that does not exist - left to the manual route.
    g.note('#211 not scriptable offline: any receiver name resolves to an offline profile')


# ---- A9 -----------------------------------------------------------------------------------
def build_antenna(g, x, y, z):
    for dx, dy, dz in ((0, 1, 0), (0, 2, 0), (1, 2, 0), (-1, 2, 0), (1, 3, 0), (-1, 3, 0), (0, 2, 1), (0, 2, -1), (0, 3, 1), (0, 3, -1)):
        g.cmd('setblock %d %d %d minecraft:iron_bars' % (x + dx, y + dy, z + dz), 0.25)


@scenario
def biome_radar(g):
    """#149-#153: crystal in/out by hand, a search that finishes (plains on a superflat world) and yields a Position Filter for paper,
    a search for a biome that does not exist here that is cancelled by breaking an antenna bar, and the crystal dropping when the radar breaks."""
    arena(g)
    g.cmd('setblock 0 4 3 randomthings:biome_radar')
    build_antenna(g, 0, 4, 3)
    crystal = lambda biome: 'randomthings:biome_crystal{biomeName:"minecraft:%s"}' % biome
    g.slot(0, crystal('plains'))
    g.select(0)
    aim_at(g, 0, 4, 3)
    g.click(427, 240, 'right'); g.wait(0.5)
    out = g.cmd('data get block 0 4 3 currentCrystal')
    g.expect_chat(out, r'biome_crystal', '#149: right-clicking the radar with a crystal inserts it')
    g.select(1)
    g.click(427, 240, 'right'); g.wait(0.5)
    out = g.cmd('data get block 0 4 3 currentCrystal')
    g.check('biome_crystal' not in ' '.join(out), '#149: an empty-handed right-click takes the crystal out again', ' '.join(out)[:120])
    g.check('biome_crystal' in inv(g), '#149: ... and it is back in the inventory')
    # --- #150/#151: plains exist everywhere on a superflat world, so the search finishes at once (the crystal is in slot 1 now)
    g.select(1)
    g.click(427, 240, 'right'); g.wait(0.5)
    g.cmd('setblock 1 4 3 minecraft:redstone_block')
    g.wait(2)
    g.check(dv(g, 'block 0 4 3', 'state') == '2', '#150/#151: powered with the antenna built the radar searches and finishes (state FINISHED)')
    g.shot('radar_finished')
    g.slot(2, 'minecraft:paper')
    g.select(2)
    g.click(427, 240, 'right'); g.wait(0.6)
    out = inv(g)
    g.check('position_filter' in out and 'hasPosition' in out and 'minecraft:paper' not in out, '#151: paper is turned into a Position Filter pointing at the biome', out[:240])
    # --- #152: a biome that cannot be found keeps the radar searching; breaking a far antenna bar returns it to idle
    g.cmd('setblock 1 4 3 minecraft:air')
    g.wait(1)
    g.cmd('data merge block 0 4 3 {currentCrystal:{id:"randomthings:biome_crystal",Count:1b,tag:{biomeName:"minecraft:desert"}},state:0}')
    g.cmd('setblock 1 4 3 minecraft:redstone_block')
    g.wait(2)
    g.check(dv(g, 'block 0 4 3', 'state') == '1', '#150: searching for a biome that does not exist on this world keeps the radar searching')
    g.cmd('setblock 1 7 3 minecraft:air')
    g.wait(4)
    g.check(dv(g, 'block 0 4 3', 'state') == '0', '#152: breaking an antenna bar sends the radar back to idle within seconds')
    # --- #153: breaking the radar with a crystal inside drops the crystal
    g.cmd('kill @e[type=item]')
    g.cmd('setblock 0 4 3 minecraft:air destroy')
    g.wait(0.8)
    g.check(entity_count(g, '@e[type=item,nbt={Item:{id:"randomthings:biome_crystal"}}]') >= 1, '#153: the crystal dropped when the radar was broken')


def bucket_item(item, fluid):
    """An Ender Bucket holding 1000 mB of `fluid` (the fluid component on 1.20.5+, the FluidHandlerItemStack NBT before)."""
    if globals().get('MC205'):
        return '%s[randomthings:fluid={id:"minecraft:%s",amount:1000}]' % (item, fluid)
    return '%s{Fluid:{FluidName:"minecraft:%s",Amount:1000}}' % (item, fluid)


@scenario
def bucket_held(g):
    """Ender Bucket and Reinforced Ender Bucket as the held item (first person, looking at the grass): while the bucket holds water the opening is drawn blue,
    with lava orange, and an empty bucket shows neither. Counted in the lower right of the screen where the hand item is, against the empty bucket of the same kind."""
    arena(g)
    g.cmd('gamerule doDaylightCycle false')
    g.cmd('time set 6000')
    g.tp(0.5, 4, 0.5, 0, 25)                                  # look at the grass ahead: the hand item is in view and the sky (above y 120) stays out of the counted area
    g.key('1'); g.wait(0.3)
    blue = lambda r, gg, b: b > 60 and b - gg > 25 and b - r > 40                  # water: dark navy once lit and tinted, e.g. (21,40,77)
    orange = lambda r, gg, b: r > 70 and r - b > 50 and r - gg > 25                # lava: dark orange, e.g. (105,51,13)
    xs, ys = range(560, 850, 3), range(150, 430, 3)

    def scan(item, label):
        g.cmd(item_cmd('hotbar.0', item), 0.4)
        g.wait(1.4)                                           # the equip animation of the new stack and the first frames with it
        fr = desk.grab('Minecraft')
        nb = no = 0
        for x in xs:
            for y in ys:
                px = desk.frame_px(fr, x, y)
                if blue(*px):
                    nb += 1
                if orange(*px):
                    no += 1
        g.shot('held_' + label)
        return nb, no

    for name, item in (('ender', 'randomthings:ender_bucket'), ('reinforced', 'randomthings:reinforced_ender_bucket')):
        empty_b, empty_o = scan(item, name + '_empty')
        water_b, water_o = scan(bucket_item(item, 'water'), name + '_water')
        lava_b, lava_o = scan(bucket_item(item, 'lava'), name + '_lava')
        g.check(water_b >= empty_b + 12, 'a held %s bucket with water is drawn with blue in its opening' % name, 'blue pixels: empty %d, water %d' % (empty_b, water_b))
        g.check(lava_o >= empty_o + 12, 'a held %s bucket with lava is drawn with orange in its opening' % name, 'orange pixels: empty %d, lava %d' % (empty_o, lava_o))
        g.check(water_o <= empty_o + 3 and lava_b <= empty_b + 3, 'the %s bucket shows only its own fluid (no orange with water, no blue with lava)' % name,
                'water: orange %d (empty %d); lava: blue %d (empty %d)' % (water_o, empty_o, lava_b, empty_b))
    g.cmd(item_cmd('hotbar.0', 'minecraft:air'), 0.3)


@scenario
def render_fixes_18(g):
    """1.18.2: Advanced Redstone Torch has no black box, Fluid Display is hidden behind a wall (control: visible without one), Sound Recorder rows highlight on hover."""
    arena(g)
    g.cmd('fill -3 4 3 3 4 3 minecraft:stone')                       # wall in front of the first display
    g.cmd('setblock 1 4 5 randomthings:fluid_display{fluidStack:{FluidName:"minecraft:water",Amount:1000}}')
    g.cmd('setblock 5 4 5 randomthings:fluid_display{fluidStack:{FluidName:"minecraft:water",Amount:1000}}')
    g.cmd('setblock 5 4 2 randomthings:advanced_redstone_torch')
    g.cmd('setblock 6 4 2 minecraft:redstone_torch')
    g.wait(1.5)                                                      # the chunk has to be re-meshed with the new blocks before anything is drawn
    g.tp(5.5, 4, 0.5, 0, 12)
    g.key('f1'); g.wait(1.0)
    g.shot('torch_and_display_control')
    g.tp(0.5, 4, 0.5, 0, 8)
    g.wait(1.5)
    g.shot('display_behind_wall')
    g.key('f1'); g.wait(1.0)
    px = desk.grab('Minecraft')
    wall = desk.frame_px(px, 355, 290)       # where the display behind the wall overlapped the wall's face before the fix (blue)
    g.check(wall[2] - wall[0] < 20, 'fluid display behind a wall is occluded (wall face is not tinted blue)', 'pixel %s' % (wall,))
    g.slot(0, 'randomthings:sound_recorder{recordList:["block.stone.step","entity.cow.ambient","ui.button.click"]}')
    g.select(0)
    g.click(427, 240, 'right')
    g.wait(1.0)
    gui_open(g, 190, 186, 'Sound Recorder')
    g.do('move', '366', '106')
    g.wait(0.6)
    g.shot('recorder_hover_row1')
    bar = desk.pixel('Minecraft', 580, 106)
    g.check(bar[0] < 90 and bar[1] < 90 and bar[2] < 90, 'sound recorder: hovered row shows the dark highlight bar', 'pixel %s' % (bar,))
    g.do('move', '366', '135')
    g.wait(0.6)
    g.shot('recorder_hover_row2')
    g.key('escape'); g.wait(0.5)


# ---- A10 ----------------------------------------------------------------------------------
@scenario
def bean_stew(g):
    """#121: eating Bean Stew restores 8 hunger and leaves an empty bowl."""
    arena(g)
    g.cmd('gamemode survival')
    g.cmd('effect give @s minecraft:hunger 6 100')
    game_wait(g, 7)
    g.cmd('effect clear @s')
    f0 = dv(g, 'entity @s', 'foodLevel')
    n0 = int(f0) if re.fullmatch(r'\d+', f0) else -1
    g.slot(0, 'randomthings:bean_stew')
    g.select(0)
    sky(g)
    g.hold(427, 240, 'right', 2.4)
    g.wait(0.6)
    f1 = dv(g, 'entity @s', 'foodLevel')
    n1 = int(f1) if re.fullmatch(r'\d+', f1) else -1
    g.check(n0 >= 0 and (n1 - n0 == 8 or n1 == 20), '#121: Bean Stew restores 8 food points', 'food %s -> %s' % (n0, n1))
    g.check('minecraft:bowl' in inv(g), '#121: an empty bowl is left')
    g.cmd('gamemode creative')


@scenario
def magnetic(g):
    """#288: with the Magnetic enchantment mined drops go straight into the inventory; without it they fall on the ground (control)."""
    arena(g)
    g.cmd('gamemode survival')
    g.slot(0, 'minecraft:diamond_pickaxe')
    g.select(0)
    g.cmd('setblock 0 4 3 minecraft:stone')
    aim_at(g, 0, 4, 3)
    g.hold(427, 240, 'left', 1.6)
    g.wait(0.5)
    g.check(entity_count(g, '@e[type=item,nbt={Item:{id:"minecraft:cobblestone"}}]') >= 1 and 'cobblestone' not in inv(g),
            'control: without the enchantment the drop lies on the ground')
    g.cmd('kill @e[type=item]')
    g.slot(0, 'minecraft:diamond_pickaxe{Enchantments:[{id:"randomthings:magnetic",lvl:1}]}')
    g.cmd('setblock 0 4 3 minecraft:stone')
    aim_at(g, 0, 4, 3)
    g.hold(427, 240, 'left', 1.6)
    g.wait(0.5)
    g.check('cobblestone' in inv(g) and entity_count(g, '@e[type=item]') == 0, '#288: Magnetic put the drop straight into the inventory, nothing on the ground')
    g.cmd('gamemode creative')


@scenario
def colored_grass(g):
    """#343 / #344: coloured grass seeds turn dirt into coloured grass; it spreads onto nearby dirt and reverts to dirt when covered.
    Done on a stone platform 7 blocks above the grass floor so vanilla grass cannot spread onto the test dirt (it reaches 3 blocks down)."""
    arena(g)
    g.cmd('fill -2 9 -2 6 9 6 minecraft:stone')
    base = (0.5, 10.0, 0.5)
    g.cmd('setblock 0 10 3 minecraft:dirt')
    g.cmd('setblock 2 10 3 minecraft:dirt')
    g.slot(0, 'randomthings:grass_seeds_red')
    g.select(0)
    aim_at(g, 0, 10, 3, frm=base)
    g.click(427, 240, 'right'); g.wait(0.6)
    g.check(block_is(g, 0, 10, 3, 'randomthings:colored_grass'), '#343: the seeds turned the dirt into coloured grass')
    g.shot('colored_grass')
    g.cmd('gamerule randomTickSpeed 3000')
    g.cmd('setblock 1 10 3 minecraft:dirt')
    until(g, lambda: block_is(g, 1, 10, 3, 'randomthings:colored_grass') or block_is(g, 2, 10, 3, 'randomthings:colored_grass'), 40.0, 1.0)
    g.check(block_is(g, 1, 10, 3, 'randomthings:colored_grass') or block_is(g, 2, 10, 3, 'randomthings:colored_grass'), '#344: it spread onto the nearby lit dirt')
    g.check(not block_is(g, 1, 10, 3, 'minecraft:grass_block') and not block_is(g, 2, 10, 3, 'minecraft:grass_block'), 'control: no vanilla grass appeared on the platform')
    g.cmd('setblock 0 11 3 minecraft:stone')
    until(g, lambda: block_is(g, 0, 10, 3, 'minecraft:dirt'), 40.0, 1.0)
    g.check(block_is(g, 0, 10, 3, 'minecraft:dirt'), '#344: covered by a block it reverts to dirt')
    g.cmd('gamerule randomTickSpeed 3')


# ---- B (default world: new_world(kind='default')) ----------------------------------------------
def player_pos(g):
    out = ' '.join(g.cmd('data get entity @s Pos'))
    nums = re.findall(r'(-?\d+\.\d+)d', out)
    return [float(n) for n in nums[:3]] if len(nums) >= 3 else None


def arena_default(g):
    g.cmd('gamemode creative')
    g.cmd('weather clear')
    g.cmd('kill @e[type=!player]')
    g.cmd('effect clear @s')
    g.cmd('clear @s')
    g.cmd('gamerule randomTickSpeed 3')


@scenario(world='default')
def ancient_furnace(g):
    """#265: an Ancient Furnace in a snowy biome (the nearest igloo of seed 424242) melts the ice around it, turns the biome warm and then explodes.
    The 5-minute heating time is skipped by setting the block entity's counter to 10 ticks before the end."""
    arena_default(g)
    before = g.mark()
    g.cmd('locatebiome %s' % SNOWY_BIOME if MC16 else 'locate Igloo', 0.5)     # 1.16 igloo spots are not reliably snowy: ask for the biome itself
    text = ''
    for _ in range(40):                       # the search generates chunks: can take a while
        g.wait(3)
        text = g.new_log(before)
        if re.search(r'nearest (igloo|minecraft:snowy_(?:tundra|plains))', text, re.I) or 'Could not find' in text or 'Incorrect argument' in text or 'Unknown' in text:
            break
    m = re.search(r'(?i)nearest (?:igloo|minecraft:snowy_(?:tundra|plains)) is at \[(-?\d+), (?:~|-?\d+), (-?\d+)\]', text)
    out = [text[-300:]]
    if not m:
        g.check(False, 'locate Igloo found nothing', ' '.join(out)[:200])
        return
    ix, iz = int(m.group(1)), int(m.group(2))
    g.note('igloo at %d %d' % (ix, iz))
    g.cmd('tp @s %d 200 %d' % (ix, iz), 0.5)
    g.wait(12)
    pos = player_pos(g)
    if not pos:
        g.check(False, 'could not read the player position')
        return
    px, py, pz = int(math.floor(pos[0])), int(math.floor(pos[1])), int(math.floor(pos[2]))
    g.note('standing at %d %d %d' % (px, py, pz))
    fx, fz = (px, pz) if MC16 else (px + 6, pz)
    ice = 5 if MC16 else 8
    # 1.16: /locatebiome is approximate and biome edges are fuzzy, so one ice block may sit in a warm column. Put ice (replacing the top ground block, found with
    # `positioned over motion_blocking`) on a ring of spots up to 40 blocks out: the furnace melts the ones in cold columns; at least one must turn to water.
    ring = [(-40, 0), (40, 0), (0, -40), (0, 40), (-28, -28), (28, 28), (-28, 28), (28, -28), (-14, 0), (14, 0), (0, -14), (0, 14), (20, 0), (-20, 0)]
    g.cmd('forceload add %d %d %d %d' % (fx - 56, fz - 56, fx + 56, fz + 56))      # 7x7 chunks
    g.wait(6)
    if MC16:
        for dx, dz in ring:
            g.cmd('execute positioned %d 0 %d over motion_blocking run setblock ~ ~-1 ~ minecraft:packed_ice' % (fx + dx, fz + dz), 0.3)
    else:
        g.cmd('setblock %d %d %d minecraft:packed_ice' % (fx + ice, py - 1, fz))
    g.cmd('setblock %d %d %d randomthings:ancient_furnace' % (fx, py, fz))
    g.cmd('data merge block %d %d %d {heating:1b,ticks:5990}' % (fx, py, fz))
    g.wait(5)
    g.check(block_isnt(g, fx, py, fz, 'randomthings:ancient_furnace'), '#265: the Ancient Furnace exploded when its heating finished')
    if MC16:
        melted = 0
        for dx, dz in ring:
            out = g.cmd('execute positioned %d 0 %d over motion_blocking if block ~ ~-1 ~ minecraft:water run say melted' % (fx + dx, fz + dz), 0.3)
            melted += 1 if any('melted' in l for l in out) else 0
        g.check(melted >= 1, '#265: ice in snowy columns around it was melted to water (1.16 ring of 14 spots)', '%d of %d spots melted' % (melted, len(ring)))
    else:
        g.check(block_is(g, fx + ice, py - 1, fz, 'minecraft:water'), '#265: the ice in a snowy biome around it was melted to water', 'at %d %d %d' % (fx + ice, py - 1, fz))
    g.shot('after_furnace')
    g.cmd('forceload remove all')


@scenario(world='default')
def glowing_mushroom_cave(g):
    """#387: a Glowing Mushroom placed in a sealed dark room stays (it used to drop itself) and spreads to dark neighbouring spots."""
    arena_default(g)
    g.cmd('gamerule randomTickSpeed 3000')
    x, y, z = 20000, 20, 20000
    g.cmd('tp @s %d 80 %d' % (x, z), 0.5)
    g.wait(8)
    g.cmd('fill %d %d %d %d %d %d minecraft:stone' % (x - 5, y - 1, z - 5, x + 5, y + 8, z + 5))
    g.cmd('fill %d %d %d %d %d %d minecraft:air' % (x - 4, y + 1, z - 4, x + 4, y + 6, z + 4))
    g.cmd('tp @s %d %d %d' % (x, y + 1, z), 0.4)
    g.cmd('setblock %d %d %d randomthings:glowing_mushroom' % (x, y + 1, z + 2))
    g.shot('mushroom_placed')
    g.wait(2)
    g.check(block_is(g, x, y + 1, z + 2, 'randomthings:glowing_mushroom'), '#387: the placed Glowing Mushroom is still there after 2 s (does not drop itself)')
    for t in (3, 5, 8):                 # diagnostics: how the placed mushroom evolves, and whether it dropped itself as an item
        g.wait(t)
        here = block_is(g, x, y + 1, z + 2, 'randomthings:glowing_mushroom')
        items = entity_count(g, '@e[type=item,nbt={Item:{id:"randomthings:glowing_mushroom"}}]')
        g.note('t+%ds: mushroom block %s, dropped mushroom items %d' % (t, 'present' if here else 'GONE', items))
    g.wait(8)
    out = g.cmd('fill %d %d %d %d %d %d minecraft:air replace randomthings:glowing_mushroom' % (x - 4, y + 1, z - 4, x + 4, y + 6, z + 4), 0.8)
    m = re.search(r'filled (\d+) block', ' '.join(out))
    n = int(m.group(1)) if m else 0
    g.check(n >= 1, '#387: after 27 s the mushroom is still there', '%d mushroom blocks' % n)
    g.check(n >= 2, '#387: ... and it spread to neighbouring dark spots', '%d mushroom blocks' % n)
    g.cmd('gamerule randomTickSpeed 3')



# ---------------------------------------------------------------------------------------------
# World lifecycle: every scenario gets a brand-new superflat creative world with fixed rules, and the world is deleted afterwards.
# ---------------------------------------------------------------------------------------------
SAVES = os.path.join(RUN_DIR, 'saves')


def prune_scenario_worlds():
    if os.path.isdir(SAVES):
        for d in os.listdir(SAVES):
            if d.startswith('Scn_'):
                shutil.rmtree(os.path.join(SAVES, d), ignore_errors=True)


def is_dark(x, y, limit=40):
    r, gg, b = desk.pixel('Minecraft', x, y)
    return max(r, gg, b) < limit


def require_screen(g, what, x, y):
    """Refuse to click on until a black text box (search/name field) is visible at (x, y)."""
    for _ in range(40):
        if is_dark(x, y):
            return
        desk.sleep(0.5)
    raise desk.Abort('expected the %s screen but it never appeared' % what)


def new_world(g, world, kind='flat'):
    global WORLD_KIND_NOW
    WORLD_KIND_NOW = kind
    g.note('creating %s world %s with seed %s' % (kind, world, SEED))
    g.do('focus')
    g.click(426, 236)                          # main menu: Singleplayer
    require_screen(g, 'world list', 426, 64)   # the black search box
    g.click(584, 395)                          # Create New World
    require_screen(g, 'create world', 300, 139)
    g.click(426, 139); g.key('a', ctrl=True); g.do('text', world)
    if MC16:                                   # 1.16 create-world screen: game mode at (266,219); picking Creative also turns cheats ON
        g.click(266, 219); g.wait(0.6); g.click(266, 219); g.wait(0.6)      # slow click pairs are needed on the production client
        g.click(586, 389)                      # More World Options
    else:
        g.click(426, 249); g.click(426, 249)   # Survival -> Hardcore -> Creative
        g.click(426, 393)                      # More World Options
    g.wait(1.0)
    g.click(426, 139); g.key('a', ctrl=True); g.do('text', SEED)     # seed field
    if kind == 'flat':
        g.click(584, 219)                      # World Type: Default -> Superflat
    g.wait(0.5)
    g.shot('create_options')
    g.click(266, 443)                          # Create New World
    for attempt in range(100):                  # wait until the in-game chat works
        if not desk.find_window('Minecraft'):
            raise desk.Abort('the Minecraft window is gone')
        g.wait(2)
        before = g._log_size()
        try:
            if FAST and os.path.exists(FAST_MARK):
                # the world is not up yet: hand the command to the channel only. Typing it as a fallback opens the chat with a stray '/' when the world appears mid-way,
                # and the first click of the first scenario then lands in the chat
                g._send_fast('say ready', before, 0.6)
                with open(LOG, 'rb') as f:
                    f.seek(before)
                    out = [l.split('[CHAT] ', 1)[1] for l in f.read().decode('utf-8', errors='replace').splitlines() if '[CHAT] ' in l]
            else:
                out = g.cmd('say ready', 0.6)
        except desk.Abort:
            raise                              # window gone, stop key pressed, wrong window: stop at once, never retry
        except Exception:
            out = []
        if any('ready' in l for l in out):
            break
    else:
        raise desk.Abort('world did not load in time')
    controlled_setup(g)


WORLD_KIND_NOW = 'flat'     # set by new_world: the flat-world floor is only laid in flat worlds


def controlled_setup(g):
    """Same controlled conditions as the automated suite's world: creative, always day, no weather, no mob spawning, no stray mobs."""
    for c in ('gamemode creative', 'time set day', 'gamerule doDaylightCycle false', 'gamerule doWeatherCycle false',
              'gamerule doMobSpawning false', 'gamerule doFireTick false', 'gamerule announceAdvancements false', 'weather clear',
              'kill @e[type=!player]', 'effect clear @s', 'clear @s'):
        g.cmd(c, 0.3)
    if MC18 and WORLD_KIND_NOW == 'flat':
        # 1.18's superflat ground is at y -61 (world bottom -64); every scenario was written for ground at y 3 (blocks at y 4), so a
        # grass floor is laid there (a single /fill is limited to 32768 blocks: 180x180)
        g.cmd('fill -90 3 -90 89 3 89 minecraft:grass_block', 1.5)
    g.tp(0.5, 4, 0.5, 0, 0)
    g.wait(2.0)                                                      # a freshly created world: the first chunks, the inventory and the camera settle before the first scenario clicks


def leave_world(g, world):
    g.key('escape'); g.wait(0.8)
    g.shot('pause_menu')
    g.click(426, 347)                          # Save and Quit to Title
    g.wait(5)
    shutil.rmtree(os.path.join(SAVES, world), ignore_errors=True)
    g.note('world %s deleted' % world)


if __name__ == '__main__':
    names = sys.argv[1:]
    FIRST = ['imbuing', 'imbue_effects', 'rain_shield', 'ender_anchor']
    if names == ['all']:
        names = sorted(SCENARIOS, key=lambda n: WORLD_KIND[n] != 'flat')      # stable: flat-world scenarios first, then the default-world ones
    elif names == ['new']:
        names = sorted([n for n in SCENARIOS if n not in FIRST], key=lambda n: WORLD_KIND[n] != 'flat')
    if not names or names == ['list']:
        print('scenarios:', ', '.join(SCENARIOS), '   (also: all, new)')
        sys.exit(0)
    ok = True
    prune_scenario_worlds()
    sessions = []              # one session (= one brand-new world) per world kind needed; scenarios run in the order given
    for n in names:
        if sessions and sessions[-1][0] == WORLD_KIND[n]:
            sessions[-1][1].append(n)
        else:
            sessions.append((WORLD_KIND[n], [n]))
    for kind, group in sessions:
        world = 'Scn_%s_%s' % (kind, time.strftime('%H%M%S'))
        print('== session: %s world %s, scenarios %s' % (kind, world, ', '.join(group)))
        g0 = Game('session', minutes=30)
        try:
            new_world(g0, world, kind)
        except desk.Abort as e:
            g0.check(False, 'aborted while creating the world: %s' % e)
            g0.finish()
            sys.exit(1)
        aborted = False
        for n in group:
            print('-- %s' % n)
            g = Game(n)
            try:
                SCENARIOS[n](g)
            except desk.Abort as e:
                g.check(False, 'aborted: %s' % e)
                aborted = True
            except Exception as e:
                g.check(False, 'scenario error: %r' % e)
            ok = g.finish() and ok
            if aborted:
                break          # an abort ends the whole run; the world is left for inspection (Scn_*)
        if aborted:
            sys.exit(1)
        leave_world(g0, world)
    sys.exit(0 if ok else 1)
