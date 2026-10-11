# Open questions and deferred tasks

Questions that came up while building the testing setup and this knowledge base and that need the owner's
answer (or a later session's time). Each entry has the **question**, **why it matters**, a **default being
used meanwhile**, and the **task** that follows from the answer. Close an entry by editing it (date + decision)
and moving it to "Decided".

Last updated: 2026-10-08.

## Open

### Q10. Multi-version support: scope and strategy (asked 2026-10-08; DECIDED, see below)
- Moved to "Decided". Remaining follow-ups are Q11 and Q12.

### Q11. Skipped lines: 1.15.2 and 1.17.1 (assessed 2026-10-10; skip stands)
- Skipped; port 1.14.4 -> 1.16.5 -> 1.18.2 directly. **Assessment of whether the jumps are too large (estimate from the version notes, not yet measured):**
  - **1.14.4 -> 1.16.5 (est. 0.8-0.9x of the first port):** the work 1.15.2 would isolate (rendering moves to `MatrixStack` /
    `IRenderTypeBuffer`, 0.3x) is renderer signatures only and is the same code in 1.15 and 1.16, so it would not be redone. What
    1.15.2 would not remove is the 1.16 data-driven dimension/worldgen work (Spectre, biome features, Peace Candle pools), which
    is the real cost. A 1.15.2 stop only adds a loader build, a test cycle and Forge 31 differences for no saved work.
  - **1.16.5 -> 1.18.2 (est. 1.0-1.2x):** 1.17.1 would isolate the Mojang-names rename + block-entity tickers + Java 16, but those
    changes are needed for 1.18.2 anyway and the rename is mechanical; 1.18.2 adds the world-height and worldgen rewrite on top.
- **Recommendation:** keep both skipped, but run the big jumps as ordered milestones on the one branch, each with a green test run:
  1.16.5 = (1) rendering signatures, (2) `TileEntity.read` / `ActionResultType`, (3) data-driven dimension + worldgen;
  1.18.2 = (1) Mojang-name rename script + tickers, (2) height/worldgen. If a milestone's tests will not go green, cut a 1.15.2 or
  1.17.1 branch at the previous green milestone.
- **Revisit if** a milestone proves unmanageable, or users ask for those versions. Follow `versions/mc-1.15.md` / `mc-1.17.md`.

### Q12. Do JS coremods still load on each newer line? (PARTLY ANSWERED 2026-10-08)
- Owner decision is to keep coremods. Mojang names from 1.17 also change every transformer target.
**Findings (2026-10-08, from the official installer `version.json` of each build; library presence, not yet a runtime boot):**

| Line | Loader build checked | JS coremod library | JS engine |
| --- | --- | --- | --- |
| 1.14.4 | Forge 28.2.26 | in use today (works, verified by tests) | not checked |
| 1.16.5 | Forge 36.2.42 | `coremods` 4.0.6 | not checked |
| 1.18.2 | Forge 40.3.12 | `coremods` 5.2.5 | not checked |
| 1.19.4 | Forge 45.4.5 | `coremods` 5.2.4 | not checked |
| 1.20.1 | Forge 47.4.26 | `coremods` 5.2.4 | `nashorn-core` 15.4 |
| 1.20.6 | Forge 50.2.10 | `coremods` 5.2.6 | not checked |
| 1.21.1 to 1.21.10 | Forge 52.1.14 to 60.1.15 (all checked) | `coremods` 5.2.6 | `nashorn-core` 15.4 |
| **1.21.11** | Forge 61.2.1 | **only `coremods-api` 5.3.1** (an `ASMAPI` helper class, no JS loader) | **none** |
| 1.20.2 to 1.21.5 | NeoForge 20.2.93 to 21.5.98 (all checked) | `net.neoforged:coremods` 6.0.2 / 7.0.3 | `nashorn-core` 15.4 (21.5) |
| **1.21.8 and newer** | NeoForge 21.8.54, 21.10.64, 21.11.45 | **none** | **none** |

**Runtime boot results (2026-10-10, DONE).** A data-only stub mod (a tiny `@Mod` class, `META-INF/coremods.json`, a JS transformer
on `net.minecraft.server.MinecraftServer` that prints a marker) was dropped into a freshly installed dedicated server of each
loader and booted:

| Loader | JDK | Coremod initialized + applied | Server reached "Done" |
| --- | --- | --- | --- |
| Forge 1.16.5 (36.2.42) | 8 | yes | yes |
| Forge 1.18.2 (40.3.12) | 17 | yes | yes |
| Forge 1.19.4 (45.4.5) | 17 | yes | yes |
| Forge 1.20.1 (47.4.26) | 17 | yes | yes |
| Forge 1.20.6 (50.2.10) | 21 | yes | yes |
| Forge 1.21.1 (52.1.14) | 21 | yes | yes |
| Forge 1.21.10 (60.1.15) | 21 | yes | yes |
| **Forge 1.21.11 (61.2.1)** | 21 | **no (silently ignored)** | yes |
| NeoForge 20.2.93 | 17 | yes | yes |
| NeoForge 20.4.251 | 17 | yes | yes |
| NeoForge 21.1.259 | 21 | yes | yes |
| NeoForge 21.5.98 | 21 | yes | yes |
| **NeoForge 21.8.54** | 21 | **no (silently ignored)** | yes |
| **NeoForge 21.11.45** | 21 | **no (silently ignored)** | yes |

Findings:
- JS coremods work at runtime on Forge 1.16.5 through 1.21.10 and NeoForge 20.2 through 21.5. On Forge 1.21.11 and NeoForge 21.8+
  the mod loads but its `coremods.json` is ignored with no warning, so a missing coremod shows up only as missing behavior.
  **The NeoForge cutoff is between 21.5.98 and 21.8.54** (21.6/21.7 not tested).
- Forge 1.16.5 has no `lowcodefml` loader (a coremod-only jar needs a `javafml` mod class); the other lines also worked with a class.
- Since 1.18.2 the JS sandbox blocks `Java.type('java.lang.System')` (`ClassNotFoundException`). Transformer scripts may only use
  the ASM/coremod API classes; use `print(...)` for diagnostics. Check the existing `transformer/*.js` for stray `Java.type` uses at port time.
- Not covered: Mojang-name target classes (1.17+) in our real transformers, and a client boot; the stub only proves loading and
  applying on the server side. The per-line port still needs its own coremod test (the existing `CoremodTests` do that).
- **Plan:** keep JS coremods through Forge 1.21.10 / NeoForge 21.5. At 1.21.11 (Forge) or NeoForge 21.8+ a different mechanism is
  needed (Java-based transformer service, or Mixin); spike it when that branch starts.

### Q13. Do the coremods work in a reobfuscated production jar? (found 2026-10-11, open)
- All JS transformers match **dev (MCP) method names** (`renderModel`, `getBiome`, ...). A production Forge runs SRG names (`func_...`), so the same scripts would silently not apply.
  Nothing in either branch has verified this. Fix pattern: match SRG ids via `ASMAPI.mapMethod("func_...")` (maps to the right name in both environments); the SRG id of each
  method comes from the MCP `methods.csv`. Check with a real jar in a launcher install before any release.

### Q14. Suppress the "experimental settings" prompt when reopening a world with the Spectre dimension (open)
- 1.16.5 marks any world with a non-vanilla dimension as experimental on load. Options: accept (other dimension mods do), or coremod `ServerWorldInfo.getLifecycle` / the `Lifecycle.stable()` comparison in `Minecraft.loadWorld`.

### Q3. Next migration target - DECIDED 2026-10-08
- The next target is **1.16.5 on Forge**, taken directly from 1.14.4 (1.15.2 skipped, Q11); see `versions/README.md`.
  Start it only after the 1.14.4 playtest backlog is cleared, unless the owner says otherwise.

### Q4. Cover MinecraftSavePorter (world-save upgrading)? - DECIDED 2026-10-08: no, skip it.

### Q5. Finish the transcript review - DONE 2026-10-08
- Read to the first message: "Continue with the plan (fork)" (6,235), "Continue with the plan" (1,344, largely
  duplicates the fork), "Minecraft mod 1.12.2 to 1.13 upgrade" (3,359, transcript begins mid-Batch 2), "Worldgen
  implementation" (3,976), "Fluid Display TileEntityRenderer" (3,758). Together with the earlier reads this covers all
  Random Things sessions except the sibling world-save project (Q4, skipped by decision).
- Limits: the transcripts show assistant prose and tool names only (tool inputs/outputs are not included), so details
  that lived only in commands or file contents are not recoverable; the repo's own docs are the authority for those.
- Output: pitfall catalog K14-K23 and L1-L19; Mixin timeline details in `04`. Most hazards found were already in the
  catalog, which is a good sign about the earlier review's coverage.

### Q6. Should the automated tests gate anything? - DECIDED 2026-10-08: manual only.
- Run the client suite (`docs/testing/`), the server suite and `./gradlew build` by hand before commits and releases.
  Revisit when a CI runner with a display exists.

### Q7. Verify Magnetic Enchantment and the Redstone Interface fix in-game with a real player
- **Why:** both fixes are proven by automated tests (fake player / contraption) but not playtested. #288 (Magnetic) was
  never filled in; the Interface fix touches rows #283-285. Also check silk touch, fortune and tool-required blocks for
  Magnetic, and that explosions and mob kills are unaffected.

### Q8. More behavior-test coverage - DONE 2026-10-09 (round 5)
- Round 5: Online/Chat/Global Chat detectors, Floo teleport (with a typo'd destination), Blazing Fire catch rate (about
  4x vanilla, statistical), Magic Hood (nametag hook and particle colour event), Spectre cube allocation/lookup,
  Sound Box and Sound Dampener server-visible state. **Found a real bug:** the Spectre cube lookup only matched the first
  cube (checklist #380, fixed in `SpectreHandler`).
- Not automatable here (documented limits): the client sound hook that actually mutes sounds, nametag rendering, the
  Online Detector's positive case on the dedicated server (fake player is not in the player list; it runs on the client
  run), real teleports between dimensions with a real connection, and anything visual.
- Stability work: light-dependent spawn tests now wait longer and treat vanilla's randomised light check as 'any of 40
  tries' because the client's light engine settles slowly after heavy earlier tests.

### Q9. Dedicated-server smoke test - DONE for boot (2026-10-08); gameplay on a server still open
- Boot test found that the mod crashed every dedicated server; fixed (checklist #378). The server now reaches "Done".
- The full suite (317 tests) now also runs on a dedicated server with a fake player (#379).
- **Q9 session 2026-10-09 (real client on `runServer`):** found and fixed a server crash (client-only `PacketBuffer.readString()` in text packets, #382). Verified: Ender Letter to another player is delivered to their mailbox, Spectre dimension round trip, Portkey (#381, #383).
- **Still open:** join a real client to a dedicated server and exercise sync-heavy features (Ender Mailbox, Portkey, Spectre
  dimension, GUIs, packets). Playing on a server needs a human or a client bot.

## Decided

- **Q3, Q4, Q6 (2026-10-08):** next target 1.16.5 on Forge; MinecraftSavePorter not covered; tests are run manually (no gating).
- **Q10 (2026-10-08): multi-version scope.** Lower bound 1.14.4; Forge then NeoForge (Fabric a later separate sweep);
  only what was valid in 1.14.4 is ported (not 1.12.2-only features); one branch per Minecraft version; 1.15.2 and
  1.17.1 skipped for now (Q11); stay with coremods (Q12 verifies feasibility per line). Catalog: `versions/README.md`.
- **Q1 (2026-10-07): keep the dev tooling in the release jar.** It is never loaded without `-Drandomthings.debug=true`,
  costs nothing at runtime and lets the tools run on a launcher install with one JVM argument.
- **Q2 (2026-10-07): dev features are enabled by the JVM property only;** the `[debug]` config options stay removed.
