# 05 — Verification lessons

"Done" in a port means *behaves like the original in the running game*. Compiling is the first and weakest
check. This is the ladder of checks that worked, ordered by cost, and what each one catches.

## 1. The verification ladder

| Level | Check | Catches | Misses |
| --- | --- | --- | --- |
| 0 | Compile (`compileJava`) | Syntax, signatures, renamed APIs | Everything else |
| 1 | `javap`/sources-jar ground-truth *before* writing | Wrong API assumptions, missing/dead hooks | Logic errors |
| 2 | Full `./gradlew build` incl. `reobfJar` | Production-jar build problems | Runtime |
| 3 | **Boot the client** (`runClient`) | ObjectHolder crashes, lifecycle NPEs, coremod `VerifyError`, model bake errors | Anything needing a world |
| 4 | **Automated audit** (resource audit) | Missing/invalid models, textures, recipes, loot, coverage gaps | Behavior |
| 5 | **Automated in-game behavior + round-trip tests** | Dead hooks, wrong redstone/capability logic, non-idempotent NBT | Visuals, feel, multiplayer |
| 6 | Automated GUI pass (screenshots) | GUIs that crash on open, stale tile entities (found `onReplaced` bug) | Layout correctness, interaction |
| 7 | Human playtest against a numbered checklist | Visuals, feel, intent mismatches | Rare states |
| 8 | Multiplayer / dedicated server test | Side leaks, sync-only bugs | — (not done in this project) |

Rule of thumb: **every bug class found at level 7 should get a level 3–5 check** so it can't come back.
This project did that retroactively; do it as you go ([`docs/testing/`](../testing/README.md)).

## 2. Ground truth hierarchy

1. The compiled game/loader jar for the exact target build (`javap -c -p`). Cheapest and most reliable for
   "does it exist / what does it call."
2. The sources jar for the same build (readable behavior; grep for who *posts* an event).
3. The original mod's real source from the old branch (`git show origin/<branch>:<path>`), including *data
   files* (JSON recipes/loot/blockstates), not just Java.
4. The original mod's behavior in the running old version, if obtainable.
5. Online guides, wiki pages, memory — hints only. Guides described later builds than the one in use; wiki
   pages documented intended behavior, not always shipped behavior.

Questions to ask of any hook before relying on it:

- Does it exist? (`javap`)
- **Who calls it?** (grep the sources jar for the post/fire/invocation site)
- Does it fire at the moment I need (before vs after the decision)?
- On which side(s) and for which entity/world classes?
- Does it do what its name implies in the special case I need? (`tryPickUpFluid` didn't for plain fluid blocks)

## 3. Getting runtime evidence instead of guessing

- **Rule:** after two static-review fixes that don't resolve a bug, stop and get evidence.
- Tools used successfully:
  - `[RT-DEBUG]`-style tagged logging at every decision point (placement refused/accepted, settled, queue sizes),
    then ask for the log. The Spectre Illuminator's third bug was identified from one log excerpt.
  - Running the real client with extra flags and reading the log (Mixin debug/export).
  - Offline harnesses (Nashorn + ASM verifier) for coremods.
  - Behavior tests that reproduce a user's report deterministically.
  - A screenshot or crash report from the tester — ask for it immediately.
- State plainly what is verified and what is not: "build-verified", "boots", "unit-tested", "needs a playtest".
  Never mark a bug fixed on the strength of a code change.

## 4. What the automated setup in this repo does (summary)

Details: [docs/testing](../testing/README.md).

- Resource audit: blocks/items ↔ blockstates/models/textures/recipes, parse-checked with the game's own model
  deserializer, recipe-load completeness, "every item has a recipe unless allowlisted."
- Behavior tests: contraptions built in a scratch area ticked on the real server, including tests that drive the real
  patched vanilla code paths for each coremod-backed feature (spawn dispatch, rain check, power query, movement
  friction, death-drop routine). A coremod is not verified until a test observes its effect.
- NBT/capability round trips for every tile entity / entity / item.
- One dev-only switch so none of it affects players ([isolation](../testing/isolation.md)).

Things it found that nothing else did: 64 silently missing recipes; a dead hook (Magnetic Enchantment); a model the
game rejected; a non-idempotent tile entity serializer; a stale tile entity after `onReplaced`; a Redstone Interface whose
refresh hook never fired (found by a contraption test that also proved six coremods apply at runtime).

## 5. Playtest protocol that worked

1. Fresh build, restart the client (stale builds caused phantom "not craftable" reports).
2. Work the checklist top to bottom; one behavior per row; owner fills the result column.
3. For every `FAIL`: reproduce → ground-truth against the original → fix → mark `FIXED` (not `PASS`) → owner flips
   to `PASS`.
4. Root-cause narrative in `TESTING_NOTES.md` (now `docs/testing/fix-log/`) keyed by row, not in the owner's feedback column.
5. Periodically regenerate the "untested/open" summary and prune stale statuses.

## 6. Things you cannot verify headlessly (say so)

Visual correctness of renderers (lighting, tint, winding), sound, particles, input feel, multiplayer. For these,
describe exactly what to look at and ask for a screenshot.

## 7. Tooling quirks worth knowing (Windows + Git Bash + PowerShell environment)

- `jjs` mis-parses multi-jar classpaths → use `ScriptEngineManager`.
- No image library available → System.Drawing from PowerShell for pixel-row edits of GUI textures.
- A heredoc with an unbalanced quote in a shell tool call fails to parse and runs **nothing** — check that file
  creation actually happened; prefer the editor/write tool for large files.
- Backgrounded `runClient` + log grep beats watching the console; give it a timeout.
- Sandboxes without Forge/Mojang Maven access cannot build; tag work done there "not build-verified".
- Line endings: the repo's markdown is CRLF on Windows; scripts that rewrite files should preserve them.
