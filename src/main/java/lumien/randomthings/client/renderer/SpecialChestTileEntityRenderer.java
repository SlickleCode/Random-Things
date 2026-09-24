package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.tileentity.SpecialChestTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.model.ChestModel;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

/**
 * Renders {@link SpecialChestTileEntity} using vanilla's own modern
 * {@code ChestModel} (matrix-stack-era API, ground-truthed via bytecode
 * disassembly of {@code ChestTileEntityRenderer} since there is no public
 * per-block texture hook to reuse vanilla's renderer directly - it hardcodes
 * its texture choice among exactly trapped/christmas/normal/ender). Only the
 * texture selection differs from vanilla's own logic; the transform/lid-
 * rotation math is copied faithfully. This mod's first custom
 * {@code TileEntityRenderer}.
 */
public class SpecialChestTileEntityRenderer extends TileEntityRenderer<SpecialChestTileEntity>
{
	private static final ResourceLocation TEXTURE_NATURE = new ResourceLocation("randomthings:textures/block/special_chest/nature.png");
	private static final ResourceLocation TEXTURE_WATER = new ResourceLocation("randomthings:textures/block/special_chest/water.png");

	private final ChestModel simpleChest = new ChestModel();

	@Override
	public void render(SpecialChestTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		GlStateManager.enableDepthTest();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);

		BlockState state = te.hasWorld() ? te.getBlockState() : Blocks.CHEST.getDefaultState();
		Direction facing = state.has(HorizontalBlock.HORIZONTAL_FACING) ? state.get(HorizontalBlock.HORIZONTAL_FACING) : Direction.SOUTH;

		if (destroyStage >= 0)
		{
			this.bindTexture(DESTROY_STAGES[destroyStage]);

			GlStateManager.matrixMode(5890);
			GlStateManager.pushMatrix();
			GlStateManager.scalef(4.0F, 4.0F, 1.0F);
			GlStateManager.translatef(0.0625F, 0.0625F, 0.0625F);
			GlStateManager.matrixMode(5888);
		}
		else
		{
			this.bindTexture(te.getChestType() == 1 ? TEXTURE_WATER : TEXTURE_NATURE);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		}

		GlStateManager.pushMatrix();
		GlStateManager.enableRescaleNormal();

		GlStateManager.translatef((float) x, (float) y + 1.0F, (float) z + 1.0F);
		GlStateManager.scalef(1.0F, -1.0F, -1.0F);

		float angle = facing.getHorizontalAngle();
		if (Math.abs(angle) > 1.0E-5)
		{
			GlStateManager.translatef(0.5F, 0.5F, 0.5F);
			GlStateManager.rotatef(angle, 0.0F, 1.0F, 0.0F);
			GlStateManager.translatef(-0.5F, -0.5F, -0.5F);
		}

		applyLidRotation(te, partialTicks, simpleChest);
		simpleChest.renderAll();

		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		if (destroyStage >= 0)
		{
			GlStateManager.matrixMode(5890);
			GlStateManager.popMatrix();
			GlStateManager.matrixMode(5888);
		}
	}

	static void applyLidRotation(SpecialChestTileEntity te, float partialTicks, ChestModel model)
	{
		float lidAngle = te.getLidAngle(partialTicks);
		lidAngle = 1.0F - lidAngle;
		lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;
		model.getLid().rotateAngleX = -(lidAngle * ((float) Math.PI / 2F));
	}
}
