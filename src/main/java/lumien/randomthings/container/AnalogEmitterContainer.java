package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.AnalogEmitterTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;

/**
 * AnalogEmitterContainer
 */
public class AnalogEmitterContainer extends AbstractContainerMenu implements ISignalContainer
{
	ContainerLevelAccess pos;

	public DataSlot emitLevel = DataSlot.standalone();

	public AnalogEmitterContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData)
	{
		this(windowId, ContainerLevelAccess.NULL);
	}

	public AnalogEmitterContainer(int windowId, ContainerLevelAccess pos)
	{
		super(ModContainerTypes.ANALOG_EMITTER, windowId);

		this.pos = pos;

		this.addDataSlot(emitLevel);
	}

	@Override
	public boolean stillValid(Player playerIn)
	{
		return stillValid(this.pos, playerIn, ModBlocks.ANALOG_EMITTER);
	}

	@Override
	public void broadcastChanges()
	{
		this.pos.execute((world, pos) -> {
			BlockEntity te = world.getBlockEntity(pos);

			if (te instanceof AnalogEmitterTileEntity)
			{
				this.emitLevel.set(((AnalogEmitterTileEntity) te).emitLevel());
			}
		});

		super.broadcastChanges();
	}

	@Override
	public void handle(int id, FriendlyByteBuf data)
	{
		if (id == 0)
		{
			int action = data.readInt();

			this.pos.execute((world, pos) -> {
				BlockEntity te = world.getBlockEntity(pos);

				if (te instanceof AnalogEmitterTileEntity)
				{
					AnalogEmitterTileEntity ae = (AnalogEmitterTileEntity) te;

					if (action == 0)
					{
						ae.setEmitLevel(Math.max(0, ae.emitLevel() - 1));
					}
					else if (action == 1)
					{
						ae.setEmitLevel(Math.min(15, ae.emitLevel() + 1));
					}
				}
			});
		}
	}

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
