package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.InventoryTesterTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * InventoryTesterContainer
 */
public class InventoryTesterContainer extends AbstractContainerMenu implements ISignalContainer
{
	ContainerLevelAccess pos;

	public DataSlot invertSignal = DataSlot.standalone();

	public InventoryTesterContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData)
	{
		this(windowId, (Inventory) playerInventory, new ItemStackHandler(1), ContainerLevelAccess.NULL);
	}

	public InventoryTesterContainer(int windowId, Inventory playerInventory, IItemHandler itemHandler, ContainerLevelAccess pos)
	{
		super(ModContainerTypes.INVENTORY_TESTER, windowId);

		this.pos = pos;

		this.addSlot(new SlotItemHandler(itemHandler, 0, 64, 18));

		for (int row = 0; row < 3; row++)
		{
			for (int col = 0; col < 9; col++)
			{
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 54 + row * 18));
			}
		}

		for (int col = 0; col < 9; col++)
		{
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 112));
		}

		this.addDataSlot(invertSignal);
	}

	@Override
	public boolean stillValid(Player playerIn)
	{
		return stillValid(this.pos, playerIn, ModBlocks.INVENTORY_TESTER);
	}

	@Override
	public void broadcastChanges()
	{
		this.pos.execute((world, pos) -> {
			BlockEntity te = world.getBlockEntity(pos);

			if (te instanceof InventoryTesterTileEntity)
			{
				this.invertSignal.set(((InventoryTesterTileEntity) te).invertSignal() ? 1 : 0);
			}
		});

		super.broadcastChanges();
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public void handle(int id, FriendlyByteBuf data)
	{
		if (id == 0)
		{
			this.pos.execute((world, pos) -> {
				BlockEntity te = world.getBlockEntity(pos);

				if (te instanceof InventoryTesterTileEntity)
				{
					((InventoryTesterTileEntity) te).toggleInvert();
				}
			});
		}
	}
}
