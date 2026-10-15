package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import java.util.List;

import lumien.randomthings.block.AdvancedItemCollectorBlock;
import lumien.randomthings.container.AdvancedItemCollectorContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraft.world.level.block.state.BlockState;

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
public class AdvancedItemCollectorTileEntity extends BlockEntity implements MenuProvider
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
			setChanged();
		}
	};

	public AdvancedItemCollectorTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.ADVANCED_ITEM_COLLECTOR, pos, state);
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
		this.setChanged();
	}

	public void adjustRangeY(int amount)
	{
		rangeY = clampRange(rangeY + amount);
		this.setChanged();
	}

	public void adjustRangeZ(int amount)
	{
		rangeZ = clampRange(rangeZ + amount);
		this.setChanged();
	}

	private static int clampRange(int value)
	{
		return Math.max(0, Math.min(MAX_RANGE, value));
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

		Direction facing = this.getBlockState().getValue(AdvancedItemCollectorBlock.FACING);
		BlockEntity target = this.level.getBlockEntity(this.worldPosition.relative(facing.getOpposite()));

		boolean didSomething = false;

		if (target != null)
		{
			LazyOptional<IItemHandler> capability = target.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
			IItemHandler itemHandler = capability.orElse(null);

			if (itemHandler != null)
			{
				ItemStack filterStack = filterInventory.getStackInSlot(0);

				List<ItemEntity> nearbyItems = this.level.getEntitiesOfClass(ItemEntity.class, new AABB(this.worldPosition.offset(-rangeX, -rangeY, -rangeZ), this.worldPosition.offset(rangeX + 1, rangeY + 1, rangeZ + 1)), EntitySelector.ENTITY_STILL_ALIVE);

				for (ItemEntity itemEntity : nearbyItems)
				{
					if (!itemEntity.isAlive())
					{
						continue;
					}

					if (!filterStack.isEmpty() && !ItemStack.isSame(filterStack, itemEntity.getItem()))
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

	@Override
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.putInt("rangeX", rangeX);
		compound.putInt("rangeY", rangeY);
		compound.putInt("rangeZ", rangeZ);
		compound.put("inventory", filterInventory.serializeNBT());
	}

	@Override
	public void load(CompoundTag compound)
	{
		super.load(compound);
		rangeX = compound.getInt("rangeX");
		rangeY = compound.getInt("rangeY");
		rangeZ = compound.getInt("rangeZ");
		filterInventory.deserializeNBT(compound.getCompound("inventory"));
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new AdvancedItemCollectorContainer(windowId, playerInventory, filterInventory, ContainerLevelAccess.create(this.level, worldPosition));
	}

	@Override
	public Component getDisplayName()
	{
		return new TranslatableComponent("block.randomthings.advanced_item_collector");
	}
}
