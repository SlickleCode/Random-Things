#!/usr/bin/env python3
"""One-off Phase 3 helper: builds structured `recipes` for data/features/*.json from the mod's recipe JSON.

Usage: python3 -I build_recipes.py <MOD_RECIPES_DIR> [--write]
Without --write it only prints a report. Pages left as is (user decision J1) and Legacy pages are skipped.
Pages whose mod recipes are only "special" serializers keep their recipe pictures.
"""
import json, os, re, sys, glob, collections

W = '/home/user/Random-Things'
REC = sys.argv[1]
WRITE = '--write' in sys.argv

SKIP_PAGES = set()  # the six pages left as is were released on request once their features existed
COLORS = ['white', 'orange', 'magenta', 'light_blue', 'yellow', 'lime', 'pink', 'gray', 'light_gray', 'cyan',
          'purple', 'blue', 'brown', 'green', 'red', 'black']

# recipe file (without .json) -> page id.  Anything not listed here is looked up in OVERRIDE_PREFIX or skipped.
FILE_TO_PAGE = {}
for line in open(W + '/audit/facts/H_registry.md'):
    m = re.match(r'\| (\S+)\.json \| .* \| ([a-z_]+)( \(.*|\s*/.*)? \|$', line.rstrip())
    if m and m.group(2) != 'UNMAPPED':
        FILE_TO_PAGE[m.group(1)] = m.group(2)
FILE_TO_PAGE.update({
    'id_card': 'id_card', 'biome_sensor': 'biome_radar', 'redstone_tool': 'redstone_tool',
    'spectre_string': 'spectre_ingot', 'spectre_ingot': 'spectre_ingot', 'spectre_ingot_bulk': 'spectre_ingot',
    'super_lubricent_ice': 'super_lubricent_ice', 'super_lubricent_platform': 'super_lubricent_ice',
    'filtered_super_lubricent_platform': 'super_lubricent_ice', 'super_lubricent_tincture': 'super_lubricent',
    'advanced_item_collector': 'item_collector', 'basic_redstone_interface': 'redstone_interface',
    'rainbowlamp': 'rainbow_lamp', 'spectre_plank': 'spectre_sapling', 'blackout_powder': 'spectre_illuminator', 'evil_tear': 'artificial_end_portal',
    'ender_anchor': 'ender_bridge', 'prismarine_ender_bridge': 'ender_bridge', 'ender_mailbox': 'ender_mailbox',
    'global_chat_detector': 'chat_detector', 'reinforced_ender_bucket': 'ender_bucket',
    'spectre_energy_injector': 'spectre_charger',
})
for c in COLORS:
    FILE_TO_PAGE['luminous_block_' + c] = 'luminous_blocks'
    FILE_TO_PAGE['luminous_block_translucent_' + c] = 'luminous_blocks'
    FILE_TO_PAGE['stained_brick_' + c] = 'stained_bricks'
    FILE_TO_PAGE['luminous_stained_brick_' + c] = 'stained_bricks'
    FILE_TO_PAGE['grass_seeds_' + c] = 'colored_grass'
FILE_TO_PAGE['grass_seeds'] = 'colored_grass'
for c in COLORS:
    FILE_TO_PAGE['rune_dust_' + c] = 'runic_dust'
EXTRA_PAGES = {'spectre_energy_injector': ['spectre_coils']}

TAGS = {
    'forge:ingots/iron': ('iron_ingot', 'Iron Ingot'), 'forge:stone': ('stone', 'Stone'),
    'forge:cobblestone': ('cobblestone', 'Cobblestone'), 'forge:glass': ('glass', 'Glass'),
    'forge:rods/wooden': ('stick', 'Stick'), 'minecraft:signs': ('oak_sign', 'Any Sign'),
    'forge:slimeballs': ('slime_ball', 'Slimeball'), 'forge:gems/lapis': ('lapis_lazuli', 'Lapis Lazuli'),
    'minecraft:planks': ('oak_planks', 'Any Planks'), 'forge:nuggets/gold': ('gold_nugget', 'Gold Nugget'),
}

def slot_from_ing(ing):
    if isinstance(ing, list):
        keys = [slot_from_ing(i) for i in ing]
        return {'key': keys[0]['key'], 'label': ' or '.join(k.get('label') or k['key'] for k in keys)}
    if 'tag' in ing:
        t = ing['tag']
        if t in TAGS:
            k, l = TAGS[t]
            return {'key': k, 'label': l}
        if t.startswith('forge:dyes/'):
            c = t.split('/')[1]
            return {'key': c + '_dye', 'label': c.replace('_', ' ').title() + ' Dye'}
        raise SystemExit('unknown tag ' + t)
    return {'key': ing['item'].split(':', 1)[1]}

def norm(slot):
    if slot is None:
        return None
    if set(slot) == {'key'}:
        return slot['key']
    return slot

def pad(shape):
    rows = [list(r) + [None] * (3 - len(r)) for r in shape]
    while len(rows) < 3:
        rows.append([None, None, None])
    return rows

