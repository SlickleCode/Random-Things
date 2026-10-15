# Rename / conversion tools (used for the 1.16.5 -> 1.18.2 port)

Scratch-quality but reproducible. They expect a scratch dir holding `client16.txt` (Mojang 1.16.5 `client.txt`), `mcp/` (the unzipped
`mcp_snapshot-20210309-1.16.5.zip`) and `cp16.txt` (the 1.16.5 compile classpath, printed by a Gradle init script). Paths are hard-coded for this machine.

| Tool | Does |
| --- | --- |
| `maps.py` | MCP 1.16.5 -> Mojang names: joins the 1.16.5 `mcp_mappings.tsrg`, `methods.csv`/`fields.csv` and Mojang `client.txt`; writes `classes/methods/fields.tsv` |
| `Remap.java` | Type-aware source remapper: javac attributes the 1.16.5 sources against the 1.16.5 jar and rewrites every type/method/field/import reference (overrides resolved through the supertypes). 13k edits, 0 attribution errors |
| `fixfqn.py` | Fully-qualified names written inline in code (and `fml.network` -> `network`) |
| `autoimp.py` | Adds imports for "cannot find symbol: class X" errors (unique Mojang simple name only) |
| `errfix.py` | Error-oracle fixer: replaces a token at the compiler caret (used for `BlockPos.offset(Direction)` -> `relative`, the one overload the name map cannot tell apart) |
| `be_convert.py` | `TileEntity` -> `BlockEntity` (ctor `(pos, state)`, `saveAdditional`, `load(tag)`, ticking via `EntityBlock.getTicker`/`util/Tickers`) |

Order used: `maps.py` -> `Remap.java` -> `fixfqn.py` -> delete the two wildcard imports -> `be_convert.py` -> compile -> `autoimp.py` -> compile -> `errfix.py`.

Added during the port: `lookup.py` (Mojang name -> SRG `f_`/`m_` id in 1.18.2, for reflection and coremod names), `jpm.py` (print one method's `javap -c` from the 1.18.2 jar), `js_classes.py` / `js_names.py` (retarget the JS coremods' class names / method names). Hard-coded paths are for this machine.

`CoremodCheck.java` (offline coremod check, run after every transformer change): loads each `transformer/*.js` target class from the mapped jar, runs the real script through Nashorn and reports `MISSING CLASS` / `UNCHANGED` (the transformer matched nothing) / `ok`. Classpath: asm, asm-tree, nashorn-core 15.4 and `net.minecraftforge:coremods:5.2.5` from the Gradle cache; it would have caught the double-applied class rename that pointed `ItemRendererTransformer` at `ItemEntityRenderer` (found by the scripted playtest, not by any suite). A rename script must never be applied twice to the same text: a Mojang name can also be an MCP name of another class.
