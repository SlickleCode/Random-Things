"""1.20: Material/MaterialColor are gone. Block.Properties.of(Material.X[, MaterialColor.Y]) -> of().mapColor(MapColor.Y)[.instrument(..)][.pushReaction(..)]
python m1_1201_material.py <src root>"""
import re, sys, pathlib

MAT = {  # material: (default map colour, extra builder calls)
    'STONE': ('STONE', '.instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM)'),
    'METAL': ('METAL', '.instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.IRON_XYLOPHONE)'),
    'WOOD': ('WOOD', '.instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS).ignitedByLava()'),
    'GLASS': ('NONE', '.instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.HAT)'),
    'ICE': ('ICE', '.instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.HAT)'),
    'SAND': ('SAND', '.instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.SNARE)'),
    'PLANT': ('PLANT', '.pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)'),
    'GRASS': ('GRASS', ''),
    'DIRT': ('DIRT', ''),
    'CLAY': ('CLAY', '.instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.FLUTE)'),
    'BUILDABLE_GLASS': ('NONE', '.instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.HAT)'),
    'LEAVES': ('PLANT', '.ignitedByLava().pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)'),
    'DECORATION': ('NONE', '.pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY)'),
}
pat = re.compile(r'(Block\.Properties|Properties)\.of\(\s*(?:net\.minecraft\.world\.level\.material\.)?Material\.(\w+)(?:\s*,\s*(?:net\.minecraft\.world\.level\.material\.)?MaterialColor\.(\w+))?\s*\)')
n = 0
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s = p.read_bytes().decode('utf-8')
    def rep(m):
        global n
        mat = m.group(2)
        if mat not in MAT:
            print('UNKNOWN material', mat, p); return m.group(0)
        color = m.group(3) or MAT[mat][0]
        n += 1
        return '%s.of().mapColor(net.minecraft.world.level.material.MapColor.%s)%s' % (m.group(1), color, MAT[mat][1])
    t = pat.sub(rep, s)
    if t != s:
        p.write_bytes(t.encode('utf-8'))
print('converted', n)
