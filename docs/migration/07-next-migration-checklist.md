# 07 — Next-migration checklist

A concrete plan for the next version bump (e.g. this port → a newer Minecraft/Forge, or another mod from 1.12.2).
Copy it into the new project's state file and tick items off. Items are ordered; later ones assume earlier ones.

## Phase 0 — Set up (day 1)

- [ ] Branch strategy: keep the old code readable (`git show origin/<old>:<path>`); create the new working branch.
- [ ] Pin the toolchain (JDK, Gradle, loader plugin) in an activation script; document the ceiling versions.
- [ ] Choose the newest *stable* loader build for the target version; note it and why.
- [ ] Make the mapped jar **and sources jar** available; write the exact `javap` command in the state file.
- [ ] Get a skeleton that boots to the main menu and into a world via `runClient`.
- [ ] Write the **state file** (≤ 300 lines: state, decisions, conventions, gotchas, coremod map) and the
      **per-feature status table**; agree on the status vocabulary.
- [ ] Decide scope with the owner: authoritative feature list (wiki/docs), what is out of scope, which third-party
      compat is dropped. Record the decisions as binding.

## Phase 1 — Build the safety net *before* the bulk of the port

- [ ] Port the dev switch and test scaffolding from this repo (`DevMode`, `DevTools`, `RTTestRunner`,
      `ResourceAudit`, `AutoTest`) — see `docs/testing/`. Keep it dev-only (`isolation.md`).
- [ ] Hands-free boot command that opens a flat world and writes reports.
- [ ] The audit and round-trip tests need almost no per-feature work, so they pay back immediately.
- [ ] Decide how to read the old mod's data files at scale (script to list recipes, loot, blockstates, lang).

## Phase 2 — Inventory the old mod

- [ ] Features list from the authoritative source → status table (all NOT STARTED).
- [ ] The old mod's engine patches (ASM/coremod/mixin): list each, its target, the features it serves, and
      classify with the decision tree in [04](04-coremods-and-bytecode-patching.md). **Read the hook
      implementation bodies, not only the dispatcher.**
- [ ] Third-party dependencies and removed APIs (metadata, ore dict, old GUI handler, old networking).
- [ ] Diff vanilla assets between versions for every vanilla texture/model the mod references.
- [ ] Per target version: list APIs that disappeared/renamed ([03](03-version-notes-1.12.2-to-1.14.4.md) as a seed).

## Phase 3 — Port in batches (loop per slice)

Order: standalone blocks → tile-entity blocks (+GUI/network) → items → deferred subsystems → hook-dependent features.

Per slice:

- [ ] Read original class + data files (`git show`).
- [ ] `javap`/sources-check each uncertain API; check **who posts** every event you rely on.
- [ ] Port with the new version's idioms; extend vanilla classes where possible.
- [ ] Disclose simplifications in the javadoc and the checklist.
- [ ] Run the automated suite filtered to the feature; add a behavior test if it has redstone/capability/hook logic.
- [ ] Boot the client if you added registry entries, capabilities, coremods or models.
- [ ] Update checklist + state file in the same pass; leave uncommitted until asked.

Always remember (high-frequency bugs): `@ObjectHolder`-style statics; item-for-block registration; render
layers; TE sync trio; entity data parameters; client vs server tick events; GL state in renderers; text-field
sync; equal-width shaped recipe rows; array-valued model `display` fields; `super` in removal hooks.

## Phase 4 — Backfill and audit

- [ ] Loot tables for every block/entity; chest-loot injection.
- [ ] Recipes (compare count with the old mod's recipe files; the audit's allowlist records intentional gaps).
- [ ] Lang keys; tooltips; creative tabs.
- [ ] Config toggles (read at use time).
- [ ] GUI audit of all GUIs (screenshots) and a layout review against the original textures.
- [ ] Re-run the full automated suite; fix everything; keep the report in the PR.

## Phase 5 — Hook-dependent features

- [ ] Prove the patching pipeline with a no-op transform in dev client **and** shipped jar.
- [ ] One shared hook per choke point; offline-verify bytecode; boot-verify; behavior test.
- [ ] Document each coremod in the state file (target, purpose, how verified).

## Phase 6 — Human playtest

- [ ] Numbered checklist; fresh builds; evidence-first bug reports; `FIXED` until the owner confirms.
- [ ] Triage *blank* rows as risk (the Magnetic Enchantment dead hook sat in a blank row for over a week).
- [ ] Regularly prune stale statuses and condense logs (target: state file under ~300 lines).

## Phase 7 — Release hygiene

- [ ] Build the production jar; boot it in a normal launcher install (dev switch **off**) and confirm no
      `[RT-DEBUG]` output and no dev-tool registration.
- [ ] Decide whether to strip dev classes from the release jar (open question).
- [ ] Update `docs/migration/` with whatever this migration taught: new pitfalls, corrected claims, new version notes.

## Questions to put to the owner at the start

(Also in [open-questions-backlog.md](open-questions-backlog.md), which lists the ones still unanswered.)

1. Exact target version and loader; is a multi-loader build wanted?
2. Scope source and the list of features to cut.
3. Fidelity vs modernization: match the original exactly, or take the new version's idioms where they differ?
4. Who playtests, how often, and with what environment (launcher install vs dev client)?
5. Commit/push policy (here: only when explicitly asked).
