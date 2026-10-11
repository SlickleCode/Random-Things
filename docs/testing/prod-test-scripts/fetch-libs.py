"""Download the vanilla libraries listed in the 1.16.5 version.json that the Forge installer does not fetch (sha1-verified)."""
import hashlib, json, os, urllib.request
client = os.path.join(os.path.dirname(os.path.abspath(__file__)), 'client')
v = json.load(open(os.path.expanduser('~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.16.5/version.json')))
n = size = 0
for lib in v['libraries']:
    rules = lib.get('rules')
    if rules:
        ok = False
        for r in rules:
            o = r.get('os', {}).get('name')
            if o is None or o == 'windows':
                ok = r['action'] == 'allow'
        if not ok:
            continue
    nat = lib.get('natives', {}).get('windows')
    cls = lib.get('downloads', {}).get('classifiers', {})
    for key in ([nat.replace('${arch}', '64')] if nat else []):
        art = cls.get(key)
        if art:
            path = os.path.join(client, 'libraries', art['path'].replace('/', os.sep))
            if not os.path.exists(path):
                os.makedirs(os.path.dirname(path), exist_ok=True)
                data = urllib.request.urlopen(art['url']).read()
                assert hashlib.sha1(data).hexdigest() == art['sha1'], art['path']
                open(path, 'wb').write(data)
                n += 1
    art = lib.get('downloads', {}).get('artifact')
    if not art:
        continue
    path = os.path.join(client, 'libraries', art['path'].replace('/', os.sep))
    if os.path.exists(path):
        continue
    os.makedirs(os.path.dirname(path), exist_ok=True)
    data = urllib.request.urlopen(art['url']).read()
    assert hashlib.sha1(data).hexdigest() == art['sha1'], art['path']
    open(path, 'wb').write(data)
    n += 1
    size += len(data)
print('downloaded', n, 'libraries,', size // 1024, 'KB')
