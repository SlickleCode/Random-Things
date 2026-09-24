package lumien.randomthings.tileentity;

import lumien.randomthings.container.AdvancedRedstoneRepeaterContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Holds the configurable on/off delay (in game ticks, 2-10000) for
 * {@link lumien.randomthings.block.AdvancedRedstoneRepeaterBlock}, standing
 * in for vanilla's fixed 1-4 tick / 0-3 DELAY property.
 */
public class AdvancedRedstoneRepeaterTileEntity extends TileEntity implements INamedContainerProvider
{
	private int turnOnDelay = 20;
	private int turnOffDelay = 20;

	public AdvancedRedstoneRepeaterTileEntity()
	{
		super(ModTileEntityTypes.ADVANCED_REDSTONE_REPEATER);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		compound.putInt("turnOnDelay", turnOnDelay);
		compound.putInt("turnOffDelay", turnOffDelay);
		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		this.turnOnDelay = compound.getInt("turnOnDelay");
		this.turnOffDelay = compound.getInt("turnOffDelay");
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
		turnOnDelay = Math.max(2, Math.min(10000, turnOnDelay + amount));
		this.markDirty();
	}

	public void adjustTurnOffDelay(int amount)
	{
		turnOffDelay = Math.max(2, Math.min(10000, turnOffDelay + amount));
		this.markDirty();
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new AdvancedRedstoneRepeaterContainer(windowId, IWorldPosCallable.of(this.world, pos));
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.advanced_redstone_repeater");
	}
}
