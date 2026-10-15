"""1.18.2 -> 1.19.2 mechanical rewrites, batch 2 (run once from the worktree root, after m1_1192.py).
- NetworkHooks.openGui -> openScreen
- player.sendMessage(c, Util.NIL_UUID) -> sendSystemMessage(c)
- java.util.Random parameters in overrides -> RandomSource (not asm/: coremod descriptors are checked by hand)
- AbstractContainerMenu.quickMoveStack became abstract: add an EMPTY-returning default where missing
"""
import re, pathlib

root = pathlib.Path('src/main/java')
for p in root.rglob('*.java'):
    t = p.read_bytes().decode('utf-8')
    s = t
    nl = '\r\n' if '\r\n' in s else '\n'
    s = s.replace('NetworkHooks.openGui(', 'NetworkHooks.openScreen(')
    s = re.sub(r'\.sendMessage\((.*), net\.minecraft\.Util\.NIL_UUID\)', r'.sendSystemMessage(\1)', s)
    rel = str(p).replace(chr(92), '/')
    if '/asm/' not in rel:
        s2 = re.sub(r'\bRandom (\w+)(\s*[,)])', r'RandomSource \1\2', s)
        s2 = re.sub(r'\bRandom (\w+) = context\.random\(\);', r'RandomSource \1 = context.random();', s2)
        if s2 != s and 'import net.minecraft.util.RandomSource;' not in s2:
            s2 = re.sub(r'(package [^;]+;\r?\n)', r'\1' + nl + 'import net.minecraft.util.RandomSource;', s2, count=1)
        s = s2
    if s != t:
        p.write_bytes(s.encode('utf-8'))
        print('fixed', rel)

# containers that never overrode quickMoveStack
for p in (root / 'lumien/randomthings/container').glob('*.java'):
    s = p.read_bytes().decode('utf-8')
    if 'extends' in s and 'quickMoveStack' not in s and 'AbstractContainerMenu' in s:
        nl = '\r\n' if '\r\n' in s else '\n'
        i = s.rindex('}')
        add = (nl + '    @Override' + nl + '    public net.minecraft.world.item.ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) {' + nl +
               '        return net.minecraft.world.item.ItemStack.EMPTY;' + nl + '    }' + nl)
        p.write_bytes((s[:i] + add + s[i:]).encode('utf-8'))
        print('quickMove', p.name)
