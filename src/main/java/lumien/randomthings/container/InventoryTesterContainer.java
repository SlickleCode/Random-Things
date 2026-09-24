package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.InventoryTesterTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

/**
 * InventoryTesterContainer
 */
public class InventoryTesterContainer extends Container implements ISignalContainer
{
	IWorldPosCallable pos;

	public IntReferenceHolder invertSignal = IntReferenceHolder.single();

	public InventoryTesterContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, (PlayerInventory) playerInventory, new ItemStackHandler(1), IWorldPosCallable.DUMMY);
	}

	public InventoryTesterContainer(int windowId, PlayerInventory playerInventory, IItemHandler itemHandler, IWorldPosCallable pos)
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

		this.trackInt(invertSignal);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.INVENTORY_TESTER);
	}

	@Override
	public void detectAndSendChanges()
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

			if (te instanceof InventoryTesterTileEntity)
			{
				this.invertSignal.set(((InventoryTesterTileEntity) te).invertSignal() ? 1 : 0);
			}
		});

		super.detectAndSendChanges();
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public void handle(int id, PacketBuffer data)
	{
		if (id == 0)
		{
			this.pos.consume((world, pos) -> {
				TileEntity te = world.getTileEntity(pos);

				if (te instanceof InventoryTesterTileEntity)
				{
					((InventoryTesterTileEntity) te).toggleInvert();
				}
			});
		}
	}
}
