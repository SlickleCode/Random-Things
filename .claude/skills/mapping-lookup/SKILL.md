---
name: mapping-lookup
description: Ground-truth Minecraft/Forge/NeoForge APIs with javap before writing code or a coremod against them, and map between name schemes (MCP / Mojang / SRG) where the branch has several. Use before writing code against an API you are not certain of, and whenever a transformer or reflection call must match a method or field by name.
---

# Mapping and javap lookup

Project rule (CLAUDE.md): never write against a Minecraft/Forge API from memory - read the real mapped jar.

**This skill is version-independent. Everything that differs per branch (naming scheme, where the mapped jar and sources are, helper scripts, learned API facts) is in `docs/project/version-notes.md`, section "mapping-lookup". Read that section first.**

## Method
- Signatures: `javap -p -cp "<mapped jar>" <class> | grep -i <word>`; quote `$` inner classes. Bytecode (coremods): `javap -p -c`; `docs/migration/tools/jpm.py <class> <method> [regex]` prints one method's bytecode where the version notes say it applies. Use these instead of dumping whole classes.
- Class names are the same in dev and production; **member names may differ** (SRG ids on some branches, plain Mojang names on the newest). The version notes say which. A transformer or reflection call that matches a member by name must accept every form that occurs in a real install, and match descriptors with the `net/minecraft/...` class names.
- Avoid hand-matching names in injected bytecode: put the call in a Java static helper (`AsmHandler`/`ClientAsmHandler`) so the compiler maps the name, and inject only an `INVOKESTATIC` of your own class.
- There is usually no vanilla source jar; bytecode is the ground truth (the version notes say when decompiled sources exist).
- Before relying on a Forge/NeoForge event, find who posts it in the loader's sources; check the 1.12.2 behaviour with `git show origin/1.12.2:<path>` before "fixing" anything that looks wrong.
- The `bytecode-scout` agent does this lookup for you and returns a short answer.
