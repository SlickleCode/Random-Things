package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.ChunkAnalyzerContainer;
import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * Port of 1.12.2's {@code GuiChunkAnalyzer}: a "Scan" button that asks the
 * server to start counting blocks in the player's current chunk
 * ({@link ChunkAnalyzerContainer}), and a scrollable results list
 * ({@link ChunkAnalyzerScanResultList}) reading straight from the held
 * item's own NBT (auto-synced with the rest of the player's inventory, no
 * special wiring needed) so previously-saved results still show up if you
 * reopen the GUI without rescanning.
 */
public class ChunkAnalyzerScreen extends ContainerScreen<ChunkAnalyzerContainer> {
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/chunk_analyzer.png");

    private ChunkAnalyzerScanResultList scanResultList;
    private Button scanButton;
    private boolean wasScanning;

    // Local replacement for the "..." animation - the original drove this off a
    // global RTEventHandler.clientAnimationCounter that doesn't exist in this
    // port yet; a per-screen tick counter gives the exact same visual effect
    // without needing to port a shared counter just for this.
    private int ticksElapsed;

    public ChunkAnalyzerScreen(ChunkAnalyzerContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);

        this.xSize = 190;
        this.ySize = 124;
    }

    @Override
    protected void init() {
        super.init();

        this.scanButton = this.addButton(new Button(this.guiLeft + 136, this.guiTop + 5, 50, 14, "Scan", (button) -> {
            this.container.send(0, (pb) -> {
            });
        }));

        this.scanResultList = new ChunkAnalyzerScanResultList(this.minecraft, 182, 100, this.guiTop + 20, this.guiTop + 120, this.guiLeft + 4);
        this.children.add(this.scanResultList);

        refreshResultsFromHeldStack();
    }

    private void refreshResultsFromHeldStack() {
        ItemStack held = this.playerInventory.player.getHeldItemMainhand();
        CompoundNBT resultTag = held.getChildTag("result");

        if (resultTag != null) {
            ChunkAnalyzerResult results = new ChunkAnalyzerResult();
            results.readFromNBT(resultTag);
            this.scanResultList.setResults(results);
        } else {
            this.scanResultList.setResults(null);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString(this.title.getString(), 8, 6, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURES);
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.blit(x, y, 0, 0, this.xSize, this.ySize);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);

        this.scanResultList.render(mouseX, mouseY, partialTicks);

        boolean scanning = this.container.scanning.get() != 0;

        this.scanButton.active = !scanning;

        if (scanning) {
            String text = "Scanning Chunk" + dots();
            this.font.drawString(text, this.guiLeft + (int) (95 - this.font.getStringWidth(text) / 2F), this.guiTop + 65, 16777215);
        }

        if (this.wasScanning && !scanning) {
            // Scan just finished (transition detected via the auto-synced
            // `scanning` flag) - the server already wrote the new result into
            // the held stack's NBT, which the client's inventory sync has by
            // now applied, so just re-read it.
            refreshResultsFromHeldStack();
        }

        this.wasScanning = scanning;

        this.renderHoveredToolTip(mouseX, mouseY);
    }

    private String dots() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < (this.ticksElapsed / 15) % 3; i++) {
            sb.append('.');
        }

        return sb.toString();
    }

    @Override
    public void tick() {
        super.tick();
        ticksElapsed++;
    }
}
