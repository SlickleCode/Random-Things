package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.AnalogEmitterTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;

/**
 * AnalogEmitterContainer
 */
public class AnalogEmitterContainer extends Container implements ISignalContainer
{
	IWorldPosCallable pos;

	public IntReferenceHolder emitLevel = IntReferenceHolder.single();

	public AnalogEmitterContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, IWorldPosCallable.DUMMY);
	}

	public AnalogEmitterContainer(int windowId, IWorldPosCallable pos)
	{
		super(ModContainerTypes.ANALOG_EMITTER, windowId);

		this.pos = pos;

		this.trackInt(emitLevel);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.ANALOG_EMITTER);
	}

	@Override
	public void detectAndSendChanges()
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

			if (te instanceof AnalogEmitterTileEntity)
			{
				this.emitLevel.set(((AnalogEmitterTileEntity) te).emitLevel());
			}
		});

		super.detectAndSendChanges();
	}

	@Override
	public void handle(int id, PacketBuffer data)
	{
		if (id == 0)
		{
			int action = data.readInt();

			this.pos.consume((world, pos) -> {
				TileEntity te = world.getTileEntity(pos);

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
}
