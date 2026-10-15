"""Error-oracle line fixer for the 1.19.2 port: for each javac "cannot find symbol ... method X()" at file:line,
rewrite `.X()` on that line using MAP. Usage: python errfix_1192.py <javac-log>
"""
import re, sys, pathlib, collections

MAP = {
    'getWorld': 'getLevel',
    'getEntityLiving': 'getEntity',
    'getPlayer': 'getEntity',
}
lines = open(sys.argv[1], encoding='utf-8', errors='replace').read().splitlines()
todo = collections.defaultdict(set)
for i, l in enumerate(lines):
    m = re.match(r'(.*?\.java):(\d+): error: cannot find symbol', l)
    if not m:
        continue
    for k in range(i + 1, min(i + 6, len(lines))):
        sm = re.search(r'symbol:\s+method (\w+)\(\)', lines[k])
        if sm and sm.group(1) in MAP:
            todo[m.group(1)].add((int(m.group(2)), sm.group(1)))
            break
n = 0
for f, items in todo.items():
    p = pathlib.Path(f)
    data = p.read_bytes().decode('utf-8')
    src = data.split('\n')
    for ln, name in items:
        new = src[ln - 1].replace('.' + name + '()', '.' + MAP[name] + '()')
        if new != src[ln - 1]:
            src[ln - 1] = new
            n += 1
    p.write_bytes('\n'.join(src).encode('utf-8'))
print('fixed', n)
