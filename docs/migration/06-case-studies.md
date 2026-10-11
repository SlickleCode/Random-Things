# 06 — Case studies

The long bugs, told end to end. Each lists the *sequence of beliefs* because the wrong turns are the lesson.

---

## 1. Super Lubricent Boots — the Mixin that did nothing (and the project it killed)

**Goal:** boots that make every surface frictionless unless sneaking.

1. First port put "boots negate slipperiness" inside the three custom Super Lubricent blocks. Tester: *every
   surface is slippery with the boots on — backwards*. (Boots were supposed to make everything slippery; the code
   made the three special blocks *not* slippery when worn. A per-block override cannot affect other blocks.)
2. Ground truth: the original did it in an ASM patch on the entity's movement method (`slipFix`). No event exists.
3. Wrote a Mixin redirecting the single `getSlipperiness` call in `LivingEntity.travel`. Compiled. Tester: *no
   change in movement.* Logged as an open issue.
4. Static review twice: "correct". Then the **runtime** was inspected (client run with Mixin debug flags):
   - dev client mod has no manifest → manifest-driven Mixin config never registered in dev;
   - both mixins used `remap = false` with dev names → in a production jar the target method name doesn't exist, so
     the injection silently no-ops; with remap enabled the refmap filled in the real SRG name;
   - ultimately: the Mixin runtime cannot bootstrap from a mod jar on this Forge at all.
5. Converted both mixins to coremods; removed all Mixin build infrastructure. Verified offline with a Nashorn+ASM
   harness and by booting. Tester later confirmed the boots work.

**Lessons:** the behavior's subject decides the hook location; prove a patching pipeline works before building on
it; after two failed code reviews get runtime evidence; "compiles clean" meant nothing for annotation-driven bytecode
injection.

---

## 2. Tile entity live sync — six blocks that only updated after relogging

**Symptom:** Rune Base patterns, Biome Radar state, Fluid Display fluid, Redstone Observer target, Sound Dampener
slots, interface targets appeared only after rejoining.

1. Early fix for Redstone Observer: corrected `getUpdateTag()` (default omitted subclass fields) and added
   `notifyBlockUpdate`. Believed done. Also believed integrated-server tile entities are shared with the client
   (wrong).
2. Later, Rune Base live placement still failed. Reading the loader's decompiled sources: the live-update callback
   `onDataPacket` is an **empty default** on the extension interface; only the initial-load path
   (`handleUpdateTag`) read data by default → relog always worked, live never did.
3. Fixed all six with the same override; owner confirmed immediately.

**Lessons:** default methods can be intentional no-ops; the relog path masks the live path; "singleplayer shares the
object" is false; after fixing one instance grep for siblings.

---

## 3. Spectre Illuminator — four bugs in a row (lighting a whole chunk)

Feature: an orb that lights its chunk to full brightness.

1. No visual, no light. Cause: `illuminated` was a plain field, not synced; the client never learned the state, and
   the client also needs to re-run the light update itself. Fixed with a synced data parameter + relight on both sides.
2. Crash in the *loader's light engine* (`ArrayIndexOutOfBoundsException` deep in fastutil maps). Cause: a faithful
   port of the original's synchronous 83,000-position sweep; the new engine schedules into bounded structures.
   Fixed by bounding the height range and draining a queue 256/tick.
3. Still nothing visible. Rather than guess a fourth time, added tagged diagnostics at each decision point and
   asked for a log from a *fresh chunk* (stale leftover entities could silently refuse placement).
4. The log was unambiguous: the server queue drained, the client queue never did. In singleplayer client and server
   worlds are separate objects, and the world-tick event doesn't fire for the client world. Fixed by draining the
   client queue from the client tick event.
5. Confirmed lit. Then a fourth bug: after pickup the original orb's edge stayed lit (neighbor padding of 15 needed),
   plus a visible-billboard feature request.
6. Related: `TimeAcceleratorEntity` crashed the client the moment it synced — the same *class* of bug (a field set only
   by a secondary constructor is null on the client).

**Lessons:** the original's cost model may not apply (mass updates); two separate worlds in singleplayer; client tick vs
world tick; instrument instead of guessing; name the bug class and audit for siblings.

---

## 4. Fluid Display — a renderer that was wrong in five different ways

Symptoms came one at a time from screenshots: twisted cube and near-black fluid → still transparent → top face
invisible → empty block fully invisible → neighbor block showing a hole.

Causes, in order: one quad had a corner on the wrong axis; lighting enabled by the dispatcher darkened untextured
vertices; top/bottom faces had inward winding (culled); leftover GL current color (near-zero alpha) multiplied the
vertex colors; the empty state drew nothing (the original drew a tank sprite); the block's default solid render layer
made neighbors cull their faces.

**Lessons:** hand-written geometry needs a per-face invariant check; TESR GL state must be reset explicitly; test the
empty state; block culling depends on the render layer, not the render type; each screenshot-driven fix was correct but
incomplete — list all states of a renderer up front.

