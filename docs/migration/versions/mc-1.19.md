# Minecraft 1.19 (final release 1.19.4) — analysis

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2022-06-07 ✔ |
| Final release | 1.19.4 — 2023-03-14 ✔ |
| Java | 17 |
| Forge | 45.4.5 ✔ |
| NeoForge | n/a |
| Fabric | ✔ |
| Mappings | Official Mojang names |
| Upstream mod status | No upstream branch ✔ |
| Tier | B — feasible, secondary demand |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | M (≈0.4×) |

## Era

The Wild Update; chat signing; many internal API reshuffles during 1.19.x.

## What changes (relevant to this mod)

- Components: `TranslatableComponent` etc. replaced by `Component.translatable(...)` factories (1.19).
- Registries: `RegisterEvent`/`DeferredRegister`; direct `Registry` access changes in 1.19.3+ (`BuiltInRegistries`).
- Creative tabs rebuilt (1.19.3 → `CreativeModeTabEvent`).
- Chat events/signing changed (Chat Detector, Global Chat Detector, Floo chat teleport use chat events).
- Block/entity data (`DataParameter`→`SynchedEntityData` accessors).

## Impact on Random Things

- Chat-based features (Chat Detector, Global Chat Detector, Floo) must be re-done against signed-chat events.
- Registration: `@ObjectHolder` pattern is replaced by `DeferredRegister`/`RegisterEvent`.
- Creative tab registration.
- Mixins/coremods: check status.

## Source and path

This repo (forward from 1.18.2).

## Verdict

Feasible; 1.19.2 vs 1.19.4 differ; choose 1.19.2 *or* 1.19.4 deliberately. Tier B.

## To verify before starting

- [ ] Choose final-minor 1.19.4 vs the more used 1.19.2 (Forge 43.5.2 ✔).
- [ ] Chat event API.

## Work log

*(none yet — add dated notes, findings and links here as work on this version proceeds.)*
