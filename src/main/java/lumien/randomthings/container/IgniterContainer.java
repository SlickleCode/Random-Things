package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.IgniterTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;

/**
 * IgniterContainer
 */
public class IgniterContainer extends AbstractContainerMenu implements ISignalContainer
{
	ContainerLevelAccess pos;

	public DataSlot mode = DataSlot.standalone();

	public IgniterContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData)
	{
		this(windowId, ContainerLevelAccess.NULL);
	}

	public IgniterContainer(int windowId, ContainerLevelAccess pos)
	{
		super(ModContainerTypes.IGNITER, windowId);

		this.pos = pos;

		this.addDataSlot(mode);
	}

	@Override
	public boolean stillValid(Player playerIn)
	{
		return stillValid(this.pos, playerIn, ModBlocks.IGNITER);
	}

	@Override
	public void broadcastChanges()
	{
		this.pos.execute((world, pos) -> {
			BlockEntity te = world.getBlockEntity(pos);

			if (te instanceof IgniterTileEntity)
			{
				this.mode.set(((IgniterTileEntity) te).mode().ordinal());
			}
		});

		super.broadcastChanges();
	}

	@Override
	public void handle(int id, FriendlyByteBuf data)
	{
		if (id == 0)
		{
			this.pos.execute((world, pos) -> {
				BlockEntity te = world.getBlockEntity(pos);

				if (te instanceof IgniterTileEntity)
				{
					((IgniterTileEntity) te).rotateMode();
				}
			});
		}
	}
}
