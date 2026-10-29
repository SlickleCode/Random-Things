# Edits G (entities / eggs / pearls / ender / teleport)

- eclipsed_clock: changed: removed false "hover with bottle shows needed time" claim (1.12.2 renderer also only shows target time, so wiki was wrong); added +/-600 ticks per click, label, forward-only skip, animated ~12 s skip (scope-decision), no message if too little time; properties added.
- time_in_a_bottle: changed: cost sequence fixed (30,30,60,120,240,480 s; 6 clicks to 32x; formula identical in 1.12.2 ItemTimeInABottle, so wiki was wrong); added gain rate, tooltip, duplicate rule, not-enough-time message; properties added.
- weather_egg: changed: added 5-15 min duration, 2 per craft, dispenser, rain vs storm; properties extended.
- golden_egg: changed: chicken lays 3 ingots per gold ORE item fed (same design in 1.12.2 EntityGoldenChicken, wiki was wrong); config GoldenEgg, explosion chance; properties added.
- summoning_pendulum: changed: players excluded too, release on block, FIFO, bar/glint, loot chances, config; properties added.
- stable_ender_pearl: changed: not thrown, whole stack used, any living entity; properties extended.
- artificial_end_portal: changed: 10 s charge, no consume on bad structure, players only, config, tear ingredients; properties added.
- floo_teleportation: changed: "Floo Dust" -> Floo Powder, main hand, cost 1 powder, brick limit, break behaviour, temp fireplace ~13 s and free, spelling fix; properties added.
- portkey: changed: same-dimension only, placement rule (+-2 horizontal, up to 10 below), unbound never teleports, donor item kept; config HideCoordinates; properties added.
- ender_bridge: changed: facing, anchor destination is block above, also teleports items/minecarts, any block stops scan, chunkloading config; properties added.
- ender_bucket: changed: stacks to 16 empty, reinforced always picks up when aimed at fluid; capacity and search limit properties.
- ender_letter: changed: 9 slots, 200-char message, unknown receiver / full inbox not sent, owner-only mailboxes, glow delay; properties added.

SKIPPED-POSSIBLE-BUG:
- Reinforced Ender Bucket drains source before checking fluid compatibility (fluid lost when mixing): not written, no wiki sentence touched.
- Eclipsed Clock drop loses targetTime NBT; sneak from 0 gives negative target: not written (EclipsedClockEntity ~252, 194-199; 1.12.2 comparison not done).
- Floo registry removal of fireplace when target chunk unloaded (FlooNetworkHandler:119): not written.
- Stable Ender Pearl bound player in other dimension: no transfer (1.12.2 transferred): not written, wiki sentence unchanged ("teleport the player it's bound to").
- Stable Ender Pearl tag without player-uuid throws: not written.

HOLD:
- Floo cross-dimension fireplace teleport (shared saved data unverified): not written.
- Ender Letter / mailbox cross-dimension inbox sharing: not written.
- Ender Anchor chunk loading across reload (checklist rows blank): not written.
- Time Accelerator wheel visuals (M5 unverified): not written.

REMOVED-CLAIMS:
- eclipsed_clock: "Hovering ... with a Time in a Bottle will display the time necessary" - renderer only shows target time (EclipsedClockEntityRenderer.java:79-86; 1.12.2 RenderEclipsedClock.java:115,156).
- time_in_a_bottle: "first 30, second 60, third 120, fourth 240, fifth 480" - TimeInABottleItem.java:134,179.
- golden_egg: chicken "lays gold ingots instead of eggs" unprompted - GoldenChickenEntity.java:111-127.

NEW-FACTS-ADDED: about 95
