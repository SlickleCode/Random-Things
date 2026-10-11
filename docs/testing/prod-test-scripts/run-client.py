"""Launch the production (SRG-named) Forge 1.16.5 client from prod-test/1.16.5/client without the Minecraft launcher.

usage: python run-client.py [extra -D system properties ...]
Example: python run-client.py -Drandomthings.autotest=true -Drandomthings.autotest.noguis=true
"""
import json, os, subprocess, sys

root = os.path.dirname(os.path.abspath(__file__))
client = os.path.join(root, 'client')
game_dir = os.path.join(root, 'client-run')
assets = os.path.expanduser('~/.gradle/caches/forge_gradle/assets')


def allowed(lib):
    rules = lib.get('rules')
    if not rules:
        return True
    ok = False
    for r in rules:
        osn = r.get('os', {}).get('name')
        if osn is None or osn == 'windows':
            ok = (r['action'] == 'allow')
    return ok


forge = json.load(open(os.path.join(client, 'versions/1.16.5-forge-36.2.39/1.16.5-forge-36.2.39.json')))
vanilla = json.load(open(os.path.expanduser('~/.gradle/caches/forge_gradle/minecraft_repo/versions/1.16.5/version.json')))
cp = []
seen = set()
for lib in forge['libraries'] + vanilla['libraries']:
    if not allowed(lib):
        continue
    art = lib.get('downloads', {}).get('artifact')
    if not art:
        continue
    path = os.path.join(client, 'libraries', art['path'].replace('/', os.sep))
    if not os.path.exists(path):
        print('missing library', path)
        continue
    # one version per group:artifact(:classifier)
    parts = art['path'].split('/')
    key = '/'.join(parts[:-2]) + (':' + parts[-1].split('-natives-')[-1] if '-natives-' in parts[-1] else '')
    if key in seen:
        continue
    seen.add(key)
    cp.append(path)
cp.append(os.path.join(client, 'versions/1.16.5/1.16.5.jar'))

# natives: unpack every *-natives-windows jar's dlls into one folder and point java.library.path at it
import zipfile
natives = os.path.join(client, 'natives')
os.makedirs(natives, exist_ok=True)
native_jars = []
for lib in vanilla['libraries']:
    nat = lib.get('natives', {}).get('windows')
    art = lib.get('downloads', {}).get('classifiers', {}).get(nat.replace('${arch}', '64')) if nat else None
    if art and allowed(lib):
        native_jars.append(os.path.join(client, 'libraries', art['path'].replace('/', os.sep)))
for jar in native_jars:
    if os.path.exists(jar):
        with zipfile.ZipFile(jar) as z:
            for n in z.namelist():
                if n.endswith('.dll'):
                    open(os.path.join(natives, os.path.basename(n)), 'wb').write(z.read(n))

os.makedirs(game_dir, exist_ok=True)
args = ['java', '-Xmx2G', '-XX:+IgnoreUnrecognizedVMOptions',
        '--add-exports=java.base/sun.security.util=ALL-UNNAMED', '--add-exports=jdk.naming.dns/com.sun.jndi.dns=java.naming',
        '--add-opens=java.base/java.util.jar=ALL-UNNAMED',
        '-Drandomthings.debug=true', '-Djava.library.path=' + natives,
        '-DlibraryDirectory=' + os.path.join(client, 'libraries'),
        '-Dminecraft.launcher.brand=prod-test', '-Dminecraft.launcher.version=1']
args += sys.argv[1:]
args += ['-cp', os.pathsep.join(cp), forge['mainClass'],
         '--launchTarget', 'fmlclient', '--fml.forgeVersion', '36.2.39', '--fml.mcVersion', '1.16.5',
         '--fml.forgeGroup', 'net.minecraftforge', '--fml.mcpVersion', '20210115.111550',
         '--username', 'Dev', '--version', '1.16.5-forge-36.2.39', '--gameDir', game_dir,
         '--assetsDir', assets, '--assetIndex', vanilla['assetIndex']['id'], '--accessToken', '0', '--userType', 'mojang',
         '--versionType', 'release']
sys.exit(subprocess.call(args, cwd=game_dir))
