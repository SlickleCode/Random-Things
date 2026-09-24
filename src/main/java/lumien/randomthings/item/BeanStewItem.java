package lumien.randomthings.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class BeanStewItem extends Item
{
	public BeanStewItem(Item.Properties properties)
	{
		super(properties);
	}

	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity livingEntity)
	{
		super.onItemUseFinish(stack, worldIn, livingEntity);

		if (stack.getCount() == 0)
		{
			return new ItemStack(Items.BOWL);
		}
		else
		{
			if (livingEntity instanceof PlayerEntity)
			{
				boolean added = ((PlayerEntity) livingEntity).inventory.addItemStackToInventory(new ItemStack(Items.BOWL));

				if (!added && !worldIn.isRemote)
				{
					((ServerWorld) worldIn).addEntity(new ItemEntity(worldIn, livingEntity.posX, livingEntity.posY, livingEntity.posZ, new ItemStack(Items.BOWL)));
				}
			}
			return stack;
		}
	}
}
