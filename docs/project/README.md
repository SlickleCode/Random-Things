# Project knowledge (written on 1.14.4, extended on 1.16.5 and 1.18.2)

Reference material moved out of the old root `PORTING_PLAN.md` / `WIKI_FEATURE_STATUS.md` in the 2026-10-11 documentation refactor. Nothing was removed; the root files are now short.
Start with [`../../PORTING_PLAN.md`](../../PORTING_PLAN.md).

| File | Contents |
| --- | --- |
| [toolchain.md](toolchain.md) | repo, JDK/Gradle/Forge pins, mapped jar for `javap`, dev tooling summary |
| [scope-decisions.md](scope-decisions.md) | binding user decisions, deliberate divergences from 1.12.2, standing simplifications |
| [audit-fixes-2026-10-29.md](audit-fixes-2026-10-29.md) | bug fixes from the 2026-10-29 wiki audit mirrored to this branch, written without a compiler (unverified), with per-fix notes; round 2 section: missing features (Spectre Sapling, Runic Pattern, Numbered Spectre Coil, Blackout Powder) and more fixes (AG1-AG11, Luminous Powder blocked) |
| [cloud-session-limits-2026-10-29.md](cloud-session-limits-2026-10-29.md) | what the 2026-10-29 cloud session could not do (no compiler, jars or game), what is unproven and what to run first |
| [architecture-conventions.md](architecture-conventions.md) | how blocks/items/TEs/GUIs/network/config are organised in this port |
| [coremods.md](coremods.md) | the JS transformers, what each patches and why (Mixin is dead) |
| [api-gotchas-1.14.4.md](api-gotchas-1.14.4.md) | Forge 28.2.26 / 1.14.4 API surprises |
| [api-gotchas-1.16.5.md](api-gotchas-1.16.5.md) | Forge 36.2.39 / 1.16.5 behaviour changes that cost debugging time (culling/`notSolid`, glint render types, data-pack biomes, string dimensions, coremod names) |
| [api-gotchas-1.18.2.md](api-gotchas-1.18.2.md) | Forge 40.3.12 / 1.18.2: Mojang names, block-entity tickers, shaders, Holder biomes, placed features, datapack dimension type, frozen registries, jigsaw/generation test traps |
| [api-gotchas-1.20.6.md](api-gotchas-1.20.6.md) | NeoForge 20.6.141 / 1.20.6: Mojang names in production, StackTag/custom_data, ObjectHolder replacement, capabilities, payload networking, `useItemOn`/`useWithoutItem`, provider-taking (de)serialization, data pack formats, coremod fixes |
| [pitfalls.md](pitfalls.md) | generalized lessons that cost real time |
| [state-history-2026-10.md](state-history-2026-10.md) | detailed state and change-set history up to 2026-10-11 |
| [slice-history.md](slice-history.md) | one line per porting slice |
| [wiki-feature-status.md](wiki-feature-status.md) | full per-feature table and the "Not migrating" list |
| [old-plan-intro.md](old-plan-intro.md) | the old plan's intro and file map |
