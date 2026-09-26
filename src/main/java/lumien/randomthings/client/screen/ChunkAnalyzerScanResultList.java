package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.list.ExtendedList;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

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
public class ChunkAnalyzerScanResultList extends ExtendedList<ChunkAnalyzerScanResultList.ResultEntry> {
    public ChunkAnalyzerScanResultList(Minecraft client, int width, int height, int top, int bottom, int left) {
        super(client, width, height, top, bottom, 20);

        this.setLeftPos(left);
        this.setRenderSelection(false);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.disableLighting();
        GlStateManager.disableFog();

        int rowLeft = this.getRowLeft();
        int rowTop = this.y0 + 4 - (int) this.getScrollAmount();

        this.renderList(rowLeft, rowTop, mouseX, mouseY, partialTicks);

        renderScrollbar();
    }

    private void renderScrollbar() {
        int totalHeight = this.getItemCount() * this.itemHeight;
        int visibleHeight = this.y1 - this.y0;

        if (totalHeight <= visibleHeight) {
            return;
        }

        int scrollbarX = this.getScrollbarPosition();
        int maxScroll = totalHeight - visibleHeight;
        int thumbHeight = Math.max(32, visibleHeight * visibleHeight / totalHeight);
        int thumbY = this.y0 + (int) ((visibleHeight - thumbHeight) * (this.getScrollAmount() / maxScroll));

        fill(scrollbarX, this.y0, scrollbarX + 6, this.y1, 0xFF000000);
        fill(scrollbarX, thumbY, scrollbarX + 6, thumbY + thumbHeight, 0xFF808080);
    }

    public void setResults(ChunkAnalyzerResult results) {
        this.clearEntries();

        if (results != null) {
            for (int i = 0; i < results.blockCounts.size(); i++) {
                this.addEntry(new ResultEntry(results.displayStacks.get(i), results.blockDescriptions.get(i), results.blockCounts.get(i)));
            }
        }
    }

    public static class ResultEntry extends ExtendedList.AbstractListEntry<ResultEntry> {
        private final ItemStack stack;
        private final String description;
        private final int count;

        public ResultEntry(ItemStack stack, String description, int count) {
            this.stack = stack;
            this.description = description;
            this.count = count;
        }

        @Override
        public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            Minecraft mc = Minecraft.getInstance();

            RenderHelper.enableGUIStandardItemLighting();
            mc.getItemRenderer().renderItemAndEffectIntoGUI(mc.player, stack, left + 2, top + 1);
            RenderHelper.disableStandardItemLighting();

            String countText = count + "x ";
            mc.fontRenderer.drawString(countText, left + 2 + 16 + 4, top + 5, 16777215);

            String displayDescription = description;

            if (displayDescription.length() > 20) {
                displayDescription = displayDescription.substring(0, 19) + "...";
            }

            mc.fontRenderer.drawString(displayDescription, left + 2 + 16 + 4 + 50, top + 5, 16777215);
        }
    }
}
