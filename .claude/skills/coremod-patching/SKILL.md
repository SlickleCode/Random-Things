---
name: coremod-patching
description: Write or change a JS coremod transformer (src/main/resources/transformer/*.js, registered in META-INF/coremods.json) that patches vanilla bytecode, with its AsmHandler/ClientAsmHandler redirect target. Use when a feature needs to hook vanilla code that no Forge event or API reaches (rendering internals, redstone power queries, spawn rules, etc.). Mixins do not work here; coremods do.
---

# Coremod patching

Mixins cannot be tested via `runClient` and this Forge build never shipped Mixin's runtime - the project converts them to coremods (`META-INF/coremods.json` + `transformer/*.js` + `AsmHandler`/`ClientAsmHandler`). Existing transformers are the best templates; the map of what each patches is `docs/project/coremods.md` (update it when you add one).

## Workflow
1. Find the target with the `mapping-lookup` skill; read the real bytecode (`javap -p -c`) - local variable slots, constants and call order are what you anchor on. Prefer anchoring on a unique call or constant, not an absolute instruction index.
2. Put the logic in a **static Java method** (`AsmHandler` for code that also loads on a dedicated server, `ClientAsmHandler` for anything touching client classes - a server must never load it). The transformer only inserts `INVOKESTATIC lumien/randomthings/asm/<Handler>.<method>`.
3. Transformer skeleton: `initializeCoreMod()` returns `{name: {target: {type:'CLASS', name:'net.minecraft...'}, transformer: function(classNode){...; return classNode;}}}`; add the file to `coremods.json`.
4. Match methods by **both** dev and SRG name, and by full descriptor (see `mapping-lookup`). Never inject an `INVOKEVIRTUAL` of a vanilla method by SRG name (dev runtime has the MCP name); wrap it in your Java helper.
5. Test in this order: build -> dev suite (`RT_AUTOTEST=true RT_AUTOTEST_NO_GUIS=true RT_AUTOTEST_EXIT=true ./gradlew runClient`) -> `prod-jar-check` -> a rendering scenario via `scripted-playtest` if it draws anything. Add a `CoremodTests` behaviour test for logic.

## Traps that cost real time
- **Insert-before loops must advance past the inserted node.** `insertBefore(node, ...)` puts the new instruction at index i and `node` at i+1; a `for` loop that does not `i++`/skip re-matches the same `RETURN`/`ARETURN` forever and the client **hangs at startup** right after `Developer tools enabled` (`jstack` shows `Nashorn ... initializeCoreMod#transformer(<eval>:N)`). Always `i++` after inserting (or collect targets first, then insert).
- `break` after patching one method stops the loop - use `continue` when one transformer patches several methods.
- A transformer that patches nothing fails silently; verify by observable behaviour (a test or scenario), never by "no error".
- Replacing a call by changing `owner/name/desc` of the same `MethodInsnNode` (same descriptor) is the simplest redirect and keeps the stack intact.
- Stack-neutral injections only: push args, call a `void`/matching-type static, never leave extra values.
- Client-only hooks must clean up per-call state (`setRenderingStack` ... `clearRenderingStack` before every `RETURN`) so stale state cannot leak into unrelated rendering.
- 1.16 glint: item/armor glint types use `POSITION_TEX` (colour = GL colour at draw time) and every custom `RenderType` needs its own entry in the main buffer source's `fixedBuffers`, otherwise `BufferBuilder not started` (see `TintedGlintRenderType`).
