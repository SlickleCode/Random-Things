package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.AdvancedRedstoneRepeaterTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;

/**
 * AdvancedRedstoneRepeaterContainer
 */
public class AdvancedRedstoneRepeaterContainer extends AbstractContainerMenu implements ISignalContainer
{
	ContainerLevelAccess pos;

	public DataSlot turnOnDelay = DataSlot.standalone();
	public DataSlot turnOffDelay = DataSlot.standalone();

	public AdvancedRedstoneRepeaterContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData)
	{
		this(windowId, ContainerLevelAccess.NULL);
	}

	public AdvancedRedstoneRepeaterContainer(int windowId, ContainerLevelAccess pos)
	{
		super(ModContainerTypes.ADVANCED_REDSTONE_REPEATER, windowId);

		this.pos = pos;

		this.addDataSlot(turnOnDelay);
		this.addDataSlot(turnOffDelay);
	}

	@Override
	public boolean stillValid(Player playerIn)
	{
		return stillValid(this.pos, playerIn, ModBlocks.ADVANCED_REDSTONE_REPEATER);
	}

	@Override
	public void broadcastChanges()
	{
		this.pos.execute((world, pos) -> {
			BlockEntity te = world.getBlockEntity(pos);

			if (te instanceof AdvancedRedstoneRepeaterTileEntity)
			{
				AdvancedRedstoneRepeaterTileEntity arr = (AdvancedRedstoneRepeaterTileEntity) te;

				this.turnOnDelay.set(arr.turnOnDelay());
				this.turnOffDelay.set(arr.turnOffDelay());
			}
		});

		super.broadcastChanges();
	}

	@Override
	public void handle(int id, FriendlyByteBuf data)
	{
		int amount = data.readInt();

		this.pos.execute((world, pos) -> {
			BlockEntity te = world.getBlockEntity(pos);

			if (te instanceof AdvancedRedstoneRepeaterTileEntity)
			{
				AdvancedRedstoneRepeaterTileEntity arr = (AdvancedRedstoneRepeaterTileEntity) te;

				if (id == 0)
				{
					arr.adjustTurnOnDelay(amount);
				}
				else if (id == 1)
				{
					arr.adjustTurnOffDelay(amount);
				}
			}
		});
	}
}
