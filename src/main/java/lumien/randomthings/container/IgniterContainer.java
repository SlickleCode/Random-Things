package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.IgniterTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;

/**
 * IgniterContainer
 */
public class IgniterContainer extends Container implements ISignalContainer
{
	IWorldPosCallable pos;

	public IntReferenceHolder mode = IntReferenceHolder.single();

	public IgniterContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, IWorldPosCallable.DUMMY);
	}

	public IgniterContainer(int windowId, IWorldPosCallable pos)
	{
		super(ModContainerTypes.IGNITER, windowId);

		this.pos = pos;

		this.trackInt(mode);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.IGNITER);
	}

	@Override
	public void detectAndSendChanges()
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

			if (te instanceof IgniterTileEntity)
			{
				this.mode.set(((IgniterTileEntity) te).mode().ordinal());
			}
		});

		super.detectAndSendChanges();
	}

	@Override
	public void handle(int id, PacketBuffer data)
	{
		if (id == 0)
		{
			this.pos.consume((world, pos) -> {
				TileEntity te = world.getTileEntity(pos);

				if (te instanceof IgniterTileEntity)
				{
					((IgniterTileEntity) te).rotateMode();
				}
			});
		}
	}
}
