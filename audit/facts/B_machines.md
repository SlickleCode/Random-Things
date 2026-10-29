# B_machines fact sheet (DETECTORS / INTERFACES / MACHINES) - 1.14.4 baseline

Paths are relative to MOD (mod1144). J = src/main/java/lumien/randomthings, R = src/main/resources, T = J/debug/test,
CA = docs/testing/checklist-archive (PASS rows there count as [test] per brief; open TESTING_CHECKLIST.md rows are untested).
1.12.2 branch (origin/1.12.2) WAS fetched and readable; used only where stated.
No OpenComputers code exists anywhere in J (grep "opencomputers" -i over J and R: only docs hits). Third-party compat dropped:
docs/project/scope-decisions.md:5-6.
Common: every block below has a block loot table randomthings:<id> dropping itself (R/data/randomthings/loot_tables/blocks/<id>.json, all 20 present), a
BlockItem registered in the RT creative group (J/item/ModItems.java:431-437 registerItemForBlock; per-block lines cited below).
Block-entity registry names equal the block names (J/tileentity/ModTileEntityTypes.java:150-184). Container types (GUI) with same name:
J/container/ModContainerTypes.java:100-125.

## chat_detector (Chat Detector)  [registry: block+item+TE+container randomthings:chat_detector; ModBlocks.java:366, ModItems.java:310, ModTileEntityTypes.java:157, ModContainerTypes.java:105]
OBTAIN: shaped SRS/RTR/SRS; S=tag forge:stone, R=minecraft:repeater, T=tag forge:dyes/red; 1x (R/data/randomthings/recipes/chat_detector.json) [code]. No loot/worldgen. Not creative-only.
BEHAVIOR:
- On placement by a ServerPlayerEntity the TE stores the placer UUID (J/block/ChatDetectorBlock.java:56-67). Placed by non-player (dispenser) = no owner, never matches [code]. Owner is not changeable afterwards (no GUI/item to rebind) [code].
- Right-click opens GUI for ANY player (no owner check) (ChatDetectorBlock.java:70-88) [code]. GUI: one text field (max 256 chars, ChatDetectorScreen.java:38) + one toggle button "Hide Message: Yes/No" (lang en_us.json:297-299). Text syncs every change/Enter/close (ChatDetectorScreen.java:63-68) [code][test: CA/5-batch-3-...-3.md row 360 not in table; CA/3-batch-2 row 93 PASS "confirmed working by user 2026-10-04"].
- Matching: ServerChatEvent; message must be EXACTLY equal (String.equals, case-sensitive, whole message) to stored text AND sender UUID == stored owner UUID (J/tileentity/ChatDetectorTileEntity.java:110-123) [code][test: T/RemainingBehaviorTests.java:109,128].
- Output: on match sets POWERED=true for 20 ticks then false (ChatDetectorTileEntity.java:92-98, 42-57); block provides weak AND strong power 15 on all sides while powered (ChatDetectorBlock.java:90-108) [code].
- Consume mode (default off): returns true -> event cancelled so message is not shown in chat (ChatDetectorTileEntity.java:123; RandomThings.java:550-572) [code][test: RemainingBehaviorTests.java:109].
- Order: Floo chat handler runs first and returns early if it handled the message, so detectors are skipped for Floo-fireplace chat (RandomThings.java:551-553, 736-746) [code].
- All detectors checked each message (global registry WeakSets) (RandomThings.java:557-567) [code]. Hardness 2.0, Material.ROCK (ChatDetectorBlock.java:32).
- NBT: consume, chatMessage, playerUUID, pulsing, pulsingCounter (ChatDetectorTileEntity.java:127-160) [code].
CONFIG: none.
DIVERGENCE: none recorded. CA/3-batch-2 row 93 was a prior BUGGY (GUI-close) now PASS.
PLAYTEST-NEEDED: none beyond CA row 93.
PARITY: none noted.
WIKI-CLAIM-CHECK: "outputs a redstone signal when the player it was placed by enters something in the chat" -> confirmed (ChatDetectorTileEntity.java:110-122). "default: message still displayed; mode in gui consumes" -> confirmed (consume default false, :31,:123). Wiki omits: message must be an exact full-string match; signal is a fixed 20-tick pulse; owner fixed at placement.

## global_chat_detector (Global Chat Detector; wiki folds it into chat_detector page)  [registry: randomthings:global_chat_detector block/item/TE/container; ModBlocks.java:377, ModItems.java:324, ModTileEntityTypes.java:167, ModContainerTypes.java:116]
OBTAIN: shaped "T  "/"RDR"/"R  "; T=minecraft:redstone_torch, R=minecraft:redstone, D=randomthings:chat_detector; 1x (recipes/global_chat_detector.json) [code].
BEHAVIOR:
- No owner link: ANY player's exact-match message pulses (GlobalChatDetectorTileEntity.java:121-128) [code][test: RemainingBehaviorTests.java:144].
- Consume (button same as chat detector) only returns true if the SENDER'S UUID equals the UUID of an ID Card (IdCardItem) in the block's 9-slot whitelist (GlobalChatDetectorTileEntity.java:130-150; slots accept only IdCardItem :40-53) [code][test: RemainingBehaviorTests.java:144-160, CA/3-batch-2 row 116 PASS].
- Pulse regardless of consume/whitelist; 20-tick POWERED pulse (:110-116) [code]. Whitelist cards dropped when block removed (GlobalChatDetectorBlock.java:57-79) [code].
- Class javadoc says "or be a server operator" (GlobalChatDetectorTileEntity.java:27-28) but NO op bypass is coded; scope-decisions.md:12 + CA row 117 ("removed per request") confirm deliberate [code].
- GUI: text field (127px, max 256) at top, consume toggle, 9 ID-card slots, player inventory (GlobalChatDetectorContainer.java:44; GlobalChatDetectorScreen.java:37,44). Hardness 2.0 (GlobalChatDetectorBlock.java:34).
CONFIG: none.
DIVERGENCE: no operator bypass for consume (scope-decisions.md:12; CA row 117).
PLAYTEST-NEEDED: none.
PARITY: none.
WIKI-CLAIM-CHECK: "Global Chat Detector reacts to chat messages from any player; consuming requires an ID Card of the player" -> confirmed (GlobalChatDetectorTileEntity.java:121-150).

