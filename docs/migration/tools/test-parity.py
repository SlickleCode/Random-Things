"""List which in-game tests exist on which version worktree.

    python docs/migration/tools/test-parity.py            # only tests that are missing somewhere
    python docs/migration/tools/test-parity.py --all       # every test
    python docs/migration/tools/test-parity.py --missing 1.18.2   # what 1.18.2 lacks compared with the other branches

Reads the committed working trees of the sibling folders (<parent>/<branch>) and collects every name given to
RTTest.named("...") under src/main/java/**/debug/test. A test that is only on one branch because it is version-specific
(e.g. 1.20.6's behavior.stack_data.*) shows up as missing elsewhere: judge it, the script only reports.
"""
import os
import re
import sys

here = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
parent = os.path.dirname(here)
BRANCHES = sorted((d for d in os.listdir(parent) if re.fullmatch(r'\d+\.\d+(\.\d+)?', d)), key=lambda v: [int(x) for x in v.split('.')])
NAME = re.compile(r'RTTest\s*\.\s*named\(\s*"([^"]+)"')


def collect(branch):
    names = set()
    root = os.path.join(parent, branch, 'src', 'main', 'java')

    if not os.path.isdir(root):
        return None

    for dirpath, _, files in os.walk(root):
        for f in files:
            if f.endswith('.java') and os.sep + 'debug' + os.sep in dirpath + os.sep:
                with open(os.path.join(dirpath, f), encoding='utf-8', errors='replace') as fh:
                    names.update(NAME.findall(fh.read()))

    return names


args = sys.argv[1:]
show_all = '--all' in args
only = args[args.index('--missing') + 1] if '--missing' in args else None
tests = {b: collect(b) for b in BRANCHES}
present = [b for b in BRANCHES if tests[b] is not None]
universe = sorted(set().union(*(tests[b] for b in present)))

print('tests per branch: ' + ', '.join('%s=%d' % (b, len(tests[b])) for b in present))
rows = []

for n in universe:
    have = [b for b in present if n in tests[b]]

    if only:
        if only in present and n not in tests[only]:
            rows.append((n, have))
    elif show_all or len(have) != len(present):
        rows.append((n, have))

if not rows:
    print('nothing to report: every listed test is on every branch')
    sys.exit(0)

w = max(len(r[0]) for r in rows)
print('%-*s  %s' % (w, 'TEST', ' '.join('%-6s' % b[2:] for b in present)))
for n, have in rows:
    print('%-*s  %s' % (w, n, ' '.join('%-6s' % ('x' if b in have else '.') for b in present)))
print('\n%d tests, %d not on every branch' % (len(universe), sum(1 for _, h in rows if len(h) != len(present))))
