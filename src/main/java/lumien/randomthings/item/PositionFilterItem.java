package lumien.randomthings.item;

import lumien.randomthings.util.StackTag;
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
	public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldCtx, net.minecraft.world.item.component.TooltipDisplay rtDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        Level world = lumien.randomthings.client.ClientHooks.level();
		CompoundTag compound = StackTag.get(stack);

		if (lumien.randomthings.config.RTConfig.HIDE_COORDINATES.get())
		{
			// 1.12.2's HideCoordinates: no coordinate tooltip (and no "hold Shift" hint, since there's nothing to reveal)
			return;
		}

		if (net.minecraft.client.Minecraft.getInstance().hasShiftDown())
		{
			if (compound != null && compound.getBooleanOr("hasPosition", false))
			{
				tooltip.accept(Component.translatable("tooltip.randomthings.position_filter.dimension", compound.getStringOr("dimension", "")));
				tooltip.accept(Component.translatable("tooltip.randomthings.position_filter.x", compound.getIntOr("filterX", 0)));
				tooltip.accept(Component.translatable("tooltip.randomthings.position_filter.y", compound.getIntOr("filterY", 0)));
				tooltip.accept(Component.translatable("tooltip.randomthings.position_filter.z", compound.getIntOr("filterZ", 0)));
			}
		}
		else
		{
			tooltip.accept(Component.translatable("tooltip.randomthings.general.shift"));
		}
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		if (!context.getLevel().isClientSide())
		{
			setPosition(context.getItemInHand(), context.getLevel().dimension().identifier().toString(), context.getClickedPos());
		}

		return InteractionResult.SUCCESS;
	}

	public static void setPosition(ItemStack positionFilter, String dimension, BlockPos pos)
	{
		StackTag.edit(positionFilter, compound -> {
			compound.putBoolean("hasPosition", true);
			compound.putString("dimension", dimension);
			compound.putInt("filterX", pos.getX());
			compound.putInt("filterY", pos.getY());
			compound.putInt("filterZ", pos.getZ());
		});
	}

	public static BlockPos getPosition(ItemStack positionFilter)
	{
		CompoundTag compound = StackTag.get(positionFilter);

		if (compound == null || !compound.getBooleanOr("hasPosition", false))
		{
			return null;
		}

		return new BlockPos(compound.getIntOr("filterX", 0), compound.getIntOr("filterY", 0), compound.getIntOr("filterZ", 0));
	}
}