## entity_detector (Entity Detector)  [registry: randomthings:entity_detector block/item/TE/container; ModBlocks.java:372, ModItems.java:316, ModTileEntityTypes.java:162, ModContainerTypes.java:112]
OBTAIN: shaped SRS/EPE/SRS; S=forge:stone, R=redstone_torch, E=ender_pearl, P=stone_pressure_plate; 1x (recipes/entity_detector.json) [code].
BEHAVIOR:
- Ticks every server tick; scans AABB of block expanded by rangeX/Y/Z around the block (EntityDetectorTileEntity.java:179-218) [code][test: UtilityBehaviorTests.java:53,73; CA/3-batch-2 rows 104-107 PASS].
- Radius per axis default 5, clamped 0..10 (:55,:59-61,:140-143); GUI has -/+ button per axis (EntityDetectorScreen.java:37-49; EntityDetectorContainer.java:139-176) [code].
- Filters cycle ALL, LIVING, ANIMAL(AnimalEntity), MONSTER(MonsterEntity), PLAYER, ITEMS(ItemEntity), CUSTOM (:43-53, cycleFilter :145-150). Default ALL. No filter state "off".
- CUSTOM: uses the 1 filter slot item if it implements IEntityFilterItem (implemented by EntityFilterItem and IdCardItem: J/item/EntityFilterItem.java, J/item/IdCardItem.java); empty/non-filter slot => no entity ever matches, output = invert (:201-213) [code].
- Invert: powered = invert XOR (any matching entity in box) (:212,:217). So inverting PLAYER powers when NO player is in range (not "any non-player entity in range") [code].
- Output: boolean; weak power 15 all sides when powered; strong power 15 only if "strong output" toggle on (default off) (EntityDetectorBlock.java:45-65); on state change notifies neighbours (+ their neighbours when strong) (:166-177) [code]. Wiki omits the Weak/Strong toggle (lang en_us.json:317-318).
- Drops the filter item when removed (EntityDetectorBlock.java:68-83). Hardness 1.5 (:29).
- NBT: powered, rangeX/Y/Z, filter, invert, strongOutput, inventory (:220-250).
CONFIG: none.
DIVERGENCE: ANIMAL/MONSTER filters use AnimalEntity/MonsterEntity instead of 1.12.2 IAnimals/IMob (class javadoc EntityDetectorTileEntity.java:28-40). 1.12.2 logic otherwise matches (checked origin/1.12.2 TileEntityEntityDetector: same ranges/invert formula).
PLAYTEST-NEEDED: none.
PARITY: none.
WIKI-CLAIM-CHECK: "invert Players -> emits if any entity that is NOT a player is inside radius" -> contradicted: code powers when there is no PLAYER in range (:212,:217); non-player entities are irrelevant. "radius & type configurable" -> confirmed. "Filter Custom uses Entity Filter in its slot" -> confirmed (:201-211).

## online_detector (Online Detector)  [registry: randomthings:online_detector block/item/TE/container; ModBlocks.java:358, ModItems.java:304, ModTileEntityTypes.java:151, ModContainerTypes.java:101]
OBTAIN: shaped SRS/RTR/SRS; S=forge:stone, R=repeater, T=forge:dyes/blue; 1x (recipes/online_detector.json) [code].
BEHAVIOR:
- GUI: single text field "Player Username" (lang en_us.json:281); saved on change/close (container handle id 0 OnlineDetectorContainer.java:52-68) [code][test CA row 360 not shown; CA/3-batch-2 row 75 PASS].
- Every 20 game ticks (getGameTime()%20==0) server-side: online = username non-empty AND server.getPlayerList().getPlayerByUsername(username) != null; sets block-state POWERED accordingly (OnlineDetectorTileEntity.java:56-69) [code][test: RemainingBehaviorTests.java:82,90 (skipped on fake-player envs: docs/project/version-notes.md:106)]. Case-sensitivity of getPlayerByUsername: [unverified] (vanilla method, source not in repo).
- Power: weak and strong 15 on all sides while POWERED (OnlineDetectorBlock.java:55-72). Hardness 2.0 (:31). playerOnline field not persisted (:25 field, not in write()) => re-evaluated within 1 s after load [code].
- Detects only usernames (not UUID) so a renamed player is not followed [code].
CONFIG: none.
DIVERGENCE: no OpenComputers component (compat dropped, scope-decisions.md:5-6).
PLAYTEST-NEEDED: none.
PARITY: none.
WIKI-CLAIM-CHECK: "emits redstone while specified player is online" -> confirmed. "When Open Computers is installed provides component online_detector (isPlayerOnline, getPlayerList)" -> not-in-code (no OC integration anywhere in J/R).

## notification_interface (Notification Interface)  [registry: randomthings:notification_interface block/item/TE/container; ModBlocks.java:376, ModItems.java:323, ModTileEntityTypes.java:166, ModContainerTypes.java:115]
OBTAIN: shaped SPS/PQP/SPS; S=minecraft:stone, P=paper, Q=quartz; 1x (recipes/notification_interface.json) [code].
BEHAVIOR:
- Owner = placer UUID set in onBlockPlacedBy (NotificationInterfaceBlock.java:44-57) [code].
- Fires on redstone rising edge (isBlockPowered false->true via neighborChanged) (NotificationInterfaceBlock.java:58-67; TE updatePowerState NotificationInterfaceTileEntity.java:77-87). `powered` not persisted => a load while powered can re-fire on next neighbour update [code].
- On fire: if owner is online, sends NotificationMessage(title, description, icon stack) to that one player only; client shows a Toast (TE :90-107; network/messages/NotificationMessage.java:55-57) [code][test CA/3-batch-2 row 114 PASS].
- GUI: Title field (max 64 chars), Description field (max 256) (NotificationInterfaceScreen.java:40,45), icon "ghost" slot: clicking with an item copies 1 as icon, empty-hand click clears; shift-click does nothing (NotificationInterfaceContainer.java:40-50,82-100; CA/5-batch-3-...-3 row 363 PASS). Defaults "Title"/"Description" (TE :27-28).
- Icon is an ItemStack (1.14.4); 1.12.2 used name+meta. Block removal drops the ghost icon stack (NotificationInterfaceBlock.java:79) => can duplicate any item (copy of an item the player holds) [code] - OBSERVATION, see OPEN QUESTIONS.
- Hardness 2.0 (NotificationInterfaceBlock.java:29).
CONFIG: none.
DIVERGENCE: icon chosen via ghost slot not name/meta (1.12.2 OC signature notify(title,desc,iconName,iconMeta)); no OC.
PLAYTEST-NEEDED: toast look/duration (NotificationToast client rendering).
PARITY: none.
WIKI-CLAIM-CHECK: "send yourself toast notifications with customizable title, description & icon, activated by redstone" -> confirmed. "OC component notification_interface notify(...)" -> not-in-code.

