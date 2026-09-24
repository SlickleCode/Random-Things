package lumien.randomthings.client.renderer;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.SpecialChestTileEntity;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

/**
 * Renders the item-form icon of the special chest blocks as a real 3D chest
 * (matching vanilla chest items), by handing a disconnected
 * {@link SpecialChestTileEntity} to the dispatcher, which looks up and
 * invokes {@link SpecialChestTileEntityRenderer} - the same renderer used
 * for the placed block - exactly like vanilla's own chest/banner/bed item
 * icons work internally (`javap`-verified via
 * {@code ItemStackTileEntityRenderer.renderByItem}).
 */
public class SpecialChestItemRenderer extends ItemStackTileEntityRenderer
{
	private final SpecialChestTileEntity dummy = new SpecialChestTileEntity();

	@Override
	public void renderByItem(ItemStack stack)
	{
		if (!(stack.getItem() instanceof BlockItem))
		{
			return;
		}

		dummy.setChestType(((BlockItem) stack.getItem()).getBlock() == ModBlocks.SPECIAL_CHEST_WATER ? 1 : 0);

		TileEntityRendererDispatcher.instance.renderAsItem(dummy);
	}
}
