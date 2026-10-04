 # GUI test list (for the debug screenshot pass)

Run `./gradlew runClient` (debug flag is on by default there). Every GUI below is screenshotted
automatically ~5 frames after it opens, to `run/screenshots/randomthings_debug/<ScreenClass>_<time>.png`.
`run/logs/latest.log` has a `[RT-DEBUG] Opened GUI ...; recent input: ...` line per GUI. A new
screenshot is taken each time you re-open a GUI.

Open every GUI at least once, then hand the screenshots (or just say "done") back for review.
Tip: with the debug flag on, `/rtdebug gui <id>` (tab-completes; add `sneak` for the Redstone Remote edit screen) opens
any block's/item's GUI directly - blocks get placed 3 blocks in front of you, items go in your main hand. Otherwise
`/give @s randomthings:<id>` for blocks/items; creative tab is "Random Things".

Status column: `[ ]` not yet seen, `[x]` screenshot reviewed and layout OK.

## Block GUIs (right-click the placed block)

| Done | Screen class | Block id | Notes to check |
|---|---|---|---|
| [x] | AdvancedRedstoneTorchScreen | `advanced_redstone_torch` | mode/strength controls legible |
| [ ] | AnalogEmitterScreen | `analog_emitter` | FIXED 10/3 - re-screenshot; signal strength control |
| [ ] | IgniterScreen | `igniter` | FIXED 10/3 - re-screenshot; mode buttons legible |
| [ ] | OnlineDetectorScreen | `online_detector` | FIXED 10/3 - re-screenshot; player name field + mode button |
| [x] | AdvancedRedstoneRepeaterScreen | `advanced_redstone_repeater` | delay/mode controls |
| [ ] | IronDropperScreen | `iron_dropper` | FIXED 10/3 - re-screenshot; slots + mode buttons |
| [ ] | InventoryTesterScreen | `inventory_tester` | ghost slot + mode buttons |
| [x] | ChatDetectorScreen | `chat_detector` | re-check after 10/3 rebuild: field, sprite toggle, tooltip |
| [x] | RedstoneObserverScreen | `redstone_observer` | |
| [x] | BasicRedstoneInterfaceScreen | `basic_redstone_interface` | |
| [x] | AdvancedRedstoneInterfaceScreen | `advanced_redstone_interface` | re-check after 10/3 texture fix |
| [x] | PotionVaporizerScreen | `potion_vaporizer` | |
| [ ] | EntityDetectorScreen | `entity_detector` | FIXED 10/3 - re-screenshot; filter slot + mode buttons |
| [ ] | AdvancedItemCollectorScreen | `advanced_item_collector` | FIXED 10/3 - re-screenshot; filter grid + buttons |
| [x] | FilteredSuperLubricentPlatformScreen | `filtered_super_lubricent_platform` | |
| [ ] | NotificationInterfaceScreen | `notification_interface` | |
| [x] | GlobalChatDetectorScreen | `global_chat_detector` | re-check after 10/3 rebuild: ID-card slots, toggle |
| [x] | SoundDampenerScreen | `sound_dampener` | 9 slots |
| [x] | ImbuingStationScreen | `imbuing_station` | |
| [ ] | EnderMailboxScreen | `ender_mailbox` | |
| [ ] | BlockDestabilizerScreen | `block_destabilizer` | |
| [x] | DyeingMachineScreen | `dyeing_machine` | |

## Item GUIs (right-click while holding)

| Done | Screen class | Item id | Notes to check |
|---|---|---|---|
| [ ] | ChunkAnalyzerScreen | `chunk_analyzer` | FIXED 10/3 - re-screenshot; scan-result list text fits |
| [x] | SoundRecorderScreen | `sound_recorder` | re-check after 10/3: list text readable; needs recorded sounds + a blank `sound_pattern` |
| [x] | PortableSoundDampenerScreen | `portable_sound_dampener` | looked fine |
| [x] | EnderLetterScreen | `ender_letter` | |
| [x] | RedstoneRemoteEditScreen | `redstone_remote` (sneak + right-click) | re-check after 10/3 texture fix |
| [ ] | RedstoneRemoteUseScreen | `redstone_remote` (plain right-click) | FIXED 10/3 - re-screenshot; bind some `position_filter`s first to see the buttons |

## Total: 28 container screens (22 block + 6 item screens)

For GUIs that need setup (Sound Recorder needs recorded sounds; Remote Use needs bound Position
Filters; Imbuing Station / Potion Vaporizer are best checked with items inside) - screenshots are taken
once at open, so open them again after setting up if you also want to see the populated state.