## player_interface (Player Interface)  [registry: randomthings:player_interface block/item/TE (no container); ModBlocks.java:364, ModItems.java:308, ModTileEntityTypes.java:155]
OBTAIN: shaped OCO/ONO/OSO; O=obsidian, C=ender_chest, N=nether_star, S=randomthings:stable_enderpearl; 1x (recipes/player_interface.json) [code].
BEHAVIOR:
- No GUI. Links to the placer's UUID (PlayerInterfaceBlock.java:41-52). Exposes the linked player's inventory as an IItemHandler capability only while that player is ONLINE (else EmptyHandler) (PlayerInterfaceTileEntity.java:60-80) [code][test: CA/3-batch-2 row 89 PASS].
- Per side (absolute world directions, block is not rotatable): UP = armor slots 36-39; DOWN = hotbar 0-8; NORTH = offhand (slot 40); EAST/SOUTH/WEST = main inventory 9-35 (PlayerInterfaceTileEntity.java:85-100) [code]. Armor slots only accept matching armor type (SlotValidatingInventory :150-170; fix recorded in CA row 89).
- Hardness 4.0, sound stone (PlayerInterfaceBlock.java:23). Wiki says "4 sides: Hotbar, Storage, Armor, Shield".
CONFIG: none.
DIVERGENCE: none documented.
PLAYTEST-NEEDED: none.
PARITY: none.
WIKI-CLAIM-CHECK: "access inventory of your player as if it was a chest; Hotbar, Storage, Armor, Shield" -> confirmed as capability sides (down/side/up/north). "Creative Player Interface provides an OC component to set target player" -> not-in-code (see creative_player_interface).

## creative_player_interface (Creative Player Interface)  [registry: randomthings:creative_player_interface block/item/TE; ModBlocks.java:394, ModItems.java:380, ModTileEntityTypes.java:176]
OBTAIN: no recipe file (none in R/data/.../recipes); only the RT creative tab entry (registerItemForBlock, ModItems.java:380) [code]. CA/5-batch-3-...-1 row 244: "creative-menu-only (no survival recipe, matching 1.12.2)".
BEHAVIOR: identical to Player Interface (extends PlayerInterfaceBlock; TE extends PlayerInterfaceTileEntity, no extra behaviour) (J/block/CreativePlayerInterfaceBlock.java:15-19; J/tileentity/CreativePlayerInterfaceTileEntity.java:8-13). Still binds to the placer; no way to set another player (no name/uuid API, no OC) [code]. Breakable at normal hardness 4.0 (inherited) [code].
CONFIG: none. DIVERGENCE: OC component dropped. PLAYTEST-NEEDED: none (open row 244 in TESTING_CHECKLIST.md untested).
WIKI-CLAIM-CHECK: all five OC functions (getPlayerName/UUID, setPlayerName/UUID, isCurrentlyConnected) -> not-in-code.

## inventory_tester (Inventory Tester)  [registry: randomthings:inventory_tester block/item/TE/container; ModBlocks.java:365, ModItems.java:309, ModTileEntityTypes.java:156, ModContainerTypes.java:104]
OBTAIN: shaped "S  "/"SCS"/"I  "; S=stone, C=comparator, I=chest; output count 2 (recipes/inventory_tester.json) [code].
BEHAVIOR:
- Thin nub block (shape 4x1x4 px), no collision; FACING = face clicked when placing, so it points away from the inventory it sits on (InventoryTesterBlock.java:39-50,108-111). Can be placed anywhere (original popped off without an inventory) (class javadoc :28-36) [code].
- GUI: 1 item slot + invert button ("Signal: Normal/Inverted", lang en_us.json:295-296) (InventoryTesterContainer.java:39-54). The slot is a real slot (stack stays in TE).
- Every 2 ticks: simulate ItemHandlerHelper.insertItemStacked(target handler on side facing.opposite, stack as stored incl. its count) and signal = (remainder empty) XOR invert (InventoryTesterTileEntity.java:74-114) [code][test: CA/3-batch-2 row 90 PASS]. Whole stack must fit.
- If the slot is empty OR no TE/item handler is behind it the method returns early WITHOUT updating emitRedstone, so last signal state is retained (:83-96) [code].
- Output: weak power 15 on every side while signal true (InventoryTesterBlock.java:121-125); no strong power [code]. Hardness 0.3.
- Slot contents are NOT dropped when the block is broken (no onReplaced in InventoryTesterBlock) [code].
CONFIG: none.
DIVERGENCE: placement validation / auto-pop removed (InventoryTesterBlock.java:28-36).
PLAYTEST-NEEDED: none.
PARITY: none.
WIKI-CLAIM-CHECK: "continuously simulates inserting the ItemStack specified in gui... emits redstone while successful; invert button; no items actually inserted" -> confirmed (:104-114). Wiki omits that it only updates every 2 ticks and keeps old state when slot empty/no inventory.

## igniter (Igniter)  [registry: randomthings:igniter block/item/TE/container; ModBlocks.java:357, ModItems.java:303, ModTileEntityTypes.java:150, ModContainerTypes.java:100]
OBTAIN: shaped NCC/NFC/NCC; N=netherrack, C=forge:cobblestone, F=flint_and_steel; 1x (recipes/igniter.json) [code].
BEHAVIOR:
- FACING = player look direction on placement (6-way) (IgniterBlock.java:70-87); output side is facing.
- Redstone rising edge (isBlockPowered, via neighborChanged) -> if front block isAir AND vanilla fire valid there: plays flint_and_steel sound and places vanilla minecraft:fire (IgniterTileEntity.java:60-110) [code][test: T/BehaviorTests.java:110,121; CA/3-batch-2 rows 72,73 PASS]. It ignites only AIR positions (cannot light TNT/campfire-type targets directly) [code].
- Falling edge: in TOGGLE mode removes the front block only if it is minecraft:fire (not Blazing Fire) (:84-91) [code].
- Modes: ONLY TOGGLE (default) and IGNITE (enum :26-29); GUI button toggles between them (IgniterContainer.java:58-71; lang en_us.json:279-280). IGNITE leaves fire when unpowered.
- isFireSource(side == facing) so fire in front persists (IgniterBlock.java:60-63) [code].
- `powered` not persisted. Hardness 1.5 (:35). Does NOT use randomthings:blazing_fire (not relevant).
CONFIG: none.
DIVERGENCE: "Keep Ignited" mode REMOVED "per request" (CA/3-batch-2 row 74; docs/project/wiki-feature-status.md row Igniter). 1.12.2 had TOGGLE, IGNITE, KEEP_IGNITED (origin/1.12.2 TileEntityIgniter.java enum).
PLAYTEST-NEEDED: none.
PARITY: none.
WIKI-CLAIM-CHECK: "3 modes ... Keep Ignited: ignite front block and keep reigniting on block updates" -> contradicted (only 2 modes; IgniterTileEntity.java:28). "Toggle Fire default: ignite when powered, extinguish when unpowered" -> confirmed. "ignites the block in front" -> partly: it places fire into an air block in front (:68).

