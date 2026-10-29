# Random Things 1.18.2 - Testing Checklist

Only what needs a person is listed here (looks, sound, feel, multiplayer). Everything an automated test checks is out: see [`docs/testing/covered-by-tests-1.18.2.md`](docs/testing/covered-by-tests-1.18.2.md). The old full sheets are in [`docs/testing/checklist-archive/`](docs/testing/checklist-archive/README.md).

**World for parts A-C:** `RT_AUTOTEST=true RT_MANUAL=true ./gradlew runClient` ([`docs/testing/manual-test-world.md`](docs/testing/manual-test-world.md)): every block on show with a sign, set-ups and a stocked inventory. It is deleted when the game closes. Parts D-F need a normal survival world (D also a dedicated server and a second client).

Fill in **Result** with `Pass` or `Fail` and a sentence in **Feedback**; blank = untested (nothing on this branch has been tested by hand yet). Row numbers are the original sheet's. Several rows cover a group, written `29, 30, 33-38`.

## A. Look around the museum (rendering and item models)

| # | Look at | Result | Feedback |
|---|---|---|---|
| 1-5, 52, 53, 57, 58 | Contact Lever (item, off, on), Luminous Brick and Stained Brick (spot-check a few of the 16 colours; luminous ones glow), Luminous Block, Luminous Translucent Block (glows and is see-through) |  |  |
| 29, 30, 33-38, 50 | Lapis Lamp, Quartz Lamp, Compressed Slime Block, Contact Button, Spectre Block / Log / Plank / Leaves, Sided Redstone look right |  |  |
| 39, 56, 62, 66 | Spectre Sapling grows into a tree; Colored Grass (16 colours via Grass Seeds, tinted, spreads, "<Color> Grass Block"); Bean Sprout also plants on grass/dirt; Lotus Seeds plant Lotus |  |  |
| M15 | Lotus and Bean Sprout render with proper transparency |  |  |
| 59-61, 63-65 | Beans / Lesser Magic Bean / Magic Bean plant the right sprout and stalk; breaking a Pod drops beans |  |  |
| 15, 18, 28 | Trigger Glass is translucent, clearly more so when powered; a mob paths around Quartz Glass instead of into it |  |  |
| glass blocks | Trigger / Lapis / Quartz Glass, Diaphanous, Spectre and the other see-through blocks: no holes in the faces of blocks next to them |  |  |
| M10 | Spectre Block is more opaque than before |  |  |
| 31, 32, 136, 113, M1 | Super Lubricent Ice / Platform / Stone / Filtered Platform: placing and breaking plays the ice (glass) sounds; the Filtered Platform sits as low in its item box as the other platforms (R12); items matching its filter fall through |  |  |
| M2 | Blazing Fire on netherrack looks like vanilla fire (flame shape, not a solid box) |  |  |
| M3, N4, 321-323 | Glint colours: Redstone Tool, Spectre Key, Escape Rope, Spectre Sword and Portkey are solid (not faded) and glow in their colour; Dyeing Machine results are tinted held, in the inventory, on the ground and worn (leather and non-leather armor, armor stand), and the glint result glows in the dye colour. |  |  |
| M9, M11, M12, 366, 381, N10 | Item models in the inventory, hand, ground and item frame: Peace Candle lit evenly, Ender Mailbox fits its slot, Fluid Display is an isometric cube, Portkey (plain and camouflaged) is normal size, tinted items, compasses, Time in a Bottle |  |  |
| M12 | Spirit texture is a deep, pure blue |  |  |
| M16 | Ender Bucket and Reinforced Ender Bucket draw their fluid in the opening (water blue, lava orange); held in first person too |  |  |
| 234 | Fluid Display: empty, water, lava, rotation; still vs flowing; the poured fluid's colour; no holes beside sand/stone; hidden behind blocks in front of it |  |  |
| 346, 347 | Runes: noisy texture, same-colour pixels join across the base edge, no flicker while moving; `FlatRunes = true` gives the plain texture |  |  |
| 315, 317, M13, N11 | Diaphanous Block: the displayed block fades in with distance (nearly invisible up close, solid past ~33 blocks), no collision while not inverted, same per-face shading as a normal block, break particles of the shown block, the dropped item remembers block and inverted flag |  |  |
| 319 | Light Redirector: open faces show the block on the opposite side (periscope) |  |  |
| 261 | Rain Shield: no rain or snow visible inside its radius |  |  |
| 260 | Rain Shield: ring of flame and smoke particles around its candle while raining |  |  |
| 254 | Magic Hood: own green texture, no potion swirl around you in F5, other players see no nametag (two clients) |  |  |
| 149-153 | Biome Radar: crystal spins in the middle, antenna particles change colour when the search ends; an empty hand takes the crystal out even after the search finished |  |  |
| M5, 201 | Time in a Bottle: every face of the accelerated block shows the spinning wheel of 1.12.2 (decagon rings, one more ring at time rate 2, 4, 8, 16 and 32), not the floating bottle icon - FIXED 2026-10-29 (`TimeWheel` + `TimeAcceleratorEntityRenderer`; render scenes `time_wheel` and `time_wheel_close` checked on all nine branches); the wheel is about a third of a block across, as in 1.12.2 |  |  |
| 194, 195 | Eclipsed Clock: hangs at the right size, the dial follows the target time, floating time text shows when you look at it |  |  |
| 189-192 | Floo: departure and arrival flame bursts visible to a nearby player; a dropped Floo Token turns into a temporary fireplace after a few seconds (chat teleports for free there), a second token near it does not duplicate |  |  |
| 205, 241, 242 | Weather Egg clouds look different per type while rising; the Artificial End Portal charges ~10 s with sparkles, then shows the end starfield |  |  |
| 273, M6 | Spectre Illuminator: after ~12 s the orb settles in the middle of the chunk, the chunk lights up over about a minute, and goes dark again when picked up |  |  |
| 217 | A received unopened Ender Letter shimmers in the inventory |  |  |
| 229, 276 | Tooltips: Summoning Pendulum ("Holding N ..." and, with Shift, the mob names), Portkey (Shift shows dimension and x/y/z, plain hover shows a hint), Biome Crystal biome, Spectre Charger, Floo Pouch; no raw `block.randomthings.xxx` or `item.randomthings.xxx` text anywhere (118) |  |  |
| N3 | Redstone Observer + Redstone Tool: red lines to the bound target are visible while holding the tool |  |  |
| N12 | Durability bars of Floo Pouch and Summoning Pendulum show the right fill and colour |  |  |
| M8 | The sign at the south edge (east of the set-ups) gives distance, direction and x/z of the closest plains village with a Peace Candle church |  |  |
| M14 | After closing the game the museum world is gone and rebuilt at the next start |  |  |

