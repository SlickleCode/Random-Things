# Port "Random Things" from 1.12.2 to Forge 1.14.4

This is Claude's running engineering log for the port, moved into the repo (2026-09-28) from an
external plan file so it travels with the codebase. It's written session-by-session as work
happens - "this session"/"the user" below refer to whoever's working with Claude at the time, not
necessarily the repo owner personally. See `CLAUDE.md` for the condensed rules a new session should
follow; this file is the detailed history and the "what's actually true right now" state - read the
**START HERE** section below first in any new session.

## START HERE (session handoff)

**Update, 2026-09-27:** pulled `origin/1.14.4` (3 commits behind locally at session start). A separate
session/container had already shipped the Worldgen batch — Ancient Furnace + Peace Candle, the only two
wiki-tagged worldgen features, commit `3e60013` — plus two LICENSE commits. That container didn't have
this plan file, so its own commit message has the full reasoning (see "Worldgen batch" progress-log
entry added below, and the roadmap/Batch-7 corrections above). **Worldgen (roadmap item 4) is now done**,
both features shipped with disclosed environment-level gaps (no runtime biome-reassignment API for
Ancient Furnace; no village-jigsaw hook point for Peace Candle's church spawn — corrected from this
plan's earlier assumption that the latter was just another Batch 7 coremod candidate, it isn't).

Pulling in that merge broke the build: `PeaceCandleTileEntity.java` was missing its
`net.minecraft.tileentity.TileEntity` import entirely (didn't compile in the container that wrote it
either, or wasn't caught before commit) — 9 compiler errors, all from that one missing import. Fixed by
adding the import; `./gradlew build -x test` is clean again. **This fix is currently uncommitted** —
standing instruction is to wait for an explicit commit request.

No FAIL rows in `TESTING_CHECKLIST.md` are new since last session (checked). Note: rows #75/#93/#100/
#102/#114 still show old `FAIL` text even though the plan's "Bug-fix pass 4" progress-log entry
(2026-09-25) documents real fixes for all of them — those checklist rows were apparently never updated
to reflect the fix the way #80/#81 were. Not re-investigated this session (out of scope for the worldgen
pull); worth reconciling next time bug-fixing resumes, or just asking the user to retest those five rows
on a fresh build.

**Update, 2026-09-27 (same session, later): a real bug-fixing + worldgen-expansion pass followed**, driven
by the user actually playtesting the freshly-pulled worldgen batch plus other recent features. Full chain,
in order (see each one's own progress-log entry above for detail): Super Lubricent Boots got a speed cap
matching the blocks' own cap; Lapis/Quartz Lamp's Peaceful-mode bug went through *two* mechanism changes
in one day (force-ALLOW/DENY → a 1.12.2-faithful `getLightValue` trick → back to force-ALLOW/DENY with an
explicit `Difficulty.PEACEFUL` guard, since the light trick proved unreliable in this Forge version's
threading model — **confirmed working** by the user); Rune Base went through three real rendering/sync
bugs (GL color4f reset, GL_LIGHTING darkening, and the big one — a systemic `TileEntity#onDataPacket`
no-op-by-default bug that silently broke *live* sync, found while chasing this and fixed across all 5
affected TEs project-wide) plus a requested left-click-clears-everything behavior change — **confirmed
working**; Fluid Display got a folded-bottom-face fix, a lighting fix, then a follow-up fixing *actually*
backwards top/bottom-face winding (culling) and adding real per-face diffuse brightness (not yet
re-confirmed by the user). Then, asked what tooling the two remaining worldgen gaps needed: **Ancient
Furnace's "impossible" biome reassignment turned out to be wrong** — implemented for real (`Chunk
.getBiomes()` returns the live array, no reflection needed) — and **Special Chest (previously missing its
ocean-monument spawn entirely) got a new coremod**, since unlike Peace Candle's village case, ocean
monuments are still the old hardcoded-piece system in 1.14.4. Peace Candle's village-church case remains
the one real, still-open tooling gap (needs interactive Structure Block access this environment doesn't
have - see the tooling-answer note in that section above).

**Update, 2026-09-27 (same session, later still): user decided to stop migrating three undocumented
bonus features** — Plate family (11 blocks), Special Chest (including the ocean-monument coremod just
added this session), and Sakanade — none appear in any wiki page. All deleted entirely (blocks, TEs,
containers, screens, the now-orphaned `OceanMonumentTransformer` coremod, and every asset/lang/checklist
reference); see "Plate family, Special Chest, and Sakanade removed entirely" progress-log entry below
for the full file list. Build verified clean after. Checklist row #271 (never tested) and rows #100-103
(previously FAILing on a chest-opening crash) are now moot rather than open bugs.

**Update, 2026-09-27 (same session, later still): Spectre Illuminator implemented** - user picked it
from 4 offered NOT-STARTED candidates. Needed a real structural fix, not just a port: 1.12.2's ASM patch
targeted a `Block.getLightValue` overload that no longer exists in this Forge version at all (dropped
`world`/`pos` params entirely) - ground-truthed the actual 1.14.4 light-computation call site instead
(`IBlockReader.getLightValue(BlockPos)`'s default method, one shared choke point) and wrote a new
`IBlockReaderTransformer` coremod for it. See "Spectre Illuminator implemented" progress-log entry below
for the full story (three disclosed simplifications, the bytecode-verification harness getting
generalized to take any target class, and the `runClient` verification). Build clean, checklist #272-274
added, not yet tested in-game.

**Next scoped-and-ready work:** every wiki-tagged worldgen feature is done, and the two disclosed gaps
from this batch are now down to just Peace Candle's village generation (genuinely blocked on tooling).
Remaining NOT-STARTED wiki features still need either an enchantment-system prerequisite, a generic
runtime block-model renderer, a new falling-entity type, or kicking off the Redstone Interface family's
own shared wireless-signal infrastructure (flagged as needing its own multi-slice breakdown, not a single
pass) - see `WIKI_FEATURE_STATUS.md`'s NOT STARTED rows. Otherwise: a growing pile of DONE-UNTESTED/
NEEDS-RETEST rows from this session alone (#95, #150-152, #158, #234, #235-236, #272-274, plus the five
stale rows noted below) are waiting on the user's own playtesting - resume there, or ask which
infrastructure prerequisite / NOT-STARTED feature to tackle next.

**Update, 2026-09-28: bug-fixing pass over a fresh round of user playtesting.** User asked to triage
`TESTING_CHECKLIST.md` and work through the open problems. Full list and outcome (each row's own
checklist entry has the full technical writeup):
- **Real bugs found and fixed:** Ender Bucket/Reinforced Ender Bucket fluid pickup (#219/#223 - this
  Forge build's own `FluidUtil.tryPickUpFluid` never actually handles a plain fluid block, ground-truthed
  via `javap -c`; rewrote to drain directly against `IFluidBlock`/`IBucketPickupHandler`); Rain Shield's
  missing support-break (#257 - was missing the `checkForDrop`-equivalent call in `neighborChanged` that
  Peace Candle already had right); Basic/Advanced Redstone Interface not craftable/not in creative menu
  at all (#283 - simply missing from `ModItems`'s `registerItemForBlock` calls, the only two blocks in
  the whole list skipped); Magic Hood's potion-particle hiding lagging up to 30s when the hood is put on
  mid-effect (#254 - `LivingEntity`'s private `potionsNeedUpdate` flag doesn't get set by an armor change,
  only by an effect change; new `LivingEquipmentChangeEvent` listener flips it via one narrowly-scoped
  reflective set, new `util/PotionMetadataUtil.java`); Magic Hood's "indestructible" claim was actually
  wrong (#255 - `ArmorItem`'s constructor unconditionally sets max damage from the material regardless of
  what's overridden; gave it real durability matching Leather's via a small delegating `IArmorMaterial`
  that copies every other stat from Chain, per user request); Portkey's camo'd/plain icon both rendering
  oversized/offset (#279/#280 - `portkey.json` had no `display` transforms block at all, the actual source
  of all GUI/hand/ground scaling for a `builtin/entity` item in this version - ground-truthed against
  vanilla's real `item/template_shulker_box.json`); Ender Letter's receiver field never saving (#208 - was
  only ever sent from `Screen#removed()`, racing the vanilla close-window packet over the same connection
  and frequently losing; now syncs on every text change instead). Floo teleport flame height raised
  (#189) and Global Chat Detector's op-bypass removed entirely (#117), both simple requested changes.
- **Investigated, no bug found (flagged in the checklist, needs a more specific retest):** Rain Shield's
  mechanical rain-suppression (#258 - re-ran the actual coremod bytecode via a throwaway Nashorn+ASM
  harness, confirmed all 4 `ireturn` sites correctly redirect, and confirmed `FarmlandBlock`/`FireBlock`/
  `ServerWorld` all call the patched method directly); Redstone Remote's edit-GUI slots (#287 - container/
  screen code is identical to the untested Advanced Interface's own, which was itself blocked by #283
  until now); Portkey's glow-after-priming (#277 - ground-truthed 1.12.2's real `hasEffect`: an *unbound*
  Portkey glows forever by design, only a *bound* one stops - likely tested unbound). Weather Egg/Golden
  Egg's missing thrown sprite (#205/#230) stayed unresolved - both are near-verbatim vanilla
  `ProjectileItemEntity`+`SpriteRenderer` usage, no code difference found from vanilla's own working
  thrown egg; possibly a sync-timing issue specific to fast/short-lived projectiles, not confirmed.
- Full `./gradlew build -x test` clean after all fixes. Checklist rows updated in place with each fix's
  root cause. **User asked to commit and push** - all of the above, plus every other uncommitted feature
  bundled in the working tree at the time (Plate family/Special Chest/Sakanade removal, Spectre
  Illuminator, Portkey + camo, Spectre Anchor, the Redstone Wireless cluster), landed on `1.14.4` as one
  commit (`32e6f01`) and pushed to `origin/1.14.4`.

**Update, 2026-09-28 (same day, later): this plan file and the project's memory notes moved into the
repo.** User asked for Claude's plan/memories to live in repo-specific files instead of Claude Code's
external (machine-local, not git-tracked) plan/memory storage. This file is that plan, copied over
verbatim; a new `CLAUDE.md` at the repo root now carries the condensed workflow rules (javap-first
verification, match 1.12.2 unless told otherwise, never commit/push without an explicit ask) that
previously lived only in Claude's external memory. The external memory/plan files still exist but now
just point here rather than duplicating content.

**Update, 2026-09-28 (new session, cloud sandbox): continued the migration - Magnetic Enchantment,
Block Breaker, Block Destabilizer.** This session started in a fresh cloud container with **no**
Forge/Mojang Maven access at all (unlike the usual local dev machine) - `./gradlew build` fails
immediately trying to resolve `ForgeGradle` itself (403 from `files.minecraftforge.net` through this
sandbox's network policy), and there's no cached mapped jar to `javap` either. That means this
session's own working rule (ground-truth every uncertain API against the real jar before writing code)
could not be followed - everything below is written from strong prior knowledge of stable, long-lived
Forge 1.14.4 APIs and cross-checked against this codebase's own already-proven usages wherever
possible, but **none of it has been compiled or run**. Flagged per-row in `TESTING_CHECKLIST.md`
(#288-296) and in `WIKI_FEATURE_STATUS.md`'s own note - please build and retest before trusting it.

First found and pulled in a fair amount of work from three separate branches
(`claude/worldgen-continuation-r6u8yj` fast-forwarded into `1.14.4` locally, then `origin/1.14.4` had
already moved further ahead with the Redstone Wireless cluster/Portkey camo/Spectre Anchor batch and
this plan file's own move into the repo) - all fast-forward, no conflicts, nothing lost.

Then picked two NOT-STARTED wiki features that shared a prerequisite (a `magnetic` enchantment
package, `enchantment/` - didn't exist in this port yet) and did both in one pass:
- **Magnetic Enchantment**: a plain `DIGGER`-type `Enchantment` plus a `BlockEvent.HarvestDropsEvent`
  listener that redirects a Magnetic harvester's drops straight into their inventory. Turns out this
  needs **no coremod at all** in 1.14.4 (contrary to the wiki's own "ASM?" flag, which describes
  1.12.2's need) - canceling that event already clears its own drop list before anything spawns.
- **Block Breaker**: the FakePlayer-driven continuous-mining machine. Reuses the Magnetic listener
  above instead of porting 1.12.2's own separate drop-catching path, since its FakePlayer's pickaxe is
  itself Magnetic-enchanted. The riskiest unverified surface in this session: `FakePlayerFactory`,
  `PlayerInteractionManager#tryHarvestBlock`, `BlockState#getPlayerRelativeBlockHardness` - all used
  with high confidence but zero ability to confirm this build's exact method signatures.
- **Block Destabilizer**: the BFS-flood-fill demolition machine. Real finding here: 1.12.2's own
  `EntityFallingBlockSpecial` (listed on the wiki as "needs a new falling-entity type") turns out to
  have been a near line-for-line copy of vanilla's own falling-block entity, apparently written only to
  expose a public `shouldDropItem` field - which is already public and mutable on this Forge version's
  real `net.minecraft.entity.item.FallingBlockEntity`. Spawns that directly; no new entity class needed
  at all. Disclosed simplifications: plain text Lazy/Fuzzy toggle buttons instead of 1.12.2's custom
  icon-toggle widget (reusing the real 1.12.2 GUI background texture at its original 85x35 size), and
  the always-on glow overlay dropped rather than guessing at a `VertexLighterFlat`-style tint blind.

Both blocks' real 1.12.2 crafting recipes, block/item models, and textures were pulled directly from
the `origin/1.12.2` branch (`git show origin/1.12.2:<path>`) rather than reinvented, so those parts are
exact, verified-against-source ports, not guesses - only the Java logic and the two new GUI/asset files
(Block Destabilizer's screen text, both blocks' 1.14.4-style blockstate/model JSON restructuring) are
new-and-unverified.

**Next scoped-and-ready work, if the network/build situation doesn't improve this session:** the
remaining NOT-STARTED wiki features left after this batch (Diaphanous Blocks, Light Redirector, Dyeing
Machine, Magnetic Enchantment's own now-done, Spectre energy network/Spectre Key/Spectre Tools) all
need either a generic runtime block-model renderer, a brand-new render-time item-recolor coremod, or a
whole new energy-network/custom-dimension subsystem - all considerably higher-risk to ship blind than
this batch was. Recommend pausing new ASM/coremod-dependent or custom-dimension work until a session
has real build access again, and using that time instead on anything not requiring compilation
verification (further disclosure/documentation passes, asset-only ports, etc.) if the network policy
doesn't change.

**Repo:** `D:\Projects\Clauding\Random-Things`, fork at
[SlickleCode/Random-Things](https://github.com/SlickleCode/Random-Things). Branches: `main` is a
landing-page-only README (no code); `1.14.4` is where all active port work lives (checked out by
default); `master` preserves the original unmodified 1.12.2-era source as a reference/backup.

**State as of 2026-09-26 (end of session):** Batch 1 and Batch 2 are functionally complete; Batch 3 is in
progress. Since the Mixin/boots investigation below (commit `fee7645`), later sessions have also shipped
Batch 3 slices 3-5 (Compass/Escape Rope/Chunk Analyzer/BiomeRadar, commit `13b54f6`), the Sound-pattern
family, a Chunk Analyzer GUI/scan-bug fix, "Bug-fix pass 4" (worked `TESTING_CHECKLIST.md` top to bottom),
and the full Imbuing/Rune deferred-subsystem batch (Rune Base half, then Imbuing Station half - see that
section near the end of this file). **All of this work from the Sound-pattern family onward is still
UNCOMMITTED** - only commit/push when the user explicitly asks. The rest of this "START HERE" section
below is preserved as-written from the Mixin/boots investigation session; treat its "Next step" paragraph
as historical, superseded by the paragraph right after it.

This was a very long session on the Mixin/boots bug specifically - full
chain of findings, in order, most important last:
1. Fixed `TESTING_CHECKLIST.md` #76/#83/#95 (see "Testing-checklist pass, 2026-09-24 (later)").
2. Found and fixed a real `remap = false` bug in the boots Mixin's own code (see "Mixin root cause found,
   2026-09-25").
3. Discovered the dev client (`runClient`) structurally cannot load Mixins at all in general - real, but
   turned out to be secondary (see "Mixins cannot be tested in this dev environment at all, 2026-09-25").
4. **The actual deepest root cause: this project's `build.gradle` never shipped Mixin's own
   runtime/bootstrap library anywhere, in any environment, since the very first Mixin was added** (see
   "The real root cause: Mixin's runtime library was never actually shipped anywhere, 2026-09-25").
5. Fixed the dev client's crash too by bumping the project's Forge dependency to `28.2.26` (see "Dev
   client Mixin support, partially restored, 2026-09-25") - kept, this is a genuine improvement.
6. **Shaded Mixin's runtime into the built jar and had the user test it in their real Minecraft install -
   this revealed a further, structural limitation: Mixin needs an `ILaunchPluginService` registered from
   an early ModLauncher classpath scan that happens *before* the `mods` folder is even scanned, so a
   mod's own jar can never satisfy it, only the separate `ITransformationService` half. This is a hard
   architectural constraint (confirmed via Mixin's own source), not a bug we can fix in our code** (see
   "Mixin's launch-plugin half cannot come from a mods-folder jar, 2026-09-25" below).
7. **User explicitly chose to pause here rather than pursue a risky, unverified PrismLauncher
   instance-file edit.** Reverted the runtime shading (kept the `implementation` dependency
   *commented out*, with a detailed inline comment explaining why) so the shipped jar launches cleanly
   again, matching the state that worked in the user's first two real-environment tests. Sent that
   restored jar to the user.
8. **User then asked the forward-looking question that should drive next steps: can Mixins ever be fully
   self-contained in one drop-in jar for end users, with no separate install steps?** Answer given:
   **no, not via SpongePowered Mixin on this Forge version** (same launch-plugin constraint from point
   6) - **but yes via coremods** (Forge's own JS-based ASM transformer system, `META-INF/coremods.json`),
   which this project already uses successfully and self-containedly for `transformer/VertexLighterFlat.js`.
   **The recommended path forward, not yet started: convert `FireBlockMixin` and `SuperLubricentBootsMixin`
   to coremods instead of SpongePowered Mixin**, eliminating the whole class of problem rather than
   working around it. This is real, not-yet-done work - see the "Coremod conversion" note below for what
   it'd involve.

9. Batch 3 Slice 3: Golden/Emerald Compass (user's choice over the `EntitySoul` alternative) - done, see
   "Slice 3 (done), 2026-09-25". Both compasses' actual "set a target" logic turned out to live in custom
   crafting recipes (new `PositionFilterItem` + the already-existing `IdCardItem` from Batch 2), not the
   item classes - ported the whole working chain. First custom `SpecialRecipe`/`IRecipeSerializer` in
   this port, new `recipes/` package. **Tested in-game: 5/6 pass** (both compasses' wobble/bind/track
   behaviors, plus the corner-vs-center fix). **One real bug found and fixed: `PositionFilterItem` was
   missing its item model/texture entirely** (a genuine oversight, not created in the original slice) -
   fixed, see "Slice 3 in-game results" in the progress log. Its crafting recipe is separately flagged
   `NEEDS RETEST` (#136) - looks correct in the JSON, probably a Magenta-vs-Purple-dye mixup on the
   user's end, not re-derived without confirmation.
10. Batch 3 Slice 4: Escape Rope - done, see "Slice 4 (done), 2026-09-25". Hold right-click under open
    sky's reach to flood-fill search for the nearest daylight and teleport there. New `util/
    EscapeRopeHandler.java` (near-verbatim port of the original's search algorithm) plus
    `EscapeRopeItem.java`. Caught two of my own porting mistakes by comparing line-by-line against the
    original before calling it done (a missing `iterator.remove()` that would have leaked finished tasks
    forever, and a `playSound` call that should exclude the traveling player but didn't) - worth noting
    as a reminder that "compiles and looks right" isn't the same as "matches the original," especially
    for handler classes ported by restructuring rather than copying near-verbatim. Not yet tested in-game
    (`TESTING_CHECKLIST.md` #142-144).

Nothing has been committed yet this session (per standing instruction to wait for an explicit commit
request) - the working tree currently reflects: the #76/#83/#95 fixes, the boots' `remap` code fix (still
correct, currently inert - can't apply without a working Mixin bootstrap), the Forge 28.2.26 bump (kept,
beneficial), the Mixin shading *disabled* (commented out, not deleted - it's the right building block
for a future working setup, whether that's the coremod path or a properly-configured launcher library),
and the new Golden/Emerald Compass + Escape Rope slices (points 9-10 above, unrelated to any Mixin work -
plain items/recipes, no Mixin involved at all).

**Open items as of this update:** (1) Batch 3 Slice 4 (Escape Rope) needs an in-game test -
`TESTING_CHECKLIST.md` #142-144. (2) Slice 3's Position Filter recipe (#136) needs a specific retest with
`minecraft:purple_dye` - the texture half is already fixed. (3) The Mixin-to-coremod migration is scoped
as its own roadmap item - **Batch 7, deliberately last** (see the "Batch 7 — coremod-dependent features"
section near the end of this plan for full scope, since expanded well beyond just this) - not urgent, do the rest of the roadmap first unless
the user asks otherwise. (4) The two `INVESTIGATING` texture rows (#76, #83) still need a retest on a
fresh build - unrelated to any
of the above, still open. No other untouched `FAIL` rows remain.

**Testing loop:** `TESTING_CHECKLIST.md` (repo root, `1.14.4` branch) is the live testing tracker - a
numbered table of every shippable feature
with Pass/Fail/Feedback columns the user fills in after playing. When starting a session, check that
file for any new `FAIL` rows first; that's almost always the next work, ahead of starting a fresh
batch/slice. Each investigated bug gets `FIXED (round N)` or `NOT A BUG` written back into its row with
the root cause, not just a checkbox flip - keep that convention.

**Toolchain:** `source env/activate.sh` (JDK 8) then `./gradlew build` - see "Toolchain note"/"Toolchain
upgrade" entries in the Batch 1 progress log below for why this specific combination is required. A full
build (not just `compileJava`) is expected to succeed before calling any slice done.

**Working style established across this whole project, keep following it:** small self-contained
slices, `javap` against the real mapped Forge jar (path in "Verification" section below) to ground-truth
every uncertain API before writing code that uses it, real 1.12.2 textures/models copied via
`git show origin/1.12.2:<path>` never redrawn, disclosed simplifications stated plainly in-line (both in
commit-adjacent progress-log entries here and, where relevant, as a class-level javadoc in the code
itself), and no scope creep into deferred subsystems (Spectre energy, Ender network, Floo network,
wireless Redstone Interface, Rain Shield, Peace Candle worldgen, worldgen in general, potions/
enchantments, most recipes - see the "Batch 2 is now functionally complete" and "Batch 3" survey
sections below for the full current deferred list with reasons).

**Next step (superseded - see the actual current one below):** ~~no untouched `FAIL` rows remain. ...
Otherwise resume Batch 3 (see its section below for the next scoped-and-ready candidate, Obsidian
Skull/Rez Stone's `EntitySoul` mini-subsystem).~~ #134 (boots) and #69/#70 (Blazing Fire catch-chance) are
still paused pending the Batch 7 coremod conversion - don't re-attempt Mixin-side fixes without new
information.

**Actual next step as of 2026-09-26:** the Imbuing/Rune deferred-subsystem batch is now fully done (both
halves) but entirely untested in-game - `TESTING_CHECKLIST.md` #161-179. The Floo Teleportation slice
shipped after that (#180-192), also untested. Everything from the Sound-pattern family onward this
session is uncommitted.

**Major replanning pass, 2026-09-26 (same day, later):** the user handed over the mod's public wiki,
exported as 100 feature JSON files, and made it this port's authoritative scope document (see "Wiki as
authoritative scope" section below and `WIKI_FEATURE_STATUS.md` at the repo root for the full
per-feature table). Consequences: (1) a handful of undocumented-but-already-shipped features were kept
as deliberate exceptions (Plate family, Special Chest, Sakanade, Bottle of Air - all small and already
working); (2) **Blood Rose was removed entirely** (new content added directly on the 1.14.4 branch,
not 1.12.2 tech debt, not on the wiki either - didn't fit any keep-bucket); (3) a full audit of
`ClassTransformer.java`'s 17 ASM patches was done, correcting two mistaken assumptions from the
project's very first planning pass (Slime Cube's spawn-ALLOW problem needing a coremod was already true
in 1.12.2 itself, not a new 1.14.4 gap; the `Block.getLightValue` patch belongs to Spectre Illuminator,
not the lamps) - see "Corrected & consolidated ASM/coremod batch" below; (4) all ASM-derived,
not-yet-yielding-to-a-clean-event features were consolidated into Batch 7 (expanded scope, still last)
instead of being threaded piecemeal into whichever batch happens to own each feature.

**Update, 2026-09-26 (later same day):** two real `runClient` crashes were found and fixed (see "Post-slice
bug hunt" section) - an `@ObjectHolder`-scanned non-registry field crash and a fluid-capability-not-yet-
registered NPE, both verified fixed via a real client boot. Then a fourth wiki-scoped slice shipped:
Summoning Pendulum, Golden Egg/Golden Chicken, and Fluid Display (see "Fourth wiki-scoped slice" section) -
`TESTING_CHECKLIST.md` #225-237, not yet tested in-game. `WIKI_FEATURE_STATUS.md` is now current (it had
gone stale on five already-shipped features from earlier this session).

No slice is in progress. Next scoped-and-ready candidate is whatever the user picks via the usual
"let's do another slice" flow (now scoped strictly against `WIKI_FEATURE_STATUS.md`'s NOT STARTED
rows), or resuming an in-game testing/bug-fixing pass over the untested rows already shipped.

**Update, 2026-09-26 (later still):** a fifth and sixth wiki-scoped slice shipped after that (Artificial
End Portal + Creative Player Interface; then Ender Bridge/Ender Anchor/Prismarine Ender Bridge, with Item
Filter deliberately skipped as an orphaned-item scope surprise flagged to the user) - see those sections
below. That exhausted every remaining no-ASM NOT-STARTED wiki feature; everything left needs either
Mixin/coremod work, an enchantment system that doesn't exist yet, or touches the deferred Spectre
subsystem. User then asked to start Batch 7. **Batch 7's original scope (Mixin→coremod conversion) is
now DONE** - see that section for the full writeup: both Mixins converted to coremod transformers,
verified via a real `runClient` (both new coremods load, both target classes transform with no
`VerifyError`/`LinkageError`), and all SpongePowered Mixin infrastructure removed from the project
entirely (no more `mixin/` package, no more Mixin Gradle plugin/dependencies). `TESTING_CHECKLIST.md`
#69/#70 and #134/#135 flipped from BLOCKED to "unblocked, needs a fresh in-game test." Batch 7's
*expanded* scope (the new coremod-dependent features: Slime Cube spawn-ALLOW, Peace Candle, Rain Shield,
Redstone Interface family, Magic Hood particle-hiding, Spectre Illuminator, Special Chest worldgen
placement) has NOT been started - the pattern is proven and ready to reuse, but none of that per-feature
work has happened. Next step: pick one of those as its own scoped slice, same as every other batch.

**Update, 2026-09-26 (later still):** Batch 7's expanded scope got its first entry too - Slime Cube
spawn-ALLOW + Lapis Lamp spawn-prevention, both fixed by one new coremod
(`transformer/SpawnPlacementTransformer.js`, redirecting `EntitySpawnPlacementRegistry`'s single shared
spawn-predicate dispatch point - see "Batch 7, slice 2" for the full writeup, including a genuinely useful
discovery: every mob's spawn-placement predicate funnels through that one method, so this same hook covers
any future spawn-control feature too, e.g. Peace Candle's mob-suppression half). Verified via a standalone
bytecode-verification harness (Nashorn `ScriptEngine` + real `CheckClassAdapter.verify`, since this
particular vanilla class isn't touched during a plain `runClient` boot the way slice 1's two classes were)
rather than just the game log. `TESTING_CHECKLIST.md` #29 and #110/#111 flipped from BLOCKED to
"unblocked, needs a fresh in-game test." Then Batch 7, slice 3 (Magic Hood) shipped too - one coremod for
the nametag half (`MagicHoodTransformer.js`, 6 early-exit branches instead of the usual 1), a plain
already-existing Forge event (`PotionColorCalculationEvent`) for the particle half - the exact opposite
split from what this plan's own "consolidated ASM/coremod batch" section guessed (it claimed the nametag
half had a clean event and the particle half needed ASM; fresh `javap` found the reverse for this Forge
version - see "Batch 7, slice 3" for the full note, worth re-verifying that section's other claims before
trusting them for whichever candidate comes next). `TESTING_CHECKLIST.md` #252-255 added.
Then Batch 7, slice 4 (Rain Shield) shipped too - new block/TE, one coremod
(`RainShieldTransformer.js`, redirecting `World.isRainingAt` - 1.14.4 collapsed 1.12.2's two separate ASM
entry points into this single shared method, so it needed even less than 1.12.2 did) covering the
mechanical rain/snow suppression. **Deliberately incomplete this once**: the purely cosmetic
client-side "rain visually still falls near the shield" half was NOT ported - `javap -c` found 1.14.4's
rain-rendering method computes its per-column skip inline rather than through the redirected method, so
fixing it would mean injecting inside a ~300-line rendering loop instead of wrapping a return value like
every other Batch 7 redirect - disclosed in `TESTING_CHECKLIST.md` #261, not silently dropped. Remaining
Batch 7 expanded-scope candidates: Peace Candle, Redstone Interface family, Spectre Illuminator, Special
Chest worldgen placement - still not started.

## Context

`Random Things` (Lumien) was last complete on the `1.12.2` branch (568 Java files: blocks, items,
tile entities, worldgen, GUIs/networking, potions/enchantments, a custom "Spectre" magic subsystem,
and compat integrations with Baubles/JEI/OpenComputers/Thaumcraft/Thermal Expansion). `master` is an
already-started, incomplete port to Forge **1.14.4** (36 files, ~6%) using the modern
`@Mod`/`RegistryEvent`/`ObjectHolder` registration style and the `GenerationStage`/`Feature` worldgen
API. 1.13 never shipped a stable modding API, so 1.14.4 is the correct "next minor version" target,
consistent with what's already on `master`.

The old ASM bytecode-transformer system (17 hand-patched vanilla/Forge classes, see
`origin/1.12.2:src/main/java/lumien/randomthings/asm/ClassTransformer.java`) has already been mostly
removed on `master` — only one harmless value-override helper remains
([AsmHandler.java](src/main/java/lumien/randomthings/asm/AsmHandler.java)), which isn't bytecode
manipulation at all. Per your direction, third-party mod compat is dropped for this port, and we work
package-by-package in checked-in batches rather than attempting the whole mod at once.

**Full scope is ~530 remaining files** — this plan covers the porting approach and defines **Batch 1**
concretely. Later batches (tile entities/GUIs/network, worldgen, potions/enchantments, remaining items)
will each get their own short scoping pass and a check-in, following the same pattern established here,
rather than being fully enumerated now.

## Wiki as authoritative scope (added 2026-09-26)

The user exported the mod's public wiki as 100 feature JSON files (`RT_Wiki_remake/data/features.zip`)
and made it this port's **authoritative scope document**, replacing "port everything in the 1.12.2
source." Full per-feature status (all 100 wiki pages: matched 1.12.2 class(es), current 1.14.4 port
status, whether it needed ASM) now lives in **`WIKI_FEATURE_STATUS.md`** at the repo root — that file
is the reference table going forward; don't duplicate it here.

**Not migrating:** anything in the 1.12.2 codebase with no matching wiki page. Full list (Festival
system, Player Soul/Revive Circle, Rez Stone, Dungeon Chest Generator, Voxel Projector, Nature Core,
third-party mod compat, `RTCommand`, the ASM infra itself) is in `WIKI_FEATURE_STATUS.md`'s "Not
migrating" section. Four undocumented-but-already-shipped exceptions were explicitly kept rather than
ripped out (user's call, asked via `AskUserQuestion`): the **Plate family** (11 blocks, already
extensively tested), **Special Chest**, **Sakanade**, **Bottle of Air** — all small, working, and not
worth the destructive busywork of removal. **Blood Rose** was the one exception removed entirely: it
wasn't 1.12.2 tech debt at all (added fresh directly on the 1.14.4 branch, no 1.12.2 source, no wiki
page), so it didn't fit either bucket — deleted the block/TE/worldgen feature/its VFX framework (which
nothing else used) and all registrations/assets/lang entries; verified with `./gradlew compileJava`
clean afterward.

## Corrected & consolidated ASM/coremod batch (rewritten 2026-09-26)

`ClassTransformer.java` (1.12.2) bytecode-patched exactly **17 vanilla/Forge classes** (verified by
reading its `transform()` dispatcher directly — 17 `transformedName.equals(...)` branches, no more, no
less) plus one dead prototype (`patchLiquidBlock`/`BlockLiquid`, defined but never dispatched — not
real behavior, ignore it). Each patch was traced to its exact feature by reading both
`ClassTransformer.java`'s patch bodies and `handler/AsmHandler.java`'s hook implementations in full
(the previous version of this section, written before Batch 1 started, got two things wrong — corrected
below).

**Correction found this session:** the previous version of this section claimed `EntitySlime
.getCanSpawnHere` → `LivingSpawnEvent.CheckSpawn` was a clean, zero-Mixin replacement for Slime Cube.
It isn't — this was independently rediscovered the hard way while testing Slime Cube/Lapis Lamp this
session (bytecode-traced: `CheckSpawn` fires too late in `WorldEntitySpawner.performNaturalSpawning` to
ever ALLOW an already-rejected spawn, only DENY an allowed one). Reading the ASM source now confirms
1.12.2's own author hit the exact same wall: `EntitySlime` is only *one* of two classes patched for
this feature — `WorldEntitySpawner` itself is *also* directly patched (`modifyValidSpawningChunks`),
which is exactly the level this session concluded a fix has to happen at. Also corrected: the
`Block.getLightValue` patch was mis-attributed to Lapis/Quartz Lamp — reading `AsmHandler
.overrideLightValue`'s body shows it's actually `SpectreIlluminationHandler`-driven (**Spectre
Illuminator**'s dynamic per-position lighting); the lamps themselves never needed ASM at all, just
`Block.Properties.lightValue(15)`.

**Zero Mixin needed — a direct Forge-1.14.4 API/event already covers it (11 of 17 classes):**
- `BlockFalling.canFallThrough` → `Block.canFallThrough(state)` override. **Trigger Glass** — done.
- `Block.getLightValue` → `IForgeBlock.getLightValue` override, dynamic per-position via a handler
  lookup inside it (same shape as the original). **Spectre Illuminator** — not started.
- `Block.addCollisionBoxesToList` (skip a liquid's surface-collision box for certain worn boots) →
  this port already ships a different, deliberately non-ASM design (a per-tick nudge-up event
  listener) for **Water Walking Boots / Obsidian Water Walking Boots / Lava Waders** — already done,
  no further action; just documenting that the original's mechanism was ASM and ours isn't.
- `EntityLivingBase.travel` (friction) → `IForgeBlock.getSlipperiness` override. **Super Lubricent**
  (Ice/Platform) + **Super Lubricent Stone** — done this way already.
- `PlayerInteractionManager.tryHarvestBlock` → `BlockEvent.BreakEvent` + `HarvestDropsEvent`.
  **Magnetic Enchantment** — not started (no enchantment package exists yet either).
- `WorldGenAbstractTree.setDirtAt` → `SaplingGrowTreeEvent`/soil-behavior override. **Fertilized
  Dirt** — done.
- `InventoryPlayer.dropAllItems` → `PlayerDropsEvent`. **Spectre Anchor** — not started.
- `RenderLivingBase.canRenderName` → `RenderNameTagEvent`. **Magic Hood** (nametag half) — not
  started.
- `RenderItem` (enchant-glow recolor) → `IItemColor`/custom `BakedModel` overlay. Covers **Dyeing
  Machine**'s recolor-any-item mechanic plus a purely cosmetic colored-glint detail on **Redstone
  Observer** (red), **Spectre Key** (cyan), **Portkey** (magenta), **Spectre Tools** (white, the
  Spectre Sword), and **Escape Rope** (yellow) — all already-shippable without this, the glint recolor
  is cosmetic-only polish, not core functionality; none of it is built yet even for the DONE features
  in that list.
- `LayerArmorBase` (worn-armor dye + enchant-glow) → same `IItemColor`-family approach, applied to
  armor layers. **Dyeing Machine** — not started.
- `VertexLighterFlat` (fullbright tinted-quad rendering) → per-block light value + `RenderType`, no
  core render patch needed — this port's TESR-based replacement (see **Rune Base**'s slice) is the
  actual pattern used, not a fullbright-quad trick at all. Covers **Stained Bricks**, **Glowing
  Mushrooms**, **Floo Teleportation** (Floo Brick) — all done, all with this exact simplification
  already disclosed in their own slices — plus **Slime Cube** (done, separately from its spawn-ALLOW
  block below), and not-yet-started **Spectre Coils**, **Block Destabilizer**.
- `BlockRendererDispatcher` (swap which block's model renders at a position) → `IModelData`/
  `ModelDataManager` on the redirector's own TE. **Light Redirector** — not started (needs a generic
  runtime block-model renderer, shared need with Diaphanous Blocks, neither started).

**No clean Forge-1.14.4 event/hook exists — needs real bytecode patching (6 of 17 classes). Important
correction while assembling this section: these are NOT "Mixin-blocked."** `SpongePowered Mixin`
specifically cannot self-bootstrap from this mod's own jar on this Forge version (needs an
`ILaunchPluginService` registered before the `mods` folder is even scanned — see Batch 7 below, already
fully diagnosed). But **Forge's own coremod system doesn't have that limitation at all** — it's
discovered through Forge's normal mod-jar scanning, fully self-contained, and **already proven working
in this exact repo**: `src/main/resources/transformer/VertexLighterFlat.js` +
`META-INF/coremods.json` is a real, live coremod already patching `VertexLighterFlat.processQuad` to
call `AsmHandler.modBlockLight` (currently just a placeholder stub gated on a magic `tintIndex == 12340`
that nothing produces yet — proof the *pipeline* works, not yet wired to a real feature). So every
item below needs a **coremod** (the same proven mechanism, not a new one), and none of them are
actually blocked the way Super Lubricent Boots' *Mixin* currently is:
- `EntityLivingBase.updatePotionEffects` (hide potion-particle bubbles from viewers). **Magic Hood**
  (particle half) — not started. Worth one more check before committing to a coremod: 1.14.4 may have
  grown a clean `EntityLivingBase`-side event by 1.14 that 1.12.2 lacked — hasn't been
  `javap`-verified yet, do that first when this slice comes up.
- `EntitySlime.getCanSpawnHere` + `WorldEntitySpawner.modifyValidSpawningChunks` (the corrected finding
  above — both classes, together, for the ALLOW direction). **Slime Cube**'s spawn-permission half —
  currently marked BLOCKED in `TESTING_CHECKLIST.md` (#110-111), but per the correction above that
  status is stale: it's not structurally impossible, it just needs a coremod instead of
  `LivingSpawnEvent.CheckSpawn`. Also affects **Peace Candle**'s mob-suppression half — not started.
- `StructureVillagePieces$Church.addComponentParts` (place a Peace Candle in generated village
  churches) — 1.14.4 predates the jigsaw structure system, no clean non-ASM injection point. **Peace
  Candle** worldgen half — **correction, 2026-09-27: not a coremod job either.** A separate session
  shipped the Worldgen batch (Ancient Furnace + Peace Candle, see progress log) and found 1.14.4's
  village generator is fully data-driven jigsaw/structure-template data with no hardcoded piece class
  left to ASM-patch at all - hooking in a custom building would mean hand-authoring NBT structure
  template data, not writing a transformer. Genuinely deferred (needs real structure-block tooling this
  environment doesn't have), not a Batch 7 candidate. Peace Candle itself (block + mob-suppression via
  the existing `SpawnPlacementTransformer` coremod) shipped and works; only natural village generation
  is missing.
- `World` weak/strong redstone power → wireless signal for **Advanced Redstone Interface**,
  **Redstone Interface**, **Redstone Activator**, **Redstone Remote** (one shared mechanism, four wiki
  pages) — not started.
- `World.shouldRain`/`canSnowAt` (server-side weather suppression near a block) + `EntityRenderer`
  (client-side rain/snow rendering + particle suppression — Forge exposes no cancel-friendly rain
  event pre-1.16) → **Rain Shield** — not started.
- `StructureOceanMonumentPieces$MonumentCoreRoom.addComponentParts` (places a **Special Chest** loot
  room in generated ocean monuments) — **Special Chest** itself is already shipped and kept (see
  above, undocumented-but-kept); only its ocean-monument worldgen placement is missing. Worth a closer
  look before defaulting to a coremod here too — 1.14.4's structure-piece system might expose something
  cleaner than 1.12.2 had for this one specifically (unlike the jigsaw-village case, ocean monuments
  aren't jigsaw-based even in later versions).

**This absorbs and replaces Batch 7 below, rather than sitting next to it** — Batch 7 was already
"convert 2 Mixins to coremods," which is the exact same mechanism and the exact same underlying
problem (one of those 2 Mixins, Super Lubricent Boots, *is* one of these 17 original ASM patches) as
everything above. Splitting them into two separate batches would mean standing up the coremod-writing
pattern twice for no reason. See the merged batch below (still called "Batch 7," scope expanded).

## Overall roadmap (package order, compat dropped)

1. **Batch 1 (this plan): standalone blocks** — no TileEntity/GUI/Container/network/compat dependency.
2. Tile-entity-backed blocks + their TileEntities + Containers/GUIs + network packets, subsystem by
   subsystem (redstone interface, spectre energy network, floo network, etc.).
3. Remaining items (non-block items: tools, spectre tools, potions ingredients, etc.).
4. Worldgen (biomes, features — adapting 1.12.2's `WorldGenerator`/`IWorldGenerator` code to the
   `Feature`/`GenerationStage`/`Placement` API already used by `BloodRoseFeature`/`ModFeatures`). **Done,
   2026-09-27** — the only two wiki-tagged worldgen features (Ancient Furnace, Peace Candle) both shipped,
   see progress log's "Worldgen batch" entry. Both have disclosed, environment-level gaps (no runtime
   biome-reassignment API; no village-jigsaw hook point) rather than being incomplete ports.
5. Potions/enchantments.
6. Recipes (crafting/anvil/imbuing) and remaining client rendering (particles, magic circles, notifications).
7. **Mixin-to-coremod migration** — technical debt cleanup, not new content; deliberately last. See
   "Batch 7" near the end of this plan for the full scope and why it's deferred to the end.

Each batch: port the files, keep the code compiling against the Forge 1.14.4 API conventions already
established on `master`, register everything through the existing `ModBlocks`/`ModItems`-style
`@ObjectHolder` + `RegistryEvent.Register` pattern, then check in with you before moving to the next batch.

## Batch 1 — standalone blocks

### Shared infrastructure to port first (used by nearly every block in this batch)

- `block/BlockBase.java` (1.12.2) → adapt to 1.14.4 `Block` base: registry name via `setRegistryName`
  at registration time (1.14.4 no longer auto-registers an ItemBlock from the block constructor — that
  now happens explicitly in `ModItems.registerItems`, per the existing pattern), creative tab via
  `Block.Properties`, `ISuperLubricent` slipperiness hook.
- `block/material/MaterialHardWood.java` → trivial `Material` subclass, direct port.
- Marker/behavior interfaces from `lib/`: `INoItem.java`, `ISuperLubricent.java`, `ILuminousBlock.java`,
  `ILuminousItem.java`, `IRTBlockColor.java`, `IRTItemColor.java` — port as-is, updating imports only.
- `BlockContainerBase.java` and the unlisted-property helpers (`lib/properties/UnlistedBool.java`,
  `UnlistedEnum.java`) are **not** needed for Batch 1 (no TileEntity blocks here) — skip until Batch 2.

Reuse the conventions already established by the six ported blocks
([PlatformBlock.java](src/main/java/lumien/randomthings/block/PlatformBlock.java),
[RainbowLampBlock.java](src/main/java/lumien/randomthings/block/RainbowLampBlock.java),
[FertilizedDirtBlock.java](src/main/java/lumien/randomthings/block/FertilizedDirtBlock.java),
[SuperLubricentStoneBlock.java](src/main/java/lumien/randomthings/block/SuperLubricentStoneBlock.java),
[SticksBlock.java](src/main/java/lumien/randomthings/block/SticksBlock.java),
[BloodRoseBlock.java](src/main/java/lumien/randomthings/block/BloodRoseBlock.java)) and the registries
([ModBlocks.java](src/main/java/lumien/randomthings/block/ModBlocks.java),
[ModItems.java](src/main/java/lumien/randomthings/item/ModItems.java)): `Block.Properties.create(...)`
builder chains, `BlockState`/`StateContainer.Builder` for properties, `VoxelShape` for shapes,
`@ObjectHolder` + explicit `registry.register(new Xxx().setRegistryName(...))`.

### Blocks to port (1.12.2 source → new 1.14.4 class, following the existing `FooBlock` naming convention)

`block/`: BeanSprout, BeanStalk, BiomeGlass, BiomeStone (has metadata+color ItemBlock), BlazingFire,
BlockLuminous, BlockLuminousTranslucent, BlockOfSticks *(note: distinct from the already-ported
`SticksBlock`/`block_of_sticks` — check for naming collision against 1.12.2 registry names before
picking the new registry name)*, ColoredGrass, CompressedSlimeBlock, ContactButton, ContactLever,
GlowingMushroom, LapisGlass, LapisLamp, Lotus, PitcherPlant (fluid-capability read only, no TE), Pod,
QuartzGlass, QuartzLamp, Sakanade, SidedRedstone, StainedBrick, SuperLubricentIce,
SuperLubricentPlatform, TriggerGlass, SpectreBlock (plain decorative block — no energy-network code
despite the name).

`block/plates/`: AcceleratorPlate, BouncyPlate, CollectionPlate (reads neighbor `IItemHandler`
capability, no own TE), CorrectorPlate, DirectionalAcceleratorPlate, ItemRejuvenatorPlate,
ItemSealerPlate, RedirectorPlate, RedstonePlate.

`block/spectretree/`: SpectreLeaf, SpectreLog, SpectrePlank, SpectreSapling (tree-set blocks only; the
spectre *energy* subsystem is Batch 2+, these are just leaves/logs/planks/saplings).

`item/block/`: ItemBlockBiomeStone, ItemBlockColored, ItemBlockColoredGrass, ItemBlockOfSticks,
ItemBlockPlatform, ItemBlockSpecialChest → port as `BlockItem` subclasses in 1.14.4 style.
`ItemBlockLuminous` and `ItemBlockClothLuminous` also belong here once their `@Optional.Interface`
JEI (`ISlowRenderItem`) hook is stripped per the "drop compat" decision — otherwise standalone.

This batch also restores two behaviors that used to come from ASM and must not be dropped:
`TriggerGlass` gets a direct `canFallThrough(BlockState)` override (was ASM patch #17) so triggered glass
lets sand/gravel fall through, and `FertilizedDirtBlock` (already ported but currently missing this) gets
a `SaplingGrowTreeEvent` listener restoring "vanilla tree growth doesn't revert Fertilized Dirt to plain
dirt" (was ASM patch #12).

Register every new block/item through `ModBlocks.registerBlocks` / `ModItems.registerItems` and add the
matching `@ObjectHolder` fields, following the exact pattern already in those files. Where a block needs
resources (blockstate JSON, model JSON, lang entry, loot table), add minimal versions mirroring the
existing ported blocks' `src/main/resources/assets/randomthings/...` layout — reuse 1.12.2's textures
(`git show origin/1.12.2:src/main/resources/assets/randomthings/textures/...`) copied over, not redrawn.

## Progress log

- **Slice 1 (done):** LapisGlassBlock, LapisLampBlock, QuartzGlassBlock, QuartzLampBlock,
  SuperLubricentIceBlock, SuperLubricentPlatformBlock, TriggerGlassBlock, CompressedSlimeBlock,
  ContactButtonBlock, ContactLeverBlock, SpectreBlock ported and registered, with copied textures and
  new blockstate/model/lang resources. `TriggerGlassBlock.canFallThrough` and a `BlockEvent.EntityPlaceEvent`
  listener in `RandomThings.java` restore ASM patches #17 and #12. StainedBrickBlock's Java class is
  written but not yet registered/resourced (needs a 16-color x luminous/non asset matrix — deferred to
  the next slice so it doesn't get a half-finished registration).
- **Slice 2 (done):** `spectretree/` package (SpectreLogBlock, SpectrePlankBlock, SpectreLeafBlock,
  SpectreSaplingBlock) — extended vanilla's `RotatedPillarBlock`/`LeavesBlock`/`BushBlock` directly
  instead of porting the 1.12.2 hand-rolled axis/decay logic, since 1.14.4 already implements it
  generically. `SpectreSaplingBlock.grow()` places a simplified fixed-shape tree directly rather than
  going through the worldgen `Feature` system (that belongs to the worldgen batch). Also the full
  `plates/` package (9 blocks) via a new shared `PlateBlock` base class (thin shape, no collision,
  break-when-unsupported — replacing ~40 lines of duplicated boilerplate per block in the original).
  Both compiled clean (after fixing `Material.GROUND` → `Material.EARTH`, a 1.14.4 rename) and got
  textures/blockstates/models/lang. Simplifications: plate directional/color texture overlays are
  dropped (flat base texture only), `SpectreLeafBlock` drops the Ectoplasm drop chance (item not yet
  ported), redirector/redstone plates render identically regardless of facing (no arrow decal).
- **Slice 3 (done):** GlowingMushroomBlock, SidedRedstoneBlock, PitcherPlantBlock (simplified - see
  below), and StainedBrickBlock finished (32 variants: 16 colors x luminous/non, registered via a
  `DyeColor` loop rather than 32 hand-written fields). All textures are the real 1.12.2 art (copied
  byte-for-byte via `git show`, verified as valid PNGs), not placeholders.
  Skipped this round (need infrastructure or items not yet ported, revisit later): BeanSprout/BeanStalk/
  Pod (need a `beans` item + interlinked grow chain), Lotus/Sakanade (need unported ingredient items +
  potion effects), ColoredGrass/BiomeGlass/BiomeStone (need a runtime `IBlockColor`/`BlockColors`
  registration system - none of the mod's ported blocks have needed real per-position color tinting yet,
  RainbowLamp/Platform/StainedBrick all use pre-baked per-variant textures instead).
  Simplification: `PitcherPlantBlock` drops the fluid-bottle-filling/cauldron-filling mechanic - Forge
  1.14.4's `net.minecraftforge.fluids.Fluid` is a legacy compat class disconnected from vanilla's real
  `net.minecraft.fluid.Fluids`, and getting this right needs real research (relevant to several
  tile-entity blocks in Batch 2 too), so it's deferred rather than guessed under time pressure.
  Luminous stained brick uses only the `_tint` texture layer (the base+tint 2-layer compositing from
  1.12.2 isn't reproduced) - still real original art, just one of the two layers.
- **Slice 4 (done): block/item color infrastructure.** Ported `lib/IRTBlockColor.java` /
  `lib/IRTItemColor.java` (updated to 1.14.4 signatures: `IEnviromentBlockReader` in place of
  `IBlockAccess`) and added two generic listeners in `RandomThings.RegistryEvents`
  (`onBlockColorHandler`/`onItemColorHandler`, both `ColorHandlerEvent`-based) that scan
  `ForgeRegistries.BLOCKS`/`ITEMS` at client-setup time and auto-wire any randomthings block/item
  implementing the interface into Forge's `BlockColors`/`ItemColors` system - no per-block registration
  code needed, matching the "generic bridge" approach the plan called for. Exercised it with three
  blocks: `BiomeGlassBlock` and `BiomeStoneBlock` (x5 variants: cobble/smooth/brick/cracked/chiseled,
  reusing vanilla stone-family textures with a tinted custom model, exactly like the 1.12.2 resources
  did) using biome color, and `ColoredGrassBlock` using a static per-`DyeColor` tint. Simplifications:
  biome tint uses vanilla's own `BiomeColors.getGrassColor` instead of the original's bespoke
  BiomeDictionary-category heuristic (still a real per-biome color); `ColoredGrassBlock` ships as a
  single default-white block/item for now rather than 16 selectable color variants - picking a color
  needs a crafting recipe or creative-subitem mechanism that belongs with the recipes/items batch.
- **Slice 5 (done):** `LuminousBlock`/`LuminousTranslucentBlock`, 16 `DyeColor` variants each (32 total),
  following the exact StainedBrick loop-registration pattern; real 1.12.2 textures copied for all 32
  (confirmed the "_t" translucent variant genuinely has alpha vs. the opaque one, via `file`).
  **`item/block/` classes resolved, not ported:** read `ItemBlockOfSticks`/`ItemBlockPlatform`/
  `ItemBlockSpecialChest` - all three exist purely to implement 1.12.2's `getMetadata`/
  `getUnlocalizedName` metadata-subtype plumbing, with no other distinct behavior. Since master's
  established 1.14.4 architecture already replaces every metadata variant with discrete separate blocks
  (Platform, StainedBrick, LuminousBlock, etc.), these classes are obsolete - a plain `BlockItem` via
  `registerItemForBlock` is the correct 1.14.4 equivalent and already what's used throughout. This line
  item is closed, not just skipped.
- **Bean family scoped for a future slice (BeanSprout, BeanStalk, Pod):** found the piece that was
  missing context last time - `item/ItemBean.java` (1.12.2) is what actually plants/originates a
  `BeanStalkBlock` in the world; the three blocks don't stand alone. Do `ItemBean` + `BeanStalkBlock` +
  `BeanSproutBlock` (can extend vanilla `CropsBlock` - handles age/growth/bonemeal generically, override
  `onBlockActivated` for the original's "harvest without breaking" interaction) + `PodBlock` (can override
  `Block.getDrops(BlockState, LootContext.Builder)` directly instead of a loot table JSON) together, plus
  a `beans` item, so the subsystem works end-to-end rather than landing partially wired.
- **Slice 6 (done): the Bean family, end-to-end.** `BeanItem` (generic - plants a given block on right-click,
  reused for all 3 "bean" metadata variants from 1.12.2, now 3 discrete items: `beans`, `lesser_magic_bean`,
  `magic_bean`), `BeanSproutBlock` (extends vanilla `CropsBlock` for age/growth/bonemeal, custom
  `onBlockActivated` override for the original's "harvest without breaking"), `BeanStalkBlock`
  (constructor flag distinguishes the fast/strong "magic" stalk from the slow "lesser" one; grows upward
  each scheduled tick, magic variant caps itself with a `PodBlock` near the build-height limit), and
  `PodBlock` (drops beans via a direct `getDrops(BlockState, LootContext.Builder)` override, no loot
  table JSON needed). Compiled clean first try. All textures are real 1.12.2 art (bean_sprout has 2
  growth-stage textures mapped 0-6/7 exactly like the original blockstate did; bean_stalk vs
  lesser_bean_stalk correctly map to the original's two *different* stalk textures - "specialbeanstalk"
  for the magic variant, "beanstalk" for the lesser one).
  Simplifications: dropped the original's self-destroys-into-a-pod-when-blocked edge case and the custom
  `isLadder` override (the existing push-while-climbing collision behavior already gives a climbable feel).
- **Slice 7 (done): Lotus + Sakanade.** Rather than port the full 14-value `ItemIngredient` megaitem (which
  also touches several unported subsystems - dispenser behavior, entities, the Spectre/Floo systems -
  for the other 12 values), registered just the two discrete ingredient items these blocks actually need
  (`lotus_blossom`, `sakanade_spores`) as plain items, matching the established discrete-item convention.
  Also added `lotus_seeds` (`LotusSeedsItem`, plants `LotusBlock`) since the original's `getItemDropped`/
  `getPickBlock` depended on it. `LotusBlock` (4-stage crop, harvest-without-breaking at max age, like
  BeanSprout) and `SakanadeBlock` (`IShearable`, hangs from giant mushroom caps) both compiled clean
  first try, with real 1.12.2 textures (Lotus's 4 growth-stage textures mapped 1:1 to the 4 age values).
  Simplifications: dropped Lotus's dead `IGrowable` implementation (bonemeal was disabled and `grow()`
  was a no-op in the original - functionally inert code, not a real feature) and Sakanade's "collapse"
  potion effect (that custom potion type isn't ported yet).
  The remaining 12 `ItemIngredient` values (Evil Tear, Ecto Plasm, Spectre Ingot, Golden Egg, etc.) stay
  deferred - each belongs with whatever future batch ports its owning subsystem.
- **Slice 8 (done): BlazingFireBlock — Batch 1 complete.** Extended vanilla `FireBlock` directly instead
  of reimplementing its ~250-line spread algorithm. Ground-truth checked via `javap`: `tickRate(IWorldReader)`
  is a clean public override point (vanilla default 30, overridden to 15 here — roughly doubles the
  effective spread rate over time, the highest-impact of the original's three tuning tweaks). The other
  two 1.12.2 tweaks (4x catch-chance multiplier, faster per-catch age growth) live inside FireBlock's
  *private* `tryCatchFire`/`tick` internals in this Forge version and aren't reachable without copying the
  whole algorithm — traded exact tweak fidelity for not maintaining a second copy of vanilla fire logic,
  and said so plainly rather than silently dropping it. Real 1.12.2 animated fire art (`fire_layer_0/1`
  + `.mcmeta` animation frames) reused via vanilla's own `block/fire_floor`/`fire_side`/`fire_side_alt`/
  `fire_up`/`fire_up_alt` multipart model convention, since `BlazingFireBlock` inherits FireBlock's exact
  NORTH/EAST/SOUTH/WEST/UP/AGE state properties. Compiled clean first try.
- **Slice 9 (done): Mixin toolchain stood up + the two remaining BlazingFire tweaks restored.** This is
  the project's first real Mixin usage. Added `org.spongepowered:mixingradle:0.7-SNAPSHOT` (buildscript)
  + `org.spongepowered:mixin:0.8:processor` (`annotationProcessor` and `compileOnly`) to `build.gradle`,
  `randomthings.mixins.json` config, and the `MixinConfigs` jar-manifest attribute Forge 1.14.4 uses to
  discover it. Disassembled vanilla `FireBlock.tryCatchFire` via `javap -c` to get exact ground truth
  (it wasn't available any other way, being private) and confirmed precisely what the 1.12.2 tweaks
  changed:
  - catch chance: `random.nextInt(chance) < flammability` &rarr; `< flammability * 4`
  - die-out check bound: `random.nextInt(age + 10) < 5` &rarr; `random.nextInt(age / 2 + 1) < 5`
  - age growth: `age + random.nextInt(5) / 4` &rarr; `age + random.nextInt(2)` (reproduced exactly by
    widening the nextInt bound to 8, since `nextInt(8) / 4` has the identical 50/50 {0,1} distribution)

  `FireBlockMixin` (`@Mixin(value = FireBlock.class, remap = false)`) applies all three via
  `@Redirect`/`@ModifyArg`, each guarded with `(Object) this instanceof BlazingFireBlock` so vanilla fire
  and any other `FireBlock` subclass are provably untouched.
  **Known limitation, disclosed plainly:** `remap = false` was required because the Mixin AP couldn't find
  an MCP&rarr;SRG obfuscation mapping for `tryCatchFire` in this environment. This means the mixin targets
  the MCP-mapped dev names directly - it's confirmed to compile and will work correctly when running via
  the dev environment (`gradlew runClient`/`runServer`), but has **not** been verified against a
  reobfuscated production jar, since nothing here can launch the actual game to confirm the injection
  fires as intended at runtime either way (same caveat as all resource/JSON work all along - full
  in-game verification is on you).
- **Batch 1 (standalone blocks) is now fully ported and closed out.** Next: scope Batch 2 (tile-entity-
  backed blocks + their TileEntities + Containers/GUIs + network packets), per the roadmap in this plan's
  Context section.
- **Toolchain note:** a working JDK 8 + Gradle 4.9 compile loop is now set up (`env/activate.ps1`,
  `env/activate.sh`) and every slice going forward is compile-verified with `javap` used to check real
  API signatures against the cached mapped Forge jar before/after writing code, instead of guessing
  from training-data recall.
- **Toolchain upgrade:** Gradle wrapper bumped from 4.9 to 5.6.4 (empirically verified as the real
  ceiling for this project's pinned `ForgeGradle 3.0.197` - 4.10.3 and 5.6.4 both build clean, but
  Gradle itself flags the next boundary at 6.0, where FG3.0.197's removed-in-5.x internals would
  actually break). `build.gradle` and all source are untouched; only
  `gradle/wrapper/gradle-wrapper.properties` changed. Verified via `./gradlew build` including the
  `reobfJar` production-jar step, not just `compileJava`.

### Batch 2 — tile-entity-backed blocks (in progress)

Scope: ~50 1.12.2 tile-entity-backed blocks (`tileentity/`, matching `client/gui/` screens and, where
present, `inventory/`-style containers), each needing a 1.14.4 `TileEntity` + `INamedContainerProvider`
+ `Container`/`ContainerType` + `ContainerScreen` (where the original had a GUI) + registration, using
the pattern already established by `AdvancedRedstoneTorchTileEntity`/`AdvancedRedstoneTorchContainer`/
`AdvancedRedstoneTorchScreen`/`ISignalContainer` (a single generic `ContainerSignalMessage` network
message shared by every GUI, keyed by an `id` the container itself interprets - no new packet types
needed per block). This is far larger than Batch 1 and will span multiple slices; each slice below is
its own self-contained, compile-verified unit of real, working game content, not a stub.

- **Slice 1 (done):** Six self-contained, non-subsystem-dependent blocks, picked specifically to avoid
  the big deferred subsystems (Spectre energy, Ender network, Floo network, wireless Redstone Interface,
  Rain Shield/Peace Candle - all of which need Mixin work per this plan's ASM-mapping section, or depend
  on other not-yet-ported items/enchantments):
  - `AnalogEmitterBlock` - redstone signal amplifier; reads power on its facing side, re-emits a
    player-configurable 0-15 strength out every other side. GUI is a straight copy of
    `AdvancedRedstoneTorchScreen`'s +/- pattern with one value instead of two.
  - `IgniterBlock` - a block version of flint and steel with a 3-mode cycle (Toggle/Ignite/Keep
    Ignited) that lights/extinguishes the block in front of it on redstone transitions. Since 1.14.4
    has no `IRedstoneSensitive`-style "fires only on an actual power transition" hook, the TE tracks its
    own previous powered state and diffs it in `neighborChanged` (same technique used for
    `AdvancedRedstoneTorchBlock`'s burnout tracking). Keep-Ignited re-checks/relights on a
    self-rescheduling 10-tick loop rather than only on neighbor events, so it also recovers from e.g.
    the fire being doused by water.
  - `OnlineDetectorBlock` - emits redstone when a configured player username is currently on the
    server. First GUI in the mod with a text field (`TextFieldWidget`); the username is threaded through
    `NetworkHooks.openGui`'s `Consumer<PacketBuffer>` extra-data overload so the client-side container
    (built from a `ContainerType` factory with no direct TE access) still gets the current value to
    pre-populate the field, then edits are pushed back through the same generic `ISignalContainer.send`
    used everywhere else.
  - `ItemCollectorBlock` - no GUI at all. A small mountable nub (custom per-face `VoxelShape`) that
    vacuums nearby dropped items into whatever inventory it's attached to, with the same adaptive
    1-20 tick self-throttling backoff as 1.12.2 (ticks faster while it's actually finding items, backs
    off when the area's empty).
  - `plates/ExtractionPlateBlock` and `plates/ProcessingPlateBlock` - thin pressure-plate-shaped
    blocks (now sharing Batch 1's `PlateBlock` base for the drop-when-unsupported/no-collision shape
    boilerplate, which that base was already written generically enough to cover). Extraction pulls
    items out of the inventory below (or behind its output side) and ejects them as dropped items
    toward a sneak-click-cycled horizontal output direction; a facing-cycle GUI picks which side of the
    *target* inventory to pull from. Processing combines that same extraction-and-eject timer with a
    touch-triggered insert (any dropped item that collides with it gets pushed into the inventory below).
    Simplification, disclosed here: 1.12.2's `ProcessingPlate` also had an elaborate cosmetic
    item-bounce/trajectory-redirect effect on collision (redirecting a moving item entity's motion
    vector based on which way it was already moving) - only the actual item transfer is reproduced, not
    that bounce animation.
  - Every new TE with a GUI reuses `ISignalContainer`/`ContainerSignalMessage` with zero new network
    code; facing-cycle buttons all follow the same "click sends an empty-payload id, server rotates and
    the tracked int re-syncs" pattern proven by `IgniterContainer.rotateMode`.
  - Drive-by fix: `AdvancedRedstoneTorchTileEntity.getDisplayName()` was using the translation key
    `randomthings.block.advanced_redstone_torch`, which doesn't match the lang file's actual
    `block.randomthings.advanced_redstone_torch` entry (so the container title was silently falling
    back to the raw untranslated key) - corrected to the right key while in the area.
  - All 6 blocks have real 1.12.2 textures (copied via `git show`, verified as valid PNGs), blockstates,
    models, item models, loot tables (self-drop-on-break), and lang entries. `IgniterScreen`'s three
    mode-label lang keys were kept as the original 1.12.2 key names (`gui.igniter.toggle`/`ignite`/
    `keepIgnited`) rather than the `gui.randomthings.<block>.<field>` convention used everywhere else in
    this file - functionally correct, just a minor naming-convention inconsistency worth normalizing
    later.
  - `ItemCollectorBlock`'s 6-way directional model uses a derived (not vanilla-verified) x/y rotation
    table for reorienting a single "up-facing" base model to the other 5 directions - the block's actual
    facing logic/shape/collision are all correct regardless, but the horizontal-facing render orientation
    is a best-effort derivation that hasn't been visually confirmed in-game.
  - Compile-verified (`./gradlew compileJava` and a full `./gradlew build` including `reobfJar`) after
    every API-uncertain call was checked against the real Forge jar via `javap` first - this caught and
    fixed real mistakes before they became bugs: `DirectionProperty`/`TileEntity.getCapability` returns
    `LazyOptional<T>` not a direct value (no `hasCapability` method exists in this Forge version at all),
    entity spawning (`addEntity`) lives on `ServerWorld` not `World`, `Direction.byIndex` for cycling.
- **Slice 2 (done): AdvancedRedstoneRepeaterBlock.** Extends vanilla `RepeaterBlock`/`RedstoneDiodeBlock`
  directly, replacing its fixed 1-4 tick `DELAY` property with a TileEntity-backed `turnOnDelay`/
  `turnOffDelay` pair (2-10000 ticks each), same GUI pattern as `AdvancedRedstoneTorchScreen`. Ground-
  truthed via `javap -c` bytecode disassembly of `RedstoneDiodeBlock`, which caught a real bug before it
  shipped: the primary "wait N ticks then flip POWERED" scheduling decision happens in `updateState()`
  (reached from `neighborChanged`), not in `tick()` as first assumed - an initial draft only overrode
  `tick()` and would have silently ignored the configured delay for the main neighbor-triggered path.
  Both `updateState()` and `tick()` are now overridden with the exact decoded vanilla control flow,
  substituting `arr.turnOnDelay()`/`arr.turnOffDelay()` for vanilla's single `getDelay(state)` based on
  transition direction (becoming powered vs. unpowered). Confirmed via `javap` that vanilla's own
  `Blocks.REPEATER` is a single block with a `POWERED` boolean property (not an unpowered/powered pair
  like `AdvancedRedstoneTorchBlock`), so only one `ModBlocks`/`ModTileEntityTypes`/`ModContainerTypes`
  entry was needed. All resources (real 1.12.2 off/on/off_locked/on_locked custom models and textures,
  GUI texture, item texture, 16-combo blockstate covering facing x powered x locked, loot table, lang)
  copied/ported, translating 1.12.2's `textures/blocks/`+`textures/items/` vanilla references to 1.14.4's
  renamed `block/`+`item/` folders. Full `./gradlew build` (including `reobfJar` and resource processing)
  verified clean, not just `compileJava`.
- **Slice 3 (done): IronDropperBlock.** First Batch 2 block with a real item-slot inventory GUI (previous
  slices were all signal/value-only containers). Internal 9-slot `ItemStackHandler` exposed as an
  `IItemHandler` capability on all sides (own `getCapability`/`invalidateCaps` override, no existing
  precedent for a block exposing its *own* inventory as a capability in this mod yet - `ExtractionPlate`/
  `ItemCollector` only ever read from *other* blocks' capabilities). `IronDropperContainer` uses
  `SlotItemHandler` for the 9 dropper slots plus standard `Slot`s for the 36 player-inventory/hotbar
  slots, with the client-side constructor binding to a fresh throwaway `ItemStackHandler(9)` (not the
  real TE) - confirmed via `javap` this is exactly vanilla `DispenserContainer`'s own established
  pattern, since ordinary container slot-sync packets populate whatever `IItemHandler` the client Slot
  wraps regardless of its identity. Six-way facing (`context.getNearestLookingDirection()`, matching
  vanilla dispenser's own placement convention) with real 1.12.2 art reusing vanilla's `block/orientable`/
  `block/orientable_vertical` parent models exactly like a dispenser. `onReplaced` override drops the
  inventory contents on block removal via `InventoryHelper.spawnItemStack` (ported from 1.12.2's
  `breakBlock`/`InventoryUtil.dropItemHandlerItems`).
  Redstone modes (`PULSE`/`REPEAT_POWERED`/`REPEAT`), pickup delay, effects (sound/particle), and random-
  motion toggle all ported faithfully from `TileEntityIronDropper`'s `drop()` logic, including the
  "insert into a capability-exposing neighbor directly if present, else spawn a dropped ItemEntity with
  hand-tuned motion" branch and PULSE mode's rising-edge-only trigger (reusing Igniter's established
  previous-state-diff technique, since 1.14.4 has no transition-only redstone hook here either).
  **Disclosed simplification:** the original's 4 icon-sprite buttons (`GuiEnumButton`/`GuiBoolButton`,
  a custom tooltip/icon-atlas widget system) are replaced with plain text cycle buttons, matching
  `IgniterScreen`'s already-established convention elsewhere in this port, rather than porting a second
  custom button widget class for one screen. The custom `mergeItemStack`/`transferStackInSlot` override
  from 1.12.2 was NOT ported as a distinct algorithm - 1.14.4's own inherited `Container.mergeItemStack`
  already implements equivalent vanilla slot-merge logic, confirmed via `javap`, so only a slimmed-down
  `transferStackInSlot` (shift-click routing) was written, not a full custom merge reimplementation.
  Full `./gradlew build` verified clean.
- **Slice 4 (done): CustomWorkbenchBlock.** Extends vanilla `CraftingTableBlock` directly (`javap`-
  confirmed its constructor is `protected` and `onBlockActivated`/`getContainer` are inherited as-is),
  so this needed no TileEntity, Container, or Screen at all - vanilla's own crafting-grid GUI is reused
  unchanged, matching 1.12.2's own `getGuiID() -> "minecraft:crafting_table"` shortcut. **Disclosed
  simplification:** dropped the original's dynamic per-wood-type frame recoloring (a custom
  `IUnlistedProperty<IBlockState>`-backed baked model driven by the TileEntity's stored wood block/meta)
  entirely, shipping one fixed skin instead (this mod's real "tools on top" 1.12.2 art on the sides/top,
  vanilla oak planks on the underside) - reproducing the dynamic version needs a custom `IBakedModel`,
  infrastructure this mod hasn't needed anywhere else yet, and doing it as a shallow imitation would be
  worse than a plainly-disclosed fixed skin. Functionally identical to vanilla's own crafting table
  otherwise. Full `./gradlew build` verified clean.
- **Batch 2 session summary (4 slices, this session):** AnalogEmitter/Igniter/OnlineDetector/
  ItemCollector/ExtractionPlate/ProcessingPlate (slice 1), AdvancedRedstoneRepeater (slice 2, with a
  real bytecode-disassembly-caught scheduling bug fixed before shipping), IronDropper (slice 3, first
  real item-slot inventory GUI in this port), CustomWorkbench (slice 4). 10 new tile-entity/GUI-backed
  blocks total, all compile- and full-build-verified. Next candidates for a future slice need real new
  scoping passes before starting, not a quick continuation: item-filter/slot-heavy GUIs
  (`FilteredRedirectorPlate`, `FilteredSuperLubricentPlatform`, `EntityDetector`), items-dependent blocks
  (`SoundDampener`, `BlockBreaker`), and the big deferred subsystems (Spectre energy, Ender network, Floo
  network, wireless Redstone Interface, Rain Shield, Peace Candle) that need Mixin work.
- **Slice 5 (done): survey + 4 more standalone utility blocks.** Surveyed all ~37 remaining 1.12.2
  TileEntity-backed blocks (full list, dependencies checked via `git show`/import inspection) to pick the
  next self-contained batch without unported-item or heavy-new-rendering dependencies:
  - `PlayerInterfaceBlock` - links to whoever placed it and exposes their online inventory as an
    `IItemHandler` capability (hotbar from below, armor from above, offhand from north, rest of main
    inventory from every other side) - no GUI at all, matching the original exactly. Turned out simpler
    to port than 1.12.2: `javap` confirmed 1.14.4's `PlayerInventory` already `implements IInventory`
    directly, so the original's ~80-line custom `IInventory` wrapper class bridging `InventoryPlayer`
    wasn't needed at all - `new InvWrapper(player.inventory)` works directly.
    **`CreativePlayerInterfaceBlock` was deliberately NOT ported** - traced its only code path for
    ever acquiring a linked player UUID and found it's exclusively wired through an OpenComputers driver
    class (`DriverCreativePlayerInterface`), i.e. genuinely dead/unusable without third-party OC compat,
    which this port has already decided to drop. Porting it would have shipped a block with no way to
    ever function.
  - `InventoryTesterBlock` - a small mountable nub (reusing `ItemCollectorBlock`'s six-way
    `DirectionProperty`/per-face-`VoxelShape` pattern) that emits weak redstone power based on whether
    the item sitting in its own slot could be inserted into the inventory it faces, with an
    invert-signal toggle. **Disclosed simplification:** the original's item slot was a "ghost slot"
    (`SlotGhostItemHandlerStacked` - configures the test item without ever actually consuming it,
    click-drag doubles/halves the ghost count); this port uses a real `SlotItemHandler` instead, so the
    test item is genuinely held by the block rather than being a free-to-configure ghost. Also dropped
    the original's strict "must be placed against a real inventory or it pops itself off" placement
    validation - it can be placed anywhere now, simply reading no power if nothing valid is behind it.
  - `InventoryRerouterBlock` - a hopper-shaped block that remaps capability access per-side (each
    non-facing side can be redirected to a different real side of whatever's on its facing side, cycled
    by right-clicking that face), with the same static-set cycle-detection guard as the original to
    prevent two rerouters from infinite-looping into each other. **Disclosed simplification:** dropped
    the original's per-face arrow-decal overlay rendering (a custom `IUnlistedProperty`/baked-model
    system showing each side's current mapping) - the block now looks identical regardless of mapping,
    matching the precedent already set by this mod's redirector/redstone plates in Batch 2 slice 1.
  - `ChatDetectorBlock` - links to whoever placed it, watches their chat for one exact configured
    message (`TextFieldWidget` GUI, reusing `OnlineDetectorScreen`'s established text-input convention)
    and emits a 20-tick redstone pulse when matched, optionally consuming/hiding that message from chat.
    Required this port's first Forge chat-event hook: registered a `ServerChatEvent` listener in
    `RandomThings.java` (confirmed `@Cancelable` via `javap`) that iterates a static
    `Set<ChatDetectorTileEntity>` (mirroring the original's `WeakHashMap`-backed set) and cancels the
    event if any linked detector consumed the message.
  All four registered, resourced (real 1.12.2 textures/models/lang/loot tables - `InventoryTesterBlock`
  and `ChatDetectorBlock`'s models/blockstates ported directly from the originals' JSON, which turned out
  to already use simple rotation transforms rather than per-facing element definitions), and full
  `./gradlew build` verified clean on the first compile attempt for all four blocks.
  **Deferred during this survey, with reasons noted for whoever scopes them next:**
  `BlockDiaphanous` (a "ghost block" that mimics another block's appearance/collision - needs a real
  `TileEntityRenderer`, `EnumBlockRenderType.ENTITYBLOCK_ANIMATED`-equivalent; this port has no TER
  infrastructure yet), `AncientFurnaceBlock` (a one-shot biome-flood-fill "hell furnace" - needs runtime
  biome mutation, and 1.14.4's biome-storage API for that hasn't been verified anywhere in this port
  yet), `LinkOrbBlock` (appears to belong to the already-deferred Ender network subsystem), `SoundBox`/
  `BiomeRadar`/`GlobalChatDetector` (need unported items: `ItemSoundPattern`, `ItemBiomeCrystal`/
  `ItemPositionFilter`, `ItemIDCard`), `RedstoneObserverBlock` (self-contained logic-wise, but the
  original's targeting flow depends on an unported `redstoneTool` item for click-to-target; needs a
  small design decision on a replacement targeting UI, e.g. X/Y/Z `TextFieldWidget`s, before starting),
  `PotionVaporizerBlock` and `SpecialChestBlock` (both plausible next candidates, not yet fully scoped).
- **Slice 6 (done): RedstoneObserverBlock + RedstoneToolItem.** Resolved the targeting-UI question noted
  after Slice 5 by just porting the original's actual mechanism instead of inventing a new one: a
  `RedstoneToolItem` (`onItemUse(ItemUseContext)`, NBT-tagged "linking" state, `hasEffect` glow while
  active) that right-click-links a Redstone Observer to any distant block, exactly matching 1.12.2's
  `ItemRedstoneTool` - simplified to only handle the Redstone Observer case, since the tool's other
  target (the wireless Redstone Interface) is still deferred pending Mixin work. `RedstoneObserverBlock`
  mirrors its linked target's weak/strong power onto itself, refreshed via a `BlockEvent.NeighborNotifyEvent`
  listener (registered in `RandomThings.java`, this port's first use of that event) that checks a static
  `Set<RedstoneObserverTileEntity>` for any observer whose target matches the changed position - direct
  port of the original's `WeakHashMap`-backed-set-plus-global-event-listener design. Its GUI is read-only
  (current target X/Y/Z or "No Target"); the actual link/unlink action happens through the tool, matching
  the original's `ContainerEmptyContainer`-based display-only GUI exactly. Real 1.12.2 textures reused,
  including the shared `redstoneinterface/basicredstoneinterface.png` GUI background (found despite a
  camelCase/lowercase filename mismatch between the Java source and the actual asset path in the
  original repo). Full `./gradlew build` verified clean, first compile attempt.
- **Slice 7 (done): PotionVaporizerBlock.** A furnace-fuelled room-filling potion-gas machine: BFS-style
  flood-fill of the enclosed air space it faces (capped at 100 blocks, spread over several ticks so large
  rooms don't cause a tick-time spike), applies the stored potion's effect to every `LivingEntity` inside
  that space while burning fuel, matching 1.12.2's exact algorithm and constants (night vision gets a
  longer top-up window than other effects, same as vanilla brewing). Reused vanilla API directly instead
  of porting 1.12.2 furnace-fuel helpers by hand: `AbstractFurnaceTileEntity.isFuel`/`getBurnTimes()` for
  the fuel slot, `PotionUtils.getEffectsFromStack` for reading the potion item, all `javap`-verified
  first (this port's first use of the renamed `Effect`/`EffectInstance` classes - `Potion`/`PotionEffect`
  in 1.12.2). **Disclosed simplifications:** the original sent a dedicated network packet
  (`MessagePotionVaporizerParticles`) to nearby players for the ambient per-block particle effect; this
  port just calls `ServerWorld.spawnParticle` directly, which already broadcasts to nearby players on its
  own, so no custom packet was needed - and `RedstoneParticleData` (tinted to the potion's own color)
  stands in for the original's bespoke vapor particle, reusing vanilla's colorable "dust" particle instead
  of adding a new particle type. The output (bottle) slot is enforced output-only via `ItemStackHandler
  .isItemValid` returning false for player-driven inserts, while the TE's own logic bypasses that
  correctly via `setStackInSlot` - which needed catching mid-write: an early draft used `insertItem` for
  the TE's own output-filling logic too, which would have silently no-opped once the slot was locked down,
  since `insertItem` (unlike `setStackInSlot`) itself calls `isItemValid`. Three-slot inventory GUI (fuel/
  potion-in/bottle-out, plus fuel-percent and potion-duration-percent progress readouts) using the same
  item-slot + player-inventory Container pattern established by `IronDropperContainer`. Real 1.12.2
  textures/models reused, including the dispenser-style `block/orientable`/`block/orientable_vertical`
  parent models shared with `IronDropperBlock`. Full `./gradlew build` verified clean, first compile
  attempt.
- **Slice 8 (done): SpecialChestBlock - this port's first custom `TileEntityRenderer`.** Picked up the
  deferral from Slice 7 and built it properly rather than dropping the feature. Key finding that made
  this much smaller than first estimated: `SpecialChestTileEntity extends ChestTileEntity` directly
  works cleanly - `javap`-disassembling `ChestTileEntity.createMenu` proved it calls
  `ChestContainer.createGeneric9X3(windowId, playerInv, this)` with **zero** reference to `ChestBlock`,
  so all of the ~150 lines of 1.12.2's hand-rolled lid-angle animation / open-close-sound / player-use-
  counting logic (`TileEntityLockableLoot` in 1.12.2) is inherited for free in 1.14.4, and the GUI itself
  is 100% vanilla's own chest screen/texture (`ContainerType.GENERIC_9X3` is a shared vanilla type, so
  no Container or Screen class was needed at all). `SpecialChestBlock` deliberately does NOT extend
  `ChestBlock` though - that class bakes in the double-chest `TYPE` property/adjacency-merging behavior,
  which 1.12.2's original never had, so extending it would have added unrequested double-chest merging
  as a side effect; instead it's a standalone `Block` with `BlockRenderType.ENTITYBLOCK_ANIMATED`
  (skips baked-model rendering, delegates entirely to the TER) using the same single-`HORIZONTAL_FACING`
  shape as the original.
  For the actual rendering, `javap -c` bytecode-disassembled `ChestTileEntityRenderer.render`/
  `getChestModel`/`applyLidRotation` in full (confirmed its texture selection hardcodes exactly
  trapped/christmas/normal/ender with no per-mod hook, which is why this needed a real custom renderer
  and not a config tweak) and reused vanilla's own modern `ChestModel` class directly - only the texture
  selection differs from vanilla, the matrix-stack transform/lid-rotation math was copied faithfully
  from the decoded bytecode. Also wrote a matching `ItemStackTileEntityRenderer` subclass
  (`Item.Properties().setTEISR(...)`, `javap`-verified this is the exact mechanism vanilla's own chest/
  banner/bed items use for their 3D item-icon rendering) so the item form renders as a real 3D chest
  instead of a flat icon, matching 1.12.2's own `"parent": "builtin/entity"` item model choice.
  Two discrete item/block variants (`special_chest_nature`/`special_chest_water`, matching this mod's
  established "separate block per variant" convention rather than 1.12.2's metadata-damage-value
  approach) sharing one `TileEntityType`, real 1.12.2 chest-skin textures reused. Full `./gradlew build`
  verified clean, first compile attempt - including the client-only rendering classes, which
  `compileJava` compiles alongside everything else in ForgeGradle's dev environment.
- **Slice 9 (done): EntityDetectorBlock.** Emits redstone (weak, or optionally strong on all sides) when
  an entity matching a configurable filter/class is within a configurable per-axis radius (0-10 blocks),
  with an invert toggle and a filter-item slot for a CUSTOM matching mode. **Disclosed simplifications:**
  the CUSTOM filter mode's item slot exists and is wired up, but since 1.12.2's `IEntityFilterItem`
  interface and its implementing items haven't been ported, CUSTOM mode is currently inert (matches
  behavior with an empty slot) rather than dead code removed outright - it'll start working the moment a
  filter item is ported. Also, 1.12.2's ANIMAL/MONSTER filters matched the marker interfaces
  `IAnimals`/`IMob`; `IAnimals` was removed entirely in 1.14.4, and `IMob` isn't itself an `Entity`
  subtype so it can't be used as a `Class<? extends Entity>` filter parameter either - both substituted
  with their closest concrete equivalents, `AnimalEntity` and `MonsterEntity` (confirmed via `javap`).
  Uses a real, unrestricted item slot (no `isItemValid` filtering needed, since `IEntityFilterItem`
  matching is an optional interface check, not a hard type restriction) alongside the standard player-
  inventory Container pattern. Icon-sprite toggle buttons again replaced with plain text buttons, per
  this port's established convention. Full `./gradlew build` verified clean after two small, expected
  first-pass errors (`IMob` not being `Class<? extends Entity>`-compatible, and
  `InventoryHelper.dropInventoryItems` needing an `IInventory` rather than a raw item list - fixed by
  switching to `InventoryHelper.dropItems`), both caught by the compiler immediately.
- **Slice 10 (done): `IEntityFilterItem` + `EntityFilterItem`, retrofitted into `EntityDetectorBlock`'s
  CUSTOM mode, plus `FilteredRedirectorPlateBlock`.** Rather than leave EntityDetector's CUSTOM mode
  permanently inert (as disclosed in Slice 9), ported the small foundational piece it and several other
  deferred blocks all depend on: `IEntityFilterItem` (an interface any item can implement to answer "does
  this entity match me") and `EntityFilterItem` (right-click/attack any living entity to capture its
  exact type as the filter target). **Disclosed simplification:** 1.12.2 matched via
  `Class.isAssignableFrom` on the raw entity class; 1.14.4 replaced ad-hoc entity subclassing with the
  data-driven `EntityType` registry, so this matches by exact `EntityType` instead (`javap`-confirmed
  `EntityType.getKey`/`byKey`/`getName` as the clean 1.14.4 equivalents) - the natural port choice, and
  arguably more correct given how entities are structured in this version. Wired this back into
  `EntityDetectorTileEntity.checkSupposedPoweredState`, so CUSTOM mode is now genuinely live, not a stub.
  `FilteredRedirectorPlateBlock` reuses Batch 1's `RedirectorPlateBlock`/`PlateBlock` entity-redirect
  physics math (ground-truthed from that already-ported class rather than re-deriving it) but picks
  between a left-turn or right-turn output per-entity from two `IEntityFilterItem` slots instead of one
  static configured output direction (second slot wins if both match, matching 1.12.2's sequential-
  overwrite order exactly). **Noted, not fixed:** 1.12.2 itself never shipped a block texture/model/
  blockstate for this block (confirmed via `git ls-tree` - only its GUI texture and the filter item's
  texture existed, no `textures/blocks/filteredredirectorplate*` at all, a genuine gap in the original
  mod); this port reuses the already-ported `plate_redirector` block's own texture/model shape rather
  than leaving a missing-texture placeholder, since it's the same "arrow redirect plate" concept.
  Full `./gradlew build` verified clean, first compile attempt.
- **Slice 11 (done): SlimeCubeBlock + AdvancedItemCollectorBlock.** `SlimeCubeBlock` forces its own chunk
  to always/never allow slime spawns (unpowered/powered) via a `LivingSpawnEvent.CheckSpawn` listener -
  the clean 1.14.4 replacement for ASM patch's `overrideSlimeChunk` hook that this plan's original ASM
  audit already identified. Disclosed scope difference: the original ASM patch targeted specifically the
  per-chunk slime-chunk RNG gate used by the cave spawn path; `CheckSpawn` fires for every slime spawn
  attempt instead, so this forces ALLOW/DENY for any slime spawn located in the cube's chunk rather than
  precisely re-targeting that one RNG check - functionally very close, not a byte-for-byte recreation.
  `AdvancedItemCollectorBlock` is a longer-range, filterable, configurable-per-axis-radius version of
  Batch 2 Slice 1's `ItemCollectorBlock`, reusing its adaptive 1-20 tick self-throttling tick loop
  directly. **Disclosed simplification:** 1.12.2's filter was a whole separate configurable item
  (`ItemItemFilter`) with its own 9-slot example-item inventory, whitelist/blacklist mode, and
  independently toggleable item/metadata/NBT/ore-dictionary matching - real scope for its own future
  slice if wanted. This port uses one plain example-item slot directly on the collector instead, matching
  by item type (`ItemStack.areItemsEqual`). Both compiled clean first try; full `./gradlew build` verified.
- **Slice 12 (done): FilteredSuperLubricentPlatformBlock.** Extends the already-ported
  `SuperLubricentPlatformBlock` directly, overriding its modern per-entity `getCollisionShape` (already
  a clean `ISelectionContext.getEntity()`-based check, no `addCollisionBoxToList`-style list mutation
  needed in 1.14.4) to also let dropped items matching a configured filter fall straight through. Same
  disclosed single-example-item-slot filter simplification as `AdvancedItemCollectorTileEntity`
  (Slice 11) rather than the full `ItemItemFilter` system.
- **Slice 13 (done): NotificationInterfaceBlock.** Links to whoever placed it and pops a real vanilla-
  style toast (title/description/item icon) on their screen on a redstone rising edge. This port's first
  server-to-targeted-client network message (`NotificationMessage`, added `RTPacketHandler.sendTo`
  using `PacketDistributor.PLAYER`) and first custom `IToast` (`NotificationToast`) - its exact
  background/text/icon layout ground-truthed via `javap -c` disassembly of vanilla's own
  `SystemToast.draw`, since IToast has no public layout spec to copy from otherwise. Confirmed safe to
  reference the client-only `NotificationToast`/toast-GUI classes from the message's `handle()` (only
  ever invoked when the packet is actually received, i.e. client-side only) using the same reasoning
  already validated by Slice 8's `Item.Properties.setTEISR` pattern.
- **Slice 14 (done): IdCardItem + GlobalChatDetectorBlock.** `IdCardItem` (right-click to bind to
  yourself, matches only that player wherever an `IEntityFilterItem` is accepted) is a small sibling to
  Slice 10's `EntityFilterItem`. `GlobalChatDetectorBlock` is `ChatDetectorBlock`'s broadcast cousin:
  listens for ANY player's chat (not just a linked owner's) and additionally requires a matching ID card
  in its 9-slot whitelist (or server-operator status, `Entity.hasPermissionLevel(2)`) to actually hide
  the matched message. Both wired into the same `ServerChatEvent` listener already registered for
  `ChatDetectorTileEntity` in Slice 5. Full `./gradlew build` verified clean, first compile attempt.
- **Batch 2 is now functionally complete** for every tile-entity-backed block that doesn't require a
  whole separate subsystem, entity type, recipe system, or worldgen structure. Final survey of what's
  left, and why each is deliberately deferred rather than attempted:
  - **`AncientFurnaceBlock` - checked and deliberately deferred, not just skipped:** turns out to
    implement `INoItem` (a pure marker interface, confirmed by reading it - `hasNoItem()` always `true`,
    used by `ModItems` registration to skip creating a `BlockItem` for it at all). This block was never
    player-placeable in 1.12.2 - it only ever appeared pre-placed inside a generated dungeon/structure.
    Porting the block in isolation, with no structure to place it and no worldgen batch yet, would ship
    a technically-registered but practically unreachable block. Belongs with this plan's future
    "Worldgen" batch instead, where the structure that places it would also get built.
  - **Custom-rendering blocks needing the same complexity class of work as a *generic* (not fixed, like
    `SpecialChestBlock`'s) block-model TER:** `BlockDiaphanousBlock` (mimics an arbitrary block's
    appearance/collision), `LightRedirectorBlock` (borrows a *neighbor's* rendered appearance - the ASM
    audit's "clean replacement exists" note refers to the `IModelData`/`ModelDataManager` mechanism, but
    the actual "render position X as if it were unrelated block Y" logic is still real, non-trivial new
    work), `VoxelProjectorBlock` (a rotating hologram of an arbitrary block/item model). All three need
    to invoke vanilla's generic block-model rendering pipeline for an arbitrary runtime-chosen
    `BlockState`, not a single fixed custom model like the chest - meaningfully harder than Slice 8's TER
    and worth its own dedicated slice.
  - **`BlockDestabilizerBlock`:** needs a custom `Entity` (`EntityFallingBlockSpecial` in 1.12.2) that
    falls in an arbitrary configurable direction, not just down like vanilla's `FallingBlockEntity`/
    `BlockFalling` - real new entity-registration, `DataManager`-synced, custom-movement-physics work,
    not a quick addition.
  - **The "sound pattern" family (`SoundRecorderBlock`, `ItemSoundPattern`, `SoundBoxBlock`,
    `SoundDampenerBlock`):** traced how a pattern item actually gets its recorded sound and found a
    whole `ContainerSoundRecorder` with a searchable/scrollable sound-registry-browser GUI behind it,
    plus (for the dampener half) a global sound-mute hook. A real, self-contained cluster of 4
    interdependent pieces - good candidate for one dedicated future slice, not a quick bolt-on.
  - **`ImbuingStationBlock` + `RuneBaseBlock`:** both need a real custom recipe system
    (`ImbuingRecipeHandler`/`recipes/imbuing/` in 1.12.2) - belongs with this plan's own "Recipes
    (crafting/anvil/imbuing)" future batch, not Batch 2.
  - **`BiomeRadarBlock`:** needs `ItemBiomeCrystal`/`ItemPositionFilter` (unported items), a custom
    particle effect class, and its own network message - a real standalone feature bundle.
  - **`BlockBreakerBlock`:** needs `ModEnchantments`, which doesn't exist yet in this port (no
    enchantment system ported at all so far).
  - **`FluidDisplayBlock`:** needs real research into 1.14.4's fluid API, flagged as a real unknown
    since Batch 1 (`PitcherPlantBlock`'s fluid-filling mechanic was dropped for the same reason) - still
    unresearched, still a genuine open question rather than a quick task.
  - **Everything else remaining** was already explicitly identified as its own deferred subsystem from
    this plan's original ASM-mapping section, unchanged since then: Spectre energy network
    (`SpectreCoilBlock`, `SpectreEnergyInjectorBlock`, `SpectreLensBlock`), Ender network
    (`EnderAnchorBlock`, `EnderBridgeBlock`, `EnderMailboxBlock`, `PrismarineEnderBridgeBlock`,
    `LinkOrbBlock`), Floo network (`FlooBrickBlock`), wireless Redstone Interface, Rain Shield, and Peace
    Candle worldgen - all need Mixin work and/or worldgen and were never really Batch 2's tactical scope.
  This session ported **14 slices covering ~34 new blocks/items** (repeater, iron dropper, custom
  workbench, player interface, inventory tester, inventory rerouter, chat detector, redstone observer +
  redstone tool, potion vaporizer, special chest (+ this port's first custom `TileEntityRenderer`),
  entity detector, entity filter item + filtered redirector plate, slime cube + advanced item collector,
  filtered super lubricent platform, notification interface (+ this port's first targeted server→client
  network message and first custom toast), id card + global chat detector), each individually
  `./gradlew build`-verified. Batch 2's remaining scope is now entirely the subsystem/recipe/worldgen/
  rendering work above, each item deserving its own dedicated future slice rather than a rushed add-on.
- **Known gap noticed in passing, not yet fixed:** none of Batch 1's ~50 standalone blocks
  (`contact_button`, the `plates/` family, `spectretree/`, etc.) have a loot table, meaning they
  currently drop nothing when broken in survival. Batch 2's new blocks all got loot tables as part of
  this slice; backfilling Batch 1's is a good candidate for its own small cleanup slice.

### Batch 3 — remaining (non-block) items

Scope per the original roadmap: everything left in `item/` (~50 1.12.2 classes) that isn't a `BlockItem`
already covered by Batch 1/2. Also folds in a post-Batch-2 bug-fix pass (see below) before starting.

- **Post-Batch-2 bug-fix pass (this session, before Batch 3 started):** working through
  `TESTING_CHECKLIST.md` surfaced and fixed real regressions: Contact Lever/Button's `activate()`
  trigger was never wired to anything (right-clicking the block they're mounted against now correctly
  triggers them, restoring 1.12.2's "hidden pressure sensor" mechanic via a new `PlayerInteractEvent
  .RightClickBlock` listener in `RandomThings.java`); Contact Button's model was parented to vanilla's
  thin button shape instead of the original's full-cube `orientable` shape; Luminous Stained Brick's
  block/item model only referenced its glow overlay texture, never the opaque base layer underneath
  (fixed with a proper two-element composited model, then had to add back `"parent": "block/block"`
  after that same fix accidentally dropped the standard GUI/hand display-transform inheritance, causing
  oversized/misrendered held items); Compressed Slime Block had a stray `asItem()` override hijacking
  its identity to vanilla's Slime Block item, AND the actual creation mechanic (right-click a Slime
  Block with a shovel to compact it, again to compact further, 3 levels) was never ported at all so
  there was no way to ever obtain one - both fixed, plus added back the original's per-compression gray
  tint via `IRTBlockColor` (was dropped from scope in the initial port); Spectre Log was missing its
  crafting-into-planks recipe entirely; Spectre Sapling's tree-growth placed every log/leaf block with
  a `setBlockState` flag that updates the server but never syncs to the client, causing "phantom
  blocks" until a reload re-synced the chunk (fixed to the standard notify+sync flag); Spectre Sapling
  and (found proactively while fixing it) Glowing Mushroom/Pitcher Plant's item icons rendered as 3D
  crossed-plane "sprites" in hand because their item models parented straight to the block's
  `block/cross` model instead of a flat `item/generated` icon like vanilla saplings use. Also ported
  the previously-deferred `ectoplasm` item and gave Spectre Leaf a real loot table (had none - silently
  dropped nothing). **Investigated and confirmed NOT bugs, no code changed:** Lapis/Quartz Lamp's
  "always on" behavior matches the real 1.12.2 source (`BlockLapisLamp.getLightValue` unconditionally
  returns 15 - it was never redstone-controlled); the whole plate family's mostly-transparent,
  see-through-to-the-block-below texture style is the actual original 1.12.2 design (checked
  `plate_accelerator.json` - a paper-thin decal using a ~77%-transparent icon texture), not a
  regression. **Deliberate deviation from 1.12.2, per your explicit direction:** Spectre Leaf's loot
  table drops Ectoplasm only - the real 1.12.2 source also had an independent chance to drop a Spectre
  Sapling from leaves (confirmed by reading `BlockSpectreLeaf.getDrops`), but you asked for
  Ectoplasm-only, so that's what's implemented; flagging this specific divergence since every other
  fix this pass matched the original exactly.
- **Slice 1 (done): four self-contained items with zero subsystem dependency.**
  - `BeanStewItem` - plain food item (8 hunger), returns an empty bowl to the player's inventory (or
    drops it) when finished eating, direct port.
  - `BlazeAndSteelItem` - a Flint and Steel variant that places `BlazingFireBlock` instead of vanilla
    fire; ground-truthed the modern `Item.onItemUse(ItemUseContext)` signature against vanilla's own
    `FlintAndSteelItem` via `javap`.
  - `BottleOfAirItem` - drink while submerged to top up your air meter every 5 ticks while held-used.
    1.12.2 achieved "hold right-click indefinitely" via reflection into a private
    `activeItemStackUseCount` field (no clean API existed then); confirmed via `javap` that `Item` in
    this Forge build has no `onUsingTick` hook at all, so this port instead uses `getUseDuration`
    returning a large value (drink action never actually "finishes") combined with `inventoryTick`
    checking `LivingEntity.isHandActive()`/`getActiveItemStack()` each tick - no reflection needed, and
    arguably cleaner than the original's workaround.
  - `StableEnderpearlItem` - bind it to yourself, throw it, and 140 ticks after it lands you're pulled
    to its resting position (or, unbound, it randomly teleports the nearest living entity within 10
    blocks). **Disclosed simplification:** 1.12.2 additionally force-transferred the bound player's
    dimension if the dropped pearl ended up in a different one than them (e.g. near a portal); 1.14.4's
    dimension/teleportation API is a substantial rewrite from 1.12.2's `SimpleTeleporter`-based one,
    and this rare edge case wasn't worth porting in isolation - same-dimension binding (by far the
    common case) is unaffected. **Notable API-gap finding:** confirmed via `javap` that
    `Item.onEntityItemUpdate(EntityItem)` - the 1.12.2 hook this item used to tick its landed-pearl
    countdown - doesn't exist on `Item` in this Forge build at all. Replaced with a
    `TickEvent.WorldTickEvent` listener in `RandomThings.java` that scans `ServerWorld.getEntities()`
    for dropped stacks of this item each server tick and drives the countdown from there - the same
    "centralized listener in `RandomThings`" pattern already used for Chat Detector/Slime Cube/Redstone
    Observer.
  - All four compile- and full-build-verified (`./gradlew build`) first attempt after `javap`-checking
    every uncertain signature (`ActionResult` lives in `net.minecraft.util`, not `net.minecraft.item`;
    `Item`'s random-number field is `random` not `rand`; `Entity.posX/Y/Z` are still plain public fields
    in this build, not getters; `World.getEntitiesWithinAABB(Class, AABB, Predicate)` requires the
    predicate arg in this version, no 2-arg overload; entity spawning is `ServerWorld.addEntity`, not
    `World.addEntity`, consistent with earlier batches' finding).
  - Real 1.12.2 textures/models copied for all four; lang entries added, including the "Bound to %s"
    tooltip.
- **Full remaining-items survey (this session), for scoping future slices:** read every one of the
  ~50 still-unported item classes' imports/structure to sort them:
  - **Its own dedicated future slice, real scope, not a quick add-on:** the "player passive equipment
    effects" cluster - Obsidian Skull (+ Baubles-based Obsidian Skull Ring, needs a Baubles-replacement
    design decision since third-party compat is dropped), Lava Wader (charge-based fire immunity) +
    Lava Charm, Water Walking Boots + Obsidian Water Walking Boots, Super Lubricent Boots. Traced their
    actual mechanics (the boot/skull item classes themselves are nearly empty - all the real logic
    lives in ~150 lines of `RTEventHandler.livingAttacked`/a player-tick handler checking equipped
    items) - a real, interconnected subsystem worth its own scoping pass, not bundled into "simple
    items."
  - **Golden/Emerald Compass:** both use the old `IItemPropertyGetter`/`addPropertyOverride` item-model
    predicate system (1.14.4 has a different, not-yet-used-anywhere-in-this-port API for animated item
    model properties) - real new client-rendering infrastructure needed. Also noted Golden Compass's
    tracking logic looks incomplete even in the original (nothing ever sets its target NBT tags) -
    worth double-checking against a live 1.12.2 reference before assuming what "done" looks like.
  - **Obsidian Skull/Rez Stone are actually a mini-subsystem, not simple items:** despite both classes
    being nearly empty, they're wired to `EntitySoul` (a new custom entity implementing a
    death/resurrection mechanic) - deferred with the other new-entity-needing items below.
  - **Needs a new custom `Entity` (deferred with Batch 2's already-identified `BlockDestabilizer`,
    same category of work):** `ItemEclipsedClock` (`EntityEclipsedClock`), `ItemWeatherEgg`
    (`EntityThrownWeatherEgg`), `ItemTimeInABottle` (`EntityTimeAccelerator`), Rez Stone/Obsidian Skull
    (`EntitySoul`, above).
  - **Belongs with an already-deferred subsystem, confirmed by imports, not re-analyzed in depth:**
    Floo network (`ItemFlooPouch`/`ItemFlooSign`/`ItemFlooToken`), Ender network-adjacent
    (`ItemEnderBucket`/`ItemReinforcedEnderBucket`/`ItemEnderLetter`), Imbuing/Rune system
    (`ItemImbue`/`ItemRuneDust`/`ItemRunePattern`), Spectre energy/dimension
    (`ItemSpectreAnchor`/`ItemSpectreCharger`/`ItemSpectreIlluminator`/`ItemSpectreKey`), sound-pattern
    family (`ItemSoundPattern`/`ItemSoundRecorder`/`ItemPortableSoundDampener`), BiomeRadar
    (`ItemPositionFilter`/`ItemBiomeCrystal`), the full `ItemItemFilter` (9-slot whitelist/blacklist
    filter config item - Batch 2 already shipped several blocks with a simplified single-item-slot
    filter instead, noted at the time as a placeholder for this).
  - **Needs real design/research before starting, not a quick port:** `ItemDungeonChestGenerator` (used
    1.12.2's `LootTableList.getAll()` in-memory registry listing, which doesn't exist the same way in
    1.14.4's data-driven loot table system); `ItemRedstoneActivator`/`ItemRedstoneRemote` (tied to the
    still-deferred wireless Redstone Interface subsystem, needs the Mixin work noted back in this
    plan's original ASM-mapping section); `ItemChunkAnalyzer`/`ItemCraftingRecipe`/`ItemEnderLetter`
    (all use the old numeric `GuiIds` custom-GUI system - each needs its own `Container`/`Screen` pair
    scoped individually, not a batch).
  - **Plausible next self-contained candidates, not yet scoped in detail:** `ItemObsidianSkull`/
    `ItemRezStone` were initially miscategorized as "trivial" from their near-empty class bodies before
    tracing their real dependency on `EntitySoul` - a reminder to keep verifying actual behavior via
    cross-references, not just class size, when scoping future slices here.
- **Slice 2 (done): the "player passive equipment effects" cluster.** Traced the actual mechanics into
  `RTEventHandler` (they're barely in the item classes at all) and ported the whole interlocking set:
  - `ObsidianSkullItem`, `LavaCharmItem` - both work simply by being carried anywhere in the player's
    inventory (matched by a new `InventoryUtil.getPlayerInventoryItem` helper, direct port of the
    original). **Disclosed simplification:** both could alternatively be worn in a Baubles amulet slot
    in 1.12.2; since third-party compat (including Baubles) is dropped for this port and 1.12.2's own
    logic already fell back to the inventory-carried check when no Baubles slot was equipped, that
    fallback is all this port needs - both items still fully work. **`ItemObsidianSkullRing` stays
    deferred** - unlike the two above, it had no non-Baubles fallback at all in the original (bauble
    slot only), so there's no equivalent behavior to port without inventing a new mechanic.
  - `LavaWaderItem` - boots with an `onArmorTick`-driven 0-200 charge meter (regenerates 1/tick off a
    40-tick post-use cooldown) that a shared `LivingAttackEvent` listener in `RandomThings` spends to
    fully cancel `DamageSource.LAVA` hits.
  - A second effect on the same `LivingAttackEvent` listener: Obsidian Skull (inventory-carried),
    Obsidian Water Walking Boots, and Lava Wader (both worn) each give a chance - scaling as
    `amount³/100`, so small burns are usually blocked and a big single hit usually isn't - to cancel
    *any* fire-type damage, not just lava.
  - `WaterWalkingBootsItem`, `ObsidianWaterWalkingBootsItem`, plus Lava Wader again for lava: a shared
    `LivingEvent.LivingUpdateEvent` listener nudges the wearer upward each tick while they're jumping
    over the relevant liquid with clear air above, instead of letting them sink - a repeated small hop
    rather than true buoyancy, matching 1.12.2 exactly. Client-side only (movement prediction, not
    server-arbitrated physics), and driven off each entity's own synced `isJumping` flag so it works
    for any wearer you can see, not just the local player. **Notable API-gap finding:** confirmed via
    `javap` that `LivingEntity` has a public `setJumping` but no public getter for that same field in
    this Forge build - used plain reflection into the field by its real name (simpler than 1.12.2's
    version, which needed an obfuscation-name lookup on top).
  - `SuperLubricentBootsItem` - no logic of its own; `SuperLubricentIceBlock`/`PlatformBlock`/
    `StoneBlock` each got a `getSlipperiness` override (the entity-aware 4-arg one Forge exposes
    directly - confirmed via `javap` this needs zero ASM/Mixin here, matching this plan's original
    ASM-mapping note) that returns normal ground friction when the entity's FEET slot holds these
    boots, instead of the block's own near-ice slipperiness value.
  - All Java compiled clean, and the full `./gradlew build` (including resource processing) succeeded,
    on the **first attempt** for the whole slice - every uncertain 1.14.4 signature (`ArmorItem`
    constructor args, `IForgeItem.getArmorTexture`/`onArmorTick`, `Block.getSlipperiness`'s 4-arg
    overload, `EquipmentSlotType`, `LivingAttackEvent`/`LivingEvent.LivingUpdateEvent`,
    `DamageSource.LAVA`/`isFireDamage()`) was `javap`-checked against the real jar before writing the
    code that used it.
  - Real 1.12.2 textures (both the 2D item icons and the 3D worn-armor textures) copied for all 6 new
    items; lang entries added.
- **Slice 3 (done), 2026-09-25: Golden/Emerald Compass, chosen by the user over the Obsidian Skull/Rez
  Stone `EntitySoul` alternative.** Real scope surprise found before writing any code: reading the
  original `ItemGoldenCompass.java` closely showed it only ever *reads* `targetX`/`targetZ` NBT, never
  sets it - confirmed via `git grep` that the actual target-setting logic lives entirely in a custom
  crafting recipe in `ModRecipes.java` (`goldenCompassSetPosition`: combine the compass with an
  `ItemPositionFilter`, itself a *new* small item - right-click a block to remember its position). Same
  pattern for Emerald Compass (`emeraldcompass_settarget`: combine with an `ItemIDCard` to bind a player
  UUID) - but `IdCardItem` **already exists** in this port (added silently as part of Batch 2's Entity
  Detector work), so that half needed no new item at all, just reuse. Ported all of it as one slice
  rather than shipping a compass that could never actually be given a target:
  - `PositionFilterItem.java` - new, small, self-contained (right-click a block to store dimension+x/y/z
    in its own NBT, shift-tooltip to view it). Direct port of `ItemPositionFilter`; dropped the original's
    `Features.HIDE_CORDS` config gate since no config system has been ported anywhere in this project yet.
  - `CompassItemBase.java` - new shared base class for the needle-angle math (both compasses had
    byte-for-byte identical `IItemPropertyGetter` logic in 1.12.2 - extracted rather than duplicated,
    a judgment call, not a behavior change). **Positive surprise: 1.14.4 still has `Item.addPropertyOverride`
    and `IItemPropertyGetter` in essentially the same shape as 1.12.2** (method renamed `apply`→`call`,
    params `World`/`LivingEntity` instead of `IWorldReader`-ish/`EntityLivingBase`, everything else
    identical down to the exact math) - this plan's own earlier note calling this "real new client-rendering
    infrastructure" was an over-cautious assumption made before actually checking; it wasn't needed.
    `javap`-confirmed the item-model JSON `overrides`/`predicate` format is also byte-for-byte unchanged
    from 1.12.2 by diffing against vanilla's own bundled `compass.json`.
  - `GoldenCompassItem.java`/`EmeraldCompassItem.java` - thin subclasses; Emerald additionally overrides
    `inventoryTick` (re-resolves its bound UUID to a live position once/second via
    `ServerWorld.getServer().getPlayerList().getPlayerByUUID`, same pattern as Batch 3's own Stable
    Enderpearl) and `shouldCauseReequipAnimation` (exists on `IForgeItem` in this Forge build, unlike some
    other 1.12.2 `Item` overrides found missing in earlier slices).
  - `GoldenCompassSetPositionRecipe.java`/`EmeraldCompassSetTargetRecipe.java` (new `recipes/` package,
    first custom recipe in this port) - ported as `SpecialRecipe`/`SpecialRecipeSerializer` subclasses,
    the modern 1.14.4 replacement for 1.12.2's `IRecipe`+`RecipeSorter` custom-recipe pattern (confirmed
    the shape via `javap` on vanilla's own `MapCloningRecipe`). Registered through a new
    `RegistryEvent.Register<IRecipeSerializer<?>>` handler in `RandomThings.RegistryEvents`, following
    the same per-type registry-event pattern already used for containers/tile-entity-types/features. Each
    still needs a trivial `{"type": "randomthings:..."}` data-pack recipe JSON to actually instantiate
    (SpecialRecipe logic is Java-only; the JSON just declares that the type exists). Both correctly leave
    the "input" item (Position Filter / ID Card) unconsumed via `getRemainingItems`, matching the
    originals - **dropped one leftover, clearly-irrelevant condition** from the emerald recipe's
    `matches()` (a check for a `"spectreAnchor"` NBT tag on the ID card, copy-pasted from an unrelated
    recipe right above it in the original `ModRecipes.java` - always true once the item's already
    confirmed to be an ID card, flagged in code rather than silently ported as dead weight).
  - Assets: all 64 real texture frames (32 per compass) and all 62 real per-frame item models copied
    byte-for-byte from 1.12.2 (`git show origin/1.12.2:...`), renamed to this project's snake_case asset
    convention (`golden_compass_17.png`, not `goldencompass_17.png`); the two 33-entry master override
    models were regenerated (not copied) since only the resource-location prefixes changed, verified via
    a Python JSON-parse sanity check rather than by eye. Base crafting recipes ported as plain shaped
    recipes with plain vanilla item keys (gold ingot / emerald / purple dye), matching this project's
    established simplification of dropping 1.12.2's ore-dictionary tags now that third-party compat is
    out of scope. Lang entries added; skipped the original's unused `item.*.info` flavor-text keys
    (confirmed via grep this key style is never displayed by anything in this port - no JEI-equivalent).
  - Full `./gradlew build -x test` verified clean. Added `TESTING_CHECKLIST.md` rows #136-141 for all
    three new items' behaviors (including the "unset compass wobbles randomly", "bound player logs off ->
    wobbles again" and "input item not consumed" edge cases) - not yet tested in-game.
- **Slice 3 bug fix (user report): Golden Compass pointed at the target block's corner, not its center.**
  `CompassItemBase.getAngleToPos` was computing the bearing straight to the raw `targetX`/`targetZ`
  `BlockPos` values; added a `+0.5` offset on both X and Z so the needle aims at the block's center
  instead. Deliberately fixed in the angle math, not in what gets saved - `PositionFilterItem` still
  records the exact block position it was used on unchanged, only the compass's own aiming point moved.
  Full `./gradlew build -x test` verified clean. `TESTING_CHECKLIST.md` #138 updated; still needs an
  in-game retest along with the rest of #136-141.
- **Slice 3 in-game results, 2026-09-25: 5/6 pass, one real bug found and fixed.** #137-141 (both
  compasses' wobble/bind/track/target-loss behaviors, including the corner-vs-center fix above) all
  `PASS`. #136 (Position Filter) `FAIL`: "Crafting recipe does not work, texture ... purple/black
  checkered." The texture half was a genuine, concrete oversight - unlike the two compasses,
  `models/item/position_filter.json` and its texture file were never actually created in the original
  slice, despite the item class and registration being done; `find` confirmed neither file existed.
  Fixed: copied the real 1.12.2 texture (`git show origin/1.12.2:.../positionfilter.png`,
  byte-identical) and added the model json, same pattern as every other item this project has ported.
  The recipe half is unconfirmed as an actual bug - the JSON parses fine and uses the same plus-shape
  pattern style as the compass recipes (which crafted successfully), and `minecraft:purple_dye` is a
  real, valid item id - likely the user reached for Magenta Dye instead of Purple Dye (similar-looking
  but different vanilla colors); flagged in the checklist for a specific retest rather than guess-fixing
  a recipe that looks correct. Full `./gradlew build -x test` verified clean.
- **Slice 4 (done), 2026-09-25: Escape Rope, chosen by the user after Magic Hood/Escape Rope/Grass
  Seeds/Port Key were all surveyed and found more complex than their size suggested (see the AskUserQuestion
  exchange this session for the full survey - Magic Hood specifically needs a scoreboard-team workaround
  since this Forge version has no `RenderNameTagEvent`, contrary to this plan's own early "De-ASM-ifying"
  audit assumption; Port Key needs a whole disguise-as-another-item dynamic model system; Grass Seeds
  needs a new 16-variant block).** Escape Rope itself: hold right-click under open sky's reach to search
  outward (flood-fill/DFS, throttled to 4 block-checks/server-tick) for the nearest path to daylight,
  then teleport there.
  - `EscapeRopeItem.java` - the item/use-action/particle-while-charging half. **Disclosed
    simplification:** the original spawned a custom spiraling yellow smoke particle
    (`EntityColoredSmokeFX`, a bespoke particle class); this port has no custom-particle infrastructure
    yet (nothing else needed one), so it reuses vanilla `ParticleTypes.PORTAL` for the same "charging"
    visual cue instead of building particle registration just for this one cosmetic effect.
  - `util/EscapeRopeHandler.java` (new package placement, alongside `InventoryUtil` - no `handler/`
    package precedent exists in this port, that whole 1.12.2 package folded into `RandomThings.java`
    elsewhere) - the actual search algorithm, a near-verbatim port. API updates: `isChunkGeneratedAt`
    -> `chunkExists`, `getCollisionBoundingBox(...) == null` -> `getCollisionShape(...).isEmpty()`
    (`VoxelShape` replaced `AxisAlignedBB` for this purpose), `isSideSolid(UP) || isFullBlock()` ->
    `BlockState.isSolid()` (not pixel-identical to the old check but the standard modern "can stand on
    this" analog), `EnumFacing.HORIZONTALS` -> `Direction.Plane.HORIZONTAL` (implements `Iterable`,
    confirmed via javap). Ticked from a new `TickEvent.ServerTickEvent` listener in `RandomThings.java`
    (matches the original's own call site exactly - server-tick, not world-tick, so it runs once
    regardless of how many dimensions are loaded).
  - **Caught two of my own port mistakes via direct comparison against the original before calling this
    done, not after:** (1) first draft dropped the `iterator.remove()` call when a task finishes
    (success or failed-search) - would have silently leaked a `Task` object into the list forever per
    use, never actually breaking anything visibly but never cleaning up either; caught by re-reading the
    original's control flow line-by-line rather than trusting my own restructuring. (2) first draft used
    `world.playSound(null, ...)` for both of the original's two `playSound` calls; the original's first
    call deliberately excludes the traveling player (`playSound(actualPlayer, ...)`) since their own
    client already predicts/plays the teleport sound once it receives the new position, while the second
    call (after teleporting) uses `null` so they hear their own arrival - fixed to match, with a comment
    explaining why the two calls differ so a future re-read doesn't "simplify" them back to being
    identical.
  - Assets: real 1.12.2 texture copied byte-for-byte; base crafting recipe ported with the same
    ore-dict-tag-to-plain-vanilla-item simplification already established (`ingotGold` tag ->
    `minecraft:gold_ingot`). No custom recipe needed (unlike the compasses) - Escape Rope's target is
    wherever daylight happens to be, not something craftable/settable.
  - Full `./gradlew build -x test` verified clean; `unzip -l` confirmed all new assets packaged.
    `TESTING_CHECKLIST.md` #142-144 added, not yet tested in-game.
- **Post-Batch-3 tweak (user request, not from the checklist): Super Lubricent Platform now has true
  zero friction ("you do not slow down"), no Mixin needed.** The user expected this would need a Mixin
  patching the player's friction calculation. `javap -c` disassembly of `LivingEntity.travel` (the
  method has no source/docs to read otherwise) showed vanilla itself multiplies horizontal motion by
  `blockSlipperiness * 0.91F` every tick as its friction step - `0.91F` is a hardcoded constant, but
  `slipperiness` is a free multiplicand against it, so setting the block's slipperiness to exactly
  `1F / 0.91F` makes that product exactly `1.0F`: perfect momentum retention, entirely through the
  existing `Block.Properties.slipperiness` mechanism already in use. Changed from the previous
  `1F / 0.98F` (which matched vanilla ice's own field value, inverted - close, but not actually zero
  friction). Documented the derivation and one real consequence worth knowing directly in
  `SuperLubricentPlatformBlock`'s class javadoc: true zero friction also means holding a movement key
  on it now accelerates without any steady-state cap for as long as you hold it (the friction that used
  to counterbalance acceleration into a top speed is gone) - an inherent property of zero friction, not
  a bug, but a real behavior change worth knowing about. Scoped to the Platform only, per the request -
  Super Lubricent Ice/Stone still use their original values. Full `./gradlew build` verified clean.
- **Follow-up: added a max-speed cap to Super Lubricent Platform, explicitly NOT a 1.12.2 port.** User
  recalled 1.12.2 having one; traced the real ASM injection site in full
  (`ClassTransformer.patchEntityLivingBase`'s `0.91F`-constant search -> `AsmHandler.slipFix`) and
  confirmed it only ever forces the friction factor to `1.0F`, nothing else - searched the rest of
  `AsmHandler`'s full method list, both block classes, and the whole 1.12.2 source for any
  `maxSpeed`/`clampSpeed`-shaped code and found none. Original was genuinely uncapped. Per explicit
  direction after being told this, added a new `onEntityCollision` override that clamps horizontal
  speed to `1.0` blocks/tick (~72 km/h, a tunable constant) - a deliberate new feature, documented as
  such in the block's javadoc so this isn't mistaken for a missed port detail later. Full
  `./gradlew build` verified clean.
- **Follow-up: retuned the speed cap to ~0.35 blocks/tick ("slightly faster than sprinting") and
  extended zero-friction + cap + boots-negation to Ice and Stone too**, per user request. Extracted the
  shared logic into a new `SuperLubricentPhysics` helper (`applyBootsOverride`/`capHorizontalSpeed`)
  used identically by all three blocks. Initially wired `capHorizontalSpeed` through each block's
  `onEntityCollision` override (mirroring the original Platform-only implementation) - **this turned out
  to be completely non-functional**, confirmed by direct user testing ("going super fast on all of the
  lubricent blocks"). Root cause: `onEntityCollision` only fires when an entity's hitbox actually
  *overlaps* a block's collision volume - never true for an entity simply resting on top of a block
  (feet at the surface, not inside it), so the cap silently never ran on any of the three blocks, likely
  since the very first Platform-only version. Fixed by moving the cap into a new
  `LivingEvent.LivingUpdateEvent` listener in `RandomThings`'s constructor that looks up the block
  directly underfoot each tick using the exact same position formula vanilla's own friction code uses in
  `LivingEntity.travel` (one full block below the entity's bounding box's `minY`), confirmed via
  `javap -c`. Registered unconditionally (both sides), unlike the nearby water-walking listener which is
  client-only by design. Removed the now-dead `onEntityCollision` overrides from all three blocks; made
  `SuperLubricentPhysics` and `capHorizontalSpeed` `public` for the cross-package call from `RandomThings`.
  Full `./gradlew build -x test` (via `source env/activate.sh`, see "Toolchain note") verified clean.
  **User confirmed this fully fixed the speed cap on all three blocks.**
- **Follow-up: fixed Super Lubricent Boots, which had the exact opposite of their real behavior.** The
  previous port implemented the boots as negating slide on the three Super Lubricent blocks specifically
  (`SuperLubricentPhysics.applyBootsOverride`, called from each block's `getSlipperiness`). User reported
  the actual in-game effect should be the reverse: wearing the boots makes EVERY surface (not just the
  three Super Lubricent blocks) maximally slippery, with sneaking as the only way to stop sliding.
  Ground-truthed the real 1.12.2 source (`AsmHandler.slipFix`, an ASM patch never previously read for
  this feature) and confirmed: it returns `1F` (max slip) whenever the block is `ISuperLubricent` OR the
  entity wears the boots and isn't sneaking - i.e. always block-agnostic for the boots case, not scoped
  to the three custom blocks at all. A per-block `getSlipperiness` override structurally cannot reproduce
  "every surface," regardless of its internal logic, since it only ever runs for the blocks that define
  it. Since no Forge event exists for block slipperiness in this Forge version, fixed with a new Mixin,
  `SuperLubricentBootsMixin` (`@Mixin(LivingEntity.class)`, `remap = false`, matching the existing
  `FireBlockMixin` convention), `@Redirect`-ing the single `BlockState.getSlipperiness(...)` call site
  inside `LivingEntity.travel` (confirmed via `javap -c` there's exactly one, reading the block directly
  underfoot) to return `1F / 0.91F` whenever the entity wears the boots and isn't sneaking, regardless of
  which block state was passed in. Registered in `randomthings.mixins.json`. Removed the now-dead
  `applyBootsOverride` from `SuperLubricentPhysics` and the `getSlipperiness` overrides it was called
  from on all three blocks (down to just their `Properties.slipperiness(...)` constructor calls now -
  the speed-cap listener from the previous entry is untouched and still scoped to only the three blocks,
  matching 1.12.2's own boots behavior being uncapped off those blocks). Full `./gradlew build -x test`
  verified clean; Mixin application itself (vs. just compilation) still needs an in-game check from the
  user, same as any Mixin change.
- **STILL BROKEN as of 2026-09-24, logged per explicit user request to stop iterating blindly and hand
  off to next session:** user retested `SuperLubricentBootsMixin` and reported "The boots still do not
  change anything about the player movement" - no effect at all, not even a partial/wrong one. This is a
  DIFFERENT failure mode than the backwards-logic bug it replaced (that one visibly did something, just
  the wrong thing); zero observable effect points at the Mixin not applying/firing at all, not a logic
  bug inside `randomthings_bootsMaxSlip` itself.
  - **Do not just re-read/re-reason about the Java source again** - that was already done twice this
    session and produced a plausible-looking but non-functional result each time. The bug is almost
    certainly in the Mixin *application* layer (config, target resolution, transform failure), which
    static code reading can't diagnose - it needs runtime evidence.
  - Concrete next steps, roughly in order of how much they'd tell us:
    1. **Get the user's actual game log** (or have them run the client themselves and paste output) and
       search it for `Mixin apply failed`, `randomthings.mixins.json`, `SuperLubricentBootsMixin`, or any
       SpongePowered Mixin errors/warnings at startup - this alone would likely show a silent failure if
       there is one (wrong target descriptor, `LivingEntity.class` resolving to the wrong class file at
       runtime, refmap mismatch, etc).
    2. **Already checked (2026-09-24): `TESTING_CHECKLIST.md` rows #69/#70 (Blazing Fire - the ONLY
       other Mixin, `FireBlockMixin`, in this codebase) both have BLANK Result columns - never tested,
       confirmed working, or confirmed broken by the user at all.** So there is currently zero evidence
       ANY Mixin has ever actually applied successfully in this project, which meaningfully raises the
       odds of a systemic pipeline issue (AP config, mixins.json wiring, `addMixinsToJar` task,
       `-mixin.jar`/manifest setup) over a bug specific to the boots redirect. Getting the user to test
       Blazing Fire (place fire near wood/wool with Blaze and Steel, #122) alongside the boots would be a
       cheap way to tell these two hypotheses apart - if Blazing Fire ALSO does nothing special, treat it
       as a pipeline-wide problem, not a boots-specific one.
    3. Re-verify the exact `@Redirect` target descriptor against the SAME mapped jar used for compiling
       (`forge-1.14.4-28.0.49_mapped_snapshot_20190819-1.14.3-recomp.jar`) is *also* what's present in
       the actual runtime/launch classpath - FG3 dev-launch sometimes resolves a different
       (non-recomp/obfuscated) jar than what's used for `javap`, which would make a `remap = false`
       target silently fail to match anything at runtime even though it compiles fine (Mixin's
       AP-time validation is much weaker under `remap = false` than under `remap = true`).
    4. If the redirect target truly can't be found/matched, consider `@ModifyVariable` on the local
       slipperiness variable instead of `@Redirect`-ing the method call itself (a different, sometimes
       more robust injection strategy for the same effect), or an `@Inject` at `RETURN`/`TAIL` that
       recomputes and overwrites the friction result directly.
    5. As a last resort if Mixin proves unworkable in this dev environment for some environment-specific
       reason, fall back to the per-tick `LivingUpdateEvent` technique already proven to work for the
       speed cap: directly counteract vanilla friction by reading the entity's velocity before/after
       `travel()` runs isn't possible without Mixin either, BUT a coarser approximation (each tick, if
       the entity is grounded, not sneaking, wearing the boots, and NOT on a block whose own
       `getSlipperiness` is already near 1.0, nudge horizontal motion back toward its pre-friction value
       by dividing out an assumed 0.6 vanilla-ground-slipperiness factor) could get close without
       needing byte-exact interception - flagged as a last resort since it's approximate, not exact like
       the 1.12.2 original.
  - Do not repeat the mistake from the speed-cap bug: don't report this fixed again without either (a)
    direct user confirmation after a fresh rebuild, or (b) concrete runtime evidence (a log line,
    behavior a static read of the Java can't produce) that the new approach actually changes something.
- **Testing-checklist pass, 2026-09-24 (later):** worked the three `TESTING_CHECKLIST.md` `FAIL` rows
  that had never been touched (#76, #83, #95), per the standing "check the checklist for new FAILs first"
  rule, since the Mixin bug above needs the user's runtime evidence before it can move further.
  - **#95 Redstone Observer, FIXED:** "does not work if the target is weakly powered, only strongly
    powered." `RedstoneObserverTileEntity.updateRedstoneState()` read weak power via
    `targetState.getWeakPower(world, target, side)` directly - that only returns power a block emits
    itself (levers, torches, redstone wire's own level), not power it's merely conducting from a
    neighbor. `javap -c` on `World.getRedstonePower(BlockPos, Direction)` confirmed this is the actual
    vanilla method for "effective power in a direction," which checks `BlockState.shouldCheckWeakPower`
    and falls back to aggregate strong power for ordinary blocks (only wire-like blocks that opt out get
    their own `getWeakPower` read directly). Swapped the call to `this.world.getRedstonePower(target, f)`.
    Strong-power reading was already correct and untouched.
  - **#76 Item Collector, half-fixed/half-investigated:** two separate complaints in one row.
    - Vacuum not working: ground-truthed 1.12.2's `TileEntityItemCollector.update()`
      (`origin/1.12.2:.../tileentity/TileEntityItemCollector.java`) and found the port fetched the
      target's `IItemHandler` capability with `facing.getOpposite()` as the side, but the original used
      plain `facing` for the actual `getCapability` call (only its existence-check `hasCapability` used
      `.getOpposite()`, inconsistently, in the original too). Most inventories (chests) ignore the side
      argument so this mostly worked by luck, but side-filtering inventories would return no handler at
      all. Fixed in both `ItemCollectorTileEntity` and `AdvancedItemCollectorTileEntity` (same bug,
      copy-pasted into both).
    - Texture "wrong (black/purple checkered)": checked blockstate/model/texture for `item_collector` -
      byte-identical to the original 1.12.2 PNG, same `block/block` parent and UV layout (deliberately
      simplified to a flat plate vs. the original's antenna details, a disclosed simplification already
      noted elsewhere). Nothing in the checked-in assets explains a missing-texture render. Logged as
      `INVESTIGATING`, not guess-fixed - asked the user to retest after a genuinely fresh
      `./gradlew build`/`runClient` rather than a hot-reload, in case that's what produced the original
      report.
  - **#83 Iron Dropper, investigated, no bug found:** "texture is just an iron block." Checked
    `iron_dropper.json`/`iron_dropper_vertical.json` - both use the vanilla dispenser-style
    `block/orientable`/`block/orientable_vertical` parents with distinct front/side/top textures (arrow
    baked into the front texture), and all 4 source PNGs are byte-identical to the original 1.12.2 files.
    This is not a plain-iron-block texture in source. Same call as #76's texture half: logged
    `INVESTIGATING`, asked for a retest on a fresh build rather than a blind re-edit of already-correct
    assets.
  - Full `./gradlew build -x test` verified clean after all changes above.
- **Mixin root cause found, 2026-09-25:** the "STILL BROKEN" entry above says static code review had
  already failed twice and asked for runtime evidence instead. Got it by actually running
  `./gradlew runClient` (backgrounded, logs tailed/grepped rather than watched live) instead of
  re-reading the Java a third time.
  - First pass (plain `runClient`, no extra flags): client boots to the title screen with no crash and
    no Mixin-related log lines at all, success or failure - inconclusive on its own.
  - Added `property 'mixin.debug.verbose', 'true'` / `'mixin.debug.export', 'true'` to the client run
    config and relaunched: still nothing Mixin-specific logged, and `ModLauncher`'s own discovery lines
    (`Found launch plugins: [...]`, `Found transformer services : [fml]`) never listed anything
    Mixin-related either. Traced this to a real, separate, dev-environment-only gap: FML's own log
    explicitly states `Mod file .../build/resources/main is missing a manifest` - the ForgeGradle dev
    client loads the mod straight from the compiled classes/resources directory (no jar, so no
    `MANIFEST.MF` exists there at all), so the `MixinConfigs: randomthings.mixins.json` attribute
    declared in `build.gradle`'s `jar { manifest {...} } }` block is invisible to FML in dev - Mixin
    config registration is manifest-driven and never happens for the dev-client mod entry. Tried loading
    the real built jar from `run/mods/` instead to sidestep this, but ForgeGradle's `client` run config
    auto-adds the sourceSet-based dev-mod entry regardless of whether it's explicitly declared in
    `build.gradle`, so both loaded at once and FML crashed on a duplicate mod ID
    (`IllegalStateException: Duplicate key ...ModFileInfo...`) - didn't chase further since a proper fix
    needs real ForgeGradle config surgery, and it wasn't the productive direction (see below). Reverted
    the debug properties and the `run/mods` jar copy afterward - `build.gradle` is unchanged from before
    this investigation.
  - The actually productive direction: confirmed the **built production jar's manifest does correctly
    contain `MixinConfigs: randomthings.mixins.json`** (unzip + read `META-INF/MANIFEST.MF` from
    `build/libs/RandomThings-MC1.14.3-5.0.jar`), so the dev-environment manifest gap above is a real bug
    worth knowing about but is NOT what's breaking the user's actual testing (they test the real built
    jar in real Minecraft, not the raw dev client). That reframed the question: why would Mixin silently
    fail to apply even with a correct manifest? Re-read both Mixin classes with that framing and noticed
    both declare `remap = false`
    (`SuperLubricentBootsMixin.java`, `FireBlockMixin.java`) while targeting method names written in
    plain MCP/dev form (`"travel"`, `getSlipperiness(...)`, `"tryCatchFire"`, `getFlammability(...)`).
    `remap = false` tells Mixin to use those literal strings as the real runtime names instead of
    translating them through the refmap - correct only if the target already has a stable,
    never-obfuscated name in production. Checked the generated refmap
    (`build/tmp/compileJava/randomthings.refmap.json`) and it was completely empty (`"mappings": {}`),
    consistent with `remap = false` telling the annotation processor not to bother populating it for
    either class - but not proof by itself of which (if either) class actually needed remapping.
    Determined that by testing empirically: temporarily removed `remap = false` from both classes and
    rebuilt.
    - `SuperLubricentBootsMixin` (targets `LivingEntity.travel`): compiled with only a *warning* (not an
      error) - "Cannot find method mapping for @At(INVOKE.<target>) ...getSlipperiness(...)" - and the
      `method = "travel"` selector itself resolved cleanly. The regenerated refmap now contains a real
      entry: `"travel": "Lnet/minecraft/entity/LivingEntity;func_213352_e(Lnet/minecraft/util/math/Vec3d;)V"`
      - `func_213352_e` is `travel`'s actual SRG name, confirmed present in the built jar's packaged
      refmap too. This is the bug: with `remap = false`, the Mixin was telling the transformer to find a
      method literally named `"travel"` in the real (reobfuscated) `LivingEntity` class - which doesn't
      exist under that name in production, only as `func_213352_e` - so the `@Redirect` could never find
      its target and the whole mixin silently failed to apply. Explains every observed symptom at once:
      compiles clean (compiling only needs the MCP-mapped dev jar, where "travel" is a valid name), zero
      effect in-game (the real runtime class has no method called "travel"), and no crash (Mixin doesn't
      hard-fail the whole game over one missing-target mixin by default). **Fix: removed `remap = false`**
      (now defaults to `true`). The remaining `getSlipperiness` warning is expected and harmless -
      `javap`-confirmed that overload (`IWorldReader, BlockPos, Entity` params) is declared on Forge's own
      `IForgeBlockState`, not vanilla Mojang code, so it's never obfuscated in production and has no SRG
      mapping to find - referencing it unmapped is correct.
    - `FireBlockMixin` (targets `FireBlock.tryCatchFire`/`getFlammability`): removing `remap = false`
      here instead produced a hard **compile error** - "No obfuscation mapping for @ModifyArg target
      tryCatchFire" (twice) and the same warning-not-error pattern for `getFlammability`. Checked both
      with `javap`: `getFlammability(IBlockReader, BlockPos, Direction)` is declared on
      `IForgeBlock`/`IForgeBlockState` (Forge's own extension interfaces, not vanilla), and
      `tryCatchFire` doesn't appear anywhere in the raw notch↔SRG mapping table
      (`build/extractSrg/output.srg`) for `FireBlock` at all despite every genuinely-vanilla method
      (public or private) being present there - strong evidence it's a Forge-patch-added private helper
      method that was never part of Mojang's original obfuscated class, and so (like Forge's own
      interface methods) keeps its literal name unobfuscated in production too. **`remap = false` was
      already correct for this class - reverted the accidental change back**, so `FireBlockMixin` is
      untouched from before this session. This also **narrows the "project-wide Mixin pipeline problem"
      theory from the previous entry**: `FireBlockMixin`'s config was fine all along; #69/#70 (Blazing
      Fire) being untested was just... never tested, not evidence of a shared bug with #134.
  - Full `./gradlew build -x test` verified clean with the fix; `unzip`-confirmed the built jar's
    packaged refmap now has the real `travel` → `func_213352_e` entry. This is as far as static/build-time
    verification can go - genuinely needs the user to retest in-game, since "the mixin can now find its
    target" is provable from a build, but "the physics feel right" isn't.
- **Mixins cannot be tested in this dev environment at all - a real tooling limitation, 2026-09-25:**
  asked the user to retest #134 via `./gradlew runClient` (their only available environment - no real
  Minecraft 1.14.4 install exists). They reported "Blazing fire definitely has increased spread, but the
  Super Lubricent Boots still do nothing" - seemingly contradicting the remap fix above (if Mixins can
  run FireBlockMixin, why not SuperLubricentBootsMixin?). Added throttled diagnostic logging directly in
  `SuperLubricentBootsMixin.randomthings_bootsMaxSlip` (fires whenever the `@Redirect` actually runs) and
  had the user walk around wearing the boots - zero log lines despite confirmed gameplay. Escalated with
  an unconditional `@Inject(method = "travel", at = @At("HEAD"))` probe, completely independent of the
  `@Redirect`'s own target-matching - **also zero fires**, despite the user actively playing. This
  resolved the earlier contradiction instead of deepening it: re-read `BlazingFireBlock.java` and found
  `tickRate()` is overridden to return 15 instead of vanilla's 30 - plain Java, zero Mixin involvement -
  which fully explains "increased spread" on its own. The Blazing Fire observation was never actual
  evidence any Mixin applied in this environment.
  - Root-caused definitively via `javap -c` decompilation of FML's actual loader classes (extracted from
    `forge-1.14.4-28.0.49-launcher.jar`, not the recomp/mapped jar - FML's own loading classes live
    there): `ExplodedDirectoryLocator.findManifest(java.nio.file.Path)` - the locator FML uses for the
    dev-mode `sourceSets.main`-based mod entry (confirmed by log: `{ExplodedDir locator}`) - is
    **hardcoded** to `return Optional.empty()` unconditionally. No file written to disk can ever change
    this; it's not a caching or timing issue, the method plainly never attempts to read anything.
    Verified this is really the blocker (not a red herring) by writing a real `META-INF/MANIFEST.MF`
    (matching the `jar` task's own attributes, via a temporary `devManifest` Gradle task) into both
    `build/resources/main` and `build/classes/java/main`, rebuilding, and relaunching - FML still logged
    "is missing a manifest" for the exact same path, exactly as the hardcoded `Optional.empty()`
    predicts. Reverted `devManifest` entirely (it cannot work; removed from `build.gradle`, deleted the
    stray manifest files it wrote) rather than leave dead code behind.
  - Tried two workarounds, both dead ends:
    1. Mixin's own `MixinConnector`/`IMixinConnector` service mechanism (`org.spongepowered.asm.launch
       .platform.MixinPlatformAgentDefault`, decompiled from the mixin-0.8 sources jar) - reads the
       connector class list from a `"MixinConnector"` manifest attribute via the *same*
       `handle.getAttribute(...)` abstraction that traces back to the same broken `findManifest()`, so it
       cannot bypass the problem either. Confirmed the interface name differs between the sources jar
       (`org.spongepowered.asm.launch.MixinConnector`, doesn't exist in the actual resolved dependency)
       and the real processor jar (`org.spongepowered.asm.mixin.connect.IMixinConnector`) - a version
       mismatch discovered along the way, not the actual blocker.
    2. Registering the config directly and imperatively - `Mixins.addConfiguration("randomthings.mixins
       .json")` in a `static { }` block in `RandomThings.java` (the main `@Mod` class, so it runs at
       mod-class-load time, well before any world/entity code) - compiled fine (the Mixin API classes
       are present at *compile* time via the `org.spongepowered:mixin:0.8` dependency the MixinGradle
       plugin adds automatically) but **crashed the entire mod at launch** with
       `NoClassDefFoundError: org/spongepowered/asm/mixin/Mixins` /
       `ClassNotFoundException: org.spongepowered.asm.mixin.Mixins` - proving that class isn't actually
       on the mod's *runtime* classpath in this environment at all, only available for compilation.
       **Immediately reverted** (removed the static block and the now-unused import) rather than leave
       the mod in a broken, non-launching state - rebuilt and relaunched to confirm a clean boot again
       before doing anything else.
  - **Conclusion, not further pursued given diminishing returns and real regression risk (already caused
    one crash while investigating): this specific combination (ForgeGradle 3 dev client + MixinGradle
    0.7-SNAPSHOT + Forge 28.0.49) cannot run Mixins in `runClient` at all, full stop - a genuine tooling
    limitation of this exact old toolchain, not a bug in this project's own code.** The `remap = false`
    fix from the previous entry is still believed correct (build-verified refmap population, `javap`
    -confirmed real-vanilla vs. Forge-added targets) but is now known to be **unverifiable without a real
    Minecraft + Forge 1.14.4 installation** - the user confirmed they don't have one, only this dev
    client. Until one exists, treat any Mixin-dependent feature (currently: Super Lubricent Boots,
    Blazing Fire's catch-chance/age-growth tuning specifically - not its tick-rate speed, which is
    separate plain Java and already confirmed working) as unverifiable in-game, not "broken." Left the
    diagnostic logging/probe in `SuperLubricentBootsMixin` in place (harmless, throttled) in case a real
    install becomes available later - remove it once boots are actually confirmed working.
- **The real root cause: Mixin's runtime library was never actually shipped anywhere, 2026-09-25.** The
  user set up a real Minecraft+Forge 1.14.4 install (PrismLauncher) and tested directly - boots still did
  nothing, and even with `-Dmixin.debug.verbose=true` explicitly set, there was zero Mixin-related log
  output anywhere, not even a startup banner. That absence, even in a genuinely real environment with a
  correct jar manifest, was the clue that something more fundamental than "dev client can't test mixins"
  was going on.
  - `unzip -l` on the shipped jar showed **zero `org/spongepowered` classes** - the mod jar didn't bundle
    Mixin at all. `unzip -l` on Forge's own `-launcher.jar`/`-universal.jar` showed the same - Forge
    1.14.4 doesn't bundle it either. `./gradlew dependencies --configuration runtimeClasspath` confirmed
    `org.spongepowered:mixin:0.8` was **completely absent** from the runtime dependency graph -
    `build.gradle` only ever declared it as `compileOnly`/`annotationProcessor` (needed for the AP to
    generate the refmap at compile time), never as a real runtime dependency. Nothing, anywhere, was ever
    providing Mixin's actual bootstrap machinery at game launch - true since the very first Mixin
    (`FireBlockMixin`) was added to this project, long before this session.
  - Fix: added `implementation 'org.spongepowered:mixin:0.8'` and shaded that dependency's classes
    directly into the built jar via the `jar` task (`from({ configurations.runtimeClasspath.filter {
    it.name.startsWith('mixin-0.8') }.collect { zipTree(it) } })`) - specifically the *plain* `mixin-0.8`
    artifact, not the `:processor` classifier already depended on, since `unzip -l` showed only the plain
    one contains `META-INF/services/cpw.mods.modlauncher.api.ITransformationService` (what ModLauncher's
    own service discovery needs to find and bootstrap Mixin) - the processor jar only has
    annotation-processing service files.
  - First test (local dev client) crashed immediately: `Found launch plugins: [mixin,eventbus,...]` and
    `Found transformer services : [mixin,fml]` - **"mixin" appeared for the first time ever** - but then
    `[mixin/]: MixinService [ModLauncher] is not valid` →
    `ServiceNotAvailableError: No mixin host service is available`. Traced via the Mixin sources jar to
    `MixinServiceModLauncher.isValid()`, which requires
    `ITransformationService.class.getPackage().isCompatibleWith("4.0")` - i.e. ModLauncher's own
    declared `Specification-Version` must be `>= 4.0`. Downloaded and inspected
    `modlauncher-3.2.0.jar`'s manifest directly: `Specification-Version: 3.0` - too old. That's the
    ModLauncher version this project's `build.gradle` pins via `net.minecraftforge:forge:1.14.4-28.0.49`
    for *building* - not necessarily what the user's real, separately-installed Forge actually runs.
  - Checked what the user's real install (Forge **28.2.26**, read from their PrismLauncher log) actually
    needs: extracted `config.json` from `forge-1.14.4-28.2.26-userdev.jar` and found
    `"cpw.mods:modlauncher:4.1.0"`. Downloaded that jar directly and confirmed its manifest declares
    `Specification-Version: 4.0` - exactly satisfies Mixin 0.8's requirement. Confirmed via Mixin's own
    GitHub release notes for the `0.8` tag (Jan 2020) that "Mixin 0.8 adds support for ForgeGradle 3.0
    used for Minecraft 1.13 and 1.14 modding" - `mixin:0.8` genuinely is the intended version for this
    project, the dev-client crash was purely an artifact of the *older* Forge patch (28.0.49) this
    project happens to be pinned to for compiling, not a flaw in the fix itself. (Briefly explored
    whether an older Mixin build - e.g. `0.7.11-SNAPSHOT`, the exact coordinate Sponge's own team used
    for 1.14.4 circa 2019 per a web search - would be more broadly compatible; confirmed via `unzip -l`
    that build predates ModLauncher entirely (July 2018, old LaunchWrapper-era FML only) and would be
    *worse*, not better, for this purpose. `0.8` is correct.)
  - Full `./gradlew build -x test` verified clean; `unzip -l` confirmed the final jar now contains the
    ModLauncher service files and the manifest's `MixinConfigs` attribute together for the first time.
    Sent the rebuilt jar to the user for an in-game retest - this is unproven until confirmed, but is by
    far the strongest, most fundamentally-justified fix so far (a real, previously-undiscovered gap in
    the project's entire Mixin setup, not a per-mixin bug).

- **Dev client Mixin support, partially restored, 2026-09-25:** while the user tested the real jar,
  worked on getting `./gradlew runClient` usable for Mixin testing too (a "nice to have" parallel task,
  not blocking their real test). Root cause of the dev-client crash (see previous entry) was
  `MixinServiceModLauncher.isValid()` requiring ModLauncher spec `>= 4.0`, but this project's pinned
  `minecraft 'net.minecraftforge:forge:1.14.4-28.0.49'` ships ModLauncher 3.2.0 (spec 3.0). **Bumped the
  pin to `1.14.4-28.2.26`** (documented inline in `build.gradle` - matches the user's real test install's
  Forge version too, ModLauncher 4.1.0/spec 4.0, satisfies the requirement). This surfaced exactly one
  API break: `ItemEntity.getEntityData()` no longer exists on 28.2.26's mappings - `javap`-confirmed it's
  now `Entity.getPersistentData()` (a Forge-added method that got renamed between these patches, not a
  removal). Fixed the one call site (`StableEnderpearlItem.java`); full `./gradlew build -x test` then
  compiled clean with no other breakage, so this was an isolated rename, not a broader compatibility
  problem - the Forge 28.0.49 -> 28.2.26 bump is safe.
  - Result: `runClient` now boots to the title screen cleanly (no crash) and the real
    `SpongePowered MIXIN Subsystem Version=0.8 ... Env=CLIENT` banner appears for the first time ever in
    this project - Mixin's transformer engine is genuinely active. However, `randomthings.mixins.json`
    still never gets registered for our specific dev-mode mod entry (confirmed: zero log lines mentioning
    `randomthings.mixins` or either mixin class outside plain file-scan noise) - the known
    `ExplodedDirectoryLocator` manifest limitation from the earlier entry still applies to Mixin's OWN
    manifest-based config discovery (`MixinPlatformAgentDefault`), not just FML's. So the dev client is
    now **crash-free and structurally capable of running Mixins in general, but still can't test THIS
    project's specific mixins** without further work.
  - Looked into swapping the dev-mode mod source for the real built jar (which has a working manifest)
    via `run/mods/`, to route around this - hit the same "automatic sourceSet-as-mod, causes a duplicate
    mod ID crash" issue as the very first attempt at this (see the earlier entry). Checked ForgeGradle
    3.0.197's own decompiled sources for a DSL option to suppress the automatic `sourceSets.main`-as-mod
    registration (`MOD_CLASSES`-style env var) - not present in the plugin's own source at all, meaning
    it's baked into the per-Forge-version `userdev.jar` launch machinery, not something `build.gradle`
    can override. Stopped here rather than keep digging - this was already a secondary/parallel task
    while the user tested the real environment, and further pursuit has the same "long tail of build
    surgery" risk profile as earlier dead ends this session. **If picked back up later:** the path would
    need either patching/regenerating the `userdev.jar`'s launch args, or finding whatever actually sets
    the `MOD_CLASSES`-equivalent env var for the `runClient` task (not obviously exposed) and suppressing
    it, then loading the real jar from `run/mods/` instead.
- **Mixin's launch-plugin half cannot come from a mods-folder jar, 2026-09-25:** sent the user the jar
  with Mixin's runtime shaded in (previous entry) for a real-environment test. Progress: ModLauncher now
  found `mixin` as a `transformer service` for the first time (`Found transformer services : [mixin,fml]`
  in their log) - but the game then hard-crashed before reaching the title screen:
  `MixinInitialisationError: Mixin Launch Plugin Service could not be located`, `Process exited with
  code 2`. Extracted and read `MixinTransformationService.java` from the mixin-0.8 sources jar directly:
  `initialize(IEnvironment environment)` calls `environment.findLaunchPlugin(MixinLaunchPlugin.NAME)` and
  throws immediately if absent - no fallback, no late-registration path. Cross-referenced against the
  user's own log line ordering: `Found launch plugins: [...]` (without mixin) is logged by
  `LaunchPluginHandler` *before* `Discovering transformation services` even starts; a mod's jar only
  becomes visible to ModLauncher *later*, via a separate "discovery services" pass inside
  `TransformationServicesHandler` that `Found additional transformation services from discovery services:
  [...RandomThings-MC1.14.3-5.0.jar]` explicitly shows finding our jar for - i.e. `ITransformationService`
  gets a second, later discovery chance that scans the `mods` folder; `ILaunchPluginService` does not.
  This is a real, hard ModLauncher architectural constraint for this Forge version, confirmed from
  first-party source, not something fixable by any jar-shading or build.gradle change - Mixin's launch
  plugin genuinely needs to be on the classpath before ModLauncher starts looking at mods at all, i.e. a
  registered *launcher-level library* (e.g. a PrismLauncher custom-library JSON patch pointing at
  `mixin-0.8.jar`), not something achievable from inside the mod itself.
  - Offered the user three options (try the risky launcher-level patch myself with best-effort
    instructions since I can't test PrismLauncher directly; stop here; or research it themselves) - **user
    chose to stop.** Reverted the runtime shading in `build.gradle` (commented out, not deleted, with a
    detailed explanation inline) and rebuilt/resent a jar confirmed to match the launchable (if
    Mixin-inert) state from the user's first two real-environment tests - didn't want to leave them with a
    mod that can't even launch. `unzip -l` confirmed zero `org/spongepowered` classes in this restored jar
    (910KB, matching the original size before any shading work).
- **Coremod conversion identified as the real long-term fix, not yet started, 2026-09-25:** the user then
  asked directly whether Mixins can ever be fully self-contained in a single drop-in jar with no separate
  install step for end users. Answer, given directly: **not via SpongePowered Mixin on this Forge
  version** (same hard constraint as the entry above) **but yes via coremods** - Forge's own JS-based ASM
  transformer system (`META-INF/coremods.json`), which this project already ships successfully and
  self-containedly for `src/main/resources/transformer/VertexLighterFlat.js` (one of the original 17
  ClassTransformer-era patches, ported early in this project - see the "clean 1.14.4 replacement" list
  near the top of this plan). Coremods load directly from a mod's own jar with no launch-plugin-style
  early-classpath requirement, because they're driven by FML's "fml" transformation service (which
  *does* get the later "discovery services" pass), not a separate ModLauncher-level service the way
  Mixin's launch plugin is.
  - **Not yet done - this is real future work, not a quick fix.** Would mean rewriting
    `FireBlockMixin.java` (the `tryCatchFire` catch-chance/age-growth redirects) and
    `SuperLubricentBootsMixin.java` (the `LivingEntity.travel` slipperiness redirect) as JS coremods using
    Forge's ASM-transformer API (`net.minecraftforge.coremod.api.ASMAPI`, `ITransformer`-style
    hooks) instead of Mixin's declarative `@Redirect`/`@Inject` annotations - meaningfully more verbose
    and lower-level (working with raw `MethodNode`/`InsnList` bytecode manipulation, closer to what the
    original 1.12.2 `ClassTransformer.java` did, which this whole project moved away from specifically
    because Mixin was "less hacky" - see "De-ASM-ifying" section near the top of this plan). Once this is
    done, the `org.spongepowered:mixin`/MixinGradle dependencies, the `mixin/` package, and
    `randomthings.mixins.json` could all be removed entirely - this project would no longer need Mixin at
    all. Scope this as its own slice/session when picked up; don't start it opportunistically mid-way
    through something else given how deep the investigation already went to get here.
- **Slice 5 (done), 2026-09-25: Chunk Analyzer, chosen by the user ("Yes let's do CA") after Obsidian
  Skull/Rez Stone were flagged as bigger than they looked and the user asked for something smaller
  instead.** This port's first item-only GUI (no tile entity backing it - `NetworkHooks.openGui` off the
  item's own right-click, `INamedContainerProvider` implemented inline). Right-click opens a GUI with a
  "Scan" button; scanning walks every column of the player's current chunk counting distinct
  `BlockState`s, throttled to 10 columns/server-tick (via the container's own `detectAndSendChanges()`
  override, so a full chunk never causes a visible hitch), then writes a sorted, name-deduplicated result
  list into the held item's own NBT (`ChunkAnalyzerResult`) so it survives closing/reopening the GUI
  without rescanning.
  - API updates from 1.12.2: `IBlockState` -> `BlockState`; `Chunk.getHeightValue(x, z)` ->
    `Chunk.getTopBlockY(Heightmap.Type.WORLD_SURFACE, x, z)` (confirmed via `javap -c` that the latter
    internally masks its `x`/`z` params with `& 15`, so chunk-local 0-15 coordinates work identically
    either way); the old metadata/damage-value system that grouped several "sub-blocks" under one `Item`
    with different damage values is gone in 1.14.4 (each distinct item is its own registered `Item` now),
    so the display stack is just `state.getBlock().asItem()` with no damage lookup needed, and the
    original's `Item == null` / `getDisplayName().equals("Air")` double fallback collapses to a single
    `item == Items.AIR` check (1.14.4's `Block.asItem()` cleanly returns `Items.AIR` for any
    item-less block, unlike 1.12.2's messier `Item.getItemFromBlock` null-vs-air inconsistency - confirmed
    this is a safe simplification, not a behavior gap). The original's manual `sendWindowProperty`/
    `updateProgressBar` sync for the "is scanning" flag is replaced by `Container.trackInt`, which syncs
    automatically - the screen just reads `scanning.get()` every frame to drive the "Scanning Chunk..."
    animated-dots text and disable the Scan button.
  - `client/screen/ChunkAnalyzerScanResultList.java` - this port's first use of the modern
    `ExtendedList<E extends AbstractListEntry<E>>` scrollable-list widget (Forge removed the old
    `GuiScrollingList` this port had been using as precedent).
  - **Self-review caught one real bug before calling this done, via `javap -c` bytecode verification, not
    just re-reading:** first draft's `finishScanning()` correctly called `player.container.detectAndSendChanges()`
    after writing the result NBT (matching the original's `player.inventoryContainer.detectAndSendChanges()`
    call at the same point) - `player.container` in 1.14.4 is confirmed the exact analog of 1.12.2's
    `inventoryContainer` field (the player's always-present default `PlayerContainer`, distinct from
    `openContainer`, the currently-open GUI's container) via `javap -p`, so no reentrancy risk there,
    contrary to an initial suspicion. The actual bug: the original also calls
    `player.inventoryContainer.detectAndSendChanges()` *unconditionally at the very top* of its
    `detectAndSendChanges()` override, every tick, not just on scan completion. First draft of this port
    omitted that top-of-method call. Disassembled `ServerPlayerEntity.tick()` and confirmed vanilla's own
    per-tick loop only calls `openContainer.detectAndSendChanges()` (the currently-open GUI - `this`, while
    Chunk Analyzer is open) and never separately syncs `player.container` while some other container is
    open - meaning without that explicit call, the player's own inventory/armor/crafting slots would go
    stale on the client for as long as the Chunk Analyzer GUI stayed open (any pickup, armor change, etc.
    wouldn't visibly update). Fixed by adding the same unconditional top-of-method call, matching the
    original exactly.
  - Assets: real 1.12.2 GUI and item textures copied byte-for-byte; base crafting recipe ported with the
    same ore-dict-tag-to-plain-vanilla-item simplification already established (iron bars/ingots + glass +
    stone + dirt, all plain vanilla items, no tags needed in the original recipe anyway).
  - Full `./gradlew build -x test` verified clean (compiled clean on the first attempt, before the fix
    above was even needed - the bug was semantic, not a compile error); `unzip -l` confirmed all new
    assets packaged. `TESTING_CHECKLIST.md` #145-148 added, not yet tested in-game (#147 specifically
    added as a regression check for the container-sync fix).
  - **In-game report from the user (screenshot): the GUI rendered as a giant dirt-textured rectangle
    fading to black, not the intended small panel.** Root-caused via `javap -c` disassembly of
    `AbstractList.render()`: it unconditionally calls its own `protected void renderBackground()` (a
    method distinct from `Screen`'s own, easy to miss), which binds `AbstractGui.BACKGROUND_LOCATION` (the
    tiled dirt-pattern texture vanilla uses behind the world-select/resource-pack/options list screens)
    and draws it across the full list bounds with a vertex-color fade toward black at the edges - the
    exact "dirt fading to black" look reported. This is correct behavior for a full-screen menu list, but
    wrong for `ChunkAnalyzerScanResultList` (182x100, embedded inside a small container GUI panel) - it was
    never overridden, so the inherited full-screen-menu chrome painted over the whole panel. This is the
    real cost of the `ExtendedList` substitution flagged back when this slice was written: 1.12.2's
    original `GuiScrollingList` was a lightweight bespoke widget with no such menu chrome to suppress in
    the first place, and this port's replacement inherits vanilla's, unlike anything else ported so far.
    **First fix attempt (incomplete): overrode `renderBackground()` in `ChunkAnalyzerScanResultList` to a
    no-op.** Compiled clean, jar sent, user retested - **same bug, unchanged.** Went back to the full
    `AbstractList.render()` bytecode (not just the `renderBackground()` call at its very top) and found the
    real shape of the problem: the dirt-textured quad and two separate `renderHoleBackground()` calls
    (an opaque black "mask" rectangle used to hide content above/below the visible rows) are **inlined
    directly in `render()`'s own bytecode**, not routed through `renderBackground()` or any other
    overridable hook - so no-op'ing that one hook only ever removed a small piece of the chrome, never the
    dirt quad or the black masking that made up most of what was visible. Confirmed the same problem
    existed the first time around in 1.12.2: `GuiScanResultList`'s empty `drawBackground()` override was
    fixing an equivalent whole-frame draw in Forge's old `GuiScrollingList`, not a narrow single call either
    - same shape of bug, same shape of fix, just a different API surface. **Real fix:** override
    `render(int, int, float)` on `ChunkAnalyzerScanResultList` entirely, replacing vanilla's full-screen
    list frame with a minimal replacement - just `renderList(...)` (the actual row drawing, untouched) plus
    a small hand-rolled scrollbar (`fill()`-based, no texture, only drawn when content overflows the
    visible height). Verified this time by decompiling the actual built class before sending anything:
    zero remaining references to `AbstractGui.BACKGROUND_LOCATION` or `renderHoleBackground` in the
    compiled bytecode. Full `./gradlew build -x test` verified clean; second jar sent to the user for
    retest, `TESTING_CHECKLIST.md` #145 not yet confirmed fixed.
    - **Lesson for next time a vanilla widget needs chrome suppressed:** don't stop at the first
      overridable-looking hook that seems related - check the *calling* method's full bytecode too, since
      modern vanilla GUI classes often inline several draw calls directly in `render()` rather than behind
      clean per-concern hooks the way the old 1.12.2-era classes did.
  - **Second in-game report (GUI now correct, but the scan itself was wrong): consistently missing the
    topmost block in every column** (superflat's own grass layer with nothing placed on it; a block just
    placed on top of grass). Root cause: an off-by-one in `ChunkAnalyzerContainer.detectAndSendChanges()`'s
    height loop. `Chunk.getTopBlockY(type, x, z)` disassembles to `Heightmap.getHeight(x, z) - 1` - i.e. it
    already returns the *actual* Y of the topmost tracked block, not "one past" it. 1.12.2's original loop
    (`for (y = 0; y < c.getHeightValue(x, z); y++)`) relied on `getHeightValue` returning one-PAST the
    topmost block, so using `getTopBlockY(...)`'s result as-is for the same `y < top` loop silently
    excluded whatever the current topmost block was, every single column, every scan. Fixed by adding `+ 1`
    where `top` is computed, restoring the original's "one past topmost" loop-bound convention. Full
    `./gradlew build -x test` verified clean; third jar sent to the user for retest.
  - **User confirmed this fully fixed both the GUI and the scan ("It works great").** `TESTING_CHECKLIST.md`
    #145 marked PASS. Slice 5 closed out.
  - **Toolchain note:** hit a `Could not initialize class org.codehaus.groovy.reflection.ReflectionCache`
    Gradle daemon crash this slice, unrelated to any code change - caused by not sourcing the project's
    own `env/activate.sh` in this fresh shell, so Gradle 5.6.4 picked up whatever JDK was first on `PATH`
    (JDK 21, which it can't run on at all) instead of the pinned JDK 8. `env/activate.sh` (and its
    `.ps1` twin) already exist for exactly this - just forgot to `source` it before invoking `./gradlew`
    this time. No new fix needed; just a reminder to always `source ./env/activate.sh` first in any new
    shell before running Gradle in this repo.

### Deferred-subsystem batch — first slice: BiomeRadar

- **Survey (this session): re-scoped three "deferred" subsystems (Sound-pattern family, Imbuing/Rune
  system, BiomeRadar) that TESTING_CHECKLIST.md's Mixin/worldgen-blocked list doesn't actually cover -
  confirmed via full 1.12.2 source read (delegated to an Explore subagent) that none of the three need
  Mixin, ASM, or real worldgen, unlike Spectre/Ender/Floo/wireless-Redstone-Interface. Sound-pattern
  family (~1450 LOC/17 files) is the simplest, no new infra needed. BiomeRadar (~940 LOC/7 files) needs
  this project's first particle-adjacent system. Imbuing/Rune (~1950 LOC/19 files) turned out bigger than
  previously scoped - it's actually two mechanics (a cosmetic rune-pattern block + a real "Imbuing
  Station" crafting device), needs a brand-new custom Potion/MobEffect base class that doesn't exist yet,
  and the rune block's connected-texture rendering needs a real redesign since `ExtendedBlockState` isn't
  a thing in 1.14.4. **User picked BiomeRadar.** (User first declined both a Crafting Recipe item and the
  Obsidian Skull/Rez Stone cluster when offered as the next Batch-3 slice, then asked to pick a deferred
  subsystem instead of continuing to look for small standalone items - this survey is what came out of
  that redirect.)
- **Slice (done), 2026-09-25: BiomeRadar.** Searches outward from itself (an Archimedean-spiral column
  walk, 48 blocks/ring, throttled 5 columns/tick) for a {@link BiomeCrystalItem}'s target biome, when
  redstone-powered and a small iron-bars "antenna" (`isValid()` - a fixed set of relative-position checks,
  not real structure/worldgen) is built above it.
  - `BiomeRadarBlock.java`/`BiomeRadarTileEntity.java`/`BiomeCrystalItem.java` - direct ports of
    `BlockBiomeRadar`/`TileEntityBiomeRadar`/`ItemBiomeCrystal`. Reused `PositionFilterItem` as-is for the
    "convert a found location to a portable position marker" step (already ported back in the Compass
    slice) - no new item needed for that half, matching how Emerald Compass reused `IdCardItem` earlier.
  - `BiomeRadarAntennaMessage.java` - server-to-nearby-clients sync of the last 4 distinct biomes sampled
    during a search, direct port of `MessageBiomeRadarAntenna`.
  - `BiomeRadarTileEntityRenderer.java` - floating/spinning crystal render above the block. The original
    (`RenderBiomeRadar`) got this by instantiating a throwaway `EntityItem` purely to reuse its bob/spin
    math and vanilla's dropped-item renderer; simpler to just do the bob/spin transform directly and
    render the stack with `ItemRenderer.renderItem(stack, TransformType.GROUND)` - same look, no fake
    entity needed.
  - **Disclosed simplification:** the original's antenna glow used a bespoke client-side smoke-puff
    `Particle` subclass (`EntityColoredSmokeFX`) reusing vanilla's smoke sprite frames. Nothing like that,
    or any particle-registration infra at all, exists yet in this port, and building a whole new
    sprite-based `Particle`+`IParticleFactory`+atlas-sprite chain just for one cosmetic effect felt like
    the wrong first use of that infra. Swapped for vanilla's own `RedstoneParticleData` (the same colored
    dust particle redstone wire uses) - same "colored puff above the antenna" feedback, different particle
    shape. Everything else (state machine, spiral search, antenna-color network sync) is a faithful port.
  - **Real classloading bug caught in self-review, not from a compile error:** first draft called
    `lumien.randomthings.client.util.RenderUtils.getBiomeColor(...)` (a hand-ported ~150-line biome-color
    blend function, direct copy of 1.12.2's `RenderUtils.getBiomeColor`) from `BiomeRadarTileEntity`,
    `BiomeRadarBlock`, and `BiomeCrystalItem` - all common classes loaded on a dedicated server too.
    `client.util.RenderUtils` statically imports LWJGL/`GlStateManager`/other client-only rendering
    classes; even though every actual call site was guarded by an `isRemote` check (or only ever invoked
    from a client-only code path like a color handler), referencing that class's *symbol* from
    server-loaded bytecode risks a `NoClassDefFoundError` on a dedicated server depending on how eagerly
    the JVM/Forge's transformer pipeline resolves the constant pool - a well-known real crash class in
    Forge modding, not a hypothetical. Caught this by re-checking each new file's imports against which
    side loads the class, not by any build error (it compiles fine either way). Fixed by moving
    `getBiomeColor`/`blend` into a new `util.BiomeColorUtil` (common package, doesn't import anything
    client-only - the function itself is pure `Biome`/`Color` math, it never needed to live in the
    client-only rendering-helpers class in the first place) and pointing all three call sites at that
    instead. `client.util.RenderUtils` reverted to exactly its pre-slice content.
  - **Scope note, not fixed this slice:** the recipe's "S" ingredient was 1.12.2's `ItemIngredient`
    metadata sub-item `BIOME_SENSOR` (data value 4) - ported as a new bare `biome_sensor` Item (no
    recipe of its own, matching the original, which never had one either - also creative/loot-only there).
    The original's Biome Sensor had a second, unrelated feature when held in your main hand (a HUD overlay
    showing the current biome's name, via `RTEventHandler`) - **not ported**, out of scope for "port
    BiomeRadar" and a separate small feature in its own right; flagging here in case it's picked up later.
  - Full `./gradlew build -x test` verified clean; `unzip -l` confirmed all new classes/assets packaged
    (including the moved `BiomeColorUtil`). `TESTING_CHECKLIST.md` #149-153 added, not yet tested in-game.
  - **User confirmed BiomeRadar working end-to-end** after the two fix rounds (GUI-chrome bug, height-loop
    off-by-one) - `TESTING_CHECKLIST.md` #145 marked PASS, everything from this session committed and
    pushed to `origin/1.14.4` (`13b54f6`).

### Deferred-subsystem batch — second slice: Sound-pattern family

- **Slice (done), 2026-09-25: Sound Recorder / Sound Pattern / Sound Box / Sound Dampener / Portable Sound
  Dampener.** Chosen by the user over Imbuing/Rune (the other remaining surveyed deferred subsystem) after
  being re-offered the same two-option choice from the BiomeRadar-era survey. Full mechanic: sneak-right-
  click a Sound Recorder to record every sound you hear (up to 10, via a client-side `PlaySoundEvent`
  listener) into its own NBT; plain right-click opens a GUI to stamp one recorded name onto a blank Sound
  Pattern; a Sound Box plays its inserted pattern's sound on a redstone rising edge; a Sound Dampener
  (block, 9-slot filter) or Portable Sound Dampener (carried item, same 9-slot filter) mutes every sound
  matching a pattern inside it, for anyone within 20 blocks (block) or globally for the carrier (item).
  Direct port of 1.12.2's `ItemSoundRecorder`/`ItemSoundPattern`/`ItemPortableSoundDampener`/
  `BlockSoundBox`/`BlockSoundDampener`/`TileEntitySoundBox`/`TileEntitySoundDampener` and their GUIs.
  - **Ground-truthed the one make-or-break API question before writing anything:** does
    `net.minecraftforge.client.event.sound.PlaySoundEvent` (the entire mechanism this subsystem hangs off)
    still exist in this exact Forge 1.14.4/28.2.26 build? Confirmed via `javap` - yes, same shape
    (`getSound()`/`setResultSound()`), and `ISound` itself now exposes `getX()/getY()/getZ()` directly
    (no more 1.12.2's `instanceof PositionedSound` special case for position - a minor simplification, not
    a gap).
  - `SoundPatternItem`/`SoundRecorderItem` - the empty/full and idle/active model swaps used 1.12.2's
    `ItemMeshDefinition` (a per-stack-NBT model picker); reused this port's own already-proven replacement
    for that exact problem from `CompassItemBase` (an `IItemPropertyGetter` predicate + model-JSON
    `overrides`) rather than re-solving it from scratch.
  - `client/screen/SoundNameList.java` - the recorded-sounds clickable list, direct reuse of
    `ChunkAnalyzerScanResultList`'s hard-won `render()` override (see that slice's writeup) - same
    `AbstractList.render()` inlined-menu-chrome problem, same fix, applied proactively this time instead
    of being rediscovered the hard way.
  - `util/ItemInventoryHandler.java` (new) - a small NBT-backed `ItemStackHandler` for the Portable Sound
    Dampener's carried 9-slot filter; modern capability-based equivalent of 1.12.2's bespoke
    `InventoryItem` (`IInventory` implemented by hand against a stack's NBT).
  - `container/AbstractSoundDampenerContainer.java` + `client/screen/AbstractSoundDampenerScreen.java` -
    1.12.2 had `ContainerSoundDampener`/`ContainerPortableSoundDampener` and their GUIs as two
    independently hand-written, near-line-for-line-identical classes (each reimplementing
    `mergeItemStack`/`transferStackInSlot` from scratch, standard 1.12.2 boilerplate). Merged into one
    shared base pair here since 1.14.4's `Container` already provides `mergeItemStack`, leaving nothing
    left to actually duplicate between the TE-backed and item-backed versions.
  - **Disclosed simplifications**, all matching this session's established pattern of porting the real
    mechanic and swapping only what a removed system (Baubles, ore dictionary, metadata sub-items) forces:
    - Portable Sound Dampener was a Baubles body-slot item in 1.12.2; since Baubles is dropped, it works
      purely by being carried anywhere in inventory (the same fallback already used for Obsidian
      Skull/Lava Charm - 1.12.2's own code already had this exact fallback for when no Baubles slot was
      equipped, so no new mechanic was invented here, just the fallback made unconditional).
    - Recipes' `forge:ore_dict` wildcards (`plankWood`, `ingotIron`, `blockGlassColorless`) resolved to one
      concrete vanilla item each (oak planks, iron ingot, glass), matching every other recipe ported this
      session; `minecraft:dye` data 4 -> `minecraft:lapis_lazuli`; `minecraft:wool` data 32767 (any color,
      an OreDictionary wildcard) simplified to `minecraft:white_wool` specifically, since no wool-color tag
      exists anywhere else in this port yet to justify building one for a single non-critical recipe.
  - Full `./gradlew build -x test` verified clean (one real compile error the whole slice - `StringNBT` has
    no `valueOf` static factory in this version, just a public constructor - fixed immediately); `unzip -l`
    confirmed all new classes/assets packaged. `TESTING_CHECKLIST.md` #154-160 added, not yet tested
    in-game.

### Bug-fix pass 4, 2026-09-25: worked TESTING_CHECKLIST.md top to bottom

User asked to go through the checklist and start fixing. Ten real bugs found and fixed, two confirmed
architecturally blocked (same category as the already-documented Mixin gap), one investigated without a
resolution (need more repro detail from the user).

- **Special Chest crash (#100/#102), FIXED - real bug, not previously found.** `SpecialChestTileEntity`
  extends vanilla `ChestTileEntity` directly for its lid-animation/open-sound logic (this port's own past
  design choice), but vanilla's `ChestTileEntity.playSound()` unconditionally calls
  `getBlockState().get(ChestBlock.TYPE)` with no `instanceof ChestBlock` guard - confirmed via `javap -c`.
  `SpecialChestBlock` deliberately doesn't extend `ChestBlock` (to avoid picking up double-chest merging,
  a real design decision from when it was written) and never registered that property, so opening the
  chest crashed immediately. Fixed by registering `ChestBlock.TYPE` on `SpecialChestBlock`'s own state,
  fixed forever at `ChestType.SINGLE` and never implementing the merge logic that would let it become
  anything else - satisfies vanilla's assumption without picking up merging.
- **GUI closes when typing "e" while a text field is focused (#75, #93, #114's "same error"), FIXED -
  same bug in 4 screens.** `OnlineDetectorScreen`/`ChatDetectorScreen`/`GlobalChatDetectorScreen`/
  `NotificationInterfaceScreen` all had the identical (copy-pasted) `keyPressed` override that called
  `super.keyPressed()` (i.e. `ContainerScreen.keyPressed()`) *before* checking whether the text field
  consumed the key. Root cause via `javap -c`: `ContainerScreen.keyPressed()` internally closes the
  screen for the inventory keybind *whenever its own `Screen.keyPressed()` call didn't consume the key* -
  and `TextFieldWidget.keyPressed()` only ever consumes *special* keys (backspace/arrows/etc), never plain
  characters (those go through `charTyped` instead) - so typing a plain "e" reached `super.keyPressed()`,
  which saw an unconsumed key matching the inventory bind and closed the screen right there, before the
  calling code's own `return` logic could do anything about it. Fixed by routing straight to the focused
  field's own `keyPressed()` instead of ever calling `ContainerScreen.super.keyPressed()` while a field is
  focused (except ESC, explicitly let through to still close normally).
- **Advanced Redstone Repeater "half checkered" texture (#80/#81), FIXED - two stale 1.12.2 vanilla texture
  names.** `block/redstone_torch_on` (vanilla renamed this to plain `block/redstone_torch` in the 1.13
  texture flattening) and `block/stone_slab_top` (removed entirely - vanilla's own model of that name now
  reuses `block/stone` for texturing) were both left over from the mechanical `blocks/`->`block/`
  folder-rename pass, which naturally didn't catch texture-file renames/removals, only folder ones. Fixed
  in all 4 repeater model variants; found and fixed the identical `stone_slab_top` mistake in
  `models/block/inventory_tester.json` too (untested so far, #90) while sweeping the rest of this project's
  models for the same class of mistake (clean otherwise).
- **Redstone Tool lines lost on world reload (#94), FIXED - real bug, explains an asymmetry that looked
  like "sometimes works."** `TileEntity.getUpdateTag()`'s default implementation calls the *private*
  `writeInternal()` directly (confirmed via `javap -c`), never the public overridable `write()` - so
  `RedstoneObserverTileEntity` never actually included its `target` field in what gets sent to a
  (re)connecting client, only in disk saves. This is exactly why a target set *during* a session rendered
  fine (singleplayer's integrated server shares the same TE object between "client" and "server" logic, no
  real networking involved) while a genuine reload - real client-side re-sync - silently dropped it. Fixed
  with a `getUpdateTag()` override calling `write()`, plus added a live-update trigger
  (`notifyBlockUpdate`/`getUpdatePacket()`) that was missing entirely, which would have caused the same
  problem on a real dedicated server even *without* a reload. **Found the identical latent bug in this
  session's own `BiomeRadarTileEntity` and `SoundDampenerTileEntity`** (both already shipped with a
  `getUpdatePacket()` override that called the buggy default `getUpdateTag()`) and fixed both proactively -
  neither had been reload-tested yet, so this would have surfaced as a fresh "works once, breaks on
  reload" report otherwise.
- **Lotus Blossom can't be eaten for XP (#67.1), FIXED - genuine gap.** It was registered as a totally
  plain vanilla `Item`, no eat action, no behavior at all. Checked 1.12.2's source: it was a special case
  buried inside the old multi-sub-item `ItemIngredient` system (`getItemUseAction`/`onItemUseFinish`
  branching on `INGREDIENT.LOTUS_BLOSSOM`), not its own class - ported the actual mechanic (10-tick eat,
  3-12 XP split across orbs) into a new dedicated `LotusBlossomItem`, matching how every other former
  `ItemIngredient` sub-item has been handled in this port.
- **Iron Dropper "Tick Delay" button label (no # - a wording request, not a bug), DONE.** Relabeled all
  three states to "Pickup Delay: None/5 Ticks/20 Ticks".
- **Jittery water/lava walking (#128/#130), FIX ATTEMPTED - untested on my end, no way to run the game.**
  Found two real issues in the per-tick `LivingUpdateEvent` listener: (1) it checked the liquid block at an
  exact `floor(posY)` snapshot of the player's feet - once resting right at the surface, the feet Y sits
  exactly on the block boundary, and floating-point noise from the *previous* tick's own upward nudge was
  enough to flip that floor() between the liquid block and the air block above it tick-to-tick, causing the
  "am I on liquid" check to flicker. Now checks slightly below the feet position instead. (2) it only ever
  nudged position upward and never touched vertical velocity, so normal per-tick gravity kept accumulating
  downward speed underneath the nudge and fighting it every tick - now zeroes downward velocity before
  nudging. Genuinely can't verify this one without the user's retest.
- **Two spawn-permission bugs (#29 Lapis Lamp, #110 Slime Cube unpowered), BLOCKED - same category as
  #134/#70, confirmed architectural, not a code bug.** Root-caused via `javap -c` disassembly of
  `WorldEntitySpawner.performNaturalSpawning`: it calls the entity type's own registered spawn predicate
  (which does the light-level check for hostile mobs, or the deterministic slime-chunk check for slimes)
  and bails out immediately on failure - well before it ever reaches `ForgeHooks.canEntitySpawn`, which is
  what fires the `LivingSpawnEvent.CheckSpawn` listener both these blocks rely on to force-ALLOW a spawn
  that would otherwise be rejected. A rejection from the entity's own predicate never reaches that event at
  all, so the ALLOW override is a no-op for exactly the case that matters. This cleanly explains an
  asymmetry that would otherwise look confusing: Quartz Lamp (#30) and (presumably) Slime Cube *powered*
  (#111) both work fine, because *denying* an already-permitted spawn late in the pipeline is something the
  event genuinely can do - only forcing an *already-rejected* spawn back to allowed needs to intercept the
  predicate itself (Mixin/ASM), which hits the exact same "Mixin can't fully bootstrap without a
  launcher-level library" wall already blocking #134 and #70. Not re-attempting that here, consistent with
  the existing decision to pause on it.
- **Sakanade placement (#68), INVESTIGATED, not resolved.** `SakanadeBlock.isValidPosition` and the
  vanilla placement-gate mechanism (`BlockItem.canPlace` calling `state.isValidPosition`, confirmed via
  `javap -c`) both look correct on paper - couldn't find an actual code bug via static review alone. Left
  a specific ask for the user in `TESTING_CHECKLIST.md` (exact mushroom-block source, exact on-attempt
  feedback) rather than guess further blind.
- Full `./gradlew build -x test` verified clean after every fix in this pass; jar sent to the user for
  retest. `TESTING_CHECKLIST.md`'s stale "not implemented yet" section also cleaned up (BiomeRadar and the
  whole Sound-pattern family were listed there from before those slices existed - removed).
- **User feedback: stop using SendUserFile for the built jar - we're both working in the same local
  checkout, and the user runs `./gradlew runClient` directly.** Saved to
  `feedback_randomthings_workflow` memory. SendUserFile was a holdover from an earlier phase of this
  project where the user tested on a separate real Minecraft/PrismLauncher install (the Mixin
  investigation); that's not the normal day-to-day workflow. Only send an actual jar file again if asked
  directly for one.
- **Continued the same bug-fix pass into the three remaining Potion Vaporizer items (#98/#99/#99.1), all
  resolved.**
  - **#99 (bottle-out slot manually fillable), FIXED - real bug, not previously found.** The output slot's
    "reject everything" validation only lived on the tile entity's own `ItemStackHandler`; the
    container's client-side reconstruction constructor (rebuilds a local container purely from a window id
    + player inventory, per the registered `ContainerType` factory - it has no access to the real tile
    entity) built a completely unvalidated plain `ItemStackHandler(3)` instead. Since a slot's
    manual-placement gate is checked against whichever handler backs *that side's own* container, the
    client's copy of the output slot never actually enforced the rule. Fixed by pulling the validation
    into a new shared `PotionVaporizerItemHandler` class, used by both the real tile entity and the
    client-side reconstruction path - the same fix shape as the `getUpdateTag()` bug found earlier this
    pass (a thing correctly implemented once, then silently not applied everywhere it needed to be).
  - **#98 (no burn effect, "two confusing percentage counters"), FIXED - genuine gap, not a regression.**
    The GUI showed two plain percentage-text readouts instead of the original's actual sprites. Checked
    1.12.2's source (`GuiPotionVaporizer`) and confirmed this port's GUI texture is already byte-identical
    to the original's own sheet (verified via `sha1sum` - it was correctly copied in whichever earlier
    slice added this block, just never actually drawn from). Replaced both text readouts with the real
    shrinking-flame (fuel, crops upward from the bottom as it burns down, same technique vanilla's own
    furnace flame icon uses) and draining potion-colored tank (duration, tinted via the stored effect's
    liquid color) sprites, matching the original's rendering exactly using the same texture coordinates.
  - **#99.1 (non-full blocks like torches blocking the room), DONE - explicit behavior-change request, not
    a bug.** Checked 1.12.2's source first: it used the identical `world.isAirBlock(pos)` check the port
    already had, so this was never a porting regression - both matched. Implemented the user's requested
    divergence anyway (explicitly asked for, "if possible"): the flood-fill now also passes through, and
    includes in the potion-effect area, any block with an empty collision shape (torches, signs, buttons,
    levers, flowers, tripwire, etc.) while still stopping at anything with real collision (slabs, stairs,
    fences, glass). Documented in the tile entity's own javadoc as a deliberate divergence from both the
    original and this port's own prior behavior, not a fix.
  - Full `./gradlew build -x test` verified clean; `TESTING_CHECKLIST.md` updated for all three rows.

### Deferred-subsystem batch — third slice: Imbuing/Rune (Rune Base half)

- **Research pass (this session) before writing anything: read the full 1.12.2 source for both halves of
  this subsystem** (`BlockRuneBase`/`TileEntityRuneBase`/`ModelRune`/`ItemRuneDust`/`ItemRunePattern` for
  Rune Base; `BlockImbuingStation`/`TileEntityImbuingStation`/`ImbuingRecipe`/`ImbuingRecipeHandler`/
  `ItemImbue`/`PotionBase`/`ModPotions`/the 5 `Imbue*` classes/the combat-event hooks for Imbuing Station).
  Two findings before starting implementation:
  - **The 5th imbue potion, Collapse, is genuinely abandoned content in the original itself - confirmed,
    not assumed.** `ModPotions.java` has a literal `// TODO: Remove` comment directly above the
    `imbueCollapse` field; `ItemImbue.getSubItems()` only loops `i < 4` (0-3), deliberately excluding
    Collapse (4) from ever appearing in the creative menu; no crafting recipe anywhere produces it either.
    **User confirmed: skip it entirely** - not porting `ImbueCollapse`, `EffectCollapse`, or the separate
    brewable "Collapse" potion-type chain (`PotionHelper.addMix` calls, `SimpleBrewingRecipe`).
  - **Rune Base's rendering needs a full rewrite, not a port.** 1.12.2's `ModelRune` was a custom baked
    model driven by `ExtendedBlockState`/`IUnlistedProperty` (`net.minecraftforge.common.property` -
    doesn't exist in 1.14.4) to pass the per-instance 4x4 rune-color grid and inter-block connection data
    to the renderer. This port's established fix for exactly that problem (per-instance dynamic render
    data with no clean BlockState-based path) is a custom `TileEntityRenderer` instead - same pattern as
    `SpecialChestTileEntityRenderer`/`BiomeRadarTileEntityRenderer`, just newly applied here.
  - **Given the real total scope (~1950 LOC across two only-loosely-related halves - a purely decorative
    Rune Base and a functional Imbuing Station), split into two slices at the user's choice** (offered
    Imbuing Station first as the functional payoff, Rune Base first to prove out the trickier rendering
    piece up front - **user picked Rune Base first**).
- **Slice (done), 2026-09-25: Rune Base half only** (Imbuing Station is a separate, not-yet-started
  slice). `RuneDustItem` (one instance per `DyeColor`, matching this port's "each former metadata
  sub-item is its own registered Item" convention, but - unusually for that pattern - still needs a
  runtime tint via `IRTItemColor` since a rune's whole point is its stored color, not a per-color icon;
  16 registrations share one grayscale texture) - right-click the top of a solid block (or an existing
  Rune Base) to place a colored pixel into its 4x4 grid, auto-placing a new Rune Base if needed.
  `RunePatternItem` - right-click a Rune Base to copy its pattern as a reusable stencil; right-click
  elsewhere to stamp a new Rune Base, consuming matching dust from the placer's inventory per colored
  cell (creative skips this); sneak-right-click reverts to paper. `RuneBaseBlock` - a paper-thin decal
  (`VoxelShape` 16x0.1x16), `BlockRenderType.INVISIBLE` (no baked model or blockstate JSON exists for it
  at all - every visible pixel comes from the TESR), breaks into its placed dust pixels rather than
  dropping itself, and removes one pixel at a time on left-click (via a manual `world.rayTraceBlocks`
  call against the OUTLINE shape, replacing the old `world.rayTraceBlocks` 1.12.2 overload with the same
  intent) rather than needing a full break.
  - `RuneBaseTileEntityRenderer` - the actual rendering rewrite. Verified the connection-strip geometry
    matches the original's exactly by hand-checking all 4 directions' unit math against `ModelRune`'s
    `generateNewRuneQuad` calls (each direction's quad position/size independently confirmed against the
    original's `1/16`-unit arithmetic) before trusting it, not just eyeballing it. **Disclosed
    simplification:** the original additionally sampled a random 2x2 sub-region out of an 8x8
    noise-variation grid baked into its texture (via a per-position seeded RNG) so no two rune pixels
    looked quite identical - this renders flat tinted colors instead (`POSITION_COLOR` vertex format, no
    texture bound at all) with the exact same size/position/connection logic, just without that per-pixel
    texture noise. Full-bright via `TileEntityRenderer.setLightmapDisabled(true)`, matching the original's
    `ILuminousBlock.shouldGlow()` intent without needing that now-pointless marker interface (it existed
    purely to plug into the old baked-model tinting system this port isn't using anymore).
  - **Scope note:** the recipe's "luminous powder" ingredient was another of 1.12.2's `ItemIngredient`
    metadata sub-items (like Batch Radar's Biome Sensor before it) - ported as a new, actually-craftable
    `luminous_powder` Item (glowstone dust + glass + Glowing Mushroom, matching the original's own recipe
    for it) rather than leaving it as a dead-end reagent, since unlike Biome Sensor this one had a real
    recipe in 1.12.2 worth carrying over.
  - Full `./gradlew build -x test` verified clean (compiled clean on the first attempt - the raytrace/TESR
    geometry work, the trickiest part of this slice, needed no fix-up pass); `unzip -l` confirmed all new
    classes/assets packaged. `TESTING_CHECKLIST.md` #161-167 added, not yet tested in-game. Imbuing Station
    (the functional half - a real crafting-like machine, 4 usable combat potion effects) remains a
    separate, fully-scoped, not-yet-started slice.
- **Slice (done), 2026-09-26: Imbuing Station half.** `ImbuingStationBlock`/`ImbuingStationTileEntity`/
  `ImbuingStationContainer`/`ImbuingStationScreen` follow this port's established GUI-machine pattern
  exactly (see `PotionVaporizerTileEntity`/`Container`/`Screen`) rather than 1.12.2's older
  `GuiIds`/direct-tile-entity-reference style: `NetworkHooks.openGui`, an `ImbuingStationItemHandler`
  (slot 4 output rejects manual insertion, matching the validated-handler convention adopted after this
  session's Potion Vaporizer output-slot exploit fix) shared between the real TE and the container's
  client-side reconstruction constructor, and a tracked `IntReferenceHolder` for the 200-tick progress bar.
  `ImbuingRecipe`/`ImbuingRecipeHandler` are a direct port of the original's bespoke unordered-3-ingredient-
  plus-center-item matcher (registered in Java at `setupCommon`, not as datapack JSON - matches the
  original's own approach, and this is a one-off matcher the vanilla recipe pipeline doesn't model anyway);
  dropped the original's ore-dictionary fallback (`ItemUtil.areOreDictionaried`) for plain
  `ItemStack.areItemsEqual`+`areItemStackTagsEqual`, matching this port's established ore-dict-free
  convention. 5 recipes ported 1:1: the one non-potion conversion (water bottle + vine + bone meal +
  cobblestone → mossy cobblestone) plus the 4 potion recipes (fire/poison/experience/wither), each
  producing one of 4 new `ImbueItem` registrations (one shared class parameterized by a `Supplier<Effect>`,
  matching `BeanItem`/`RuneDustItem`'s "shared class per variant" convention - a `Supplier` rather than the
  `Effect` directly, since `ModEffects`'s `@ObjectHolder` fields are populated by their own separate
  registry event and referencing them synchronously from `ModItems`'s event risked an ordering-dependent
  null, same class of risk the `BeanItem`/`ModBlocks` precedent happens to dodge only because blocks
  register before items).
  - **New infra, not needed before now: `ModEffects`/`ImbueEffect`.** 1.14.4 renamed `Potion`→`Effect` and
    dropped the old `PotionBase`'s custom HUD/inventory icon drawing entirely - confirmed via `javap -c` on
    `IForgeEffect` that its `renderInventoryEffect`/`renderHUDEffect` default methods are empty no-ops, and
    on `DisplayEffectsScreen`/`PotionSpriteUploader` that both HUD and inventory icons are instead looked
    up automatically from a `textures/mob_effect/<registry_path>.png` atlas sprite (same convention as
    block/item textures) - so no rendering code was needed at all, just supplying that PNG (the original's
    own 18x18 GUI icon, copied byte-identical). The 5th original imbue, Collapse, and its whole separate
    brewable-potion-type chain (`EffectCollapse`/`ModPotions.collapseType*`) stay excluded, per the same
    already-confirmed-abandoned-content decision from the Rune Base slice.
  - Combat hooks ported into `RandomThings.java` (this port keeps event listeners inline there rather than
    a separate `RTEventHandler`, unlike 1.12.2): a `LivingHurtEvent` listener applies fire(10s)/Wither
    II(5s)/Poison II(10s) on a direct (non-indirect) hit while the attacker has the matching imbue active,
    and a `LivingExperienceDropEvent` listener doubles dropped XP under Experience Imbue - both a
    straight 1:1 port of the original's `RTEventHandler` logic and API names (`EntityDamageSource`/
    `IndirectEntityDamageSource`, `LivingHurtEvent`/`LivingExperienceDropEvent`) confirmed unchanged via
    `javap`.
  - Full `./gradlew build -x test` verified clean (compiled clean on the first attempt); `unzip -l`
    confirmed all new classes/assets packaged, including the `mob_effect`/`item`/`block`/`gui` textures and
    the `data/randomthings/recipes/imbuing_station.json` crafting recipe. `TESTING_CHECKLIST.md` #168-179
    added, not yet tested in-game. This closes out the Imbuing/Rune deferred-subsystem batch entirely.

### Deferred-subsystem batch — fourth slice: Floo network

- **Scoping (this session):** offered four candidates via `AskUserQuestion` after the Imbuing Station
  slice closed out (Item Filter retrofit, a new-custom-Entity cluster, Ender network, Floo network) - a
  research subagent scoped all four from the real 1.12.2 source first. **User picked Floo network.**
  Read the full original source before writing anything: `BlockFlooBrick`/`TileEntityFlooBrick`,
  `FlooNetworkHandler`/`FlooFireplace`, `ItemFlooPouch`/`ItemFlooSign`/`ItemFlooToken`,
  `EntityTemporaryFlooFireplace`/`ParticleFlooFlame`, `MessageFlooParticles`/`MessageFlooToken`, and (found
  only by grepping for where `FlooNetworkHandler.teleport` was actually *called* from, since neither the
  block nor any item class contained the trigger) the real teleport-trigger logic, buried in
  `RTEventHandler.chatEvent` - standing on a fireplace brick (or inside a temporary token-fireplace) and
  typing a chat message sends that message as a destination name instead of actually chatting.
- **Slice (done), 2026-09-26.** This port's first custom (non-tile-entity) `Entity` registration -
  `ModEntityTypes`, following the same `@ObjectHolder` + registry-event pattern as `ModBlocks`/`ModItems`/
  `ModTileEntityTypes`, needed for `FlooFireplaceEntity` (the temporary-fireplace marker a dropped Floo
  Token spawns). `FlooBrickBlock`/`FlooBrickTileEntity` - the individual bricks a fireplace group is made
  of; internal-only (no `BlockItem`, no loot table - matches `RuneBaseBlock`'s precedent for a block only
  ever placed by other game logic). `FlooNetworkHandler`/`FlooFireplace` - a `WorldSavedData` registry of
  every named fireplace, ported onto 1.14.4's renamed `DimensionSavedDataManager` API (same concept).
  `FlooPouchItem`/`FlooSignItem`/`FlooTokenItem` - direct ports, `FlooTokenItem`'s dropped-item aging
  driven by the same `TickEvent.WorldTickEvent`-listener pattern established for `StableEnderpearlItem`
  earlier this session (`Item.onEntityItemUpdate` still doesn't exist in this Forge build). The actual
  teleport trigger (`handleFlooChat`, in `RandomThings.java`) slots into the *existing* `ServerChatEvent`
  listener (the one Chat Detector/Global Chat Detector already use), matching where the original had it,
  rather than adding a second competing chat listener.
  - **Three disclosed simplifications, each following an already-established precedent from earlier this
    session rather than inventing a new kind of shortcut:**
    - Dropped the original's third-party `info.debatty:java-string-similarity` dependency (Levenshtein
      fuzzy-matching a typed name against registered fireplaces) for a plain ~15-line in-house
      implementation - this port has added zero external dependencies anywhere else, and the algorithm
      itself is small and well-known enough not to need one now.
    - Dropped the original's bespoke `ParticleFlooFlame` class and its two dedicated network messages
      (`MessageFlooParticles`/`MessageFlooToken`) for plain vanilla `ParticleTypes.FLAME`, spawned directly
      via `ServerWorld.spawnParticle` (already broadcasts to nearby players on its own - the same finding
      and fix as `PotionVaporizerTileEntity`'s particle system earlier this session, so no custom packet is
      needed here either).
    - `FlooBrickBlock`'s cosmetic "glowing tint overlay" used 1.12.2's `ILuminousBlock` full-bright-
      regardless-of-light-level rendering trick - already dropped for every other block that used it in
      this port (see `LuminousBlock`'s own javadoc), so this isn't a new divergence; reused the
      already-shipped `StainedBrickBlock`/`luminous_stained_brick_*` precedent instead (a plain two-element
      composited model, base + normally-lit tint layer, `BlockRenderLayer.CUTOUT`).
  - **One real bug found and fixed, not ported forward:** 1.12.2's `TileEntityFlooBrick.breakBlock`
    converted a fireplace's children to plain bricks *before* the master, which meant each child's own
    "find and break the master" lookup still found a fully live, unconverted master TE and re-triggered
    the *entire* group conversion again, redundantly, once per child. Restructured (as `FlooBrickBlock
    .onReplaced`, this port's established replacement for `breakBlock`, matching `RuneBaseBlock`'s
    precedent) to convert the master's own blockstate first - by the time any child converts, the master
    TE is already gone, so there's nothing left for a child's lookup to redundantly re-trigger. Not a
    player-observable behavior change (redundant no-op reconversions either way), just removes pointless
    repeated work; not a case of "match 1.12.2 exactly" since this was a genuine inefficiency bug, not a
    deliberate design choice.
  - Full `./gradlew build -x test` verified clean (compiled clean after two small generic-inference/API
    fixes - an explicit `EntityType.Builder<FlooFireplaceEntity>` type witness needed since `T` couldn't be
    inferred through the fluent builder chain, and `AxisAlignedBB` has no copy constructor in this Forge
    version); `unzip -l` confirmed all new classes/assets packaged, including the new `entity`/
    `handler/floo` packages, the `floo_brick` block/textures, all four item textures/models/recipes, and
    the new lang keys. `TESTING_CHECKLIST.md` #180-192 added, not yet tested in-game.

### First wiki-scoped slice — New-Entity cluster: Eclipsed Clock, Weather Eggs, Time in a Bottle

- **Slice (done), 2026-09-26.** First slice picked directly against `WIKI_FEATURE_STATUS.md`'s NOT
  STARTED rows (offered via `AskUserQuestion` alongside Item Filter and Ender Bucket; user picked this
  one). Three wiki pages sharing one need: this port's first non-`FlooFireplaceEntity` custom `Entity`
  types, all registered through the same `ModEntityTypes`/`RegistryEvent.Register<EntityType<?>>`
  infra Floo Teleportation already stood up. `EclipsedClockEntity` (a `HangingEntity` subclass, like an
  item frame) shows a chosen target time of day on a 64-frame dial (still `IItemPropertyGetter`,
  ground-truthed unchanged in this Forge version - vanilla's own clock uses the same mechanism);
  right-click cycles the target by 30s, right-click with a charged `TimeInABottleItem` fast-forwards the
  real world clock to match. `TimeInABottleItem` passively banks 1 tick of charge per real second held
  anywhere in inventory, spent either on the clock or to plant/upgrade a `TimeAcceleratorEntity` (an
  invisible marker that force-ticks one target `TileEntity` extra times per game tick, up to 32x).
  `WeatherEggItem` (three separate registered items - Sun/Rain/Storm - matching this port's per-variant-
  item convention rather than 1.12.2's metadata subtype) throws to spawn a `WeatherCloudEntity` that
  rises to the world height limit and then sets the weather to match.
  - **API-porting notes worth keeping in mind for future entity work:** `ProjectileItemEntity` (vanilla's
    own base for `EggEntity`/`SnowballEntity`) already syncs its held `ItemStack` to clients via its own
    `DataParameter` and implements `IRendersAsItem` - used this directly for
    `ThrownWeatherEggEntity`, so no custom spawn-data packet was needed for its egg-type sync at all
    (a simpler path than `FlooFireplaceEntity`/`EclipsedClockEntity` needed, both plain `Entity`
    subclasses that DO need their own synced `DataParameter`s). Similarly, `SpriteRenderer<T extends
    Entity & IRendersAsItem>` is a ready-made vanilla renderer for "draws as its own held item" entities -
    used it directly for the thrown egg via `RenderingRegistry`, no custom renderer class needed at all.
    `HangingEntity`'s own `hangingPosition`/`facingDirection` fields aren't part of the generic Forge
    entity-spawn packet (`NetworkHooks.getEntitySpawningPacket`), unlike a `DataParameter` - had to add
    explicit synced `DataParameter<BlockPos>`/`DataParameter<Direction>` fields for
    `EclipsedClockEntity` and re-apply them to the real hanging fields via `notifyDataManagerChange`, or
    other clients would see it hanging in the wrong place/orientation until reload.
  - **Three disclosed simplifications, all dropping the same underlying thing:** 1.12.2's custom
    immediate-mode-GL "magic circle" rendering framework (`MKRRenderUtil`/`ColorFunctions`/
    `IColorFunction`), used by both `EclipsedClockEntity`'s time-skip burst and (much more heavily)
    `TimeAcceleratorEntity`'s rate-tier rotating-ring decoration. Neither dropped mechanic is core
    functionality - both are purely decorative flourishes on top of already-correct gameplay - and
    porting that whole bespoke rendering framework (not otherwise needed anywhere else in this port yet)
    for two cosmetic effects wasn't a reasonable trade. Replaced both with plain vanilla particle bursts
    instead. Separately, `WeatherCloudEntity`'s custom-tinted smoke particle class
    (`EntityColoredSmokeFX`) is replaced with `RedstoneParticleData` (the same tintable-dust substitute
    already used by Potion Vaporizer/Floo Network), and its Sun-variant sunburst overlay render (on top
    of, not instead of, its particle cloud) is dropped entirely.
  - **One recipe ingredient substituted, disclosed:** the Eclipsed Clock's original recipe used "Evil
    Tear," an `ItemIngredient` subtype belonging to the not-yet-ported Artificial End Portal feature (no
    wiki page dependency chain built for it yet) - substituted vanilla Ghast Tear instead rather than
    standing up an unrelated feature's ingredient item just for this one recipe slot. Also loosened the
    Rain Weather Egg's water-bottle ingredient to match any potion (the original used a Forge NBT-
    matching custom ingredient type to require specifically water; no such ingredient serializer is
    pre-registered in this Forge version, and writing one for this single recipe slot wasn't worth it).
  - Full `./gradlew build -x test` verified clean (a handful of real API-mismatch fixes needed along the
    way, all found by the compiler rather than the earlier `javap` pass - wrong sub-package for
    `EntityRenderer`/`EntityRendererManager` (`client.renderer.entity`, not `client.renderer`), no
    `getItemRenderer()`/`renderViewEntity` on `EntityRendererManager` itself (routed through
    `Minecraft.getInstance()` instead), `Entity.motionX/Y/Z` fields don't exist in this version
    (`getMotion()`/`setMotion()` instead, matching a pattern already used elsewhere this session), and
    `IForgeItem.onItemUseFirst` takes the `ItemStack` as its own first parameter rather than pulling it
    off the `ItemUseContext`); `unzip -l` confirmed all new classes/assets packaged, including all 64
    Eclipsed Clock dial-frame textures/models. `TESTING_CHECKLIST.md` #193-205 added, not yet tested
    in-game.

### Second wiki-scoped slice — Ender Letter

- **Slice (done), 2026-09-26.** Picked via `AskUserQuestion` alongside Item Filter/Golden Egg/Ender
  Bucket. Read the full 1.12.2 source before writing anything and found the actual mechanic is richer
  than the wiki title alone suggests - two separate blocks/GUIs, not one: `ItemEnderLetter` opens a
  9-slot "write a letter" GUI (its content lives in the letter's own NBT); sneak-right-clicking *any*
  placed `EnderMailboxBlock` while holding an addressed letter looks up the named recipient by username,
  finds their personal mailbox inventory, and delivers a copy there - the block you clicked is never
  where it's stored. `EnderMailboxTileEntity` only remembers *whose* mailbox it is (set at placement);
  the actual mail lives in `EnderLetterHandler`, a `WorldSavedData` keyed by player UUID, so a player's
  inbox is identical no matter which of their own placed mailboxes they open (ticks every 10s to light
  up/emit portal particles when there's unread mail). A delivered letter's slots go output-only and the
  item deletes itself once emptied.
  - **Reused three already-established patterns directly, no new mechanism needed:** `ItemInventoryHandler`
    (an item-NBT-backed `ItemStackHandler`, built for `PortableSoundDampenerItem`) for the letter's own
    9 slots; `ISignalContainer` (built for `ChatDetectorContainer` et al.) for the receiver-name field
    instead of porting 1.12.2's dedicated `MessageEnderLetter` packet; and the `ChatDetectorScreen`-
    established `TextFieldWidget` key-routing fix (route keys to the field directly instead of calling
    `super.keyPressed`, which would otherwise treat a plain letter keystroke as the inventory keybind
    and close the GUI mid-sentence) for the same field. Worth calling out since none of these existed
    when this project was first scoped - each was purpose-built for an earlier, unrelated feature and
    turned out to generalize cleanly.
  - **One correctly-confirmed behavioral detail, not a bug:** `WorldSavedData` (`EnderLetterHandler`) is
    server-only and never auto-syncs to clients - unlike a `TileEntity`. `EnderMailboxContainer`'s
    client-side reconstruction constructor doesn't (and can't) read real inbox contents; it only needs
    the right slot *shape* (a fresh empty `ItemStackHandler(9)`), same as every other container's dummy
    constructor this port already uses - the real contents arrive over the ordinary slot-sync packets
    every container gets, not through the world-data store at all.
  - Full `./gradlew build -x test` verified clean (a few real API fixes needed: `Block.Properties
    .notSolid()` doesn't exist in this version, `.doesNotBlockMovement()` does; `Block.onBlockActivated`
    returns `boolean` here, not `ActionResultType`; an ambiguous-constructor compile error needed an
    explicit `(UUID) null` cast to disambiguate two same-arity constructors); `unzip -l` confirmed all
    new classes/assets packaged, including the hand-authored multi-element mailbox model (ported with
    only texture-path renames - the element/face/uv format itself is unchanged from 1.12.2).
    `TESTING_CHECKLIST.md` #206-217 added, not yet tested in-game.

### Third wiki-scoped slice — Ender Bucket + Reinforced Ender Bucket

- **Slice (done), 2026-09-26.** This port's first feature touching Forge's fluid capability API at all
  (`WIKI_FEATURE_STATUS.md` had flagged Fluid Display as blocked on exactly this being unresearched) -
  ground-truthed fresh via `javap` rather than assumed, since nothing in this port had used it yet.
  Findings: the API is essentially intact from 1.12.2, just modernized - `ItemStack.getCapability`
  returns a `LazyOptional<T>` now (`.orElse(null)` gets the same nullable value back out), vanilla
  fluids (`FlowingFluidBlock`) implement `IBucketPickupHandler` instead of `BlockLiquid`/`IFluidBlock`
  (both checked, matching the original's dual check), and `Item.rayTrace(World, PlayerEntity,
  RayTraceContext.FluidMode)` is a ready-made protected helper that already does what 1.12.2 needed a
  bespoke `WorldUtil.rayTraceAll` for. `EnderBucketItem` floods outward (BFS, capped at 2000 blocks,
  matching the original) from wherever you're pointing to find any position in that connected body it
  can actually drain a source from - the "Ender" gimmick is reaching into a lake's flowing edge and
  still getting a real pickup, not literally unlimited range. `ReinforcedEnderBucketItem` holds 10x the
  fluid and, sneaking, keeps draining the same connected body instead of stopping after the first
  successful drain.
  - **One modernization, not a behavior change:** the original hardcoded a Water=blue/Lava=orange color
    switch (plus a specific check for a third-party mod's fluid by name) for the reinforced bucket's
    durability-bar tint. 1.14.4's `FluidAttributes.getColor(FluidStack)` already lets any fluid report
    its own display color generically, so this asks the fluid directly instead of hardcoding a lookup
    table that would silently miss anything not already in it.
  - **One disclosed simplification:** the original's item icon used a 1.12.2-era Forge-specific
    `forge:forgebucket` model type that dynamically layers a base + fluid-tinted overlay texture at
    runtime. Time ran out (usage-limit-driven wrap-up) before confirming whether an equivalent dynamic
    bucket model loader exists in this Forge version - rather than guess at unfamiliar model-loader
    JSON, shipped a plain static icon instead (the item's name/tooltip still correctly identifies the
    contained fluid via `getDisplayName`, just not the icon's color). Worth a follow-up look before
    calling this permanently settled, since a real equivalent may well exist.
  - Full `./gradlew build -x test` verified clean, compiled clean on the first attempt (the `javap`
    ground-truthing paid off - no follow-up API fixes needed unlike the last two slices); `unzip -l`
    confirmed both classes and all assets packaged. `TESTING_CHECKLIST.md` #218-224 added, not yet
    tested in-game.

### Post-slice bug hunt: two real `runClient` crashes found and fixed, 2026-09-26

- **User report: "build errors when I try RunClient."** `compileJava` and `build -x test` were both
  already clean (as verified after every slice this session) - this was the first time anyone had
  actually run `./gradlew runClient` since the Rune Base slice, so it was the first chance for these two
  bugs to surface at all. Ran `runClient` directly (with a timeout, capturing the log) to get the real
  crash instead of guessing.
- **Bug 1 (hard crash, mod-load time): `ModItems.RUNE_DUST`.** `IllegalStateException: The
  ObjectHolder annotation cannot apply to a field that does not map to a registry ... at
  lumien.randomthings.item.ModItems.RUNE_DUST`. Root cause: `ModItems` carries a class-level
  `@ObjectHolder("randomthings")`, and Forge's `ObjectHolderRegistry` scans *every* static field on such
  a class, not just ones with their own field-level annotation - a plain `Map<DyeColor, Item>` (added
  during the Rune Base slice, deliberately *not* meant to go through `@ObjectHolder`, per its own
  docstring) still got scanned and crashed the whole game at startup. Fixed by moving it to a new,
  unannotated `RuneDustItems` class (`BY_COLOR` field) - updated the 3 call sites
  (`RuneBaseBlock` x2, `RunePatternItem`). While fixing this, found and fixed the **exact same latent
  bug lying in wait** for two more fields in the same class - `colorHolder`/`tagHolder` (plain
  `ArrayList`s, scratch accumulators for the Divining Rod registration step) - moved to a second new
  unannotated class, `DiviningRodScratch`, with zero logic changes (same accumulate-then-clear
  behavior, just relocated). Worth remembering for any *future* `@ObjectHolder`-annotated Mod* class:
  a non-registry-type static field anywhere in it is a guaranteed startup crash, not a warning - and a
  plain `compileJava`/`build` will never catch it, only an actual `runClient` will.
- **Bug 2 (hard crash, client init): `EnderBucketItem`/`ReinforcedEnderBucketItem`.**
  `NullPointerException` inside Forge's own `FluidHandlerItemStackSimple.getCapability`, reached via
  `EnderBucketItem.getDisplayName` → `getContainedFluid` → `stack.getCapability(...)`, itself reached
  from `Minecraft.init()`'s `populateSearchTreeManager` (the creative-menu/recipe-book item search
  index, built very early in client startup). Root cause, confirmed by decompiling the crash site:
  Forge's own built-in `CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY` static field is still null
  at this point in the lifecycle - it isn't populated until `FMLCommonSetupEvent`, which fires *after*
  `Minecraft.init()`'s search-tree build, not before. This is a lifecycle-ordering hazard inherent to
  querying a fluid capability from `getDisplayName()` specifically (not a mistake in the fluid-handling
  logic itself, which was already correct) - fixed with a guard in `getContainedFluid` in both classes:
  return null immediately if the capability field itself is still null, instead of calling
  `getCapability` at all. No behavior change once the game is fully loaded (the search tree gets
  rebuilt on resource reload anyway); this only matters for the handful of frames before
  `FMLCommonSetupEvent` fires.
- **Verified fixed, not just patched-and-hoped:** re-ran `runClient` after each fix. First re-run got
  past the `RUNE_DUST` crash and hit the second one immediately; second re-run got all the way through
  mod construction, registry freezing, sound engine init, and texture atlas creation (block atlas,
  particle atlas, painting atlas, **and the mob_effect atlas** - confirming the Imbuing Station potion
  icons load correctly too) with no further crash - it was still running normally when my own test
  timeout killed it. Full `./gradlew build -x test` re-verified clean after both fixes.

### Fourth wiki-scoped slice — Summoning Pendulum, Golden Egg/Golden Chicken, Fluid Display, 2026-09-26

- User picked 3 of 4 offered not-started, no-ASM-needed candidates (Item Filter was the one not picked).
- **Summoning Pendulum**: new `SummoningPendulumItem.java`. Right-click a passive mob (not `IMob`, not a
  player) to capture it into the item's own NBT via `Entity.writeUnlessPassenger` (the direct 1.14.4
  equivalent of 1.12.2's `writeToNBTOptional` - confirmed identical by-decompilation semantics: skip if
  passenger, else write "id" + full NBT unless removed), up to 5, FIFO; right-click a block to pop the
  oldest one back out via `EntityType.loadEntityUnchecked` (replaces `EntityList.createEntityFromNBT`).
  Purple durability bar + enchant glow at 5/5, shift-tooltip lists captured entity names via
  `EntityType.getName()`. Direct port of `ItemSummoningPendulum`.
- **Golden Egg / Golden Chicken**: new `entity/ThrownGoldenEggEntity.java` (rebased onto vanilla's
  `ProjectileItemEntity`, same reasoning as `ThrownWeatherEggEntity` - free item-stack sync/rendering),
  `entity/GoldenChickenEntity.java`, `client/renderer/GoldenChickenEntityRenderer.java` (reuses vanilla's
  `ChickenModel` wholesale), `item/GoldenEggItem.java`. 1.12.2's `EntityGoldenChicken` deliberately
  extended `EntityAnimal` directly rather than `EntityChicken` (its egg-timer/wing-flap fields are
  private on the vanilla class) - matched that exact choice here (extends `AnimalEntity`, not
  `ChickenEntity`), copying vanilla chicken's own goal list minus the breed goal (matching the original's
  own apparent omission - it has `isBreedingItem` but never registers a breed AI goal) and its own
  wing-flap/gold-ingot-drop logic verbatim. `OreDictionary` (removed in 1.14.4) → `Tags.Items.ORES_GOLD`
  for the "is this a gold ore item" check - a straight modern-equivalent swap, not a capability loss, so
  not flagged as a divergence. First entry in this port with real animal AI goals (`SwimGoal`/`PanicGoal`/
  `TemptGoal`/`FollowParentGoal`/`WaterAvoidingRandomWalkingGoal`/`LookAtGoal`/`LookRandomlyGoal`), all
  ground-truthed by decompiling vanilla `ChickenEntity.registerGoals()` bytecode rather than guessed.
  Disclosed simplification: skipped re-overriding `getEyeHeight()` (1.12.2's version returned a flat
  `this.height` instead of the standing-pose calculation) since `Entity.getEyeHeight()` is `final` in
  1.14.4 - no gameplay-visible difference from the AI-look-target-height default.
- **Fluid Display**: new `block/FluidDisplayBlock.java`, `tileentity/FluidDisplayTileEntity.java`,
  `client/renderer/FluidDisplayTileEntityRenderer.java`. The "blocked on fluid API research" note from
  the original survey pass is resolved - this session's Ender Bucket slice already ground-truthed the
  modern `IFluidHandlerItem`/`LazyOptional` capability API, and the modern `IFluidHandler.getFluidInTank
  (int)` is actually *simpler* than 1.12.2's `IFluidTankProperties[]` indirection. 1.12.2's
  `ModelFluidDisplay` (a custom baked model driven by `ExtendedBlockState`/`IUnlistedProperty`, not a
  thing in 1.14.4) replaced with a TESR, matching this port's established substitute for that exact kind
  of per-instance dynamic render data (see `RuneBaseTileEntityRenderer`) - reads the fluid's still/
  flowing sprite straight off the live block atlas (`Minecraft.getTextureMap().getSprite(...)`, the same
  atlas vanilla's own fluid rendering samples from) and tints it via `FluidAttributes.getColor`.
  Disclosed simplification: the rotation property physically rotates the whole rendered cube in world
  space each 90-degree step (via immediate-mode `GlStateManager.rotatef`) instead of 1.12.2's UV-remap-
  only rotation on an otherwise-static cube - visually near-identical since every face shares one
  texture, but not byte-for-byte the same transform. Recipe ported with 1.12.2's ore-dict "any colorless
  glass" key simplified to plain vanilla `minecraft:glass` (not a real divergence - that ore-dict tag
  only ever matched vanilla clear glass anyway).
- Full `./gradlew build -x test` verified clean (one round of fixes needed: `SoundCategory` is under
  `net.minecraft.util` in 1.14.4, not `net.minecraft.entity` where 1.12.2 had it - caught immediately by
  the compiler, not a runtime surprise). `TESTING_CHECKLIST.md` #225-237 added, not yet tested in-game.
  `WIKI_FEATURE_STATUS.md` updated for all three features plus the four features from the prior two
  sessions' slices that the status table had gone stale on (Eclipsed Clock/Weather Eggs/Time in a Bottle/
  Ender Letter/Ender Bucket were still marked NOT STARTED there despite shipping earlier this session).

### Fifth wiki-scoped slice — Artificial End Portal, Creative Player Interface (bonus), 2026-09-26

- User picked Artificial End Portal from 3 offered candidates (Item Filter, Ender Bridge were the other
  two, not picked); Creative Player Interface was offered up front as a near-zero-effort bonus (1.12.2's
  own `TileEntityCreativePlayerInterface` is a bare empty subclass of the already-shipped `TileEntity
  PlayerInterface`) and bundled in regardless.
- **Artificial End Portal**: new `entity/ArtificialEndPortalEntity.java`, `client/renderer/
  ArtificialEndPortalEntityRenderer.java`, `item/EvilTearItem.java`. A ritual structure (End Rod/End
  Stone/obsidian ring, exact layout in `isValidPosition`) + right-clicking the top End Rod with an Evil
  Tear spawns this - after a 200-tick charge-up, touching it teleports a player to the End. The
  charge-up timer moved from 1.12.2's `IEntityAdditionalSpawnData` (a raw `ByteBuf` spawn-data channel)
  to a synced `DataParameter<Integer>`, matching this port's own established convention for exactly this
  problem (see `TimeAcceleratorEntity`'s `TIME_RATE`) - not a capability loss, `IEntityAdditionalSpawnData`
  still exists in 1.14.4 but the modern approach is more idiomatic and this port already had the pattern.
  `Entity.changeDimension` now takes a `DimensionType` (`DimensionType.THE_END`) instead of a raw int id.
  The renderer is the interesting part: 1.12.2's `RenderArtificialEndPortal` was itself a copy of
  vanilla's `RenderEndPortal`, and ground-truthing vanilla's *current* 1.14.4 `EndPortalTileEntityRenderer`
  by decompiling its bytecode confirmed the exact same fixed-function `GL_TEXTURE_GEN` swirling-starfield
  trick is still there, completely unchanged in approach (`GlStateManager.getMatrix` replaces 1.12.2's
  `getFloat`, `enableTexGen`/`texGenMode`/`texGenParam` replace `enableTexGenCoord`/the two-arg and
  four-arg `texGen` overloads, `GameRenderer.setupFogColor` replaces `EntityRenderer.setupFogColor` -
  otherwise a near-verbatim port, not a simplification at all). Disclosed simplification: dropped the
  original's per-particle random-purple-tint override on the charge-up sparkles (`ParticleTypes.ENCHANT`
  via `World.addParticle` doesn't expose per-particle color the way the old `Particle.setRBGColorF` call
  did) - same particle family, just its own default look instead of a custom tint.
- **Creative Player Interface**: new `block/CreativePlayerInterfaceBlock.java`,
  `tileentity/CreativePlayerInterfaceTileEntity.java` - both trivial subclasses (override only
  `createTileEntity`, and only the constructor calling `super(ModTileEntityTypes.CREATIVE_PLAYER_INTERFACE)`,
  respectively) reusing 100% of `PlayerInterfaceBlock`/`PlayerInterfaceTileEntity`'s real logic, exactly
  mirroring the original's own empty-subclass structure. Its own registered `TileEntityType` was still
  needed (Forge ties one type to one concrete class). Creative-menu-only, no survival recipe - matches
  1.12.2 (no recipe json existed for it there either). Textures reused from 1.12.2's existing `c`-prefixed
  variant files (`cbottom`/`cside`/`cside_shield`/`ctop`), already sitting unused in the 1.12.2 source
  tree next to Player Interface's own.
- Full `./gradlew build -x test` verified clean, compiled clean on the first attempt. Also ran a 150s
  `runClient` smoke test given the recent history of runtime-only `@ObjectHolder` crashes when adding new
  fields to `Mod*` classes carrying that annotation - got cleanly through registry freeze, sound engine,
  and all four texture atlases with no crash before my own test timeout killed it (confirmed via
  `tasklist` that no `java.exe` processes were left running). `TESTING_CHECKLIST.md` #238-244 added, not
  yet tested in-game. `WIKI_FEATURE_STATUS.md` updated for both features.

### Sixth wiki-scoped slice — Ender Bridge/Ender Anchor/Prismarine Ender Bridge; Item Filter deliberately skipped, 2026-09-26

- User picked both remaining no-ASM NOT-STARTED candidates (Ender Bridge, Item Filter) - the last two
  before the backlog is down to ASM/Mixin-dependent, enchantment-dependent, or deferred-Spectre-subsystem
  features only.
- **Ender Bridge investigation revealed it's simpler than the name suggests**: no physical bridge
  extends between the two blocks at all - `BlockEnderBridge`/`TileEntityEnderBridge` is a redstone-
  triggered teleport pad that scans outward along its facing direction for a matching `BlockEnderAnchor`,
  teleporting nearby whitelisted entities (players/items/minecarts) there if found. `EntityEnderConnection`
  turned out to be genuinely dead code in 1.12.2 itself (its `onCollideWithPlayer` built a list and never
  used it) - not ported, since there was nothing to port. `TileEntityPrismarineEnderBridge` was a
  byte-for-byte duplicate of the regular tile entity except scanning 10 blocks/tick instead of 1 - unified
  into one shared `EnderBridgeTileEntity` base class with an overridable `getScansPerTick()`, and one
  shared `EnderBridgeBlockBase` holding the (previously duplicated) `FACING`/`ACTIVE` block-state
  properties, rather than porting two near-identical copy-pasted classes as the original had. New:
  `block/EnderBridgeBlockBase.java`, `block/EnderBridgeBlock.java`, `block/PrismarineEnderBridgeBlock.java`,
  `block/EnderAnchorBlock.java`, `tileentity/EnderBridgeTileEntity.java`,
  `tileentity/PrismarineEnderBridgeTileEntity.java`, `tileentity/EnderAnchorTileEntity.java`. Full 6-way
  `Direction` facing (matches 1.12.2's `EnumFacing.getDirectionFromEntityLiving`, which can return up/down
  too, not just the 4 horizontal directions - ground-truthed via `Direction.getFacingFromVector`, already
  established by `AnalogEmitterBlock`). Disclosed simplification: `TileEntityEnderAnchor`'s optional Forge
  chunk-loading ticket (itself gated behind a config flag most packs leave off) isn't ported - Forge's
  ticket API changed significantly by 1.14.4, and it only matters if the destination chunk would otherwise
  unload, not the common case of a nearby player using it.
- **Item Filter deliberately skipped, not ported**: investigation found its only two 1.12.2 consumers -
  Advanced Item Collector and Filtered Super Lubricent Platform - are both already shipped in this port,
  and both already made their own disclosed simplification (a single built-in example-item equality slot)
  instead of consuming the fully configurable `ItemItemFilter`. Flagged this scope surprise to the user via
  AskUserQuestion rather than silently building an orphaned item or silently retrofitting two already-
  shipped, already-disclosed features - user chose to skip it. Noted in `WIKI_FEATURE_STATUS.md` and
  `TESTING_CHECKLIST.md` section 6 as a deliberate call, revisit only if some future feature actually needs
  the fully configurable version.
- Full `./gradlew build -x test` verified clean, compiled clean on the first attempt (`BlockState.isAir()`
  needed a one-line fix mid-way - 1.12.2's context-aware `Block.isAir(state, world, pos)` overload doesn't
  exist in 1.14.4, only the parameterless `BlockState.isAir()`, caught immediately by the compiler). Also
  ran a 150s `runClient` smoke test - registered cleanly (confirmed via the registry dump logs for all new
  blocks/tile-entity-types), no missing-model/texture warnings for any of this slice's assets, reached
  sound engine init with no crash before my own test timeout (the only warnings in the log were pre-
  existing/unrelated: a stale-world-save mapping notice for the already-removed Blood Rose, and two
  already-known TESR-only blocks' expected "missing blockstate" warnings). `TESTING_CHECKLIST.md` #245-251
  added, not yet tested in-game. `WIKI_FEATURE_STATUS.md` updated.

### Plate Family, Special Chest, Sakanade removed by explicit request; four wiki-scoped slices (Spectre Illuminator, Time in a Bottle/Eclipsed Clock follow-ups, Portkey + camo, Spectre Anchor), 2026-09-27

- **User asked to drop three features outright rather than port them**: the whole Plate family
  (`AcceleratorPlateBlock`, `BouncyPlateBlock`, `CollectionPlateBlock`, `CorrectorPlateBlock`,
  `DirectionalAcceleratorPlateBlock`, `ExtractionPlateBlock`, `FilteredRedirectorPlateBlock`,
  `ItemRejuvenatorPlateBlock`, `ItemSealerPlateBlock`, `ProcessingPlateBlock`, `RedirectorPlateBlock`,
  `RedstonePlateBlock`, plus the shared `PlateBlock` base), Special Chest (block/item renderer/TESR/tile
  entity, plus the `OceanMonumentTransformer.js` coremod that placed it in ocean monuments), and (mid-turn,
  a follow-up ask) Sakanade (`SakanadeBlock` + `sakanade_spores` item). All fully deleted - source files,
  registrations in `ModBlocks`/`ModItems`/`ModTileEntityTypes`/`ModContainerTypes`/`ModScreens`, assets,
  lang entries, the coremod entry and its `.js` file. `WIKI_FEATURE_STATUS.md`/`TESTING_CHECKLIST.md`
  updated to mark these deliberately-dropped rather than not-started.
- **Spectre Illuminator** (user-picked slice): floating light-source orb, right-click a block to place,
  right-click again to collect. Went through four real bugs in sequence, all found by the user actually
  testing it: (1) a crash from scheduling ~83,000 `WorldLightManager#checkBlock` calls synchronously in one
  method call (`ArrayIndexOutOfBoundsException` in this Forge version's rewritten light engine) - fixed by
  queuing positions into `SpectreIlluminatorRelight` and draining a bounded number per tick; (2) the
  client-side queue never drained because a `WorldTickEvent` listener doesn't reliably fire for the client
  `World` the way it does for the server one - fixed with a `ClientTickEvent` listener targeting
  `Minecraft.getInstance().world` directly; (3) no visible model/particles at all on placement - added a
  billboard icon via `IRendersAsItem`+vanilla `SpriteRenderer` (the same mechanism already used for Thrown
  Golden/Weather Egg) plus a particle trail; (4) after pickup, most of the chunk went dark but one edge
  stayed stuck lit - root cause was the relight sweep's chunk-boundary padding being only 1 block wide,
  while vanilla block light naturally bleeds up to 15 blocks past a boundary once the neighbor chunk is
  flooded to level 14 - widened `PADDING` to 15. Perf-tuned the per-tick drain rate twice more at the
  user's request (split server/client rates → unified back into one shared `PER_TICK` constant, landing on
  256, for consistency). New `IBlockReaderTransformer` coremod patches `IBlockReader.getLightValue`'s
  default method (`Block.getLightValue`'s old 3-arg overload doesn't exist in this Forge version).
  **User confirmed working** after all four fixes.
- **Time in a Bottle / Time Accelerator follow-ups**: user confirmed the core effect worked but reported
  the entity itself wasn't visible (particles were). Added a billboard via `IRendersAsItem` (a real synced
  `DataParameter<BlockPos>` for `target` was needed first - the client copy had a null `target` field and
  crashed on `tick()`, a real bug caught from the user's own crash log, not just the missing-visual report).
  Added a "not enough time saved" status message for both insufficient-time code paths. Perf: unified
  server/client tick rates once more per user request (matching the Illuminator pattern), landed on 256.
  Then: user reported the icon/particles were centered inside the block instead of on its surfaces (most
  blocks players click are full blocks) - redesigned `TimeAcceleratorEntityRenderer` to draw the icon as a
  billboard on all 6 faces near each surface (transform sequence copied from `SpriteRenderer#doRender`,
  ground-truthed via `javap -c`), and widened the particle spread across the full block volume. Same
  request also covered the **Eclipsed Clock**: it worked functionally when hit with a bottle but snapped
  time instantly - added a `FAST_FORWARD_RATE`-stepped day-time animation instead (`fastForwardTarget`
  field, advanced in `tick()`); and its floating time-display label was one block too high - a real
  off-by-one in the `renderLivingLabel` y-offsets, ground-truthed via `javap -c` against the vanilla
  method's own baked-in `entity.getHeight()+0.5` offset, fixed (`y+0.45`→`y-0.55`, `y`→`y-1.0`).
- **Portkey** (user-picked slice): craft, bind to a block via right-click, drops on the ground prime it
  after ~5s (glowing while unbound/priming), the next player to pick up a primed+bound one teleports to the
  target instead of collecting it (same safe-landing-spot search as 1.12.2). Disclosed simplifications:
  no custom tint on the drop-glow, no full-screen HUD directional beam (both cosmetic, would need new
  coremods for one detail each). **User then asked specifically to research the item's camo/disguise
  feature** (combine with any other item to visually disguise the Portkey as that item, in every render
  context) before committing to implement it - researched and confirmed a clean, reflection-free 1.14.4
  path exists (`Item.Properties#setTEISR`, the same mechanism vanilla uses for shulker boxes; ground-
  truthed via `javap -c` that `ItemRenderer.renderItem` checks `getTileEntityItemStackRenderer()`
  universally, not just for one render context). **User approved, implemented same-day**: new
  `PortkeyItemRenderer extends ItemStackTileEntityRenderer`, `PortkeyCamoRecipe`
  (`SpecialRecipe`/`SpecialRecipeSerializer`, same pattern as `GoldenCompassSetPositionRecipe`). Caught and
  fixed one real bug via a `runClient` smoke test before it ever reached the user: a doubled `item/item/`
  model path from passing an already-`"item/"`-prefixed string to a `ModelResourceLocation` that itself
  auto-prefixes `"item/"`.
- **Spectre Anchor** (user-picked slice, most recent): craft (6 iron + 1 Ectoplasm), combine with any
  other single non-stackable item in a crafting table to tag it "Anchored" (dark-aqua tooltip) - an
  Anchored item survives player death instead of dropping, restored to the same inventory slot on respawn.
  Ground-truthed against 1.12.2's real mechanism first (`ItemSpectreAnchor`, `BaublesSpectreAnchor.java`,
  `RTEventHandler`, `ModRecipes.java`'s `spectreAnchorCombine`) rather than assuming from the plan alone -
  confirmed the "combine in crafting table, tag `spectreAnchor` NBT byte, main-inventory `PlayerEvent.Clone`
  restore, dark-aqua `ItemTooltipEvent` line" design matches exactly; Baubles doesn't exist in this port so
  only the main-inventory half of 1.12.2's dual (inventory + Baubles) handling applies. The hard part was
  the drop-skip itself: **no Forge event in this version covers a player's death-drops** -
  `PlayerDropsEvent` no longer exists (confirmed via jar listing), `ItemTossEvent` only fires from the
  manual Q-key-drop overload not the death-drop one, `LivingDropsEvent` is a different (mob-loot) code path
  entirely - all three ruled out via `javap -c` before concluding a coremod was the only option, same as
  1.12.2's own approach. New `PlayerEntityTransformer.js` coremod redirects the single
  `PlayerInventory.dropAllItems()` call inside `PlayerEntity.dropInventory()` (confirmed via `javap -c` to
  be a short, self-contained method) to a new `AsmHandler.dropAllItemsExceptAnchored`, which skips both the
  drop and the slot-clear for any Anchored stack. Verification took real work this time: `PlayerEntity` is
  one of the largest/most complex vanilla classes, and `CheckClassAdapter.verify`'s `SimpleVerifier` fully
  type-checks every method in the class (not just the patched one), so it needed a much bigger verification
  classpath than any previous coremod target in this project - individually chasing missing jars
  (`brigadier`, Forge `eventbus`, Guava) got tedious and error-prone, so switched to generating the *exact*
  classpath Gradle itself resolves via a throwaway init-script task
  (`configurations.runtimeClasspath.files.each { println it.absolutePath }`) and feeding that whole list to
  the standalone harness - **verified clean on the first attempt** with the complete list. Also ran a real
  `runClient` boot smoke test (killed by its own timeout after loading cleanly, no crash) both right after
  the coremod alone and again after the rest of the feature (item/recipe/listeners/assets) was added. New:
  `recipes/SpectreAnchorCombineRecipe.java` (same `SpecialRecipe` pattern as `PortkeyCamoRecipe`, but fully
  consumes both ingredients unlike Portkey's camo recipe), a plain `spectre_anchor` item (registered inline
  like `ectoplasm`/`floo_powder`, no dedicated item class needed), a `PlayerEvent.Clone` listener and an
  `ItemTooltipEvent` listener in `RandomThings.java`. `./gradlew build -x test` and the full `runClient`
  smoke test both clean. Not yet tested in a real play session (needs an actual in-game death to confirm
  the respawn carryover). `TESTING_CHECKLIST.md` #281-282 added, `WIKI_FEATURE_STATUS.md` updated.
- Nothing from this entire session has been committed or pushed - standing instruction remains to wait for
  an explicit "commit"/"push" request.

### Redstone Wireless cluster — Basic/Advanced Redstone Interface, Redstone Activator, Redstone Remote, 2026-09-27

- User picked this cluster (over Block Destabilizer and Dyeing Machine) when asked for "another batch of
  work" - the biggest single slice this session: new shared infrastructure (`RedstoneSignalHandler`,
  `RedstoneInterfaceTileEntity`) plus 4 features built on it, previously all NOT STARTED and explicitly
  called out in this plan's Batch 7 note as needing new coremod work.
- **Ground-truthed 1.12.2's actual mechanism first** (`RedstoneSignalHandler`/`RedstoneSignal`,
  `TileEntityRedstoneInterface`/`TileEntityBasicRedstoneInterface`/`TileEntityAdvancedRedstoneInterface`,
  `AsmHandler#getRedstonePower`/`getStrongPower`, `ItemRedstoneActivator`, `ItemRedstoneRemote` +
  `ContainerRedstoneRemote`/`MessageRedstoneRemote`) before writing anything: two independent wireless-
  power sources (a live per-TE sensor+broadcaster, and a fixed-duration timed pulse) both funnel through
  one 1.12.2 ASM patch on `World.getRedstonePower`/`getStrongPower`, both taking the max against vanilla's
  own computed value.
- **The coremod design didn't carry over 1:1** - ground-truthed via `javap -c` that 1.14.4 restructured
  strong-power computation: `World.getStrongPower(BlockPos, Direction)` (the exact method 1.12.2 patched)
  no longer exists; strong power per-direction now runs through a *default* `IWorldReader.getStrongPower`
  method instead (confirmed by disassembling `World`'s own `getStrongPower(BlockPos)` aggregate, which
  calls a synthetic-looking `getStrongPower(BlockPos, Direction)` that turned out to resolve to this
  inherited default, not a concrete `World` method). So this needed *two* new coremods instead of one:
  `WorldRedstonePowerTransformer.js` (weak power, `World.getRedstonePower` - unchanged from 1.12.2's
  choke point) and `WorldReaderStrongPowerTransformer.js` (strong power, now on `IWorldReader`). Both use
  the "wrap the value right before its single IRETURN" pattern already proven by `IBlockReaderTransformer`
  (Spectre Illuminator's light-value patch) rather than 1.12.2's own "insert at method head with an early
  conditional IRETURN" ASM style - simpler, and this project's own established idiom.
- **Verification found the classpath-completeness lesson from Spectre Anchor's coremod already paid off**:
  reused the same "ask Gradle for its own exact `runtimeClasspath` via a throwaway init-script task" trick
  from that slice, and both new coremods verified clean via the bytecode-verification harness on the
  *first* attempt - no missing-jar chase this time.
- Implemented, in order: `RedstoneSignal`/`RedstoneSignalHandler` (`WorldSavedData`, one instance per
  world instead of 1.12.2's one global instance keyed by dimension id - every call site already has the
  right `World` in hand, so the dimension field was dead weight); `RedstoneInterfaceTileEntity` (shared
  abstract base, mirrors `RedstoneObserverTileEntity`'s already-proven `NeighborNotifyEvent`/
  `getUpdatePacket`+`onDataPacket`+`getUpdateTag` live-sync pattern, just sensing its own neighbors instead
  of a remote target); `BasicRedstoneInterfaceBlock`/`TileEntity` (single tool-bound target, extended
  `RedstoneToolItem` to recognize this block alongside its existing Redstone Observer case) +
  read-only status `Container`/`Screen` (copy of `RedstoneObserverContainer`/`Screen`);
  `AdvancedRedstoneInterfaceBlock`/`TileEntity` (9-slot Position-Filter `ItemStackHandler`, item-type
  restriction enforced on the handler itself rather than a dedicated slot class, matching
  `FilteredSuperLubricentPlatformContainer`'s established precedent) + `Container`/`Screen`;
  `RedstoneActivatorItem` (NBT-stored duration index, `addPropertyOverride` texture swap, same pattern as
  `SoundRecorderItem`); `RedstoneRemoteItem` + `RedstoneRemoteEditContainer` (9 Position Filter slots
  stored in the item's own NBT rather than a tile entity) + `RedstoneRemoteUseContainer`/`Screen` (no
  slots at all - reads the held stack's NBT directly client-side, same as 1.12.2's own
  `ContainerEmptyContainer` approach) + `RedstoneRemoteActivateMessage` (new network message).
- **Disclosed simplification**: dropped 1.12.2's Redstone Remote "edit" GUI's second row of ghost/camo-
  icon slots (cosmetic only - even in 1.12.2, a button's own function never depended on its icon, only on
  the Position Filter's stored coordinates) and its item-icon buttons on the "use" screen, using plain
  text buttons instead - matches this port's own already-established `EntityDetectorScreen` precedent of
  text buttons over icon sprites.
- Full `./gradlew build -x test` clean, compiled clean on the first attempt. Both new coremods verified via
  the harness; two separate `runClient` boot smoke tests (once for the coremods alone, once for the full
  feature) both loaded cleanly with no crash or missing-asset warnings. Not yet tested in a real play
  session (needs actual redstone-circuit testing in-world). `TESTING_CHECKLIST.md` #283-287 added,
  `WIKI_FEATURE_STATUS.md` updated (Redstone Interface/Advanced Redstone Interface/Redstone Activator/
  Redstone Remote all moved from NOT STARTED to DONE-UNTESTED).

## Batch 7 — coremod-dependent features (Mixin migration + expanded ASM-derived scope, deliberately last)

**Status, 2026-09-26 (later same day): the original scope (Mixin→coremod conversion) is DONE - see
"Batch 7, slice 1" below.** **Update, still later the same day: Batch 7, slice 2 (Slime Cube spawn-ALLOW
+ Lapis Lamp spawn-prevention) is also DONE - see that section below.** The remaining expanded scope
(Peace Candle, Rain Shield, Magic Hood's particle-hiding half, Spectre Illuminator, Special Chest's
worldgen placement) is still NOT STARTED - each of those needs its own new coremod transformer(s) plus
whatever new feature code they gate, and its own ground-truthing pass. **The Redstone Interface family
is DONE as of 2026-09-27** - see the "Redstone Wireless cluster" progress-log entry above; it needed two
new coremods (`World.getRedstonePower`/`IWorldReader.getStrongPower`), not the one originally assumed
here, since 1.14.4 restructured strong-power computation onto a different method entirely. The pattern is
now proven several times over and ready to reuse, but the rest of this expanded scope has
happened yet. Next step whenever this batch is picked back up: scope one of those features as its own
focused slice, same as every other batch in this project - don't try to do all of them in one pass. Note
Peace Candle's mob-suppression half can reuse slice 2's exact same `EntitySpawnPlacementRegistry` hook
(see slice 2's own writeup) - only its village-worldgen placement half needs new investigation.

**Scope, originally** (below, mostly historical now that slice 1 is done - kept for the reasoning, which
doesn't change):

**Batch 7, slice 1 (done), 2026-09-26: Mixin→coremod conversion.** Converted both existing Mixins
(`FireBlockMixin`, `SuperLubricentBootsMixin`) to coremod JS transformers, following the plan below
almost exactly as scoped. What actually happened:
- Ground-truthed the *current* mapped-jar bytecode fresh via `javap -c` for both `FireBlock.tryCatchFire`
  and `LivingEntity.travel` (the Mixins' own javadocs described the redirects' *meaning*, not the exact
  instruction indices/stack shapes needed to hand-write ASM) - confirmed exactly one call to
  `BlockState.getFlammability` and exactly three calls to `Random.nextInt(I)I` in `tryCatchFire` (ordinals
  0/1/2, matching the Mixin's own `ordinal = 1`/`ordinal = 2` targets), and exactly one call to
  `BlockState.getSlipperiness` in `travel`.
- New `transformer/FireBlockTransformer.js`: a full redirect (insert `ALOAD 0` then retarget the
  `getFlammability` call itself to `AsmHandler.boostFlammability(...)`) plus two `@ModifyArg`-style
  argument rewrites (insert `ALOAD 0` + a static call *before* each of the 2nd/3rd `nextInt` calls,
  leaving those calls themselves untouched) - collects matching instruction nodes by object reference in
  one pass first, then inserts relative to those references, rather than inserting while iterating by
  index (which would shift every later index out from under the loop - a real bug caught and fixed before
  it ever ran).
- New `transformer/SuperLubricentBootsTransformer.js`: simpler - `travel`'s `getSlipperiness` call already
  passes `this` (the `LivingEntity` executing `travel`) as its own 3rd argument (the "entity standing on
  this block" parameter), so no instruction insertion was needed at all, just mutating the existing call's
  opcode/owner/name/desc in place to retarget it at `AsmHandler.bootsMaxSlip(...)`.
- New static targets added to `lumien.randomthings.asm.AsmHandler` (`boostFlammability`,
  `lowerDieOutBound`, `fasterAgeGrowth`, `bootsMaxSlip`) - same ground-truthed logic the two Mixins had,
  including the `instanceof BlazingFireBlock`/`instanceof SuperLubricentBootsItem` guards; the boots one
  drops the temporary `RT_BOOTS_DEBUG_LOG` diagnostic logging as planned, since it already served its
  purpose narrowing down the old Mixin-specific bug.
- Registered both in `META-INF/coremods.json` alongside the existing `VertexLighterFlat` entry.
- **Removed all Mixin infrastructure entirely**: deleted `mixin/FireBlockMixin.java`,
  `mixin/SuperLubricentBootsMixin.java`, `randomthings.mixins.json`; removed from `build.gradle` - the
  `org.spongepowered:mixingradle` buildscript dependency, the sponge maven repo, the
  `org.spongepowered.mixin` plugin, the whole `mixin { ... }` block, the `annotationProcessor`/
  `compileOnly`/disabled-`implementation` mixin dependencies and their explanatory comments, the
  `MixinConfigs` jar-manifest attribute, and the mixin-runtime jar-shading block. This project no longer
  depends on Mixin at all - the entire investigation chain documented earlier in this plan file (Mixin
  bootstrap gaps, launcher-library dead ends, etc.) is now moot for this project going forward, kept only
  as historical record of why the coremod path was chosen.
- Updated the stale javadoc mentions of `SuperLubricentBootsMixin` in `SuperLubricentIceBlock`,
  `SuperLubricentPhysics`, `SuperLubricentPlatformBlock`, `SuperLubricentStoneBlock`, and
  `SuperLubricentBootsItem` to point at the new transformer/AsmHandler method instead.
- **Verification, real this time**: full `./gradlew build -x test` clean. Then an actual `runClient`
  smoke test specifically to check the coremods apply at runtime (not just compile) - confirmed in the
  log: both new coremods `Loaded successfully`, both registered against the correct target classes
  (`net/minecraft/block/FireBlock`, `net/minecraft/entity/LivingEntity`), both classes actually
  `Transforming ...` logged, and - critically - **no `VerifyError`/`LinkageError`/`ClassFormatError`
  anywhere**, with the client reaching the same "registries frozen → all 4 texture atlases created"
  success point as every other clean run this session. Since `FireBlock` and `LivingEntity` are two of
  the most heavily-instantiated classes in the entire game, this is strong (though not conclusive)
  evidence the bytecode surgery is stack/descriptor-correct - actual gameplay behavior (fire spread speed
  near Blazing Fire, boots making every surface slippery) still needs a real in-game test, which is what
  `TESTING_CHECKLIST.md` #69/#70 and #134/#135 are now waiting on (updated from BLOCKED to
  "unblocked, needs a fresh test" - this is the *first* time the boots' fix has had a real chance to prove
  itself, since the Mixin version was never confirmed working at all).

**Batch 7, slice 2 (done), 2026-09-26: Slime Cube spawn-ALLOW + Lapis Lamp spawn-prevention.** User
picked this from 4 offered candidates (Magic Hood, Rain Shield, the Redstone Interface family were the
others, not picked - Redstone Interface flagged up front as big enough to need its own multi-slice
breakdown, not a single pass). What actually happened, and how it diverged from the original guess:
- The plan's own "one more check before committing to a coremod" instinct paid off, just not where
  expected - fresh `javap -c` on the *current* mapped jar found something better than either row's own
  checklist note assumed. `WorldEntitySpawner` (the 1.12.2-named class the notes pointed at) turned out to
  be almost a dead end in 1.14.4 - the real per-mob checks now live in each mob's own registered
  `EntitySpawnPlacementRegistry.IPlacementPredicate` (`SlimeEntity.func_223366_c` for Slime, `MonsterEntity
  .func_223323_a`/`func_223325_c` for the light-level check most hostile mobs use) - **but all of those
  predicates funnel through exactly one shared dispatch method, `EntitySpawnPlacementRegistry.func_223515_a`**
  (confirmed via `javap -c`: look up the registered predicate for this `EntityType`, call it, return its
  raw boolean, one `ireturn` total). That one shared choke point, not two separate per-mob head-injections,
  is what actually needed patching - intercepting it once fixes the ALLOW direction for every mob type at
  once (Slime Cube today, any future coremod-dependent spawn-control feature - e.g. Peace Candle's
  mob-suppression half - for free).
- New `AsmHandler#overrideSpawnResult(boolean, EntityType<?>, IWorld, BlockPos)` - ported the exact logic
  the two (now-removed) `LivingSpawnEvent.CheckSpawn` listeners had (`SlimeCubeTileEntity.cubes` lookup for
  Slime; a 4-block proximity scan for `ModBlocks.LAPIS_LAMP`/`QUARTZ_LAMP` for anything classified
  `EntityClassification.MONSTER`), just now actually reachable for the ALLOW direction.
- New `transformer/SpawnPlacementTransformer.js` - a plain value-rewrite right before the method's single
  `ireturn` (push `EntityType`/`IWorld`/`BlockPos` - already-available method params, no local-variable
  bookkeeping or control-flow injection needed - and redirect through `AsmHandler`), same "insert a static
  call before an existing instruction" pattern already used for `FireBlockTransformer.js`'s `@ModifyArg`
  cases, not the more invasive head-injection-with-early-return pattern that seemed likely before the
  shared-dispatch-point discovery.
- **Removed both old `LivingSpawnEvent.CheckSpawn` listeners from `RandomThings`'s constructor entirely**
  (Slime Cube's and Lapis/Quartz Lamp's) rather than keeping them alongside the new coremod - they'd have
  been pure duplicate logic checking the exact same conditions one step later, since the coremod now
  handles the DENY direction too (which the old listeners already did correctly).
- **Verification needed a different approach than slice 1's**, and this is worth remembering for future
  coremod work in this project: `EntitySpawnPlacementRegistry` isn't touched during a plain `runClient`
  boot to the main menu at all (nothing calls it before an actual world starts spawning mobs), so - unlike
  `FireBlock`/`LivingEntity` in slice 1 - a `runClient` smoke test alone couldn't confirm this one's
  bytecode was even valid, only that the coremod *loaded* and *registered* its transformer. Built a
  standalone verification harness instead (`VerifyCoremod.java`, compiled and run with a plain `java -cp`
  against `asm`/`asm-tree`/`asm-analysis`/`asm-util` 6.2 + the real mapped Forge jar): loads the real
  `EntitySpawnPlacementRegistry.class` bytes, runs the *actual shipped* `SpawnPlacementTransformer.js`
  against it via a JDK8 Nashorn `ScriptEngine` (not a copy - the real file), re-serializes with
  `ClassWriter.COMPUTE_FRAMES`, and runs `org.objectweb.asm.util.CheckClassAdapter.verify` (a full
  bytecode verification pass, the same kind the JVM itself does) on the result - zero errors, "class
  re-serialized cleanly." (Tried the `jjs` CLI shell first for this and hit a wall - Nashorn's `-cp` flag
  parsing on this specific JDK8 Temurin build rejects any semicolon-joined multi-jar classpath value
  outright, no combination of `-cp`/`-classpath`/`--classpath=`/quoting worked around it, from both Git
  Bash and PowerShell; switched to driving Nashorn's `javax.script.ScriptEngineManager` from a tiny Java
  program instead, which uses `java`'s own ordinary and unambiguous `-cp` parsing - not a Nashorn
  limitation worth fighting further, just use the `ScriptEngine` API directly next time instead of the
  `jjs` shell for anything beyond a one-liner.) Actual in-game behavior (Slime Cube ALLOW, Lapis Lamp
  spawn-prevention) still needs a real playtest - `TESTING_CHECKLIST.md` #29 and #110/#111 updated from
  BLOCKED to "unblocked, needs a fresh in-game test," same as slice 1's rows. `WIKI_FEATURE_STATUS.md`
  updated for both features.

**Batch 7, slice 3 (done), 2026-09-26: Magic Hood.** User picked this over Rain Shield (the other
offered candidate) from a 2-option choice. **Corrected a wrong assumption from this same plan file
before starting**: the "corrected & consolidated ASM/coremod batch" section above claims Magic Hood's
nametag-hiding half has a clean replacement (`RenderLivingBase.canRenderName` → `RenderNameTagEvent`) -
fresh `javap`/jar-content-check against this project's actual Forge 28.2.26 jar found no such event
class exists at all (`RenderNameplateEvent` was added to Forge in a later version than this project
targets). The particle-hiding half, which that same section flagged as needing "one more check before
committing to a coremod... 1.14.4 may have grown a clean hook," turned out to be the one that DID get a
clean event - the exact opposite split from what the plan guessed, but the total coremod work needed
ended up smaller either way (one redirect, not two). Lesson worth remembering for the remaining Batch 7
candidates (Peace Candle, Rain Shield, the Redstone Interface family, Spectre Illuminator, Special
Chest's worldgen placement): re-verify every "clean event exists" claim in that section against the real
jar before relying on it, the same as every other assumption in this project - some of those guesses
were made without ever running `javap` against this specific Forge version.
- New `item/MagicHoodItem.java` - plain `ArmorItem`, `ArmorMaterial.CHAIN`, `EquipmentSlotType.HEAD`, no
  crafting recipe (dungeon-chest-only in 1.12.2 too, same as Golden Egg/Summoning Pendulum). Indestructible
  for free - `Item.isDamageable()` already just checks `getMaxDamage() > 0`, and this item's `Properties`
  never sets one, so no override needed at all (1.12.2's version explicitly overrode both `getMaxDamage`
  and `isDamageable` to get the same result 1.14.4 gives by default).
- **Nametag half**: new `transformer/MagicHoodTransformer.js` + `AsmHandler#overrideCanRenderName`,
  redirecting `LivingRenderer.canRenderName(T)`. Ground-truthed via `javap -c`: this method has 6 separate
  early-exit branches, each ending in its own `ireturn` (unlike every previous Batch 7 redirect, which had
  exactly one) - handled by collecting all 6 `IRETURN` instruction nodes by reference first, then applying
  the same "insert a static call right before it" wrap to each one, the same simple value-rewrite pattern
  as before, just repeated. No control-flow injection needed even with 6 exit points.
- **Particle half**: a plain `MinecraftForge.EVENT_BUS` listener for
  `net.minecraftforge.event.entity.living.PotionColorCalculationEvent` in `RandomThings`'s constructor -
  a real, mutable event (`event.shouldHideParticles(true)`) that already exists in this Forge version
  for exactly this purpose. No coremod, no `AsmHandler` entry, nothing else needed.
- Full `./gradlew build -x test` clean, compiled clean first try. `runClient` smoke test confirmed
  `MagicHoodTransformer` loads and - unlike slice 2's `EntitySpawnPlacementRegistry` - `LivingRenderer` IS
  touched during a plain boot-to-menu (needed for the main menu's player-model preview), so this one
  *did* show up as `Transforming net/minecraft/client/renderer/entity/LivingRenderer` in the log with no
  `VerifyError`/`LinkageError`, real in-game confirmation this time rather than needing the standalone
  harness again. `TESTING_CHECKLIST.md` #252-255 added (new feature, not a fix to an existing row), not
  yet tested in-game. `WIKI_FEATURE_STATUS.md` updated.

**Batch 7, slice 4 (done), 2026-09-26: Rain Shield.** User picked this over the (now cheaper-than-scoped)
Peace Candle mob-suppression-only alternative offered alongside it.
- New `block/RainShieldBlock.java`, `tileentity/RainShieldTileEntity.java` - direct port of 1.12.2's
  `BlockRainShield`/`TileEntityRainShield`, same "shields" static-registry pattern already used by
  `SlimeCubeTileEntity`/`TileEntityPeaceCandle`. Candle-on-a-stick custom model (two cuboid elements,
  ported verbatim from 1.12.2's own `elements` JSON), needs solid ground underneath (`isValidPosition`,
  auto-breaks/drops otherwise - vanilla's own engine handles that once the override exists, no manual
  neighbor-change bookkeeping needed, matching `EnderMailboxBlock`'s established precedent), redstone
  toggles active/inactive the same way `IronDropperBlock`/`RedstoneObserverBlock` etc. already do
  (`world.isBlockPowered`).
- **Server-side mechanical suppression**: new `transformer/RainShieldTransformer.js` +
  `AsmHandler#overrideIsRainingAt`, redirecting `World.isRainingAt(BlockPos)`. Ground-truthing here found
  1.14.4 actually *simplified* what 1.12.2 needed: 1.12.2 had two separate ASM entry points
  (`World.shouldRain`/`canSnowAt`, both just delegating to the same `TileEntityRainShield.shouldRain`
  check) because vanilla itself queried rain and snow-eligibility through two different call sites back
  then; in 1.14.4 both collapse into this one shared method (confirmed via `javap -c` - farmland wetting,
  snow/ice formation, and fire-extinguishing near water/rain all read through it), so only one redirect
  was needed instead of two. Same multi-`ireturn` wrap pattern as `MagicHoodTransformer`'s `canRenderName`
  redirect (4 early-exit branches this time). Matches 1.12.2's own horizontal-only distance check and
  fixed 80-block range exactly. Disclosed simplification: dropped 1.12.2's `ConcurrentHashMap`-based
  per-position result cache (never invalidated on a shield's own power-toggle in the original, only
  implicitly stale until server restart) - not needed, and dropping it makes a shield's toggle take
  effect immediately instead of waiting out a stale cache entry, strictly more correct than what it
  replaces.
- **Client-side visual suppression deliberately NOT ported** - this is the one place this session's
  Batch 7 work stopped short of a full port, worth flagging clearly since every prior redirect fully
  matched 1.12.2. Ground-truthed why before deciding: 1.14.4's `GameRenderer.renderRainSnow` (the vanilla
  method drawing falling rain/snow) computes its own per-column skip condition *inline*
  (`world.getBiome(pos).getPrecipitation() == RainType.NONE`, confirmed via `javap -c`), not by calling
  `World.isRainingAt` - so the server-side redirect above has no effect on what a player visually sees.
  Reproducing the visual half would mean injecting a conditional skip *inside a loop body* deep in a
  ~300-line rendering method, not wrapping a single return value like every redirect so far this batch -
  a materially different (and materially more invasive) kind of ASM surgery for a purely cosmetic
  mismatch (rain still appears to visually fall in the shielded area even though it has zero mechanical
  effect there). Disclosed in `TESTING_CHECKLIST.md` #261 rather than silently dropped.
- Full `./gradlew build -x test` clean (one round of fixes: `BlockState.isSolidSide(...)` doesn't exist in
  this version, only the parameterless `isSolid()` - caught immediately by the compiler, and
  `AxisAlignedBB`/`getBoundingBox` was the wrong 1.12-era API for the block's shape too, corrected to
  `VoxelShape`/`getShape` matching `EnderMailboxBlock`'s established convention before it ever reached the
  compiler). `runClient` smoke test confirmed `RainShieldTransformer` loads and `World` itself - one of
  the most heavily-used classes in the entire game, loaded within the first couple thousand log lines -
  transforms with no `VerifyError`/`LinkageError`. `TESTING_CHECKLIST.md` #256-261 added.
  `WIKI_FEATURE_STATUS.md` updated (marked PARTIAL given #261's disclosed gap, not DONE-UNTESTED outright).

**Not started** (original text below, describing what led to the expanded scope):
section above, per explicit user request ("get the list of all items that used ASM in 1.12.2, flag
them, separate them into their own batch instead of hitting them randomly"). Originally this batch was
just "convert 2 existing Mixins to coremods" (pure infra cleanup, zero new content). It now also covers
every wiki feature whose 1.12.2 implementation needed one of `ClassTransformer.java`'s 17 bytecode
patches and has no clean Forge-1.14.4 event/hook replacement — see that section for the full
per-feature breakdown and reasoning. Both halves use the exact same mechanism (a Forge coremod, proven
self-contained via the already-working `transformer/VertexLighterFlat.js`), so building the pattern
once and knocking out everything that needs it in one focused pass is strictly better than re-deriving
it piecemeal per-feature or leaving those features permanently mischaracterized as "blocked."

**New content this batch now delivers** (previously mischaracterized as structurally blocked, or simply
not yet scoped as needing this infra at all): Slime Cube's spawn-ALLOW half (#110-111 — re-verify this
status changes once a coremod, not a Mixin, backs it), Peace Candle (both its mob-suppression and its
village-worldgen placement), Rain Shield, the Redstone Interface family (Advanced Redstone
Interface/Redstone Interface/Redstone Activator/Redstone Remote's wireless signal), Magic Hood's
potion-particle-hiding half, Spectre Illuminator, and Special Chest's ocean-monument worldgen placement.
Still placed last in the roadmap: everything else (Batches 2-6, and any of these features' own
non-coremod-dependent parts, e.g. Magic Hood's nametag-hiding half or Peace Candle's own block/TE)
doesn't depend on this and can ship independently first; this is the infrastructure investment that
unblocks the remainder once made.

**Original scope, unchanged:** this doesn't port any 1.12.2 feature that isn't already ported, it just
changes *how* two already-implemented features (Blazing Fire's catch-chance/age-growth tuning, Super
Lubricent Boots) get their bytecode hook wired up at runtime. Doing it earlier would just mean
re-touching these files again if a Batch 2-6 slice needed its own Mixin later - better to do the whole
migration once, covering everything that ended up needing it, in a single pass.

**Why this exists at all:** see the "Mixin's launch-plugin half cannot come from a mods-folder jar,
2026-09-25" and "Coremod conversion identified as the real long-term fix, not yet started, 2026-09-25"
progress-log entries above for the full investigation. Short version: SpongePowered Mixin, as integrated
in this project (`org.spongepowered:mixin:0.8` + MixinGradle), cannot fully bootstrap from a self-contained
mod jar on Forge 1.14.4 - it needs an `ILaunchPluginService` registered from an early ModLauncher classpath
scan that happens *before* the `mods` folder is even scanned, confirmed from Mixin's own source
(`MixinTransformationService.initialize()`). A mod's own jar structurally cannot satisfy that, so end users
would need a separately-installed library, which the user (owner of this project) explicitly doesn't want.
Forge's own coremod system (`META-INF/coremods.json`, JS-based ASM transformers) has no such requirement -
already proven working self-containedly in this exact project for `transformer/VertexLighterFlat.js`.

**Scope - exactly two files to convert, both small:**
- `src/main/java/lumien/randomthings/mixin/FireBlockMixin.java` → a new coremod JS file (e.g.
  `transformer/FireBlockTransformer.js`, following `VertexLighterFlat.js`'s existing structure/conventions
  in this repo). Needs to reproduce: the `@Redirect` on `BlockState.getFlammability(...)` (4x catch-chance
  multiplier when `this instanceof BlazingFireBlock`) and the two `@ModifyArg`s on
  `Random.nextInt(int)` calls inside `FireBlock.tryCatchFire` (faster age growth / wider die-out
  distribution) - see the existing Mixin's own javadoc and inline comments for the exact ground-truthed
  behavior each one reproduces, that reasoning doesn't change, only the mechanism does.
- `src/main/java/lumien/randomthings/mixin/SuperLubricentBootsMixin.java` → a new coremod JS file (e.g.
  `transformer/SuperLubricentBootsTransformer.js`). Needs to reproduce the single `@Redirect` on
  `BlockState.getSlipperiness(IWorldReader, BlockPos, Entity)` inside `LivingEntity.travel` (boots worn +
  not sneaking → force max slip). Same ground-truthing (from `AsmHandler.slipFix` in the 1.12.2 original)
  still applies, only the mechanism changes. The temporary diagnostic logging added this session
  (`RT_BOOTS_DEBUG_LOG`, the `travel()` HEAD probe) should be removed as part of this conversion, not
  carried into the coremod version - it served its purpose narrowing down the Mixin-specific bug and
  isn't needed once coremods sidestep that whole failure mode.

**Ground-truthing still required, same as every other slice in this project:** `javap -c` disassembly of
the real target methods (`LivingEntity.travel`, `FireBlock.tryCatchFire`) against the mapped Forge jar is
still the right way to find exact method descriptors and the precise bytecode instructions to target -
that groundwork is already done (see the existing Mixin classes' own javadocs, which document exactly
what was found), this batch is about re-expressing the same already-verified redirects as ASM
transformations instead of re-deriving them. `net.minecraftforge.coremod.api.ASMAPI` (used by
`VertexLighterFlat.js` already) has helper methods for common patterns like this (finding a method node,
locating a specific instruction, inserting/replacing instructions) - read that existing file first for
this project's own established conventions before writing new ASM code from scratch.

**When done:** the `org.spongepowered:mixin:0.8` dependencies (`annotationProcessor`, `compileOnly`, and
the currently-disabled `implementation` in `build.gradle`), the `org.spongepowered.mixin` Gradle plugin,
the `mixin { add sourceSets.main, ... }` block, the `mixin/` package, `randomthings.mixins.json`, and the
`randomthings.refmap.json` generation can all be removed entirely - this project would no longer depend on
Mixin at all, closing out this entire investigation for good. Full `./gradlew build -x test` plus an
actual in-game test (dev client is fine for this - coremods don't have the Mixin dev-client limitations
documented earlier in this plan) are both real verification steps here, not just a compile check, since
the whole point is these need to actually apply at runtime.

### Special Chest: ocean monument worldgen placement, 2026-09-27

User asked to continue worldgen work after the tooling-needs answer above. Unlike Peace Candle's village
case, ocean monuments in 1.14.4 are still the older hardcoded-Java-piece system
(`OceanMonumentPieces.MonumentCoreRoom`, confirmed via the sources jar - not jigsaw), so a coremod (the
project's own proven, working mechanism) could hook it directly, no external tooling needed.

Ground-truthed 1.12.2's real mechanism first (`git show origin/1.12.2:.../worldgen/WorldGenOceanChest.java`
+ the matching `ClassTransformer.patchOceanMonument` ASM patch): an injection into
`StructureOceanMonumentPieces$MonumentCoreRoom.addComponentParts` that places a water-variant Special
Chest at local (6,1,6) - computed via the room's own facing (`getCoordBaseMode()`) and bounding box - then
eager-fills it with `LootTableList.CHESTS_JUNGLE_TEMPLE` plus a guaranteed 50/50 Bottle of Air/Water
Walking Boots.

**Ported**: new `transformer/OceanMonumentTransformer.js` (registered in `META-INF/coremods.json`) inserts
a call to a new `AsmHandler#placeSpecialChest` right before `addComponentParts`'s single `ireturn` -
unlike every other redirect in this project so far, this one doesn't rewrite the return value
(`placeSpecialChest` is `void`), so it's a pure side-effecting insert, simpler than the
collect-every-ireturn-and-wrap pattern `RainShieldTransformer`/`MagicHoodTransformer` needed.
`getXWithOffset`/`getYWithOffset`/`getZWithOffset` are reimplemented in `AsmHandler` (ground-truthed from
`StructurePiece`'s own decompiled source - identical logic to 1.12.2's) since they're still `protected` on
`StructurePiece` in 1.14.4 (confirmed via `javap -p`) and not directly callable from outside; `
getBoundingBox()`/`getCoordBaseMode()` are public though, so no reflection needed anywhere this time
(1.12.2 needed it for the package-private `setBlockState`). Placement goes straight through `IWorld
#setBlockState` instead.

**Loot uses 1.14.4's own lazy-fill convention** (`LockableLootTileEntity#setLootTable`, rolled on first
open) instead of 1.12.2's eager `fillInventory` at generation time - ground-truthed as the *established*
1.14.4 idiom, not a deviation (`StructurePiece#generateChest`, vanilla's own dungeon-chest helper, does the
exact same thing). The guaranteed rare item is expressed as a second guaranteed pool in a new
`data/randomthings/loot_tables/chests/special_chest_water.json`, which also nests a
`minecraft:loot_table` entry referencing vanilla's real `chests/jungle_temple` table for the general loot
half (confirmed via `TableLootEntry`'s decompiled source that this recursively rolls the *whole* referenced
table, not just picks one entry from it) - matching 1.12.2's `LootTableList.CHESTS_JUNGLE_TEMPLE` exactly.

**Verification, matching this project's established precedent for "target class isn't touched during a
plain client boot"** (same situation as Batch 7 slice 2's `EntitySpawnPlacementRegistry` redirect -
`MonumentCoreRoom` is only loaded once actual ocean monument generation happens): built a standalone
`VerifyCoremod.java` harness (compiled against ASM 6.2 + the real mapped Forge jar, run via `java`, not the
`jjs` CLI which had the same classpath-parsing issues noted before) that loads the *real*
`OceanMonumentPieces$MonumentCoreRoom.class` bytes, runs the actual shipped `OceanMonumentTransformer.js`
against it via Nashorn (`ScriptEngineManager`/`Invocable`, not global bindings), re-serializes with
`ClassWriter.COMPUTE_FRAMES`, and runs `CheckClassAdapter.verify` - clean, no errors. Also did a full
`runClient` boot: `OceanMonumentTransformer` shows `CoreMod loaded successfully` and the boot proceeds all
the way to `All registries frozen` + texture atlas creation with no `VerifyError`/`LinkageError`, so the
coremod itself doesn't break anything else in the pipeline. Actual in-game generation (finding a real
ocean monument and confirming the chest/loot) still needs a real playtest - `TESTING_CHECKLIST.md` #271
added. `WIKI_FEATURE_STATUS.md`'s Special Chest note updated. `./gradlew build -x test` clean. Left
uncommitted per standing instruction.

### Ancient Furnace: real biome reassignment implemented, 2026-09-27

User asked what tooling was needed for the two deferred worldgen gaps (Peace Candle's village-church
generation, Ancient Furnace's biome reassignment). Re-investigating Ancient Furnace's before answering
found the earlier "no supported public API" claim was simply wrong - `Chunk.getBiomes()` returns the
chunk's own live `Biome[]` array (confirmed via the decompiled source, not a defensive copy), and
`IChunk.getBiome(BlockPos)` - what every gameplay biome read in the game goes through - indexes straight
into that same array fresh every call (`getBiomes()[z<<4|x]`, one biome per column). Mutating it in place
is a legitimate, no-reflection biome reassignment.

Implemented for real: `AncientFurnaceTileEntity.warmSurroundings` now looks up each column's current
biome, checks it against a cold->warm conversion table (ground-truthed from 1.12.2's own
`AncientFurnaceConversion` map, re-mapped onto 1.14.4's renamed biome fields via `javap` against
`Biomes` - e.g. `ICE_PLAINS`->`SNOWY_TUNDRA`, the "Mutated" biomes became "_Mountains"/"_Hills"-suffixed
names), and if it matches, mutates `world.getChunkAt(pos).getBiomes()[...]` to the warm counterpart and
marks the chunk dirty for resave. Snow/ice melting is now gated on that same per-column match (previously
unconditional everywhere in radius - tightened to match 1.12.2's own conditional structure now that the
real check exists).

**Disclosed gap kept**: the mutation is correct immediately for anything read server-side (spawn tables,
weather), but an already-connected client's visual grass/foliage/fog color won't update until the chunk
reloads - that's the client's own separate copy of the biome data, and live-resyncing it would need the
server to send a full `SChunkDataPacket` to every tracking player, a meaningfully bigger change than the
mutation itself, not attempted. Also kept: this port's existing instant-fixed-radius pacing rather than
1.12.2's real per-tick flood-fill-outward mechanism (a pre-existing, already-accepted simplification, not
something this change touched).

`./gradlew build -x test` clean. `WIKI_FEATURE_STATUS.md` and `TESTING_CHECKLIST.md` #265 updated. Left
uncommitted per standing instruction.

### Fluid Display rendering: folded bottom face + lighting darkening, 2026-09-27

User reported (with a screenshot) that a placed Fluid Display looked wrong three ways: a "blank" one
showed a flat pale quad instead of nothing, and water/lava both looked twisted/non-cube-shaped and almost
black instead of their real colors.

Two confirmed, concrete bugs found by reading `FluidDisplayTileEntityRenderer`'s 6 quad calls carefully:
1. **Folded bottom face.** Every face's `quad(...)` call should have all 4 corners share that face's own
   constant axis value (e.g. every corner of the `+Y` top face has `y=1`) - true for 5 of the 6 faces, but
   the `-Y` (bottom) face's 3rd corner was `(0, 1, 1)` (`y=1`, on the TOP of the cube) instead of the
   correct `(1, 0, 1)`. That single wrong corner folds the bottom face diagonally up through the middle of
   the cube instead of it lying flat - exactly the twisted, non-cube shape in the screenshot. Fixed by
   correcting the corner list to `0,0,0, 0,0,1, 1,0,1, 1,0,0` (all `y=0`).
2. **Same lighting-darkening bug as Rune Base's fix** (see that entry above) - `TileEntityRendererDispatcher
   .render()` enables real `GL_LIGHTING` before calling any TESR, and this renderer's `POSITION_TEX_COLOR`
   vertices carry no normal data, so the lighting math darkened the whole textured cube toward black
   regardless of the fluid's actual tint. Fixed the same way: `GlStateManager.disableLighting()`/
   `enableLighting()` around the draw.

**The "blank" (no fluid) case is NOT fully explained** - ground-truthed `FluidStack.loadFluidStackFromNBT`
(the real Forge source) confirms it returns `EMPTY` for a compound with no `FluidName` key, and
`render()`'s existing `fluidStack == null || fluidStack.isEmpty()` early-return looks correct on
inspection - a genuinely-unset display should render nothing at all. Left as a real open question in
`TESTING_CHECKLIST.md` #234 rather than guessing at a fix with no clear code-level cause - possible this
was just the same folded-face bug viewed at an angle that looked like a flat panel rather than a twisted
cube, but that's a guess, not a confirmed diagnosis. `./gradlew build -x test` clean. Left uncommitted per
standing instruction.

**Follow-up, same day: user confirmed the quad shape is fixed, but found two more real bugs** ("the light
is definitely still off on the blocks faces", "the top block face is always transparent, never filled with
fluid texture"). Both traced to real causes:
1. **Top face always culled.** Computed each face's normal from its vertex winding order via the
   right-hand rule (cross product of the first two edge vectors) to check for consistency - all 4 side
   faces pointed outward correctly, but both `-Y` and `+Y` had their winding backwards (pointing inward).
   With back-face culling on (never disabled in this renderer, correctly so for an opaque cube), a
   backwards-wound face's *visible* side from outside the cube is exactly the side culling removes -
   invisible from the normal viewing angle, exactly matching "always transparent." The bottom face had the
   same defect (introduced by my own earlier fix, which only fixed the folding, not the winding) but is
   rarely viewed from below so wasn't independently reported. Fixed by reversing both faces' corner order
   (same 4 corners, same plane, opposite loop direction).
2. **Flat, unshaded lighting.** `disableLighting()` correctly stopped the darkening, but as a side effect
   made every face render at the exact same flat brightness - real blocks don't get their per-face shading
   from `GL_LIGHTING` either (that's an entity/item-rendering mechanism), they bake a direction-based
   multiplier directly into each face's vertex color at quad-build time. Ground-truthed the actual
   constants from `BlockModelRenderer`'s own per-direction table: UP 1.0, DOWN 0.5, NORTH/SOUTH 0.8,
   WEST/EAST 0.6. Added the same multiplier to each face's `(r,g,b)` tint here, matching what vanilla
   (and this project's own fluid-rendering javadoc claim, "exactly like vanilla's own fluid rendering
   does") actually does.

`./gradlew build -x test` clean. `TESTING_CHECKLIST.md` #234 updated again. Left uncommitted per standing
instruction.

### Systemic bug: TileEntity live sync silently did nothing without a relog, 2026-09-27

User reported the Rune Base rendering was STILL not showing up live (patterns only appeared after
relogging), even after the color4f/blendFunc/lighting fixes above. This turned out to be a third,
completely different, and much more consequential bug than either of the first two.

**Root cause**, found by reading Forge's own decompiled sources: `TileEntity.onDataPacket` (the method a
live `SUpdateTileEntityPacket` - sent via `getUpdatePacket()`/`world.notifyBlockUpdate`- is supposed to
arrive at client-side) **doesn't exist as a real method on vanilla's `TileEntity` class at all in this
Forge version.** It's a Forge-added *default* method on the `IForgeTileEntity` extension interface, and
that default is a literal empty no-op: `default void onDataPacket(NetworkManager net,
SUpdateTileEntityPacket pkt){ }`. Compare this to the *other* client-side sync path,
`handleUpdateTag(CompoundNBT)` (used only for the initial chunk-load sync - i.e. on rejoin/relog, via
`SChunkDataPacket`) - its Forge default DOES call `read(tag)`. So: a relog always worked (goes through
`handleUpdateTag`, which works out of the box), but any *live* update while already in the world went
through `onDataPacket`, which silently did nothing unless a subclass explicitly overrides it - which
nothing in this project ever did.

**This affects every tile entity in the project using the `getUpdatePacket()`/`notifyBlockUpdate` pattern,
not just Rune Base** - checked all of them: `RuneBaseTileEntity`, `BiomeRadarTileEntity`,
`FluidDisplayTileEntity`, `RedstoneObserverTileEntity`, `SoundDampenerTileEntity`. None had an
`onDataPacket` override. Fixed all five identically: `public void onDataPacket(NetworkManager net,
SUpdateTileEntityPacket pkt) { this.read(pkt.getNbtCompound()); }`, matching what `handleUpdateTag`
already does by default. This also **retroactively explains why "Bug-fix pass 4"'s #94 fix
(`RedstoneObserverTileEntity`, 2026-09-25) likely wasn't actually complete**: that fix corrected
`getUpdateTag()`'s payload (a real, separate bug - the default calls the private `writeInternal()`, not
`write()`) and added the `notifyBlockUpdate` call, believing that alone restored live sync - but without
this `onDataPacket` override, the live-update packet would have still silently done nothing on arrival. It
also corrects a wrong claim in that TE's own old comment (that singleplayer's integrated server "shares
this exact TE object" with the client, making live sync supposedly unnecessary there) - that was never
true; client and integrated server always have separate `World`/`TileEntity` instances even over the local
loopback connection.

**Worth remembering for any future tile entity in this project that needs live (not just reload) sync**:
overriding `getUpdatePacket()`/`getUpdateTag()` alone is not sufficient - `onDataPacket` must be overridden
too, or the packet arrives and is silently discarded. `./gradlew build -x test` clean.
`TESTING_CHECKLIST.md` updated (#95, #150-152, #158, #161, #235-236 all flagged for retest). Left
uncommitted per standing instruction.

**Confirmed working, 2026-09-27 (same day, user retested): Rune Dust now works great** - live
placement/pixel-adding shows up immediately, no relog needed. `TESTING_CHECKLIST.md` #161 marked `PASS`.
The other four TEs sharing this fix (#95, #150-152, #158, #235-236) are still unconfirmed - not yet
retested by the user, left flagged.

### Rune Base: left-click now clears the whole block, 2026-09-27

User confirmed the rendering fix works (patterns visible after relaunching with the new jar), then asked
for a real behavior change: left-clicking (not fully breaking) a Rune Base should drop every pixel on the
block at once, not just the one cell under the cursor.

Ground-truthed 1.12.2's real `BlockRuneBase#onBlockClicked` first, per this project's standing
convention - confirmed the one-pixel-per-click behavior this port already had was an exact match to the
original, not a bug. This is a deliberate user-requested deviation, not a fix, so it's flagged plainly as
one (in both the block's own javadoc and `TESTING_CHECKLIST.md`).

Implementation: `RuneBaseBlock.onBlockClicked` no longer computes a hit-cell x/y or mutates a single grid
entry - it now just validates the click actually hit this block, confirms the tile entity has pixels, and
calls `world.removeBlock(pos, false)`. That triggers `onReplaced`, which already contains the "drop every
non-null pixel as its own Rune Dust item" loop (used today when the support block is removed, or the
block is broken outright) - reusing it instead of duplicating the loop keeps one single source of truth
for "drop everything" rather than two copies that could drift. `./gradlew build -x test` clean.
`TESTING_CHECKLIST.md` #163 updated. Left uncommitted per standing instruction.

### Rune Base rendering fix: invisible placed block, 2026-09-27

User-reported bug: Rune Dust right-click placed "something" (block+data confirmed correct, since
left-click removed it and refunded the dust) but it rendered entirely transparent/invisible.

Root cause in `RuneBaseTileEntityRenderer.java`: it draws every rune pixel via per-vertex
`BufferBuilder.color(...)` (`DefaultVertexFormats.POSITION_COLOR`), but that vertex color is multiplied
by the *global* `GlStateManager.color4f` state, which persists across unrelated render calls and is never
reset automatically. The renderer never set it to opaque white before drawing - every other custom TESR
in this project that draws its own colors already does this defensively (`SpecialChestTileEntityRenderer`
sets/restores `color4f(1,1,1,1)` around its draw), just missed here since this is the only one using
per-vertex buffer colors instead of a single `color4f` call per shape (`RedstoneObserverLineRenderer`,
this project's other raw-color renderer, sets it per-vertex instead, same reasoning, different call
shape). Whatever alpha a previous frame's render call happened to leave behind (commonly near-zero)
silently multiplied every rune pixel down to invisible.

Fixed by adding `GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F)` right before the `Tessellator`/
`BufferBuilder` draw, plus an explicit `GlStateManager.blendFunc(SRC_ALPHA, ONE_MINUS_SRC_ALPHA)` (was
relying on whatever blend func happened to already be set, same class of leftover-GL-state bug even
though it wasn't the actual cause here since every vertex alpha is 1.0 anyway - fixed for correctness/
defensiveness, matching `RenderUtils.java`'s established blend-func pattern elsewhere in this project).
`./gradlew build -x test` clean. `TESTING_CHECKLIST.md` #161 updated. Left uncommitted per standing
instruction.

**Follow-up, same day: user retested, still invisible - color4f wasn't the (whole) story.** Dug further
and found `TileEntityRendererDispatcher.render()` (decompiled from the mapped sources jar) calls
`RenderHelper.enableStandardItemLighting()` - real fixed-function `GL_LIGHTING` with positioned lights -
immediately before invoking ANY TESR's own `render()`. Every other TESR in this project either draws a
real baked model (proper normals, lighting behaves correctly) or is `RedstoneObserverLineRenderer`, which
already calls `GlStateManager.disableLighting()` before its own raw-colored-primitive draw for exactly
this reason. `RuneBaseTileEntityRenderer`'s raw `POSITION_COLOR` vertices carry no normal data at all, so
with lighting left enabled, the lighting math ran against whatever normal happened to be globally set,
plausibly darkening every pixel toward invisible on top of the alpha issue already fixed. Added
`GlStateManager.disableLighting()`/`enableLighting()` around the draw, matching
`RedstoneObserverLineRenderer`'s established pattern. `./gradlew build -x test` clean.
`TESTING_CHECKLIST.md` #161 updated again, explicitly flagged as a second, still-unverified attempt at the
same bug since this can't be confirmed without an actual in-game test. Left uncommitted per standing
instruction.

### Lapis Lamp/Quartz Lamp: real peaceful-mode bug, mechanism reverted to match 1.12.2, 2026-09-27

User-reported bug: Lapis Lamp let hostile mobs spawn even on Peaceful difficulty, where they got
instantly deleted again (vanilla's own peaceful despawn check catching them a tick later). Initial plan
was to just add a `world.getDifficulty() != Difficulty.PEACEFUL` guard to `AsmHandler#overrideSpawnResult`
directly, but the user redirected before that landed: match 1.12.2's actual mechanism instead of patching
the coremod further.

**Root cause, confirmed via `git show origin/1.12.2:...`:** neither `BlockLapisLamp` nor `BlockQuartzLamp`
ever used ASM for this in 1.12.2 at all - Batch 7 slice 2 (2026-09-26) reinvented a more complex,
less-correct mechanism (force the spawn result via the shared `EntitySpawnPlacementRegistry` coremod
dispatch point) without re-checking what the original actually did first, and that's exactly why peaceful
broke: forcing the boolean result bypasses the *entire* real placement predicate, including whatever gates
sit upstream of it (peaceful-difficulty included). 1.12.2's real trick is much simpler and was hiding in
plain sight: `getLightValue(state, world, pos)` returns a different value depending on
`FMLCommonHandler#getEffectiveSide()` - Lapis Lamp reports 15 (lit) to the client but 0 (dark) to the
server, Quartz Lamp the exact inverse. Since the *real* vanilla placement predicate reads light level
itself, this makes it naturally allow/deny spawns without bypassing anything else it checks - peaceful
included, for free.

**Ported directly**, since 1.14.4's `Block.getLightValue` lost the `(state, world, pos)` overload (only
`getLightValue(BlockState)` survives, confirmed via `javap`) - side comes from
`net.minecraftforge.fml.common.thread.EffectiveSide.get()` instead (still present in this Forge version,
confirmed via `javap`), the modern equivalent of the 1.12.2 API. Also ported the `randomDisplayTick`
self-heal (renamed `animateTick` in 1.14.4, same "client-only, called every tick for nearby blocks
regardless of `ticksRandomly`" semantics, confirmed via the mapped sources jar's `ClientWorld.animateTick`)
that nudges the light engine (`world.getChunkProvider().getLightManager().checkBlock(pos)`, the 1.14.4
equivalent of `checkLightFor`) to recheck a position if the client ever sees a stale light value.

**Removed entirely**: the Lapis/Quartz Lamp 4-block proximity scan inside
`AsmHandler#overrideSpawnResult` (`ModBlocks`/`Block` imports dropped, now unused) - Slime Cube's ALLOW
half and Peace Candle's DENY half still use that dispatch point, unaffected, since neither has a
light-based equivalent available. `TESTING_CHECKLIST.md` #29/#30 and `WIKI_FEATURE_STATUS.md` updated -
#30 (Quartz Lamp) was previously `PASS` under the old mechanism but flagged `NEEDS RETEST` since the
underlying code changed completely even though it should behave the same. `./gradlew build -x test`
clean. Left uncommitted per standing instruction.

**Reverted the same day: user retested, found it completely backwards** (Lapis Lamp started blocking
spawns, Quartz Lamp stopped blocking them). Root cause, found by reading
`net.minecraftforge.fml.common.thread.EffectiveSide`'s own source directly: `get()` returns
`LogicalSide.CLIENT` for *any* thread that isn't part of FML's own `SidedThreadGroup` - `return group
instanceof SidedThreadGroup ? ((SidedThreadGroup) group).getSide() : LogicalSide.CLIENT;`. 1.14.4's
block-light propagation for a placed block apparently doesn't reliably run on that specific thread (chunk/
light processing can happen on background worker threads FML doesn't tag as sided), so the "server" branch
of the trick silently defaulted to the client value where it mattered most - not a viable mechanism for
this Forge version's light engine regardless of how faithfully it matches 1.12.2's actual source. Real
lesson for this project: "ground-truth against the real jar" caught the *API surface* (does the method
exist, what's its signature) but not this *runtime threading* mismatch - worth remembering that some
1.12.2 mechanisms don't survive intact even when the equivalent API technically exists.

**Back to the coremod force-ALLOW/DENY scan** (restored `AsmHandler#overrideSpawnResult`'s lamp branch,
`ModBlocks`/`Block` imports re-added), now with an explicit `Difficulty.PEACEFUL` guard on the ALLOW
branch only (`world.getWorldInfo().getDifficulty() != Difficulty.PEACEFUL` - Quartz's DENY branch needs no
guard, forcing DENY on Peaceful is harmless since nothing would spawn there anyway) - this fixes the
original Peaceful bug's actual root cause (a missing check) directly, without depending on side-detection
proven unreliable here. `LapisLampBlock`/`QuartzLampBlock` reverted back to plain static `Block.Properties`
(no `getLightValue`/`animateTick` overrides). `./gradlew build -x test` clean. `TESTING_CHECKLIST.md`
#29/#30 and `WIKI_FEATURE_STATUS.md` updated again to reflect the revert. Left uncommitted per standing
instruction.

**Confirmed working, 2026-09-27 (same day, user retested): both lamps behave correctly** - Lapis Lamp
allows spawns (no more spawn-then-despawn on Peaceful), Quartz Lamp blocks them. `TESTING_CHECKLIST.md`
#29/#30 and `WIKI_FEATURE_STATUS.md` marked `PASS`/`DONE`. This whole investigation (three real bugs
found across two behavior reverts in one day) is now fully closed out.

### Super Lubricent Boots speed cap, 2026-09-27

User request: cap the boots' max horizontal speed to match the three Super Lubricent blocks' own cap
(`SuperLubricentPhysics.MAX_HORIZONTAL_SPEED`, 0.35 blocks/tick). Previously uncapped - the boots make
every surface maximally slippery via a coremod (`AsmHandler#bootsMaxSlip`, `LivingEntity.travel`'s
friction lookup, see #134's writeup), but nothing watched for that on ordinary ground the way the
existing `LivingUpdateEvent` listener already did for the three actual Super Lubricent blocks, so it
could accelerate indefinitely there.

Extended that same listener (`RandomThings.java`, right after the block-only speed cap) rather than
adding a new one or touching the coremod: now also fires whenever Super Lubricent Boots are worn and the
wearer isn't sneaking - the exact same condition `bootsMaxSlip` itself checks - regardless of the block
underfoot, then reuses `SuperLubricentPhysics.capHorizontalSpeed` unchanged. No new files, no coremod
changes. `./gradlew build -x test` clean. `TESTING_CHECKLIST.md` #134 updated with a note to test
alongside the rest of that row (not yet tested in-game). Left uncommitted per standing instruction.

### Worldgen batch (done), 2026-09-27: Ancient Furnace, Peace Candle

Shipped by a separate session/container (no access to this plan file — reconstructed scope from the raw
wiki JSON data instead), commit `3e60013`. Ports the two remaining worldgen-tagged wiki features
(previously both NOT STARTED, deferred to "a future worldgen batch" - see roadmap item 4 above, now
marked done).

- **Ancient Furnace**: rare `Feature`-based surface embed in cold biomes (temperature-gated inside
  `AncientFurnaceFeature`, same per-attempt pattern `PitcherPlantFeature` already established).
  Right-clicking the top with a Nether Star starts a heating countdown via a real `HEATING` BlockState
  property (vanilla-furnace-`LIT`-style, so `animateTick` can render it with no custom TE sync);
  completion melts surrounding surface snow/ice and explodes. **Disclosed simplification:** the wiki's
  "turn an area into a warmer biome" ships as its concretely observable effect (melt snow/ice in a
  ~56-block radius) rather than a literal biome-registry reassignment — 1.14.4 bakes biomes into each
  chunk's `BiomeContainer` at generation time with no supported runtime API to reassign them.
- **Peace Candle**: standalone decorative block + `PeaceCandleTileEntity` tracked in a static set (same
  pattern as `RainShieldTileEntity`/`SlimeCubeTileEntity`), extending the existing
  `SpawnPlacementTransformer` coremod hook to deny hostile spawns within a 3-chunk radius — no new
  coremod needed. **Disclosed gap:** the wiki's ~33% village-church generation is genuinely deferred, not
  a coremod candidate (see the correction in the Batch 7 section above) — 1.14.4's village generator is
  fully data-driven jigsaw structures with no hardcoded piece class left to hook into, and hand-authoring
  the NBT structure template data isn't feasible without in-game structure-block tooling this environment
  lacks. Block/mob-suppression works; natural village spawning doesn't exist yet, creative-menu only.
- Both `WIKI_FEATURE_STATUS.md` and `TESTING_CHECKLIST.md` (Slice 25, #262-270) updated as part of that
  commit. Not yet tested in-game.
- **This session (2026-09-27), on pulling that commit:** `./gradlew build -x test` failed — 9 compiler
  errors, all from `PeaceCandleTileEntity.java` missing its `net.minecraft.tileentity.TileEntity` import
  entirely (never compiled clean, or the error was introduced/missed before that container's commit).
  Fixed by adding the import; build verified clean afterward. Left uncommitted per standing instruction.

### Plate family, Special Chest, and Sakanade removed entirely, 2026-09-27

Explicit user decision: "remove the Plate Family and the Special Chests... I don't find them valuable
and don't want them migrated anymore," followed mid-turn by "Now remove Sakanade." All three were
undocumented bonus content (none appear in any of the 100 wiki pages) — not a bug fix or deviation, a
scope cut.

- **Plate family** (`block/plates/*`, 11 blocks + `PlateBlock` base): `AcceleratorPlateBlock`,
  `BouncyPlateBlock`, `CollectionPlateBlock`, `CorrectorPlateBlock`,
  `DirectionalAcceleratorPlateBlock`, `ExtractionPlateBlock` (+ TE/container/screen),
  `FilteredRedirectorPlateBlock` (+ TE/container/screen), `ItemRejuvenatorPlateBlock`,
  `ItemSealerPlateBlock`, `ProcessingPlateBlock` (+ TE/container/screen), `RedirectorPlateBlock`,
  `RedstonePlateBlock`. Deleted the whole `block/plates` package, the 3 GUI-backed TE/container/screen
  triples, and every blockstate/model/gui-texture/loot-table/lang entry referencing them.
- **Special Chest** (`SpecialChestBlock`, `SpecialChestTileEntity`, `SpecialChestItemRenderer`,
  `SpecialChestTileEntityRenderer`) — deleted along with the `OceanMonumentTransformer` coremod +
  `AsmHandler#placeSpecialChest`/`getXWithOffset`/`getYWithOffset`/`getZWithOffset` helpers added
  earlier this *same* session (see "Special Chest: ocean monument worldgen placement" entry above) —
  that worldgen hook only existed to place this block, so it went too, along with its
  `coremods.json` entry and the `special_chest_water` loot tables (block-drop + chest-contents).
  Checklist rows #100-103/#271 marked removed (rows #100/#102 had actually been FAILing anyway — a
  chest-opening crash from a stray vanilla `ChestType` blockstate property — so nothing regressed).
- **Sakanade** (`SakanadeBlock`, decorative shearable mushroom plant dropping `sakanade_spores`) —
  deleted block class, item registration, and all assets/lang. Checklist row #68 (previously
  `INVESTIGATING` — an unresolved placement-check question) marked removed, closing out that open
  investigation as moot.
- Removed each one's `ModBlocks`/`ModItems`/`ModTileEntityTypes`/`ModContainerTypes`/`ModScreens`
  `@ObjectHolder` fields and `registry.register(...)` calls, plus two stale doc-comment references to
  `SpecialChestTileEntityRenderer` in `RuneBaseTileEntityRenderer.java` (used only as a "see also"
  example in its javadoc, not a real dependency). `./gradlew build -x test` clean afterward.
  `WIKI_FEATURE_STATUS.md`'s "Not migrating" section rewritten to record all three as removed this
  session (previously listed under "kept, undocumented bonus content"). Left uncommitted per standing
  instruction.

### Spectre Illuminator implemented, 2026-09-27

User picked this from 4 offered NOT-STARTED candidates (Dyeing Machine, Spectre Lens, kicking off the
Redstone Interface family were the others, not picked). Full 1.12.2 source ground-truthed first
(`ItemSpectreIlluminator`, `EntitySpectreIlluminator`, `RenderSpectreIlluminator`,
`SpectreIlluminationHandler`/`ClientHandler`/`Helper`, `AsmHandler#overrideLightValue`/
`overrideLightValueClient`) via `git show origin/1.12.2:...` before writing anything.

- **The mechanism**: right-click places a floating orb (`SpectreIlluminatorEntity`) that drifts up to
  hover above the tallest block in its chunk, then centers itself horizontally over the chunk; once
  settled it makes every block in that chunk report as lit (light value 14). Right-click the orb to
  collect it back, turning the light off.
- **Real structural blocker found and worked around**: 1.12.2's ASM patch targeted `Block.getLightValue
  (state, world, pos)` directly. Ground-truthed via `javap -p` that this 3-argument overload doesn't
  exist in this Forge version at all - `Block.getLightValue` now takes only a `BlockState`, with no
  `world`/`pos` to inspect, so the original per-position injection point is structurally gone. Found
  the real 1.14.4 equivalent via `javap -c` on `BlockLightEngine`: its own `getLightValue(long)` calls
  `IBlockReader.getLightValue(BlockPos)`, a default interface method (`getBlockState(pos)
  .getLightValue()`) inherited by every `IBlockReader` implementor (`Chunk`, `World`, ...) with no
  override of its own anywhere (confirmed via `javap -p` on `Chunk`/`ChunkPrimer`) - a single shared
  choke point, same "one dispatch point" philosophy as `EntitySpawnPlacementRegistry`. New
  `IBlockReaderTransformer.js` coremod patches that one method; `AsmHandler#overrideLightValue(int,
  IBlockReader, BlockPos)` is the new redirect target - resolves the `Chunk` back to its `World` via
  `Chunk#getWorld()` and scans the same live-entity registry described below.
- **Three disclosed simplifications from 1.12.2** (full reasoning in `SpectreIlluminatorEntity`'s own
  javadoc and `WIKI_FEATURE_STATUS.md`'s note): (1) collapsed 1.12.2's separate persisted
  `WorldSavedData` "illuminated chunks" registry + custom client-sync network packet into one static
  `WeakHashMap`-backed `Set<SpectreIlluminatorEntity>` (`ILLUMINATORS`), matching this port's own
  established `SlimeCubeTileEntity.cubes`/`RainShieldTileEntity.shields` pattern exactly - the entity's
  ordinary vanilla position sync already gives the client everything it needs, so both sides just scan
  the same set. (2) Replaced 1.12.2's bespoke immediate-mode-GL "magic circle" render (`MKRRenderUtil`,
  the same framework `EclipsedClockEntity` already disclosed skipping) with a plain vanilla particle
  trail (`ParticleTypes.END_ROD`) spawned directly in `tick()` - the renderer class itself draws
  nothing, same approach as `ArtificialEndPortalEntity`/`EclipsedClockEntity`'s own cosmetic effects.
  (3) 1.12.2's item model was `"parent": "builtin/entity"` with no `ItemStackTileEntityRenderer` ever
  registered for it (confirmed via `git grep` across the whole 1.12.2 source) - would have rendered
  blank in-game, looks like an authoring oversight rather than an intentional design; ported as a
  normal flat `item/generated` icon instead so it's actually visible.
- New files: `entity/SpectreIlluminatorEntity.java`, `item/SpectreIlluminatorItem.java`,
  `client/renderer/SpectreIlluminatorEntityRenderer.java`, `transformer/IBlockReaderTransformer.js`,
  plus the item's model/texture/mcmeta (texture copied byte-for-byte from 1.12.2's own animated
  10-frame strip) and a crafting recipe (Ectoplasm + Glowstone Dust + Luminous Powder in a ring,
  ground-truthed against 1.12.2's `ItemIngredient` enum ordinals to find the modern per-item
  equivalents). Registered in `ModEntityTypes`/`ModItems`/`RandomThings.java`'s client renderer setup/
  `AsmHandler`/`coremods.json`/lang.
- **Verification**: generalized the existing `VerifyCoremod.java` bytecode-verification harness (it
  previously hardcoded the Ocean Monument target class) to take the target class name as a 3rd arg;
  running it against `net/minecraft/world/IBlockReader` needed the real Forge jar added to the
  verifier's *own* classpath too (not just read as raw bytes) - `CheckClassAdapter`'s `SimpleVerifier`
  needs to actually resolve unrelated Minecraft types referenced by `IBlockReader`'s *other* default
  methods (hit a `ClassNotFoundException: net.minecraft.util.math.Vec3d` from `rayTraceBlocks` before
  fixing this) - `CheckClassAdapter.verify` came back clean afterward. Then a real `runClient` boot
  (via PowerShell for the verifier, Bash+`timeout 180` backgrounded for the client) confirmed in the
  log: coremod `Loaded successfully`, `Transforming net/minecraft/world/IBlockReader` actually happens,
  no `VerifyError`/`LinkageError`/`ClassFormatError`/`FATAL` anywhere, `All registries frozen` and every
  texture atlas created cleanly afterward - same success bar as every other coremod this session. New
  item/entity's own assets loaded with no missing-model warning (unlike a pre-existing, unrelated
  `fluid_display` blockstate warning already in the log before this session). `./gradlew build -x test`
  clean. `TESTING_CHECKLIST.md` #272-274 added, `WIKI_FEATURE_STATUS.md` updated to DONE-UNTESTED.
  Never placed in a real world - needs an actual playtest to confirm the chunk really lights up.

### Spectre Illuminator: real bug fixed, 2026-09-27 (reported by user: no visual, no lighting)

First real playtest report on this feature. Root-caused by re-reading 1.12.2's actual client-side
network handler (`SpectreIlluminationClientHandler#setIlluminated`) rather than guessing - it revealed
two things this port's first pass got wrong, both from the "collapse the separate WorldSavedData +
network packet into a synced live-entity registry" simplification being incomplete:

1. `illuminated` was a plain field, synced only through NBT `readAdditional`/`writeAdditional` (save/
   load persistence, not live sync) - the entity spawn packet doesn't carry NBT additional data, so the
   client's own copy of the entity never learned the server had settled at all. Fixed by making it a
   real synced `DataParameter<Boolean>` (`registerData`/`dataManager.get`/`.set`), matching this port's
   own established pattern for anything the client needs to know (`ArtificialEndPortalEntity`'s
   `ACTION_TIMER`, `EclipsedClockEntity`'s `TARGET_TIME`).
2. Even with that fixed, syncing the boolean alone still wouldn't have been enough - 1.12.2's own
   client-side handler explicitly re-ran its light-recheck helper (`SpectreIlluminationHelper
   .lightUpdateChunk`) *client-side* upon receiving the toggle, mirroring the server's own call exactly.
   Knowing the flag changed doesn't itself force the client's light engine to recompute. `relight()` (the
   `checkBlock` sweep) now fires from both sides: the server's existing call on settling, plus a new
   `notifyDataManagerChange` override reacting to the synced value arriving client-side.
3. Found and fixed the mirror-image gap on pickup while in there, before it could become its own bug
   report: `onRemovedFromWorld` wasn't relighting at all, so collecting the orb back would have left the
   chunk stuck lit forever. `onRemovedFromWorld` is a generic Entity lifecycle hook (fires on both sides
   whenever an entity is removed from either World, not server-only) so one relight-if-was-illuminated
   check there covers both the server's `remove()` call and the client's own tracked-entity teardown,
   matching 1.12.2's `toggleChunk` always relighting symmetrically for both the on and off transition.

`./gradlew build -x test` clean. `TESTING_CHECKLIST.md` #273/#274 updated with the fix and reset to
needs-retest. Not yet re-confirmed in-game - this is a blind fix like everything else this session.

### Time in a Bottle: real crash fixed, 2026-09-27 (reported by user)

Same bug CLASS as the Spectre Illuminator fix above, found in a different entity minutes later -
worth naming the pattern since it may well recur: **a field set only by a secondary,
position/target-taking constructor is never visible to the client's own copy of the entity**, because
Forge always constructs that client-side copy via the bare `(EntityType, World)` constructor when
applying a spawn packet, and NBT `readAdditional`/`writeAdditional` only covers save/load persistence,
never the live spawn packet.

- `TimeAcceleratorEntity.target` (the block it force-ticks) was a plain `BlockPos` field, set only in
  `TimeAcceleratorEntity(World, BlockPos, double, double, double)` - the constructor `TimeInABottleItem`
  calls server-side. Client-side, `target` stayed `null` forever. `tick()` calls `world.getTileEntity
  (target)` unconditionally (correctly *not* gated to server-only - ground-truthed against 1.12.2's
  `EntityTimeAccelerator.onEntityUpdate`, which also force-ticks the target TE on both sides) - `javap -c`
  on `World.getTileEntity`/`isOutsideBuildHeight` confirmed it calls `pos.getY()` with no null-check as
  its very first instruction, so `getTileEntity(null)` NPEs immediately. The client crashed the moment
  the entity synced over and started ticking - i.e., right after the item was used.
- 1.12.2's own `EntityTimeAccelerator` avoided this via `IEntityAdditionalSpawnData`'s `writeSpawnData`/
  `readSpawnData`, explicitly serializing `target` into the spawn packet - dropped when porting, with
  nothing put in its place. Fixed the same way this port already solved the identical problem for
  `EclipsedClockEntity`'s `HANGING_POS`: a real synced `DataParameter<BlockPos>` (default `BlockPos
  .ZERO`) instead of a plain field, matching this port's established idiom rather than reintroducing
  `IEntityAdditionalSpawnData` for one field.
- Quick audit of every other custom entity in this port (`grep` for field assignments outside `world`/
  `rand`/`pos*`/`dataManager`) turned up nothing else matching this exact shape - the other entities'
  secondary-constructor fields are either purely cosmetic (animation counters, safe as their zero
  default) or already properly synced. Not exhaustive, but no other instance found.
- `./gradlew build -x test` clean. `TESTING_CHECKLIST.md` #200 marked fixed/needs-retest, #201 flagged
  as blocked on it. Not yet re-confirmed in-game.

### Spectre Illuminator: the sync fix above itself crashed the client, 2026-09-27 (reported via crash log)

User pasted a real crash report minutes after the previous fix. `ArrayIndexOutOfBoundsException: 103245`
in `it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap$KeySet.forEach`, called from `LevelBasedGraph
.bulkCancel` → `SectionLightStorage.cancelSectionUpdates` → `SectionLightStorage.updateSections` →
`LightEngine.tick` → `WorldLightManager.tick` → `GameRenderer.updateCameraAndRender`. Not a mod-code
stack frame anywhere in it - a real bug in this Forge version's own (rewritten-for-1.14) light engine,
triggered by mod behavior rather than mod code crashing directly.

- Root cause: `relight()` (added in the previous fix) calls `WorldLightManager#checkBlock` for every
  position in an 18x18x256 padded column - about 83,000 calls - synchronously, in one method call. This
  is a byte-for-byte port of 1.12.2's own `SpectreIlluminationHelper.lightUpdateChunk`, which did the
  exact same 83,000-position sweep with no issue - but 1.12.2's lighting engine was architecturally
  completely different (a straightforward synchronous BFS, no internal scheduling structures to
  overflow). 1.14's engine schedules updates into fixed-capacity internal structures
  (`SectionLightStorage`'s own fastutil maps) instead of processing them immediately; a burst this large
  overwhelms it. Real vanilla code never enqueues anywhere near this many light updates in a single tick
  either - a torch placement is one position, not 83,000.
- Fixed two ways, both in a new `entity/SpectreIlluminatorRelight.java`: (1) the scanned height range is
  now bounded to just above the chunk's own tallest block (reusing the same heightmap scan `tick()`
  already does for centering) instead of the full 0-255 world height - most of that range is empty air
  no build ever reaches, and touching it means allocating/touching far more chunk sections than actually
  matter. (2) The remaining positions are queued into a per-`World` `ArrayDeque` and drained a bounded
  256/tick via a new `WorldTickEvent` listener in `RandomThings`'s constructor (matching this project's
  own established pattern for exactly this kind of "drive a static queue off a tick event" need - see
  `EscapeRopeHandler`/`StableEnderpearlItem`'s dropped-pearl countdown right above it), instead of one
  big burst. `SpectreIlluminatorEntity#relight()` now just calls `SpectreIlluminatorRelight.queue(...)`.
- This also incidentally fixes a design gap the previous fix had: queuing instead of executing
  synchronously means the "turn off" relight (called from `onRemovedFromWorld`, right as the entity is
  being torn down and stops ticking) no longer needs the entity itself to still be alive to finish the
  work - the queue lives in a static map keyed by `World`, drained independently by the global tick
  listener, not tied to any one entity's lifecycle.
- `./gradlew build -x test` clean. `TESTING_CHECKLIST.md` #273 updated with this second fix. Not yet
  re-confirmed in-game - this is now the third blind fix in a row for this one feature, all reported by
  the user in quick succession; worth a careful real playtest before assuming it's solid.

### Spectre Illuminator: instrumented instead of guessing a fourth time, 2026-09-27

User retested after the crash fix (no crash this time) and reported it's still not working - no visual,
no lighting. Before making another blind code change, spent real effort trying to rule out remaining
hypotheses via ground-truth instead of guessing:

- Re-verified the full `IBlockReader` inheritance chain from `Chunk` (`Chunk implements IChunk,
  IForgeChunk` → `IChunk extends IStructureReader` → `IStructureReader extends IBlockReader`) via
  `javap -p` on each interface - confirmed nothing in that chain redeclares `getLightValue(BlockPos)`
  and shadows the default method the coremod patches. Ruled out.
- Re-confirmed `world.getChunk(chunkX, chunkZ)` (chunk coordinates, not block coordinates) matches
  existing precedent already established in `ChunkAnalyzerContainer.java`. Ruled out as a coordinate bug.
- Re-checked base `Entity.tick()`/`baseTick()` bytecode for anything that would reset motion/position
  before the entity's own centering logic runs each tick. Nothing found. Ruled out.
- **Landed on the most likely explanation**: the user has now tested this exact feature three times in
  a row, very likely in the same chunk each time (the natural thing to do). The first attempt (before
  any fixes) never reached `illuminated=true` reliably; the second attempt (crash fix, still broken)
  might have. Either way, a leftover `SpectreIlluminatorEntity` could easily still be sitting in that
  chunk from an earlier attempt - `SpectreIlluminatorItem`'s "already exists" check (both the
  `isInChunk`-illuminated check and the 2-chunk-radius AABB scan) would silently refuse to place a new
  one, with zero player feedback, matching 1.12.2's own silent-fail behavior exactly. This would produce
  precisely the reported symptom: right-click does nothing, no crash, no message, no visual, no light
  change - because nothing new actually happened.
- Rather than guess this is the answer and make a fourth blind code change, added `LOGGER.info`
  diagnostics (tagged `[SpectreIlluminator]`) at every real decision point instead: placement
  refused (which check, and details of the blocking entity if any) or placed (entity id) in
  `SpectreIlluminatorItem`; settling progress every 5 seconds and the settled/relight-queued transition
  in `SpectreIlluminatorEntity`; positions queued and queue-drained-empty in `SpectreIlluminatorRelight`.
  This turns the next test into real evidence instead of another round of speculation - asked the user
  to test in a genuinely fresh chunk (to rule out stale leftover entities) and share back what the
  `[SpectreIlluminator]` log lines show.
- `./gradlew build -x test` clean. No `TESTING_CHECKLIST.md` status change (still failing from the
  user's perspective) beyond noting the instrumentation and the fresh-chunk-retest ask.

### Spectre Illuminator: third real bug found from the diagnostic logs, 2026-09-27

The instrumentation immediately paid off - user's log made the actual root cause unambiguous, no more
guessing needed:

```
15:38:18 [Server thread] entity 981 settled ... illuminating chunk [16, 11]
15:38:18 [Server thread] queued 26892 relight positions ... (world.isRemote=false ...)
15:38:18 [Client thread] entity 981 learned illuminated=true on the client, relighting
15:38:18 [Client thread] queued 26892 relight positions ... (world.isRemote=true ...)
15:38:23 [Server thread] relight queue for world overworld drained (drained 12 this tick)
15:38:49 [Server thread] Saving and pausing game...          <- user relogs here
```

Server queued and fully drained (~5 seconds, exactly matching 26892 / (256/tick × 20 ticks/sec)) - the
"drained" log line is proof. The client queued the identical 26892 positions at the same instant, but
**no client-side "drained" line ever appeared**, not even 31 seconds later (relogging happened at
15:38:49, and the queue would need only ~5 seconds to drain at the same rate if it were draining at
all). User confirmed after relogging the chunk was instantly, fully lit - consistent with the server's
already-correct light data simply being resent fresh as part of a normal full chunk (re)load on
rejoin, entirely independent of whatever was or wasn't happening with the client's own local queue.

Root cause: in singleplayer, the client's `ClientWorld` and the integrated server's `ServerWorld` are
two entirely separate `World` object instances, even though they represent "the same" world to the
player. `SpectreIlluminatorRelight.PENDING` is keyed by `World` identity, so the client's queue-entry is
a completely distinct map entry from the server's. The `WorldTickEvent` listener draining
`SpectreIlluminatorRelight.tick(event.world)` evidently doesn't fire for the client's `ClientWorld` in
this Forge version the way this file's *other* `WorldTickEvent` listeners' defensive `event.world
.isRemote` early-returns implied it might (they were seemingly just cautious boilerplate, not evidence
it actually fires client-side) - so the client's queue was queued once and then never touched again.

Fixed by adding a second drain path specifically for the client: a `ClientTickEvent` listener (this
project's own already-established pattern for "needs to run every tick on the client" - the exact same
event this file already uses for `DiviningRodRenderer.get().tick()`, right next to the new listener),
explicitly draining `SpectreIlluminatorRelight.tick(Minecraft.getInstance().world)`. The existing
`WorldTickEvent`-based drain is left in place too (proven working for the server, and harmless even if
it turns out to also fire for the client somehow - draining an already-empty or already-draining queue
via two independent paths is idempotent).

The `[SpectreIlluminator]` diagnostic logging added in the previous round is left in place rather than
removed immediately - the next retest should show a client-side "drained" log line within a few seconds
of settling, which would be direct confirmation this is actually fixed rather than another guess.

`./gradlew build -x test` clean. `TESTING_CHECKLIST.md` #273 updated with all three fixes in sequence.
Not yet re-confirmed in-game.

### Spectre Illuminator: user confirmed lighting-on works, then a fourth bug + a feature request, 2026-09-27

User's retest log confirmed the ClientTickEvent fix worked: both server and client queues drained at the
same timestamp (15:38:40) after settling, and again together after pickup (15:38:47). Then live
confirmation: chunk visually lit up (no relog needed this time), particles visible. Two remaining items:

**Feature request: a visible billboard.** The entity shipped with zero visible model by design (matching
1.12.2's own renderer, which also had no texture - only its dropped MKR magic-circle effect, itself
already replaced with particles). User asked for a real visible marker that moves from the clicked spot
to the chunk center - exactly the entity's own existing drift animation, just nothing was rendering it.
Implemented by making `SpectreIlluminatorEntity implements IRendersAsItem` (`getItem()` returns a
`SPECTRE_ILLUMINATOR` stack) and swapping its renderer registration for vanilla's own `SpriteRenderer` -
the exact same billboard-icon mechanism this port already uses for `ThrownGoldenEggEntity`/
`ThrownWeatherEggEntity`. Since `SpriteRenderer` tracks the entity's real interpolated position every
frame, and the entity already drifts from spawn to chunk-center as part of its existing centering logic,
this needed zero new animation code - the existing motion IS the requested visual. Deleted the old
empty `SpectreIlluminatorEntityRenderer` class (no longer needed).

**Fourth real bug (reported by user): after pickup, part of the chunk stayed stuck lit** - specifically
the edge where the orb first settled and started forcing light. Root cause: the relight sweep's chunk-
boundary padding was only 1 block. Vanilla block light doesn't stop at a chunk boundary - once every
position inside the illuminated chunk reports 14, the light engine's own neighbor-cascade (confirmed via
`javap -c` on `LightEngine.checkLight` back when investigating the original crash - it reschedules a
checked position's 6 neighbors too) keeps carrying reduced values outward for up to 15 more blocks,
vanilla's own max light falloff distance. The 1-block pad only reached far enough to catch the override
itself; anything the override's bleed actually reached beyond that kept relying on a stale bright value
nothing ever told to re-derive once the orb was removed. Fixed by widening `SpectreIlluminatorRelight`'s
`PADDING` from 1 to 15 (matching vanilla's own falloff distance exactly, so nothing possibly affected is
left unchecked), and bumping `PER_TICK` from 256 to 512 to keep the ~7x-larger sweep (a chunk padded by
15 blocks instead of 1: 46x46 vs 18x18 in the XZ plane) draining in a comparable wall-clock time - still
~160x below the ~83,000-positions-in-one-synchronous-call threshold that caused the original crash, so
no regression risk there.

Full build clean, plus a `runClient` boot smoke test (registries froze, texture atlases built, no
VerifyError/LinkageError, no missing-model warning for the item now used as an in-world billboard).
`TESTING_CHECKLIST.md` #272 (billboard) and #274 (padding fix) updated; #273 marked confirmed-working
for the lighting-on half specifically. Not yet re-tested for the padding fix or the new billboard.

### Spectre Illuminator: padding fix confirmed working; client-side perf tuning requested, 2026-09-27

User confirmed the widened-padding fix worked (implied - moved straight to a follow-up perf request
rather than reporting it still broken) and asked to slow down the relight sweep specifically on the
client side, since watching a chunk light up or down still stutters. Root cause of the stutter (not
directly reported as a "bug," just inferred from the request): server-side `checkBlock` calls are pure
light-data bookkeeping, essentially free; client-side, each one that actually changes a position's light
value can trigger a chunk render-mesh rebuild, which is real per-frame work - draining both sides at the
same 512/tick rate meant the client was doing up to 512 potential mesh rebuilds in a single frame.

Split `PER_TICK` into `PER_TICK_SERVER` (kept at 512) and `PER_TICK_CLIENT` (48, an ~11x reduction) -
`tick(World world)` now picks the rate based on `world.isRemote`. Trades a slower visible fade-in/out on
the client for smoother frame times while it's happening; noted in the javadoc as a single easily-tuned
constant if 48 turns out to still be choppy, or feels too slow to finish. `./gradlew build -x test`
clean. `TESTING_CHECKLIST.md` #273 updated. Not yet re-tested for actual smoothness.

**Update, same day: user asked to unify the two rates back into one** ("keep things consistent") rather
than have server and client behave differently. Reverted to a single `PER_TICK = 48` constant used by
both sides - the server has no rendering cost either way, so simply draining a bit slower than it
strictly needs to is a fine trade for the two sides behaving identically. `./gradlew build -x test`
clean. `TESTING_CHECKLIST.md` #273 updated to describe the single shared rate.

**Update, same day: user asked to raise the shared rate to 256.** Straightforward constant change
(`PER_TICK` 48 → 256) - still comfortably far (~325x) below the ~83,000-positions-in-one-synchronous-
call threshold that caused the original crash, so no regression risk regardless of value in this range.
`./gradlew build -x test` clean. `TESTING_CHECKLIST.md` #273 updated. Not yet re-tested for smoothness
at this rate.

### Time in a Bottle: billboard added + insufficient-time feedback, 2026-09-27

User confirmed the earlier `TimeAcceleratorEntity.target` sync crash fix works ("particles visible, the
effect is working fine"), noting only that the entity itself isn't visible - by design, matching Spectre
Illuminator's own original no-model state. Two follow-up requests:

1. **Same billboard treatment as Spectre Illuminator.** `TimeAcceleratorEntity` now implements
   `IRendersAsItem` (`getItem()` returns a `TIME_IN_A_BOTTLE` stack) and renders via vanilla's
   `SpriteRenderer`, exactly the same mechanism just added for `SpectreIlluminatorEntity`. Deleted the
   old empty `TimeAcceleratorEntityRenderer` class and its registration, matching the earlier pattern.
   Since the entity already drifts to its target position as part of existing logic (actually it doesn't
   drift here - it's placed once and stays put - the billboard just needed to exist, no motion concerns
   unlike Spectre Illuminator), this was a pure copy of the established pattern.
2. **Status message on insufficient stored time.** `TimeInABottleItem.onItemUseFirst` has two silent-
   failure paths (placing a new accelerator without 30s stored; upgrading an existing one without enough
   for the next doubling) that previously did nothing with no feedback, matching 1.12.2's own silent
   behavior. Per explicit user request, both now call `player.sendStatusMessage(...)` with a red
   `item.randomthings.time_in_a_bottle.not_enough_time` message - not a 1.12.2 behavior, but matching
   this port's own already-established feedback convention (`EnderMailboxBlock`'s not-owner message,
   `EnderLetterItem`'s no-player/no-space messages). Deliberately left the separate "already at max 32x
   rate" case (a different constraint, not about stored time) without a new message, staying within the
   explicit scope of what was asked.

Full build clean, plus a `runClient` boot smoke test (registries froze, no VerifyError/LinkageError, no
missing-model warnings). `TESTING_CHECKLIST.md` #200 marked PASS (user-confirmed) with the billboard
noted; #201 unblocked and the message behavior documented on both. Not yet re-tested for the billboard
or the new messages specifically.

### Three more real fixes: billboard clipping, animated day-skip, label height, 2026-09-27

After testing the billboard, user reported three things in one message - one real bug in what was just
shipped, one deliberate behavior-change request, and one real bug in older, previously-untested code:

1. **Time Accelerator billboard clipping into solid blocks.** The marker spawned dead-center in the
   clicked block (`pos.getX()+0.5, ...`), invisible-so-harmless until the billboard existed - but the
   clicked block is very often solid (a hopper, a furnace), so the now-visible icon (and its particles,
   rooted at the same position) rendered clipped inside it. Fixed in `TimeInABottleItem`: offset the
   marker 0.7 blocks out along `ItemUseContext#getFace()` (the actually-clicked face) instead of the
   block center - `TimeAcceleratorEntity.getTarget()` (which TE gets force-ticked) is a separate field,
   untouched by this, so the actual game logic is unaffected. This broke the existing "already placed
   one here" detection, though, which scanned a small box shrunk around the block's own volume - the
   marker is now rendered outside that volume, so the old scan would miss it. Widened the scan box and
   switched the match condition to `getTarget().equals(pos)` instead of positional overlap - more
   correct regardless of where the marker visually sits, not just a wider net.
2. **Eclipsed Clock: animated day-skip instead of an instant jump, per explicit user request.** Both
   1.12.2 and this port's first pass called `world.setDayTime(current + dif)` once, snapping instantly.
   User asked for a visible time-lapse instead. `EclipsedClockEntity` now advances `setDayTime` in
   100-game-tick steps once per real server tick until reaching the target (`fastForwardTarget`/
   `FAST_FORWARD_RATE`), stopping exactly on target once the remaining distance is smaller than one
   step. Purely server-side - the client needs nothing special, since it already receives the changing
   day time through vanilla's own periodic time-sync packets and renders the sky from that, the same
   mechanism that already drives the ordinary day/night cycle. Guarded re-triggering with
   `fastForwardTarget < 0` alongside the existing `cooldownCounter` check, since a full-day skip
   (~12 real seconds at this rate) can outlast the pre-existing 5.5-second cooldown.
3. **Real bug found and fixed (reported by user): the Eclipsed Clock's time-display label sits about a
   block too high.** Ground-truthed via `javap -c` on vanilla's own `EntityRenderer.renderLivingLabel` -
   it already adds `entity.getHeight() + 0.5` (≈1.0 for this entity's registered 0.5-tall size) on top
   of whatever `y` it's given. Both call sites in `EclipsedClockEntityRenderer` (the post-interaction
   display and the crosshair-hover display) were stacking their own manual offset on top of that
   baked-in one instead of accounting for it, landing the label roughly a block higher than intended.
   Both now subtract 1.0 to compensate.

Full build clean, plus a `runClient` boot smoke test (registries froze, no VerifyError/LinkageError).
`TESTING_CHECKLIST.md` #195 (label height), #196 (animated skip), and #200 (billboard offset) all
updated. None of the three re-tested in-game yet.

### Time Accelerator visual redesigned again: all 6 faces, closer, particles spread wider, 2026-09-27

User asked for three related changes in one message, all about the billboard just added: icons on every
face instead of just the clicked one, closer to the block surface, and particles spread across the whole
block rather than clustered near the single marker point.

A single `SpriteRenderer`-based billboard can only ever face/sit at one fixed position, so "every side"
needed a real custom renderer, not a tweak to the existing one. Ground-truthed `SpriteRenderer#doRender`'s
exact transform sequence via `javap -c` first (translate → enableRescaleNormal → scale → rotate yaw →
rotate pitch, sign flipped for third-person-view mode 2 → rotate 180° → bind the block atlas → `ItemRenderer
.renderItem(stack, TransformType.GROUND)`) rather than guess at billboard math, then wrote a new
`TimeAcceleratorEntityRenderer` that repeats that exact sequence once per `Direction` (6 total), each
offset from the entity's position by `0.5 + 0.05` blocks along that direction's own axis - just outside
the block's own half-width, matching "closer to the surface."

Since icons no longer favor one face, the marker's own position went back to the plain block center in
`TimeInABottleItem` (undoing the single-face offset from the previous round) - `TimeAcceleratorEntity
.getTarget()` stayed the thing that actually matters for game logic throughout all of this, so the
"already placed one here" detection (already switched to match by `getTarget()` in the previous round)
never needed touching again. `spawnParticles()` widened its random offset range from a tight 0.3-0.5
radius around one point to ±0.525 on all three axes, covering the full block volume.

Full build clean, plus a `runClient` boot smoke test (registries froze, no VerifyError/LinkageError -
this touches low-level GL calls directly, so worth the extra caution). `TESTING_CHECKLIST.md` #200
updated to describe the new 6-face/particles-spread design. Not yet re-tested in-game.

### Portkey implemented, 2026-09-27

User picked this from 4 offered NOT-STARTED candidates (Dyeing Machine, Spectre Lens, Spectre Anchor
were the others - Spectre Anchor turned out, on inspection, to not be a simple item at all but a
"combine" recipe tagging an arbitrary item to survive death, needing a death-drop hook; flagged as more
involved than it first looked rather than picked). Full 1.12.2 source ground-truthed first
(`ItemPortKey`, `PortKeyMesh`, the two `RTEventHandler` call sites - pickup-teleport and the HUD-arrow
overlay, `ModRecipes`' base recipe and the separate camo-combine recipe, `AsmHandler#enchantmentColorHook`)
via `git show origin/1.12.2:...` before writing anything, same discipline as every other slice.

- **The mechanism**: right-click a block to bind the key to that spot (dimension + x/y/z) - literally
  the same "remember where I clicked" pattern this port's own `PositionFilterItem` already established,
  reused rather than reinvented. Drop it; once it's sat undisturbed on the ground for 5 real seconds
  (100 ticks) it stops despawning and its enchant glow turns off, meaning it's primed. The next player
  to pick it up gets teleported to the bound location instead of collecting it, landing on a safe nearby
  surface found via the same search 1.12.2 used (5x5 column around the target, scanning down 10 blocks,
  first solid-topped position with 2 air blocks above) - ground-truthed 1.14.4's replacements for the
  needed World methods first (`world.isSideSolid`/`isAirBlock` don't exist as such anymore; found
  `Block.hasSolidSide(state, world, pos, direction)` + `world.isAirBlock(pos)` already proven elsewhere
  in this exact codebase, e.g. `RuneBaseBlock`/`RuneDustItem`).
- **Two disclosed simplifications, both dropped for the same reason** (cosmetic-only, no clean 1.14.4
  path, not worth a new coremod or a fragile new hack): (1) the original's "camo" system (combine with
  any item in a crafting table to disguise the icon as that item's) worked via direct reflection into
  1.12.2's own internal item-model registry map (`ReflectionUtil.getModelMap()`) - fragile even in
  1.12.2, and 1.14.4's model-loading pipeline is different enough that reproducing it would mean
  reverse-engineering an equally fragile new hack for a purely cosmetic disguise feature. (2) the
  original's custom magenta enchant-glow tint needed its own ASM hook
  (`AsmHandler#enchantmentColorHook`, shared with Spectre Key/Spectre Sword/Redstone Tool - none of
  which are ported yet either) - ships with the default vanilla glow color instead via a plain
  `Item#hasEffect` override (confirmed this hook still exists and works the same way via `javap -p`),
  no coremod needed. Also dropped: the original's full-screen HUD directional beam overlay (pointing at
  the bound location while held, rendered via a `RenderGameOverlayEvent` hook with raw GL blending) - in
  favor of the same shift-to-reveal tooltip coordinates `PositionFilterItem` already uses for an
  identical "where did I bind this" need, rather than porting a whole custom overlay-rendering system
  for one item. (Briefly considered reusing `CompassItemBase`'s already-proven "angle" item-model-
  property needle instead of a flat tooltip, since that's this port's own established compass-style
  direction indicator - decided against it: that mechanism only tracks a flat X/Z target, and Portkey
  needs full 3D position plus cross-dimension awareness, which doesn't fit its model cleanly.)
- **The dropped-item priming countdown needed the same fix this port has now used three times**:
  `Item.onEntityItemUpdate` doesn't exist in this Forge build (already found for `StableEnderpearlItem`/
  `FlooTokenItem`) - a new `WorldTickEvent` listener in `RandomThings` scans dropped `ItemEntity`s of
  this type and calls `PortkeyItem#tickDroppedPortkey` each tick, exactly matching those two items'
  existing pattern. One real design wrinkle worth recording: the priming counter had to live on the
  *stack's* own NBT (not the dropped `ItemEntity`'s separate `getPersistentData()`, unlike
  `StableEnderpearlItem`'s otherwise-identical counter) specifically because `Item#hasEffect(ItemStack)`
  only ever receives the stack, never the owning entity - so the glow-state has to be readable from the
  stack alone. `PortkeyItem#inventoryTick` clears the counter the moment it's picked back into an
  inventory before priming finishes, matching 1.12.2's own `onUpdate` (which deleted the counter every
  tick a stack was inventory-held, not dropped) - dropping it again starts the 5 seconds over.
- Pickup-teleport itself hooks `EntityItemPickupEvent` directly (confirmed this class still exists,
  same package, via a jar listing) - checks `dropCounter > 100` and a bound target in the same
  dimension, runs the safe-landing search, and on success calls `((ServerPlayerEntity) player).connection
  .setPlayerLocation(...)` plus `event.setCanceled(true)` so the item is consumed by the teleport
  rather than actually being collected, matching 1.12.2 exactly.

Full build clean, plus a `runClient` boot smoke test (registered cleanly - `Entry: 1085,
randomthings:portkey, portkey` - no missing-model warnings). `TESTING_CHECKLIST.md` #275-278 added,
`WIKI_FEATURE_STATUS.md` updated to DONE-UNTESTED. Not yet tested in-game.

### Portkey camo disguise system researched, then implemented, 2026-09-27

User asked to research whether the "camo" feature (dropped as a disclosed simplification when Portkey
was first implemented) could be done cleanly in 1.14.4, then said go ahead. Research-first approach paid
off - found a real, better-than-1.12.2 answer instead of guessing:

- Ground-truthed via `javap -c` that `ItemRenderer.renderItem` checks `Item.getTileEntityItemStackRenderer()`
  (settable via `Item.Properties#setTEISR`) and, if non-null, routes through `ItemStackTileEntityRenderer
  #renderByItem(ItemStack)` for the *low-level* `renderItem(ItemStack, IBakedModel)` overload - meaning
  every higher-level render path (GUI slots, held-in-hand, dropped-on-ground, item frames) funnels through
  it uniformly, confirmed by tracing the bytecode rather than assuming. This is the exact mechanism vanilla
  itself uses for shulker boxes - fully public, no reflection, unlike 1.12.2's own `ReflectionUtil
  .getModelMap()` hack into its internal model registry.
- Design: Portkey's own model becomes `{"parent": "builtin/entity"}` (routes every render through the new
  `PortkeyItemRenderer`). When camo NBT is set, renders that (different) item stack directly - no
  recursion risk. When it isn't, can't just call `renderItem` on the Portkey's own stack again (that would
  immediately recurse into this exact method) - instead renders a second, separate "special" model
  (`randomthings:portkey_base`, a plain flat-icon model nothing else references) fetched via `ModelManager
  #getModel`. That model only gets baked at all because of a new `ModelRegistryEvent` listener
  (`RandomThings#registerModels`) calling `ModelLoader#addSpecialModel` - confirmed this whole event/method
  pair exists via a jar listing plus javap before writing any of it.
- `PortkeyCamoRecipe` reuses this port's own already-proven `SpecialRecipe`/`SpecialRecipeSerializer`
  pattern (literally copied the shape of the existing `GoldenCompassSetPositionRecipe` and generalized the
  match condition from "exactly this other item" to "exactly one other item, whatever it is") instead of
  1.12.2's `SimpleRecipe` - same "donor item returned unconsumed" behavior as 1.12.2's own version.
- **Real bug caught by the `runClient` smoke test before it ever reached in-game testing**: `Unable to
  load model ... FileNotFoundException: randomthings:models/item/item/portkey_base.json` - the special
  model's resource path incorrectly included an `"item/"` prefix that `ModelResourceLocation`-based item-
  model resolution already adds automatically, doubling it. Fixed by passing the bare name
  (`"portkey_base"`, not `"item/portkey_base"`) - a second smoke test afterward confirmed the fix (model
  loads cleanly, no warnings).

Full build clean. `TESTING_CHECKLIST.md` #279-280 added (camo rendering + a regression check that the
un-camouflaged case still renders correctly), #278's stale "camo is dropped" note corrected.
`WIKI_FEATURE_STATUS.md` updated. Not yet tested in-game.

## Verification

- A full `./gradlew build -x test` works in this environment via `source env/activate.sh` (see
  "Toolchain note" at the top) - the real, authoritative compile check; prefer it over manual signature
  cross-checking below whenever there's time to run it.
- Fallback verification when a full build isn't run: strict adherence to the API patterns already
  proven in the six ported blocks and the registries (same imports, same
  `Block.Properties`/`BlockState` idioms), careful manual cross-check of each new class's signatures
  against 1.14.4 Forge/MC classes referenced elsewhere in `master`, and a final read-through diff before
  each check-in.
