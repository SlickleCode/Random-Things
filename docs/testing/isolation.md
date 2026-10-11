# Keeping dev tooling out of normal gameplay

**Contract:** when `-Drandomthings.debug` is not set, no developer-only code is registered, subscribed,
class-loaded, or executed. A player of the shipped jar pays nothing for the audit, the test suite, the
`/rtdebug` command, GUI screenshots or the hands-free auto-test.

## How it is enforced

```
RandomThings (constructor)
  └─ if (DevMode.ENABLED)            // static final boolean, read once from -Drandomthings.debug
       └─ DevTools.init()            // only reference to any dev class from game code
            ├─ server: /rtdebug command (FMLServerStartingEvent) + RTTestRunner tick listener
            └─ client (DistExecutor): DevToolsClient.init()
                 ├─ GuiDebugScreenshots   (registered via EVENT_BUS.register(Class))
                 ├─ GuiOpenTriggerLog
                 └─ AutoTest              (only if -Drandomthings.autotest=true)
```

Rules that keep this true:

1. **One switch.** `lumien.randomthings.debug.DevMode.ENABLED`. There is no config-file option for any dev
   feature (the old `[debug]` config section was removed), so a modpack or player cannot enable it by
   accident.
2. **One entry point.** Game code may reference only `DevMode` and (behind the check) `DevTools`. Nothing
   else in `debug/`, `debug/test/`, `client/AutoTest`, `client/ResourceAudit`, `client/GuiDebugScreenshots`,
   `client/GuiOpenTriggerLog`, `client/DevToolsClient` may be referenced from gameplay code.
3. **No `@Mod.EventBusSubscriber` on dev classes.** That annotation auto-registers the class at mod
   scan time regardless of any flag. Dev listeners are registered manually from `DevTools`/`DevToolsClient`
   so they do not exist unless the switch is on. (`GuiDebugScreenshots`, `GuiOpenTriggerLog` and `AutoTest`
   used to be auto-subscribed and ran a system-property lookup every client tick — that is gone.)
4. **Dist safety.** Client-only dev classes are only reached through `DistExecutor.runWhenOn(Dist.CLIENT, …)`
   so a dedicated server never loads them.
5. **Logging.** `RTDebug.log(...)` is a no-op unless `DevMode.ENABLED`. Informational per-feature logging
   that used to be unconditional (Spectre Illuminator placement/settling/relight lines — one every 100 ticks per
   orb) now goes through `RTDebug.log`. Real errors still go through log4j `error`/`warn`; `printStackTrace()`
   calls were replaced with logger errors that name what failed.

## Performance reasoning

- `DevMode.ENABLED` is a `static final boolean` initialised once at class load; the JIT folds checks to
  constants. The single check runs once in the mod constructor.
- No dev code sits on a hot path (no per-tick, per-block or per-entity work). The runner's tick listener is
  only added when the switch is on.
- Dev classes still ship in the jar (they are just never loaded). Stripping them from the release jar is
  possible but not done — see `docs/migration/open-questions-backlog.md` for the trade-off.

## Adding new dev tooling

- Put it under `debug/` (common/server) or in the client package, never inline in game classes.
- Register it from `DevTools.init()` / `DevToolsClient.init()`.
- Do not add `@Mod.EventBusSubscriber`, static initialisers that do work, or config entries.
- If the tool needs game code to call it (e.g. a hook point), guard the call with `DevMode.ENABLED`.

## Verifying the contract

`runClient` normally enables the switch. To launch it like a player's install (switch **off**):

```bash
RT_NO_DEBUG=true ./gradlew runClient     # let it reach the main menu, then quit
```

and check `run/logs/latest.log`: it must contain **no** `[RT-DEBUG]` lines and no `Developer tools enabled`
message, and `/rtdebug` must not exist in a world. (Verified 2026-10-07: 0 / 0, no mod exceptions.) On a launcher
install, simply do not add `-Drandomthings.debug=true`.
