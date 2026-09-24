package lumien.randomthings.tileentity;

import lumien.randomthings.block.AnalogEmitterBlock;
import lumien.randomthings.container.AnalogEmitterContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class AnalogEmitterTileEntity extends TileEntity implements INamedContainerProvider
{
	private int emitLevel = 1;
	private boolean powering = false;

	public AnalogEmitterTileEntity()
	{
		super(ModTileEntityTypes.ANALOG_EMITTER);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		compound.putInt("emitLevel", emitLevel);
		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		this.emitLevel = compound.getInt("emitLevel");
	}

	public int emitLevel()
	{
		return emitLevel;
	}

	public int getOutput()
	{
		return powering ? emitLevel : 0;
	}

	public void setEmitLevel(int level)
	{
		this.emitLevel = level;
		this.markDirty();
		notifyNeighbors();
	}

	/**
	 * Re-derives the powering flag from the redstone signal coming in on the
	 * facing side, matching 1.12.2's amplifier behavior.
	 */
	public void updateInput(World world, Direction facing)
	{
		boolean input = world.getRedstonePower(pos.offset(facing), facing) > 0;

		if (input != powering)
		{
			powering = input;
			notifyNeighbors();
		}
	}

	private void notifyNeighbors()
	{
		if (this.world != null)
		{
			BlockState state = this.world.getBlockState(pos);
			this.world.notifyNeighborsOfStateChange(pos, state.getBlock());

			for (Direction facing : Direction.values())
			{
				this.world.notifyNeighborsOfStateChange(pos.offset(facing), state.getBlock());
			}
		}
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new AnalogEmitterContainer(windowId, IWorldPosCallable.of(this.world, pos));
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.analog_emitter");
	}
}
