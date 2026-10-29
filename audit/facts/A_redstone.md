# A_redstone fact sheet (group REDSTONE) - source: mod1144 (origin/1.14.4 archive)
Path abbreviations (relative to MOD): J = src/main/java/lumien/randomthings ; RD = src/main/resources/data/randomthings ; LANG = src/main/resources/assets/randomthings/lang/en_us.json
Evidence tags: [code] read in source; [test] automated test/checklist PASS; [playtest] checklist/fix-log says verified in game; [unverified].
Registry blocks generally: all items go to creative tab RT_ITEM_GROUP (J/item/ModItems.java:244-313, 391-392). No chest loot, no worldgen, no mob drops for anything here (grep of J/handler/LootHandler.java for redstone: no hit) [code].
All block loot tables (RD/loot_tables/blocks/<name>.json) = drop self, condition survives_explosion; wall torch table drops randomthings:advanced_redstone_torch [code].
Counted CHECKLIST rows: docs/testing/checklist-archive/*.md (TESTING_CHECKLIST.md itself has no rows for this group). Test list: docs/testing/test-inventory.md:28-49.

======================================================================
## redstone_interface (Redstone Interface)  [registry: block+item+TE+container "basic_redstone_interface"; shared base class RedstoneInterfaceTileEntity; handler redstonesignal is NOT this block - see activator/remote]
OBTAIN: shaped RD/recipes/basic_redstone_interface.json "IRI/RSR/IRI": I=minecraft:iron_ingot (item, not tag), R=redstone, S=randomthings:stable_enderpearl; out x1 [code]. Loot: drops self [code]. Item registered via registerItemForBlock J/item/ModItems.java:312 [code]. Not in chest loot/worldgen.
BEHAVIOR:
- Block: Material.ROCK, hardness 2.0 (J/block/redstoneinterface/BasicRedstoneInterfaceBlock.java:25). Has TE. onBlockActivated ALWAYS opens a GUI (NetworkHooks.openGui) and returns true, no check for held item (BasicRedstoneInterfaceBlock.java:60-71) [code].
- GUI (read-only status screen, container BasicRedstoneInterfaceContainer): shows "Target" + X/Y/Z, or red "No Target" (J/client/screen/BasicRedstoneInterfaceScreen.java:32-38; LANG gui.randomthings.basic_redstone_interface.*) [code]; fix-log #364 PASS for label wording (checklist-archive/5-batch-3-...-3.md:78) [playtest-ish: PASS row, 'playtest' by user].
- Target binding: ONLY via Redstone Tool (see redstone_tool item below); TE field `target` single BlockPos, NBT keys targetX/Y/Z (BasicRedstoneInterfaceTileEntity.java:84-97) [code]. setTarget notifies old and new target (BasicRedstoneInterfaceTileEntity.java:33-55) [code]. No Position Filter path for Basic.
- Mechanic: TE samples, for each of 6 faces f, weak power `world.getRedstonePower(pos.offset(f), f)` and strong power `world.getStrongPower(pos.offset(f), f)` (RedstoneInterfaceTileEntity.java:86-110). Refreshed ONLY from the block's neighborChanged/onBlockAdded (BasicRedstoneInterfaceBlock.java:44-58) [code]. If anything changed -> notifies the target (neighborChanged + notifyNeighborsOfStateChange with REDSTONE_BLOCK) and markDirty + client sync (RedstoneInterfaceTileEntity.java:112-124) [code].
- Effect on target: coremods patch World.getRedstonePower (WorldRedstonePowerTransformer) and IWorldReader.getStrongPower (WorldReaderStrongPowerTransformer); override returns max(vanilla, interface value, activator/remote signal) (J/asm/AsmHandler.java:388-395, 410-421; META-INF/coremods.json:10-11; docs/project/coremods.md:22) [code]. Semantics: the TARGET position is the one that "receives" power: for query (pos,facing) the queried position is `pos.offset(facing.opposite)`; if an interface targets it, it gets the interface's per-side value for that same `facing` (RedstoneInterfaceTileEntity.java:51-76). So the target sees, on each side, exactly the input the interface sees on that side (weak and strong kept separate, analog strength kept 0-15, not clamped to 15 boolean) [code]. Interface does NOT make the target an emitter toward its neighbours (a clicked/target stone does not light a lamp next to it) - derived from override only affecting queries whose queried pos == target [code, derived].
- Multiple interfaces on same target: max of values (loop with Math.max) (RedstoneInterfaceTileEntity.java:51-76) [code].
- Range / dimension: no distance check anywhere in setTarget/notify [code]. Dimension: static registry check `ri.world == world` (RedstoneInterfaceTileEntity.java:56,69) so target is interpreted in the interface's OWN world; coords are plain BlockPos, no dimension stored for Basic [code]. Chunk-loading of far target: notifyOne calls world.getBlockState(targetPos) without isBlockLoaded guard (BasicRedstoneInterfaceTileEntity.java ~notifyOne) -> may load the chunk synchronously [unverified].
- Breaking/removing the TE re-notifies target (RedstoneInterfaceTileEntity.java:124-128 remove()) [code][playtest: scenario redstone_interface docs/testing/desktop-helper/scn.py:772-790 "breaking the interface stops the mirroring"].
- Persistence: weakPower/strongPower per side stored as NBT compounds "weakPower"/"strongPower" (RedstoneInterfaceTileEntity.java:150-172) so state survives reload [code].
- Redstone-tool line rendering: J/client/renderer/RedstoneObserverLineRenderer.java iterates ONLY RedstoneObserverTileEntity.loadedObservers (lines 62-82). Basic Redstone Interface connections are NOT drawn [code].
- OpenComputers component "basic_redstone_interface" (setTarget/getTarget): grep for opencomputers/li.cil in J and resources = no hit; not in code [code]. (1.12.2 had handler/compability/oc/DriverBasicRedstoneInterface.java - git ls-tree origin/1.12.2.)
CONFIG: none.
DIVERGENCE: 1.12.2's BlockBasicRedstoneInterface.onBlockActivated returned false when the player held the Redstone Tool so item use could proceed (git show origin/1.12.2:src/main/java/lumien/randomthings/block/redstoneinterface/BlockBasicRedstoneInterface.java). The 1.14.4 block has no such check (see OPEN QUESTIONS #1). Block comment/fix-log #283 still say "right-click with Redstone Tool starts linking".
PLAYTEST-NEEDED: whether right-click with Redstone Tool actually links (vs opening the GUI); GUI look; chunk loading at far targets; save/reload.
PARITY: none noted in docs/project/version-notes.md for this feature (not checked in detail).
EVIDENCE: test coremod.redstone_interface.mirrors_its_input_power_onto_the_bound_target (J/debug/test/CoremodTests.java:197-218; lamp lights, unrelated lamp stays dark, loses power when input removed; target set via setTarget directly, NOT via tool) [test]. Scenario redstone_interface (target set via /data merge, not tool) [playtest-scripted]. Checklist #283 PASS, #284 PASS (checklist-archive/5-batch-3-...-2.md:28-29), #376 (5-batch-3-...-3.md:91) [playtest].
WIKI-CLAIM-CHECK: "does not work across dimensions" -> consistent (same-world registry check, RedstoneInterfaceTileEntity.java:56) but no dimension is stored for Basic at all; "no range limitation" -> confirmed no limit in code; "red lines while holding tool" -> contradicted for the interface (renderer only draws observers, RedstoneObserverLineRenderer.java:62); "Open Computers component basic_redstone_interface setTarget/getTarget" -> not-in-code; "use a Redstone Tool: first right click interface then block" -> code path exists in RedstoneToolItem.java:43-77 but reachability doubtful (see OPEN QUESTIONS #1); "Generally acts as the block that it's linked to" -> confirmed in the sense that target receives interface's own per-side input; "up to 9 blocks advanced" -> confirmed (9-slot inventory).

## basic Redstone Tool item (redstone_tool; item recipe asked for by task; no own wiki page - bundled in redstone_observer/redstone_interface)  [registry: item "redstone_tool", J/item/ModItems.java:313, maxStackSize 1; class RedstoneToolItem]
OBTAIN: shaped RD/recipes/redstone_tool.json pattern ["R","S","S"] R=minecraft:redstone, S=tag forge:rods/wooden, out x1 [code]. Not in chest loot.
BEHAVIOR: onItemUse (RedstoneToolItem.java:43-77): if stack has no NBT and clicked block is not Redstone Observer / Basic Redstone Interface -> FAIL. Clicking one of those two blocks sets tag linking=true, oX/oY/oZ = block pos, returns SUCCESS. Next onItemUse on any block: if linking -> if clicked pos != origin, and origin block is Observer/Basic Interface, calls setTarget(pos); sets linking=false; SUCCESS. Clicking a non-link-start block while not linking = FAIL (even if tag exists) [code]. hasEffect (glint) while linking==true (RedstoneToolItem.java:31-34) [code]. Advanced Redstone Interface is NOT a link start [code]. Target block can be any block incl. self-adjacent; no distance limit, no dimension check; if origin block was removed/changed meanwhile nothing happens but linking resets [code].
DIVERGENCE: glint is the vanilla enchant glint not the 1.12.2-style recolored one (docs/project/wiki-feature-status.md:98 "red enchant-glow ... not yet re-added") [doc].
PLAYTEST-NEEDED: whether onItemUse is reached at all (OPEN QUESTIONS #1).
WIKI-CLAIM-CHECK: (redstone_interface/redstone_observer) "first right click observer/interface then the target block" -> confirmed by RedstoneToolItem logic; reachability unverified.

======================================================================
## advanced_redstone_interface (Advanced Redstone Interface)  [registry: block+item+TE+container "advanced_redstone_interface"]
OBTAIN: shaped RD/recipes/advanced_redstone_interface.json "ROR/OIO/ROR": R=minecraft:redstone_block, O=minecraft:obsidian, I=randomthings:basic_redstone_interface; x1 [code]. Loot: drops self [code]. Item via ModItems.java:312 [code].
BEHAVIOR:
- Block Material.ROCK hardness 2.0 (AdvancedRedstoneInterfaceBlock.java:25). onBlockActivated opens GUI unconditionally (lines 80-90) [code]. Breaking drops the inserted Position Filters (onReplaced lines 38-56) [code].
- GUI: AdvancedRedstoneInterfaceContainer: 9 slots (one row at y=18) + player inventory; only randomthings:position_filter accepted (isItemValid, AdvancedRedstoneInterfaceTileEntity.java:35-37); shift-click moves between 9 slots and inventory (container lines 57-90) [code]. Checklist #285 PASS [playtest]. 
- Targets: a Set<BlockPos> built from each Position Filter's stored pos via PositionFilterItem.getPosition (AdvancedRedstoneInterfaceTileEntity.java:60-100). The filter's stored DIMENSION is never read (PositionFilterItem.getPosition J/item/PositionFilterItem.java:84-94 returns only x/y/z; dimension int is stored at :78 but unused by the interface) -> positions are applied in the interface's own world [code]. Empty/unset filters ignored; duplicates collapse (Set) [code].
- Changing a slot: onContentsChanged -> markDirty + refreshTargets, which notifies newly added AND removed positions (lines 40-43, 81-101) [code].
- Same mirror mechanic, range, per-side weak/strong, max-combining as Basic (shared base class) [code].
- POSSIBLE BUG: targets are rebuilt in read() via refreshTargets() (line 131-136) but refreshTargets returns immediately when this.world == null (line 61-63); in 1.14.4 chunk loading TileEntity.read is called before the world is set, and ItemStackHandler.deserializeNBT does not fire onContentsChanged; there is no onLoad override. So after a world/chunk reload `targets` may stay empty until a slot is edited, i.e. the Advanced interface may stop powering targets after reload [unverified - code inference; see OPEN QUESTIONS #2].
CONFIG: none.
DIVERGENCE: none documented. Filter dimension ignored (see above).
PLAYTEST-NEEDED: save/reload with filters inside; GUI look.
EVIDENCE: no automated test targets the Advanced variant (test list has only Basic) [none]; checklist #285 PASS (5-batch-3-...-2.md:30); recipe craft test behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output (docs/testing/covered-by-tests.md:58) [test].
WIKI-CLAIM-CHECK: "linked to a total of 9 blocks" -> confirmed (ItemStackHandler(9), line 33); "Instead of Redstone Tool you use Position Filters inserted into the gui" -> confirmed (tool cannot link it, RedstoneToolItem.java:80-82).

======================================================================
## redstone_activator (Redstone Activator)  [registry: item "redstone_activator" J/item/ModItems.java:391 maxStack 1; handler J/handler/redstonesignal/RedstoneSignalHandler + RedstoneSignal; coremods WorldRedstonePower/WorldReaderStrongPower]
OBTAIN: shaped RD/recipes/redstone_activator.json "IRI/ITI/III": I=minecraft:iron_ingot, R=minecraft:redstone, T=minecraft:redstone_torch; x1 [code]. No loot.
BEHAVIOR:
- onItemUse on a block (server only): RedstoneSignalHandler.get(world).addSignal(world, clickedPos, DURATIONS[idx], 15) at the CLICKED block position (context.getPos(), not the adjacent one); returns SUCCESS on both sides (RedstoneActivatorItem.java:69-79) [code]. Strength always 15.
- DURATIONS = {2, 20, 100} ticks; index stored in stack NBT "durationIndex", default index 1 = 20 ticks (RedstoneActivatorItem.java:31, 81-88) [code].
- onItemRightClick (air): plain click cycles index +1 wrapping 2->20->100->2; SNEAK click cycles -1 (reverse) (lines 49-67) [code]. (Wiki says "(shift)-right click": both directions exist.)
- Tooltip "Duration: %s ticks" (LANG tooltip.randomthings.redstone_activator.duration, RedstoneActivatorItem.java:44-47) [code]; item model override "duration_index" (lines 36-41) [code].
- Signal semantics: query override makes the signal's block position read as powered from all 6 sides (weak and strong) (RedstoneSignalHandler.getStrongPower lines 69-80; AsmHandler.java:388-421); it powers THAT block as if powered, e.g. door/lamp/TNT/wire at the position; it does not by itself make a neighbour block powered [code, derived].
- Signals are timed in game ticks via TickEvent.WorldTickEvent END, server only (J/RandomThings.java:576-586); ageing only while block is loaded; expiry triggers a neighbour update of the position (RedstoneSignalHandler.java:57-78); addSignal refuses (returns false, silent) if target block not loaded (line 47-49) [code]. Several signals stack independently; no dedupe [code].
- Persistence: signals saved in WorldSavedData id "randomthings_redstone_signals" per world (lines 24, 100-124) incl. age, so pulses survive save/reload [code].
- Client-side: coremod returns 0 for signals on client (AsmHandler.java:390,418), so client does not see the phantom power (only server-side logic matters) [code].
CONFIG: none.
DIVERGENCE: ported 1:1 per javadoc (RedstoneActivatorItem.java:22-29); timed state moved to per-world WorldSavedData instead of one global handler keyed by dimension (RedstoneSignalHandler.java:7-21 javadoc).
PLAYTEST-NEEDED: visual duration texture swap; door/TNT/lamp reaction; whether sneak-click on a block cycles or pulses (sneak+block goes to onItemUse, not onItemRightClick) [unverified].
EVIDENCE: checklist #286 PASS (5-batch-3-...-2.md:31) [playtest]; no dedicated automated test for the activator itself (Remote scenario covers the shared handler) [none].
WIKI-CLAIM-CHECK: "temporarily powers blocks when you right click them (doors open, tnt ignites, lamps/redstone on)" -> confirmed mechanism (phantom power at clicked block); "durations 2/20/100 ticks by (shift)-right-clicking into the air" -> confirmed (plain = forward, sneak = backward, default 20).

======================================================================
## redstone_remote (Redstone Remote)  [registry: item "redstone_remote" ModItems.java:392 maxStack 1; containers "redstone_remote_edit" and "redstone_remote_use"; packet RedstoneRemoteActivateMessage]
OBTAIN: shaped RD/recipes/redstone_remote.json "AAA/OSO/OOO": A=randomthings:redstone_activator, O=minecraft:obsidian, S=randomthings:stable_enderpearl; x1 [code]. No loot.
BEHAVIOR:
- Right-click in air (server): sneaking -> opens EDIT container; not sneaking -> opens USE container (RedstoneRemoteItem.java:34-66) [code]. Note: clicking on a block with onItemUse is not overridden -> block click behaves as vanilla fall-through to onItemRightClick [code, unverified].
- EDIT GUI (RedstoneRemoteEditContainer): 9 position-filter slots (y=18; only randomthings:position_filter, lines 38-40), a second row of 9 ghost "camo" slots (y=36), player inventory; the remote itself is shown but locked in its hotbar slot (lines 81-98); data stored in the held remote's own NBT keys "positions" and "camo" (lines 45, 58) [code]. Ghost camo slot: clicking with a stack copies 1 of it, empty hand clears, shift-click does nothing (slotClick lines 123-140) [code]. canInteractWith only while remote held in main or offhand (lines 118-120) [code].
- USE GUI (RedstoneRemoteUseScreen): one ItemSlotButton per non-empty position filter slot at x=5+20*i; icon = camo item if set else the Position Filter; tooltip = icon's anvil name, else the Position Filter's anvil name, else none (RedstoneRemoteUseScreen.java:53-71) [code]. Wiki says "ghost slot ... specifies the icon" -> confirmed; "rename Position Filters in anvil -> tooltip" -> confirmed (fallback after camo name).
- Press: RedstoneRemoteActivateMessage(slot) -> server finds remote in main/offhand, reads positions[slot], addSignal(player.world, target, 20, 15) (RedstoneRemoteActivateMessage.java:46-74) [code]. Duration fixed 20 ticks, strength 15 -> matches wiki "20 tick redstone pulse". Slot range 0-8 validated [code].
- Position Filter dimension ignored (getPosition returns x/y/z only, PositionFilterItem.java:84-94): the pulse is applied at those coords in the player's CURRENT dimension [code]. Unloaded target chunk -> addSignal returns false, no message to player [code].
CONFIG: none.
DIVERGENCE: camo row implemented (docs/project/scope-decisions.md:16 "Remote camo row implemented (reversed an earlier simplification)"); fix-log #287 text (5-batch-3-...-2.md:92) is stale and still says the camo row was dropped [doc]. Use screen background is a plain gray panel, same as 1.12.2 (RedstoneRemoteUseScreen.java:83-96 javadoc) [code].
PLAYTEST-NEEDED: visual icon rendering of buttons; edit-screen look.
EVIDENCE: scripted scenario redstone_remote_edit (docs/testing/desktop-helper/scn.py:1059-1100: binds two filters + camo diamond through the edit GUI, each button fires only its own target) [playtest-scripted]; checklist #287 PASS (5-batch-3-...-2.md:32) and #364 PASS [playtest]; docs/testing/covered-by-tests.md:59.
WIKI-CLAIM-CHECK: "9 positions" -> confirmed; "hold shift while right clicking opens a gui to insert position filters" -> confirmed; "plain right click without shift uses it; each filter = a button; 20 tick pulse" -> confirmed; "Remote/advanced version of the Activator" -> 3 Activators in the recipe.

======================================================================
## redstone_observer (Redstone Observer)  [registry: block+item+TE+container "redstone_observer"; item "redstone_tool" separately]
OBTAIN: shaped RD/recipes/redstone_observer.json "RQR/QEQ/RQR": R=minecraft:redstone, Q=minecraft:quartz, E=minecraft:ender_eye; x1 [code]. Drops self [code].
BEHAVIOR:
- Block ROCK hardness 2.0; canProvidePower true; getWeakPower/getStrongPower(side) return per-side values stored in TE (RedstoneObserverBlock.java:28,44-63) [code]. onBlockActivated opens GUI always (lines 65-79) [code] (same linking-reachability concern as Basic Interface).
- GUI: read-only status (Target X/Y/Z or "No Target") (LANG gui.randomthings.redstone_observer.*); checklist #95 PASS, #364 PASS [playtest].
- Mechanic: setTarget(pos) -> updateRedstoneState; for each of 6 sides stores strong = targetState.getStrongPower(world,target,f) and weak = world.getRedstonePower(target,f), then notifies own neighbours (RedstoneObserverTileEntity.java:58-65, 80-108) [code]. So it mirrors the ANALOG strength (0-15) and per-side values of the target, not just on/off [code].
- Refresh trigger: Forge NeighborNotifyEvent listener (RandomThings.java:~574 `RedstoneObserverTileEntity::notifyNeighbor`), updating any observer in the same world whose target equals the event pos (RedstoneObserverTileEntity.java:67-76). No ticking; same-world only [code].
- NOT persisted: only target saved (write lines 167-178); weak/strong maps are not saved or recomputed in read(), so after reload the output stays 0 until the target posts a neighbour update [code][unverified at runtime].
- Range: unlimited; no dimension/loaded check [code].
- Lines: while holding Redstone Tool, RedstoneObserverLineRenderer draws red lines (width 4) from observer to target for observers in the same world where observer OR target is within 20 blocks of the player (MAX_DISTANCE=20, lines 31-32, 62-82) [code]. Wiki phrase "as long as you are close to the block the observer is linked to" ~ confirmed (either endpoint within 20).
- No loot-gating/config.
CONFIG: none.
DIVERGENCE: no scope-decision. 1.12.2 observer block returned false when holding the tool (git show origin/1.12.2 BlockRedstoneObserver.java) - not in the port (OPEN QUESTIONS #1). 1.12.2 had OC driver DriverRedstoneObserver; not ported.
PLAYTEST-NEEDED: tool linking, line rendering, persistence after reload.
EVIDENCE: test behavior.redstone_observer.repeats_the_power_of_its_bound_target_block (J/debug/test/BehaviorTests.java:176-190; target set directly; lamp lights when target becomes redstone block, goes dark after) [test]; checklist #94/#95 PASS (3-batch-2-...md:56-57) [playtest].
WIKI-CLAIM-CHECK: "emits redstone based on whether the linked block emits redstone" -> confirmed, plus preserves strength per side; tool flow -> see Tool section.

======================================================================
## advanced_redstone_repeater (Advanced Redstone Repeater)  [registry: block+item+TE+container "advanced_redstone_repeater"]
OBTAIN: shaped RD/recipes/advanced_redstone_repeater.json "#X#/ISI": #=minecraft:redstone_torch, X=minecraft:redstone, S=minecraft:stone, I=tag forge:ingots/iron; x1 [code]. Drops self [code].
BEHAVIOR:
- Extends vanilla RepeaterBlock (J/block/AdvancedRedstoneRepeaterBlock.java:32), Material MISC, hardness 0, no collision; right-click opens GUI instead of vanilla delay cycling (lines 51-65) [code].
- GUI (AdvancedRedstoneRepeaterScreen): two rows "Turn On Delay" / "Turn Off Delay", each with "-" and "+" 10x10 buttons that change by exactly 1 per click, no shift/ctrl multipliers (AdvancedRedstoneRepeaterScreen.java:35-47, 55-62; LANG gui.randomthings.advanced_redstone_repeater.on/off) [code].
- Value range in code: clamp 0..120 ticks (AdvancedRedstoneRepeaterTileEntity.java:56-66); default 20 on / 20 off (lines 21-22); NBT turnOnDelay/turnOffDelay [code]. The class javadoc claims "2-10000 tick" (AdvancedRedstoneRepeaterBlock.java:21, TE :15) - contradicted by the clamp [code].
- Delay applies as scheduled-tick delay: shouldPower ? turnOnDelay : turnOffDelay (updateState lines 79-114; tick re-schedule for turn-off line 142-145); priorities copied from vanilla; locked behaviour via vanilla isLocked [code]. Delay 0 possible (min clamp 0) [code].
- Powered/locked blockstate from vanilla RepeaterBlock; placement/facing vanilla.
- Delay edits markDirty on the TE (line 59,65) but do not notify neighbours [code].
CONFIG: none.
DIVERGENCE: 1.12.2 range was 2..10000 with steps 1/10/100/1000 (TileEntityAdvancedRedstoneRepeater.java: Math.max(2,...), Math.min(10000,...); ContainerAdvancedRedstoneRepeater.java:26-65 steps of 1,10,100,1000 via shift/ctrl in GuiAdvancedRedstoneRepeater.java:55-56). Port: 0..120 step 1. Not listed in docs/project/scope-decisions.md (grep: no repeater entry) [code + git show origin/1.12.2].
PLAYTEST-NEEDED: actual delay feel (checklist rows exist).
EVIDENCE: checklist #80, #81, #81.1 PASS (3-batch-2-...md:21-23) [playtest]; no automated behaviour test [none].
WIKI-CLAIM-CHECK: "configurable turn on / turn off delay" -> confirmed (independent on/off values). (Wiki gives no range.)

======================================================================
## advanced_redstone_torch (Advanced Redstone Torch incl. wall torch)  [registry: blocks "advanced_redstone_torch" and "advanced_redstone_wall_torch"; ONE item "advanced_redstone_torch" (WallOrFloorItem, ModItems.java:244); TE "advanced_redstone_torch" shared by both blocks (ModTileEntityTypes.java:148); container "advanced_redstone_torch"]
OBTAIN: shaped RD/recipes/advanced_redstone_torch.json " X /X#X/ S ": X=minecraft:redstone, #=tag forge:ingots/iron, S=minecraft:stick; x1 [code] (matches wiki grid). Wall torch table drops the floor-torch item [code].
BEHAVIOR:
- Floor block extends TorchBlock; blockstate property color RED|GREEN (default RED); light 7 constant regardless of color (AdvancedRedstoneTorchBlock.java:47-53) [code]; sound WOOD, hardness 0.
- GREEN iff the block under it (floor) is powered (`worldIn.isSidePowered(pos.down(), DOWN)`, lines 138-141); wall torch: block behind it (lines 103-108). RED otherwise (inverter logic as vanilla: powered base -> green; green means "powered" per wiki) [code]. Update via scheduled tick 2 (tickRate, lines 95-98, 176-183) [code].
- Output: per color, strength from TE: RED -> signalStrengthRed (default 15), GREEN -> signalStrengthGreen (default 0) (TE lines 16-17; getWeakPower line 129-136) [code]. Floor torch emits weak power to all sides except UP; strong power only DOWN (lines 135, 188-191). Wall torch emits to all sides except the way it faces (WallTorch getWeakPower lines 113-120); strong only DOWN inherited [code].
- GUI: right-click opens container; labels "Green Strength" (top, drawn green) and "Red Strength" (bottom, drawn red) with -/+ 10x10 buttons by 1; range 0..15 (AdvancedRedstoneTorchContainer.java:78-95; Screen lines 39-75; LANG gui.randomthings.advanced_redstone_torch.gs/rs) [code]. After change, neighbours notified (container line 94) [code].
- Burnout like vanilla: 8 toggles within 60 ticks -> sound event 1502 and 160-tick re-schedule (AdvancedRedstoneTorchBlock.java:148-174, 229-255) [code].
- Particles: red or green redstone dust particles (animateTick lines 203-222; wall torch lines 80-101) [code].
- POSSIBLE BUG: setSignalStrengthRed/Green do NOT call markDirty (AdvancedRedstoneTorchTileEntity.java:64-71), so a changed value may not be saved unless the chunk is saved for another reason [code][unverified at runtime].
- onReplaced removes TE properly (fix noted at AdvancedRedstoneTorchBlock.java:111-113) [code].
CONFIG: none.
DIVERGENCE: none documented.
PLAYTEST-NEEDED: colors/textures, wall placement, persistence after changing strengths.
EVIDENCE: docs/project/wiki-feature-status.md:27 says DONE-UNTESTED (stale label); GUI list docs/archive/gui-test-list.md:19 marks screen legible; no automated test of power output [none].
WIKI-CLAIM-CHECK: "normal torch 15 or 0; advanced: configure strength for the two states freely" -> confirmed (0..15 each); "green while powered, red while unpowered" -> confirmed (GREEN = base block powered / torch 'on' state in inverter logic is RED output 15 by default; green state default output 0); recipe grid -> confirmed.

======================================================================
## analog_emitter (Analog Emitter)  [registry: block+item+TE+container "analog_emitter"]
OBTAIN: shaped RD/recipes/analog_emitter.json "TIR/III/RIT": T=minecraft:redstone_torch, I=tag forge:ingots/iron, R=minecraft:redstone; x1 [code]. Drops self [code].
BEHAVIOR:
- Block ROCK hardness 3/res 5; 6-way FACING property set from the placer's LOOK vector at placement (getStateForPlacement + onBlockPlacedBy; AnalogEmitterBlock.java:30-36, 87-101), i.e. the input ("circle") face is the face pointing away from the player in the look direction [code, derived].
- Input: neighborChanged -> TE.updateInput reads `world.getRedstonePower(pos.offset(facing), facing) > 0` on the FACING side; strength irrelevant (AnalogEmitterTileEntity.java:60-73) [code]. Output: weak and strong power = emitLevel on every side except FACING while input is on, else 0 (AnalogEmitterBlock.java:58-76; TE getOutput lines 48-51) [code]. Default emitLevel 1 (TE line 20) [code].
- GUI: "<" and ">" buttons (20x20) by 1, range 0..15, shows "Signal Strength" level (AnalogEmitterContainer.java:57-82; Screen lines 35-51; LANG gui.randomthings.analog_emitter.level) [code].
- NBT: only emitLevel saved; `powering` flag is transient (TE write lines 28-34) so it recomputes on next neighbour update [code].
CONFIG: none.
DIVERGENCE: 1.12.2 read input with `getRedstonePower(pos.offset(facing), facing.getOpposite())` (git show origin/1.12.2 TileEntityAnalogEmitter.java:45), the port passes `facing` (AnalogEmitterTileEntity.java:66). For direction-dependent sources (repeaters, torches, Sided Redstone) results may differ from the original [code vs git]. Not documented.
PLAYTEST-NEEDED: texture/orientation (which side is the circle).
EVIDENCE: test behavior.analog_emitter.outputs_configured_level_only_while_input_is_powered (J/debug/test/BehaviorTests.java:136-) [test]; checklist #71 PASS (3-batch-2-...md:7) [playtest].
WIKI-CLAIM-CHECK: "input side (circle) powered, strength irrelevant -> outputs configured strength on the other sides" -> confirmed (code above).

======================================================================
## sided_redstone (Sided Block of Redstone)  [registry: block+item "sided_redstone"]
OBTAIN: shaped RD/recipes/sided_redstone.json "GGR/GGR/GGR": G=minecraft:gunpowder, R=minecraft:redstone; x1 [code]. Drops self [code].
BEHAVIOR: Material.IRON, hardness 5/res 10, sound METAL; FACING 6-way set from placer look vector (SidedRedstoneBlock.java:22-27, 48-65). getWeakPower = 15 when `side == FACING.getOpposite()` else 0 (line 45); no getStrongPower override (stays vanilla 0) so it behaves as a weak source toward exactly one face (the neighbour at pos.offset(FACING), by the vanilla side convention) [code, derived]. canProvidePower true.
CONFIG: none. DIVERGENCE: none (same formula as 1.12.2 BlockSidedRedstone.java:47).
PLAYTEST-NEEDED: which face emits relative to the look direction.
EVIDENCE: checklist #50 PASS (2-batch-1-...md:27) [playtest]; no automated test.
WIKI-CLAIM-CHECK: "works like a normal Block of Redstone except only outputs on one side" -> confirmed (15 on one side; weak only).

======================================================================
## contact_button_lever (Contact Button & Lever)  [registry: blocks+items "contact_button", "contact_lever"; no TE]
OBTAIN: shaped RD/recipes/contact_button.json "SIS/SBS/SSS": S=tag forge:stone, I=minecraft:iron_bars, B=minecraft:stone_button; contact_lever.json same pattern with B=minecraft:lever; x1 each [code]. Drop self [code].
BEHAVIOR:
- Full-block (ROCK, hardness 1.5), 6-way FACING from placer look vector + POWERED property (ContactButtonBlock.java:26-34, 90-104; ContactLeverBlock.java:24-34). Powered => weak and strong power 15 to all sides (button lines 58-66; lever lines 58-68) [code].
- Trigger: not a click on themselves. A global RightClickBlock listener (J/RandomThings.java:~168-196): server side, MAIN_HAND only, for the clicked block scans its 6 neighbours for a Contact Button/Lever whose FACING == direction back to the clicked block; activates the first match, button checked before lever per direction in order of Direction.values() (loop `break`s after first activation) [code]. So right-clicking ANY block (stone, chest, etc.) in front of the contact block triggers it, regardless of clicked block type; event not cancelled, so normal block interaction still occurs [code]. Only one contact block activated per click [code].
- Button: sets POWERED, click sound, notifies neighbours, schedules tick 20 (1 s), then unpowers with another sound; ignored while already POWERED (ContactButtonBlock.java:73-80, 110-121) [code]. Lever: toggles POWERED each activation, sound pitch 0.6/0.5 (ContactLeverBlock.java:109-117) [code].
- Sounds UI_BUTTON_CLICK vol 0.3.
CONFIG: none. DIVERGENCE: ported from 1.12.2 RTEventHandler.playerInteract (comment RandomThings.java:~160-167) [code].
PLAYTEST-NEEDED: none beyond visuals (checklist #1-3, #34 PASS).
EVIDENCE: tests behavior.contact_button.right_clicking_the_block_it_faces_pulses_power_then_resets and behavior.contact_lever.right_click_toggles_and_stays (J/debug/test/MoreBehaviorTests.java:61-92; includes decoy button facing another way stays unpowered) [test]; checklist #1-3 (lever visuals), #34 (button) PASS [playtest].
WIKI-CLAIM-CHECK: "full blocks; react to the block in front being right clicked" -> confirmed; "act like normal buttons/levers" -> confirmed (button 20-tick pulse, lever toggles). Detail wiki omits: button pulse = 20 ticks, only main hand triggers, first match only.

======================================================================
## trigger_glass (Trigger Glass)  [registry: block+item "trigger_glass"; no TE]
OBTAIN: shaped RD/recipes/trigger_glass.json pattern ["L  ","QAQ","L  "] L=randomthings:lapis_glass, Q=randomthings:quartz_glass, A=minecraft:redstone; result count 4 [code]. Drops self [code].
BEHAVIOR:
- Glass-material block hardness 0.3, sound GLASS, TRANSLUCENT render layer, property TRIGGERED (TriggerGlassBlock.java:46, 64-69, 163-167) [code].
- When an untriggered glass receives neighborChanged and `worldIn.isBlockPowered(pos)` (any redstone power, server only) -> iterative BFS over the 6-connected touching Trigger Glass blocks starting at that block, setting TRIGGERED=true on each and scheduling a 60-tick (3 s) tick (lines 97-146) [code]. Max blocks per pulse = config TriggerGlassChainLimit (below). Tick sets TRIGGERED=false after 60 ticks (lines 148-155) [code].
- While TRIGGERED: collision shape empty (no collision), causesSuffocation false, material swapped to Material.PLANTS so FallingBlock.canFallThrough treats it as passable (sand/gravel fall through) (lines 71-89) [code]. Not replaceable by placement (isReplaceable false, lines 91-95) [code].
- Edge: after the 60 ticks the glass relocks even if power persists; re-trigger needs a new neighbour update [code, derived].
CONFIG: TriggerGlassChainLimit (RTConfig key "TriggerGlassChainLimit", section [numbers]), default 20, range 0..MAX, 0 = unlimited (J/config/RTConfig.java:36, 75) [code].
DIVERGENCE: chain limit is port-specific; 1.12.2 had no limit (RTConfig comment line 75; docs/project/scope-decisions.md:16 "Trigger Glass chain limit config (default 20)") [doc]. Chain implemented as BFS instead of recursive neighbourChanged [code]. Sand fall-through via getMaterial override, no coremod (TriggerGlassBlock.java:26-43; wiki-feature-status.md:122 says "clean Block.canFallThrough override").
PLAYTEST-NEEDED: translucent look when triggered ("visibly more translucent", checklist #18).
EVIDENCE: tests checklist.trigger_glass.powered_is_passable_and_unsuffocating_and_solid_again_about_60_ticks_after_the_power_goes (J/debug/test/ChecklistTests.java:129), ...sand_falls_through_while_triggered_and_stops_on_it_when_not (:150), ...chain_limit_config_caps_how_many_touching_blocks_trigger (:163-195) [test]; checklist #15-20 PASS (1-priority-...md:22-27) [playtest]; #355 config row (5-batch-3-...-3.md:69).
WIKI-CLAIM-CHECK: "loses collision for 3 seconds" -> confirmed (60 ticks); "adjacent Trigger Glass also activated so powering one is enough" -> confirmed but capped at 20 blocks by default (config) - wiki omits cap.

======================================================================
UNMAPPED:
- Registry "redstone_remote_edit" and "redstone_remote_use" containers (ModContainerTypes.java:109-110): belong to redstone_remote (no separate page).
- Block "advanced_redstone_wall_torch" and "basic_redstone_interface" have no separate wiki page ids: they map to advanced_redstone_torch and redstone_interface respectively.
- Item "redstone_tool" (ModItems.java:313): no wiki page of its own in this group (wiki redstone_observer/redstone_interface mention it as "Redstone Tool").
- Handler RedstoneSignalHandler/RedstoneSignal (J/handler/redstonesignal): backs redstone_activator and redstone_remote; no page.
- Wiki claims with no code: OpenComputers component "basic_redstone_interface" (setTarget/getTarget) on redstone_interface.
- Out of group but sharing mechanics: spectre_coil_redstone / spectre_charger_redstone / divining_rod_redstone recipes (not covered here).

OPEN QUESTIONS:
1. Redstone Tool reachability (HIGH): BasicRedstoneInterfaceBlock.onBlockActivated (:60-71) and RedstoneObserverBlock.onBlockActivated (:65-79) always open the GUI and return true, with no "holding Redstone Tool -> return false" check that 1.12.2 had (git show origin/1.12.2 BlockRedstoneObserver.java / BlockBasicRedstoneInterface.java). In 1.14.4's interaction order the block's onBlockActivated runs before Item.onItemUse unless the player sneaks, so a plain right-click with the tool may open the GUI instead of linking. No automated test or scripted scenario links through the tool (tests call setTarget; scenario uses /data merge); checklist rows #95/#283 are PASS but the PASS detail doesn't record whether the player sneaked. Needs a playtest: right-click Observer with Redstone Tool without sneaking.
2. Advanced Redstone Interface reload (HIGH): targets set may be empty after reload because refreshTargets() in read() exits when world==null (AdvancedRedstoneInterfaceTileEntity.java:60-63,131-136) and deserializeNBT fires no onContentsChanged; no onLoad override. Needs a save/reload playtest with filters inserted.
3. Redstone Observer power values are not saved/recomputed on load (RedstoneObserverTileEntity.java:166-208) - does the output come back after reload without a target-side update?
4. Advanced Redstone Torch strengths: no markDirty on set (AdvancedRedstoneTorchTileEntity.java:64-71) - do edited strengths persist after quit/reload?
5. Advanced Redstone Repeater range: code clamps 0..120 step 1 while 1.12.2 was 2..10000 with 1/10/100/1000 steps; confirm intended (no scope-decision entry).
6. Analog Emitter passes `facing` rather than 1.12.2's `facing.getOpposite()` into getRedstonePower (TE:66) - confirm port is intended/correct for directional sources.
7. Position Filter dimension is ignored by Advanced Interface and Remote (coords applied in current/own world); the Redstone Interface wiki page says "does not work across dimensions" - consistent only by accident.
8. Redstone Interface connection lines are drawn only for Observers (RedstoneObserverLineRenderer), wiki says they appear for the interface too.
9. Redstone Activator: sneak-right-click ON a block goes to onItemUse (sends a pulse) rather than cycling duration; only air clicks cycle - confirm this matches wiki wording "into the air".
