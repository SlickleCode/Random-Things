#!/usr/bin/env python3
"""Mechanical parity check: compares each version branch with origin/1.14.4 (read-only git show/ls-tree).
Compares: java class basenames, RTConfig keys/defaults, recipe contents (normalised), lang keys. Writes audit/verify/PARITY_MECH.md"""
import subprocess, json, re, collections, sys
R = '/home/user/Random-Things'
BASE = '1.14.4'
BR = ['1.16.5', '1.18.2', '1.19.2', '1.20.1', '1.20.6', '1.21.1', '1.21.4', '1.21.11']

def git(*a, check=False):
    p = subprocess.run(['git', '-C', R] + list(a), capture_output=True, text=True, errors='replace')
    return p.stdout if p.returncode == 0 else ''

def files(br):
    return git('ls-tree', '-r', '--name-only', 'origin/' + br).splitlines()

def show(br, path):
    return git('show', f'origin/{br}:{path}')

def classes(fl):
    return {f.rsplit('/', 1)[1][:-5]: f for f in fl if f.endswith('.java') and '/lumien/randomthings/' in f and '/debug/' not in f}

def cfg(br, fl):
    c = [f for f in fl if f.endswith('RTConfig.java')]
    out = {}
    for f in c:
        t = show(br, f)
        for m in re.finditer(r'define(?:InRange)?\(\s*"([A-Za-z_]+)"\s*,\s*([^,)]+)', t):
            out[m.group(1)] = m.group(2).strip()
    return out

ALIAS = {'stones': 'stone', 'cobblestones': 'cobblestone', 'glass_blocks': 'glass', 'rods/wooden': 'rods/wooden',
         'slime_balls': 'slimeballs', 'dyes/light_blue': 'dyes/light_blue', 'ingots/iron': 'ingots/iron'}

def norm_tag(v):
    if v.startswith('forge:'): v = 'c:' + v[6:]
    ns, _, rest = v.partition(':')
    parts = rest.split('/')
    parts[0] = ALIAS.get(parts[0], parts[0])
    return 'tag:' + ns + ':' + '/'.join(parts)

def norm_ing(i):
    if isinstance(i, list): return '|'.join(sorted(norm_ing(x) for x in i))
    if isinstance(i, str):
        return norm_tag(i[1:]) if i.startswith('#') else 'item:' + i
    if 'tag' in i: return norm_tag(i['tag'])
    if 'item' in i: return 'item:' + i['item']
    return json.dumps(i, sort_keys=True)

def norm_recipe(d):
    t = d.get('type', '').split(':')[-1]
    res = d.get('result')
    if isinstance(res, dict):
        rid = res.get('item') or res.get('id'); cnt = res.get('count', 1)
    else:
        rid = res; cnt = 1
    if t == 'crafting_shaped':
        keys = {k: norm_ing(v) for k, v in d.get('key', {}).items()}
        pat = [''.join(keys.get(ch, ' ') if ch != ' ' else ' ' for ch in []) for _ in []]
        grid = ['/'.join(keys.get(ch, '_') if ch != ' ' else '_' for ch in row) for row in d['pattern']]
        return (t, rid, cnt, tuple(grid))
    if t == 'crafting_shapeless':
        return (t, rid, cnt, tuple(sorted(norm_ing(i) for i in d['ingredients'])))
    return (t, rid, cnt, None)

def recipes(br, fl):
    out = {}
    for f in fl:
        if '/data/randomthings/recipe' in f and f.endswith('.json'):
            try: out[f.rsplit('/', 1)[1][:-5]] = norm_recipe(json.loads(show(br, f)))
            except Exception as e: out[f.rsplit('/', 1)[1][:-5]] = ('ERR', str(e)[:40], 0, None)
    return out

def lang(br, fl):
    c = [f for f in fl if f.endswith('assets/randomthings/lang/en_us.json')]
    if not c: return {}
    try: return json.loads(show(br, c[0]))
    except Exception: return {}

bf = files(BASE); bc = classes(bf); bcfg = cfg(BASE, bf); brec = recipes(BASE, bf); bl = lang(BASE, bf)
out = ['# Mechanical parity check vs 1.14.4', '', f'Baseline: {len(bc)} classes (without debug), {len(bcfg)} config keys, {len(brec)} recipes, {len(bl)} lang keys.', '']
for br in BR:
    fl = files(br)
    c = classes(fl); cf = cfg(br, fl); rc = recipes(br, fl); lg = lang(br, fl)
    out.append(f'## {br}')
    out.append(f'classes {len(c)}, config keys {len(cf)}, recipes {len(rc)}, lang keys {len(lg)}')
    miss = sorted(set(bc) - set(c)); extra = sorted(set(c) - set(bc))
    out.append(f'- classes missing vs 1.14.4 ({len(miss)}): ' + ', '.join(miss[:60]) + (' ...' if len(miss) > 60 else ''))
    out.append(f'- classes only here ({len(extra)}): ' + ', '.join(extra[:40]) + (' ...' if len(extra) > 40 else ''))
    out.append(f'- config keys missing: {sorted(set(bcfg) - set(cf))}; only here: {sorted(set(cf) - set(bcfg))}')
    out.append('- config default differs: ' + str({k: (bcfg[k], cf[k]) for k in bcfg if k in cf and bcfg[k] != cf[k]}))
    rm = sorted(set(brec) - set(rc)); rx = sorted(set(rc) - set(brec))
    rd = sorted(k for k in brec if k in rc and brec[k] != rc[k])
    out.append(f'- recipe files missing: {len(rm)} {rm[:40]}; only here: {len(rx)} {rx[:20]}')
    out.append(f'- recipe content differs ({len(rd)}): ' + '; '.join(f'{k}: {brec[k][1:]} -> {rc[k][1:]}' for k in rd[:25]) + (' ...' if len(rd) > 25 else ''))
    lm = sorted(k for k in bl if k not in lg and (k.startswith('item.') or k.startswith('block.')))
    out.append(f'- item/block lang keys missing: {len(lm)} {lm[:30]}')
    out.append('')
open(R + '/audit/verify/PARITY_MECH.md', 'w').write('\n'.join(out) + '\n')
print('\n'.join(out)[:6000])
