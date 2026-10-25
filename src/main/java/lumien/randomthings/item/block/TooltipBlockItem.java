package lumien.randomthings.item.block;

import java.util.function.Consumer;

import lumien.randomthings.block.IBlockTooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/** Block item of a block that implements {@link IBlockTooltip}. */
public class TooltipBlockItem extends BlockItem
{
	public TooltipBlockItem(Block block, Item.Properties properties)
	{
		super(block, properties);
	}

	@Override
	public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
	{
		super.appendHoverText(stack, context, display, tooltip, flag);

		if (getBlock() instanceof IBlockTooltip blockTooltip)
		{
			blockTooltip.addTooltip(tooltip);
		}
	}
}
