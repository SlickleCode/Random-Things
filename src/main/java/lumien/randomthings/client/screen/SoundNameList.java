package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;

import java.util.List;
import java.util.function.Consumer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

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
public class SoundNameList extends ObjectSelectionList<SoundNameList.SoundNameEntry> {
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
        return this.width - 8;
    }

    /** Rows start 2px inside the border (vanilla centers them on {@link #getRowWidth()} plus 2px, which left a wide left margin). */
    @Override
    public int getRowLeft() {
        return this.x0 + 2;
    }

    /**
     * Real bug, found from the user's F2 screenshots 2026-10-04: {@code getEntryAtPosition} (which drives
     * both the hover highlight and click hit-testing) only accepts a mouse x below {@code
     * getScrollbarPosition()}, which defaults to {@code width / 2 + 124} measured from the screen's
     * left edge - not this list's own {@code x0}. With the list offset into the GUI, the right part of
     * every row (and the whole list at some window sizes) was neither highlighted nor clickable. Same
     * fix as {@link ChunkAnalyzerScanResultList}: put the scrollbar at the list's own right edge
     * (vanilla's scrollbar dragging and the row hit-test both key off this value).
     */
    @Override
    protected int getScrollbarPosition() {
        return this.x0 + this.width - 6;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // ContainerScreen#render leaves item lighting on and a stale GL color, which dimmed both the
        // hover bar and the (white) hovered text to near-black in the screenshots
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int rowLeft = this.getRowLeft();
        int rowTop = this.y0 + 4 - (int) this.getScrollAmount();

        ListClip.begin(this.minecraft, this.x0, this.y0, this.x1, this.y1);
        this.renderList(graphics, mouseX, mouseY, partialTicks);
        ListClip.end();

        renderBorder(graphics);
        renderScrollbar(graphics);

    }

    // Vanilla's drag-to-scroll only runs if the screen forwards mouseDragged here (ContainerScreen
    // doesn't), and its internal "scrolling" flag is never cleared on release - so track the drag
    // ourselves and only honour drags that started on the scrollbar.
    private boolean draggingScrollbar;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.draggingScrollbar = button == 0 && this.isMouseOver(mouseX, mouseY)
                && mouseX >= this.getScrollbarPosition() && mouseX < this.getScrollbarPosition() + 6;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.draggingScrollbar = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        return this.draggingScrollbar && super.mouseDragged(mouseX, mouseY, button, dx, dy);
    }

    /** Light 1px frame around the scrollable area. */
    private void renderBorder(GuiGraphics graphics) {
        int color = 0xFFFFFFFF;

        graphics.fill(this.x0 - 1, this.y0 - 1, this.x1 + 1, this.y0, color);
        graphics.fill(this.x0 - 1, this.y1, this.x1 + 1, this.y1 + 1, color);
        graphics.fill(this.x0 - 1, this.y0, this.x0, this.y1, color);
        graphics.fill(this.x1, this.y0, this.x1 + 1, this.y1, color);
    }

    /** Scrollbar on the list's right edge, only when the rows overflow; dragging is handled by vanilla. */
    private void renderScrollbar(GuiGraphics graphics) {
        int totalHeight = this.getItemCount() * this.itemHeight;
        int visibleHeight = this.y1 - this.y0;

        if (totalHeight <= visibleHeight) {
            return;
        }

        // same geometry as vanilla's AbstractList scrollbar (its max scroll includes a 4px pad, and
        // its drag math assumes this thumb height) - using a different formula let the thumb run
        // past the bottom border and made dragging drift
        int scrollbarX = this.getScrollbarPosition();
        int maxScroll = Math.max(1, this.getMaxPosition() - (visibleHeight - 4));
        int thumbHeight = Math.max(32, Math.min(visibleHeight - 8, (int) ((float) (visibleHeight * visibleHeight) / this.getMaxPosition())));
        int thumbY = Math.max(this.y0, Math.min(this.y1 - thumbHeight, (int) this.getScrollAmount() * (visibleHeight - thumbHeight) / maxScroll + this.y0));

        graphics.fill(scrollbarX, this.y0, scrollbarX + 6, this.y1, 0xFF373737);
        graphics.fill(scrollbarX, thumbY, scrollbarX + 6, thumbY + thumbHeight, 0xFF8B8B8B);
        graphics.fill(scrollbarX, thumbY, scrollbarX + 5, thumbY + thumbHeight - 1, 0xFFC6C6C6);
    }

    public class SoundNameEntry extends ObjectSelectionList.Entry<SoundNameEntry> {
    @Override
    public net.minecraft.network.chat.Component getNarration() {
        return net.minecraft.network.chat.CommonComponents.EMPTY;
    }

        private final String sound;

        public SoundNameEntry(String sound) {
            this.sound = sound;
        }

        @Override
        public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            net.minecraft.client.gui.Font font = Minecraft.getInstance().font;

            // Dark text on the GUI's light-gray panel (white was nearly invisible there - found via
            // the debug GUI screenshots 2026-10-03); hovered rows get a dark highlight bar + white
            // text so it's obvious they're clickable. Sound ids can be wider than the list, so the
            // text is shrunk to fit (down to half size) instead of being cut off mid-id, and the
            // bar always spans the full row.
            // The custom render() above skips vanilla's hover bookkeeping (its "hovered" field is private and
            // never set), so the isMouseOver argument is always false - hit-test the row ourselves.
            isMouseOver = SoundNameList.this.isMouseOver(mouseX, mouseY)
                    && mouseX >= left && mouseX < left + width && mouseY >= top - 2 && mouseY < top + height + 2;

            if (isMouseOver) {
                graphics.fill(left, top - 2, left + width, top + height + 2, 0xFF404040);
            }

            int available = width - 4;
            int textWidth = font.width(sound);
            float scale = textWidth > available ? Math.max(0.5F, (float) available / textWidth) : 1.0F;
            String shown = font.width(sound) * scale > available ? font.plainSubstrByWidth(sound, (int) (available / scale)) : sound;

            graphics.pose().pushPose();
            graphics.pose().translate(left + 2, top + 1 + (1.0F - scale) * 4.0F, 0.0D);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(font, shown, 0, 0, isMouseOver ? 0xFFFFFF : 0x404040, false);
            graphics.pose().popPose();
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            onSelect.accept(sound);
            return true;
        }
    }
}
