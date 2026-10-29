# Edit log, group C (spectre)
- spectre_anchor: changed: description (anchor consumed, slot/full-inventory behaviour, stack size 1, not already anchored), keywords, properties added.
- spectre_charger: changed: description (must be in inventory, per-item rate, 1,000,000 buffer, Injector owner/any side/receive only), keywords, properties added.
- spectre_key: changed: description (empty-hand click on Core returns, works from any dimension, Core unbreakable/no item, key made from ingots + stable ender pearl), keywords, properties added.
- spectre_lens: changed: properties + keywords only (placement, owner-only, beacon needs pyramid). Description left as is (possible bug, below).
- spectre_tools: changed: description (needs Spectre Ingots + obsidian, not ectoplasm directly; sword no reach; main hand only; ingot repair), keywords, properties (2000 / 22 / 8.0 / harvest 3 / reach +3).
- ectoplasm: changed: description (only player kills; fake players excluded; 20% second drop; moon bonus needs night + open sky; leaves drop ectoplasm; spawn egg), keywords, properties (config keys + defaults).
- spectre_ingot (NEW, item): created, no recipe fields.
- spectre_sapling, spectre_coils, spectre_illuminator: not touched (user decision).

SKIPPED-POSSIBLE-BUG:
- spectre_lens "only 1 Spectre Lens per dimension" / "no matter where you are in the dimension of the beacon". Code (SpectreLensHandler.get, overworld storage, key = owner UUID, no dimension check in tick) = one lens per player, effects in any dimension. Original (origin/1.12.2 SpectreLensHandler): world.getPerWorldStorage() and tick only applies when player.world == worldObj, i.e. per dimension, matching the wiki. Today's code differs from the original; the javadoc claims it matches the original but it does not, and scope-decisions.md does not list it. Left wiki sentences unchanged.

HOLD:
- Coil/injector/charger buffer across dimensions (pool per dimension unresolved); not written on spectre_charger.
- Spectre Key return trip (TeleporterTransformer on return, open question 4) and whether the saved return position survives death (open question 8); not written.
- Spectre Charger injector/charger rows never player-verified: only code-backed facts used.

REMOVED-CLAIMS:
- spectre_tools "Using Ectoplasm you can craft Spectre versions": recipes need Spectre Ingot + obsidian (D/recipes/spectre_{sword,pickaxe,axe,shovel}.json). Reworded.
- ectoplasm "when you kill an entity" / "under the sky": narrowed to player kills (RandomThings.java:484) and night + open sky (:498).

NOTES: ectoplasm still lists recipe_images crafting-ectoplasm.png but the mod has no ectoplasm recipe (sheet UNMAPPED); recipe fields untouched, left for the script.

NEW-FACTS-ADDED: 38