## B. Machines and GUIs

| # | Try | Result | Feedback |
|---|---|---|---|
| N14, 120, M7 | Every GUI looks right: buttons, text and counters visible (Sound Recorder names, titles, "Inventory"), scroll lists (Sound Recorder, Chunk Analyzer, Block Destabilizer), tooltips, tinted bars (Dyeing Machine swatch, Potion Vaporizer tank), mode buttons (Igniter, Entity Detector) legible |  |  |
| 77, 82, 83, 87 | Item Collector faces look right on all six sides; Iron Dropper opens a 9-slot GUI, faces six ways on placement, drops its contents when broken |  |  |
| 80, 81 | Advanced Redstone Repeater: turn-on and turn-off delays are respected independently |  |  |
| 89, 90 | Player Interface exposes the placer's inventory per side (hotbar below, armor above, offhand north, rest elsewhere); Inventory Tester signal follows whether the held item fits, with invert |  |  |
| 98, 99 | Potion Vaporizer burns fuel; the bottle-out slot only fills from the machine |  |  |
| 104-106, 112 | Entity Detector filter modes (All/Living/Animal/Monster/Player/Items), Custom mode with an Entity Filter, radius 0-10 per axis; Advanced Item Collector range, per-axis radius, filter slot |  |  |
| 114, 363 | Notification Interface toasts only the linked player on a redstone rising edge; its icon slot copies on click, clears on empty-hand click, shift-click does nothing |  |  |
| 364, 365 | Redstone Observer / Basic Interface show "Target" only when set; Remote use screen shows the camo item's name on hover; Advanced Item Collector GUI has no stray empty space |  |  |
| 174 | Imbuing Station: arrow and per-slot fill animations only run while a recipe is in progress; the output slot rejects manual insertion |  |  |
| 145-148 | Chunk Analyzer: Scan works, results persist after closing and reopening, scanning while moving items in your inventory or in a tall full chunk behaves |  |  |
| 154, 155 | Sound Recorder: sneak-right-click records, a blank Sound Pattern plus a clicked sound name gives a stamped pattern (blank consumed) |  |  |
| 166, 167 | Rune Pattern copies a Rune Base's pattern (tooltip lists colours and counts); sneak-right-click gives paper back |  |  |
| 163 | Left-clicking a Rune Base pops every pixel off as Rune Dust and removes the block |  |  |
| 208, 213, 216, 361, 362 | Ender Letter / Mailbox: write GUI with a receiver field and a message box (4 lines, wrap, Enter, 200 character cap `n/200`, survives reopen), tooltip "Sneak + right-click an Ender Mailbox to send" on unsent letters only, title "Incoming Mail", another player's mailbox says you are not the owner, an emptied received letter disappears |  |  |
| 237, 244 | Fluid Display drops itself; Creative Player Interface is creative-menu only and behaves like the Player Interface |  |  |
| 247 | Ender Bridge faces the direction you look when placed (including up and down) |  |  |
| 290 | Block Breaker mines at real survival speed, respecting hardness and tool |  |  |
| 224 | Reinforced Ender Bucket: sneak while picking up drains the whole connected body |  |  |
| 124, 125, 141 | Stable Ender Pearl: throw it bound and unbound; Emerald Compass goes back to wobbling when the bound player logs off and resumes on login |  |  |
| 137, 139 | Golden / Emerald Compass with no target wobble randomly and never settle |  |  |
| 123 | Bottle of Air refills the whole breath meter in water and is consumed |  |  |
| 122 | Blaze and Steel places Blazing Fire on a face and takes 1 durability |  |  |
| N13 | Block entity data syncs live to the client without relogging: Observer target, Sound Dampener list, Biome Radar crystal, Redstone Interface |  |  |
| N15 | Near y -64 and y 320 the mod blocks build and dig fine; the Spectre Illuminator still lights an area |  |  |

