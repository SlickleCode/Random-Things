# Minecraft mod migration knowledge base

Distilled from the Random Things 1.12.2 → Forge 1.14.4 port (≈ 568 source files in the original; about
two weeks of sessions, September–October 2026): what was hard, which mistakes cost the most time, which
techniques paid off, and how to approach the *next* migration (1.14.4 → a later version, or another mod)
without re-learning it.

It is written to outlive this particular Minecraft/Forge version. Every document separates:

- **Durable lessons** — true for any Minecraft version or loader (methods, failure shapes, decision rules).
- **Version-specific facts** — marked *(1.14.4 / Forge 28.2.26)* or collected in
  [03-version-notes-1.12.2-to-1.14.4.md](03-version-notes-1.12.2-to-1.14.4.md). Treat these as examples of
  the *kind* of thing that changes, and re-verify them for any other version.

## Read in this order

| # | Document | Use it for |
| --- | --- | --- |
| 01 | [Strategy and workflow](01-strategy-and-workflow.md) | How to scope, slice, order and verify a port; the working rules that held up |
| 02 | [Pitfall catalog](02-pitfall-catalog.md) | Symptom → cause → fix → "what to check in any version", by subsystem. The core of this knowledge base |
| 03 | [Version notes 1.12.2 → 1.14.4](03-version-notes-1.12.2-to-1.14.4.md) | Concrete API/behavior deltas found, as a worked example and starting point |
| 04 | [Coremods, Mixin and bytecode patching](04-coremods-and-bytecode-patching.md) | When no hook exists: how to decide, build, verify and debug transformers |
| 05 | [Verification lessons](05-verification-lessons.md) | What "done" means; ground-truthing; runtime evidence; the automated test setup |
| 06 | [Case studies](06-case-studies.md) | The long bug sagas, told end to end (they teach more than the one-line rules) |
| 07 | [Next-migration checklist](07-next-migration-checklist.md) | A step-by-step plan to start the next version bump |
| — | [Supported versions: catalog and per-version analysis](versions/README.md) | 22 `1.N` lines analysed (1.0 to 1.21, excluding 26.x); **in scope: 1.14.4 and up, Forge then NeoForge, branch per version, coremods kept** |
| — | [Open questions backlog](open-questions-backlog.md) | Decisions and follow-up tasks deferred to a later session |

Related, project-specific records that stay authoritative for *this* repo's state:
`PORTING_PLAN.md` (current state and doc map), `docs/project/` (conventions, coremod map, scope, per-feature
status), `TESTING_CHECKLIST.md` / `docs/testing/fix-log/` (open test rows and root-cause writeups),
`docs/testing/` (the automated test setup).

## The ten lessons that mattered most

1. **A clean build proves almost nothing.** The bugs that cost the most time compiled fine and only
   failed at runtime (registry scanning, lifecycle ordering, model baking, recipe parsing, coremod
   verification, dead hooks). Boot the real game early and often, and automate that boot
   ([05](05-verification-lessons.md)).
2. **Ground-truth every uncertain API against the real compiled artifact** (`javap -c`, the sources jar),
   not memory and not online guides. Guides for "1.14" described later builds; every confident recall that
   was not checked was eventually wrong somewhere.
3. **Read the original's real source before "fixing" anything that looks wrong.** A large share of
   suspected bugs were intentional original behavior (see-through blocks, always-lit lamps, items that
   glow forever until bound).
4. **An API that exists is not an API that works.** Events that are never posted
   (`HarvestDropsEvent`), helper methods with no code path for the case you need
   (`FluidUtil.tryPickUpFluid` for plain fluid blocks), default methods that are empty no-ops
   (`onDataPacket`), hooks that fire too late (`CheckSpawn`). Test the behavior, not the signature.
5. **Server and client are two different worlds, even in singleplayer.** Separate `World`, separate
   tile-entity and entity instances, separate tick events. Most "works after relog" bugs are this.
6. **Entities and tile entities need an explicit sync story.** Fields set by a secondary constructor, or
   mutated in place, never reach the client. Use synced data parameters / full update-tag + data-packet
   triplets and *test the live path without relogging*.
7. **Rendering state leaks between renderers.** Always reset GL state you depend on; check winding,
   culling, render layers, alpha channels, and per-face brightness. Most "black / invisible / dark" bugs are one
   of these.
8. **Hooks the new version lacks are a decision tree, not a dead end**: public API → existing event →
   subclass/override → shared choke-point coremod → data-driven replacement. Mixin was a trap in this
   environment; self-contained coremods worked ([04](04-coremods-and-bytecode-patching.md)).
9. **Don't iterate blindly on what you can't observe.** After two failed static-analysis fixes, add
   instrumentation or build a harness and get evidence. The third guess is usually wrong too.
10. **Keep the handoff document short and current; keep history elsewhere.** A 420 KB running log slowed every
    session; a ~300-line state file plus per-bug writeups did not.
