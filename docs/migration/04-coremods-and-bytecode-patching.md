# 04 — Coremods, Mixin and bytecode patching

The 1.12.2 mod patched **17** vanilla/Forge classes with its own ASM transformer. The port needed a replacement
for each. This document is the decision procedure that worked, the Mixin dead end, and how to build and verify
coremods.

## 1. Decision tree for "the original patched method X"

For each patch, in this order — stop at the first that works:

1. **Public API / override point.** Can the behavior be expressed by overriding a method on your own subclass
   (`tickRate`, `getSlipperiness` on a block, `canFallThrough`, `getLightValue`)? Then do that. (Caveat K3 in the
   pitfall catalog: make sure the override point belongs to the *subject* of the behavior.)
2. **An event that is actually posted at the right time.** Verify *both*: `grep` the sources jar for the call
   that posts it, and test it end to end. (Dead: `HarvestDropsEvent`. Too late: `CheckSpawn`.)
3. **Data-driven replacement.** Loot tables, global loot modifiers, tags, pool wrapping (village church).
4. **A per-tick scan/listener** (water walking boots, speed caps, dropped-item countdown) — acceptable when
   throttled and the subject is accessible.
5. **A shared choke point + coremod.** Find the single method everything funnels through (`javap -c` the
   callers) and patch that one. One spawn-dispatch method served three features.
6. **A narrowly targeted coremod** on the exact call site (a single `INVOKEVIRTUAL` inside one method) when the
   check is inline in a long method (rain rendering).

Classify every original patch into these buckets *before* starting; the Random Things audit found 11 of 17
needed no coremod, 6 needed one, and later work shrank both numbers as better hooks were found.

## 2. Mixin: what happened and why it is not recommended here

- Used for two features (Blazing Fire catch chance, Super Lubricent Boots). Both compiled, shipped, and did
  **nothing** in the game, with no error.
- First suspected cause (found by actually running the client with Mixin debug flags): the dev client loads the mod
  from `build/resources/main`, which has **no manifest**; Mixin config registration was manifest-driven, so it never
  happened in dev (`Mod file ... is missing a manifest`). Trying to load the built jar from `run/mods` crashed with a
  duplicate-mod-ID error because the dev run config also adds the source-set mod.
- Second finding: both mixins had `remap = false` with plain dev names; in a reobfuscated jar the method is
  `func_213352_e`, not `travel`, so the redirect found no target and silently did nothing. Removing `remap = false`
  produced a populated refmap.
- Final finding (the real blocker): the Mixin library cannot bootstrap from a mod jar on this Forge — its
  `ILaunchPluginService` must be on the launcher classpath **before** mods are scanned, and Forge never shipped
  the Mixin runtime in this build. Mixins therefore could not be tested through `runClient` and could not be
  loaded from a mods-folder jar either.
- Details that cost the most time, for anyone who revisits Mixin on this Forge:
  - The Mixin *runtime* artifact (plain `mixin-0.8.jar`, with the ModLauncher service files) is a different file from
    the annotation-processor jar. The project had declared it compile-only, so **no Mixin code ever shipped**; every
    Mixin had been inert since the first one was added.
  - That runtime requires ModLauncher spec >= 4.0. Forge 28.0.49 ships ModLauncher 3.2.0 (crash: `MixinService
    [ModLauncher] is not valid`); Forge 28.2.26 ships 4.1.0 and satisfies it. Pin the Forge point release.
  - Shading Mixin into the mod jar satisfies only the *transformation service* discovery. The *launch plugin* is
    discovered from the early launcher classpath, before mods are scanned, and Mixin looks it up with no fallback
    (`Mixin Launch Plugin Service could not be located`). A jar in `mods` structurally cannot provide it.
  - In the dev client the mod loads from an exploded directory, and the locator's manifest lookup is hard-coded to
    return empty, so manifest-driven Mixin config registration never happens there at all.
  - `remap = false` is right for methods Forge itself adds (never obfuscated) and wrong for real vanilla methods in a
    reobfuscated jar (the refmap stays empty and the target is not found).
  - Later Forge installers list `org.spongepowered:mixin` among their own libraries (verified 2026-10-08 for Forge 36.2.42
    to 61.2.1), so this specific blocker is a property of the 1.14.4-era Forge, not of Mixin in general.