## iron_dropper (Iron Dropper)  [registry: randomthings:iron_dropper block/item/TE/container; ModBlocks.java:362, ModItems.java:307, ModTileEntityTypes.java:154, ModContainerTypes.java:103]
OBTAIN: shaped III/II_/IRI (row2 "II "); I=forge:ingots/iron, R=redstone; 1x (recipes/iron_dropper.json) [code].
BEHAVIOR:
- 6-way FACING = getNearestLookingDirection (IronDropperBlock.java:62), Material.IRON, hardness 3.5 (:36). 9-slot inventory, drops contents on break (IronDropperBlock.java:77-94) [test CA row 87 PASS]; item handler capability exposed on all sides (IronDropperTileEntity.java:80-90) so hoppers can fill it.
- GUI (4 buttons): Redstone mode (PULSE / REPEAT_POWERED; default REPEAT_POWERED, :53), Pickup delay (NONE / 5 ticks / 20 ticks; default 5, :54, :283), Motion (Straight/Random; default straight), Effects (None/Sound/Particle/Sound+Particle; default None) (IronDropperContainer.java:141-169; lang en_us.json:284-294) [code][test CA rows 82-85 PASS].
- PULSE: drops one item per rising redstone edge (:186-190). REPEAT_POWERED: while powered drops one item every 4 ticks (dropCounter%4, :198) [test BehaviorTests.java:57,78].
- Each drop moves exactly 1 item, taken from the first non-empty slot. If the block in front has an item handler, the item is inserted into it (stays if it does not fit); otherwise spawns an ItemEntity at 0.7 blocks out with motion: straight = fixed 0.25 speed + small deterministic offset, random = 0.2-0.3 +gaussian jitter (:228-300) [code][test BehaviorTests.java:96].
- Effects: Sound = world event 1000 (dispenser click), Particle = event 2000 (dispenser smoke) (:250-262) [code].
- `powered` not persisted (REPEAT mode resumes at next neighbour update). 
CONFIG: none.
DIVERGENCE: "Repeat" (non-powered-gated) redstone mode removed per request (CA row 86).
PLAYTEST-NEEDED: exact item velocity/"perfectly straight" feel on Superlubricent ice.
PARITY: none.
WIKI-CLAIM-CHECK: "change pickup delay, perfectly-straight, sound/particles, reaction to redstone" -> confirmed (4 toggles). Wiki does not list the discrete values (None/5/20 ticks, Pulse/Repeat Powered).

## item_collector (Item Collector)  [registry: randomthings:item_collector block/item/TE (no GUI/container); ModBlocks.java:359, ModItems.java:305, ModTileEntityTypes.java:152]
OBTAIN: shaped "E  "/"H  "/"OOO"; E=ender_pearl, H=hopper, O=obsidian; 1x (recipes/item_collector.json) [code].
BEHAVIOR:
- Thin non-colliding nub; FACING = face of the block it was placed on (points at the inventory) (ItemCollectorBlock.java:29-34,96-101). Hardness 0.3. Right-click does nothing (no onBlockActivated) [code]. "(shift) place on inventory" = normal vanilla sneak-place against containers.
- Adaptive tick rate: starts 20, -1 on success (min 1), +1 on idle (max 20); every cycle collects ALL ItemEntity (alive) in a cube pos-3..pos+3 (7x7x7, fixed RANGE=3) into the item handler of the TE behind it (side = facing) with insertItemStacked; fully-inserted entities removed, partials shrink (ItemCollectorTileEntity.java:25-100) [code][test: BehaviorTests.java:46; CA/3-batch-2 row 76 PASS].
- Ignores item pickup delay; no filter; no redstone control.
CONFIG: none. DIVERGENCE: none. PLAYTEST-NEEDED: none.
WIKI-CLAIM-CHECK: "hopper with a range, 3 block radius" -> confirmed (RANGE=3, cube not sphere).

## advanced_item_collector (Advanced Item Collector; wiki folds into item_collector)  [registry: randomthings:advanced_item_collector block/item/TE/container; ModBlocks.java:374, ModItems.java:321, ModTileEntityTypes.java:164, ModContainerTypes.java:113]
OBTAIN: shaped "R  "/"GIG"; R=redstone_torch, G=glowstone_dust, I=randomthings:item_collector; 1x (recipes/advanced_item_collector.json) [code].
BEHAVIOR:
- Same mechanics as Item Collector but cube pos-r..pos+r per axis with rangeX/Y/Z default 5, clamped 0..MAX_RANGE = 16 (AdvancedItemCollectorTileEntity.java:45,50,~84-95) [code][test CA row 112 PASS "per-axis radius"]. GUI shows X/Y/Z radius buttons (lang en_us.json:326-328).
- Filter: ONE example-item slot; if non-empty only items with ItemStack.areItemsEqual (same item+damage, ignores NBT/count) are collected; no blacklist mode (class javadoc :31-43; filter check line ~"areItemsEqual") [code]. Hardness 0.3, non-colliding. Drops filter item on break (AdvancedItemCollectorBlock.java:111-125).
CONFIG: none.
DIVERGENCE: 1.12.2 ItemFilter item (9 slots, whitelist/blacklist, meta/NBT/ore-dict toggles) replaced by single example slot (TE javadoc :31-43; item filter "WILL NOT MIGRATE", CA/6-not-implemented...md:8).
PLAYTEST-NEEDED: none.
WIKI-CLAIM-CHECK: "range configurable between 0 and 10 for each direction" -> contradicted: MAX_RANGE = 16 (AdvancedItemCollectorTileEntity.java:45). "insert an Item Filter to specify which items are / are not picked up" -> contradicted: no Item Filter item exists; single example slot, whitelist only (java:31-43; CA/6-not-implemented...md:8). GUI "right click" -> confirmed.

## block_breaker (Block Breaker)  [registry: randomthings:block_breaker block/item/TE (no GUI); ModBlocks.java:405, ModItems.java:385, ModTileEntityTypes.java:183]
OBTAIN: shaped CPC/CRC/CCC; C=forge:cobblestone, P=iron_pickaxe, R=redstone_torch; 1x (recipes/block_breaker.json) [code].
BEHAVIOR:
- 6-way FACING = getNearestLookingDirection (BlockBreakerBlock.java:50), hardness 3.5. No GUI.
- Mines block in front continuously unless powered: each tick adds state.getPlayerRelativeBlockHardness(fakePlayer holding Unbreakable iron pickaxe with Magnetic I) until >=1, then tryHarvestBlock (BlockBreakerTileEntity.java:82-86,132-146) [code][test T/BehaviorTests.java:155,166; CA/5-batch-3-...-2 rows 290,291 PASS]. Shows break-crack progress (sendBlockBreakProgress :134).
- Redstone: canMine = !isBlockPowered; powering stops/resets mining (:175-186) [test BehaviorTests.java:166].
- Drops: fake player's Magnetic pickaxe + MagneticLootModifier route drops into the fake player's inventory; then every inventory slot 1.. is inserted into the item handler of the block directly BEHIND (side=facing) and leftovers spawn in front-of-breaker position (:148-165) [code][test BehaviorTests.java:155; CA row 372 PASS]. The "chest behind" feature therefore DEPENDS on RTConfig MagneticEnchantment = true: with it false MagneticLootModifier returns early (MagneticLootModifier.java:41) and drops fall in the world as normal [code].
- Tool is an iron pickaxe with player-style hardness, so "can't break what an iron pickaxe can't": unbreakable (hardness<0) never; blocks needing a higher tier take longer and give no drop in vanilla rules (getPlayerRelativeBlockHardness /100 vs /30) but are still destroyed - [unverified] (vanilla source not in repo).
- Uses a Forge FakePlayer named RTBlockBreaker with a no-op net handler (:79-110).
CONFIG: MagneticEnchantment (RTConfig.java:63) affects drop routing (see above).
DIVERGENCE: 1.12.2's own ItemCatcher ASM replaced by Magnetic loot modifier (class javadoc :27-41; wiki-feature-status.md row Block Breaker).
PLAYTEST-NEEDED: none.
PARITY: none.
WIKI-CLAIM-CHECK: "acts as iron pickaxe: same speed" -> confirmed (fake player + iron pickaxe, :82,:132). "can't break blocks iron pickaxe can't" -> partly/[unverified] (see above). "inventory behind receives drops" -> confirmed (:152-165). "redstone signal disables it" -> confirmed (:175).

