# Brief for Phase 4 verification agents (READ-ONLY; you change nothing in the wiki)

You re-check finished wiki pages (`data/features/<id>.json`, repo /home/user/Random-Things, branch `wiki`) against the mod code.
Write ONLY your report file. Do not edit pages, do not commit/push/fetch/download, do not run the game, do not edit the mod.

Mod source (1.14.4 baseline, read-only): MOD=/tmp/claude-0/-home-user-Random-Things/f859bde5-7624-5f08-9a19-e526dde76d34/scratchpad/mod1144
(`J/` = MOD/src/main/java/lumien/randomthings/, `R/` = MOD/src/main/resources/). Original source: `git -C /home/user/Random-Things show origin/1.12.2:<path>`.
Earlier fact sheets with citations: /home/user/Random-Things/audit/facts/*.md (A redstone, B machines, C spectre, D decor, E world, F tools, G entities, H sound).

## Procedure, per assigned page
1. Read the page JSON (description, properties, recipes). Split it into checkable statements (every sentence, every number, every property row,
   every recipe grid + result count + shapeless note).
2. For each statement find the code/data that settles it (file:line) and mark: CONFIRMED, CONTRADICTED, or UNVERIFIABLE (say why).
   Verify recipes against `R/data/randomthings/recipes/*.json` yourself (pattern, keys, tags, count, shaped vs shapeless; tag-to-label mapping like
   forge:stone = "Stone", minecraft:signs = "Any Sign" is acceptable). Verify config keys/defaults against `J/config/RTConfig.java`.
3. Also look for what the page gets wrong by OMISSION in a way that misleads (e.g. a gameplay rule a player would trip over). Report, do not fix.

## Facts already settled by the user's playtests (treat as CONFIRMED, do not flag as unverified)
Spectre Key return position kept even after death; Ender Letters/mailboxes work across dimensions; Floo works within one dimension only; Spectre
energy buffer shared across dimensions; Advanced Redstone Interface and Observer keep working after reload; slime block: one right click = one
compression step; Block Breaker does nothing on blocks an iron pickaxe cannot break; Diaphanous fade about 3.6 / 6.2 blocks; Online Detector name is
case sensitive; Chunk Analyzer scan takes several seconds; Rain Shield suppresses rain particles and sounds; Sound Dampener/Recorder work on servers
and clients; Magnetic as rare as Silk Touch, not on hoes or shears; Blaze and Steel fire spreads and burns out faster; Lotus grows on farmland only
(the code also accepts grass/dirt: that is a known mod bug, the page describes the intended behaviour); Lapis Lamp / Peace Candle / Slime Cube affect
spawner mobs but not spawn-egg mobs; Redstone Tool linking works (right click Observer/Interface, then the target block); Peace Candle should not ignore
slimes and church generation should not be plains-only (known mod bugs; the page describes the intended behaviour); Advanced Item Collector range 0..16
(default 5) with no config; Spectre Lens is intended per player per dimension (known mod bug; the page keeps that wording).
Known mod bugs and user-owned decisions are NOT wiki errors: do not report them as CONTRADICTED, mention them only as "known".

## Rules for judging
- "Contradicted" needs a citation. Vanilla values from memory are not evidence. No version wording belongs on pages (flag if you see any).
- Wiki rule: pages describe the mod as it should behave today; no version differences, no mod-bug descriptions.

## Report `audit/verify/<your id>.md`
Per page: `## <id>`: a table-free list `CONFIRMED n / CONTRADICTED n / UNVERIFIABLE n`, then each CONTRADICTED and UNVERIFIABLE item as
`- "<statement>" -> <what code says> (<citation>)`, then `MISLEADING-OMISSIONS:` and `RECIPE-CHECK: ok|problem (...)`. Reply with <= 12 lines
(counts and the most serious contradictions).
