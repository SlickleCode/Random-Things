# Testing checklist archive (full sheet, 2026-10-11)

Every numbered row of the 1.14.4 testing sheet exactly as it stood when the checklist was finished (steps, results, feedback), split by section. The live list of what is still open is the root [`TESTING_CHECKLIST.md`](../../../TESTING_CHECKLIST.md).

Original intro:

> One row per individually testable feature. Fill in **Result** with `Pass` or `Fail` (leave blank if untested), and add a one-sentence note in **Feedback** — especially for a `Fail`, since I can't run the game myself and need specifics (what you did, what happened vs. what you expected) to fix it. Rows are numbered so you can reference a specific one back to me, e.g. "#47 is broken." Root-cause writeups and fix history for a row (mine, not yours) live in [`TESTING_NOTES.md`](TESTING_NOTES.md) under the same row number, not here — this column stays free for your own notes.

Note: row #356 appears twice in the sheet (Ender Anchor chunk loading, and the `[worldgen] PeaceCandle = false` config row); the second is labelled #356b in the root list.

| File | Section | Rows | Row numbers |
| --- | --- | --- | --- |
| [1-priority-recently-fixed-bugs-please-co.md](1-priority-recently-fixed-bugs-please-co.md) | 1. Priority: recently fixed bugs (please confirm these first) | 28 | #1 .. #28 |
| [2-batch-1-standalone-blocks-no-gui-tile-.md](2-batch-1-standalone-blocks-no-gui-tile-.md) | 2. Batch 1 — standalone blocks (no GUI/tile entity) | 43 | #29 .. #136 |
| [3-batch-2-tile-entity-gui-backed-blocks.md](3-batch-2-tile-entity-gui-backed-blocks.md) | 3. Batch 2 — tile-entity/GUI-backed blocks | 47 | #71 .. #117 |
| [4-general-spot-checks-across-the-whole-p.md](4-general-spot-checks-across-the-whole-p.md) | 4. General spot-checks across the whole port | 3 | #118 .. #120 |
| [5-batch-3-remaining-non-block-items-1.md](5-batch-3-remaining-non-block-items-1.md) | 5. Batch 3 — remaining (non-block) items | 141 | #121 .. #261 |
| [5-batch-3-remaining-non-block-items-2.md](5-batch-3-remaining-non-block-items-2.md) | 5. Batch 3 — remaining (non-block) items | 50 | #262 .. #311 |
| [5-batch-3-remaining-non-block-items-3.md](5-batch-3-remaining-non-block-items-3.md) | 5. Batch 3 — remaining (non-block) items | 76 | #312 .. #386 |
| [6-not-implemented-yet-don-t-test-these-t.md](6-not-implemented-yet-don-t-test-these-t.md) | 6. Not implemented yet — don't test these, they don't exist | 0 | - |
