# Supporting every major Minecraft version — catalog and analysis

**Goal (2026-10-08):** the mod should eventually support every major Minecraft version except the new `26.x` line
(which is not finished: 26.1, 26.2 and 26.3 are out and 26.4 is in development ✔).
This folder catalogs those versions and analyses each one: ecosystem, what changes for this mod, where the source would
come from, and an effort/risk/verdict. It is a planning document, not a commitment.

## Scope decision (owner, 2026-10-08)

- **Lower bound: 1.14.4.** Only `1.14.4` and newer are supported; everything earlier (1.0 to 1.13.2, including the
  1.12.2 reference) is **out of scope as a target**. 1.12.2 stays as the *source* of the port only. The old-version
  analysis files are kept for reference and marked out of scope.
- **Loaders: Forge first, then NeoForge** (Forge for 1.14.4 to 1.20.1; NeoForge from 1.20.2, where Forge also
  continues). **Fabric is a separate, later sweep** and is not planned here.
- **Parity target: only what is valid in 1.14.4.** Later ports carry the feature set of this 1.14.4 port, not
  1.12.2 features that were never ported here.
- **Repository layout: one branch per Minecraft version** (`1.14.4`, `1.16.5`, ...).
- **Skipped for now: 1.15.2 and 1.17.1** (backlog Q11); the path is 1.14.4 -> 1.16.5 -> 1.18.2 -> 1.19.4 -> 1.20.1/1.20.6 -> 1.21.1/1.21.11.
- **Patching: stay with coremods** (owner: the mod's behavior is too specific for hook-only approaches). Risk: see
  cross-cutting finding 2.
- **In scope: 6 lines (8 minus the two skipped)** - 1.14.4 (done, playtesting), 1.16.5, 1.18.2, 1.19.4, 1.20.6 (plus 1.20.1) and
  1.21.11 (plus 1.21.1).

## Definitions

- **Major version** = each `1.N` release line, pinned to its **final minor release** (the version a mod would target):
  e.g. `1.12.2`, `1.20.6`, `1.21.11`. Mojang's year-based numbering began with `26.1` (released 2026-03-24 ✔); `1.21.11`
  (2025-12-09 ✔) is the last `1.x` version ✔. `26.x` is excluded because the line is still moving (26.4 in development ✔).
- ✔ marks facts **verified on 2026-10-08** from official metadata (Mojang version manifest, Forge/NeoForge maven
  metadata, Fabric meta API, upstream GitHub branches, Fabric/NeoForge porting pages). **Unmarked statements are
  general modding knowledge that has not been verified**; re-check each before starting real work on that version.
- Effort sizes are **judgment calls** relative to the 1.12.2 → 1.14.4 port (= 1.0×). They are for ordering work, not for
  scheduling.

## Catalog (22 lines analysed; **6 in scope, 1.15 and 1.17 skipped for now**, rows marked OUT are out of scope)

| Line | Final | Final released | Java | Latest Forge for final | Tier | Effort |
| --- | --- | --- | --- | --- | --- | --- |
| [1.0](mc-1.0.md) **OUT** | 1.0 | 2011-11-17 ✔ | 6 | none ✔ | D | N/A |
| [1.1](mc-1.1.md) **OUT** | 1.1 | 2012-01-11 ✔ | 6 | 1.3.4.29 ✔ | D | N/A |
| [1.2](mc-1.2.md) **OUT** | 1.2.5 | 2012-03-29 ✔ | 6 | 3.4.9.171 ✔ | D | XL for a small subset |
| [1.3](mc-1.3.md) **OUT** | 1.3.2 | 2012-08-15 ✔ | 6 | 4.3.5.318 ✔ | D | XL for a subset |
| [1.4](mc-1.4.md) **OUT** | 1.4.7 | 2012-12-27 ✔ | 6 | 6.6.2.534 ✔ | D | XL for a subset |
| [1.5](mc-1.5.md) **OUT** | 1.5.2 | 2013-04-25 ✔ | 6 | 7.8.1.738 ✔ | D | XL for a subset |
| [1.6](mc-1.6.md) **OUT** | 1.6.4 | 2013-09-19 ✔ | 6 | 9.11.1.1345 ✔ | D | XL for a subset |
| [1.7](mc-1.7.md) **OUT** | 1.7.10 | 2014-05-14 ✔ | 6/7 (build with JDK 8, target 7) | 10.13.4.1614 ✔ | C | XL |
| [1.8](mc-1.8.md) **OUT** | 1.8.9 | 2015-12-03 ✔ | 8 | 11.15.1.2318 ✔ | B | L |
| [1.9](mc-1.9.md) **OUT** | 1.9.4 | 2016-05-10 ✔ | 8 | 12.17.0.2317 ✔ | B | M |
| [1.10](mc-1.10.md) **OUT** | 1.10.2 | 2016-06-23 ✔ | 8 | 12.18.3.2511 ✔ | B | S |
| [1.11](mc-1.11.md) **OUT** | 1.11.2 | 2016-12-21 ✔ | 8 | 13.20.1.2588 ✔ | B | S |
| [1.12](mc-1.12.md) **OUT** | 1.12.2 | 2017-09-18 ✔ | 8 | 14.23.5.2864 ✔ | A | — |
| [1.13](mc-1.13.md) **OUT** | 1.13.2 | 2018-10-22 ✔ | 8 | 25.0.223 ✔ | C | M–L |
| [1.14](mc-1.14.md) | 1.14.4 | 2019-07-19 ✔ | 8 | 28.2.30 ✔ | A | — |
| [1.15](mc-1.15.md) | 1.15.2 | 2020-01-17 ✔ | 8 | 31.2.62 ✔ | B | M |
| [1.16](mc-1.16.md) | 1.16.5 | 2021-01-14 ✔ | 8 (11 optional) | 36.2.42 ✔ | A | L |
| [1.17](mc-1.17.md) | 1.17.1 | 2021-07-06 ✔ | 16 | 37.1.1 ✔ | C | L |
| [1.18](mc-1.18.md) | 1.18.2 | 2022-02-28 ✔ | 17 | 40.3.12 ✔ | B | M–L |
| [1.19](mc-1.19.md) | 1.19.4 | 2023-03-14 ✔ | 17 | 45.4.5 ✔ | B | M |
| [1.20](mc-1.20.md) | 1.20.6 | 2024-04-29 ✔ | 17 for 1.20–1.20.4; **21 for 1.20.5+** | 50.2.10 ✔ | A | 1.20.1: M; 1.20.6: XL |
| [1.21](mc-1.21.md) | 1.21.11 | 2025-12-09 ✔ | 21 | 61.2.1 ✔ | A | 1.21.1: L; 1.21.11: XL |

(Click a line for its full analysis.) Fabric supports Minecraft 1.14 and newer ✔; NeoForge: 1.20.1 builds exist in a
separate artifact ✔ and its main artifact starts at 1.20.2 ✔ (21.11.45 for 1.21.11 ✔). Forge has builds from 1.1 through
26.3 ✔ (but note: it has no 1.0 build ✔).

### Tiers (old ones kept for reference; in-scope lines are 1.14 to 1.21)

- **A — primary targets (large player base, loader support, reasonable tooling)**
- **B — feasible, secondary demand**
- **C — feasible but low value / short-lived or very expensive**
- **D — not feasible or not recommended (pre-1.7)**

Tier is a recommendation, not a fact: **A** = 1.12.2 (reference), 1.14.4 (done), 1.16.5, 1.20.1, 1.20.6, 1.21.x;
**B** = 1.8.9–1.11.2 (exist or are cheap from 1.12.2), 1.15.2, 1.18.2, 1.19.x; **C** = 1.7.10, 1.13.2, 1.17.1;
**D** = 1.0–1.6.4.

## Where the source comes from

- **Older than 1.12.2 (1.8.9–1.11.2):** upstream branches `1.8.9`, `1.9`, `1.9.4`, `1.10.2`, `1.11.2` already exist ✔; per the
  project page the author only back-ports important bug fixes to older versions (no new features), so these branches are
  feature-limited versus 1.12.2. Treat them as *reference + base*, not as equivalents of this port.
- **1.7.10:** no upstream branch ✔; older releases of the mod existed for it. Source must be found elsewhere (verify).
- **1.12.2:** the reference source for this port.
- **1.14.4 and newer:** forward ports of this repo, one line at a time, since each step changes APIs incrementally
  (see the effort column).
- A NeoForge port called "Random Things Returns" is mentioned by a mod-index listing as functionally complete against
  1.7/1.12 (search result, **not verified**; page could not be fetched). If it exists and is MIT-licensed it is worth
  studying as prior art for the 1.21.x target.

## Cross-cutting findings

1. **Four hard "cliffs"** dominate the cost, in order:
   1. **1.12.2 → 1.13/1.14:** flattening (metadata removed) — done in this repo.
   2. **1.16 → 1.17:** Mojang names + ticker-based block entities + Java 16 (mechanical but touches every file).
   3. **1.20.4 → 1.20.5/1.20.6:** item NBT replaced by data components, codec/stream-codec serialization, Java 21.
      This hits the many items that store data on the stack (Portkey, Spectre Anchor, Ender Letter, Redstone Remote...).
   4. **1.21.x:** data-driven enchantments, item model definitions, `ValueInput/ValueOutput`, new render pipelines,
      Transfer API (NeoForge) — a continuous stream of breaking changes within a single "version".
2. **Patching strategy (owner decision: keep coremods).** The 14 JS coremods work on Forge 28.2.26 but the JS coremod API is
   removed in the newest forgespi (8.0.0 ✔). Verified 2026-10-08 from installer manifests: JS coremods ship through Forge 1.21.10 and NeoForge ~1.21.5; **1.21.11 has none** (see Q12 in the open-questions backlog). **Verify first, per version,
   whether JS coremods still load** (which Minecraft/Forge/NeoForge line drops them is unconfirmed). If a line has no
   coremod mechanism, the fallback is Java `ITransformer` classes through the loader's transformation service (still
   coremod-style bytecode patching, no Mixin) - decide that only when a spike proves JS is gone. This is the biggest
   *design* risk for every version ≥ 1.17. Bytecode targets also change with Mojang names from 1.17. See [04-coremods](../04-coremods-and-bytecode-patching.md).
