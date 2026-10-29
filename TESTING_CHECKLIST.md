# Random Things 1.14.4 - Testing Checklist (open items only)

Only what still needs a person is listed. Everything an automated test checks is out: see [`docs/testing/covered-by-tests.md`](docs/testing/covered-by-tests.md). The full numbered sheet with your results from 2026-10-11 (227 rows passed) is in [`docs/testing/checklist-archive/`](docs/testing/checklist-archive/README.md); a 90 minute route with commands is [`docs/testing/playtest-route-1.14.4.md`](docs/testing/playtest-route-1.14.4.md); fix writeups are in [`docs/testing/fix-log/`](docs/testing/fix-log/README.md).

**World for parts A-C:** `RT_AUTOTEST=true RT_MANUAL=true ./gradlew runClient` ([`docs/testing/manual-test-world.md`](docs/testing/manual-test-world.md)): every block on show with a sign, set-ups and a stocked inventory. It is deleted when the game closes. Parts D-E need a normal survival world.

Fill in **Result** with `Pass` or `Fail` and a sentence in **Feedback**; blank = untested. Row numbers are the original sheet's. Several rows cover a group, written `327, 328`.

## A. Look around the museum (rendering and item models)

| # | Look at | Result | Feedback |
|---|---|---|---|
| 261 | Rain Shield: no rain or snow visible inside its radius (**fixed, waiting for your retest**) |  |  |
| 389, 149-153 | Biome Radar: the crystal spins in the middle of the block (no bobbing above it, also while searching) (**fixed, waiting for your retest**); antenna particles change colour when the search ends |  |  |
| M1 | Super Lubricent Stone / Ice / Platform / Filtered Platform: placing and breaking plays the ice (glass) sounds |  |  |
| M2 | Blazing Fire on netherrack looks like vanilla fire (it was already cutout here) |  |  |
| M5 | Time in a Bottle: every face of the accelerated block shows the spinning wheel of 1.12.2 (decagon rings, one more ring at time rate 2, 4, 8, 16 and 32), not the floating bottle icon - FIXED 2026-10-29 (`TimeWheel` + `TimeAcceleratorEntityRenderer`; render scenes `time_wheel` and `time_wheel_close` checked on all nine branches); the wheel is about a third of a block across, as in 1.12.2 |  |  |
| M6, M13 | Spectre Illuminator lights its chunk after placing and darkens it again after removing the orb; the museum sign (south edge) names the closest plains village with a Peace Candle church, and the museum is deleted when the game closes (not run in game on this branch) |  |  |
| M9, M10, M11, M12, M14 | Peace Candle item lit evenly; Spectre Block more opaque; Ender Mailbox item fits its slot and hand; Spirit texture a deep pure blue; Lotus and Bean Sprout render with proper transparency |  |  |
| M16 | Ender Bucket and Reinforced Ender Bucket draw their fluid in the opening (water blue, lava orange); also held in first person |  |  |
| 234 | Fluid Display: the poured fluid's texture and colour, still vs flowing |  |  |
| 254 | Magic Hood: no potion swirl around you in F5, other players see no nametag (two clients) |  |  |
| 277, 279 | Portkey: a dropped item shimmers, then stops glowing about 5 s later; a disguised Portkey looks like the donor item in hand, on the ground and in an item frame |  |  |
| 319 | Light Redirector: open faces show the block on the opposite side (periscope) |  |  |
| 321-323, 390 | Dyeing Machine: recolored items are tinted held, in the inventory, on the ground and worn; the glint result glows in the dye colour on items and worn armor |  |  |
| 346, 347 | Runes: noisy texture, same-colour pixels join across the base edge, no flicker while moving; `FlatRunes = true` gives the plain texture |  |  |
| 265 | Ancient Furnace: the area turns warm biome colours and the snow melts after the explosion (the heating logic is automated) |  |  |

## B. Machines and GUIs

| # | Try | Result | Feedback |
|---|---|---|---|
| 174 | Imbuing Station: arrow and per-slot fill animations only run while a recipe is in progress; the output slot rejects manual insertion |  |  |
| 211, 213 | Ender Mailbox: sneak-right-click with a letter for an unknown player gives "does not seem to reside in this world" and leaves the letter; another player's mailbox says you are not the owner and opens no GUI |  |  |
| 244 | Creative Player Interface is creative-menu only and behaves like the Player Interface |  |  |
| 309 | Spectre Coil (Genesis) generates 10,000,000 FE/tick into what it is attached to (creative tab only) |  |  |
| 348 | The recipe book shows and crafts the backfilled recipes (Item Collector, Igniter, Iron Dropper, detectors, Inventory Tester, Notification Interface, Redstone Observer/Tool, Advanced Redstone Repeater, Potion Vaporizer, Player Interface, Slime Cube, ...) |  |  |

## C. Mobs in survival