## block_destabilizer (Block Destabilizer)  [registry: randomthings:block_destabilizer block/item/TE/container; ModBlocks.java:406, ModItems.java:386, ModTileEntityTypes.java:184, ModContainerTypes.java:124]
OBTAIN: shaped ORO/SDS/ORO; O=obsidian, R=redstone, S=sand, D=diamond; 1x (recipes/block_destabilizer.json) [code].
BEHAVIOR:
- 6-way FACING (nearest looking dir) (BlockDestabilizerBlock.java:61), hardness 5.0 (:34). Rising redstone edge while IDLE starts (TE.java:105-110).
- Start block must be non-air, hardness>=0 and a normal cube (:130-137). BFS (one position checked per tick) over the 6 neighbours of every matching block; match = identical BlockState (or same Block when Fuzzy ON) AND isNormalCube (non-full blocks never match) (:166-205) [code][test T/MoreBehaviorTests.java:44; CA/5-batch-3-...-2 row 293 text].
- Limit: RTConfig.BlockDestabilizerLimit default 50 (0 = unlimited), stops search when set size >= limit (:170-173; RTConfig.java:74, section `numbers`) [test T/ChecklistTests.java:344].
- Then drops matched blocks one per tick, sorted by Y ascending then distance to the machine (comparator :209), as vanilla FallingBlockEntity with shouldDropItem=false (:235-239); blocks that have a tile entity are skipped (:235). So falling blocks that cannot land vanish w/o item (CA row 296).
- Lazy mode (GUI toggle): remembers positions that did NOT match ("invalidBlocks") and skips them next time, so only the previous shape can drop; Reset button clears memory; Fuzzy toggle. Lazy/Reset only changeable while IDLE (:67-92). GUI: 3 sprite buttons Lazy / Fuzzy / Reset (BlockDestabilizerScreen.java:35-60; lang en_us.json:361-365).
- State machine persists in NBT (:243-300).
CONFIG: numbers.BlockDestabilizerLimit (int, default 50, 0=unlimited) (RTConfig.java:73-74).
DIVERGENCE: only full cubes match/get dropped (user request 2026-09-28, TE javadoc :113-126; scope-decisions.md:11 describes it as "pass through non-full blocks"); falling-block cosmetic glow dropped (wiki-feature-status.md row Block Destabilizer); spawns vanilla FallingBlockEntity not EntityFallingBlockSpecial.
PLAYTEST-NEEDED: falling sequence feel; block landing/vanish behaviour.
PARITY: none.
WIKI-CLAIM-CHECK: "destabilize 50 (configurable) blocks, all connected and of same type" -> confirmed. "lazy mode remembers what blocks it dropped last time" -> partly: code remembers positions that were checked and did NOT match (:196-197), effect is equivalent (only previous shape can match? matches still need re-test; excluded set only prunes negatives) - treat as [unverified] semantic. "Fuzzy: block type not exact state" -> confirmed. "arrow button resets" -> a Reset button exists (icon untested).

## potion_vaporizer (Potion Vaporizer)  [registry: randomthings:potion_vaporizer block/item/TE/container; ModBlocks.java:370, ModItems.java:314, ModTileEntityTypes.java:161, ModContainerTypes.java:111]
OBTAIN: shaped CTC/IUI/CFC; C=forge:cobblestone, T=iron_trapdoor, I=forge:ingots/iron, U=cauldron, F=furnace; 1x (recipes/potion_vaporizer.json) [code].
BEHAVIOR:
- Material.IRON hardness 3.0; FACING = nearestLookingDirection.getOpposite() (PotionVaporizerBlock.java:31,57), i.e. front faces the player's far side; the room is searched starting at the block in front.
- Slots: 0 fuel (bottom, x80,y53), 1 potion input (left, x29,y17), 2 output (right, x131,y17; machine-only) (PotionVaporizerContainer.java:43-45; PotionVaporizerItemHandler.java:36-46). Fuel = anything AbstractFurnaceTileEntity.isFuel; potion slot accepts only minecraft:potion (NOT splash/lingering) whose FIRST effect is non-instant; output slot rejects all manual input [code][test CA/3-batch-2 rows 96-99.1 PASS].
- Room: incremental BFS (5 steps/tick until a room is known, then 2/tick), counts open positions; if more than 100 (checkCounter > 100) the area is rejected (room cleared) (PotionVaporizerTileEntity.java:50,76-80,284-289). "Open" = air OR any block with empty collision shape (torches, signs, buttons, flowers...) (:342) [code][test CA row 99.1].
- Potion tank: takes 1 potion from slot 1 when idle, output glass bottle +1 in slot 2 (stack up to 64) (:168-198). Uses only effects.get(0) of the potion; duration = that effect's duration in ticks.
- Fuel: when burn time is 0 and a potion is loaded and the room is non-empty, consumes 1 fuel item; burn time = furnace burn time map value (:134-152). Potion duration only counts down while fuel is burning and room is valid, so running out of fuel pauses the potion, nothing lost (:105-112,199-226).
- Effect: every tick for every living entity whose bounding box intersects a room block: if it lacks the effect or has < 3 ticks left, gives the effect 80 ticks (205 for Night Vision) at the potion's amplifier (:221-248) [code][test UtilityBehaviorTests.java:326].
- Particles: server-side coloured redstone-dust particle at each room block every 5 ticks while active (:256-275).
- NBT incl. affected blocks and inventory persisted (:306-345). Contents are NOT dropped when the block is broken (no onReplaced in PotionVaporizerBlock) [code]; 1.12.2 vaporizer breakBlock drop: not checked.
CONFIG: none.
DIVERGENCE: non-full blocks no longer end the flood fill (scope-decisions.md:11; TE javadoc :316-341); particle packet replaced by vanilla spawnParticle (class javadoc :36-45).
PLAYTEST-NEEDED: particle look.
PARITY: none.
WIKI-CLAIM-CHECK: "potions (Left slot), fuel (bottom slot), empty bottles to right slot" -> confirmed. "room volume up to 100 blocks; if larger or not sealed won't work" -> confirmed (:284). "A single potion lasts as long as drinking" -> confirmed (:174-176). "consumes fuel at furnace rate; pauses without fuel" -> confirmed (:134-152). "applies effects to any entity" -> LivingEntity only (:233).

