"""Download the vanilla libraries and Windows natives listed in the vanilla version.json that the Forge installer does not fetch
(sha1-verified). Run from the prod-test/<version>/ folder that contains client/ (any Minecraft version)."""
import glob, hashlib, json, os, urllib.request

here = os.path.dirname(os.path.abspath(__file__))
client = os.path.join(here, 'client')
forge = json.load(open(glob.glob(os.path.join(client, 'versions', '*neoforge*', '*.json'))[0]))
mc = forge['inheritsFrom']
v = json.load(open(os.path.join(client, 'versions', mc, mc + '.json')))


def fetch(art):
    path = os.path.join(client, 'libraries', art['path'].replace('/', os.sep))
    if os.path.exists(path):
        return 0
    os.makedirs(os.path.dirname(path), exist_ok=True)
    data = urllib.request.urlopen(art['url']).read()
    assert hashlib.sha1(data).hexdigest() == art['sha1'], art['path']
    open(path, 'wb').write(data)
    return len(data)


def allowed(lib):
    rules = lib.get('rules')
    if not rules:
        return True
    ok = False
    for r in rules:
        o = r.get('os', {}).get('name')
        if o is None or o == 'windows':
            ok = r['action'] == 'allow'
    return ok


n = size = 0
for lib in v['libraries']:
    if not allowed(lib):
        continue
    nat = lib.get('natives', {}).get('windows')
    classifiers = lib.get('downloads', {}).get('classifiers', {})
    art = classifiers.get(nat.replace('${arch}', '64')) if nat else None
    for a in (art, lib.get('downloads', {}).get('artifact')):
        if a:
            got = fetch(a)
            if got:
                n += 1
                size += got
print('downloaded', n, 'files,', size // 1024, 'KB for Minecraft', mc)
