# Minecraft 1.7 (final release 1.7.10) — analysis

> **OUT OF SCOPE (owner decision 2026-10-08):** the mod supports 1.14.4 and newer only. Kept for reference.

> Evidence key: ✔ = verified 2026-10-08 from official metadata; unmarked = general knowledge, verify before work.

| | |
| --- | --- |
| Line first release | 2013-10-25 ✔ (1.7.2) |
| Final release | 1.7.10 — 2014-05-14 ✔ |
| Java | 6/7 (build with JDK 8, target 7) |
| Forge | 10.13.4.1614 ✔ |
| NeoForge | n/a |
| Fabric | n/a |
| Mappings | MCP (SRG at runtime) |
| Upstream mod status | No branch in the upstream repo ✔ (branches are 1.8.9, 1.9, 1.9.4, 1.10.2, 1.11.2, 1.12.2, master); older releases of the mod existed for 1.7.10 per the project page (verify) |
| Tier | C — feasible but low value / short-lived or very expensive |
| Effort (relative to the 1.12.2→1.14.4 port = 1.0×) | XL (rewrite of most client/rendering and capability code) |

## Era

The longest-lived modded version; huge legacy modpack ecosystem.

## What changes (relevant to this mod)

- String registry names (`GameRegistry.registerBlock/Item`) replace numeric IDs.
- Blocks use metadata (4 bits) and `IIcon` texture registration; no JSON models or blockstates.
- ISBRH/TESR for custom rendering; `IInventory`/`ISidedInventory` for item access; no capability system (no `LazyOptional`/`ICapabilityProvider`).
- Fluids via Forge `Fluid`/`IFluidHandler` interfaces; energy via other mods' APIs (no Forge Energy).
- Ore dictionary is the tag system; recipes in Java.
- ForgeGradle 1.x/2.x; Forge's `IMessage` SimpleNetworkWrapper for packets.

## Impact on Random Things

- Registration/items: metadata sub-items are *native* here (the opposite problem from 1.14.4): the discrete-item design of this port would have to be re-collapsed or kept discrete.
- Rendering: all JSON models/blockstates are replaced by code; GUIs use `GuiContainer` and `IGuiHandler`.
- Capabilities/fluids/energy: Ender Bucket, Spectre energy network and many inventory interactions need interface-based rewrites.
- Coremods: Forge 10.x supports classic `IClassTransformer` coremods — the original 17 ASM patches are the model here.
- Worldgen: `IWorldGenerator` and `WorldType`/provider based dimension.
- Original mod history: Random Things began on this version.

## Source and path

Original mod history/jars for 1.7.10 and the 1.8.9/1.12.2 branches as references. Not derivable from this 1.14.4 port.

## Verdict

Possible but expensive; only worth it if the 1.7.10 modpack community is a target. Candidate "legacy tier".

## To verify before starting

- [ ] Locate the actual 1.7.10 source of Random Things (older repo history or release jars).
- [ ] Confirm available toolchain (ForgeGradle 1.2/2.x on JDK 8).

## Work log

*(none yet — add dated notes, findings and links here as work on this version proceeds.)*
