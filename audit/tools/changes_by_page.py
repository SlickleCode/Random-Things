#!/usr/bin/env python3
"""Per-page change list: compares data/features/*.json in the working tree with HEAD (the committed wiki branch)."""
import json, glob, os, subprocess
W = '/home/user/Random-Things'
def head(i):
    p = subprocess.run(['git', '-C', W, 'show', f'HEAD:data/features/{i}.json'], capture_output=True, text=True)
    return json.loads(p.stdout) if p.returncode == 0 else None
rows = []; new = []; unchanged = []
for p in sorted(glob.glob(W + '/data/features/*.json')):
    i = os.path.basename(p)[:-5]; cur = json.load(open(p)); old = head(i)
    if old is None: new.append((i, cur)); continue
    ch = []
    if cur['category'] != old['category']: ch.append(f"category {old['category']} -> {cur['category']}")
    if cur['title'] != old['title']: ch.append('title')
    if cur['description'] != old['description']:
        ch.append(f"description ({len(old['description'])} -> {len(cur['description'])} chars)")
    if cur.get('properties') != old.get('properties'): ch.append(f"properties ({len(old.get('properties') or [])} -> {len(cur.get('properties') or [])} rows)")
    if cur.get('keywords') != old.get('keywords'): ch.append('keywords')
    if cur.get('recipes') != old.get('recipes') or cur.get('recipe') != old.get('recipe'):
        ch.append(f"recipes: {len(old.get('recipe_images') or [])} picture(s)/{1 if old.get('recipe') else 0} grid -> {len(cur.get('recipes') or [])} structured grid(s)")
    if (cur.get('images'), cur.get('videos')) != (old.get('images'), old.get('videos')): ch.append('media changed')
    (rows if ch else unchanged).append((i, ch))
out = ['# Changes by page (working tree vs committed wiki branch HEAD)', '',
       f'{len(rows)} existing pages changed, {len(new)} new pages, {len(unchanged)} unchanged.', '', '## Changed pages', '']
for i, ch in rows: out.append(f'- `{i}`: ' + '; '.join(ch))
out += ['', '## New pages', '']
for i, d in new: out.append(f"- `{i}` ({d['category']}): {d['title']}; {len(d.get('recipes') or [])} recipe(s), {len(d.get('properties') or [])} property rows")
out += ['', '## Unchanged pages', '', ', '.join(f'`{i}`' for i, _ in unchanged), '']
open(W + '/audit/CHANGES_BY_PAGE.md', 'w').write('\n'.join(out))
print('\n'.join(out[:6])); print(unchanged)
