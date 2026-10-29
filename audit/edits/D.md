# Edits group D (decor)
- lapis_glass: unchanged: description confirmed (LapisGlassBlock.java:44-54).
- quartz_glass: unchanged: confirmed (QuartzGlassBlock.java:40-47).
- lapis_lamp: changed: hostile mobs can still spawn within 4 blocks (AsmHandler.java:181-198), Peaceful note, properties added.
- quartz_lamp: changed: user's description (no visible light, still prevents hostile spawns); radius and code light behaviour not mentioned.
- rainbow_lamp: changed: light 15 always, level-to-colour order (RainbowLampBlock.java:50-59), properties.
- luminous_blocks: changed: 16 colours, both variants real level-15 lights, properties.
- stained_bricks: changed: made from vanilla Bricks block + dye, luminous light 15, properties.
- diaphanous_blocks: changed: donor block not consumed, toggle back, drops itself with appearance kept, properties.
- light_redirector: changed: second click re-enables, visual only, properties.
- platforms: changed: six woods, top 2/16 solid, feet-below-top passable, keywords, properties.
- block_of_sticks: changed: hand-mined drops itself, returning range 50, none for creative, properties.
- compressed_slime_block: changed: costs 1 durability per step, not craftable, drops plain slime block, bounce speeds 0.8/1.2/1.6, properties.
- super_lubricent: changed: now the Tincture page: crafting ingredient with no own use, used to craft Super Lubricent Boots (recipe json), properties.
- super_lubricent_stone: changed: added 0.35 blocks/tick cap, boats 15 deg/tick, keeps sliding, properties.
- super_lubricent_ice: NEW page (block): ice, platform, filtered platform, single example-item filter, caps, properties. No recipe added.
- luminous_powder, runic_dust: not touched (user decision).

SKIPPED-POSSIBLE-BUG: none.
HOLD:
- Diaphanous fade distances (~3.6 / ~6.2 blocks, squared-unit derivation, sheet marks look as playtest-needed): not written.
- Block of Sticks Returning: item lost if inventory full (derived, unverified): not written.
- Compressed slime: shovel off-hand double-fire (open question 2): not written.
- Ice recipe bucket return (open question 5), filter ignoring NBT/damage (open question 6): not written; filter text says "same item type" only.
- Quartz glass projectile collision (unverified): not written.
REMOVED-CLAIMS:
- quartz_lamp "emits a light level of 15" (QuartzLampBlock.java:20-23 has no light value; user decision 2).
- lapis_lamp "does not prevent anything that requires darkness" replaced by precise spawn statement (AsmHandler.java:181-198).
NEW-FACTS-ADDED: about 40
