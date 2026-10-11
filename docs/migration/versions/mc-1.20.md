# Minecraft 1.20 (final release 1.20.6) — analysis

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2023-06-02 ✔ |
| Final release | 1.20.6 — 2024-04-29 ✔ |
| Java | 17 for 1.20–1.20.4; **21 for 1.20.5+** |
| Forge | 50.2.10 ✔ (1.20.1: 47.4.26 ✔; 1.20.4: 49.2.9 ✔) |
| NeoForge | ✔ 1.20.1 builds (47.1.106, separate artifact); main artifact from 1.20.2 (20.6.141 for 1.20.6) |
| Fabric | ✔ |
| Mappings | Official Mojang names |
| Upstream mod status | No upstream branch ✔ |
| Tier | A — primary targets (large player base, loader support, reasonable tooling) |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | 1.20.1: M (≈0.3×); 1.20.6: XL (≈1.0×, data components + payloads + codecs + Java 21) |

## Era

1.20.1 is the modern "classic": biggest modded version of the 2020s. 1.20.2–1.20.6 are transition releases where core systems change.

## What changes (relevant to this mod)

- 1.20: `GuiGraphics` replaces `PoseStack`-based GUI drawing; 1.20.1 is the last version before the major rework.
- 1.20.2: configuration phase and new networking (`CustomPacketPayload`) for NeoForge; NeoForge forks from Forge.
- 1.20.5: **data components replace item NBT**; `FriendlyByteBuf`→`RegistryFriendlyByteBuf`; Java 21; recipes/ingredients codec changes; many serializers become `Codec`/`StreamCodec`.
- 1.20.6: final of the line; same as 1.20.5 plus fixes.

## Impact on Random Things

- **Item NBT is gone in 1.20.5+**: Portkey position/camo, Spectre Anchor tag, Ender Letter message, Redstone Remote bindings, Position Filter, Golden Compass, Biome Crystal, Sound Pattern, Time in a Bottle, Dyeing Machine's `rtDye` — all item-stored data becomes custom data components.
- Networking: the generic signal message and the few targeted packets must be rewritten as payloads.
- Recipes: the six special recipes + all JSON recipes change format (codecs).
- Coremods: JS coremods still ship on Forge 1.20.1/1.20.6 and NeoForge 1.20.2+ builds ✔ (installer manifests, 2026-10-08); they only disappear at Forge 1.21.11 / NeoForge ~1.21.8.
- Choose Forge for 1.20.1 and NeoForge for 1.20.2+.

## Source and path

This repo (forward from 1.19.4). For 1.20.1 and 1.20.6 treat as two separate targets.

## Verdict

1.20.1 is the single highest-value legacy target after 1.16.5. 1.20.6 is mainly a bridge to 1.21.x.

## To verify before starting

- [ ] Whether Forge or NeoForge is preferred for 1.20.1 (both have builds ✔).
- [ ] Mixin availability and whether JS coremods still exist on each.

## Work log

*(none yet — add dated notes, findings and links here as work on this version proceeds.)*