## C. Items, mobs and the Spectre dimension in survival

| # | Try | Result | Feedback |
|---|---|---|---|
| 188, 199-201, 203, 204, 252 | Typing the name of the Floo fireplace you stand on says "You are already at this destination"; Time in a Bottle fills while carried (tooltip H/M/S) and right-click on a block / Time Accelerator uses it; Weather Egg lands a cloud that rises ~10 s, floats to the height limit, then sets the weather; Magic Hood has no crafting recipe |  |  |
| 230, 231 | Golden Egg always hatches a Golden Chicken and hits for 1; the chicken follows seeds, never lays eggs |  |  |
| 239-243 | Artificial End Portal: build the ritual structure and use an Evil Tear on the End Rod; an invalid End Rod does nothing; breaking part of the structure removes the portal; standing in it teleports to the End (not while riding) |  |  |
| 262, 266 | Ancient Furnace is rare in cold biomes (top flush with the ground); heated up it turns the area's biome colour warm and melts the snow in the whole area (the client sees it without relogging) |  |  |
| 325, 326 | Spirit: killing mobs sometimes spawns one (more at night under a full moon, more after the Ender Dragon); it drifts in the air and despawns on its own |  |  |
| 298-302, N5 | Spectre Key (hold 5 s, cyan charge-up) teleports you into your own cube; the room is unbreakable with a Spectre Core pedestal; Ectoplasm raises the ceiling; the dimension is dark and foggy with no sun, moon or clouds; leaving (key, Spectre Anchor) returns you to the same spot; no portal glitches; rejoining the world after a visit does not crash |  |  |
| 304-306 | Spectre Pickaxe / Axe / Shovel recipes work |  |  |
| 309 | Spectre Coil (Genesis) generates 10,000,000 FE/tick into what it is attached to (creative tab only) |  |  |
| 313 | Spectre Lens on a powered Beacon gives its owner the beacon effects everywhere |  |  |
| N9 | Fertilized Dirt and Colored Grass mine fast with a shovel, Platforms and Block of Sticks with an axe |  |  |
| 348 | The recipe book shows and crafts the backfilled recipes (Item Collector, Igniter, Iron Dropper, detectors, Inventory Tester, Notification Interface, Redstone Observer/Tool, Advanced Redstone Repeater, Potion Vaporizer, Player Interface, Slime Cube, ...) |  |  |

