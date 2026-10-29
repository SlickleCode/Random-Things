# Group H edits (sound)
- sound_box: changed: description (once per power-on, normal volume/pitch, blank plays nothing, removal with any hand, drops pattern when broken), properties, keywords
- sound_dampener: changed: description (radius to sound origin, client-side, same dimension, exact match, music never muted, stacking, GUI 9 slots stamped only), properties, keywords
- portable_sound_dampener: changed: description (hotbar+main inventory only, no range, any one mutes, GUI main hand, music never muted), properties, keywords
- sound_pattern: changed: description (blank vs recorded, name/tooltip, sneak-click clears held stack, who accepts what), properties, keywords
- sound_recorder: changed: description (new recording wipes old, GUI blocked while recording, music/dampened not recorded, any inventory slot, unique only, GUI slots), properties, keywords

SKIPPED-POSSIBLE-BUG: Sound Box `powered` not saved to NBT (replay after reload) - not written. Sound Box client-side pattern sync [unverified] - not written.
HOLD: audible muting/recording flows (playtest-needed in sheet); dedicated-server dampener sync (open question 3).
REMOVED-CLAIMS: none.
NEW-FACTS-ADDED: about 30 (sentences + property rows).
Note: sneak-click clearing a whole stack was written as "empties every pattern in the held stack" (sheet SoundPatternItem.java:41-54; not marked bug there).
