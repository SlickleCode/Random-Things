package lumien.randomthings.tileentity;

import javax.annotation.Nullable;

import lumien.randomthings.block.IronDropperBlock;
import lumien.randomthings.container.IronDropperContainer;
import net.minecraft.block.BlockState;
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
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

/**
 * A block-form dropper with a 9-slot inventory that ejects one item at a
 * time toward its facing (or feeds it directly into whatever inventory sits
 * there), driven by a configurable redstone mode/pickup delay/motion/effects
 * set instead of vanilla dispenser's single fixed behavior.
 */
public class IronDropperTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider
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
			markDirty();
		}
	};

	private final LazyOptional<IItemHandler> itemHandlerCap = LazyOptional.of(() -> itemHandler);

	public IronDropperTileEntity()
	{
		super(ModTileEntityTypes.IRON_DROPPER);
	}

	public IItemHandler itemHandler()
	{
		return itemHandler;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side)
	{
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			return itemHandlerCap.cast();
		}

		return super.getCapability(cap, side);
	}

	@Override
	protected void invalidateCaps()
	{
		super.invalidateCaps();
		itemHandlerCap.invalidate();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		compound.putInt("redstoneMode", redstoneMode.ordinal());
		compound.putInt("pickupDelay", pickupDelay.ordinal());
		compound.putInt("effects", effects.ordinal());
		compound.putBoolean("randomMotion", randomMotion);
		compound.putInt("dropCounter", dropCounter);
		compound.put("inventory", itemHandler.serializeNBT());
		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		this.redstoneMode = REDSTONE_MODE.values()[compound.getInt("redstoneMode")];
		this.pickupDelay = PICKUP_DELAY.values()[compound.getInt("pickupDelay")];
		this.effects = EFFECTS.values()[compound.getInt("effects")];
		this.randomMotion = compound.getBoolean("randomMotion");
		this.dropCounter = compound.getInt("dropCounter");
		itemHandler.deserializeNBT(compound.getCompound("inventory"));
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
		this.markDirty();
	}

	public void rotatePickupDelay()
	{
		int next = (pickupDelay.ordinal() + 1) % PICKUP_DELAY.values().length;
		pickupDelay = PICKUP_DELAY.values()[next];
		this.markDirty();
	}

	public void rotateRandomMotion()
	{
		randomMotion = !randomMotion;
		this.markDirty();
	}

	public void rotateEffects()
	{
		int next = (effects.ordinal() + 1) % EFFECTS.values().length;
		effects = EFFECTS.values()[next];
		this.markDirty();
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

	@Override
	public void tick()
	{
		if (this.world.isRemote)
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

		Direction facing = this.getBlockState().get(IronDropperBlock.FACING);
		TileEntity target = world.getTileEntity(pos.offset(facing));

		ItemStack toDrop = stack.copy();
		toDrop.setCount(1);

		LazyOptional<IItemHandler> targetCap = target != null ? target.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite()) : LazyOptional.empty();
		IItemHandler targetHandler = targetCap.orElse(null);

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
			world.playEvent(1000, this.pos, 0);
		}

		if (effects == EFFECTS.PARTICLE || effects == EFFECTS.SOUND_PARTICLE)
		{
			// WorldRenderer's handler for event 2000 (dispenser smoke) decodes the
			// data parameter via Direction.byIndex, not the packed 3x3-grid formula
			// this used to compute (which only ever produced values matching UP by
			// coincidence for horizontal facings) - confirmed via `javap -c` on
			// WorldRenderer, since the particle-direction encoding isn't documented
			// anywhere else.
			world.playEvent(2000, this.pos, facing.getIndex());
		}
	}

	private void spawnDroppedItem(ItemStack toDrop, Direction facing)
	{
		double posX = pos.getX() + 0.5 + 0.7D * facing.getXOffset();
		double posY = pos.getY() + 0.5 + 0.7D * facing.getYOffset();
		double posZ = pos.getZ() + 0.5 + 0.7D * facing.getZOffset();

		posY -= facing.getAxis() == Direction.Axis.Y ? 0.125D : 0.15625D;

		ItemEntity itemEntity = new ItemEntity(world, posX, posY, posZ, toDrop);

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
		itemEntity.setPickupDelay(pickupDelayTicks);

		double speed = 6;
		double d3 = randomMotion ? world.rand.nextDouble() * 0.1D + 0.2D : 0.25D;

		double motionX = facing.getXOffset() * d3;
		double motionY = 0.2D;
		double motionZ = facing.getZOffset() * d3;

		if (randomMotion)
		{
			motionX += world.rand.nextGaussian() * 0.0075D * speed;
			motionY += world.rand.nextGaussian() * 0.0075D * speed;
			motionZ += world.rand.nextGaussian() * 0.0075D * speed;
		}
		else
		{
			motionX += facing.getXOffset() * 0.5 * 0.0075D * speed;
			motionY += facing.getYOffset() * 0.5 * 0.0075D * speed;
			motionZ += facing.getZOffset() * 0.5 * 0.0075D * speed;
		}

		itemEntity.setMotion(new Vec3d(motionX, motionY, motionZ));

		((ServerWorld) world).addEntity(itemEntity);
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new IronDropperContainer(windowId, playerInventory, itemHandler, IWorldPosCallable.of(this.world, pos));
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.iron_dropper");
	}
}
