---
name: coremod-patching
description: Write or change a JS coremod transformer (src/main/resources/transformer/*.js, registered in META-INF/coremods.json) that patches vanilla 1.18.2 bytecode, with its AsmHandler/ClientAsmHandler redirect target. Use when a feature needs to hook vanilla code that no Forge event or API reaches (rendering internals, redstone power queries, spawn rules, etc.). Mixins do not work here; coremods do.
---

# Coremod patching (Forge 40.3.12, JS coremods)

> This skill's body was written on 1.18.2. Version notes for this branch: check `docs/project/` (api-gotchas, coremods) and `docs/migration/plans/1.19.2-port-plan.md` first; jar paths, versions and counts below may differ (Forge 43.5.2, JDK 17).

Mixins cannot be tested via `runClient` in this setup - the project uses coremods (`META-INF/coremods.json` + `transformer/*.js` + `AsmHandler`/`ClientAsmHandler`). Existing transformers are the best templates; `docs/project/coremods.md` maps what each patches (update it when you add one; note it was written for 1.14.4 and the 1.18.2 retargets are listed in `docs/migration/plans/1.18.2-port-plan.md`).

## Workflow
1. Find the target with the `mapping-lookup` skill; read the real bytecode (`python docs/migration/tools/jpm.py <class> <method> "<regex>"`) - local variable slots, constants and call order are what you anchor on. Prefer a unique call or constant, not an instruction index. **Re-read the anchor on this version**: 1.18.2 moved things (e.g. `VertexLighterFlat` colour array is local 4, `HumanoidArmorLayer.renderModel` is `invokevirtual`, biome lookups return `Holder`).
2. Put the logic in a **static Java method** (`AsmHandler` for code that also loads on a dedicated server, `ClientAsmHandler` for client classes). The transformer only inserts `INVOKESTATIC lumien/randomthings/asm/<Handler>.<method>`; descriptors use the Mojang `net/minecraft/...` names of your handler's signature.
3. Skeleton: `initializeCoreMod()` returns `{name: {target: {type:'CLASS', name:'net.minecraft.world.level.Level'}, transformer: function(classNode){...; return classNode;}}}`; add the file to `coremods.json`.
4. Match methods by **both** the Mojang name and the SRG id (`m_...`) and by full descriptor. Never inject an `INVOKEVIRTUAL` of a vanilla method by name; wrap it in your Java helper.
5. **Run `docs/migration/tools/CoremodCheck.java` first** (offline; every transformer must report `ok`: a wrong target class or a non-matching anchor otherwise fails silently - see Traps). Then test in this order: build -> dev suite (`RT_AUTOTEST=true RT_AUTOTEST_NO_GUIS=true RT_AUTOTEST_EXIT=true ./gradlew runClient`) -> `prod-jar-check` -> a rendering scenario if it draws anything. Add a `CoremodTests` behaviour test for logic.

## Traps that cost real time
- **A transformer whose target class name is wrong applies to nothing and no suite notices** (1.18.2: a rename script run twice turned `ItemRenderer` into `ItemEntityRenderer`; found only by the scripted dyeing-machine scenario).
- **Insert-before loops must advance past the inserted node** (`i++` after `insertBefore`, or collect targets first), otherwise the client hangs at startup after `Developer tools enabled` (`jstack` shows Nashorn `initializeCoreMod#transformer`).
- `break` after patching one method stops the loop - use `continue` when one transformer patches several methods.
- A transformer that patches nothing fails silently; verify by observable behaviour, never by "no error".
- Redirecting a call by changing `owner/name/desc` of the same `MethodInsnNode` keeps the stack intact and is the simplest form.
- Stack-neutral injections only. Client-only hooks must clean up per-call state (`setRenderingStack` ... `clearRenderingStack` before every `RETURN`).
- Since 1.18.2 the JS sandbox blocks `Java.type('java.lang.System')`; use only the ASM/coremod API classes (`print(...)` for diagnostics).
- 1.18 glint: the glint `RenderType`s are shader-based (colour via `RenderSystem.setShaderColor` at draw time); every custom `RenderType` still needs its own `fixedBuffers` entry or you get `BufferBuilder not started` (see `TintedGlintRenderType`). Verified on 1.18.2 by the dyeing-machine scenario (pixel measurement of the tinted glint).
