"""1.20.1 first-phase javac error fixer: python errfix_1201.py <javac log>.
Applies position-exact fixes for the error kinds listed in RULES (the file/line/col come from javac's own output)."""
import re, sys, collections

log = open(sys.argv[1], encoding='utf-8', errors='replace').read().split('\n')
errs = []
for i, l in enumerate(log):
    m = re.match(r'^(.+\.java):(\d+): error: (.*)$', l)
    if m and i + 2 < len(log):
        caret = log[i + 2]
        errs.append((m.group(1), int(m.group(2)), caret.index('^') if '^' in caret else -1, m.group(3), log[i + 1:i + 6]))

files = {}
def text(f):
    if f not in files:
        files[f] = open(f, encoding='utf-8').read().split('\n')
    return files[f]

stats = collections.Counter()
by_line = collections.defaultdict(list)
for f, ln, col, msg, ctx in errs:
    by_line[(f, ln)].append((col, msg, ctx))

for (f, ln), items in by_line.items():
    lines = text(f)
    s = lines[ln - 1]
    for col, msg, ctx in sorted(items, key=lambda x: -x[0]):
        mm = re.match(r'([xy]) has private access in AbstractWidget', msg)
        if mm and col >= 0:
            j = col + 1 if s[col] == '.' else col
            if s[j] == mm.group(1) and not s[j + 1:j + 2].isalnum():
                s = s[:j] + 'get' + mm.group(1).upper() + '()' + s[j + 1:]
                stats['getX/getY'] += 1
        if msg.startswith('incompatible types: possible lossy conversion from double to int') and 'new BlockPos(' in s:
            s = s.replace('new BlockPos(', 'BlockPos.containing(')
            stats['BlockPos.containing'] += 1
        if msg.startswith('cannot find symbol') and any('method getLevel()' in c for c in ctx) and '.getLevel()' in s:
            s = re.sub(r'(ServerLevel \w+ = [\w.]+)\.getLevel\(\)', r'\1.serverLevel()', s)
            s = s.replace('.getLevel()', '.level()') if 'ServerLevel ' not in s else s
            stats['getLevel'] += 1
        if msg.startswith('level has private access in Entity') and col >= 0:
            # caret is on the '.' of `x.level` or on a bare `level`
            j = col + 1 if s[col] == '.' else col
            if s[j:j + 5] == 'level' and s[j + 5:j + 6] != '(':
                s = s[:j + 5] + '()' + s[j + 5:]
                stats['level()'] += 1
    lines[ln - 1] = s

for f, lines in files.items():
    open(f, 'w', encoding='utf-8', newline='').write('\n'.join(lines))
print(dict(stats))