## imbuing_station (Imbuing Station) + imbue_fire / imbue_poison / imbue_experience / imbue_wither (items) + imbue_* (effects)  [registry: block/item/TE/container randomthings:imbuing_station; ModBlocks.java:386, ModItems.java:351, ModTileEntityTypes.java:172, ModContainerTypes.java:121. Items randomthings:imbue_fire/_poison/_experience/_wither (ModItems.java:352-355, class J/item/ImbueItem.java); effects same four names (J/potion/ModEffects.java:27-30, class J/potion/ImbueEffect.java)]
OBTAIN:
- Station: shaped " W "/"VCV"/"LEL"; W=water_bucket, V=vine, C=terracotta, L=lily_pad, E=emerald; 1x (recipes/imbuing_station.json) [code].
- Imbue items made ONLY in the station (no crafting recipe, no loot) [code]. Recipes are hard-coded, NOT datapack JSON (RandomThings.java:712-721): center slot = water bottle (minecraft:potion, Potions.WATER); 3 unordered ingredient slots:
  Fire = coal + flint + blaze_powder; Poison = spider_eye + rotten_flesh + red_mushroom; Experience = randomthings:lesser_magic_bean + lapis_lazuli + glowstone_dust; Wither = wither_skeleton_skull + nether_brick + ghast_tear. Extra recipe: center cobblestone + water bottle + vine + bone_meal -> mossy_cobblestone [code][test T/MoreBehaviorTests.java:95,121; CA/5-batch-3-...-1 rows 169-173 (169 FIXED, 170-173 blank = untested manually)].
