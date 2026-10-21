"""PostToolUse hook (Edit|Write): after touching files that need a specific follow-up check, remind the agent once per edit.

Reads the hook JSON from stdin, never blocks and never fails the tool call (always exits 0).
"""
import json
import sys

try:
    data = json.load(sys.stdin)
    path = (data.get('tool_input') or {}).get('file_path', '').replace(chr(92), '/')
except Exception:
    sys.exit(0)

notes = []

if '/src/main/resources/transformer/' in path and path.endswith('.js'):
    notes.append('A coremod transformer changed: a transformer whose anchor no longer matches silently does nothing. Re-derive the anchor from the target jar (bytecode-scout / coremod-patching) and run the coremod tests (RT_AUTOTEST_FILTER=coremod) plus the render scenes before calling it done.')

if '/src/main/resources/META-INF/coremods.json' in path:
    notes.append('coremods.json changed: check every listed transformer file exists and re-run the coremod tests.')

if path.endswith('/build.gradle') or path.endswith('/gradle.properties') or path.endswith('/block/ModBlocks.java') or path.endswith('/item/ModItems.java'):
    notes.append('Shared build/registry file changed: the same change usually belongs on the other version branches. Check /mirror (docs/migration/tools/mirror.sh) and docs/migration/tools/test-parity.py before finishing.')

if '/debug/test/' in path and path.endswith('.java'):
    notes.append('A test changed: mutation-check it (break the code under test once, see it fail, restore) and run docs/migration/tools/test-parity.py to see which branches lack it.')

if notes:
    print(json.dumps({'hookSpecificOutput': {'hookEventName': 'PostToolUse', 'additionalContext': ' '.join(notes)}}))
