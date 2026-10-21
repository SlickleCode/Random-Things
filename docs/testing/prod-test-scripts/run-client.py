"""Launch the production (SRG-named) Forge client installed in ./client without the Minecraft launcher (any Minecraft version).

usage (from prod-test/<version>/): python run-client.py [-Dsystem.property=value ...]
Example: python run-client.py -Drandomthings.autotest=true -Drandomthings.autotest.noguis=true -Drandomthings.autotest.exit=true
The built mod jar goes in ./client-run/mods/. Needs the Gradle caches (assets, vanilla version.json) from having built the project once.
"""
import glob, json, os, subprocess, sys, zipfile

here = os.path.dirname(os.path.abspath(__file__))
client = os.path.join(here, 'client')
game_dir = os.path.join(here, 'client-run')
forge_path = glob.glob(os.path.join(client, 'versions', '*neoforge*', '*.json'))[0]
forge = json.load(open(forge_path))
forge_id = os.path.basename(os.path.dirname(forge_path))
mc = forge['inheritsFrom']
vanilla = json.load(open(os.path.join(client, 'versions', mc, mc + '.json')))
assets = os.path.expanduser('~/.gradle/caches/fabric-loom/assets')   # any Gradle cache assets dir holding the asset index (NeoForge installs do not download assets)


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


cp, seen, native_jars = [], set(), []
for lib in forge['libraries'] + vanilla['libraries']:
    if not allowed(lib):
        continue
    nat = lib.get('natives', {}).get('windows')
    art_n = lib.get('downloads', {}).get('classifiers', {}).get(nat.replace('${arch}', '64')) if nat else None
    if art_n:
        native_jars.append(os.path.join(client, 'libraries', art_n['path'].replace('/', os.sep)))
    art = lib.get('downloads', {}).get('artifact')
    if not art:
        continue
    path = os.path.join(client, 'libraries', art['path'].replace('/', os.sep))
    if not os.path.exists(path):
        print('missing library (run fetch-libs.py):', path)
        continue
    parts = art['path'].split('/')
    key = '/'.join(parts[:-2]) + (':' + parts[-1].split('-natives-')[-1] if '-natives-' in parts[-1] else '')
    if key in seen:
        continue
    seen.add(key)
    cp.append(path)
cp.append(os.path.join(client, 'versions', mc, mc + '.jar'))

natives = os.path.join(client, 'natives')
os.makedirs(natives, exist_ok=True)
for jar in native_jars:
    if os.path.exists(jar):
        with zipfile.ZipFile(jar) as z:
            for n in z.namelist():
                if n.endswith('.dll'):
                    open(os.path.join(natives, os.path.basename(n)), 'wb').write(z.read(n))

os.makedirs(game_dir, exist_ok=True)
opts = os.path.join(game_dir, 'options.txt')
if not os.path.exists(opts):
    open(opts, 'w').write('pauseOnLostFocus:false\n')

subst = {'${library_directory}': os.path.join(client, 'libraries'), '${classpath_separator}': os.pathsep, '${version_name}': mc}


def fill(a):
    for k, v in subst.items():
        a = a.replace(k, v)
    return a


# 1.17+ Forge launches through the module path: the version json's own jvm arguments carry it (placeholders filled here)
jvm = [fill(a) for a in forge.get('arguments', {}).get('jvm', []) if isinstance(a, str)]
game = [a for a in forge.get('arguments', {}).get('game', []) if isinstance(a, str)]
args = ['java', '-Xmx2G', '-XX:+IgnoreUnrecognizedVMOptions'] + jvm + [
    '-Drandomthings.debug=true', '-Djava.library.path=' + natives,
    '-Dminecraft.launcher.brand=prod-test', '-Dminecraft.launcher.version=1'] + sys.argv[1:]
args += ['-cp', os.pathsep.join(cp), forge['mainClass']] + game + [
    '--username', 'Dev', '--version', forge_id, '--gameDir', game_dir, '--assetsDir', assets,
    '--assetIndex', mc + '-' + vanilla['assetIndex']['id'], '--accessToken', '0', '--userType', 'mojang', '--versionType', 'release']
sys.exit(subprocess.call(args, cwd=game_dir))
