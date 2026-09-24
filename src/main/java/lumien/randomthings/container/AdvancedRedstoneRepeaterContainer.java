package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.AdvancedRedstoneRepeaterTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;

/**
 * AdvancedRedstoneRepeaterContainer
 */
public class AdvancedRedstoneRepeaterContainer extends Container implements ISignalContainer
{
	IWorldPosCallable pos;

	public IntReferenceHolder turnOnDelay = IntReferenceHolder.single();
	public IntReferenceHolder turnOffDelay = IntReferenceHolder.single();

	public AdvancedRedstoneRepeaterContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, IWorldPosCallable.DUMMY);
	}

	public AdvancedRedstoneRepeaterContainer(int windowId, IWorldPosCallable pos)
	{
		super(ModContainerTypes.ADVANCED_REDSTONE_REPEATER, windowId);

		this.pos = pos;

		this.trackInt(turnOnDelay);
		this.trackInt(turnOffDelay);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.ADVANCED_REDSTONE_REPEATER);
	}

	@Override
	public void detectAndSendChanges()
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

			if (te instanceof AdvancedRedstoneRepeaterTileEntity)
			{
				AdvancedRedstoneRepeaterTileEntity arr = (AdvancedRedstoneRepeaterTileEntity) te;

				this.turnOnDelay.set(arr.turnOnDelay());
				this.turnOffDelay.set(arr.turnOffDelay());
			}
		});

		super.detectAndSendChanges();
	}

	@Override
	public void handle(int id, PacketBuffer data)
	{
		int amount = data.readInt();

		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

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
