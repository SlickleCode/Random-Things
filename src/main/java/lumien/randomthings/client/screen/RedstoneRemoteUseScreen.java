package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.RedstoneRemoteUseContainer;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.network.messages.RedstoneRemoteActivateMessage;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.items.ItemStackHandler;

/**
 * RedstoneRemoteUseScreen - one button per bound Position Filter slot,
 * firing a pulse at its stored target on press. Reads the held remote's own
 * NBT directly (always available locally, no server round-trip needed), and
 * uses plain text buttons rather than item-icon ones, matching {@link
 * EntityDetectorScreen}'s already-established "text button instead of icon
 * sprite" convention in this port.
 */
public class RedstoneRemoteUseScreen extends ContainerScreen<RedstoneRemoteUseContainer> {
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/redstone_remote_use.png");

    public RedstoneRemoteUseScreen(RedstoneRemoteUseContainer screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);

        this.xSize = 187;
        this.ySize = 41;
    }

    @Override
    protected void init() {
        super.init();

        ItemStack remoteStack = this.container.getRemoteStack();

        if (remoteStack.isEmpty() || remoteStack.getItem() != ModItems.REDSTONE_REMOTE || !remoteStack.hasTag() || !remoteStack.getTag().contains("positions")) {
            return;
        }

        ItemStackHandler positionInventory = new ItemStackHandler(9);
        positionInventory.deserializeNBT(remoteStack.getTag().getCompound("positions"));

        for (int i = 0; i < 9; i++) {
            ItemStack positionFilter = positionInventory.getStackInSlot(i);

            if (positionFilter.isEmpty()) {
                continue;
            }

            int slot = i;
            String label = positionFilter.getDisplayName().getString();

            this.addButton(new Button(this.guiLeft + 5 + i * 20, this.guiTop + 17, 18, 18, label.length() > 3 ? label.substring(0, 3) : label, (b) -> {
                RTPacketHandler.sendToServer(new RedstoneRemoteActivateMessage(slot));
            }));
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURES);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(i, j, 0, 0, this.xSize, this.ySize);
    }

    /**
     * Real bug, found 2026-09-28 (reported by user, screenshot of a blank-
     * looking GUI): this screen never drew its title at all - missing this
     * override entirely, unlike every other ported container screen in this
     * project (standard convention: {@code this.font.drawString(this.title
     * .getString(), 8, 6, 4210752)}). Ground-truthed 1.12.2's own {@code
     * GuiRedstoneRemoteUse#drawGuiContainerForegroundLayer} first - it drew
     * the same title text - confirming this was a real gap, not something to
     * silently add. The background texture itself is untouched: 1.12.2's own
     * {@code redstoneremoteuse.png} (ground-truthed by extracting it directly
     * from the {@code 1.12.2} branch) is *also* just a plain flat gray panel
     * with no border/frame decoration - the blank-looking appearance is
     * faithful to the original, not a missing-texture bug.
     */
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.font.drawString(this.title.getString(), 8, 6, 4210752);
    }

    @Override
    public void render(int p_render_1_, int p_render_2_, float p_render_3_) {
        this.renderBackground();
        super.render(p_render_1_, p_render_2_, p_render_3_);
        this.renderHoveredToolTip(p_render_1_, p_render_2_);
    }
}
