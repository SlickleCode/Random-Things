package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import lumien.randomthings.block.AnalogEmitterBlock;
import lumien.randomthings.container.AnalogEmitterContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class AnalogEmitterTileEntity extends BlockEntity implements MenuProvider
{
	private int emitLevel = 1;
	private boolean powering = false;

	public AnalogEmitterTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.ANALOG_EMITTER, pos, state);
    }

	@Override
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);
		compound.putInt("emitLevel", emitLevel);
	}

	@Override
	public void load(CompoundTag compound)
	{
		super.load(compound);
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
		this.setChanged();
		notifyNeighbors();
	}

	/**
	 * Re-derives the powering flag from the redstone signal coming in on the
	 * facing side, matching 1.12.2 (getRedstonePower(pos.offset(facing), facing.getOpposite())).
	 */
	public void updateInput(Level world, Direction facing)
	{
		boolean input = world.getSignal(worldPosition.relative(facing), facing.getOpposite()) > 0;

		if (input != powering)
		{
			powering = input;
			notifyNeighbors();
		}
	}

	private void notifyNeighbors()
	{
		if (this.level != null)
		{
			BlockState state = this.level.getBlockState(worldPosition);
			this.level.updateNeighborsAt(worldPosition, state.getBlock());

			for (Direction facing : Direction.values())
			{
				this.level.updateNeighborsAt(worldPosition.relative(facing), state.getBlock());
			}
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new AnalogEmitterContainer(windowId, ContainerLevelAccess.create(this.level, worldPosition));
	}

	@Override
	public Component getDisplayName()
	{
		return Component.translatable("block.randomthings.analog_emitter");
	}
}
