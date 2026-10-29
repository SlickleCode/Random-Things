# Scope decisions (user-made, binding)

_Moved verbatim from `PORTING_PLAN.md` (2026-10-11 doc refactor). Index: [README.md](README.md)._

- **Wiki is the authoritative scope** (100 feature pages); 1.12.2 code without a wiki page is not migrated
  unless explicitly kept. Third-party compat is dropped; ore-dictionary → plain vanilla items or Forge tags
  (`forge:stone`, `forge:cobblestone`, `forge:glass`, `forge:glass/colorless`, `forge:slimeballs`,
  `forge:rods/wooden`, `forge:ingots/iron`, `forge:nuggets/gold`, `forge:gems/lapis`, `forge:dyes/*`).
- **Match 1.12.2 exactly unless the user says otherwise; flag divergences.** Deliberate divergences so far:
  Spectre Leaf drops Ectoplasm only (no sapling); Floo Powder is shapeless; Colored Grass named
  "<Color> Grass Block" + shovel preference; Potion Vaporizer/Block Destabilizer flood-fills pass through
  non-full blocks; Global Chat Detector has no op bypass; Rune Base left-click clears the whole block;
  Rune Base uses the real noisy texture by default (`FlatRunes` config for flat); Super Lubricent
  blocks/boots have a speed cap (blocks 0.35 b/t, boats 15°/t turn cap) that 1.12.2 never had; Trigger
  Glass chain limit config (default 20); Magnetic config-off keeps the enchant registered but unobtainable;
  Remote camo row implemented (reversed an earlier simplification); Ender Letter message body (new).
- **Ported after all (2026-10-29, unverified):** Blackout Powder (was "will not migrate") and the loot-only Numbered Spectre Coil tier (was deferred); both are on the wiki (Spectre Illuminator, Spectre Coils pages).
- **Not to be ported (2026-10-14):** the Precious Emerald item, with the Festival system it triggers and its animated rainbow glint; the other 1.12.2 glint
  colours are ported (`GlintColors`).
- **Standing simplifications** (not worth re-litigating): metadata sub-items → discrete items per variant;
  1.12.2 icon-sprite buttons → text buttons, then partly restored as `SpriteStateButton`/`ToggleIconButton`;
  bespoke particles → vanilla `RedstoneParticleData`/`ParticleTypes`; magic-circle GL framework dropped;
  `ItemFilter` replaced by single example-item slots; Crafting recipes' tags simplified as above.
- **Check the real 1.12.2 source before "fixing" anything that looks wrong** — several suspected bugs were
  intentional (see-through Plates, Lapis/Quartz Lamp always lit, Portkey glowing forever while unbound,
  Chest-less Colored Grass recipe, Golden Compass only reading NBT set by a recipe). Also check recipe/loot
  JSON directly, not just Java grep (data is referenced by numeric meta, not enum names).

