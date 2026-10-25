package lumien.randomthings.client.screen;

import net.minecraft.client.gui.GuiGraphics;


import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ObjectSelectionList;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;

/**
 * Scrollable list of scan results (icon, count, block name), one row per
 * distinct block found. Port of 1.12.2's {@code GuiScanResultList}, which
 * extended Forge's old {@code GuiScrollingList} (removed in this Forge
 * version) - rewritten against vanilla's modern {@code ExtendedList}, the
 * same base class vanilla itself uses for the resource-pack and key-binding
 * list screens. The per-row drawing logic is otherwise a direct port.
 * <p>
 * {@code AbstractList.render()} bakes a full-screen-menu-style frame (a
 * tiled dirt texture fading into a solid black "hole" mask above/below the
 * rows) directly into its own bytecode rather than behind a cleanly
 * overridable hook - fine for the world-select/resource-pack screens it was
 * designed for, wrong for a small list embedded in a container GUI, where it
 * just paints over the panel. 1.12.2's original {@code GuiScanResultList}
 * hit the exact same problem with Forge's old {@code GuiScrollingList} and
 * fixed it the same way (an empty {@code drawBackground()} override) - here
 * that means overriding {@code render()} itself and keeping only the actual
 * row rendering plus a minimal hand-drawn scrollbar.
 */
public class ChunkAnalyzerScanResultList extends ObjectSelectionList<ChunkAnalyzerScanResultList.ResultEntry> {
    public ChunkAnalyzerScanResultList(Minecraft client, int width, int height, int top, int bottom, int left) {
        super(client, width, bottom - top, top, 20);

        this.setX(left);
    }

    /**
     * Found via the debug GUI screenshots 2026-10-03: {@code AbstractList.getRowWidth()} defaults to
     * a hardcoded 220 (vanilla's full-screen list width) and {@code scrollBarX()} to
     * {@code width / 2 + 124} measured from the screen's left edge, so rows were centered on a 220px
     * column (icons spilled out of the GUI's left edge) and the scrollbar drew through the middle of
     * the text. The list is laid out like 1.12.2's {@code GuiScanResultList}: rows start at the
     * list's own left edge and the scrollbar sits on its right edge.
     */
    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int scrollBarX() {
        return this.getX() + this.width - 6;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {

        int rowLeft = this.getRowLeft();
        int rowTop = this.getY() + 4 - (int) this.scrollAmount();

        // Rows scrolled past the top/bottom must be clipped to the list's own box (1.12.2's
        // GuiScanResultList glScissor'd the same way) - otherwise they draw over the title,
        // the Scan button and below the GUI (reported 2026-10-03).
        ListClip.begin(this.minecraft, this.getX(), this.getY(), this.getRight(), this.getBottom());
        this.renderListItems(graphics, mouseX, mouseY, partialTicks);
        ListClip.end();

        renderCustomScrollbar(graphics);
    }

    private void renderCustomScrollbar(GuiGraphics graphics) {
        int totalHeight = this.getItemCount() * this.defaultEntryHeight;
        int visibleHeight = this.getBottom() - this.getY();

        if (totalHeight <= visibleHeight) {
            return;
        }

        int scrollbarX = this.scrollBarX();
        int maxScroll = totalHeight - visibleHeight;
        int thumbHeight = Math.max(32, visibleHeight * visibleHeight / totalHeight);
        int thumbY = this.getY() + (int) ((visibleHeight - thumbHeight) * (this.scrollAmount() / maxScroll));

        graphics.fill(scrollbarX, this.getY(), scrollbarX + 6, this.getBottom(), 0xFF000000);
        graphics.fill(scrollbarX, thumbY, scrollbarX + 6, thumbY + thumbHeight, 0xFF808080);
    }

    public void setResults(ChunkAnalyzerResult results) {
        this.clearEntries();

        if (results != null) {
            for (int i = 0; i < results.blockCounts.size(); i++) {
                this.addEntry(new ResultEntry(results.displayStacks.get(i), results.blockDescriptions.get(i), results.blockCounts.get(i)));
            }
        }
    }

    public static class ResultEntry extends ObjectSelectionList.Entry<ResultEntry> {
    @Override
    public net.minecraft.network.chat.Component getNarration() {
        return net.minecraft.network.chat.CommonComponents.EMPTY;
    }

        private final ItemStack stack;
        private final String description;
        private final int count;

        public ResultEntry(ItemStack stack, String description, int count) {
            this.stack = stack;
            this.description = description;
            this.count = count;
        }

        @Override
        public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            int left = this.getContentX();
            int top = this.getContentY();
            int width = this.getContentWidth();
            int height = this.getContentHeight();
            Minecraft mc = Minecraft.getInstance();

            graphics.renderItem(stack, left + 2, top + 1);

            String countText = count + "x ";
            graphics.drawString(mc.font, countText, left + 2 + 16 + 4, top + 5, 0xFFFFFFFF, false);

            String displayDescription = description;

            if (displayDescription.length() > 20) {
                displayDescription = displayDescription.substring(0, 19) + "...";
            }

            graphics.drawString(mc.font, displayDescription, left + 2 + 16 + 4 + 50, top + 5, 0xFFFFFFFF, false);
        }
    }
}
