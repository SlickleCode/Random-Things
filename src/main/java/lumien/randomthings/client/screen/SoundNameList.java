package lumien.randomthings.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;

import java.util.List;
import java.util.function.Consumer;

/**
 * Scrollable list of recorded sound names, one clickable row per sound -
 * clicking sends the name back via {@code onSelect}. Port of 1.12.2's
 * {@code GuiStringList} (used by {@code GuiSoundRecorder}), rewritten against
 * vanilla's modern {@code ExtendedList} the same way
 * {@link ChunkAnalyzerScanResultList} already was - including the same
 * {@code render()} override, since {@code AbstractList.render()} bakes a
 * full-screen-menu-style dirt/fade background directly into its own
 * bytecode rather than behind a cleanly overridable hook (see that class's
 * javadoc for the full story - same fix applies here verbatim).
 */
public class SoundNameList extends ExtendedList<SoundNameList.SoundNameEntry> {
    private final Consumer<String> onSelect;

    public SoundNameList(Minecraft client, int width, int height, int top, int bottom, int left, List<String> sounds, Consumer<String> onSelect) {
        super(client, width, height, top, bottom, 14);

        this.setLeftPos(left);
        this.setRenderSelection(false);

        this.onSelect = onSelect;

        for (String sound : sounds) {
            this.addEntry(new SoundNameEntry(sound));
        }
    }

    /**
     * Real bug, found 2026-09-28 (reported by user): sound name text ran off
     * the edge of the GUI. Two compounding causes, found by checking 1.12.2's
     * real {@code GuiStringList} first: (1) {@code AbstractList.getRowWidth()}
     * defaults to a hardcoded {@code 220} - vanilla's own width for the
     * full-screen-style lists (resource packs, server list) this class was
     * designed for - which is wider than this whole GUI panel, so {@code
     * getRowLeft()}'s centering math (built around that 220) placed rows
     * noticeably left of where they should sit; (2) nothing clipped or
     * truncated a sound name longer than the available row width at all -
     * 1.12.2's own version explicitly {@code GL11.glScissor}-clipped each row
     * to the list's real bounds specifically to prevent this, which this
     * port's rewrite (targeting vanilla's modern {@code ExtendedList}
     * instead) dropped without a replacement. Fixed by overriding {@code
     * getRowWidth()} to the list's own real width instead of the vanilla
     * default, and by trimming the drawn text to the actual available pixel
     * width (matching {@code ChunkAnalyzerScanResultList}'s own established
     * truncation precedent in this project, just pixel-accurate via {@code
     * FontRenderer#trimStringToWidth} instead of a fixed character count,
     * since sound names vary a lot in length).
     */
    @Override
    public int getRowWidth() {
        return this.width - 10;
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        int rowLeft = this.getRowLeft();
        int rowTop = this.y0 + 4 - (int) this.getScrollAmount();

        ListClip.begin(this.minecraft, this.x0, this.y0, this.x1, this.y1);
        this.renderList(rowLeft, rowTop, mouseX, mouseY, partialTicks);
        ListClip.end();
    }

    public class SoundNameEntry extends ExtendedList.AbstractListEntry<SoundNameEntry> {
        private final String sound;

        public SoundNameEntry(String sound) {
            this.sound = sound;
        }

        @Override
        public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            net.minecraft.client.gui.FontRenderer font = Minecraft.getInstance().fontRenderer;

            // Dark text on the GUI's light-gray panel (white was nearly invisible there - found via
            // the debug GUI screenshots 2026-10-03); hovered rows get a dark highlight bar + white
            // text so it's obvious they're clickable. Sound ids can be wider than the list, so the
            // text is shrunk to fit (down to half size) instead of being cut off mid-id, and the
            // bar always spans the full row.
            if (isMouseOver) {
                net.minecraft.client.gui.AbstractGui.fill(left, top - 2, left + width, top + height + 2, 0xFF404040);
            }

            int available = width - 4;
            int textWidth = font.getStringWidth(sound);
            float scale = textWidth > available ? Math.max(0.5F, (float) available / textWidth) : 1.0F;
            String shown = font.getStringWidth(sound) * scale > available ? font.trimStringToWidth(sound, (int) (available / scale)) : sound;

            com.mojang.blaze3d.platform.GlStateManager.pushMatrix();
            com.mojang.blaze3d.platform.GlStateManager.translatef(left + 2, top + 1 + (1.0F - scale) * 4.0F, 0.0F);
            com.mojang.blaze3d.platform.GlStateManager.scalef(scale, scale, 1.0F);
            font.drawString(shown, 0, 0, isMouseOver ? 0xFFFFFF : 0x404040);
            com.mojang.blaze3d.platform.GlStateManager.popMatrix();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            onSelect.accept(sound);
            return true;
        }
    }
}
