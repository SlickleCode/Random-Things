"""1.18.2 -> 1.19.2 mechanical rewrites (run once from the worktree root).
- @ObjectHolder("x") -> @ObjectHolder(registryName="minecraft:R", value="randomthings:x"); class-level holder removed
- registry.register(EXPR.setRegistryName(ARG)) -> registry.register(ARG, EXPR)
- EXPR.getRegistryName() -> RegUtil.id(EXPR)
- TranslatableComponent/TextComponent -> Component.translatable/literal
"""
import re, pathlib, sys
root = pathlib.Path('src/main/java')
HOLDERS = {'block/ModBlocks':'block','item/ModItems':'item','container/ModContainerTypes':'menu',
 'enchantment/ModEnchantments':'enchantment','entity/ModEntityTypes':'entity_type','potion/ModEffects':'mob_effect',
 'tileentity/ModTileEntityTypes':'block_entity_type','worldgen/ModFeatures':'feature'}
base = root/'lumien/randomthings'

def match_close(s, i):
    d = 0
    while i < len(s):
        c = s[i]
        if c == '"':
            i += 1
            while s[i] != '"':
                i += 2 if s[i] == chr(92) else 1
        elif c == '(':
            d += 1
        elif c == ')':
            d -= 1
            if d == 0: return i
        i += 1
    raise ValueError

def match_open_back(s, i):
    d = 0
    while i >= 0:
        c = s[i]
        if c == ')': d += 1
        elif c == '(':
            d -= 1
            if d == 0: return i
        i -= 1
    raise ValueError

def expr_start(s, end):
    """start index of the postfix expression ending at s[end-1] (before '.getRegistryName')"""
    i = end
    while i > 0:
        c = s[i-1]
        if c == ')':
            i = match_open_back(s, i-1)
            continue
        if c == ']':
            d = 0; j = i-1
            while True:
                if s[j] == ']': d += 1
                elif s[j] == '[':
                    d -= 1
                    if d == 0: break
                j -= 1
            i = j; continue
        if c.isalnum() or c in '_.$':
            i -= 1; continue
        break
    if s[:i].endswith('new '):
        i -= 4
    return i

def fix_text(path, s):
    rel = str(path.relative_to(base)).replace(chr(92),'/')[:-5]
    if rel in HOLDERS:
        r = HOLDERS[rel]
        s = re.sub(r'^@ObjectHolder\("randomthings"\)\r?\n', '', s, flags=re.M)
        s = re.sub(r'@ObjectHolder\("([a-z0-9_/]+)"\)', lambda m: f'@ObjectHolder(registryName = "minecraft:{r}", value = "randomthings:{m.group(1)}")', s)
    # setRegistryName inside register(...)
    while True:
        m = re.search(r'\.setRegistryName\(', s)
        if not m: break
        op = m.end()-1
        cl = match_close(s, op)
        arg = s[op+1:cl]
        st = expr_start(s, m.start())
        expr = s[st:m.start()]
        # enclosed in register( ... )?
        pre = s[:st]
        mm = re.search(r'register\($', pre)
        if mm and s[cl+1] == ')':
            s = pre + arg + ', ' + expr + s[cl+1:]
        else:
            s = pre + '/*TODO-SETREG ' + arg + '*/' + expr + s[cl+1:]
    # getRegistryName -> RegUtil.id
    while True:
        m = re.search(r'\.getRegistryName\(\)', s)
        if not m: break
        st = expr_start(s, m.start())
        s = s[:st] + 'RegUtil.id(' + s[st:m.start()] + ')' + s[m.end():]
    s = re.sub(r'new TranslatableComponent\(', 'Component.translatable(', s)
    s = re.sub(r'new TextComponent\(', 'Component.literal(', s)
    s = re.sub(r'import net\.minecraft\.network\.chat\.(Translatable|Text)Component;', 'import net.minecraft.network.chat.Component;', s)
    return s

for p in root.rglob('*.java'):
    t = p.read_bytes().decode('utf-8')
    n = fix_text(p, t)
    if n != t:
        if 'RegUtil.id(' in n and 'import lumien.randomthings.util.RegUtil;' not in n and p.name != 'RegUtil.java':
            n = re.sub(r'(package [^;]+;\r?\n)', r'\1\nimport lumien.randomthings.util.RegUtil;', n, count=1)
        p.write_bytes(n.encode('utf-8'))
        print('fixed', p)