3. **Loaders:** Forge for ≤ 1.20.1; NeoForge for ≥ 1.20.2 (Forge also continues ✔); Fabric optional from 1.14 ✔
   but would need a separate loader layer (Fabric API replaces capabilities, registries, events).
4. **Mappings:** MCP up to the 1.16 era; Mojang official names from 1.17 in Forge/NeoForge; Fabric Yarn up to 1.21.11.
   `26.1` is unobfuscated and needs Java 25 ✔ (out of scope here, but it fixes the long-term direction: official names).
5. **The testing infrastructure ports with the code** (`docs/testing/`): resource audit and round-trip tests are
   version-independent in design; their API calls will change per version, but they are the fastest way to see what broke.

## Recommended strategy (proposal for discussion)

1. **Finish and playtest 1.14.4** (large DONE-UNTESTED backlog). Every forward port inherits its bugs.
2. **Repository layout: branch per Minecraft version (decided).** Keep shared fixes consistent by porting bug fixes
   forward/back deliberately (cherry-pick) and recording which branch has which fix in the status tracker.
3. **Group versions into families** that share one code base where adjacent APIs are close:
   - In scope: F3: 1.14.4-1.16.5 - F4: 1.17.1-1.20.4 - F5: 1.20.5-1.21.11. (F1/F2 below 1.14 are out of scope.)
