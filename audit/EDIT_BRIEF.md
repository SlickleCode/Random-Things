# Brief for wiki page-edit agents (Phase 3, text only)

You edit `data/features/<id>.json` of the pages assigned to you in /home/user/Random-Things (the `wiki` branch worktree).
Evidence = the fact sheet(s) named in your task (`audit/facts/*.md`, every fact has a citation + grade) and the audit
verdicts in `audit/GAP_MATRIX.md` (section 3). The mod source (read-only) is extracted at
MOD=/tmp/claude-0/-home-user-Random-Things/f859bde5-7624-5f08-9a19-e526dde76d34/scratchpad/mod1144 (paths in sheets are relative to it;
`J/` = `src/main/java/lumien/randomthings/`, `R/` = `src/main/resources/`). Original mod source for comparison:
`git -C /home/user/Random-Things show origin/1.12.2:<path>` (read-only).

## You may change
- ONLY the fields `description`, `properties`, `keywords`, `title` (only if wrong) of your assigned pages, and create the NEW
  pages assigned to you. NEVER touch `recipe`, `recipes`, `recipe_images`, `images`, `videos` (the recipe fields are rewritten
  by a script afterwards; keep the existing values untouched). Never touch other pages, `docs/`, `scripts/`, `template-html/`,
  `data/categories.json`. Do not run the generator. Do not commit, push, fetch, download, run the game or edit the mod.

## User decisions (binding)
1. Pages `spectre_sapling`, `runic_dust`, `luminous_powder`, `spectre_coils`, `spectre_illuminator`, `bean` are LEFT AS IS. Do not edit them.
2. `quartz_lamp`: replace the light claims with the user's description: the Quartz Lamp gives off no visible light, but it still
   prevents hostile mobs from spawning nearby. Do not add the radius, do not mention what the code does about light.
3. `advanced_redstone_repeater`: document the real delay range and step (read the sheet; give units, ticks) and the default.
4. `rain_shield`, `ancient_furnace`: state the fixed values (not configurable) instead of "configurable".
5. "Wiki follows the mod as it is today": remove claims about things the mod does not have, and fix wrong numbers.
6. Remove OpenComputers / third-party mod / mod-ore compatibility sentences (not part of this mod). Item Filter does not exist:
   rewrite sentences that rely on it (single example-item slot instead), as in `docs/project/scope-decisions.md` (see audit/facts).
7. If something looks like a MOD BUG, do NOT write it on the wiki page and do NOT "fix" the wiki claim around it. Leave that
   sentence exactly as it is and log it. Test: for every wiki claim the sheet marks contradicted, compare with the original
   (`git show origin/1.12.2:...` of the same class). If the original behaves like today's code, or `scope-decisions.md` /
   code comments list it as deliberate, the wiki was simply wrong: fix the wiki. If today's code differs from the original and is
   not documented as deliberate, it may be a mod bug: leave the wiki text alone and log it as SKIPPED-POSSIBLE-BUG. If you cannot
   tell, treat it as possible bug and log it. Facts graded [unverified] never go on the wiki. Behaviour that depends on an open
   question in the sheet (cross-dimension storage, reload persistence, Redstone Tool linking) is not written; log it as HOLD.

## Style rules
- Existing tone: short, player-facing, plain. Keep the page's existing structure and good sentences; change only what is wrong,
  add what is missing (obtain method, what the GUI controls do, redstone behaviour, numbers players need, config key + default).
- ASCII only (no curly quotes, no unicode arrows/dashes; use "-" and "to"). Newlines in `description` are `\n\n` between paragraphs
  and `- ` bullets as in existing pages.
- NO version wording of any kind ("1.12", "1.14", "in this version", "the port", "ported", "originally", "previously", "removed in").
- Intentional divergences listed in `docs/project/scope-decisions.md` get ONE short neutral sentence (state the behaviour, no history).
- Use `properties` rows ([{"label","value"}]) for quick facts: Obtain, Config (key + default), Range, Redstone, Stack size.
  Do not duplicate recipes there (recipes are rendered separately). Do not describe recipes in prose except to say "Shapeless" is handled
  elsewhere: never write the recipe grid in text.
- Do not invent: every number/behaviour you add must trace to a [code]/[test]/[playtest] fact in the sheets or to a source line you
  read yourself. Vanilla values from memory are NOT allowed.
- Keep keywords useful for the sidebar search (existing words + new item names that the page now covers).
- JSON: edit with Python `json.load` / `json.dump(obj, f, indent=2, ensure_ascii=True)` + trailing newline identical to the original
  file's ending; preserve key order. Validate by re-loading. If a page needs no change, do not rewrite the file.

## New pages
Create `data/features/<id>.json` with `title`, `category` (`item`, `block` or `misc`), `description`, `keywords`, `properties`;
use `"images": [], "videos": [], "recipe_images": []`. Do NOT add a recipe (the script adds it from the mod's recipe JSON).

## Deliverable
Write `audit/edits/<your group letter>.md`: for each page one line "changed: ..." or "unchanged: reason", plus lists
`SKIPPED-POSSIBLE-BUG:` (wiki claim, code behaviour, why you left it), `HOLD:` (claims left because they need a playtest),
`REMOVED-CLAIMS:` (claims you deleted as not in the mod, with citation), `NEW-FACTS-ADDED:` count. Reply with <= 12 lines.
