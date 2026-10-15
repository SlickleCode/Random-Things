import re, glob, os, sys, collections
ROOT = 'D:/Projects/Clauding/Random-Things/1.18.2/src/main/java/lumien/randomthings/'

def rd(f): return open(f, encoding='utf8', newline='').read()
def wr(f, s): open(f, 'w', encoding='utf8', newline='').write(s)

def add_import(s, fq):
    if re.search(r'^import ' + re.escape(fq) + ';', s, re.M): return s
    return re.sub(r'(package [^;]+;\r?\n)', lambda m: m.group(1) + '\nimport ' + fq + ';\n', s, count=1)

def method_span(s, start):
    """index of matching closing brace for the first '{' at/after start"""
    i = s.index('{', start); d = 0
    while True:
        c = s[i]
        if c == '{': d += 1
        elif c == '}':
            d -= 1
            if d == 0: return i
        i += 1

ticking = {}   # TE simple class -> ModTileEntityTypes constant
manual = []

for f in glob.glob(ROOT + 'tileentity/**/*.java', recursive=True):
    s = rd(f); o = s
    name = os.path.basename(f)[:-5]
    if name.startswith('ModTileEntityTypes') or 'extends' not in s: continue
    if not re.search(r'class \w+ extends (BlockEntity|\w+TileEntity)\b', s): continue
    import subprocess
    rel = f.replace(chr(92), '/').split('1.18.2/')[1]
    orig = subprocess.run(['git','-C','D:/Projects/Clauding/Random-Things/1.18.2','show','HEAD:'+rel],capture_output=True,text=True,encoding='utf8').stdout
    is_ticking = 'ITickableTileEntity' in orig
    # constructors
    s, n1 = re.subn(r'public (%s)\(\)\s*\{\s*(this|super)\((.*?)\);\s*\}' % name,
                    r'public \1(BlockPos pos, BlockState state) {\n        \2(\3, pos, state);\n    }', s, flags=re.S)
    s, n2 = re.subn(r'(protected %s)\(BlockEntityType<\?> type\)\s*\{\s*super\(type\);\s*\}' % name,
                    r'\1(BlockEntityType<?> type, BlockPos pos, BlockState state) {\n        super(type, pos, state);\n    }', s, flags=re.S)
    if n1 + n2 == 0: manual.append((name, 'ctor'))
    m = re.search(r'ModTileEntityTypes\.(\w+)', s)
    if is_ticking and m: ticking[name] = m.group(1)
    # save -> saveAdditional
    m = re.search(r'public CompoundTag save\(CompoundTag (\w+)\)', s)
    if m:
        var = m.group(1)
        end = method_span(s, m.end())
        body = s[m.start():end + 1]
        rets = re.findall(r'\breturn\b[^;]*;', body)
        nb = body.replace('public CompoundTag save(CompoundTag %s)' % var, 'protected void saveAdditional(CompoundTag %s)' % var)
        nb = nb.replace('super.save(%s);' % var, 'super.saveAdditional(%s);' % var)
        if len(rets) == 1 and rets[0].strip() == 'return %s;' % var:
            nb = re.sub(r'\n[ \t]*return %s;[ \t]*(?=\r?\n)' % var, '', nb)
        else:
            manual.append((name, 'save returns %s' % rets))
        s = s[:m.start()] + nb + s[end + 1:]
    # load(BlockState, CompoundTag)
    s = re.sub(r'public void load\(BlockState (\w+), CompoundTag (\w+)\)', r'public void load(CompoundTag \2)', s)
    s = re.sub(r'super\.load\(\w+, (\w+)\);', r'super.load(\1);', s)
    # tickable
    s = re.sub(r'\s+implements TickableBlockEntity,\s*', ' implements ', s)
    s = re.sub(r'\s+implements TickableBlockEntity\b', '', s)
    s = re.sub(r',\s*TickableBlockEntity\b', '', s)
    s = re.sub(r'@Override\s*\n(\s*)public void tick\(\)', r'public void tick()', s)
    s = s.replace('import net.minecraft.world.level.block.entity.TickableBlockEntity;\n', '').replace('import net.minecraft.world.level.block.entity.TickableBlockEntity;\r\n', '')
    if s != o:
        s = add_import(s, 'net.minecraft.core.BlockPos')
        s = add_import(s, 'net.minecraft.world.level.block.state.BlockState')
        wr(f, s)

