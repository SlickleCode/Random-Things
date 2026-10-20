package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.OnlineDetectorTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.core.BlockPos;

/**
 * OnlineDetectorContainer
 */
public class OnlineDetectorContainer extends AbstractContainerMenu implements ISignalContainer
{
	ContainerLevelAccess pos;
	String username;

	public OnlineDetectorContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData)
	{
		this(windowId, resolvePos(playerInventory, extraData.readBlockPos()), extraData.readUtf(32767));
	}

	public OnlineDetectorContainer(int windowId, ContainerLevelAccess pos, String username)
	{
		super(ModContainerTypes.ONLINE_DETECTOR, windowId);

		this.pos = pos;
		this.username = username;
	}

	private static ContainerLevelAccess resolvePos(Container playerInventory, BlockPos blockPos)
	{
		net.minecraft.world.entity.player.Inventory inv = (net.minecraft.world.entity.player.Inventory) playerInventory;
		return ContainerLevelAccess.create(inv.player.level(), blockPos);
	}

	public String getUsername()
	{
		return username;
	}

	@Override
	public boolean stillValid(Player playerIn)
	{
		return stillValid(this.pos, playerIn, ModBlocks.ONLINE_DETECTOR);
	}

	@Override
	public void handle(int id, FriendlyByteBuf data)
	{
		if (id == 0)
		{
			String newUsername = data.readUtf(32767);
			this.username = newUsername;

			this.pos.execute((world, pos) -> {
				BlockEntity te = world.getBlockEntity(pos);

				if (te instanceof OnlineDetectorTileEntity)
				{
					((OnlineDetectorTileEntity) te).setUsername(newUsername);
				}
			});
		}
	}

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
