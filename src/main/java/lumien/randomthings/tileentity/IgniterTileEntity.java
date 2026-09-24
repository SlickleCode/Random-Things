package lumien.randomthings.tileentity;

import java.util.Random;

import lumien.randomthings.block.IgniterBlock;
import lumien.randomthings.container.IgniterContainer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class IgniterTileEntity extends TileEntity implements INamedContainerProvider
{
	public enum MODE
	{
		TOGGLE, IGNITE, KEEP_IGNITED;
	}

	private MODE mode = MODE.TOGGLE;
	private boolean powered = false;

	public IgniterTileEntity()
	{
		super(ModTileEntityTypes.IGNITER);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		compound.putInt("mode", this.mode.ordinal());
		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		this.mode = MODE.values()[compound.getInt("mode")];
	}

	public MODE mode()
	{
		return mode;
	}

	private BlockPos frontPos()
	{
		BlockState state = this.world.getBlockState(pos);
		Direction facing = state.get(IgniterBlock.FACING);
		return pos.offset(facing);
	}

	private void ignite(BlockPos target)
	{
		if (world.isAirBlock(target) && Blocks.FIRE.getDefaultState().isValidPosition(world, target))
		{
			world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.BLOCKS, 1.0F, new Random().nextFloat() * 0.4F + 0.8F);
			world.setBlockState(target, Blocks.FIRE.getDefaultState());
		}
	}

	/**
	 * Called by the block's neighborChanged so KEEP_IGNITED mode relights
	 * the fire in front of it if something put it out.
	 */
	public void checkKeepIgnited()
	{
		if (mode == MODE.KEEP_IGNITED)
		{
			ignite(frontPos());
		}
	}

	/**
	 * Called by the block's neighborChanged whenever the block's own redstone
	 * power state may have changed (there is no ASM/Forge hook that fires only
	 * on an actual transition in 1.14.4, so we track the previous state
	 * ourselves and diff it, matching 1.12.2's IRedstoneSensitive callback).
	 */
	public void updatePowerState(boolean newPowered)
	{
		boolean oldPowered = this.powered;
		this.powered = newPowered;

		if (oldPowered && !newPowered)
		{
			BlockPos front = frontPos();
			if (world.getBlockState(front).getBlock() == Blocks.FIRE && mode == MODE.TOGGLE)
			{
				world.removeBlock(front, false);
			}
		}
		else if (newPowered && !oldPowered)
		{
			if (mode != MODE.KEEP_IGNITED)
			{
				ignite(frontPos());
			}
		}
	}

	public void rotateMode()
	{
		switch (mode)
		{
			case TOGGLE:
				mode = MODE.IGNITE;
				break;
			case IGNITE:
				mode = MODE.KEEP_IGNITED;
				ignite(frontPos());
				break;
			case KEEP_IGNITED:
				mode = MODE.TOGGLE;
				break;
		}

		this.markDirty();
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new IgniterContainer(windowId, IWorldPosCallable.of(this.world, pos));
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.igniter");
	}
}
