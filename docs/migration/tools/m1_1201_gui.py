"""1.20: GUI drawing goes through GuiGraphics. python m1_1201_gui.py <files...>
 - PoseStack matrixStack params of render/renderBg/renderLabels/renderButton(->renderWidget)/renderList -> GuiGraphics graphics
 - font.draw(ms, ...) -> graphics.drawString(font, ..., false); font.drawShadow -> drawString
 - this.blit(ms, ...) -> graphics.blit(<last RenderSystem.setShaderTexture texture>, ...)
 - GuiComponent.fill(ms, ...) -> graphics.fill(...); ms.pushPose()/translate/scale/popPose -> graphics.pose().X
 - new Button(x,y,w,h,comp,onPress) -> Button.builder(comp,onPress).bounds(x,y,w,h).build()"""
import re, sys, pathlib

def match_paren(s, i):
    """index of the ')' matching the '(' at s[i]"""
    depth = 0; j = i; instr = None
    while j < len(s):
        c = s[j]
        if instr:
            if c == chr(92): j += 1
            elif c == instr: instr = None
        elif c in '"\'': instr = c
        elif c == '(': depth += 1
        elif c == ')':
            depth -= 1
            if depth == 0: return j
        j += 1
    raise ValueError('unbalanced')

def split_args(a):
    out = []; depth = 0; cur = ''; instr = None; i = 0
    while i < len(a):
        c = a[i]
        if instr:
            cur += c
            if c == chr(92): cur += a[i + 1]; i += 1
            elif c == instr: instr = None
        elif c in '"\'': instr = c; cur += c
        elif c in '([{': depth += 1; cur += c
        elif c in ')]}': depth -= 1; cur += c
        elif c == ',' and depth == 0: out.append(cur.strip()); cur = ''
        else: cur += c
        i += 1
    if cur.strip(): out.append(cur.strip())
    return out

def convert(s):
    # signatures
    s = re.sub(r'\bPoseStack (matrixStack|poseStack|ms)\b', 'GuiGraphics graphics', s)
    s = s.replace('void renderButton(GuiGraphics', 'void renderWidget(GuiGraphics')
    s = re.sub(r'\b(matrixStack|poseStack)\b', 'graphics', s)
    s = re.sub(r'graphics\.(pushPose|popPose|translate|scale|mulPose|last)\(', r'graphics.pose().\1(', s)
    s = re.sub(r'GuiComponent\.fill\(\s*graphics\s*,\s*', 'graphics.fill(', s)
    s = re.sub(r'GuiComponent\.fill\(\s*(\w+)\s*,\s*', r'\1.fill(', s)
    # font.draw / drawShadow
    for name, shadow in (('draw', 'false'), ('drawShadow', None)):
        pat = re.compile(r'((?:[\w.]+\.)?font)\.' + name + r'\(\s*graphics\s*,\s*')
        while True:
            m = pat.search(s)
            if not m: break
            op = m.end() - 1
            # find the '(' of this call
            op = s.index('(', m.start() + len(m.group(1)))
            cl = match_paren(s, op)
            args = s[m.end():cl]
            tail = ', ' + shadow if shadow else ''
            s = s[:m.start()] + 'graphics.drawString(' + m.group(1) + ', ' + args + tail + ')' + s[cl + 1:]
    # blit with the last bound texture
    out = []; last = None; pos = 0
    pat = re.compile(r'RenderSystem\.setShaderTexture\(\s*0\s*,\s*([^;]+?)\)\s*;|(?:this\.)?blit\(\s*graphics\s*,\s*')
    res = ''
    for m in pat.finditer(s):
        if m.group(1):
            last = m.group(1).strip()
        else:
            res += s[pos:m.start()] + 'graphics.blit(' + (last or 'MISSING_TEXTURE') + ', '
            pos = m.end()
    res += s[pos:]
    s = res
    # new Button(...)
    pat = re.compile(r'new Button\(')
    pos = 0; res = ''
    while True:
        m = pat.search(s, pos)
        if not m: res += s[pos:]; break
        op = m.end() - 1
        cl = match_paren(s, op)
        args = split_args(s[op + 1:cl])
        if len(args) == 6:
            res += s[pos:m.start()] + 'Button.builder(%s, %s).bounds(%s, %s, %s, %s).build()' % (args[4], args[5], args[0], args[1], args[2], args[3])
            pos = cl + 1
        else:
            res += s[pos:m.end()]; pos = m.end()
    s = res
    if 'GuiGraphics' in s and 'import net.minecraft.client.gui.GuiGraphics;' not in s:
        s = re.sub(r'(\npackage [^;]+;\r?\n)', r'\1\nimport net.minecraft.client.gui.GuiGraphics;', s, count=1)
    return s

if __name__ == '__main__':
    for f in sys.argv[1:]:
        p = pathlib.Path(f)
        s = p.read_bytes().decode('utf-8'); t = convert(s)
        if t != s: p.write_bytes(t.encode('utf-8')); print('converted', f)