| # | Try | Result | Feedback |
|---|---|---|---|
| 325, 326 | Spirit: killing mobs sometimes spawns one (more at night under a full moon, more after the Ender Dragon); it drifts in the air and despawns on its own |  |  |
| 327, 328 | Spirit: ignores ordinary damage, dies in one hit from a Spectre Sword, which drops 1-2 Ectoplasm |  |  |

## D. Sound

| # | Listen for | Result | Feedback |
|---|---|---|---|
| 158 | Sound Dampener: the muted sound is silent near the block and audible again beyond 20 blocks |  |  |

## E. Worldgen, loot and config (survival world)

| # | Try | Result | Feedback |
|---|---|---|---|
| 329-333, 339 | Break blocks in survival: Batch 1 blocks drop themselves; Bean Sprout drops 3-4 beans when grown and 1 when young; Colored Grass drops Dirt (itself with Silk Touch); Compressed Slime Block drops a Slime Block; Blazing Fire, Floo Brick, Rune Base, Bean Stalks and Diaphanous Block drop nothing; Bean Pod drops iron 8-20, gold 4-15, diamonds 1-5, 0-2 emeralds, 4-8 beans and a Golden Egg |  |  |
| 334, 335 | Chests: Summoning Pendulum, Magic Hood, Slime Cube appear in dungeon / weaponsmith / stronghold / jungle-temple chests; a Biome Crystal in about 1 of 5 vanilla chests, already tuned to a biome |  |  |
| 336, 337 | First launch creates `config/randomthings-common.toml` with `[worldgen]`, `[loot]`, `[features]` booleans, all `true`; `MagneticEnchantment = false` hides Magnetic at the enchanting table and stops the pull; `ArtificialEndPortal = false` makes Evil Tear do nothing; `Lotus` / `AncientFurnace = false` stop new ones in fresh chunks |  |  |
| 345 | `[numbers]` `SpiritLifeTime`, `SpiritChanceNormal`, `SpiritChanceMoonMult`, `SpiritChanceEndIncrease` change Spirit despawn time and spawn odds (`SpiritChanceNormal = 1.0` spawns one on every kill) |  |  |
| 353 | `HideCoordinates = true`: Portkey tooltip only says `<Target set>` / `<No target set>`, Position Filter shows no coordinates and no "hold Shift" hint; `false` restores both |  |  |

**Old worlds (M18):** `docs/testing/fixture-world.md` lists what survives an upgrade; the 1.14.4 to 1.16.5 hop loses the Spectre cube because this branch never saved it before the 2026-10-28 fix.

## E. Audit bug fixes (2026-10-29; written without a compiler, NOT built or run - build, run the suites, then retest in game)

Details and unverified points: [`docs/project/audit-fixes-2026-10-29.md`](docs/project/audit-fixes-2026-10-29.md). All rows are **fixed, waiting for your retest**.

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

## F. Audit features and bug fixes, round 2 (2026-10-29; written without a compiler, NOT built or run - build, run the suites, then retest in game)

Details and unverified points: [`docs/project/audit-fixes-2026-10-29.md`](docs/project/audit-fixes-2026-10-29.md) (round 2 section). All rows are **fixed / added, waiting for your retest**.

| # | Try | Result | Feedback |
|---|---|---|---|
| AG1 | Spectre Sapling: right click any vanilla sapling with Ectoplasm turns it into a Spectre Sapling and uses one Ectoplasm (creative keeps it) |  |  |
| AG2 | Runic Pattern: right click a placed Rune Base (Runic Dust) with a piece of paper gives a Runic Pattern holding its 4x4 pattern and uses one paper |  |  |
| AG3 | Luminous Powder + an enchanted item (shapeless, 1 + 1) gives the item back with a yellow "Luminous" tooltip line and uses the powder. **The glow-in-the-dark glint itself is NOT implemented** (needs a glint coremod hook that forces full-bright lighting) |  |  |
| AG4 | Numbered Spectre Coil: found in dungeon (10), abandoned mineshaft (8) and end city (30) chests, named "Spectre Coil Nr. N", no recipe, pushes 128 FE per tick into the energy block it is attached to (no Spectre pool needed); `NumberedSpectreCoils = false` removes it from chests |  |  |
| AG5 | Blackout Powder (recipe: ink sac, coal block, lapis lazuli = 4): right click a block in a chunk lit by a Spectre Illuminator removes the orb, drops the Illuminator item, relights the chunk and uses one powder |  |  |
| AG6 | Floo: teleporting to a fireplace whose chunk is not loaded does nothing and does not delete the fireplace from the network |  |  |
| AG7 | Floo Sign with a name that already exists: action bar message, the sign is kept and no bricks convert |  |  |
| AG8 | Stable Ender Pearl: a bound player in another dimension is moved to the pearl's dimension and position (watch for dimension-change side effects) |  |  |
| AG9 | Advanced Redstone Interface and Redstone Remote ignore Position Filters stored in another dimension |  |  |
| AG10 | Redstone Observer: its output is right after reloading the world (no wait for the target to change) |  |  |
| AG11 | Analog Emitter: input is read from the opposite side like the original (power from a repeater or torch behind it) |  |  |
