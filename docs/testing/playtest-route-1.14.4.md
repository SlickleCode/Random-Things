# 1.14.4 playtest route

An ordered route through everything still needing a human on the 1.14.4 branch (see `TESTING_CHECKLIST.md`). Each step names the checklist row; tick it off or tell Claude "#NNN passes / fails: ...".
Run with `./gradlew runClient`. Total time: roughly 90 minutes, Stage A being the bulk.

Legend: **[RETEST]** = row marked FIXED, was broken or changed. **[NEW]** = never tested. **[2P]** = needs a second player (LAN client or server), do last or skip.
Item ids are `randomthings:<name>`; give yourself things with `/give @s randomthings:<name>`.

## Already verified by the scripted scenarios (2026-10-11) - you can skip these

`python -u scn.py all` (see `docs/testing/README.md`) checks the mechanics of: A1 (#158-160 muting by distance/removal/portable), A2 (#169-179), A3 (#234 fluid + toggle + rotate, #238 craft, #277 counter/teleport, #279 craft + donor), A4 (#284 mirror, #287 button pulse), A5 (#293/#294 exact + fuzzy via the GUI button), A6 (#321-324 tags/closing; #254 only screenshots), A7 (#258/#259, #319 toggles), A8 (#209/#210/#212/#214/#215 delivery + glow, #356/#357), A9 (#149-153), A10 (#121, #288, #343/#344), B (#265, #387).
What is **left for your eyes and ears** in those steps: how things look (tints, glint colours, the Light Redirector periscope picture, Fluid Display textures, Portkey glow stopping, Magic Hood particles), that muted sounds are really silent, the Redstone Remote *edit* GUI binding, #211 (online only), #213 (2 players), #295/#296, Spirit, Runes, Spectre Coil, and Stage C. Screenshots from the run are in `prod-test/desktop/shots/`.

## Stage 0 - worlds (5 min)

1. **World 1, "Playtest A":** Creative, *Superflat*, cheats ON, Allow Commands ON. Everything in Stage A happens here (no mobs, no weather unless you ask for it).
   First commands: `/gamemode creative`, `/time set day`, `/gamerule doDaylightCycle false`, `/gamerule doWeatherCycle false`.
2. **World 2, "Playtest B":** Creative or Survival, default world type, a fresh seed. Used in Stage B (worldgen).

Build a flat stone pad about 20x20 next to spawn in World 1 and a `stone` wall to test against. Keep the stack of redstone gear handy: `/give @s minecraft:redstone 64`, `minecraft:lever 4`, `minecraft:redstone_torch 4`, `minecraft:stone 64`.

## Stage A - World 1 (creative superflat)

### A1. Sound Dampener and patterns (#158 RETEST, #159, #160 NEW)
1. `/give @s randomthings:sound_recorder`, `randomthings:sound_pattern 4`, `randomthings:sound_dampener`, `randomthings:portable_sound_dampener`.
2. Make a few sounds (open a chest, place a block, `/summon minecraft:chicken`), right-click the Sound Recorder, put a **blank Sound Pattern** in its slot and click a recorded sound; repeat so you have 3-4 named patterns.
3. **#158 tooltip fix:** hover a named pattern in the recorder, in the Sound Dampener GUI and in the Portable Sound Dampener GUI - a tooltip with the sound name must show in all three (it was missing in the dampeners).
4. **#158:** place the Sound Dampener, put patterns in; make that sound within ~20 blocks (e.g. place a chest and open it, or summon the mob): silent. Walk away >20 blocks: normal again.
5. **#159:** with the Portable Sound Dampener anywhere in your inventory and patterns inside it: those sounds are silent for you wherever you are.
6. **#160:** break the placed dampener (patterns drop, muting stops); take a pattern out of a slot (that sound is audible again).

### A2. Imbuing Station and Imbue potions (#169 RETEST, #170-179 NEW)
1. `/give @s randomthings:imbuing_station`, then ingredients: `minecraft:cobblestone`, `minecraft:vine`, `minecraft:bone_meal`, `minecraft:coal`, `minecraft:flint`, `minecraft:blaze_powder`, `minecraft:spider_eye`, `minecraft:rotten_flesh`, `minecraft:red_mushroom`, `randomthings:lesser_magic_bean`, `minecraft:lapis_lazuli`, `minecraft:glowstone_dust`, `minecraft:wither_skeleton_skull`, `minecraft:nether_brick`, `minecraft:ghast_tear`, and four water bottles (`/give @s minecraft:potion{Potion:"minecraft:water"} 4`).
2. **#169:** cobblestone in the center + water bottle + vine + bone meal in the 3 ingredient slots: after ~10 seconds you get Mossy Cobblestone, 1 of each input consumed.
3. **#170-173:** water bottle in the center plus: coal+flint+blaze powder = Fire Imbue; spider eye+rotten flesh+red mushroom = Poison; lesser magic bean+lapis+glowstone dust = Experience; wither skull+nether brick+ghast tear = Wither.
4. **#175:** drink each (`/effect clear @s` between): effect lasts 20 min, glass bottle returned, own icon; drinking a second kind replaces the first.
5. **#176-179:** with Fire active hit a mob (`/summon minecraft:zombie`) in melee: burns 10 s. Poison: Poison II 10 s. Wither: Wither II 5 s. Experience: kill a mob, XP orbs are doubled. Projectiles do not count.

### A3. Fluid Display, Evil Tear, Portkey (#234, #238, #277, #279 RETEST)
1. **#234:** `/give @s randomthings:fluid_display`, `minecraft:water_bucket`, `minecraft:lava_bucket`. Place it, right-click with a filled bucket: it takes on the fluid texture/colour, still image.
2. **#238:** craft an Evil Tear in a crafting table from Wither Skeleton Skull + Ghast Tear + Ender Pearl (any arrangement).
3. **#277:** `/give @s randomthings:portkey`; drop it on the ground: it shimmers; after ~5 s undisturbed it stops glowing (and would stop despawning). Pick it up and drop again: the 5 s countdown restarts.
4. **#279:** combine the Portkey with any single item (e.g. a diamond pickaxe) in a crafting grid: it is disguised as that item in inventory, held, dropped on the ground and in an item frame; the donor item is returned.

### A4. Redstone Remote (#287 RETEST) and Redstone Interface (#284 NEW)
1. **#287:** craft it (3 Redstone Activators on top, obsidian + Stable Ender Pearl in the middle row, obsidian bottom). Sneak-right-click: an edit GUI with 9 Position Filter slots. Bind a few targets (use `randomthings:position_filter`, set position by right-clicking a block with it), then plain right-click: one button per target; pressing it fires a 20-tick strength-15 pulse at that spot (use redstone lamps).
2. **#284:** place a Basic Redstone Interface next to a lever, bind a target position (Position Filter) with a lamp/redstone wire beside it: the target mirrors the interface's input live, and stops when you break the interface.

### A5. Block Destabilizer (#293 RETEST, #294-296 NEW)
1. `/give @s randomthings:block_destabilizer`, build a wall of mixed stone and (separately) a stack of oak logs of two rotations.
2. **#293:** put it facing the wall, pulse redstone: it flood-fills only blocks with the same exact state that are full cubes (up to 50) and drops them as falling blocks, top row first.
3. **#294:** in its GUI turn *Fuzzy* on: it matches by block type only (all log rotations, all stained-glass colours).
4. **#295:** *Lazy* remembers non-matching blocks so re-triggering is faster; *Reset* clears it.
5. **#296:** a falling block landing on a block/tile entity vanishes without dropping an item (no item entity appears).

### A6. Dyeing Machine (#321-324 NEW) and Magic Hood (#254 RETEST)
1. `/give @s randomthings:dyeing_machine`, `minecraft:leather_chestplate`, `minecraft:iron_chestplate`, `minecraft:diamond`, `minecraft:red_dye`, `minecraft:blue_dye`.
2. **#321:** an item in the left slot + a dye: the "recolor" result tints the item everywhere it renders (held, inventory, on the ground), even items with no tint support (the diamond).
3. **#322:** the "enchant glint" result on an enchanted item shows the dye colour instead of purple.
4. **#323:** wear the recoloured iron chestplate: the worn armor on your body is tinted too (F5 view).
5. **#324:** close the GUI with something still in the slots: items drop at your feet, none deleted.
6. **#254:** `/give @s randomthings:magic_hood`, wear it, then `/effect give @s minecraft:speed 120` : no particle swirl around you (switch to F5), sneaking or not.

### A7. Light Redirector (#319 NEW) and Rain Shield (#258, #261 RETEST, #259 NEW)
1. **#319:** `/give @s randomthings:light_redirector`. Right-click faces to toggle them open; build a diamond block on one side and see it drawn on the opposite open face (the "periscope"). Toggle faces to confirm.
2. **#258/#261:** `/give @s randomthings:rain_shield`, `minecraft:farmland`-capable dirt (`minecraft:dirt`, `minecraft:wheat_seeds`), `minecraft:flint_and_steel`. Place the shield, `/weather rain`: within 80 blocks horizontally there are no rain streaks or splashes; no wetting/snow/ice/fire extinguishing (light a fire next to it; hoe some dirt next to water: farmland stays dry-state-agnostic, the fire must not go out in the rain). Walk >80 blocks away and rain is back.
3. **#259:** power the shield with a lever: rain and its effects return to normal near it; switch off: suppressed again. `/weather clear` afterwards.

### A8. Ender Mailbox and Ender Anchor (#209-215, #356-359 NEW)
1. `/give @s randomthings:ender_mailbox`, `randomthings:ender_letter 2`.
2. **#209/#212:** place the mailbox, right-click it (not sneaking): opens your inbox; a second mailbox of yours opens the same inbox.
3. **#210:** open an Ender Letter, type **your own name** in the receiver field, put an item in, close; sneak-right-click a mailbox holding the letter: the letter is consumed and arrives in your inbox (sound).
4. **#214:** after delivery the mailbox glows/emits portal particles within ~10 s, stops once you take everything.
5. **#215:** take the received letter, right-click it: opens read-only (output only), tooltip shows the sender.
6. **#211:** address a letter to a player name that does not exist (use an invalid name such as `No_Such_Player_Name_999`; `Zxqvvz` may be a real account, and an offline client resolves any name so this needs an online session): "does not seem to reside in this world", letter untouched. (An empty receiver is separate, already verified.)
7. **#213 [2P]:** a second player right-clicking *your* mailbox gets "You are not the owner".
8. **#356-358:** `/give @s randomthings:ender_anchor`. Place it, run `/forceload query` at its chunk: force-loaded. Save and quit, reload: still force-loaded. Break it: un-forced (unless another anchor is in that chunk). In `config/randomthings-common.toml` set `[features] EnderAnchorChunkloading = false`, restart: placed anchors force nothing, and previously forcing anchors release.
9. **#359:** hover the Ender Anchor item: "Keeps the chunk it is in loaded" (gone with the config off).

### A9. Biome Radar (#149-153 NEW)
1. `/give @s randomthings:biome_radar`, `minecraft:iron_bars 32`, `minecraft:paper 4`, and a Biome Crystal from the creative tab (any biome).
2. **#149:** right-click the radar with an empty hand while holding the crystal: inserts; right-click again empty-handed: takes it out.
3. **#150:** build the iron-bars antenna above it (small frame 2-3 blocks up), insert a crystal, power with redstone: it searches and coloured particles appear above the antenna.
4. **#151:** when the search finishes particles change to the biome colour; right-click with paper: paper is consumed and you get a Position Filter pointing at the biome.
5. **#152:** during a search break an antenna bar not adjacent to the radar: within seconds it drops back to idle.
6. **#153:** break the radar with a crystal inserted: the crystal drops.

### A10. Quick one-offs (10 min)
- **#121 Bean Stew:** `/give @s randomthings:bean_stew`, eat it: 8 hunger, empty bowl left (use `/effect give @s minecraft:hunger 10 5` first).
- **#288 Magnetic:** `/give @s minecraft:diamond_pickaxe{Enchantments:[{id:"randomthings:magnetic",lvl:1}]}`; mine blocks: drops go straight into your inventory. (Also obtainable from an enchanting table / anvil book.)
- **#309 Spectre Coil (Genesis):** creative tab: attach it to a machine that takes FE; it feeds it flat 10,000,000 FE/tick.
- **#343/#344 Colored Grass:** craft grass seeds + a dye; right-click dirt: tinted grass; it spreads onto nearby lit dirt; cover with a block and it reverts; break it: dirt (Silk Touch keeps the colour).
- **#346 Runes:** place rune dust on a Rune Base: noisy texture, stable while you move, connections across base edges.
- **Spirit (#325-328):** `/give @s randomthings:spectre_sword`, kill ~100 mobs (`/summon` in bulk); a tiny wisp spawns rarely; it drifts, only a Spectre Sword hurts it, 1-2 Ectoplasm drop. Do this last or skip: very low chance.

## Stage B - World 2 (new default world)

1. **#387 Glowing Mushroom [RETEST, important]:** explore caves for a while: patches of glowing mushrooms (about one patch per 4 chunks, never with sky access, about 4 % of chunks hold some). In a dark cave place one yourself (`/give @s randomthings:glowing_mushroom 5`): it must stay (previously it dropped itself), glow, and slowly spread to dark neighbouring spots. (Automated tests and a real-world chunk scan already cover generation: 77 of 1,687 chunks.)
2. **#265 Ancient Furnace [RETEST]:** in a snowy biome (use just fly until F3 shows Snowy Tundra) place `randomthings:ancient_furnace`, wait a few minutes: it melts snow and ice around it in a large radius, turns the biome warmer (e.g. Snowy Tundra to Plains), then explodes.
3. Optional: loot/config rows (#329-340, #345, #347, #353-355): break the listed Batch 1 blocks in survival and check each drops itself; open a few dungeon chests for Summoning Pendulum / Magic Hood / Slime Cube / Biome Crystal; flip the listed `config/randomthings-common.toml` switches and re-check the matching behaviour.

## Stage C - second player [2P] (optional)

- **#253 Magic Hood:** the second player should not see your nametag while you wear it, sneaking or not.
- **#213 Ender Mailbox:** their right-click on your mailbox gives "You are not the owner".
- **#158 Sound Dampener:** a sound made by the *other* player within 20 blocks of the dampener is silent for you too.

## Done?

Report results as "#158 pass, #234 fail: ..." and Claude updates `TESTING_CHECKLIST.md`. Anything that fails gets investigated against the 1.12.2 source first.
