package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.ObjectSelectionList;
import com.mojang.blaze3d.platform.Lighting;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.network.chat.TextComponent;

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
        super(client, width, height, top, bottom, 20);

        this.setLeftPos(left);
        this.setRenderSelection(false);
    }

    /**
     * Found via the debug GUI screenshots 2026-10-03: {@code AbstractList.getRowWidth()} defaults to
     * a hardcoded 220 (vanilla's full-screen list width) and {@code getScrollbarPosition()} to
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
    protected int getScrollbarPosition() {
        return this.x0 + this.width - 6;
    }

    @Override
    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        int rowLeft = this.getRowLeft();
        int rowTop = this.y0 + 4 - (int) this.getScrollAmount();

        // Rows scrolled past the top/bottom must be clipped to the list's own box (1.12.2's
        // GuiScanResultList glScissor'd the same way) - otherwise they draw over the title,
        // the Scan button and below the GUI (reported 2026-10-03).
        ListClip.begin(this.minecraft, this.x0, this.y0, this.x1, this.y1);
        this.renderList(matrixStack, rowLeft, rowTop, mouseX, mouseY, partialTicks);
        ListClip.end();

        renderScrollbar(matrixStack);
    }

    private void renderScrollbar(PoseStack matrixStack) {
        int totalHeight = this.getItemCount() * this.itemHeight;
        int visibleHeight = this.y1 - this.y0;

        if (totalHeight <= visibleHeight) {
            return;
        }

        int scrollbarX = this.getScrollbarPosition();
        int maxScroll = totalHeight - visibleHeight;
        int thumbHeight = Math.max(32, visibleHeight * visibleHeight / totalHeight);
        int thumbY = this.y0 + (int) ((visibleHeight - thumbHeight) * (this.getScrollAmount() / maxScroll));

        GuiComponent.fill(matrixStack, scrollbarX, this.y0, scrollbarX + 6, this.y1, 0xFF000000);
        GuiComponent.fill(matrixStack, scrollbarX, thumbY, scrollbarX + 6, thumbY + thumbHeight, 0xFF808080);
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
        return net.minecraft.network.chat.TextComponent.EMPTY;
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
        public void render(PoseStack matrixStack, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            Minecraft mc = Minecraft.getInstance();

            mc.getItemRenderer().renderAndDecorateItem(mc.player, stack, left + 2, top + 1, 0);

            String countText = count + "x ";
            mc.font.draw(matrixStack, countText, left + 2 + 16 + 4, top + 5, 16777215);

            String displayDescription = description;

            if (displayDescription.length() > 20) {
                displayDescription = displayDescription.substring(0, 19) + "...";
            }

            mc.font.draw(matrixStack, displayDescription, left + 2 + 16 + 4 + 50, top + 5, 16777215);
        }
    }
}
