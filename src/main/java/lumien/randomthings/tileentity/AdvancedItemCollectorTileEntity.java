package lumien.randomthings.tileentity;

import java.util.List;

import lumien.randomthings.block.AdvancedItemCollectorBlock;
import lumien.randomthings.container.AdvancedItemCollectorContainer;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

/**
 * A long-range item vacuum: pulls dropped items within a configurable
 * (per-axis, up to {@link #MAX_RANGE}) radius into the inventory it's
 * mounted on, same adaptive 1-20 tick self-throttling as
 * {@link ItemCollectorTileEntity}, plus an optional type filter.
 * <p>
 * Simplification, disclosed here: 1.12.2's filter was a whole separate
 * configurable item (`ItemItemFilter`) with its own 9-slot example-item
 * inventory, whitelist/blacklist mode, and independently toggleable
 * item/metadata/NBT/ore-dictionary matching. This port uses one plain
 * example-item slot directly on the collector instead, matching by item
 * type (`ItemStack.areItemsEqual`, i.e. item + damage, ignoring NBT/count) -
 * still real, useful filtering, just not the original's full configurable
 * matching system.
 */
public class AdvancedItemCollectorTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider
{
	public static final int MAX_RANGE = 16;

	private int currentTickRate = 20;
	private int counter = 0;

	private int rangeX = 5;
	private int rangeY = 5;
	private int rangeZ = 5;

	private final ItemStackHandler filterInventory = new ItemStackHandler(1)
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			markDirty();
		}
	};

	public AdvancedItemCollectorTileEntity()
	{
		super(ModTileEntityTypes.ADVANCED_ITEM_COLLECTOR);
	}

	public ItemStackHandler filterInventory()
	{
		return filterInventory;
	}

	public int getRangeX()
	{
		return rangeX;
	}

	public int getRangeY()
	{
		return rangeY;
	}

	public int getRangeZ()
	{
		return rangeZ;
	}

	public void adjustRangeX(int amount)
	{
		rangeX = clampRange(rangeX + amount);
		this.markDirty();
	}

	public void adjustRangeY(int amount)
	{
		rangeY = clampRange(rangeY + amount);
		this.markDirty();
	}

	public void adjustRangeZ(int amount)
	{
		rangeZ = clampRange(rangeZ + amount);
		this.markDirty();
	}

	private static int clampRange(int value)
	{
		return Math.max(0, Math.min(MAX_RANGE, value));
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

		Direction facing = this.getBlockState().get(AdvancedItemCollectorBlock.FACING);
		TileEntity target = this.world.getTileEntity(this.pos.offset(facing.getOpposite()));

		boolean didSomething = false;

		if (target != null)
		{
			LazyOptional<IItemHandler> capability = target.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite());
			IItemHandler itemHandler = capability.orElse(null);

			if (itemHandler != null)
			{
				ItemStack filterStack = filterInventory.getStackInSlot(0);

				List<ItemEntity> nearbyItems = this.world.getEntitiesWithinAABB(ItemEntity.class, new AxisAlignedBB(this.pos.add(-rangeX, -rangeY, -rangeZ), this.pos.add(rangeX + 1, rangeY + 1, rangeZ + 1)), EntityPredicates.IS_ALIVE);

				for (ItemEntity itemEntity : nearbyItems)
				{
					if (!itemEntity.isAlive())
					{
						continue;
					}

					if (!filterStack.isEmpty() && !ItemStack.areItemsEqual(filterStack, itemEntity.getItem()))
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

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		compound.putInt("rangeX", rangeX);
		compound.putInt("rangeY", rangeY);
		compound.putInt("rangeZ", rangeZ);
		compound.put("inventory", filterInventory.serializeNBT());
		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		rangeX = compound.getInt("rangeX");
		rangeY = compound.getInt("rangeY");
		rangeZ = compound.getInt("rangeZ");
		filterInventory.deserializeNBT(compound.getCompound("inventory"));
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new AdvancedItemCollectorContainer(windowId, playerInventory, filterInventory, IWorldPosCallable.of(this.world, pos));
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.advanced_item_collector");
	}
}
