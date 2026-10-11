package lumien.randomthings.client.screen;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.container.AdvancedRedstoneRepeaterContainer;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.text.StringTextComponent;

/**
 * AdvancedRedstoneRepeaterScreen
 */
public class AdvancedRedstoneRepeaterScreen extends ContainerScreen<AdvancedRedstoneRepeaterContainer>
{
	private static final ResourceLocation GUI_TEXTURES = new ResourceLocation("randomthings:textures/gui/advanced_redstone_repeater.png");

	public AdvancedRedstoneRepeaterScreen(AdvancedRedstoneRepeaterContainer screenContainer, PlayerInventory inv, ITextComponent titleIn)
	{
		super(screenContainer, inv, titleIn);

		this.xSize = 90;
		this.ySize = 56;
	}

	@Override
	protected void init()
	{
		super.init();

		this.addButton(new Button(this.guiLeft + 5, this.guiTop + 15, 10, 10, new StringTextComponent("-"), (button) -> {
			this.container.send(0, (pb) -> pb.writeInt(-1));
		}));
		this.addButton(new Button(this.guiLeft + 5 + 70, this.guiTop + 15, 10, 10, new StringTextComponent("+"), (button) -> {
			this.container.send(0, (pb) -> pb.writeInt(1));
		}));

		this.addButton(new Button(this.guiLeft + 5, this.guiTop + 39, 10, 10, new StringTextComponent("-"), (button) -> {
			this.container.send(1, (pb) -> pb.writeInt(-1));
		}));
		this.addButton(new Button(this.guiLeft + 5 + 70, this.guiTop + 39, 10, 10, new StringTextComponent("+"), (button) -> {
			this.container.send(1, (pb) -> pb.writeInt(1));
		}));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY)
	{

		this.font.drawString(matrixStack, I18n.format("gui.randomthings.advanced_redstone_repeater.on"), 8, 5, 0);
		this.font.drawString(matrixStack, I18n.format("gui.randomthings.advanced_redstone_repeater.off"), 8, 29, 0);

		String onString = this.container.turnOnDelay.get() + "";
		this.font.drawString(matrixStack, onString, xSize / 2 - this.font.getStringWidth(onString) / 2, 16, 0);

		String offString = this.container.turnOffDelay.get() + "";
		this.font.drawString(matrixStack, offString, xSize / 2 - this.font.getStringWidth(offString) / 2, 40, 0);

		for (Widget widget : this.buttons)
		{
			if (widget.isHovered())
			{
				widget.renderToolTip(matrixStack, mouseX - this.guiLeft, mouseY - this.guiTop);
				break;
			}
		}

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY)
	{
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(GUI_TEXTURES);
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.blit(matrixStack, i, j, 0, 0, this.xSize, this.ySize);
	}

	@Override
	public void render(MatrixStack matrixStack, int p_render_1_, int p_render_2_, float p_render_3_)
	{
		this.renderBackground(matrixStack);
		super.render(matrixStack, p_render_1_, p_render_2_, p_render_3_);
		this.renderHoveredTooltip(matrixStack, p_render_1_, p_render_2_);
	}
}
