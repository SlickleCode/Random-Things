package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import lumien.randomthings.container.AdvancedRedstoneTorchContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

public class AdvancedRedstoneTorchTileEntity extends BlockEntity implements MenuProvider
{
	int signalStrengthRed = 15;
	int signalStrengthGreen = 0;

	public AdvancedRedstoneTorchTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.ADVANCED_REDSTONE_TORCH, pos, state);
    }

	@Override
	protected void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.saveAdditional(compound, registries);
		compound.putInt("signalStrengthRed", signalStrengthRed);
		compound.putInt("signalStrengthGreen", signalStrengthGreen);

	}

	@Override
	public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.loadAdditional(compound, registries);
		this.signalStrengthRed = compound.getInt("signalStrengthRed");
		this.signalStrengthGreen = compound.getInt("signalStrengthGreen");
	}

	public int signalStrengthRed()
	{
		return signalStrengthRed;
	}

	public int signalStrengthGreen()
	{
		return signalStrengthGreen;
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new AdvancedRedstoneTorchContainer(windowId, ContainerLevelAccess.create(this.level, worldPosition));
	}

	@Override
	public Component getDisplayName()
	{
		return Component.translatable("block.randomthings.advanced_redstone_torch");
	}

	public void setSignalStrengthGreen(int newValue)
	{
		this.signalStrengthGreen = newValue;
	}

	public void setSignalStrengthRed(int newValue)
	{
		this.signalStrengthRed = newValue;
	}
}