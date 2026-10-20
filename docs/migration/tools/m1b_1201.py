"""1.20.1 second bulk pass. python m1b_1201.py <src root>"""
import re, sys, pathlib
sys.path.insert(0, str(pathlib.Path(__file__).parent))
from m1_1201_gui import match_paren

def add_import(s, imp):
    if 'import %s;' % imp in s: return s
    return re.sub(r'(^|\n)(package [^;]+;\r?\n)', r'\1\2\nimport %s;\n' % imp, s, count=1)

n = 0
for p in pathlib.Path(sys.argv[1]).rglob('*.java'):
    s = p.read_bytes().decode('utf-8'); o = s
    s = s.replace('net.minecraft.core.Registries.', 'net.minecraft.core.registries.Registries.')
    s = s.replace('ItemTransforms.ItemDisplayContext', 'ItemDisplayContext')
    s = s.replace('com.mojang.math.Vector3f', 'org.joml.Vector3f')
    s = s.replace('net.minecraftforge.items.ForgeCapabilities', 'net.minecraftforge.common.capabilities.ForgeCapabilities')
    s = re.sub(r'\bCapabilityFluidHandler\.FLUID_HANDLER_ITEM_CAPABILITY\b', 'ForgeCapabilities.FLUID_HANDLER_ITEM', s)
    s = re.sub(r'\bForgeMod\.REACH_DISTANCE\b', 'ForgeMod.BLOCK_REACH', s)
    s = re.sub(r'import net\.minecraft\.client\.gui\.GuiComponent;\r?\n', '', s)
    s = re.sub(r'\bVector3f\.(YP|YN|XP|XN|ZP|ZN)\b', r'Axis.\1', s)
    if re.search(r'\bAxis\.(YP|YN|XP|XN|ZP|ZN)\b', s):
        s = add_import(s, 'com.mojang.math.Axis')
    if 'ForgeCapabilities.' in s:
        s = add_import(s, 'net.minecraftforge.common.capabilities.ForgeCapabilities')
    s = re.sub(r'public (net\.minecraft\.network\.protocol\.)?Packet<\?> getAddEntityPacket\(\)', 'public Packet<net.minecraft.network.protocol.game.ClientGamePacketListener> getAddEntityPacket()', s)
    s = re.sub(r'\.playSound\(([^;]*?)SoundEvents\.(UI_BUTTON_CLICK|NOTE_BLOCK_\w+)(?![\w.])', r'.playSound(\1SoundEvents.\2.value()', s)
    # CommandSourceStack.sendSuccess(Component, bool) -> sendSuccess(Supplier<Component>, bool)
    pos = 0; res = ''
    while True:
        i = s.find('.sendSuccess(', pos)
        if i < 0: res += s[pos:]; break
        op = i + len('.sendSuccess')
        cl = match_paren(s, op)
        inner = s[op + 1:cl]
        k = inner.rfind(', ')
        first, last = inner[:k], inner[k:]
        if first.strip().startswith('() ->') or first.strip().startswith('Component.literal') or first.strip().startswith('Component.translatable'):
            res += s[pos:op + 1] + '() -> ' + first + last + ')'
        else:
            res += s[pos:cl + 1]
        pos = cl + 1
    s = res
    if 'TooltipButton' in s:
        s = s.replace('getTooltip()', 'getTooltipText()')
    if s != o:
        p.write_bytes(s.encode('utf-8')); n += 1
print('files changed', n)
