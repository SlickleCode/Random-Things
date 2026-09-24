package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.ProcessingPlateTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;

/**
 * ProcessingPlateContainer
 */
public class ProcessingPlateContainer extends Container implements ISignalContainer
{
	IWorldPosCallable pos;

	public IntReferenceHolder insertFacing = IntReferenceHolder.single();
	public IntReferenceHolder extractFacing = IntReferenceHolder.single();

	public ProcessingPlateContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, IWorldPosCallable.DUMMY);
	}

	public ProcessingPlateContainer(int windowId, IWorldPosCallable pos)
	{
		super(ModContainerTypes.PROCESSING_PLATE, windowId);

		this.pos = pos;

		this.trackInt(insertFacing);
		this.trackInt(extractFacing);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.PLATE_PROCESSING);
	}

	@Override
	public void detectAndSendChanges()
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

			if (te instanceof ProcessingPlateTileEntity)
			{
				ProcessingPlateTileEntity pp = (ProcessingPlateTileEntity) te;
				this.insertFacing.set(pp.insertFacing().ordinal());
				this.extractFacing.set(pp.extractFacing().ordinal());
			}
		});

		super.detectAndSendChanges();
	}

	@Override
	public void handle(int id, PacketBuffer data)
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

			if (te instanceof ProcessingPlateTileEntity)
			{
				ProcessingPlateTileEntity pp = (ProcessingPlateTileEntity) te;

				if (id == 0)
				{
					pp.rotateInsertFacing();
				}
				else if (id == 1)
				{
					pp.rotateExtractFacing();
				}
			}
		});
	}
}
