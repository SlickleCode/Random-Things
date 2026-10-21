---
description: Show which in-game tests exist on which version branch - /parity [--all | --missing <branch>]
argument-hint: [--all | --missing <branch>]
---

Run `python docs/migration/tools/test-parity.py $ARGUMENTS` and report the result.

For every test that is missing on some branch, classify it: **version-specific** (e.g. `behavior.stack_data.*` exists only because 1.20.6 has data components), **renamed** (same test under a different name, e.g. the glowing-mushroom and Spectre dimension tests), or a **real gap** (a gameplay test that should be mirrored). Offer to port the real gaps (one at a time, mutation-check each); do not port without a yes.
