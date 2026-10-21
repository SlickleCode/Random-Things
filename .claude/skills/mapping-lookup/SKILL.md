---
name: mapping-lookup
description: Ground-truth Minecraft 1.18.2 / Forge 40 APIs and find Mojang names vs production SRG ids (m_/f_) with javap and the helper scripts. Use before writing code or a coremod against an API you are not certain of, and whenever a transformer or reflection call must match a method/field by its SRG id.
---

# Mapping and javap lookup (1.18.2: official Mojang names)

> This skill's body was written on 1.18.2. Version notes for this branch: check `docs/project/` (api-gotchas, coremods) and `docs/migration/plans/1.19.2-port-plan.md` first; jar paths, versions and counts below may differ (Forge 43.5.2, JDK 17).

Project rule (CLAUDE.md): never write against a Minecraft/Forge API from memory - read the real jar.

## Naming on this branch
- Source, dev runtime and class names use **official Mojang names** (`net.minecraft.world.level.Level`, `BlockEntity`, `getBlockEntity`). Class names are the same in production; **methods and fields are SRG ids in production** (`m_46758_`, `f_58650_`).
- A transformer or reflection call that matches a member by name must accept the Mojang name **and** the SRG id (`name === "isRainingAt" || name === "m_46758_"`), and match descriptors with the `net/minecraft/...` Mojang class names. `ObfuscationReflectionHelper.findField/findMethod` takes the SRG id only.
- Avoid hand-matching names in injected bytecode: put the call in a Java static helper (`ClientAsmHandler`/`AsmHandler`) so the compiler/reobf maps the name; inject only `INVOKESTATIC` of your own class.

## Mapped jar (for javap)
```
~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.18.2-40.3.12_mapped_official_1.18.2/forge-1.18.2-40.3.12_mapped_official_1.18.2.jar
```
- Signatures: `javap -p -cp "$J" net.minecraft.client.renderer.ItemRenderer | grep -i render`; quote `$` inner classes (`'net.minecraft.core.Holder$Reference'`).
- Bytecode (coremods): `javap -p -c`. The branch helper `docs/migration/tools/jpm.py <class> <method> [regex]` prints one method's bytecode (optionally only matching lines) - use it instead of dumping whole classes.
- There is **no vanilla source** (the `-sources.jar` next to Forge only holds Forge's own classes); bytecode is the ground truth.

## SRG ids
- `python docs/migration/tools/lookup.py net.minecraft.world.level.Level.isRainingAt <more...>` prints the `m_`/`f_` id for a Mojang class+member (needs `client_mappings.txt` and `mcp_mappings.tsrg` in `~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.18.2/`, present after the first build). Methods are matched by obf descriptor, so overloads resolve to one id; fields by name.
- Mojang and SRG ids are stable for a member across 1.16 -> 1.18 only when vanilla did not refactor it; always re-check in the jar.

## Handy facts learned (1.18.2)
- `Biome` lookups return `Holder<Biome>` (`.value()`); world biomes are data-pack instances, resolve names through `level.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)`.
- `DimensionType`'s constructor is private: the Spectre type is `data/randomthings/dimension_type/spectre.json`. Registries are frozen once the server starts: swap `Holder.Reference.value` (SRG `f_205752_`) by reflection to replace an entry (Peace Candle pool).
- Empty block-entity update tags arrive as `null` in `ClientboundBlockEntityDataPacket.getTag()`; `BlockEntity.getUpdateTag()` default is empty (override it to `saveWithoutMetadata()` for synced data).
- `TickEvent.WorldTickEvent` fires for client levels too: guard `event.world.isClientSide` before casting to `ServerLevel`.
- Shaders: a vertex format needs the matching shader (`POSITION_TEX_COLOR` -> `getPositionTexColorShader`), otherwise colours are silently ignored.
- Generator steps are futures and `StructureFeatureManager`/`Blender` query chunks 7-8 chunks out; a hand-driven generation test needs a wide region and a no-structures manager (see `WorldgenTests`). Use `ConfiguredStructureFeature.generate` for real village starts.
- Before relying on a Forge event, find who posts it; check 1.12.2 behaviour with `git show origin/1.12.2:<path>` before "fixing" anything that looks wrong.