- **Decision:** convert both to Forge coremods and delete all Mixin build infrastructure. Never reintroduce
  Mixin unless the target loader ships it.

*Any version:* before choosing a patching technology, prove it applies at runtime with a trivial no-op transform
in the shipped jar *and* the dev client. Do not build features on top of an unproven patching pipeline.

## 3. How the coremods are built here

- `META-INF/coremods.json` lists transformers; each is a small JS file in `transformer/` using the loader's
  `ASM` API; the Java hook logic lives in a plain class (`asm/AsmHandler` for common code, `ClientAsmHandler` for
  client-only so a dedicated server never loads client classes).
- Everything is self-contained in the jar and discovered by the normal mod scan.
- The set (this port): light tint hook, fire catch chance, boots slipperiness, spawn placement, nametag
  visibility, rain mechanics, rain visuals, per-position light value, death drops, wireless redstone power (two
  methods), teleporter portal creation, light-redirector render swap, item/armor dye recolor.

### Patterns that worked

| Pattern | Used for | Notes |
| --- | --- | --- |
| Wrap the value before `IRETURN` | spawn result, rain, nametag (6 returns), light value | Collect all return nodes **by reference first**, then insert. Inserting while iterating by index shifts later indexes |
| Insert an early conditional return at method head | teleporter portal skip | Guard so vanilla paths are untouched |
| Redirect one call site (change opcode/owner/name/desc in place) | boots slipperiness, rain biome lookup | The call already passed the needed argument (`this`) — no stack surgery |
| Insert a static call before a call whose argument you want to change | fire `nextInt` bounds | Use ordinals determined by `javap -c` (e.g. the second and third `nextInt`) |

Always guard hooks with `instanceof`/identity checks on *your* classes so vanilla and other mods are provably
untouched.

## 4. Verification protocol (no harness ships in the repo; build a throwaway each time)

1. `javap -c -p` the target class from the mapped jar; record the exact instruction sequence, ordinals and
   descriptors you will rely on.
2. Write the transformer.
3. **Offline verify:** load the real class bytes, run the shipped `.js` through Nashorn's
   `ScriptEngineManager` (the `jjs` command-line tool mis-parses multi-jar `-cp` on Windows — use the API from a
   tiny Java program), re-serialize with `COMPUTE_FRAMES`, and run ASM `CheckClassAdapter.verify` using the exact
   runtime classpath (obtain via a throwaway Gradle init-script task).
4. **Boot verify:** `runClient`; the log must show `Transforming <class>` and no `VerifyError` / `LinkageError` /
   `ClassFormatError`.
5. **Behavior verify:** classes only loaded in a live world (spawn registry, teleporter) need a playtest or a
   behavior test; `runClient` to the menu never touches them. This repo's `debug/test/CoremodTests` calls the real
   patched vanilla method (e.g. `EntitySpawnPlacementRegistry` dispatch, `World.isRainingAt`, `LivingEntity.travel`,
   the death-drop routine) and asserts on the result - copy that pattern for every new coremod.

## 5. Debugging a coremod that "does nothing"

- Is the transformer *registered* (coremods.json) and *loaded* (`Loaded successfully` in the log)?
- Is the target class actually loaded in this scenario (see §4.5)?
- Does the instruction you match exist exactly once? (Count matches; assert in the script.)
- Is the hook's Java side reached (temporary log in `AsmHandler`)? Is it guarded away by your own `instanceof`?
- Dev vs production names: the transformer must match the names present at transform time.

## 6. Cost/benefit

Each coremod adds permanent maintenance: it must be re-ground-truthed every version bump (the bytecode changes).
Prefer fewer, shared hooks, document each in the state file's coremod table (target, purpose), and add a
behavior test for the feature it enables whenever possible.
