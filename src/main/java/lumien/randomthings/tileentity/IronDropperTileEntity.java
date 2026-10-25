package lumien.randomthings.tileentity;

import lumien.randomthings.util.CapUtil;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

import lumien.randomthings.block.IronDropperBlock;
import lumien.randomthings.container.IronDropperContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;



import lumien.randomthings.itemhandler.IItemHandler;
import lumien.randomthings.itemhandler.ItemHandlerHelper;
import lumien.randomthings.itemhandler.ItemStackHandler;

/**
 * A block-form dropper with a 9-slot inventory that ejects one item at a
 * time toward its facing (or feeds it directly into whatever inventory sits
 * there), driven by a configurable redstone mode/pickup delay/motion/effects
 * set instead of vanilla dispenser's single fixed behavior.
 */
public class IronDropperTileEntity extends BlockEntity implements MenuProvider
{
	public enum REDSTONE_MODE
	{
		PULSE, REPEAT_POWERED;
	}

	public enum PICKUP_DELAY
	{
		NONE, TICKS_5, TICKS_20;
	}

	public enum EFFECTS
	{
		NONE, SOUND, PARTICLE, SOUND_PARTICLE;
	}

	private REDSTONE_MODE redstoneMode = REDSTONE_MODE.REPEAT_POWERED;
	private PICKUP_DELAY pickupDelay = PICKUP_DELAY.TICKS_5;
	private EFFECTS effects = EFFECTS.NONE;
	private boolean randomMotion;

	private int dropCounter = 0;
	private boolean powered = false;

