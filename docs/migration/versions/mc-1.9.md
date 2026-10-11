# Minecraft 1.9 (final release 1.9.4) — analysis

> **OUT OF SCOPE (owner decision 2026-10-08):** the mod supports 1.14.4 and newer only. Kept for reference.

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2016-02-29 ✔ |
| Final release | 1.9.4 — 2016-05-10 ✔ |
| Java | 8 |
| Forge | 12.17.0.2317 ✔ |
| NeoForge | n/a |
| Fabric | n/a |
| Mappings | MCP |
| Upstream mod status | Branches `1.9` and `1.9.4` exist upstream ✔ |
| Tier | B — feasible, secondary demand |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | M |

## Era

Combat update: offhand, shields, elytra, `SoundEvent` registry.

## What changes (relevant to this mod)

- Offhand slot and `EnumHand` everywhere.
- `SoundEvent` registry replaces string sounds.
- Forge capabilities are available (introduced around 1.8.9/1.9; verify).
- Tipped arrows, lingering potions (`PotionType`).

## Impact on Random Things

- Small compared with 1.8.9→1.12.2; mostly offhand-aware item code and sound events.
- End dimension changes and bosses do not affect the mod.

## Source and path

Upstream branches `1.9`/`1.9.4` + the 1.12.2 original.

## Verdict

Feasible; low demand (short-lived version). "Optional".

## To verify before starting

- [ ] Whether upstream `1.9` vs `1.9.4` differ materially.

## Work log

*(none yet — add dated notes, findings and links here as work on this version proceeds.)*
