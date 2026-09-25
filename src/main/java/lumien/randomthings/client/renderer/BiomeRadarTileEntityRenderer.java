package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.tileentity.BiomeRadarTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;

/**
 * Renders the inserted {@link lumien.randomthings.item.BiomeCrystalItem}
 * hovering and slowly spinning above the radar. The 1.12.2 original
 * (`RenderBiomeRadar`) got this same "floating item" look by instantiating a
 * throwaway {@code EntityItem} purely to reuse its bob/spin math and vanilla
 * dropped-item renderer - simpler to just do the bob/spin transform directly
 * here and render the stack with {@code ItemRenderer}, same visual result.
 */
public class BiomeRadarTileEntityRenderer extends TileEntityRenderer<BiomeRadarTileEntity>
{
	@Override
	public void render(BiomeRadarTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		ItemStack currentCrystal = te.getCurrentCrystal();

		if (currentCrystal.isEmpty())
		{
			return;
		}

		float age = (te.getWorld().getGameTime() % 360000L) + partialTicks;

		GlStateManager.pushMatrix();
		GlStateManager.translatef((float) x + 0.5F, (float) y + 1.1F + 0.1F * net.minecraft.util.math.MathHelper.sin(age / 10.0F), (float) z + 0.5F);
		GlStateManager.rotatef((age * 2.0F) % 360.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.scalef(0.75F, 0.75F, 0.75F);

		Minecraft.getInstance().getItemRenderer().renderItem(currentCrystal, TransformType.GROUND);

		GlStateManager.popMatrix();
	}
}
