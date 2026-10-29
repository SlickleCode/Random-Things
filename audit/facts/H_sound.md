# H_sound: Sound group fact sheet (1.14.4 baseline, origin/1.14.4 archive)

Paths relative to MOD = scratchpad/mod1144; J = src/main/java/lumien/randomthings; R = src/main/resources.
1.12.2 branch not checked (no cross-version claims made).

Shared client hook (all muting + recording): J/client/ClientSetup.java:217-257 `onPlaySound(PlaySoundEvent)`; helper mutesSound :260.
- Skips everything if player null/event cancelled/sound null (:219-221). [code]
- Skips sounds of category MUSIC or RECORDS, or whose category volume slider is <= 0 (:225-227): such sounds are never muted AND never recorded. [code]
- Order: (1) placed dampeners (:232-238) -> mute + return; (2) loop over `thePlayer.inventory.mainInventory` slots (:240-256) = 36 slots (hotbar + main), NOT offhand/armor: recording Sound Recorder -> send PlayedSoundMessage and keep scanning; Portable Dampener containing the sound -> mute + return. [code]
- Because the dampener checks `return` first, a sound muted by a placed dampener is not sent to a recording recorder; recorder slot earlier in the inventory than a muting portable dampener still records it. [code]
- Muting = `event.setResultSound(null)`, i.e. purely CLIENT-side, per listening player. [code]
- Debug log lines `[RT-DEBUG] muted sound ...` at :235/:253 (used by scripted playtest; docs/project/version-notes.md:73,101). [code]

