"""Generate real default-overworld chunks on the production Forge server in this folder (any version) and count Glowing Mushrooms in the saved region files.
usage: python realgen.py [radius_in_chunks]   (the mod jar must already be in mods/)"""
import glob, os, re, shutil, subprocess, sys, threading, time, zlib

here = os.path.dirname(os.path.abspath(__file__))
radius = int(sys.argv[1]) if len(sys.argv) > 1 else 12
world = os.path.join(here, 'RealGen')
shutil.rmtree(world, ignore_errors=True)
props = os.path.join(here, 'server.properties')
open(props, 'w').write('level-name=RealGen\nlevel-type=default\nlevel-seed=424242\nonline-mode=true\nview-distance=4\ngenerate-structures=true\n')
open(os.path.join(here, 'eula.txt'), 'w').write('eula=true\n')

p = subprocess.Popen(['java', '-Xmx2G', '-jar', [j for j in glob.glob(os.path.join(here, 'forge-*.jar')) if 'installer' not in j and 'universal' not in j and 'server' not in j][0], 'nogui'], cwd=here,
                     stdin=subprocess.PIPE, stdout=open(os.path.join(here, 'realgen.out'), 'w'), stderr=subprocess.STDOUT, text=True)


def send(cmd):
    p.stdin.write(cmd + '\n')
    p.stdin.flush()


def wait_for(text, timeout):
    end = time.time() + timeout
    while time.time() < end:
        if text in open(os.path.join(here, 'realgen.out'), errors='ignore').read():
            return True
        time.sleep(2)
    return False


if not wait_for('Done (', 240):
    p.kill()
    sys.exit('server did not start')
if 'Forge Mod Loader detected missing' in open(os.path.join(here, 'realgen.out'), errors='ignore').read():
    send('/fml confirm')
# forceload accepts at most 256 chunks per command: four 15x15-chunk areas side by side
for cx, cz in ((0, 0), (16, 0), (0, 16), (16, 16)):
    r = 7 * 16 + 8
    send('forceload add %d %d %d %d' % (cx * 16 - r, cz * 16 - r, cx * 16 + r, cz * 16 + r))
    time.sleep(3)
wait_for('Marked', 30)
# wait until the generation load settles (the forced chunks are all full)
last = 0
for _ in range(120):
    time.sleep(3)
    size = os.path.getsize(os.path.join(here, 'realgen.out'))
    if size == last and _ > 10:
        break
    last = size
send('save-all flush')
time.sleep(10)
send('stop')
p.wait(timeout=120)

region = os.path.join(world, 'region')
needle = b'randomthings:glowing_mushroom'
chunks = with_m = 0
for name in os.listdir(region):
    data = open(os.path.join(region, name), 'rb').read()
    for i in range(1024):
        off = int.from_bytes(data[i * 4:i * 4 + 3], 'big') * 4096
        if off == 0:
            continue
        length = int.from_bytes(data[off:off + 4], 'big')
        comp = data[off + 4]
        raw = zlib.decompress(data[off + 5:off + 4 + length]) if comp == 2 else data[off + 5:off + 4 + length]
        chunks += 1
        if needle in raw:
            with_m += 1
print('chunks in region files:', chunks, ' chunks whose palette contains a glowing mushroom:', with_m)
