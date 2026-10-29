# Edit log, group A (redstone)

changed: advanced_redstone_interface - added 9-slot Position Filter gui, filters only, same side-by-side behaviour as basic, max combining, drops filters when broken; properties; keywords
changed: advanced_redstone_repeater - delay range 0 to 120 ticks, step 1, default 20/20 (user decision 3); properties; keywords
changed: advanced_redstone_torch - gui (0 to 15, step 1), defaults red 15 / green 0, wall placement, burnout; properties
changed: analog_emitter - gui strength 0 to 15, default 1; properties
changed: contact_button_lever - 20 tick button pulse, lever toggles, any block in front, main hand only, 15 on all sides; properties
changed: redstone_activator - default 20 ticks, strength 15, clicked block is powered, tooltip; properties
changed: redstone_interface - removed OpenComputers paragraph; added gui (target / No Target) and max combining; properties
changed: redstone_observer - added per-side analog strength copy, gui, no range limit; properties
changed: redstone_remote - typo "seperately"; properties (9 positions, 20 tick pulse, stack 1)
changed: sided_redstone - output 15 on one side, side depends on placement direction; properties
changed: trigger_glass - sand/gravel pass, relocks after 3 s, chain limit config TriggerGlassChainLimit default 20 (0 = unlimited); properties
new: redstone_tool (item) - purpose, glint while linking, any target block, no range, Advanced Interface not linkable; link steps deliberately NOT written (HOLD)

SKIPPED-POSSIBLE-BUG:
- redstone_interface: "you are able to see these connections as red lines ... close to the redstone interface". Today's RedstoneObserverLineRenderer.java:62-82 only draws Observers; the original ClientProxy.renderRedstoneInterfaceStuff drew Basic Interface lines too. Differs from original, not documented as deliberate. Sentence left as is.

HOLD:
- redstone_interface / redstone_observer: "First right click on the interface/observer and then on the block" (Redstone Tool linking). Block onBlockActivated always opens the gui, so linking may be unreachable (sheet open question 1). Sentences left as is; "Linking: Redstone Tool" property rows repeat only the existing claim. redstone_tool page states no link steps.
- redstone_interface: "does not work across dimensions" left as is (no dimension stored; sheet open question 7).
- advanced_redstone_interface: target persistence after reload not written (open question 2). Position Filter dimension behaviour not written.
- redstone_observer: output after reload not written (open question 3).
- advanced_redstone_torch: persistence of edited strengths not written (open question 4).
- analog_emitter: input-side direction semantics vs original (open question 6) not written beyond existing text.
- redstone_activator: sneak-click on a block vs air not changed; existing "into the air" wording kept (open question 9).
- sided_redstone: which face emits relative to look direction not written (playtest needed).

REMOVED-CLAIMS:
- redstone_interface: OpenComputers component "basic_redstone_interface" with setTarget/getTarget (no OC code: grep J/ and resources, A_redstone.md redstone_interface section; user decision 6).

NEW-FACTS-ADDED: ~45