---
## sound_box (Sound Box)  [registry: block randomthings:sound_box (J/block/ModBlocks.java:381), blockitem (J/item/ModItems.java:326), TE type sound_box (J/tileentity/ModTileEntityTypes.java:169); wiki id sound_box]
OBTAIN: recipes/sound_box.json shaped "PPP/PLP/PPP", P=minecraft:oak_planks (single item, NOT a planks tag), L=lapis_lazuli, output 1. [code]
OBTAIN: block loot table loot_tables/blocks/sound_box.json drops itself (survives_explosion). [code]
OBTAIN: creative tab "randomthings" (J/item/ModItems.java:326). [code]
BEHAVIOR: Block props: Material.WOOD, SoundType.WOOD, hardness 0.8 (J/block/SoundBoxBlock.java:29). Blockstate has_pattern (bool) switches model sound_box_empty/sound_box_full (R/assets/randomthings/blockstates/sound_box.json). [code]
BEHAVIOR: Right-click when box HAS a pattern (any hand, even empty, no sneak needed): server clears pattern and drops the stack as an item entity at pos.up() (not into player's hand) (SoundBoxBlock.java:76-83). Returns true. [code]
BEHAVIOR: Right-click when box is empty and held item is randomthings:sound_pattern (blank or stamped): inserts a copy with count 1; shrinks held stack unless creative (:85-99). Other items: returns false (:102). [code]
BEHAVIOR: Plays on a redstone RISING EDGE only: block.neighborChanged -> `getRedstonePowerFromNeighbors(pos) > 0` -> TE.updatePowerState (SoundBoxBlock.java:105-112; SoundBoxTileEntity.java:52-67). Plays only if a pattern is inside and its tag sound resolves in ForgeRegistries.SOUND_EVENTS (unknown id = silent) (:57-65). [code]
BEHAVIOR: Sound is played server-side `world.playSound(null, pos, event, SoundCategory.BLOCKS, 1.0F, 1.0F)` (volume 1, pitch 1, fixed; no note/pitch control, no repeat while powered) (SoundBoxTileEntity.java:63). Everyone in range hears it (dampeners can mute it client-side). [code]
BEHAVIOR: `powered` is NOT saved to NBT (write saves only "pattern", :69-78), so after a world/chunk reload `powered` starts false; the next neighbor update with power present counts as a rising edge and plays again. [code] (consequence reasoned from code, not run)
BEHAVIOR: NBT: "pattern" = full ItemStack tag (:73-75). No client sync packet (no getUpdatePacket); visual state comes via blockstate has_pattern. Breaking the block drops the pattern (SoundBoxBlock.java:50-64) -- also on piston/replace when block type changes. [code]
BEHAVIOR: insertPattern sets blockstate via setBlockState(default state with has_pattern) -- rebuilds the default state, so any other state is lost (only one property exists) (SoundBoxTileEntity.java:40-42). [code]
BEHAVIOR: No GUI, no direct redstone output, no comparator. [code]
CONFIG: none.
DIVERGENCE: "Direct port of 1.12.2 BlockSoundBox/TileEntitySoundBox" (class javadocs SoundBoxBlock.java:20-24, SoundBoxTileEntity.java:14-18); IRedstoneSensitive replaced by neighborChanged convention (SoundBoxTileEntity.java:47-51). [code]
PLAYTEST-NEEDED: audible playback on power (docs/testing/README.md lists client sound as out of automated reach per RemainingBehaviorTests.java:52-54). Block-state/NBT only [test]: debug test `behavior.sound_box.pattern_toggles_the_block_state_and_survives_nbt` (J/debug/test/RemainingBehaviorTests.java:408-422; docs/testing/test-inventory.md:68).
PARITY: none documented.
WIKI-CLAIM-CHECK: "plays the sound of the Sound Pattern when powered by redstone" -> confirmed (SoundBoxTileEntity.java:56-64; edge-triggered, once per power-on).
WIKI-CLAIM-CHECK: "right click a sound pattern on the box to put it inside and right click it again to take it out" -> confirmed; removal works with ANY (even empty) hand and the pattern pops out above the block (SoundBoxBlock.java:76-83). Blank patterns are accepted too but play nothing (getSoundLocation null, TE:59).

---
## sound_dampener (Sound Dampener)  [registry: block randomthings:sound_dampener (J/block/ModBlocks.java:382), blockitem (J/item/ModItems.java:327), TE type sound_dampener (J/tileentity/ModTileEntityTypes.java:170), container type sound_dampener (J/container/ModContainerTypes.java:119), screen J/client/screen/ModScreens.java:35; wiki id sound_dampener]
OBTAIN: recipes/sound_dampener.json shaped "PWP/WDW/PWP", P=oak_planks, W=minecraft:white_wool, D=randomthings:portable_sound_dampener (center), output 1. [code]
OBTAIN: loot_tables/blocks/sound_dampener.json drops randomthings:sound_dampener (line 9). [code] Creative tab (ModItems.java:327).
BEHAVIOR: Block props Material.ROCK, hardness 2.0 (J/block/SoundDampenerBlock.java:26). Right-click (any hand, no sneak check, server) opens GUI via NetworkHooks.openGui; returns true (:61-71). Breaking drops all 9 slot stacks (:40-58). [code]
BEHAVIOR: TE = 9-slot ItemStackHandler (SoundDampenerTileEntity.java:36-47); handler.isItemValid only requires item == sound_pattern (blank allowed via automation/hopper); NBT key "inventory" (:123,132). Muted set recomputed on any content change/read: all non-null `sound` tags of the 9 stacks (:65-76). [code]
BEHAVIOR: GUI = AbstractSoundDampenerContainer (J/container/AbstractSoundDampenerContainer.java): 9 filter slots at x=8+18i,y=18; slot accepts ONLY sound_pattern with a stamped sound (blank pattern rejected, :31-33). Player inv at y=51/109. Shift-click moves between filter and inventory (:48-73). Screen 176x166 plain chest-like (client/screen/AbstractSoundDampenerScreen.java:24-27). SoundDampenerContainer.canInteractWith always true (no distance check) (J/container/SoundDampenerContainer.java:19-22). [code]
BEHAVIOR: Mute rule (client): dampener.getWorld()==player's world, !isRemoved, `dampener.getPos().distanceSq(soundPosition) < 20*20` and mutedSounds contains the sound's ResourceLocation (ClientSetup.java:232-238). Radius = 20 blocks spherical (squared-distance < 400), measured from the DAMPENER to the SOUND'S ORIGIN (BlockPos built from sound x/y/z, truncated), NOT from the listening player. [code] docs/testing/fix-log/5-batch-3-remaining-non-block-items-3.md:43-45 (#158) states same and says a sound sourced <20 blocks from the dampener is muted for a player standing farther away.
BEHAVIOR: Tracks dampeners in `loadedDampeners` weak set, added in the TE constructor (SoundDampenerTileEntity.java:34,54); the set is per-JVM so in singleplayer/integrated both sides share TEs; on a dedicated server clients only know dampeners whose chunk they have loaded + synced: sync via getUpdatePacket/getUpdateTag/onDataPacket overrides (:78-109) (bug fixes noted in comments 2026-09-27). [code]
BEHAVIOR: Exact sound id match only (e.g. minecraft:block.note_block.harp); music/record categories and zero-volume categories never muted (ClientSetup.java:225). Sounds from the Sound Box itself are muted if its pattern is in range. [code]
BEHAVIOR: Multiple dampeners stack as union; no redstone interaction. [code]
CONFIG: none.
DIVERGENCE: "Direct port of 1.12.2 BlockSoundDampener/TileEntitySoundDampener" (SoundDampenerBlock.java:19-23; TE javadoc :24-32). Two 1.12.2 containers merged into AbstractSoundDampenerContainer (AbstractSoundDampenerContainer.java:14-23). [code]
PLAYTEST-NEEDED: audible muting (docs/testing/test-inventory.md:13 lists a "Sound Dampener" scripted-playtest scenario in the desktop helper -- result file not read here); TESTING_CHECKLIST.md row 158 (section D) has empty Result column. Server-visible state [test]: `behavior.sound_dampener.muted_set_follows_its_patterns_and_the_block_registers_itself` (RemainingBehaviorTests.java:424-436; test-inventory.md:69).
PARITY: none documented.
WIKI-CLAIM-CHECK: "prevent the sounds from the sound patterns inside of it from being played in a 20 block radius around it" -> confirmed with nuance: radius is around the dampener measured to the sound source, client-side only, same dimension only (ClientSetup.java:233-234).

---
## portable_sound_dampener (Portable Sound Dampener)  [registry: item randomthings:portable_sound_dampener (J/item/ModItems.java:333, maxStackSize 1), container type portable_sound_dampener (J/container/ModContainerTypes.java:120), screen ModScreens.java:36; wiki id portable_sound_dampener]
OBTAIN: recipes/portable_sound_dampener.json shaped "ISI/SLS/ISI", I=iron_ingot, S=minecraft:string, L=lapis_lazuli, output 1. [code]
BEHAVIOR: Right-click (server, MAIN_HAND only) opens the 9-slot filter GUI; returns SUCCESS, else FAIL (J/item/PortableSoundDampenerItem.java:35-56). Offhand use does nothing. [code]
BEHAVIOR: Inventory stored in the item's NBT child tag "inventory" via ItemInventoryHandler (PortableSoundDampenerItem.java:58-60; J/util/ItemInventoryHandler.java:22-39), saved on every content change. Same filter-slot rule as the block (stamped patterns only) (AbstractSoundDampenerContainer.java:31-33). [code]
BEHAVIOR: GUI stays open only while main hand holds an item equal (item+damage only, ignoring NBT) to the dampener (PortableSoundDampenerContainer.java:26-29). [code]
BEHAVIOR: Active when carried ANYWHERE in the 36-slot main inventory (hotbar + 27 main; not offhand, not armor) -- no Baubles/curio slot (ClientSetup.java:240-251, javadoc :205-211). Mutes ONLY for the carrying player, regardless of distance (no radius) (ClientSetup.java:248-254). Client-side only. [code]
BEHAVIOR: Multiple portables in inventory: first one containing the sound mutes. [code]
CONFIG: none.
DIVERGENCE: 1.12.2 was a Baubles body-slot (or inventory) item; Baubles/third-party compat dropped, inventory-only check (PortableSoundDampenerItem.java:22-29; ClientSetup.java:205-211; docs/project/wiki-feature-status.md:595-597). [code]
PLAYTEST-NEEDED: audible muting; GUI feel. No automated test for the portable item found. [unverified]
PARITY: none documented.
WIKI-CLAIM-CHECK: "prevent the sounds from the sound patterns inside of it from being played for the player that has one in their inventory" -> confirmed (ClientSetup.java:240-254); note: hotbar/main inventory only, offhand excluded (mainInventory list).

---
## sound_pattern (Sound Pattern)  [registry: item randomthings:sound_pattern (J/item/ModItems.java:331), stack size default 64; wiki id sound_pattern]
OBTAIN: recipes/sound_pattern.json shaped " S / P / L " (single column): S=string, P=paper, L=lapis_lazuli, output 1 (blank). [code] Stamped copies are made only in the Sound Recorder GUI (see below). Not in loot.
BEHAVIOR: NBT string key "sound" holds the ResourceLocation, e.g. `minecraft:entity.pig.ambient` (J/item/SoundPatternItem.java:76-88). Blank = no tag/"sound" key. [code]
BEHAVIOR: Display name = base name + " <" + sound.getPath() + ">" (namespace dropped, e.g. "Sound Pattern <entity.pig.ambient>") (SoundPatternItem.java:56-65). Tooltip adds a line with the full id (`minecraft:...`) or "<Empty>" for blank (:67-74). Lang has only "item.randomthings.sound_pattern": "Sound Pattern" (R/assets/randomthings/lang/en_us.json:239). [code]
BEHAVIOR: Sneak + right-click clears the sound: `me.setTag(null)` -- clears ALL NBT of the whole held stack (stack of N becomes N blank) (SoundPatternItem.java:41-54). [code]
BEHAVIOR: Item property "filled" (1.0 when stamped) selects model sound_pattern_full vs empty (SoundPatternItem.java:33-38; R/assets/randomthings/models/item/sound_pattern.json). [code]
BEHAVIOR: Used by Sound Box (any pattern accepted), Sound Dampener/Portable (stamped only in GUI), Sound Recorder input slot (SoundRecorderContainer.java:36-38 accepts any pattern incl. stamped). [code]
CONFIG: none.
DIVERGENCE: item meta swap replaced by item-property override (SoundPatternItem.java:24-28). [code]
PLAYTEST-NEEDED: model swap rendering.
PARITY: none documented.
WIKI-CLAIM-CHECK: "Has the sound it contains in its name & tooltip" -> confirmed (name shows path only; tooltip shows full id).
WIKI-CLAIM-CHECK: "Used for the Sound Box / Sound Recorder / (Portable) Sound Dampener" -> confirmed.

---
## sound_recorder (Sound Recorder)  [registry: item randomthings:sound_recorder (J/item/ModItems.java:332, maxStackSize 1), container type sound_recorder (J/container/ModContainerTypes.java:118), screen ModScreens.java:34, network message PlayedSoundMessage (J/network/RTPacketHandler.java registers); wiki id sound_recorder]
OBTAIN: recipes/sound_recorder.json shaped "BGG/ILI/III": B=minecraft:iron_bars, G=minecraft:glass, I=iron_ingot, L=lapis_lazuli, output 1. [code]
BEHAVIOR: Sneak + right-click toggles NBT boolean "recording"; starting a new recording REMOVES the old "recordList" (J/item/SoundRecorderItem.java:49-56). Non-sneak right-click opens the GUI only if NOT recording (:57-59); right-click while recording does nothing else. Both server-side; SUCCESS always. Works from either hand (no hand check) but GUI/screen read the MAIN hand stack (SoundRecorderScreen.java:35; SoundRecorderContainer.java:52). [code]
BEHAVIOR: While recording, every sound the client plays that is not in a MUSIC/RECORDS/muted-volume category, and not muted by a placed dampener, is sent as PlayedSoundMessage(soundLocation.toString(), slot) for each recording recorder in mainInventory (any slot, not just held) (ClientSetup.java:225-247; J/network/messages/PlayedSoundMessage.java:43-56). Server validates slot index and item type, then SoundRecorderItem.recordSound. [code]
BEHAVIOR: recordSound: ignores if not recording; ignores duplicates (exact string match); appends; when list size >= 10 sets recording=false (SoundRecorderItem.java:71-93). So it stops by itself after 10 unique sounds. [code]
BEHAVIOR: NBT: "recording" (bool), "recordList" (list of strings, tag type 8) (SoundRecorderItem.java:52,78,92). Item property "recording" drives a model override (:36-41). [code]
BEHAVIOR: GUI (J/container/SoundRecorderContainer.java): 2 slots: input (x65,y73) accepts ONLY sound_pattern items (any), output (x108,y73) accepts nothing from players (isItemValid slot==0 only, :36-38). Player inventory below; the hotbar slot holding the recorder is locked (can't take/insert) (:61-72). canInteractWith always true. On close, both slots' contents returned to inventory/dropped (:85-97). [code]
BEHAVIOR: Screen 190x186 with scrollable list of recorded sounds (client/screen/SoundRecorderScreen.java:27-28, SoundNameList.java); clicking a row sends container signal id 0 + sound name (:37). Server `outputSound` (SoundRecorderContainer.java:133-152): needs a pattern in input; if output empty -> output = new blank-based stamped pattern (count 1) and consumes 1 input; else if output is a pattern stamped with the SAME sound and not at max stack -> output +1 and consume 1 input; otherwise nothing. [code]
BEHAVIOR: Stamped sound id stored as the full string (e.g. minecraft:block.grass.step) -- list shows full ids (names trimmed by width, fix-log #155). [code]
CONFIG: none.
DIVERGENCE: "Direct port of 1.12.2 ItemSoundRecorder/ContainerSoundRecorder" (SoundRecorderItem.java:24-30; Container javadoc :15-24); list selection via ISignalContainer instead of custom message (SoundRecorderContainer.java:21-24). [code]
PLAYTEST-NEEDED: recording flow, list click/scroll (fix-log docs/testing/fix-log/5-batch-3-remaining-non-block-items-1.md:47-49 (#154) and ...-3.md:39-41 (#155): list rows unreachable bug fixed 2026-09-28/10-04, "Please retest" -- no PASS evidence cited here). [unverified]
PARITY: none documented.
WIKI-CLAIM-CHECK: "Shift-Right click to start recording, shift-right click again to stop" -> confirmed (SoundRecorderItem.java:49-56).
WIKI-CLAIM-CHECK: "stop recording by itself after 10 unique sounds" -> confirmed (:88-90).
WIKI-CLAIM-CHECK: "right click to open gui, put sound patterns in the left slot and select a recorded sound from the list" -> confirmed; input slot is the left slot (x=65); GUI cannot be opened while recording; output is a separate right slot; one blank pattern is consumed per click (SoundRecorderContainer.java:41-42,133-152).
WIKI-CLAIM-CHECK: "record and save sounds ... used in the Sound Box and (Portable) Sound Dampener" -> confirmed.
WIKI-CLAIM-CHECK (omitted by wiki): music/record sounds can't be recorded; new recording wipes the old list; recorder in any inventory slot can record.

---
UNMAPPED:
- No sound-group registry entries lack a wiki page. Containers sound_recorder/sound_dampener/portable_sound_dampener and message PlayedSoundMessage belong to the 5 pages above.
- Sound subsystem files outside the 5 pages: client/screen/SoundNameList.java (recorder list widget), util/WeakSets.java (loadedDampeners).

OPEN QUESTIONS:
1. (resolved) sound_dampener loot table drops itself.
2. Client-side sync of Sound Box pattern: SoundBoxTileEntity has no update packet, so client-side `hasPattern()` in onBlockActivated (SoundBoxBlock.java:76) may read empty on the client; effect on prediction/swing not analysed. [unverified]
3. Does a dedicated-server client learn about dampeners in all loaded chunks? Relies on TE sync (SoundDampenerTileEntity.java:78-109); not exercised by a test (the debug test is server-only).
4. Whether 20-block check should use the listener's distance: code uses sound origin (matches fix-log #158 claim of byte-identical behavior with 1.12.2; 1.12.2 not checked by me).