def build(path, generic_dye=False):
    d = json.load(open(path))
    t = d['type']
    if t == 'minecraft:crafting_shaped':
        keys = {k: slot_from_ing(v) for k, v in d['key'].items()}
        shape = [[None if ch == ' ' else keys[ch] for ch in row] for row in d['pattern']]
        note = None
    elif t == 'minecraft:crafting_shapeless':
        ings = [slot_from_ing(i) for i in d['ingredients']]
        n = len(ings)
        width = n if n <= 3 else (2 if n == 4 else 3)
        shape = [ings[i:i + width] for i in range(0, n, width)]
        note = 'Shapeless: the ingredients can be placed anywhere in the crafting grid.'
    else:
        return None
    if generic_dye:
        for row in shape:
            for i, s in enumerate(row):
                if s and s['key'].endswith('_dye'):
                    row[i] = {'key': 'dye', 'label': 'Any Dye'}
    res = d['result']
    if res['item'].endswith('super_lubricent_tincture'):
        for row in shape:
            for i, sl in enumerate(row):
                if sl and sl['key'] == 'potion': row[i] = {'key': 'potion', 'label': 'Water Bottle'}
    out = {'shape': [[norm(s) for s in r] for r in pad(shape)],
           'result': {'key': res['item'].split(':', 1)[1], 'count': res.get('count', 1)}}
    if note:
        out['note'] = note
    return out

def merge_note(r, extra):
    r['note'] = (r['note'] + ' ' + extra) if r.get('note') else extra
    return r

pages = collections.defaultdict(list)
for f in sorted(glob.glob(REC + '/*.json')):
    name = os.path.basename(f)[:-5]
    pages[FILE_TO_PAGE.get(name) or '?'].append(name)
    for extra in EXTRA_PAGES.get(name, []):
        pages[extra].append(name)

result = {}
special = collections.defaultdict(list)
for page, files in sorted(pages.items()):
    if page in ('?', None) or page in SKIP_PAGES:
        continue
    recs = []
    done_families = set()
    for name in files:
        fam = next((p for p in ('luminous_block_translucent_', 'luminous_block_', 'luminous_stained_brick_', 'stained_brick_', 'grass_seeds_', 'rune_dust_')
                    if name.startswith(p) and name[len(p):] in COLORS), None)
        if fam:
            if fam in done_families:
                continue
            done_families.add(fam)
            # representative: white (any dye) - falls back to first
            rep = fam + 'white' if os.path.exists(f'{REC}/{fam}white.json') else name
            r = build(f'{REC}/{rep}.json', generic_dye=True)
            if fam == 'luminous_block_translucent_':
                r['shape'][1][1] = {'key': 'luminous_block', 'label': 'Luminous Block (any color)'}
                r['result'] = {'key': 'luminous_block_translucent', 'count': 1, 'label': 'Translucent Luminous Block'}
                merge_note(r, 'The result has the same color as the Luminous Block used.')
            elif fam == 'luminous_block_':
                r['result'] = {'key': 'luminous_block', 'count': r['result']['count'], 'label': 'Luminous Block'}
                merge_note(r, 'The dye decides the color; all 16 dye colors work.')
            elif fam == 'stained_brick_':
                r['result'] = {'key': 'stained_brick', 'count': r['result']['count'], 'label': 'Stained Brick'}
                merge_note(r, 'The dye decides the color; all 16 dye colors work.')
            elif fam == 'luminous_stained_brick_':
                r['shape'][0][0] = {'key': 'stained_brick', 'label': 'Stained Brick (any color)'}
                r['result'] = {'key': 'luminous_stained_brick', 'count': 1, 'label': 'Luminous Stained Brick'}
                merge_note(r, 'The result has the same color as the Stained Brick used.')
            elif fam == 'rune_dust_':
                r['result'] = {'key': 'rune_dust', 'count': r['result']['count'], 'label': 'Runic Dust'}
                merge_note(r, 'The dye decides the color; all 16 dye colors work.')
            elif fam == 'grass_seeds_':
                r['shape'][0][0] = {'key': 'grass_seeds', 'label': 'Grass Seeds'}
                r['result'] = {'key': 'grass_seeds', 'count': 1, 'label': 'Colored Grass Seeds'}
                merge_note(r, 'The dye decides the color; all 16 dye colors work.')
            recs.append(r)
            continue
        if name.startswith('platform_') and page == 'platforms':
            if name != 'platform_oak':
                continue
            r = build(f'{REC}/{name}.json')
            r['result']['label'] = 'Platform'
            merge_note(r, 'Use spruce, birch, jungle, acacia or dark oak planks instead of oak planks to get the other platform types.')
            recs.append(r)
            continue
        r = build(f'{REC}/{name}.json')
        if r is None:
            special[page].append(name)
            continue
        recs.append(r)
    if recs:
        result[page] = recs

report = []
for page, recs in sorted(result.items()):
    p = f'{W}/data/features/{page}.json'
    if not os.path.exists(p):
        report.append(f'!! no page file for {page} ({len(recs)} recipes)')
        continue
    raw = open(p).read()
    d = json.loads(raw)
    if d.get('category') == 'legacy':
        continue
    d.pop('recipe', None)
    d.pop('recipe_images', None)
    d['recipes'] = recs
    report.append(f'{page}: {len(recs)} recipe(s)' + (f'; special kept as text: {special[page]}' if special.get(page) else ''))
    if WRITE:
        open(p, 'w').write(json.dumps(d, indent=2, ensure_ascii=True) + ('\n' if raw.endswith('\n') else ''))
print('\n'.join(report))
print('pages with only special recipes (pictures kept):', {k: v for k, v in special.items() if k not in result})
print('recipe files not mapped to any page:', [n for n in pages.get('?', [])] + [n for n in pages.get(None, [])])
print('pages skipped by decision with recipes:', {p: pages[p] for p in SKIP_PAGES if p in pages})