# registry
f = ROOT + 'tileentity/ModTileEntityTypes.java'
s = rd(f)
s = s.replace('Supplier<? extends BlockEntity> factoryIn', 'BlockEntityType.BlockEntitySupplier<? extends BlockEntity> factoryIn')
s = s.replace('import java.util.function.Supplier;\n', '').replace('import java.util.function.Supplier;\r\n', '')
wr(f, s)

# blocks
tick_blocks = 0
for f in glob.glob(ROOT + 'block/**/*.java', recursive=True):
    s = rd(f); o = s
    if 'createTileEntity' not in s and 'hasTileEntity' not in s: continue
    # remove hasTileEntity override
    s = re.sub(r'[ \t]*@Override\s*\n[ \t]*public boolean hasTileEntity\(BlockState \w+\)\s*\{\s*return true;\s*\}\s*\n', '', s)
    m = re.search(r'([ \t]*)@Override\s*\n[ \t]*public BlockEntity createTileEntity\(BlockState (\w+), BlockGetter (\w+)\)\s*\{\s*return new (\w+)\(\);\s*\}', s)
    if m:
        te = m.group(4)
        new = m.group(1) + '@Override\n' + m.group(1) + 'public BlockEntity newBlockEntity(BlockPos pos, BlockState state)\n' + m.group(1) + '{\n' + m.group(1) + '    return new ' + te + '(pos, state);\n' + m.group(1) + '}'
        extra_imports = []
        if te in ticking:
            new += '\n\n' + m.group(1) + '@Override\n' + m.group(1) + 'public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)\n' + m.group(1) + '{\n' + m.group(1) + '    return Tickers.of(type, ModTileEntityTypes.' + ticking[te] + ', (l, p, s2, be) -> be.tick());\n' + m.group(1) + '}'
            extra_imports = ['net.minecraft.world.level.block.entity.BlockEntityTicker', 'net.minecraft.world.level.block.entity.BlockEntityType', 'lumien.randomthings.util.Tickers', 'lumien.randomthings.tileentity.ModTileEntityTypes', 'net.minecraft.world.level.Level']
            tick_blocks += 1
        s = s[:m.start()] + new + s[m.end():]
        for imp in extra_imports: s = add_import(s, imp)
        s = add_import(s, 'net.minecraft.core.BlockPos')
        s = add_import(s, 'net.minecraft.world.level.block.EntityBlock')
        hm = re.search(r'((?:public |abstract )*class [^{]+)\{', s)
        if hm and 'EntityBlock' not in hm.group(1):
            decl = hm.group(1).rstrip()
            decl = decl + (', EntityBlock ' if ' implements ' in decl else ' implements EntityBlock ')
            s = s[:hm.start()] + decl + s[hm.end()-1:]
    else:
        manual.append((os.path.basename(f), 'block createTileEntity'))
    if s != o: wr(f, s)

os.makedirs(ROOT + 'util', exist_ok=True)
wr(ROOT + 'util/Tickers.java', '''package lumien.randomthings.util;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Helper for {@code EntityBlock.getTicker}: returns the ticker only when the requested type is the expected one. */
public final class Tickers
{
	private Tickers()
	{
	}

	@SuppressWarnings("unchecked")
	public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> of(BlockEntityType<A> given, BlockEntityType<E> expected, BlockEntityTicker<? super E> ticker)
	{
		return expected == given ? (BlockEntityTicker<A>) ticker : null;
	}
}
''')
print('ticking TEs', len(ticking), 'blocks with ticker', tick_blocks)
for m in manual: print('MANUAL', m)
