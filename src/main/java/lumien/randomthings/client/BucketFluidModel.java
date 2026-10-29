package lumien.randomthings.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fluids.FluidUtil;

/**
 * What the Ender Bucket and the Reinforced Ender Bucket show of their contents: the item model gets a second layer (the opening of the bucket) while a
 * fluid is inside, tinted with the average colour of the fluid's still texture times the fluid's own tint, so any fluid shows in its own colour without a
 * model per fluid. Pixels of the texture atlas are read once per fluid and cached.
 */
public final class BucketFluidModel
{
	private static final java.util.Map<net.minecraft.resources.ResourceLocation, Integer> AVERAGES = new java.util.HashMap<>();

	private BucketFluidModel()
	{
	}

	public static net.minecraftforge.fluids.FluidStack fluid(net.minecraft.world.item.ItemStack stack)
	{
		return FluidUtil.getFluidContained(stack).orElse(null);
	}

	public static boolean filled(net.minecraft.world.item.ItemStack stack)
	{
		net.minecraftforge.fluids.FluidStack fluid = fluid(stack);
		return fluid != null && !fluid.isEmpty();
	}

	/** Tint of the second model layer; the first layer (the bucket) stays untinted. */
	public static int color(net.minecraft.world.item.ItemStack stack, int tintIndex)
	{
		net.minecraftforge.fluids.FluidStack fluid = fluid(stack);

		if (tintIndex != 1 || fluid == null || fluid.isEmpty())
		{
			return 0xFFFFFF;
		}

		net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions extensions = net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid.getFluid());
		net.minecraft.resources.ResourceLocation texture = extensions.getStillTexture(fluid);
		int tint = extensions.getTintColor(fluid);
		int average = AVERAGES.computeIfAbsent(texture, BucketFluidModel::average);
		int r = (tint >> 16 & 0xFF) * (average >> 16 & 0xFF) / 255;
		int g = (tint >> 8 & 0xFF) * (average >> 8 & 0xFF) / 255;
		int b = (tint & 0xFF) * (average & 0xFF) / 255;
		return r << 16 | g << 8 | b;
	}

	/** Mean colour of the texture's first animation frame (the atlas keeps the whole strip). */
	private static int average(net.minecraft.resources.ResourceLocation texture)
	{
		net.minecraft.client.renderer.texture.TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getAtlas(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS).getSprite(texture);
		int width = sprite.contents().width();
		int height = sprite.contents().height();
		long r = 0, g = 0, b = 0, n = 0;

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int pixel = sprite.getPixelRGBA(0, x, y);      // ABGR

				if ((pixel >>> 24) == 0)
				{
					continue;
				}

				r += pixel & 0xFF;
				g += pixel >> 8 & 0xFF;
				b += pixel >> 16 & 0xFF;
				n++;
			}
		}

		return n == 0 ? 0xFFFFFF : (int) (r / n) << 16 | (int) (g / n) << 8 | (int) (b / n);
	}
}