BEHAVIOR (station):
- 5 slots: 0-2 ingredients, 3 "to imbue"/center, 4 output (machine-only) (ImbuingStationItemHandler.java:19-25; ImbuingStationContainer.java:39-43).
- Every tick it looks up a matching recipe (exact item+NBT; all three ingredient slots must be used by the recipe's ingredients, empty slots allowed) and counts 200 ticks (IMBUING_LENGTH) of progress, then consumes 1 of each of slots 0-3 and adds the result to output (ImbuingStationTileEntity.java:25,47-106). Progress resets if the matched output changes (:52-56). Only one result per cycle; output stack merges when same item (canHandleOutput :76-88).
- Hardness 1.25 (ImbuingStationBlock.java:24). Contents NOT dropped when broken (no onReplaced) [code]; 1.12.2 BlockImbuingStation.breakBlock did drop contents (origin/1.12.2 line 35-45) -> flagged.
BEHAVIOR (imbue items/effects):
- Drink (32 ticks, DRINK anim) -> removes any other imbue effect, applies the effect for 60*20*20 = 24000 ticks (20 min) amplifier 0, no particles, icon shown; consumes item (not in creative) and returns glass bottle (ImbueItem.java:37,53-90) [test UtilityBehaviorTests.java:364; CA row 175 text, unchecked manual].
- ImbueEffect is a marker (EffectType.NEUTRAL) with colours orange/dark green/yellow/bright black (ModEffects.java:27-30).
- Hit effects (RandomThings.java:198-224): LivingHurtEvent with direct EntityDamageSource by a LivingEntity attacker (arrows/indirect excluded). Priority fire > wither > poison: Fire = setFire(10) (10 s); Wither = Wither amplifier 1 (II) for 100 ticks (5 s); Poison = Poison amplifier 1 (II) for 200 ticks (10 s). Works for any living attacker with the effect (not just players).
- Experience: on LivingExperienceDropEvent with attacking player having the effect: dropped = dropped + originalExperience, i.e. DOUBLE (+100%) (RandomThings.java:226-231) [code]; same formula in origin/1.12.2 RTEventHandler.livingExperience (line 683-689). CA row 179 text says "drops double XP".
CONFIG: none.
DIVERGENCE: ore-dictionary fallback in matching dropped; Collapse imbue not ported (ImbueItem javadoc); recipes in code like original (RandomThings.java:705-711).
PLAYTEST-NEEDED: station progress arrow/slot-fill animation (CA row 174, open in TESTING_CHECKLIST.md B); HUD icon appearance.
PARITY: none.
WIKI-CLAIM-CHECK: "Fire: sets enemies on fire for 10 seconds" -> confirmed (RandomThings.java:218). "Poison II 10 seconds" -> confirmed (:222). "Experience: 50% more experience" -> contradicted: code gives +100% (doubles) (:229). "Wither II 5 seconds" -> confirmed (:220). "only one imbue active; drinking another cancels first" -> confirmed (ImbueItem.java:62-66, clearImbues). "All imbues last 20 minutes" -> confirmed (:37). "4 imbues currently" -> confirmed. Wiki omits ingredient recipes and the 200-tick brew time.

## dyeing_machine (Dyeing Machine)  [registry: randomthings:dyeing_machine block/item/container; ModBlocks.java:419, ModItems.java:413, ModContainerTypes.java:125; NO tile entity]
OBTAIN: shaped " G "/"RCB"/" W "; G=green_dye, R=red_dye, B=blue_dye, W=white_wool, C=crafting_table; 1x (recipes/dyeing_machine.json) [code].
BEHAVIOR:
- Material.WOOD hardness 0.7 (DyeingMachineBlock.java:29). Stateless: right-click opens container (:32-49); no stored inventory; left-over ingredients are dropped at the player's feet when the GUI closes (DyeingMachineContainer.java:120-134) [test CA/5-batch-3-...-3 row 324 text].
- Slots: item slot (accepts anything that is NOT a BlockItem: DyeableSlot.java:16), dye slot (vanilla dye items only, DyeColor.getColor(stack)!=null: DyeSlot/DyeUtil), two result slots (recolour result tag "rtDye" int RGB; glint result tag "enchantmentColor" int RGB) (DyeingMachineContainer.java:50-58, 62-110). Taking a result consumes 1 of each occupied ingredient slot and returns 1 item (DyeCraftingSlot onTake).
- Item in left slot with NO dye: preview result strips "rtDye" and "enchantmentColor" tags (only if the item has tags) = removes dye (:96-112).
- Colour = DyeColor.getColorValue() of the dye (DyeUtil.getDyeColor). There is NO special case for Purple (it yields purple's own RGB, not the vanilla glint colour) nor for Black (black's own RGB 0x1D1D21-ish; "almost invisible" is an effect of the additive glint blend) [code; actual RGB value unverified].
- Rendering is coremod-driven (client): ItemRendererTransformer.js (R/transformer) hooks ItemRenderer.renderQuads -> AsmHandler.getColorFromItemStack (AsmHandler.java:469-476): ANY stack with "rtDye" overrides the quad colour even if the item has its own tint; and hooks renderItem + glint lambda -> ClientAsmHandler.enchantmentColorHook -> GlintColors.colorFor (asm/GlintColors.java:28-58; priority: fixed tints Redstone Tool/Spectre Key/Portkey, then enchantmentColor tag, then Spectre Sword/Escape Rope). ArmorLayerTransformer.js: armorColorHook (ClientAsmHandler.java:170-181) tints worn armor by rtDye; armorEnchantmentHook darkens glint colour to 70% (:59-70). Tests: T/UtilityBehaviorTests.java:258 (glint priority); HookProbe item_dye_applied/armor_dye_applied (open TESTING_CHECKLIST.md rows 321-323,390 = visual retest pending).
CONFIG: none. Coremods: ItemRendererTransformer, ArmorLayerTransformer (META-INF/coremods.json).
DIVERGENCE: shift-click quick-move off-by-3 bug of 1.12.2 fixed (container comment :140-150); isBlock restriction identical to 1.12.2 SlotDyeable (DyeableSlot javadoc).
PLAYTEST-NEEDED: all rendering (held/inventory/ground/worn tint, glint colours): open rows 321-323, 390.
PARITY: none.
WIKI-CLAIM-CHECK: "dye the enchantment glow & the items themselves" -> confirmed. "colouring of item itself only works for items that don't colour themselves already" -> contradicted by code: rtDye overrides the per-quad colour for any item incl. tinted ones (AsmHandler.java:469-476); BUT block items cannot be put in the slot (DyeableSlot.java:16, wiki omits). "remove dye: put item in left slot without dye" -> confirmed (:96-112). "purple dye gives default glow back" -> not-in-code (no purple special-case; DyeUtil.java). "Armor can be dyed and coloured when worn" -> confirmed (ArmorLayerTransformer.js, ClientAsmHandler.java:170). "Black Dye makes glow almost invisible" -> not special-cased; just black's RGB (unverified visually).

## fluid_display (Fluid Display)  [registry: randomthings:fluid_display block/item/TE; ModBlocks.java:392, ModItems.java:377, ModTileEntityTypes.java:175; renderer J/client/renderer/FluidDisplayTileEntityRenderer.java]
OBTAIN: shaped GGG/GBG/GGG; G=minecraft:glass (not a tag), B=glass_bottle; 1x (recipes/fluid_display.json) [code][test CA/5-batch-3-...-1 row 233 PASS].
BEHAVIOR:
- Glass material, hardness 0.3, INVISIBLE block model drawn by a TESR cube (FluidDisplayBlock.java:30,79-86,100-103).
- Right-click: if main-hand item has an item fluid handler: non-empty -> sets displayed fluid (copied as 1000 mB, with NBT) and syncs; EMPTY container (e.g. empty bucket) -> returns false, does nothing (FluidDisplayBlock.java:50-64). Otherwise (empty hand or any non-fluid item): plain click toggles still/flowing texture, sneak-click rotates 90 degrees (4 steps) (:66-72) [test CA rows 235,236 PASS; 234 FIXED]. Vanilla makes sneak+item skip block activation, so rotate effectively needs an empty hand.
- A freshly placed display has fluidStack null -> renders its own "empty" sprite (FluidDisplayTileEntityRenderer.java:47-55); only a TE loaded from NBT without a fluidStack key defaults to water (FluidDisplayTileEntity.java read ~line 115) [code].
- Tint = fluid attributes colour; still vs flowing texture from Forge FluidAttributes (renderer :58-66).
- Breaking drops itself (CA row 237). NBT stable across save/load (CA row 373, nbt.tile_entity.fluid_display test).
CONFIG: none. DIVERGENCE: TESR instead of baked ExtendedBlockState model (class javadoc).
PLAYTEST-NEEDED: fluid texture/colour correctness (open TESTING_CHECKLIST.md row 234).
WIKI-CLAIM-CHECK: "right click with any fluid container sets fluid" -> confirmed (needs a non-empty container). "right click with something else toggles flowing/stationary" -> confirmed. "shift right click with empty hand rotates" -> confirmed (sneak branch also works with any non-fluid item only if the game delivers the click; vanilla does not).

## biome_radar (Biome Radar) + biome_sensor recipe + biome_crystal  [registry: randomthings:biome_radar block/item/TE (no container); ModBlocks.java:379, ModItems.java:325, ModTileEntityTypes.java:168. Items randomthings:biome_sensor (plain Item, ModItems.java:215) and randomthings:biome_crystal (BiomeCrystalItem, stack size 1, ModItems.java:329)]
OBTAIN:
- Radar: shaped III/GSG/III; I=iron_ingot, G=glass, S=randomthings:biome_sensor (recipes/biome_radar.json). Sensor: shaped III/RCI/IRI; I=forge:ingots/iron, R=redstone, C=randomthings:biome_crystal (recipes/biome_sensor.json) [code].
- Biome Crystal has NO crafting recipe. Loot: if RTConfig.BiomeCrystal (default true) is on, EVERY minecraft chest loot table whose path starts with "chests/" gets an extra pool: 1 roll at 20% chance of one crystal tuned to a UNIFORM-random registered biome (LootHandler.java:69-74,87-100; RTConfig.java:57) [code][test CA/5-batch-3-...-3 row 335 text]. Crystal also in creative menu (RT tab) [code].
BEHAVIOR:
- Block: Material.IRON hardness 5.0, tinted by biome colour (BiomeRadarBlock.java:35-38, colorMultiplier). Breaking drops inserted crystal (onReplaced :41-55) [CA row 153].
- Interaction (BiomeRadarBlock.java:73-141): IDLE + no crystal + hand holding biome_crystal -> inserts 1; IDLE + crystal + EMPTY hand -> crystal is put into the hand slot; FINISHED + empty hand -> take crystal, state IDLE (documented divergence from 1.12.2, :118); FINISHED + paper -> consumes 1 paper and gives a Position Filter (generatePositionFilter, TE :258-266: position = spiral cell of searchCounter at the radar's own Y, dimension id stored) [test T/UtilityBehaviorTests.java:119,203,232].
- Antenna: iron bars at +1,+2 straight above, plus bars at (+-1,+2),(+-1,+3),(0,+2,+-1),(0,+3,+-1) = 10 bars total (BiomeRadarTileEntity.java:355-376). A powered radar only starts if antenna valid; every 60 ticks a non-idle radar with an invalid antenna drops to IDLE (:75-78).
- Search starts on a rising redstone edge (powered via getRedstonePowerFromNeighbors) when IDLE with a crystal and valid antenna; an Archimedean square-spiral walk centred on the radar, ring step 48 blocks, at radar's Y, 5 samples per tick, comparing world.getBiome(pos) == target biome; no maximum radius (searchCounter unbounded) (:83-125,148-184,252-285) [code][test UtilityBehaviorTests.java:203 incl. missing biome keeps searching]. Redstone off while searching => IDLE; FINISHED + redstone falls => IDLE.
- Particles (client): while SEARCHING coloured dust at the 4 antenna tips for up to 4 most-recent distinct biomes seen; when FINISHED all 4 tips in the target biome colour and a 5th particle at pos+3.1 (centre) (:128-153). Sync via BiomeRadarAntennaMessage within 64 blocks every 100 steps when colour changed (:116-118).
- Biome Crystal: NBT "biomeName" registry name; tooltip + name suffix "(Biome Name)" (BiomeCrystalItem.java:30-45).
CONFIG: BiomeCrystal (bool, default true) toggles crystal loot only (RTConfig.java:57).
DIVERGENCE: dust particles instead of bespoke smoke particle (TE javadoc :27-38); empty-hand crystal removal in FINISHED state (BiomeRadarBlock.java:118).
PLAYTEST-NEEDED: particle colours, spinning crystal render (open TESTING_CHECKLIST.md rows 389,149-153).
PARITY: none.
WIKI-CLAIM-CHECK: "Biome Crystals in Dungeon Chests" -> contradicted/broader: added to ALL vanilla chests/* tables at 20% (LootHandler.java:69-72); no dungeon-only restriction. "antenna made of Iron Bars on top; without antenna won't work" -> confirmed (10-bar shape, :355-376). "right-click with crystal inserts; empty hand takes out" -> confirmed (also works in FINISHED). "apply redstone to start search" -> confirmed (rising edge). "once found a fifth line of smoke appears in the middle" -> confirmed (:146-152). "right-click with paper to get position filter" -> confirmed (only when FINISHED, :131).

## rain_shield (Rain Shield)  [registry: randomthings:rain_shield block/item/TE; ModBlocks.java:400, ModItems.java:382, ModTileEntityTypes.java:180]
OBTAIN: shaped " F "/" B "/"NNN"; F=flint, B=blaze_rod, N=netherrack; 1x (recipes/rain_shield.json) [code].
BEHAVIOR:
- Thin candle shape (6..10 px), Material.ROCK hardness 2.0, needs a solid block below (isValidPosition) else breaks & drops on neighbour update (RainShieldBlock.java:47-72,~85-92) [CA/5-batch-3-...-1 row 257 PASS].
- active = NOT redstone-powered (updatePowerState, RainShieldTileEntity.java:60-71; set in onBlockAdded/neighborChanged) [code].
- Effect: coremod RainShieldTransformer.js wraps every ireturn of World.isRainingAt(BlockPos) with AsmHandler.overrideIsRainingAt: if originally raining and any active shield in same world is within horizontal distance < 80 (RAIN_SHIELD_RANGE) of the position -> false (Y ignored) (AsmHandler.java:241-256; TE.isInRange :43-54) [test T/CoremodTests.java:169]. This is the single source for rain wetting, snow/ice formation, fire extinguishing.
- Client visual: GameRendererTransformer.js redirects the World.getBiome call inside GameRenderer.renderRainSnow to AsmHandler.getBiomeForRainRender, which returns Biomes.DESERT (no precipitation) for columns in range (AsmHandler.java:279-289) [CA/5-batch-3-...-1 rows 258,261 FIXED; open TESTING_CHECKLIST.md row 261 retest]. RainShieldBlock class javadoc still says the visual half is not implemented - STALE (RainShieldBlock.java:30-38).
- Block particles: when raining, a ring of flame+smoke particles around the candle regardless of active/powered state (RainShieldBlock.java:~99-113) [CA row 260 PASS].
- Rain SOUND suppression: not found in code (no sound hook; rain sound/splash particles come from the same isRainingAt? not traced) [unverified].
CONFIG: NONE. Range is a hard-coded 80 blocks (AsmHandler.java:241). RTConfig has no rain key (grep "rain" -i RTConfig.java: no hits).
DIVERGENCE: range config dropped; 1.12.2 had Numbers.RainShieldRange default 5*16 (origin/1.12.2 config/Numbers.java:7-8); per-position result cache not ported (TE javadoc); client visual suppression added (CA row 261, "real behavior change from 1.12.2").
PLAYTEST-NEEDED: visual rain/snow hiding in range, sounds (open row 261).
PARITY: none (1.12.2 config exists, noted above).
WIKI-CLAIM-CHECK: "5 Chunks by default, configurable" -> default 80 blocks = 5 chunks confirmed, "configurable" contradicted (hard-coded, AsmHandler.java:241). "no rain particles/sounds & stuff like fire react as if not raining" -> particles/mechanics confirmed via isRainingAt + render redirect; sounds [unverified]. "redstone disables effect" -> confirmed (RainShieldTileEntity.java:60). "no snow forming" -> confirmed via isRainingAt (CA row 258).

UNMAPPED:
- randomthings:blazing_fire (block) is registered (ModBlocks.java:354) but belongs to the Blaze and Steel group, not used by the Igniter; not covered here.
- global_chat_detector, advanced_item_collector, creative_player_interface: no own data/features JSON for global_chat_detector/advanced_item_collector (folded into chat_detector.json / item_collector.json); creative_player_interface.json exists (no recipe, no recipe_images).
- Items imbue_fire/imbue_poison/imbue_experience/imbue_wither and effects of the same names, biome_sensor, biome_crystal: registered items with no own wiki page (only mentioned in imbuing_station / biome_radar pages).
- Hard-coded Imbuing Station extra recipe (cobblestone+water bottle+vine+bone meal -> mossy cobblestone, RandomThings.java:715) not mentioned in the wiki imbuing page.
- Wiki claims with no code at all: OpenComputers components for online_detector, notification_interface, creative_player_interface.
- Items implementing IEntityFilterItem: EntityFilterItem, IdCardItem (usable in Entity Detector CUSTOM mode).

OPEN QUESTIONS:
1. Imbuing Station and Potion Vaporizer (and Inventory Tester) do not drop their inventory when broken (no onReplaced). Intended? 1.12.2 imbuing station did drop (BlockImbuingStation.java:35-45); vaporizer 1.12.2 not checked.
2. NotificationInterfaceBlock.onReplaced drops the ghost icon stack (NotificationInterfaceBlock.java:79) - a copy of any held item the player chose as icon => item duplication path. Needs confirmation.
3. Block Breaker vs blocks needing a better tool than iron: breaks without drops (vanilla rule) or never? Depends on vanilla getPlayerRelativeBlockHardness/tryHarvestBlock; not verifiable from repo.
4. Does the Online Detector's username compare ignore case? (vanilla PlayerList.getPlayerByUsername, source not in repo).
5. Rain Shield: are rain sounds suppressed in-range? Only isRainingAt and renderRainSnow are hooked; client rain sound logic not traced.
6. Dyeing Machine: exact RGB for Purple/Black dye (DyeColor values not in repo) - wiki's "purple restores default glow" claim cannot be matched to code.
7. Lazy mode semantics of Block Destabilizer: code stores NON-matching checked positions (BlockDestabilizerTileEntity.java:196-197), wiki says "remembers what blocks it dropped".
8. Unverified-fact count in this sheet: 9 ([unverified] tags: online-detector case sensitivity, breaker unbreakable-tier behaviour, destabilizer lazy semantics, 1.12.2 vaporizer drop, dyeing purple/black RGB, rain sounds, plus 3 rendering-only items under PLAYTEST-NEEDED).
