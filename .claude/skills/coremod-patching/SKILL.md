---
name: coremod-patching
description: Write or change a JS coremod transformer (src/main/resources/transformer/*.js, registered in META-INF/coremods.json) that patches vanilla bytecode, with its AsmHandler/ClientAsmHandler redirect target. Use when a feature needs to hook vanilla code that no Forge/NeoForge event or API reaches (rendering internals, redstone power queries, spawn rules, etc.). Mixins do not work here; coremods do.
---

# Coremod patching (JS coremods)

Mixins cannot be tested via `runClient` in this setup - the project uses coremods (`META-INF/coremods.json` + `transformer/*.js` + `AsmHandler`/`ClientAsmHandler`). Existing transformers are the best templates; `docs/project/coremods.md` maps what each patches (update it when you add one).

**Per-version details (name forms to match, JS sandbox rules, glint/render notes, traps found on that branch) are in `docs/project/version-notes.md`, section "coremod-patching". Read it first.**

## Workflow
1. Find the target with `mapping-lookup` (or the `bytecode-scout` agent); read the real bytecode - local variable slots, constants and call order are what the anchor depends on.
2. Prefer a Forge/NeoForge event or API when one exists; a coremod is the last resort.
3. Write the transformer, anchored on a unique call or constant (owner, name, descriptor), and redirect to a static helper in `AsmHandler`/`ClientAsmHandler`. Never inject an `INVOKEVIRTUAL` of a vanilla method by name from the transformer.
4. Register it in `META-INF/coremods.json`, document it in `docs/project/coremods.md`.
5. Verify: a test in `debug/test/CoremodTests` that fails without the patch, the render scenes for rendering patches, then `prod-jar-check`. Two automatic guards exist on every branch: the `coremod.applied` test (every transformer script ends by adding a synthetic field `rt$hooks$<Tag>$<count>` to its target class; a transformer whose anchor vanished leaves no marker or a count of 0) and the render-scene hook checks (`HookProbe.hit("...")` in the hooks, `SceneHookChecks` asserts per scene that they fired, report `randomthings_scenes.txt` in the game directory after `RT_AUTOTEST_SCENES=true`). A new transformer needs its marker snippet (copy the end of an existing script) and, when it draws something, a probe and a scene expectation.

## Traps (all happened)
- **A transformer whose target class name, method or anchor is wrong applies to nothing and no suite notices.** After every Minecraft bump re-derive every anchor from the new jar; three silently stopped working on 1.20.6 alone.
- A frozen startup right after `[RT-DEBUG] Developer tools enabled` is a transformer stuck in a loop: `jstack <pid>`.
- Every custom `RenderType` needs its own `fixedBuffers` entry or rendering throws; colours that look ignored usually mean the wrong vertex format/shader.