	private final ItemStackHandler itemHandler = new ItemStackHandler(9)
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			setChanged();
		}
	};


	public IronDropperTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.IRON_DROPPER, pos, state);
    }

	public IItemHandler itemHandler()
	{
		return itemHandler;
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
		compound.putInt("redstoneMode", redstoneMode.ordinal());
		compound.putInt("pickupDelay", pickupDelay.ordinal());
		compound.putInt("effects", effects.ordinal());
		compound.putBoolean("randomMotion", randomMotion);
		compound.putInt("dropCounter", dropCounter);
		compound.put("inventory", itemHandler.serializeNBT(registries));
	}

	@Override
	protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
	{
		super.loadAdditional(rtIn);
		rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
	}

	private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		this.redstoneMode = REDSTONE_MODE.values()[compound.getIntOr("redstoneMode", 0)];
		this.pickupDelay = PICKUP_DELAY.values()[compound.getIntOr("pickupDelay", 0)];
		this.effects = EFFECTS.values()[compound.getIntOr("effects", 0)];
		this.randomMotion = compound.getBooleanOr("randomMotion", false);
		this.dropCounter = compound.getIntOr("dropCounter", 0);
		itemHandler.deserializeNBT(registries, compound.getCompoundOrEmpty("inventory"));
	}

	public REDSTONE_MODE redstoneMode()
	{
		return redstoneMode;
	}

	public PICKUP_DELAY pickupDelay()
	{
		return pickupDelay;
	}

	public EFFECTS effects()
	{
		return effects;
	}

	public boolean randomMotion()
	{
		return randomMotion;
	}

	public void rotateRedstoneMode()
	{
		int next = (redstoneMode.ordinal() + 1) % REDSTONE_MODE.values().length;
		redstoneMode = REDSTONE_MODE.values()[next];
		this.setChanged();
	}

	public void rotatePickupDelay()
	{
		int next = (pickupDelay.ordinal() + 1) % PICKUP_DELAY.values().length;
		pickupDelay = PICKUP_DELAY.values()[next];
		this.setChanged();
	}

	public void rotateRandomMotion()
	{
		randomMotion = !randomMotion;
		this.setChanged();
	}

	public void rotateEffects()
	{
		int next = (effects.ordinal() + 1) % EFFECTS.values().length;
		effects = EFFECTS.values()[next];
		this.setChanged();
	}

	/**
	 * Called by the block's neighborChanged so PULSE mode drops exactly once
	 * per rising redstone edge (there is no transition-only hook in 1.14.4;
	 * same previous-state-diff technique as IgniterTileEntity).
	 */
	public void updatePowerState(boolean newPowered)
	{
		boolean oldPowered = this.powered;
		this.powered = newPowered;

		if (redstoneMode == REDSTONE_MODE.PULSE && newPowered && !oldPowered)
		{
			drop();
		}
	}

	public void tick()
	{
		if (this.level.isClientSide())
		{
			return;
		}

		dropCounter++;

		if (dropCounter % 4 == 0 && redstoneMode == REDSTONE_MODE.REPEAT_POWERED && powered)
		{
			drop();
		}
	}

	private void drop()
	{
		int slot = -1;
		ItemStack stack = ItemStack.EMPTY;

		for (int i = 0; i < itemHandler.getSlots(); i++)
		{
			stack = itemHandler.getStackInSlot(i);

			if (!stack.isEmpty())
			{
				slot = i;
				break;
			}
		}

		if (slot < 0 || stack.isEmpty())
		{
			return;
		}

		Direction facing = this.getBlockState().getValue(IronDropperBlock.FACING);
		BlockEntity target = level.getBlockEntity(worldPosition.relative(facing));

		ItemStack toDrop = stack.copy();
		toDrop.setCount(1);

		IItemHandler targetHandler = CapUtil.itemHandler(target, facing.getOpposite());

		if (targetHandler != null)
		{
			ItemStack result = ItemHandlerHelper.insertItemStacked(targetHandler, toDrop, false);

			if (result.isEmpty())
			{
				itemHandler.extractItem(slot, 1, false);
			}
		}
		else
		{
			itemHandler.extractItem(slot, 1, false);
			spawnDroppedItem(toDrop, facing);
		}

		if (effects == EFFECTS.SOUND || effects == EFFECTS.SOUND_PARTICLE)
		{
			level.levelEvent(1000, this.worldPosition, 0);
		}

		if (effects == EFFECTS.PARTICLE || effects == EFFECTS.SOUND_PARTICLE)
		{
			// WorldRenderer's handler for event 2000 (dispenser smoke) decodes the
			// data parameter via Direction.byIndex, not the packed 3x3-grid formula
			// this used to compute (which only ever produced values matching UP by
			// coincidence for horizontal facings) - confirmed via `javap -c` on
			// WorldRenderer, since the particle-direction encoding isn't documented
			// anywhere else.
			level.levelEvent(2000, this.worldPosition, facing.get3DDataValue());
		}
	}

	private void spawnDroppedItem(ItemStack toDrop, Direction facing)
	{
		double posX = worldPosition.getX() + 0.5 + 0.7D * facing.getStepX();
		double posY = worldPosition.getY() + 0.5 + 0.7D * facing.getStepY();
		double posZ = worldPosition.getZ() + 0.5 + 0.7D * facing.getStepZ();

		posY -= facing.getAxis() == Direction.Axis.Y ? 0.125D : 0.15625D;

		ItemEntity itemEntity = new ItemEntity(level, posX, posY, posZ, toDrop);

		int pickupDelayTicks;
		switch (pickupDelay)
		{
			case NONE:
				pickupDelayTicks = 0;
				break;
			case TICKS_20:
				pickupDelayTicks = 20;
				break;
			case TICKS_5:
			default:
				pickupDelayTicks = 5;
				break;
		}
		itemEntity.setPickUpDelay(pickupDelayTicks);

		double speed = 6;
		double d3 = randomMotion ? level.random.nextDouble() * 0.1D + 0.2D : 0.25D;

		double motionX = facing.getStepX() * d3;
		double motionY = 0.2D;
		double motionZ = facing.getStepZ() * d3;

		if (randomMotion)
		{
			motionX += level.random.nextGaussian() * 0.0075D * speed;
			motionY += level.random.nextGaussian() * 0.0075D * speed;
			motionZ += level.random.nextGaussian() * 0.0075D * speed;
		}
		else
		{
			motionX += facing.getStepX() * 0.5 * 0.0075D * speed;
			motionY += facing.getStepY() * 0.5 * 0.0075D * speed;
			motionZ += facing.getStepZ() * 0.5 * 0.0075D * speed;
		}

		itemEntity.setDeltaMovement(new Vec3(motionX, motionY, motionZ));

		((ServerLevel) level).addFreshEntity(itemEntity);
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new IronDropperContainer(windowId, playerInventory, itemHandler, ContainerLevelAccess.create(this.level, worldPosition));
	}

	@Override
	public Component getDisplayName()
	{
		return Component.translatable("block.randomthings.iron_dropper");
	}

	@Override
	public void preRemoveSideEffects(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state)
	{
		super.preRemoveSideEffects(pos, state);

		// 1.21.5+: Block.onRemove is gone and the tile entity is already removed when the neighbour callback runs, so the block's old onRemove code runs from here
		if (this.level != null && state.getBlock() instanceof lumien.randomthings.block.IronDropperBlock block)
		{
			block.rtOnRemove(state, this.level, pos, this.level.getBlockState(pos), false);
		}
	}
}
