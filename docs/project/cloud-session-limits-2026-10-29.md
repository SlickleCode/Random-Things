# What could not be done in the cloud session (2026-10-29)

A wiki audit (branch `wiki`) found behaviour bugs and missing features; they were fixed or added on all nine branches (`1.14.4` to `1.21.11`) in a cloud session with no build tools. This page lists
what that session could NOT do, so nothing is mistaken for tested work. Per-branch details: [`audit-fixes-2026-10-29.md`](audit-fixes-2026-10-29.md). Checklist rows: `TESTING_CHECKLIST.md` (rows `AF1-AF12`,
`AG1-AG11` and the Magic Bean stalk row, in the sections added that day).

## 1. The environment

- Only JDK 21 was installed (1.14.4 and 1.16.5 need JDK 8, 1.18.2 to 1.20.1 need JDK 17), so `source env/activate.sh` and `./gradlew build` could not run.
- `maven.minecraftforge.net` and `libraries.minecraft.net` were unreachable and there were no Gradle caches, so there were no Minecraft/Forge/NeoForge jars: no `javap`, no mapping lookups, no sources jar.
- `minecraft.wiki` was denied by the network policy (403), so the wiki texture download did not run.
- No display, no game: `runClient`, `runServer`, the test suites, the production-jar check and the scripted playtests were not run.

## 2. What that means for the code

**Nothing was compiled or run. Treat every change from 2026-10-29 as unverified.** The only checks were: a parse-level `javac` pass on every changed Java file (it catches syntax errors, but not unknown symbols,
wrong signatures, missing imports or generics), manual review of imports, lambdas (captured locals must be effectively final), changed method signatures and their callers, `python json.load` on every changed JSON file,
and reading the original 1.12.2 source. APIs were only used where the same call already existed elsewhere on the same branch; where no such call existed the fix was left out or flagged.

## 3. What to do first, locally

1. For each branch: `source env/activate.sh`, then `./gradlew build -x test` (compile errors are the most likely finding), then the suites from `CLAUDE.md`
   (`RT_AUTOTEST=true RT_AUTOTEST_NO_GUIS=true RT_AUTOTEST_EXIT=true ./gradlew runClient` and the `runServer` suite) and read `run/randomthings_audit.txt` and `run/randomthings_tests.txt`.
   Tests added or rewritten that day (never run): the Online Detector case test, the Peace Candle slime test, the Floo duplicate-name test, the Entity Detector empty Custom filter test, the Lotus Seeds farmland test, the coil loot pool asserts.
2. Retest the checklist rows in game (all marked fixed/added, waiting for retest). The riskiest assumptions are listed in section 5.
3. Then `/mirror` and `/drift` as usual if you change anything.

## 4. Not done

- **Luminous Powder glint rendering:** the recipe, NBT flag and tooltip exist, but the enchantment glint does not glow in the dark. 1.12.2 forced full-bright lighting around the glint render
  (`AsmHandler.preEnchantment` / `postEnchantment`); here it needs a new glint coremod hook (`docs/project/coremods.md`), which cannot be tested without the game.
- **Peace Candle churches in every village type:** only the plains village pool is wrapped (`worldgen/PeaceCandleChurchPool`). Desert, savanna, snowy and taiga (and zombie) village pools and church piece names are not referenced anywhere
  in the repo; they have to be read from the mapped jar. The cobblestone template will also look out of place in other biomes.
- **Nature Core and the Magic Bean source:** not ported (final decision). The Magic Bean is creative-only.
- **Wiki icons:** the recipe grids on the wiki show text labels for most ingredients because the texture downloader was blocked.
- **Parity bugs:** about 27 differences between branches were found and not fixed (`audit/PARITY_BUGS.md` in the `wiki` branch), for example Trigger Glass on 1.16.5+, the Magnetic config on 1.21.1+, weather cloud and bean stalk height limits on 1.18.2+.
- **Other reported bugs not fixed:** Block Breaker drop routing depends on the Magnetic config; Redstone Interface lines are drawn only for Observers; Slime Cube forces "allow" at any height; missing config keys (`RainShieldRange`,
  Ancient Furnace area); Stable Ender Pearl details; see `audit/MOD_BUGS.md` in the `wiki` branch.

## 5. Unproven assumptions to check first (per branch they differ, see the audit-fixes doc)

- Returning `PASS` / `false` from the block use method lets `RedstoneToolItem.useOn` run (Redstone Tool plain-click linking). Not verified against the vanilla/loader source. On 1.21.1 and 1.21.4 only the main hand is covered.
- Cancelling `PlayerInteractEvent.RightClickBlock` for the Ectoplasm sapling and paper-on-rune listeners (no earlier use on the newer branches).
- The cross-dimension Stable Ender Pearl uses each branch's own dimension-change call (`changeDimension`, `teleportTo`, `teleport`): check the player reference afterwards and any side effects.
- The enchanted-item check of the Luminous recipe on 1.20.6 and later loops over the enchantment registry (that exact call combination was never used before).
- `scheduleTick` from `onLoad` for the Redstone Observer (1.18.2 uses a block entity ticker instead); the Ender Bucket puts the source block back with `setBlock` after a simulated fill (possible neighbour-update blip).
- `forge:nbt` ingredient format of the Super Lubricent Tincture on 1.14.4 was already in place; not re-checked.
- Old worlds: Spectre Lens entries saved before the change have no dimension tag and are dropped on load (lenses re-register within 80 ticks); Ender Letter data keeps its saved-data name.

## 6. The wiki

`wiki` describes the intended behaviour, not the bugs. One claim still depends on code that is missing: the Luminous glint glow (section 4). The Magic Bean stalk breaking blocks was added on 2026-10-29 and has to be retested (row AG12).
