package lumien.randomthings.client.screen;

import lumien.randomthings.util.StackTag;
import net.minecraft.client.gui.GuiGraphics;

import net.minecraft.client.renderer.GameRenderer;


import lumien.randomthings.container.ChunkAnalyzerContainer;
import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.vertex.PoseStack;

/**
 * Port of 1.12.2's {@code GuiChunkAnalyzer}: a "Scan" button that asks the
 * server to start counting blocks in the player's current chunk
 * ({@link ChunkAnalyzerContainer}), and a scrollable results list
 * ({@link ChunkAnalyzerScanResultList}) reading straight from the held
 * item's own NBT (auto-synced with the rest of the player's inventory, no
 * special wiring needed) so previously-saved results still show up if you
 * reopen the GUI without rescanning.
 */
public class ChunkAnalyzerScreen extends AbstractContainerScreen<ChunkAnalyzerContainer> {
    private static final Identifier GUI_TEXTURES = Identifier.parse("randomthings:textures/gui/chunk_analyzer.png");

    private ChunkAnalyzerScanResultList scanResultList;
    private Button scanButton;
    private boolean wasScanning;

    // Local replacement for the "..." animation - the original drove this off a
    // global RTEventHandler.clientAnimationCounter that doesn't exist in this
    // port yet; a per-screen tick counter gives the exact same visual effect
    // without needing to port a shared counter just for this.
    private int ticksElapsed;

    public ChunkAnalyzerScreen(ChunkAnalyzerContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);

        this.imageWidth = 190;
        this.imageHeight = 124;
    }

    @Override
    protected void init() {
        super.init();

        this.scanButton = this.addRenderableWidget(Button.builder(Component.literal("Scan"), (button) -> {
            this.menu.send(0, (pb) -> {
            });
        }).bounds(this.leftPos + 136, this.topPos + 5, 50, 14).build());

        this.scanResultList = new ChunkAnalyzerScanResultList(this.minecraft, 182, 100, this.topPos + 20, this.topPos + 120, this.leftPos + 4);
        this.addWidget(this.scanResultList);

        refreshResultsFromHeldStack();
    }

    private void refreshResultsFromHeldStack() {
        ItemStack held = this.minecraft.player.getMainHandItem();
        CompoundTag resultTag = StackTag.getElement(held, "result");

        if (resultTag != null) {
            ChunkAnalyzerResult results = new ChunkAnalyzerResult();
            results.readFromNBT(resultTag);
            this.scanResultList.setResults(results);
        } else {
            this.scanResultList.setResults(null);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.title.getString(), 8, 6, 0xFF404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        graphics.blit(net.minecraft.client.renderer.RenderPipelines.GUI_TEXTURED, GUI_TEXTURES, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    /**
     * {@code ContainerScreen.mouseDragged} never calls up to {@code Screen.mouseDragged}, so drags
     * are not forwarded to child widgets - without this the result list's scrollbar could be
     * clicked but never dragged (reported 2026-10-03).
     */
    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent rtEvent, double dragX, double dragY) {
		double mouseX = rtEvent.x();
		double mouseY = rtEvent.y();
		int button = rtEvent.button();
        if (this.scanResultList.mouseDragged(rtEvent, dragX, dragY)) {
            return true;
        }

        return super.mouseDragged(rtEvent, dragX, dragY);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);

        this.scanResultList.render(graphics, mouseX, mouseY, partialTicks);

        boolean scanning = this.menu.scanning.get() != 0;

        this.scanButton.active = !scanning;

        if (scanning) {
            String text = "Scanning Chunk" + dots();
            graphics.drawString(this.font, text, this.leftPos + (int) (95 - this.font.width(text) / 2F), this.topPos + 65, 0xFFFFFFFF, false);
        }

        if (this.wasScanning && !scanning) {
            // Scan just finished (transition detected via the auto-synced
            // `scanning` flag) - the server already wrote the new result into
            // the held stack's NBT, which the client's inventory sync has by
            // now applied, so just re-read it.
            refreshResultsFromHeldStack();
        }

        this.wasScanning = scanning;

        this.renderTooltip(graphics, mouseX, mouseY);
    }

    private String dots() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < (this.ticksElapsed / 15) % 3; i++) {
            sb.append('.');
        }

        return sb.toString();
    }

    @Override
    public void containerTick() {
        super.containerTick();
        ticksElapsed++;
    }
}