## D. Sound

| # | Listen for | Result | Feedback |
|---|---|---|---|
| 158 | Sound Dampener: the muted sound is silent near the block and audible again beyond 20 blocks |  |  |
| 154 | Sound Recorder records footsteps, mobs and blocks while you walk around |  |  |

## E. Worldgen, loot and config (survival world)

| # | Try | Result | Feedback |
|---|---|---|---|
| N7 | Peace Candle: in a real plains village roughly a third of the churches have one |  |  |
| N8 | Glowing Mushrooms appear in caves between y 0 and the surface, not in the deepslate layer |  |  |
| 334, 335 | Chests: Summoning Pendulum, Magic Hood, Slime Cube appear in dungeon / weaponsmith / stronghold / jungle-temple chests; a Biome Crystal in about 1 of 5 vanilla chests, already tuned to a biome |  |  |
| 336, 337 | First launch creates `config/randomthings-common.toml` with `[worldgen]`, `[loot]`, `[features]` booleans, all `true`; `ArtificialEndPortal = false` makes Evil Tear do nothing; `Lotus` / `AncientFurnace = false` stop new ones in fresh chunks |  |  |
| 345 | `[numbers]` `SpiritChanceNormal`, `SpiritChanceMoonMult`, `SpiritChanceEndIncrease` change spawn odds (`SpiritChanceNormal = 1.0` spawns one on every kill) |  |  |
| 353 | `HideCoordinates = true`: Portkey tooltip only says `<Target set>` / `<No target set>`, Position Filter shows no coordinates and no "hold Shift" hint; `false` restores both |  |  |

## F. Server, multiplayer and old worlds

| # | Try | Result | Feedback |
|---|---|---|---|
| 383 | Dedicated server with a real client: Spectre entry, exit to the same overworld position, unload; right-clicking an Eclipsed Clock with a charged Time in a Bottle fast-forwards without a crash |  |  |
| M18 | Open a world saved by the previous version (`docs/testing/fixture-world.md` lists what survives and the three known upgrade problems: 1.14.4 Spectre cube, 1.16.5 to 1.18.2 world refuses to open, 1.20.1 to 1.20.6 item stack counts and data) |  |  |

## G. Audit bug fixes (2026-10-29; written without a compiler, NOT built or run - build, run the suites, then retest in game)

Section letter G because E and F were already taken on this branch (the 1.14.4 branch calls it E). Details and unverified points: [`docs/project/audit-fixes-2026-10-29.md`](docs/project/audit-fixes-2026-10-29.md). All rows are **fixed, waiting for your retest**.

