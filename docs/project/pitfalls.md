# Pitfalls that cost real time

_Moved verbatim from `PORTING_PLAN.md` (2026-10-11 doc refactor). Index: [README.md](README.md)._

- A fix applied once is often needed in several copies (shared slot validation, `getUpdateTag`,
  `ClientTickEvent` vs `WorldTickEvent`, text-field key routing across 4–5 screens). Grep for siblings.
- "Compiles and looks right" ≠ matches the original: re-read the original control flow line by line
  (missing `iterator.remove()`, wrong `playSound` player arg, missing unconditional top-of-method
  `detectAndSendChanges`).
- When a vanilla widget must lose its chrome, read the **caller's** bytecode, not just the obvious hook.
- Don't iterate blindly on something that can't be verified headlessly; get runtime evidence (logs,
  probes) and state clearly when a fix is unverified. Never report a bug fixed without user confirmation
  or runtime evidence.
- Keep scope tight: split multi-piece features into slices and say what was deferred. Ask via
  `AskUserQuestion` before building something whose consumers are already shipped (Item Filter).

