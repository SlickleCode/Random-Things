package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import lumien.randomthings.container.AdvancedRedstoneRepeaterContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Holds the configurable on/off delay (in game ticks, 2-10000) for
 * {@link lumien.randomthings.block.AdvancedRedstoneRepeaterBlock}, standing
 * in for vanilla's fixed 1-4 tick / 0-3 DELAY property.
 */
public class AdvancedRedstoneRepeaterTileEntity extends BlockEntity implements MenuProvider
{
	private int turnOnDelay = 20;
	private int turnOffDelay = 20;

	public AdvancedRedstoneRepeaterTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.ADVANCED_REDSTONE_REPEATER, pos, state);
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
		compound.putInt("turnOnDelay", turnOnDelay);
		compound.putInt("turnOffDelay", turnOffDelay);
	}

	@Override
	protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
	{
		super.loadAdditional(rtIn);
		rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
	}

	private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		this.turnOnDelay = compound.getIntOr("turnOnDelay", 0);
		this.turnOffDelay = compound.getIntOr("turnOffDelay", 0);
	}

	public int turnOnDelay()
	{
		return turnOnDelay;
	}

	public int turnOffDelay()
	{
		return turnOffDelay;
	}

	public void adjustTurnOnDelay(int amount)
	{
		turnOnDelay = Math.max(0, Math.min(120, turnOnDelay + amount));
		this.setChanged();
	}

	public void adjustTurnOffDelay(int amount)
	{
		turnOffDelay = Math.max(0, Math.min(120, turnOffDelay + amount));
		this.setChanged();
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new AdvancedRedstoneRepeaterContainer(windowId, ContainerLevelAccess.create(this.level, worldPosition));
	}

	@Override
	public Component getDisplayName()
	{
		return Component.translatable("block.randomthings.advanced_redstone_repeater");
	}
}
