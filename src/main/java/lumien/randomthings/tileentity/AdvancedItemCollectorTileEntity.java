package lumien.randomthings.tileentity;

import lumien.randomthings.util.CapUtil;
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


import lumien.randomthings.itemhandler.IItemHandler;
import lumien.randomthings.itemhandler.ItemHandlerHelper;
import lumien.randomthings.itemhandler.ItemStackHandler;
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
		if (this.level.isClientSide())
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
			IItemHandler itemHandler = CapUtil.itemHandler(target, facing);

			if (itemHandler != null)
			{
				ItemStack filterStack = filterInventory.getStackInSlot(0);

				List<ItemEntity> nearbyItems = this.level.getEntitiesOfClass(ItemEntity.class, lumien.randomthings.util.AABBs.of(this.worldPosition.offset(-rangeX, -rangeY, -rangeZ), this.worldPosition.offset(rangeX + 1, rangeY + 1, rangeZ + 1)), EntitySelector.ENTITY_STILL_ALIVE);

				for (ItemEntity itemEntity : nearbyItems)
				{
					if (!itemEntity.isAlive())
					{
						continue;
					}

					if (!filterStack.isEmpty() && !ItemStack.isSameItem(filterStack, itemEntity.getItem()))
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
	protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput rtOut)
	{
		super.saveAdditional(rtOut);
		CompoundTag rtTag = new CompoundTag();
		rtSaveAdditional(rtTag, lumien.randomthings.util.StackTag.provider());
		lumien.randomthings.util.TagIO.write(rtOut, rtTag);
	}

	private void rtSaveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		compound.putInt("rangeX", rangeX);
		compound.putInt("rangeY", rangeY);
		compound.putInt("rangeZ", rangeZ);
		compound.put("inventory", filterInventory.serializeNBT(registries));
	}

	@Override
	protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
	{
		super.loadAdditional(rtIn);
		rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
	}

	private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		rangeX = compound.getIntOr("rangeX", 0);
		rangeY = compound.getIntOr("rangeY", 0);
		rangeZ = compound.getIntOr("rangeZ", 0);
		filterInventory.deserializeNBT(registries, compound.getCompoundOrEmpty("inventory"));
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new AdvancedItemCollectorContainer(windowId, playerInventory, filterInventory, ContainerLevelAccess.create(this.level, worldPosition));
	}

	@Override
	public Component getDisplayName()
	{
		return Component.translatable("block.randomthings.advanced_item_collector");
	}

	@Override
	public void preRemoveSideEffects(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state)
	{
		super.preRemoveSideEffects(pos, state);

		// 1.21.5+: Block.onRemove is gone and the tile entity is already removed when the neighbour callback runs, so the block's old onRemove code runs from here
		if (this.level != null && state.getBlock() instanceof lumien.randomthings.block.AdvancedItemCollectorBlock block)
		{
			block.rtOnRemove(state, this.level, pos, this.level.getBlockState(pos), false);
		}
	}
}
