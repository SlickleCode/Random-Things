package lumien.randomthings.client.screen;

import lumien.randomthings.client.screen.ScreenWidgets;

import net.minecraft.client.renderer.GameRenderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.RedstoneRemoteUseContainer;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.network.messages.RedstoneRemoteActivateMessage;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraftforge.items.ItemStackHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;

/**
 * RedstoneRemoteUseScreen - one button per bound Position Filter slot,
 * firing a pulse at its stored target on press. Reads the held remote's own
 * NBT directly (always available locally, no server round-trip needed), and
 * draws each button as an {@link ItemSlotButton}: the camo item chosen in the
 * Redstone Remote's edit screen, or the Position Filter's own icon if none.
 */
public class RedstoneRemoteUseScreen extends AbstractContainerScreen<RedstoneRemoteUseContainer> {
    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/redstone_remote_use.png");

    public RedstoneRemoteUseScreen(RedstoneRemoteUseContainer screenContainer, Inventory inv, Component titleIn) {
        super(screenContainer, inv, titleIn);

        this.imageWidth = 187;
        this.imageHeight = 41;
    }

    @Override
    protected void init() {
        super.init();

        ItemStack remoteStack = this.menu.getRemoteStack();

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
            String name = icon.hasCustomHoverName() ? icon.getHoverName().getString() : positionFilter.hasCustomHoverName() ? positionFilter.getHoverName().getString() : null;
            int slot = i;

            this.addRenderableWidget(new ItemSlotButton(this.leftPos + 5 + i * 20, this.topPos + 17, icon.copy(), name, (b) -> {
                RTPacketHandler.sendToServer(new RedstoneRemoteActivateMessage(slot));
            }));
        }
    }

    @Override
    protected void renderBg(PoseStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, GUI_TEXTURES);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
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
    protected void renderLabels(PoseStack matrixStack, int mouseX, int mouseY) {
        this.font.draw(matrixStack, this.title.getString(), 8, 6, 4210752);
        SpriteStateButton.renderTooltips(matrixStack, this, ScreenWidgets.buttons(this), mouseX, mouseY, this.leftPos, this.topPos);
    }

    @Override
    public void render(PoseStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, p_render_1_, p_render_2_, p_render_3_);
        this.renderTooltip(matrixStack, p_render_1_, p_render_2_);
    }
}
