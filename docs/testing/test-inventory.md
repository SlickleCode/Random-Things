# Test inventory

Everything automated in this repo, what it checks, and how to run it (all dev-only, behind `DevMode.ENABLED`; run commands in [README.md](README.md)).

## Layers

| Layer | Env var | Where | Report | What it covers |
| --- | --- | --- | --- | --- |
| Resource audit | `RT_AUTOTEST=true` | `client/AutoTest` + audit code | `run/randomthings_audit.txt` | Every block/item/recipe: models, textures, lang keys, loot, recipe parsing. Always runs. |
| In-game tests (client) | `RT_AUTOTEST=true` | `debug/test/*` via `/rtdebug test` | `run/randomthings_tests.txt` | Behaviour, NBT round trips, coremod hooks, run inside a flat test world on the integrated server. |
| In-game tests (dedicated server) | `RT_AUTOTEST=true` with `runServer` | `debug/test/ServerAutoTest` | `run/randomthings_tests.txt` | The same suite on a headless server (catches client-class leaks). |
| GUI screenshots | `RT_AUTOTEST=true` (omit `_NO_GUIS`) | `client/AutoTest` `/rtdebug allguis` | `run/screenshots/randomthings_debug/` | One screenshot of each of the GUIs, to compare by eye. |
| Render scenes | `RT_AUTOTEST_SCENES=true` [`_SCENES_ONLY=id,id`] | `debug/RenderScenes`, `client/SceneShots` | `run/screenshots/randomthings_debug/scenes/` | Built scenes for every block entity/entity renderer, items, armor dye, fluids, rain, Spectre sky + creative-tab item pages. Reviewed by eye. |
| GUI click-through | `RT_AUTOTEST_CLICKS=true` | `client/GuiClickTest` | `run/randomthings_clicks.txt` | Opens every GUI, hovers/clicks every button, diffs server-side NBT, types into text fields, reopens to check persistence. |

## Scripted in-game tests (named)

### test\BehaviorTests.java (12)

- `behavior.item_collector.moves_nearby_items_into_inventory`
- `behavior.iron_dropper.pulse_mode_drops_one_item_per_rising_edge_into_inventory`
- `behavior.iron_dropper.repeat_mode_keeps_dropping_while_powered_and_stops_when_not`
- `behavior.iron_dropper.drops_item_entity_when_nothing_is_in_front`
- `behavior.igniter.toggle_mode_lights_fire_when_powered_and_removes_it_when_unpowered`
- `behavior.igniter.ignite_mode_leaves_fire_when_unpowered`
- `behavior.analog_emitter.outputs_configured_level_only_while_input_is_powered`
- `behavior.block_breaker.mines_front_block_into_rear_inventory`
- `behavior.block_breaker.does_nothing_while_redstone_powered`
- `behavior.redstone_observer.repeats_the_power_of_its_bound_target_block`
- `behavior.recipes.spectre_anchor_combine_tags_single_unstackable_item`
- `behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output`

### test\CoremodTests.java (7)

- `coremod.spawn.lapis_lamp_forces_monster_spawns_allowed_even_when_lit`
- `coremod.spawn.peace_candle_denies_monster_spawns_in_range`
- `coremod.spawn.slime_cube_allows_slimes_unpowered_and_denies_them_when_powered`
- `coremod.rain_shield.suppresses_rain_within_its_range_only_while_present`
- `coremod.redstone_interface.mirrors_its_input_power_onto_the_bound_target`
- `coremod.spectre_anchor.anchored_items_survive_the_death_drop_routine`
- `coremod.super_lubricent_boots.remove_friction_on_ordinary_ground_unless_sneaking`

### test\MoreBehaviorTests.java (5)

- `behavior.block_destabilizer.drops_the_connected_group_it_faces_and_leaves_the_rest`
- `behavior.contact_button.right_clicking_the_block_it_faces_pulses_power_then_resets`
- `behavior.contact_lever.right_click_toggles_and_stays`
- `behavior.imbuing_station.brews_mossy_cobblestone_from_unordered_ingredients_after_200_ticks`
- `behavior.imbuing_station.a_wrong_ingredient_never_produces_anything`

### test\RTTest.java (1)

- `x`

### test\RemainingBehaviorTests.java (11)

- `behavior.online_detector.stays_off_for_a_name_that_is_not_online`
- `behavior.online_detector.powers_for_an_online_player_and_drops_when_they_are_not_named_any_more`
- `behavior.chat_detector.matching_message_from_the_bound_player_pulses_and_is_consumed`
- `behavior.chat_detector.ignores_other_players_and_non_consume_mode_leaves_the_message`
- `behavior.global_chat_detector.pulses_for_anyone_and_only_consumes_for_an_id_card_owner`
- `behavior.floo.typing_a_destination_on_a_fireplace_teleports_even_with_a_typo`
- `coremod.blazing_fire.catches_flammable_blocks_about_four_times_as_often_as_vanilla_fire`
- `behavior.magic_hood.hides_the_nametag_and_the_potion_particles_only_while_worn`
- `behavior.spectre.every_player_gets_their_own_cube_and_positions_inside_it_resolve_to_that_cube`
- `behavior.sound_box.pattern_toggles_the_block_state_and_survives_nbt`
- `behavior.sound_dampener.muted_set_follows_its_patterns_and_the_block_registers_itself`

### test\UtilityBehaviorTests.java (6)

- `behavior.entity_detector.powers_while_an_entity_is_in_range_and_ignores_far_ones`
- `behavior.entity_detector.monster_filter_and_invert`
- `behavior.ender_mailbox.sneak_click_with_a_letter_delivers_it_to_the_named_receivers_inbox`
- `behavior.ender_mailbox.unknown_receiver_keeps_the_letter`
- `behavior.portkey.binds_primes_after_100_ticks_and_teleports_the_picker_upper`
- `behavior.potion_vaporizer.applies_its_potion_to_mobs_in_the_sealed_room_and_returns_a_bottle`

## Generated in-game tests

`RoundTripTests` generates one test per registered object, so new content is covered automatically:

- `nbt.tile_entity.<id>` - write/read round trip of every block entity type
- `nbt.entity.<id>` - same for every entity type
- `nbt.item.<id>` - same for item stack NBT of every item

Together with the named tests above this is the 333-test suite (+1 skipped: `behavior.ender_mailbox.unknown_receiver_keeps_the_letter` on servers that resolve arbitrary names).

## Tooling pieces

- `RTTestPlayer` (1.16.5): detached `ServerPlayerEntity` with a real `ServerPlayNetHandler`, because Forge's `FakePlayer` sits at 0,0,0 and ignores net calls. Behaviour tests that need a connection check `getClass() != ServerPlayNetHandler.class`.
- `AutoTest` (1.16.5): auto-presses the 1.16 world-open dialogs (`ConfirmScreen`, `ConfirmBackupScreen`), `allguis 40` slower GUI pass.
- `/rtdebug` command: `allguis [delay]`, `gui <id> [sneak]`, `scene <id>`, `test [filter]`.

## Not automated (needs a human)

- Slot interactions in GUIs (click test covers buttons and text fields only)
- Natural worldgen, Peace Candle village church, coremods under production (SRG) names, the experimental-settings prompt
- Whether render scenes *look* right - the pass produces images, a person (or Claude viewing them) judges
