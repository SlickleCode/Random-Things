package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.AdvancedRedstoneTorchTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;

/**
 * AdvancedRedstoneTorchContainer
 */
public class AdvancedRedstoneTorchContainer extends AbstractContainerMenu implements ISignalContainer
{
	ContainerLevelAccess pos;

	public DataSlot strengthGreen = DataSlot.standalone();
	public DataSlot strengthRed = DataSlot.standalone();

	AdvancedRedstoneTorchTileEntity te;

	public AdvancedRedstoneTorchContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData)
	{
		this(windowId, ContainerLevelAccess.NULL);
	}

	public AdvancedRedstoneTorchContainer(int windowId, ContainerLevelAccess pos)
	{
		super(ModContainerTypes.ADVANCED_REDSTONE_TORCH, windowId);

		this.pos = pos;

		this.addDataSlot(strengthGreen);
		this.addDataSlot(strengthRed);
	}

	@Override
	public boolean stillValid(Player playerIn)
	{
		return stillValid(this.pos, playerIn, ModBlocks.ADVANCED_REDSTONE_TORCH) || stillValid(this.pos, playerIn, ModBlocks.ADVANCED_WALL_REDSTONE_TORCH);
	}

	@Override
	public void broadcastChanges()
	{
		this.pos.execute((world, pos) -> {
			BlockEntity te = world.getBlockEntity(pos);

			if (te instanceof AdvancedRedstoneTorchTileEntity)
			{
				AdvancedRedstoneTorchTileEntity art = (AdvancedRedstoneTorchTileEntity) te;

				this.strengthGreen.set(art.signalStrengthGreen());
				this.strengthRed.set(art.signalStrengthRed());
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

				if (te instanceof AdvancedRedstoneTorchTileEntity)
				{
					AdvancedRedstoneTorchTileEntity art = (AdvancedRedstoneTorchTileEntity) te;

					switch (action)
					{
						case 0: // Decrease Green Strength
							art.setSignalStrengthGreen(Math.max(0, art.signalStrengthGreen() - 1));
							break;
						case 1: // Increase Green Strength
							art.setSignalStrengthGreen(Math.min(15, art.signalStrengthGreen() + 1));
							break;
						case 2: // Decrease Red Strength
							art.setSignalStrengthRed(Math.max(0, art.signalStrengthRed() - 1));
							break;
						case 3: // Increase Red Strength
							art.setSignalStrengthRed(Math.min(15, art.signalStrengthRed() + 1));
							break;
					}

					art.getLevel().updateNeighborsAt(art.getBlockPos(), art.getBlockState().getBlock());
				}
			});
		}
	}


    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}