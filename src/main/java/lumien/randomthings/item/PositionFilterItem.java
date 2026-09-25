package lumien.randomthings.item;

import java.util.List;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

/**
 * Right-click a block to remember its position (dimension + x/y/z) in this
 * item's own NBT. Used by {@link GoldenCompassItem}'s set-target recipe to
 * hand it a target position without needing a full block-position-tracking
 * subsystem of its own.
 */
public class PositionFilterItem extends Item
{
	public PositionFilterItem(Item.Properties properties)
	{
		super(properties);
	}

	@Override
	public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag)
	{
		CompoundNBT compound = stack.getTag();

		if (Screen.hasShiftDown())
		{
			if (compound != null && compound.getBoolean("hasPosition"))
			{
				tooltip.add(new TranslationTextComponent("tooltip.randomthings.position_filter.dimension", compound.getInt("dimension")));
				tooltip.add(new TranslationTextComponent("tooltip.randomthings.position_filter.x", compound.getInt("filterX")));
				tooltip.add(new TranslationTextComponent("tooltip.randomthings.position_filter.y", compound.getInt("filterY")));
				tooltip.add(new TranslationTextComponent("tooltip.randomthings.position_filter.z", compound.getInt("filterZ")));
			}
		}
		else
		{
			tooltip.add(new TranslationTextComponent("tooltip.randomthings.general.shift"));
		}
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext context)
	{
		if (!context.getWorld().isRemote)
		{
			setPosition(context.getItem(), context.getWorld().getDimension().getType().getId(), context.getPos());
		}

		return ActionResultType.SUCCESS;
	}

	public static void setPosition(ItemStack positionFilter, int dimension, BlockPos pos)
	{
		CompoundNBT compound = positionFilter.getTag();

		if (compound == null)
		{
			positionFilter.setTag(compound = new CompoundNBT());
		}

		compound.putBoolean("hasPosition", true);
		compound.putInt("dimension", dimension);
		compound.putInt("filterX", pos.getX());
		compound.putInt("filterY", pos.getY());
		compound.putInt("filterZ", pos.getZ());
	}

	public static BlockPos getPosition(ItemStack positionFilter)
	{
		CompoundNBT compound = positionFilter.getTag();

		if (compound == null || !compound.getBoolean("hasPosition"))
		{
			return null;
		}

		return new BlockPos(compound.getInt("filterX"), compound.getInt("filterY"), compound.getInt("filterZ"));
	}
}
