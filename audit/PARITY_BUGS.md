# Parity-bugs list (branches 1.16.5 .. 1.21.11 versus the 1.14.4 baseline)

For you to fix elsewhere. NOTHING here is documented on the wiki (the wiki has no version differences). Full detail with file:line on both sides:
`audit/verify/P1.md` (redstone/machines), `P2.md` (blocks/world/config), `P3.md` (spectre/tools), `P4.md` (entities/ender/sound),
`PARITY_MECH.md` (mechanical diff). CONFIRMED = both sides read; SUSPECTED = code differs, effect inferred (no jars available to the agents).

## Mechanical checks (all 8 branches)
Identical on every branch: registered item/block lang keys, the 20 config keys and defaults, all 222 recipe files (after normalising tag names and the
1.21 ingredient format) EXCEPT none (a first run flagged the Super Lubricent Tincture, see Correction). Classes missing vs 1.14.4 on every branch: MathUtils, SpectreDimension (replaced by SpectreDimensionType /
other), SpiritSpawnEggItem (replaced by the loader's spawn egg class).

## Recipe
- Correction (false positive): the Super Lubricent Tincture recipe is NOT different. 1.14.4 already requires a Water Bottle (`forge:nbt` ingredient with
  `Potion:minecraft:water`); the comparison script reduced NBT ingredients to the bare item name, so 1.14.4 looked like "any potion". The wiki shows "Water Bottle".

## CONFIRMED differences
1. Magnetic enchantment config (1.21.1, 1.21.4, 1.21.11): `MagneticEnchantment=false` only disables the effect; the enchantment stays in enchanting
   tables, trades and loot (data-driven tags). On 1.14.4..1.20.6 the option also closes table/book/loot. (P2 D3, P3 C1)
2. Trigger Glass (1.16.5+): sand and gravel no longer fall through an open Trigger Glass (the `getMaterial` override was dropped; only pathing re-added). (P1 D1)
3. Rain Shield (1.16.5+ patch rain particles/sounds hooks; 1.21.4+ patch precipitation lookup); 1.14.4 patches only the renderer. See "conflicts" below. (P1 D2)
4. Weather cloud ceiling: Y=256 on 1.14.4/1.16.5, build height (320) on 1.18.2+, so weather changes take longer there. (P4 A1)
5. Bean stalk / Bean Sprout height limits: pod at Y=253 on 1.14.4/1.16.5, near the new build height on 1.18.2+. Bean Sprout biome set differs per branch. (P4 A2, A5)
6. Portkey landing search: Y floor and "solid block" test changed in 1.16.5 and again in 1.18.2. (P4 A3)
7. Super Lubricent server-side boat cap skips chest boats on 1.19.2, 1.20.1, 1.20.6, 1.21.1 (1.21.4+ correct). (P2 D5)
8. Spirit spawn egg (1.16.5+): the custom egg class is replaced by the loader/vanilla egg; spawner/dispenser/position behaviour differs. From 1.20.1 a Spirit
   vanishes with `discard()` and `/kill` no longer works on it. (P3 C2, C4)
9. Blazing Fire tick delay: 1.14.4 15 plus vanilla random; 1.16.5+ flat 15 (effect suspected: faster fire). (P3 C3)
10. Ancient Furnace biome conversion list: 13 entries on 1.14.4/1.16.5, a different 12 on 1.18.2+ (adds deep frozen ocean, snowy slopes, grove). (P2 D6)
11. Imbue on-hit: filter changes at 1.20.1; different damage event from 1.21.1 (blocked/invulnerable hits can still apply effects). (P1 D3/D3b, P4 A6)
12. Fertilized Dirt: growth boost applies to all IPlantable up to 1.20.6, only BushBlock from 1.21.1 (sugar cane, cactus lose it). (P2 D1)

## SUSPECTED differences
- Artificial End Portal on 1.21.1/1.21.4/1.21.11: End teleport rewritten (arrival position / obsidian platform). (P4 A4)
- Spectre dimension: vanilla void start platform no longer suppressed on 1.16.5+; return trip handling may differ on the baseline. (P3 S1, S2)
- Spectre log/plank not in mineable/axe tag on 1.18.2+; no mineable/pickaxe tag; hand-mined ROCK blocks (Injector, Coils) drop on 1.16.5+ but not on 1.14.4. (P3 S3, S4, P4 A8, P2 D9)
- Rune Dust / Pattern / Base support check looser on 1.16.5+. Escape Rope landing-block test on 1.18.2+. (P3 S5, S6)
- Magnetic tool set widens (hoe from 1.16.5, `#minecraft:enchantable/mining` on 1.21.x). (P3 S7)
- Spirit: moon bonus in the End on 1.21.11; dragon breath no longer hurts Spirits on 1.20.1+. Sapling ground check wider on 1.21.1+. (P3 S8-S10)
- Fertilized Dirt hoe tilling path (UseHoeEvent vs BlockToolModificationEvent from 1.19.2); Block of Sticks dead overrides from 1.16.5. (P2 D2, D4)
- Advanced Redstone Repeater/Torch pending-tick test (1.18.2+); Dyeing Machine remove-dye slot (1.20.6+); Biome Radar 3D biome sampling (1.18.2+); Block Destabilizer timing;
  Player Interface armor slots (1.21.1+); Potion Vaporizer fuel lookup (1.21.4+). (P1 D4-D9)
- Golden Chicken gold-ore tag depends on the loader version. (P4 A7)
- Not checkable by reading: 1.21.11 break-drop order, 1.21.11 inventory adapter slot limit, coremod redstone hooks against real bytecode. (P1)
