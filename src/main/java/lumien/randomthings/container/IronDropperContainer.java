package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.IronDropperTileEntity;
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
 * IronDropperContainer
 */
public class IronDropperContainer extends AbstractContainerMenu implements ISignalContainer
{
	ContainerLevelAccess pos;
	IItemHandler itemHandler;

	public DataSlot redstoneMode = DataSlot.standalone();
	public DataSlot pickupDelay = DataSlot.standalone();
	public DataSlot effects = DataSlot.standalone();
	public DataSlot randomMotion = DataSlot.standalone();

	public IronDropperContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData)
	{
		this(windowId, (Inventory) playerInventory, new ItemStackHandler(9), ContainerLevelAccess.NULL);
	}

	public IronDropperContainer(int windowId, Inventory playerInventory, IItemHandler itemHandler, ContainerLevelAccess pos)
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

		this.addDataSlot(redstoneMode);
		this.addDataSlot(pickupDelay);
		this.addDataSlot(effects);
		this.addDataSlot(randomMotion);
	}

	@Override
	public boolean stillValid(Player playerIn)
	{
		return stillValid(this.pos, playerIn, ModBlocks.IRON_DROPPER);
	}

	@Override
	public void broadcastChanges()
	{
		this.pos.execute((world, pos) -> {
			BlockEntity te = world.getBlockEntity(pos);

			if (te instanceof IronDropperTileEntity)
			{
				IronDropperTileEntity idte = (IronDropperTileEntity) te;

				this.redstoneMode.set(idte.redstoneMode().ordinal());
				this.pickupDelay.set(idte.pickupDelay().ordinal());
				this.effects.set(idte.effects().ordinal());
				this.randomMotion.set(idte.randomMotion() ? 1 : 0);
			}
		});

		super.broadcastChanges();
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index)
	{
		ItemStack result = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);

		if (slot != null && slot.hasItem())
		{
			ItemStack stackInSlot = slot.getItem();
			result = stackInSlot.copy();

			if (index < 9)
			{
				if (!this.moveItemStackTo(stackInSlot, 9, 45, true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if (!this.moveItemStackTo(stackInSlot, 0, 9, false))
			{
				return ItemStack.EMPTY;
			}

			if (stackInSlot.isEmpty())
			{
				slot.set(ItemStack.EMPTY);
			}
			else
			{
				slot.setChanged();
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
	public void handle(int id, FriendlyByteBuf data)
	{
		this.pos.execute((world, pos) -> {
			BlockEntity te = world.getBlockEntity(pos);

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
