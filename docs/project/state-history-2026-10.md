# Detailed state and change-set history (as of 2026-10-04..11)

_Moved verbatim from `PORTING_PLAN.md` (2026-10-11 doc refactor). Index: [README.md](README.md)._

- **Coverage:** every wiki-documented feature has 1.14.4 code except those marked `WILL NOT MIGRATE`/
  `REMOVED` in `WIKI_FEATURE_STATUS.md`. Most of it is `DONE-UNTESTED` (builds, boots, renders in the
  autotest; not play-verified). Last tallies (2026-10-03): PASS 196, UNTESTED 106, DONE 40, FIXED 10, FAIL 8.
- **Open issues:** none known. Sound Dampener #158: muting confirmed working 2026-10-11; the missing Sound Pattern tooltips in its GUI were fixed (`AbstractSoundDampenerScreen.render`), awaiting retest.
  Confirmed working 2026-10-04: GUI layout audit (#120, all 28 GUIs reviewed), Sound Recorder GUI (#155), Colored Grass,
  Weather/Golden Egg sprites, Floo Powder recipe, Chat Detector button. Always check
  `TESTING_CHECKLIST.md` for new `FAIL` rows before starting new work; don't re-investigate a `FIXED` row
  without reading its `TESTING_NOTES.md` entry.
- **Latest change set (2026-10-07, uncommitted):** automated tests + dev isolation (see Dev tooling above).
  Found and fixed by them: **Magnetic Enchantment never worked** - `BlockEvent.HarvestDropsEvent` is never posted
  in this Forge build, so the old listener was dead code and the Block Breaker's drops fell on the ground; now a
  global loot modifier (`handler/MagneticLootModifier`, registered in `data/forge/loot_modifiers/`) (#288/#372, FIXED);
  64 missing per-color recipes restored (#367-370); Fluid Display item is an isometric cube (#366; first attempt used
  scalar `display.scale`, which the game rejects - arrays required); Fluid Display TE NBT now idempotent (#373).
  Logging cleanup: Spectre Illuminator INFO logs moved to `RTDebug.log` (dev-only), `printStackTrace` -> logger.
  Q11/Q12 (2026-10-10): stub coremod booted on 14 Forge/NeoForge builds - JS coremods work to Forge 1.21.10 and NeoForge 21.5, silently ignored on Forge 1.21.11 and NeoForge 21.8+; 1.15.2/1.17.1 skip stands (see backlog). Transcript review finished (2026-10-08; backlog Q5): pitfall catalog now has K14-K23 and L1-L19, `04` has the Mixin
  timeline. First real client-on-server session (2026-10-09, Q9): server crashed on an Ender Letter receiver packet (`PacketBuffer.readString()` is client-only; all reads now `readString(32767)`, #382); Portkey display transforms fixed (#381). Dedicated server (2026-10-08): `runServer` revealed the mod crashed every dedicated server at construction (client
  classes referenced from the main class); client wiring moved to `client/ClientSetup`, `IRendersAsItem` entities
  got `@OnlyIn(_interface)`; server now boots (#378). Client suite 317/317 after adding Contact Button/Lever, Imbuing Station and Block Destabilizer tests; those found that the
  **Imbuing Station never delivered its result** (#169, FIXED). The suite also runs headless on `runServer` (`debug/test/ServerAutoTest`, #379).
  Second round of tests (`debug/test/CoremodTests`, Observer): found that the **Wireless Redstone Interface never refreshed**
  when redstone next to it changed (listener compared the wrong event position; now `neighborChanged`, #376 FIXED) and
  confirmed at runtime that the spawn-dispatch, rain, death-drop and boots coremods apply. The audit also checks the vanilla
  loot tables LootHandler injects into (#377). Decisions (2026-10-07): keep dev classes in the release jar; dev features by
  JVM property only. Open questions: `docs/migration/open-questions-backlog.md`.
- **Previous change set (2026-10-04, commit `6a76190`):** text-field GUIs sync on every change instead of
  only from `removed()` (#360); Ender Letter has a 200-char multiline message body via
  `MultilineTextFieldWidget` (#361, **new feature, not in 1.12.2**; GUI 50px taller, `ender_letter.png`
  rebuilt; message stored in the letter's NBT `message`, sent by signal id 1, trimmed server-side);
  letter tooltip "Sneak + right-click an Ender Mailbox to send" and mailbox label "Incoming Mail" (#362);
  Notification Interface icon is a ghost/copy slot like the Remote's camo row (#363); "Target" label on
  Redstone Observer / Basic Redstone Interface only when a target is set, Remote use-screen hover shows the
  displayed item's anvil name (#364); Advanced Item Collector GUI trimmed 24px (#365). All `FIXED`,
  awaiting in-game retest.
- **Removed on request (don't re-port):** Plate family (11 blocks), Special Chest (+ ocean-monument
  coremod), Sakanade, Pitcher Plant, fire/water-protection family (Obsidian Skull, Lava Charm, Lava Wader,
  Water Walking Boots, Obsidian Water Walking Boots), Biome Glass, Biome Stone x5, Inventory Rerouter,
  Blood Rose (was never 1.12.2 content).
- **Will not migrate (final):** Item Filter (both consumers shipped with a built-in single-example-item
  filter), "Ender network"/Link Orb (abandoned stub in 1.12.2 itself), Collapse
  potion/effect/brewing, modded-ore divining rods and all third-party mod compat (Baubles, JEI,
  OpenComputers, Thaumcraft, Thermal Expansion), Obsidian Skull Ring (Baubles-only), Festival system,
  Player Soul/Revive Circle/Rez Stone, Dungeon Chest Generator, Voxel Projector, Nature Core, `RTCommand`.
  Full list: `WIKI_FEATURE_STATUS.md` "Not migrating".
- **Known disclosed gaps:** Ancient Furnace's biome change doesn't live-resync to already-connected
  clients until chunk reload; Spectre Energy Injector/Coil use plain cube models; Ender Bucket icon is
  static (no dynamic fluid-layer model); Biome Sensor's held-item biome HUD wasn't ported; Obsidian Skull
  Ring and the `Numbers` range tunables aren't ported. (Rain Shield's visual suppression was added later via
  `GameRendererTransformer`; the thrown Weather/Golden Egg sprite bug was a missing `createSpawnPacket`.)
- **Peace Candle village churches shipped 2026-09-29** via a delegating `JigsawPattern` wrapper
  (`PeaceCandleChurchPool`) + user-authored `peace_candle_church.nbt` (needs a jigsaw block at the door).

**Production names (2026-10-11):** transformers match MCP and SRG names and reflection uses `ObfuscationReflectionHelper`; static registries use `util/WeakSets`; both verified on a production server and a production client (`prod-test/`, see `docs/testing/README.md`; scenes `light_redirector` and `magic_hood` added for the render coremods).

**Test tooling (2026-10-11):** the GUI click-through and render-scene screenshot passes were backported from the 1.16.5 branch (`RT_AUTOTEST_CLICKS`, `RT_AUTOTEST_SCENES`; inventory in `docs/testing/test-inventory.md`).

