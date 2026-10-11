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
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * RedstoneRemoteUseScreen - one button per bound Position Filter slot,
 * firing a pulse at its stored target on press. Reads the held remote's own
 * NBT directly (always available locally, no server round-trip needed), and
 * draws each button as an {@link ItemSlotButton}: the camo item chosen in the
 * Redstone Remote's edit screen, or the Position Filter's own icon if none.
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

        ItemStackHandler camoInventory = new ItemStackHandler(9);

        if (remoteStack.getTag().contains("camo")) {
            camoInventory.deserializeNBT(remoteStack.getTag().getCompound("camo"));
        }

        for (int i = 0; i < 9; i++) {
            ItemStack positionFilter = positionInventory.getStackInSlot(i);

            if (positionFilter.isEmpty()) {
                continue;
            }

            // 1.12.2: the button shows the camo item if one is set, else the Position Filter itself;
            // the tooltip is the Position Filter's custom (anvil) name, if it has one
            ItemStack camo = camoInventory.getStackInSlot(i);
            ItemStack icon = camo.isEmpty() ? positionFilter : camo;
            // the displayed item's own (anvil) name wins, else fall back to the Position Filter's
            String name = icon.hasDisplayName() ? icon.getDisplayName().getString() : positionFilter.hasDisplayName() ? positionFilter.getDisplayName().getString() : null;
            int slot = i;

            this.addButton(new ItemSlotButton(this.guiLeft + 5 + i * 20, this.guiTop + 17, icon.copy(), name, (b) -> {
                RTPacketHandler.sendToServer(new RedstoneRemoteActivateMessage(slot));
            }));
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURES);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.xSize, this.ySize);
    }

    /**
     * Real bug, found 2026-09-28 (reported by user, screenshot of a blank-
     * looking GUI): this screen never drew its title at all - missing this
     * override entirely, unlike every other ported container screen in this
     * project (standard convention: {@code this.font.drawString(matrixStack, this.title
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
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        this.font.drawString(matrixStack, this.title.getString(), 8, 6, 4210752);
        SpriteStateButton.renderTooltips(matrixStack, this, this.buttons, mouseX, mouseY, this.guiLeft, this.guiTop);
    }

    @Override
    public void render(MatrixStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, p_render_1_, p_render_2_, p_render_3_);
        this.renderHoveredTooltip(matrixStack, p_render_1_, p_render_2_);
    }
}
