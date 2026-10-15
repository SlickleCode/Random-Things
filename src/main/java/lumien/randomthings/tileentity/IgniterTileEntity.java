package lumien.randomthings.tileentity;

import java.util.Random;

import lumien.randomthings.block.IgniterBlock;
import lumien.randomthings.container.IgniterContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class IgniterTileEntity extends BlockEntity implements MenuProvider
{
	public enum MODE
	{
		TOGGLE, IGNITE;
	}

	private MODE mode = MODE.TOGGLE;
	private boolean powered = false;

	public IgniterTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.IGNITER, pos, state);
    }

	@Override
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.putInt("mode", this.mode.ordinal());
	}

	@Override
	public void load(CompoundTag compound)
	{
		super.load(compound);
		this.mode = MODE.values()[compound.getInt("mode")];
	}

	public MODE mode()
	{
		return mode;
	}

	private BlockPos frontPos()
	{
		BlockState state = this.level.getBlockState(worldPosition);
		Direction facing = state.getValue(IgniterBlock.FACING);
		return worldPosition.relative(facing);
	}

	private void ignite(BlockPos target)
	{
		if (level.isEmptyBlock(target) && Blocks.FIRE.defaultBlockState().canSurvive(level, target))
		{
			level.playSound(null, worldPosition, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, new Random().nextFloat() * 0.4F + 0.8F);
			level.setBlockAndUpdate(target, Blocks.FIRE.defaultBlockState());
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
			if (level.getBlockState(front).getBlock() == Blocks.FIRE && mode == MODE.TOGGLE)
			{
				level.removeBlock(front, false);
			}
		}
		else if (newPowered && !oldPowered)
		{
			ignite(frontPos());
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
				mode = MODE.TOGGLE;
				break;
		}

		this.setChanged();
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new IgniterContainer(windowId, ContainerLevelAccess.create(this.level, worldPosition));
	}

	@Override
	public Component getDisplayName()
	{
		return Component.translatable("block.randomthings.igniter");
	}
}
