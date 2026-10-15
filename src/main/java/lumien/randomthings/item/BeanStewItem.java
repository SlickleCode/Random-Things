package lumien.randomthings.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class BeanStewItem extends Item
{
	public BeanStewItem(Item.Properties properties)
	{
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity livingEntity)
	{
		super.finishUsingItem(stack, worldIn, livingEntity);

		if (stack.getCount() == 0)
		{
			return new ItemStack(Items.BOWL);
		}
		else
		{
			if (livingEntity instanceof Player)
			{
				boolean added = ((Player) livingEntity).getInventory().add(new ItemStack(Items.BOWL));

				if (!added && !worldIn.isClientSide)
				{
					((ServerLevel) worldIn).addFreshEntity(new ItemEntity(worldIn, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), new ItemStack(Items.BOWL)));
				}
			}
			return stack;
		}
	}
}
