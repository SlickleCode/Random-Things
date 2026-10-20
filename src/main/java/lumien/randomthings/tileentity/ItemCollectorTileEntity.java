package lumien.randomthings.tileentity;

import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.core.BlockPos;

import java.util.List;

import lumien.randomthings.block.ItemCollectorBlock;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Vacuums nearby dropped items into the inventory it's mounted on, self
 * throttling its own tick rate based on whether it actually found anything
 * to move (matches 1.12.2's adaptive 1-20 tick backoff).
 */
public class ItemCollectorTileEntity extends BlockEntity
{
	private static final int RANGE = 3;

	private int currentTickRate = 20;
	private int counter = 0;

	public ItemCollectorTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.ITEM_COLLECTOR, pos, state);
    }

	public void tick()
	{
		if (this.level.isClientSide)
		{
			return;
		}

		counter++;

		if (counter < currentTickRate)
		{
			return;
		}

		counter = 0;

		Direction facing = this.getBlockState().getValue(ItemCollectorBlock.FACING);
		BlockEntity target = this.level.getBlockEntity(this.worldPosition.relative(facing.getOpposite()));

		boolean didSomething = false;

		if (target != null)
		{
			LazyOptional<IItemHandler> capability = target.getCapability(ForgeCapabilities.ITEM_HANDLER, facing);
			IItemHandler itemHandler = capability.orElse(null);

			if (itemHandler != null)
			{
				List<ItemEntity> nearbyItems = this.level.getEntitiesOfClass(ItemEntity.class, new AABB(this.worldPosition.offset(-RANGE, -RANGE, -RANGE), this.worldPosition.offset(RANGE + 1, RANGE + 1, RANGE + 1)), EntitySelector.ENTITY_STILL_ALIVE);

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
						itemEntity.discard();
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
