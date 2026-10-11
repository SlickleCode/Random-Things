# Writing in-game tests

Tests live in `src/main/java/lumien/randomthings/debug/test/`:

| Class | Role |
| --- | --- |
| `RTTest` | A named test = list of steps, each optionally followed by a wait of N server ticks |
| `TestCtx` | What a step receives: world, scratch area, block/entity helpers, assertions |
| `RTTestRunner` | Tick-driven runner (`/rtdebug test`), report writer, cleanup between tests |
| `BehaviorTests` | Hand-written gameplay tests (add yours here) |
| `RoundTripTests` | Generated NBT/capability tests, one per registered tile entity / entity / item |

All of it is dev-only (see [isolation.md](isolation.md)); never reference these classes from game code.

## Anatomy of a behavior test

```java
tests.add(RTTest.named("behavior.igniter.toggle_mode_lights_fire_when_powered_and_removes_it_when_unpowered")
    .run(c -> {                                    // step 1: build + trigger
        c.floor();                                 // stone floor one block below the origin plane
        c.set(c.at(0, 1, 0), ModBlocks.IGNITER.getDefaultState().with(IgniterBlock.FACING, Direction.EAST));
        c.set(c.at(0, 2, 0), Blocks.REDSTONE_BLOCK);
    })
    .waitTicks(2)                                  // let the world tick
    .run(c -> {                                    // step 2: assert, then change something
        c.eq(Blocks.FIRE, c.world.getBlockState(c.at(1, 1, 0)).getBlock(), "block in front when powered");
        c.remove(c.at(0, 2, 0));
    })
    .waitTicks(2)
    .run(c -> c.check(c.world.isAirBlock(c.at(1, 1, 0)), "fire should be removed in TOGGLE mode")));
```

- A step passes if it returns normally and fails if it throws. The first throw ends the test; the
  message is recorded as `FAIL <name> (step N): <message>`.
- `c.at(x, y, z)` is relative to the test origin. Y = 0 is the floor plane (`c.floor()` fills it), Y = 1 is
  the first usable layer. The usable box is ±7 horizontally and −2…+7 vertically.
- Use `c.set(...)` (normal neighbor updates, flag 3) for anything that must *react* — redstone, placement
  hooks, neighbor-changed logic. Use `c.setQuiet(...)` for scaffolding that must not trigger reactions.
- The runner wipes the whole box (blocks, tile entities, loose entities) **before and after** every test,
  even failing ones. State you need across steps goes in the world, or in an effectively-final holder such
  as an `int[1]` captured by the lambdas (see the Iron Dropper repeat-mode test).
- Immediate tests (no waits) are batched up to 64 steps per server tick; delayed tests advance one step per
  wait. A test with `waitTicks(30)` costs 30 ticks regardless of how many tests run after it.

## TestCtx reference

| Member | Purpose |
| --- | --- |
| `world`, `origin`, `player` | The `ServerWorld`, the area origin, the player who started the run |
| `at(x,y,z)` | Position relative to origin |
| `set(pos, state\|block)` | Place with neighbor updates |
| `setQuiet(pos, state)` | Place without neighbor reactions |
| `remove(pos)` | Set air with neighbor updates (e.g. unpower a contraption) |
| `floor()` | Stone floor at Y=0 across the box |
| `spawnItem(x,y,z,stack)` | Spawn a stationary item entity (coordinates relative to origin) |
| `itemEntities()` | Live item entities in the box |
| `inventory(pos)` | The `IItemHandler` at a position (capability, or an `IInventory` wrapper) |
| `TestCtx.count(handler, item)` | Total count of an item across all slots |
| `tile(pos, Class)` | Typed tile entity or a descriptive failure |
| `check(cond, msg)` / `eq(expected, actual, what)` | Assertions |
| `throw new TestCtx.Skip(reason)` | Mark "not applicable" (reported separately from failures) |

## Choosing what to test

Prefer tests that **cross an API boundary the compiler cannot see**:

- redstone / neighbor-update driven logic (power on/off edges, pulse vs level behavior),
- anything that depends on a Forge hook, event or coremod actually firing (drop redirection, spawn control),
- capabilities between two blocks (collector → chest, dropper → chest, breaker → chest),
- machines that tick (give them enough ticks; tile entities throttle themselves),
- special crafting recipes (`matches`, `getCraftingResult`, `getRemainingItems` with edge cases).

Always include the **negative** case next to the positive one (powered vs unpowered, stackable vs
unstackable, already-tagged vs untagged) — a test that only proves "it can work" misses the hooks that
always work.

When a behavior test fails, first decide whether the code or your expectation is wrong. Check the real
original source (`git show origin/1.12.2:<path>`) before changing game behavior.

## Round-trip tests

`RoundTripTests` needs no per-feature work: new registered tile entity types, entity types and items are
picked up automatically. For a tile entity it places the first mod block whose type accepts it, fills every
item/fluid/energy capability on every side with test content, round-trips the NBT through a *fresh*
instance and compares NBT plus capability snapshots. If a feature stores state outside NBT/capabilities
(static maps, client-only fields), write a dedicated behavior test for it instead.

If a round-trip test fails with "NBT after write → read → write", the usual causes are:

1. a field that `write` emits but `read` does not restore (or restores differently, e.g. `null` vs EMPTY),
2. `read` that depends on world state not present on a fresh instance,
3. nondeterministic content (timestamps, random UUIDs generated on write).

## Adding a recipe-coverage exception

The resource audit fails when an item has no recipe. Genuinely uncraftable items (loot-only, creative-only,
worldgen, imbuing results, spawn eggs) go in `ResourceAudit.NO_RECIPE_EXPECTED`. Do this deliberately:
first confirm the original 1.12.2 mod also had no recipe (`ModRecipes.java`, `assets/randomthings/recipes`).

## Checklist when adding a feature

1. Add the blockstate/model/texture/lang/recipe — the audit will tell you if something is missing.
2. Run the suite with the filter for your feature, then once unfiltered.
3. If the feature has behavior with redstone, capabilities or a hook, add a behavior test.
4. Add the `TESTING_CHECKLIST.md` row as usual; the automated suite complements, not replaces, the
   playtest rows.
