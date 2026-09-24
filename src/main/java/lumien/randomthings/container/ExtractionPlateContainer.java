package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.ExtractionPlateTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;

/**
 * ExtractionPlateContainer
 */
public class ExtractionPlateContainer extends Container implements ISignalContainer
{
	IWorldPosCallable pos;

	public IntReferenceHolder extractFacing = IntReferenceHolder.single();

	public ExtractionPlateContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, IWorldPosCallable.DUMMY);
	}

	public ExtractionPlateContainer(int windowId, IWorldPosCallable pos)
	{
		super(ModContainerTypes.EXTRACTION_PLATE, windowId);

		this.pos = pos;

		this.trackInt(extractFacing);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.PLATE_EXTRACTION);
	}

	@Override
	public void detectAndSendChanges()
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

			if (te instanceof ExtractionPlateTileEntity)
			{
				this.extractFacing.set(((ExtractionPlateTileEntity) te).extractFacing().ordinal());
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

				if (te instanceof ExtractionPlateTileEntity)
				{
					((ExtractionPlateTileEntity) te).rotateExtractFacing();
				}
			});
		}
	}
}
