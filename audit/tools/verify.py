#!/usr/bin/env python3
"""Phase 4 verification. Run from anywhere: python3 -I audit/tools/verify.py
Checks: ascii, categories, schema, generated pages, orphan pages, dead links/media in docs/*.html, recipe key coverage,
version wording, and that every registered item/block display name appears on some page (or is an explicit exemption)."""
import json, glob, os, re, sys, html
W = '/home/user/Random-Things'
MOD = '/tmp/claude-0/-home-user-Random-Things/f859bde5-7624-5f08-9a19-e526dde76d34/scratchpad/mod1144'
fail = []; info = []
cats = {c['id'] for c in json.load(open(W + '/data/categories.json'))}
feats = {}
for p in sorted(glob.glob(W + '/data/features/*.json')):
    i = os.path.basename(p)[:-5]; raw = open(p, encoding='utf-8').read()
    d = json.loads(raw); feats[i] = d
    if any(ord(c) > 127 for c in raw): fail.append(f'{i}: non-ascii')
    for k in ('title', 'category', 'description'):
        if not d.get(k): fail.append(f'{i}: missing {k}')
    if d['category'] not in cats: fail.append(f'{i}: bad category')
    if not os.path.exists(f'{W}/docs/{i}.html'): fail.append(f'{i}: no generated page')
    for pr in d.get('properties', []):
        if set(pr) != {'label', 'value'}: fail.append(f'{i}: bad property row')
    for r in d.get('recipes', []):
        if len(r['shape']) != 3 or any(len(row) != 3 for row in r['shape']): fail.append(f'{i}: recipe shape not 3x3')
        if not r['result'].get('key'): fail.append(f'{i}: recipe without result')
    for k in ('images', 'recipe_images', 'videos'):
        for ref in d.get(k, []) or []:
            if not os.path.exists(f'{W}/docs/{ref}'): fail.append(f'{i}: missing {k} file {ref}')
for p in glob.glob(W + '/docs/*.html'):
    n = os.path.basename(p)[:-5]
    if n != 'index' and n not in feats: fail.append(f'orphan page docs/{n}.html')
# dead links in generated html
pat = re.compile(r'(?:href|src)="([^"#]+)(?:#[^"]*)?"')
for p in glob.glob(W + '/docs/*.html'):
    for ref in pat.findall(open(p, encoding='utf-8').read()):
        if ref.startswith(('http:', 'https:', 'mailto:', 'data:', '//', 'javascript:')): continue
        ref = html.unescape(ref)
        if not os.path.exists(os.path.join(W, 'docs', ref)): fail.append(f'{os.path.basename(p)}: dead link/media {ref}')
# category pages / index lists every feature
idx = open(W + '/docs/index.html', encoding='utf-8').read()
for i in feats:
    if f'{i}.html' not in idx: fail.append(f'{i}: not linked from index')
# version wording
ver = re.compile(r"1\.\d{1,2}\.\d|\b1\.1[0-9]\b|\b1\.2[01]\b|\bported\b|\bthe port\b|originally|previously|in this version|since version", re.I)
for i, d in feats.items():
    t = d['description'] + ' ' + ' '.join(str(x) for x in d.get('properties', []))
    for m in ver.finditer(t): fail.append(f'{i}: version wording "{m.group(0)}"')
# recipe key coverage (informational)
labels = set()
for d in feats.values():
    for r in d.get('recipes', []):
        for row in r['shape']:
            for s in row:
                if s:
                    k = s if isinstance(s, str) else s['key']
                    if not any(os.path.exists(f'{W}/docs/textures/{sd}/{k}.png') for sd in ('item', 'block', 'models/armor', 'vanilla')) and k not in feats: labels.add(k)
info.append(f'recipe keys shown as text labels (no icon): {len(labels)}')
# registry coverage by display name
lang = json.load(open(MOD + '/src/main/resources/assets/randomthings/lang/en_us.json'))
reg = []
for line in open(W + '/audit/facts/H_registry.md'):
    m = re.match(r'\| (item|block|blockitem) \| ([a-z0-9_]+) \|', line)
    if m: reg.append(m.group(2))
reg = sorted(set(reg))
corpus = {}
for i, d in feats.items():
    corpus[i] = (d['title'] + ' ' + d['description'] + ' ' + ' '.join(str(x) for x in d.get('properties', [])) + ' ' +
                 ' '.join((r['result'].get('label') or r['result']['key'].replace('_', ' ')) for r in d.get('recipes', []))).lower()
allt = ' '.join(corpus.values())
EXEMPT = {'blazing_fire': 'block of Blaze and Steel fire, not obtainable', 'spectre_core': 'covered in spectre_ingot / spectre_key text',
          'advanced_redstone_wall_torch': 'wall variant of the advanced redstone torch', 'bean_sprout': 'part of Beans (page left as is)',
          'bean_stalk': 'part of Beans (page left as is)', 'lesser_bean_stalk': 'part of Beans (page left as is)', 'bean_pod': 'part of Beans (page left as is)',
          'magic_bean': 'part of Beans (page left as is)', 'lesser_magic_bean': 'part of Beans (page left as is)', 'rune_base': 'Runic Dust (page left as is)',
          'rune_pattern': 'Runic Dust (page left as is)', 'spirit_spawn_egg': 'creative-only spawn egg of the Spirit (ectoplasm page)',
          'floo_brick': 'Floo Bricks, floo page', 'spectre_leaf': 'Spectre Sapling (page left as is)', 'spectre_log': 'Spectre Sapling (page left as is)',
          'spectre_plank': 'Spectre Sapling (page left as is)', 'spectre_block': 'Spectre Sapling / key pages'}
missing = []
for n in reg:
    name = lang.get('item.randomthings.' + n) or lang.get('block.randomthings.' + n) or n.replace('_', ' ').title()
    if name.lower() in allt or n in EXEMPT: continue
    # accept family names, e.g. luminous_block_red -> pages mention "luminous block"
    base = re.sub(r'_(' + '|'.join(['white','orange','magenta','light_blue','yellow','lime','pink','gray','light_gray','cyan','purple','blue','brown','green','red','black','normal','redstone','ender','genesis','oak','spruce','birch','jungle','acacia','darkoak','sun','rain','storm','coal','iron','gold','lapis','emerald','diamond','vanilla']) + r')$', '', n)
    base_name = (lang.get('item.randomthings.' + base) or lang.get('block.randomthings.' + base) or base.replace('_', ' ').title()).lower()
    if base_name in allt or base.replace('_', ' ') in allt: continue
    missing.append((n, name))
info.append(f'registered item/block names not found on any page text: {len(missing)}')
for n, name in missing: info.append(f'   - {n} ("{name}")')
print('FAILURES:', len(fail)); [print('  ' + f) for f in fail]
print('\n'.join(info))
print('pages:', len(feats), 'by category:', {c: sum(1 for d in feats.values() if d['category'] == c) for c in cats})
sys.exit(1 if fail else 0)