---

## 5. Lapis / Quartz Lamp and Slime Cube — spawn control

1. Original used an ASM patch; first port used `CheckSpawn`. Lamps stopped blocking spawns after an experiment with a
   light-value trick; Slime Cube couldn't ALLOW anything.
2. Bytecode tracing showed the vanilla monster predicate rejected the spawn ~165 instructions *before* the event fired:
   the event can only DENY.
3. Found that **all** per-mob predicates funnel through one dispatch method. One coremod there fixed ALLOW for
   everything, later reused by Peace Candle.
4. A Peaceful-difficulty bug: forcing ALLOW spawned monsters that vanilla would despawn; fixed by guarding Peaceful.

**Lessons:** read exact event timing; search for the shared choke point; test the difficulty/edge settings.

---

## 6. Super Lubricent speed cap — the collision event that never fired

A speed cap implemented in `onEntityCollision` never ran: a player standing on a block isn't colliding with it.
Replaced with a living-update listener reading the block underfoot with vanilla's own formula (`minY - 1.0`). Then a boat
on the block accelerated until the game crashed — boats aren't `LivingEntity`; added a boat cap (turn rate and speed).

**Lessons:** read how vanilla itself finds the block underfoot; cover every entity class that can be on the block.

---

## 7. Magnetic Enchantment / Block Breaker — the event that is declared but never posted

1. Port used `BlockEvent.HarvestDropsEvent` (comment: "no coremod needed"); compiled, registered, untested (the code was
   written in an environment without the loader available).
2. Checklist row #288 stayed blank (untested) for over a week. The Block Breaker used the same enchantment on its fake player, so its drops
   fell on the ground; the owner had marked only mining/redstone rows as pass.
3. The automated behavior test "Block Breaker mines the front block into the rear inventory" failed: block mined, nothing
   in the chest, a loose item on the ground. Searching the sources jar showed **no caller of the event's fire method** in
   the whole game.
4. Replaced with a global loot modifier (sees every loot roll with the harvesting entity and the tool); the test
   then passed.

**Lessons:** "exists" ≠ "fires"; a test that crosses the machine → inventory boundary found what rows testing the
machine alone could not; blank checklist rows are risk, not neutrality.

---

## 8. Mixed bag of one-liners with big payoffs

- **Spectre dimension crash on rejoin:** registered at mod load; the registry is rebuilt per world load → register at
  the world/server start event.
- **Block Breaker NPE:** fake player has no connection; assign a no-op net handler.
- **Time in a Bottle:** client crash from a null target → synced data parameter.
- **Thrown eggs invisible:** missing spawn packet.
- **Portkey priming glow never stopped:** in-place mutation of a synced `ItemStack` tag; mutate a copy.
- **Portkey camo off-center:** item render path double-translates; cancel the outer offset.
- **Magic Hood particles:** the "ambient" flag doesn't hide particles; zero the synced particle color.
- **Sound Recorder list off-screen:** default row width 220 and no clipping.
- **Rain Shield "doesn't work":** the report concerned rain particles; mechanical effect already fine → separate visual coremod.
- **Spectre cube lookup:** the original compared chunk and block coordinates (off by 16); porting faithfully would have
  preserved the bug.

## 9. The Redstone Interface — a feature that "worked" for a week and never did

1. Ported with two coremods (power queries) and a `NeighborNotifyEvent` listener to refresh the interface's sensed power.
   Build clean, boot clean, `DONE-UNTESTED`.
2. A behavior test built the obvious contraption (redstone block next to the interface, lamp at the bound target).
   The lamp never lit; a diagnostic message showed the interface's stored power was 0 while the world reported 15
   next to it, so the coremods were fine and the *refresh* never ran.
3. The event's position is the changed block, the listener compared it with the interface's own position, so the refresh
   fired only when the interface itself changed. The original used the block's own `neighborChanged`.
4. Fix: `neighborChanged` and `onBlockAdded` on both interface blocks; the dead listener removed. Test passes.

**Lessons:** two verified coremods and a clean boot proved nothing about the feature; a contraption-level test did. Add a
one-line semantic note per event ("position = ...") when you first use it.

## 10. GUI layout drift — eleven screens with invented sizes

A hands-free "open every GUI, screenshot it" pass (after a tester reported several overlapping screens) showed eleven of
26 GUIs re-laid-out with sizes that did not match their textures. Fixes were mechanical: restore the original panel
size, slot coordinates and sprite buttons from the original class and PNG. The same pass found a stale tile entity that
made the next block placed at that position open the wrong GUI, and led to the `/rtdebug` command family and the
auto-test. Scrollbars, clipping and drag forwarding (G8) were found one screenshot at a time afterwards.

**Lesson:** automate "open every screen" early; layout bugs are cheap to find with screenshots and expensive to find
by hand-playing.
