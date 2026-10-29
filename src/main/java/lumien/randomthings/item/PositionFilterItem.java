package lumien.randomthings.item;

import java.util.List;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

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
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag)
	{
		CompoundTag compound = stack.getTag();

		if (lumien.randomthings.config.RTConfig.HIDE_COORDINATES.get())
		{
			// 1.12.2's HideCoordinates: no coordinate tooltip (and no "hold Shift" hint, since there's nothing to reveal)
			return;
		}

		if (Screen.hasShiftDown())
		{
			if (compound != null && compound.getBoolean("hasPosition"))
			{
				tooltip.add(Component.translatable("tooltip.randomthings.position_filter.dimension", compound.getString("dimension")));
				tooltip.add(Component.translatable("tooltip.randomthings.position_filter.x", compound.getInt("filterX")));
				tooltip.add(Component.translatable("tooltip.randomthings.position_filter.y", compound.getInt("filterY")));
				tooltip.add(Component.translatable("tooltip.randomthings.position_filter.z", compound.getInt("filterZ")));
			}
		}
		else
		{
			tooltip.add(Component.translatable("tooltip.randomthings.general.shift"));
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		if (!context.getLevel().isClientSide)
		{
			setPosition(context.getItemInHand(), context.getLevel().dimension().location().toString(), context.getClickedPos());
		}

		return InteractionResult.SUCCESS;
	}

	public static void setPosition(ItemStack positionFilter, String dimension, BlockPos pos)
	{
		CompoundTag compound = positionFilter.getTag();

		if (compound == null)
		{
			positionFilter.setTag(compound = new CompoundTag());
		}

		compound.putBoolean("hasPosition", true);
		compound.putString("dimension", dimension);
		compound.putInt("filterX", pos.getX());
		compound.putInt("filterY", pos.getY());
		compound.putInt("filterZ", pos.getZ());
	}

	/**
	 * The dimension id stored by {@link #setPosition}; a filter without one counts as the overworld.
	 */
	public static String getDimension(ItemStack positionFilter)
	{
		CompoundTag compound = positionFilter.getTag();

		if (compound == null || !compound.contains("dimension", 8))
		{
			return "minecraft:overworld";
		}

		return compound.getString("dimension");
	}

	public static BlockPos getPosition(ItemStack positionFilter)
	{
		CompoundTag compound = positionFilter.getTag();

		if (compound == null || !compound.getBoolean("hasPosition"))
		{
			return null;
		}

		return new BlockPos(compound.getInt("filterX"), compound.getInt("filterY"), compound.getInt("filterZ"));
	}
}
