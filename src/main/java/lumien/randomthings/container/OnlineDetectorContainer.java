package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.OnlineDetectorTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;

/**
 * OnlineDetectorContainer
 */
public class OnlineDetectorContainer extends Container implements ISignalContainer
{
	IWorldPosCallable pos;
	String username;

	public OnlineDetectorContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, resolvePos(playerInventory, extraData.readBlockPos()), extraData.readString());
	}

	public OnlineDetectorContainer(int windowId, IWorldPosCallable pos, String username)
	{
		super(ModContainerTypes.ONLINE_DETECTOR, windowId);

		this.pos = pos;
		this.username = username;
	}

	private static IWorldPosCallable resolvePos(IInventory playerInventory, BlockPos blockPos)
	{
		net.minecraft.entity.player.PlayerInventory inv = (net.minecraft.entity.player.PlayerInventory) playerInventory;
		return IWorldPosCallable.of(inv.player.world, blockPos);
	}

	public String getUsername()
	{
		return username;
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.ONLINE_DETECTOR);
	}

	@Override
	public void handle(int id, PacketBuffer data)
	{
		if (id == 0)
		{
			String newUsername = data.readString();
			this.username = newUsername;

			this.pos.consume((world, pos) -> {
				TileEntity te = world.getTileEntity(pos);

				if (te instanceof OnlineDetectorTileEntity)
				{
					((OnlineDetectorTileEntity) te).setUsername(newUsername);
				}
			});
		}
	}
}
