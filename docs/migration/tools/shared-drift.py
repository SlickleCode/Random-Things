"""Report shared files that differ between the version worktrees.

    python docs/migration/tools/shared-drift.py [--diff]

The files below must be byte-identical (ignoring line endings) on every branch; all version-specific facts belong in
docs/project/version-notes.md, which is deliberately NOT in the list. With --diff, show a unified diff against the newest branch.
"""
import difflib
import os
import re
import sys

here = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
parent = os.path.dirname(here)
BRANCHES = sorted((d for d in os.listdir(parent) if re.fullmatch(r'\d+\.\d+(\.\d+)?', d)), key=lambda v: [int(x) for x in v.split('.')])
SHARED_DIRS = ['.claude']
SHARED_FILES = ['.gitattributes', 'CLAUDE.md', 'docs/project/branches.md'] + ['docs/migration/tools/' + f for f in ('mirror.sh', 'suite-all.sh', 'test-parity.py', 'shared-drift.py')]
IGNORE = {'__pycache__'}


def files_of(branch):
    root = os.path.join(parent, branch)
    found = {f for f in SHARED_FILES if os.path.isfile(os.path.join(root, f))}

    for d in SHARED_DIRS:
        for dirpath, dirs, names in os.walk(os.path.join(root, d)):
            dirs[:] = [x for x in dirs if x not in IGNORE]
            for n in names:
                found.add(os.path.relpath(os.path.join(dirpath, n), root).replace(os.sep, '/'))

    return found


def read(branch, rel):
    p = os.path.join(parent, branch, rel)

    if not os.path.isfile(p):
        return None

    with open(p, 'rb') as fh:
        return fh.read().replace(b'\r\n', b'\n')


ref = BRANCHES[-1]
paths = sorted(set().union(*(files_of(b) for b in BRANCHES)))
bad = 0

for rel in paths:
    base = read(ref, rel)
    odd = [b for b in BRANCHES if read(b, rel) != base]

    if odd:
        bad += 1
        print('%-60s differs on: %s' % (rel, ', '.join(odd)) + ('' if base is not None else '  (missing on %s)' % ref))

        if '--diff' in sys.argv and base is not None:
            for b in odd:
                other = read(b, rel)
                diff = difflib.unified_diff((other or b'').decode('utf-8', 'replace').splitlines(), base.decode('utf-8', 'replace').splitlines(), b, ref, lineterm='', n=1)
                print('\n'.join(list(diff)[:30]))

print('%d shared files checked on %d branches (reference %s), %d differ' % (len(paths), len(BRANCHES), ref, bad))
sys.exit(1 if bad else 0)
