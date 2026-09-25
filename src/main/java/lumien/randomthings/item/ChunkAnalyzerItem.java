package lumien.randomthings.item;

import lumien.randomthings.container.ChunkAnalyzerContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Right-click (main hand only, matching the original) to open a GUI that
 * scans your current chunk for block counts - see {@link ChunkAnalyzerContainer}
 * for the actual scan. Direct port of 1.12.2's {@code ItemChunkAnalyzer};
 * the old numeric {@code GuiIds}/{@code player.openGui(...)} system is
 * replaced with {@code NetworkHooks.openGui}, matching the pattern already
 * established for tile-entity GUIs in this port - this is the first
 * *item*-triggered one (no block/tile entity involved), so the
 * {@link INamedContainerProvider} is built inline here rather than
 * implemented by a tile entity.
 */
public class ChunkAnalyzerItem extends Item
{
	public ChunkAnalyzerItem(Item.Properties properties)
	{
		super(properties);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand)
	{
		ItemStack stack = playerIn.getHeldItem(hand);

		if (!worldIn.isRemote && hand == Hand.MAIN_HAND)
		{
			NetworkHooks.openGui((ServerPlayerEntity) playerIn, new INamedContainerProvider()
			{
				@Override
				public ITextComponent getDisplayName()
				{
					return new TranslationTextComponent("item.randomthings.chunk_analyzer");
				}

				@Override
				public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player)
				{
					return new ChunkAnalyzerContainer(windowId, playerInventory, null);
				}
			});

			return new ActionResult<>(ActionResultType.SUCCESS, stack);
		}

		return new ActionResult<>(ActionResultType.FAIL, stack);
	}
}
