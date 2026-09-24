package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.IronDropperTileEntity;
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
 * IronDropperContainer
 */
public class IronDropperContainer extends Container implements ISignalContainer
{
	IWorldPosCallable pos;
	IItemHandler itemHandler;

	public IntReferenceHolder redstoneMode = IntReferenceHolder.single();
	public IntReferenceHolder pickupDelay = IntReferenceHolder.single();
	public IntReferenceHolder effects = IntReferenceHolder.single();
	public IntReferenceHolder randomMotion = IntReferenceHolder.single();

	public IronDropperContainer(int windowId, IInventory playerInventory, PacketBuffer extraData)
	{
		this(windowId, (PlayerInventory) playerInventory, new ItemStackHandler(9), IWorldPosCallable.DUMMY);
	}

	public IronDropperContainer(int windowId, PlayerInventory playerInventory, IItemHandler itemHandler, IWorldPosCallable pos)
	{
		super(ModContainerTypes.IRON_DROPPER, windowId);

		this.pos = pos;
		this.itemHandler = itemHandler;

		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				this.addSlot(new SlotItemHandler(itemHandler, j + i * 3, 62 + j * 18, 17 + i * 18));
			}
		}

		for (int row = 0; row < 3; row++)
		{
			for (int col = 0; col < 9; col++)
			{
				this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
			}
		}

		for (int col = 0; col < 9; col++)
		{
			this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
		}

		this.trackInt(redstoneMode);
		this.trackInt(pickupDelay);
		this.trackInt(effects);
		this.trackInt(randomMotion);
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return isWithinUsableDistance(this.pos, playerIn, ModBlocks.IRON_DROPPER);
	}

	@Override
	public void detectAndSendChanges()
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

			if (te instanceof IronDropperTileEntity)
			{
				IronDropperTileEntity idte = (IronDropperTileEntity) te;

				this.redstoneMode.set(idte.redstoneMode().ordinal());
				this.pickupDelay.set(idte.pickupDelay().ordinal());
				this.effects.set(idte.effects().ordinal());
				this.randomMotion.set(idte.randomMotion() ? 1 : 0);
			}
		});

		super.detectAndSendChanges();
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
	{
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);

		if (slot != null && slot.getHasStack())
		{
			ItemStack stackInSlot = slot.getStack();
			result = stackInSlot.copy();

			if (index < 9)
			{
				if (!this.mergeItemStack(stackInSlot, 9, 45, true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.mergeItemStack(stackInSlot, 0, 9, false))
			{
				return ItemStack.EMPTY;
			}

			if (stackInSlot.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}

			if (stackInSlot.getCount() == result.getCount())
			{
				return ItemStack.EMPTY;
			}

			slot.onTake(playerIn, stackInSlot);
		}

		return result;
	}

	@Override
	public void handle(int id, PacketBuffer data)
	{
		this.pos.consume((world, pos) -> {
			TileEntity te = world.getTileEntity(pos);

			if (!(te instanceof IronDropperTileEntity))
			{
				return;
			}

			IronDropperTileEntity idte = (IronDropperTileEntity) te;

			switch (id)
			{
				case 0:
					idte.rotateRedstoneMode();
					break;
				case 1:
					idte.rotatePickupDelay();
					break;
				case 2:
					idte.rotateRandomMotion();
					break;
				case 3:
					idte.rotateEffects();
					break;
			}
		});
	}
}
