# Shared brief for the wiki-audit fact-sheet agents (READ-ONLY inventory)

Goal: for the feature group assigned to you, produce a behavior fact sheet of what the Random Things mod
does TODAY on the 1.14.4 baseline, so a later step can diff it against the wiki pages. You write NOTHING
except your own report file (path given in your task). Never edit the mod source, never run gradle/the
game, never commit/push/download. Do not use the network.

## Where things are (absolute paths)
- MOD = /tmp/claude-0/-home-user-Random-Things/f859bde5-7624-5f08-9a19-e526dde76d34/scratchpad/mod1144
  (a read-only `git archive` of origin/1.14.4). Cite paths relative to MOD, e.g.
  `src/main/java/lumien/randomthings/block/IgniterBlock.java:57`.
- Java: MOD/src/main/java/lumien/randomthings/{block,item,tileentity,container,client/screen,entity,handler,
  enchantment,potion,recipes,worldgen,config,network,util,asm}. Registration lists: block/ModBlocks.java,
  item/ModItems.java, tileentity/ModTileEntityTypes.java, container/ModContainerTypes.java,
  entity/ModEntityTypes.java, potion/ModEffects.java, enchantment/ModEnchantments.java, worldgen/ModFeatures.java,
  recipes/ModRecipeSerializers.java, config/RTConfig.java, handler/LootHandler.java (chest loot injection),
  handler/MagneticLootModifier.java.
- Data: MOD/src/main/resources/data/randomthings/{recipes,loot_tables/blocks,structures}, data/forge/loot_modifiers,
  assets/randomthings/lang/en_us.json (display names + tooltips), coremods in src/main/resources/transformer/*.js
  + META-INF/coremods.json, docs/project/coremods.md.
- Wiki data you will compare against later (you only READ the pages of your group, to know which claims matter):
  WIKI = /home/user/Random-Things/data/features/<id>.json (fields: title, category, description, keywords,
  properties, recipe...). Read the page(s) of your group first, so your sheet answers every claim they make
  AND records facts they omit. Do NOT write the verdict (wrong/ok) - that is the next phase - but DO flag a
  wiki claim you saw is contradicted by code in a `WIKI-CLAIM-CHECK` line at the end of each feature.
- Docs for context (do not treat as evidence of behavior): MOD/docs/project/wiki-feature-status.md,
  MOD/docs/project/scope-decisions.md (binding decisions, deliberate divergences from 1.12.2), MOD/TESTING_CHECKLIST.md
  and MOD/docs/testing/** (evidence of what was tested), MOD/src/main/java/lumien/randomthings/debug/test/*
  (automated behavior tests). Its `DONE-UNTESTED` labels are stale - not evidence.
- Original 1.12.2 source: `git -C /home/user/Random-Things show origin/1.12.2:<path>` (branch is fetched? if not,
  say "1.12.2 not checked" - do NOT fetch). Use it only when needed to tell "original behavior" from a port change.

## What each feature's sheet must contain (cite `path:line` for EVERY fact; no citation = don't state it)
1. Registry: block/item/TE/entity/etc. registry names (exact), what wiki feature id(s) they belong to.
2. Obtaining: exact recipe(s) from data/randomthings/recipes/*.json (type shaped/shapeless/special, pattern, keys,
   tag ingredients, output count) - read the JSON, quote the pattern compactly; loot (chest tables incl. config
   gating in LootHandler/RTConfig, block loot tables, mob drops), worldgen (features, structures, biomes, config),
   creative-only? 
3. Behavior: GUI and every control; redstone input/output, per-side behavior; stored state/NBT; dimension/biome/
   light/time/weather conditions; ranges, cooldowns, durability, stack size, tick rates; permission/creative vs
   survival/multiplayer differences; interactions with other features; particles/sounds; config options (key,
   default, effect); coremod dependence (name which transformer); tooltips from lang file.
4. Deliberate divergences from 1.12.2 / simplifications (from scope-decisions.md or code comments) - one line each.
5. Evidence grade per fact (a bracket tag at the end of the line): [code] read in source; [test] a behavior test or
   a PASS row in TESTING_CHECKLIST.md covers it (cite test file:line or checklist row #); [playtest] only if a
   checklist/fix-log row says it was verified in game (cite); [unverified] you could not confirm. If something
   can only be settled by running the game (rendering, timing, GUI feel), list it under PLAYTEST-NEEDED.
6. PARITY: if you notice (cheaply) that another branch differs, you cannot see other branches - skip, unless
   docs/project/version-notes.md or a code comment states a cross-version difference for the feature; then note it
   under PARITY (cite the doc line). Do not document version differences as facts.
7. Registered things in your group's area that have NO wiki page (or wiki pages with nothing registered) go under
   `UNMAPPED:` at the end of the report.

## Report format (strict; keep it compact - facts, not prose; target <= 60 lines per feature, less for simple ones)
```
## <wiki id> (<title>)  [registry: ...]
OBTAIN: ...
BEHAVIOR: ...
CONFIG: ...
DIVERGENCE: ...
PLAYTEST-NEEDED: ...
PARITY: ...
WIKI-CLAIM-CHECK: <claim> -> contradicted/confirmed/not-in-code (cite)
```
Finish with `UNMAPPED:` and `OPEN QUESTIONS:` sections. Write the report to the file named in your task, then
reply with a <=15 line summary (counts of features, number of [unverified] facts, notable contradictions) - not
the report body.
Rules: be honest about evidence - never state behavior from memory of the original mod; if you cannot find it in
the code, say so. ASCII only in your report.
