# Random Things

"Random Things packed into one Mod." — a Minecraft Forge mod originally by
[Lumien](https://github.com/lumien231), adding a large grab-bag of utility,
redstone, decorative, and magic-themed blocks and items (advanced redstone
components, inventory/item-transport utilities, the Spectre magic subsystem,
Ender and Floo networks, and more).

This repository is [SlickleCode](https://github.com/SlickleCode)'s fork of
the [original mod](https://github.com/lumien231/Random-Things), currently
being used to port the mod from 1.12.2 to modern Forge (1.14.4 and beyond).

## Branches

| Branch | Purpose |
| --- | --- |
| `main` | Default branch. Mirrors upstream's last released state (1.12.2-era source) — the stable base this fork's work branches from. |
| `1.14.4` | **Active work.** In-progress port of the mod to Forge 1.14.4, built package-by-package on top of `main`. Not yet feature-complete — see [TESTING_CHECKLIST.md](https://github.com/SlickleCode/Random-Things/blob/1.14.4/TESTING_CHECKLIST.md) on that branch for what's migrated vs. still deferred. |
| `master` | Upstream's old default branch name, kept around unchanged after `main` was introduced as the new default. Same content as `main`; not actively used going forward. |
| `1.8.9`, `1.9`, `1.9.4`, `1.10.2`, `1.11.2`, `1.12.2` | Upstream's historical per-Minecraft-version release branches, inherited as-is from the original repository. |

## Supported Minecraft versions

| Minecraft version | Branch | Status |
| --- | --- | --- |
| 1.8.9 | [`1.8.9`](https://github.com/SlickleCode/Random-Things/tree/1.8.9) | Legacy release (upstream) |
| 1.9 | [`1.9`](https://github.com/SlickleCode/Random-Things/tree/1.9) | Legacy release (upstream) |
| 1.9.4 | [`1.9.4`](https://github.com/SlickleCode/Random-Things/tree/1.9.4) | Legacy release (upstream) |
| 1.10.2 | [`1.10.2`](https://github.com/SlickleCode/Random-Things/tree/1.10.2) | Legacy release (upstream) |
| 1.11.2 | [`1.11.2`](https://github.com/SlickleCode/Random-Things/tree/1.11.2) | Legacy release (upstream) |
| 1.12.2 | [`1.12.2`](https://github.com/SlickleCode/Random-Things/tree/1.12.2) / `main` | Last complete, fully-released version (upstream) |
| 1.14.4 | [`1.14.4`](https://github.com/SlickleCode/Random-Things/tree/1.14.4) | **In-progress port** (this fork) — most standalone and tile-entity-backed blocks/items are ported; worldgen, potions/enchantments, recipes, and a few subsystems (Spectre energy, Ender network, Floo network, Rain Shield) are still outstanding |
