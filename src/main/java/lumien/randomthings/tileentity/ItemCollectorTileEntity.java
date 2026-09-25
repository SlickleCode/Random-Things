package lumien.randomthings.tileentity;

import java.util.List;

import lumien.randomthings.block.ItemCollectorBlock;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Vacuums nearby dropped items into the inventory it's mounted on, self
 * throttling its own tick rate based on whether it actually found anything
 * to move (matches 1.12.2's adaptive 1-20 tick backoff).
 */
public class ItemCollectorTileEntity extends TileEntity implements ITickableTileEntity
{
	private static final int RANGE = 3;

	private int currentTickRate = 20;
	private int counter = 0;

	public ItemCollectorTileEntity()
	{
		super(ModTileEntityTypes.ITEM_COLLECTOR);
	}

	@Override
	public void tick()
	{
		if (this.world.isRemote)
		{
			return;
		}

		counter++;

		if (counter < currentTickRate)
		{
			return;
		}

		counter = 0;

		Direction facing = this.getBlockState().get(ItemCollectorBlock.FACING);
		TileEntity target = this.world.getTileEntity(this.pos.offset(facing.getOpposite()));

		boolean didSomething = false;

		if (target != null)
		{
			LazyOptional<IItemHandler> capability = target.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
			IItemHandler itemHandler = capability.orElse(null);

			if (itemHandler != null)
			{
				List<ItemEntity> nearbyItems = this.world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(this.pos.add(-RANGE, -RANGE, -RANGE), this.pos.add(RANGE + 1, RANGE + 1, RANGE + 1)), EntityPredicates.IS_ALIVE);

				for (ItemEntity itemEntity : nearbyItems)
				{
					if (!itemEntity.isAlive())
					{
						continue;
					}

					ItemStack original = itemEntity.getItem().copy();
					ItemStack left = ItemHandlerHelper.insertItemStacked(itemHandler, original, false);

					if (left.getCount() < original.getCount())
					{
						didSomething = true;
					}

					if (left.isEmpty())
					{
						itemEntity.remove();
					}
					else
					{
						itemEntity.setItem(left);
					}
				}
			}
		}

		if (didSomething)
		{
			currentTickRate = Math.max(1, currentTickRate - 1);
		}
		else
		{
			currentTickRate = Math.min(20, currentTickRate + 1);
		}
	}
}