4. **Port order:** 1.16.5 (directly from 1.14.4) → 1.18.2 → 1.20.1 → 1.20.6 → 1.21.1 → 1.21.11 (Forge through 1.20.1, then NeoForge; 1.17.1 only if needed as a stepping stone).
5. **Per version:** follow [07-next-migration-checklist](../07-next-migration-checklist.md); re-verify every ✔-less claim in
   that version's analysis file first; create `docs/migration/03b-version-notes-<from>-to-<to>.md` as work proceeds.

## Decisions needed from the owner (also in the open-questions backlog)

- ~~Lower bound~~ **Decided 2026-10-08: 1.14.4 and up.**
- ~~Loader scope~~ **Decided 2026-10-08: Forge, then NeoForge; Fabric is a separate later sweep.**
- ~~Feature parity~~ **Decided 2026-10-08: only what was valid in 1.14.4.**
- ~~Repository layout~~ **Decided 2026-10-08: branch per version.**
- ~~Short-lived versions~~ **Decided 2026-10-08: skip 1.15.2 and 1.17.1 for now** (backlog Q11).
- ~~Coremod vs Mixin~~ **Decided 2026-10-08: stay with coremods** (see cross-cutting finding 2 for the verify-first caveat).

## Status tracker

| Line | Status |
| --- | --- |
| 1.0 | OUT of scope (below 1.14.4) |
| 1.1 | OUT of scope (below 1.14.4) |
| 1.2 | OUT of scope (below 1.14.4) |
| 1.3 | OUT of scope (below 1.14.4) |
| 1.4 | OUT of scope (below 1.14.4) |
| 1.5 | OUT of scope (below 1.14.4) |
| 1.6 | OUT of scope (below 1.14.4) |
| 1.7 | OUT of scope (below 1.14.4) |
| 1.8 | OUT of scope (below 1.14.4) |
| 1.9 | OUT of scope (below 1.14.4) |
| 1.10 | OUT of scope (below 1.14.4) |
| 1.11 | OUT of scope (below 1.14.4) |
| 1.12 | OUT of scope (below 1.14.4) |
| 1.13 | OUT of scope (below 1.14.4) |
| 1.14 | IN PROGRESS — this repo (playtesting backlog) |
| 1.15 | Skipped for now (backlog Q11) |
| 1.16 | Not started |
| 1.17 | Skipped for now (backlog Q11) |
| 1.18 | Not started |
| 1.19 | Not started |
| 1.20 | Not started |
| 1.21 | Not started |
