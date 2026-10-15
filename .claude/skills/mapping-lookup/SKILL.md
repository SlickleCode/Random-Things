---
name: mapping-lookup
description: Ground-truth Minecraft/Forge APIs and find dev (MCP) vs production (SRG func_/field_) names with javap and the mapping files, per Minecraft version. Use before writing code or a coremod against an API you are not certain of, and whenever a transformer must match a method by its SRG name.
---

# Mapping and javap lookup

Project rule (CLAUDE.md): never write against a Minecraft/Forge API from memory - read the real jar.

## Mapped jar (dev names), per version
```
~/.gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/<forge-version>_mapped_snapshot_<date>-<mc>/
  1.14.4: forge-1.14.4-28.2.26_mapped_snapshot_20190819-1.14.3-recomp.jar   (+ -sources.jar)
  1.16.5: forge-1.16.5-36.2.39_mapped_snapshot_20210309-1.16.5.jar          (use the one WITHOUT -launcher; there is no -sources next to it)
```
Locate it with `find ~/.gradle/caches/forge_gradle/minecraft_user_repo -name "*mapped_snapshot*.jar"`. Then:
- Signatures: `javap -p -cp "$J" net.minecraft.client.renderer.ItemRenderer | grep -i glint`
- Bytecode (needed for coremods): `javap -p -c -cp "$J" <class>` - read the real instruction shapes, local variable slots, constants.
- Fields/inner classes: `'net.minecraft.block.AbstractBlock$Properties'` (quote the `$`).

## SRG names (production)
- **Methods/fields -> SRG:** MCP zip, e.g. `~/.gradle/caches/forge_gradle/maven_downloader/de/oceanlabs/mcp/mcp_snapshot/<date>-<mc>/mcp_snapshot-<date>-<mc>.zip` -> `methods.csv` / `fields.csv` (`searge,name,...`). Read it with Python `zipfile` (no unzip needed). Only names that have an MCP name are in there; the rest are already `func_...` in dev too.
- **Class/obf layout, signatures with obf types:** `~/.gradle/caches/forge_gradle/mcp_repo/de/oceanlabs/mcp/mcp_config/<mc>-<date>/joined/rename/config/joined.tsrg` (class blocks `obf net/minecraft/...`, members `obf desc func_xxx`). Find a class block with `grep -n " net/minecraft/client/renderer/ItemRenderer$"` then read the lines after it.
- **Runtime reality (1.16 userdev):** methods that HAVE an MCP name show that name at dev runtime, methods without one keep `func_...`; production always has SRG. A transformer that matches by name must accept **both** (`name === "renderItem" || name === "func_229111_a_"`) and match descriptors using the `net/minecraft/...` class names.
- **Avoid hand-matching names in bytecode you inject**: put the call in a small Java static helper (e.g. `ClientAsmHandler.setRenderingStackFromSlot(entity, slot)`) so the compiler/reobf maps the method name; inject only `INVOKESTATIC` of your own class. Injecting `INVOKEVIRTUAL ... func_184582_a` crashed the dev client with `NoSuchMethodError`.

## Handy facts learned
- 1.16: `BlockState.isSolid()` (default true) decides whether a neighbour's face is culled against a block; see-through blocks need `Block.Properties.notSolid()` (the render layer is separate, `RenderLayerSetup`).
- 1.16: world biomes are data-pack instances; look names up through `((ServerWorld) world).func_241828_r().getRegistry(Registry.BIOME_KEY)`, not `ForgeRegistries.BIOMES`.
- 1.16: dimensions in NBT are registry-key strings (`"minecraft:overworld"`), 1.14 uses int ids; attribute `generic.max_health` vs `generic.maxHealth`; structure names for `/locate` are lower case on 1.16.
- Before relying on a Forge event, find who posts it in the sources jar (`HarvestDropsEvent` is never posted).
- Check 1.12.2 behaviour with `git show origin/1.12.2:<path>` before "fixing" anything that looks wrong.
