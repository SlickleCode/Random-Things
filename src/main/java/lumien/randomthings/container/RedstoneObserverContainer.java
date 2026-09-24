package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.RedstoneObserverTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;

/**
 * Read-only status display - the actual target is set by right-clicking
 * with a Redstone Tool, not through this GUI.
 */
public class RedstoneObserverContainer extends Container
{
	IWorldPosCallable pos;

	public IntReferenceHolder hasTarget = IntReferenceHolder.single();
	public IntReferenceHolder targetX = IntReferenceHolder.single();
	public IntReferenceHolder targetY = IntReferenceHolder.single();
	public IntReferenceHolder targetZ = IntReferenceHolder.single();

	public RedstoneObserverContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, IWorldPosCallable.DUMMY);
	}

	public RedstoneObserverContainer(int windowId, IWorldPosCallable pos)
	{
		super(ModContainerTypes.REDSTONE_OBSERVER, windowId);

		this.pos = pos;

		this.trackInt(hasTarget);
		this.trackInt(targetX);
		this.trackInt(targetY);
		this.trackInt(targetZ);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.REDSTONE_OBSERVER);
	}

	@Override
	public void detectAndSendChanges()
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

			if (te instanceof RedstoneObserverTileEntity)
			{
				BlockPos target = ((RedstoneObserverTileEntity) te).getTarget();

				this.hasTarget.set(target != null ? 1 : 0);
				this.targetX.set(target != null ? target.getX() : 0);
				this.targetY.set(target != null ? target.getY() : 0);
				this.targetZ.set(target != null ? target.getZ() : 0);
			}
		});

		super.detectAndSendChanges();
	}
}