| # | Try | Result | Feedback |
|---|---|---|---|
| AF1 | Redstone Tool: a plain right click (no sneaking) on a Redstone Observer or a basic Redstone Interface starts a link (glint), a right click on any block finishes it; sneaking still works; both blocks still open their GUI with an empty hand |  |  |
| AF2 | Online Detector: a name that differs from the player's name only in upper/lower case does not power it (`behavior.online_detector.matches_the_player_name_case_sensitively`) |  |  |
| AF3 | Lotus Seeds only plant on farmland (not on grass or dirt); naturally generated Lotus in snowy biomes stays put |  |  |
| AF4 | Peace Candle within 3 chunks stops slime spawns too, also inside an unpowered Slime Cube's chunk (`coremod.spawn.peace_candle_denies_slimes_even_inside_an_unpowered_slime_cube_chunk`); Slime Cube, Lapis Lamp and Quartz Lamp unchanged elsewhere |  |  |
| AF5 | Spectre Lens: one lens per player per dimension, it only affects its owner while the owner is in the lens's dimension; a second lens by the same owner in the same dimension replaces the first; old worlds: a lens re-registers within 80 ticks |  |  |
| AF6 | Ender Letters and Ender Mailboxes work across dimensions (send from the Nether, receive in the Overworld); letters from before the change are still there |  |  |
| AF7 | Sound Box and Slime Cube keep their powered state through save and reload (no replayed sound, no reset slime gate) |  |  |
| AF8 | Advanced Redstone Torch: edited green/red strengths are still set after save and reload |  |  |
| AF9 | Potion Vaporizer drops its stored items when broken (and not when its blockstate only changes) |  |  |
| AF10 | Ender Bucket / Reinforced Ender Bucket: clicking a different fluid than the one inside (or when full) does not remove the source block; watch for a neighbour-update blip when the block is put back |  |  |
| AF11 | Eclipsed Clock: the dropped item keeps its target time and restores it when hung again; sneak-clicking from a target below 00:30 wraps to the evening instead of showing a negative time |  |  |
| AF12 | Stable Ender Pearl with a tag but no stored player (or a broken UUID) does not crash, it acts as unbound |  |  |

## H. Audit features and bug fixes, round 2 (2026-10-29; written without a compiler, NOT built or run - build, run the suites, then retest in game)

Section letter H because G is the round 1 section on this branch. Details and unverified points: [`docs/project/audit-fixes-2026-10-29.md`](docs/project/audit-fixes-2026-10-29.md) (round 2 section). All rows are **fixed / added, waiting for your retest**.

| # | Try | Result | Feedback |
|---|---|---|---|
| AG1 | Spectre Sapling: right click any vanilla sapling with Ectoplasm turns it into a Spectre Sapling and uses one Ectoplasm (creative keeps it) |  |  |
| AG2 | Runic Pattern: right click a placed Rune Base (Runic Dust) with a piece of paper gives a Runic Pattern holding its 4x4 pattern and uses one paper (the pattern replaces the paper if it was the last) |  |  |
| AG3 | Luminous Powder + an enchanted item (shapeless, 1 + 1) gives the item back with a yellow "Luminous" tooltip line and uses the powder. **The glow-in-the-dark glint itself is NOT implemented** (needs a glint coremod hook that forces full-bright lighting) |  |  |
| AG4 | Numbered Spectre Coil: found in dungeon (10), abandoned mineshaft (8) and end city (30) chests, named "Spectre Coil Nr. N", no recipe, pushes 128 FE per tick into the energy block it is attached to (no Spectre pool needed); `NumberedSpectreCoils = false` removes it from chests |  |  |
| AG5 | Blackout Powder (recipe: ink sac, coal block, lapis lazuli = 4): right click a block in a chunk lit by a Spectre Illuminator removes the orb, drops the Illuminator item next to the clicked block, relights the chunk and uses one powder (not in creative) |  |  |
| AG6 | Floo: teleporting to a fireplace whose chunk is not loaded does nothing and does not delete the fireplace from the network |  |  |
| AG7 | Floo Sign with a name that already exists (any case): action bar message, the sign is kept and no bricks convert |  |  |
| AG8 | Stable Ender Pearl: a bound player in another dimension is moved to the pearl's dimension and position (watch for dimension-change side effects: no portal may be created) |  |  |
| AG9 | Advanced Redstone Interface and Redstone Remote ignore Position Filters stored in another dimension (filters without a dimension count as the overworld) |  |  |
| AG10 | Redstone Observer: its output is right after reloading the world (no wait for the target to change) |  |  |
| AG11 | Analog Emitter: input is read from the opposite side like the original (power from a repeater or torch behind it) |  |  |
