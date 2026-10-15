"""1.18.2 -> 1.19.2 mechanical rewrites, batch 3: event renames (run once from the worktree root, after m1b).
Code only; javadoc mentions of the old names are left alone except where they are plain text.
"""
import re, pathlib

root = pathlib.Path('src/main/java')
for p in root.rglob('*.java'):
    t = p.read_bytes().decode('utf-8')
    s = t
    s = s.replace('net.minecraftforge.event.world.', 'net.minecraftforge.event.level.')
    s = s.replace('TickEvent.WorldTickEvent', 'TickEvent.LevelTickEvent')
    s = re.sub(r'(TickEvent\.LevelTickEvent event\) -> \{)', r'\1', s)
    s = re.sub(r'\bevent\.world\b', 'event.level', s)
    s = s.replace('LivingEvent.LivingUpdateEvent', 'LivingEvent.LivingTickEvent')
    s = s.replace('ColorHandlerEvent.Block', 'RegisterColorHandlersEvent.Block')
    s = s.replace('ColorHandlerEvent.Item', 'RegisterColorHandlersEvent.Item')
    s = s.replace('import net.minecraftforge.client.event.ColorHandlerEvent;', 'import net.minecraftforge.client.event.RegisterColorHandlersEvent;')
    s = s.replace('new net.minecraft.network.chat.TranslatableComponent(', 'net.minecraft.network.chat.Component.translatable(')
    s = s.replace('new net.minecraft.network.chat.TextComponent(', 'net.minecraft.network.chat.Component.literal(')
    s = s.replace('net.minecraft.network.chat.TextComponent.EMPTY', 'net.minecraft.network.chat.CommonComponents.EMPTY')
    if s != t:
        p.write_bytes(s.encode('utf-8'))
        print('fixed', str(p).replace(chr(92), '/'))
