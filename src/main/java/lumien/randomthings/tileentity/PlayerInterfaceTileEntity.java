package lumien.randomthings.tileentity;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.EmptyHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.RangedWrapper;

/**
 * Exposes the linked player's inventory as an {@code IItemHandler} capability
 * (hotbar from below, armor from above, offhand from the north, the rest of
 * the main inventory from every other side) so hoppers and similar can pull
 * from / push into a specific online player. No GUI - it links to whoever
 * placed it and works purely through the capability.
 */
public class PlayerInterfaceTileEntity extends TileEntity
{
	private UUID playerUUID;

	public PlayerInterfaceTileEntity()
	{
		this(ModTileEntityTypes.PLAYER_INTERFACE);
	}

	protected PlayerInterfaceTileEntity(TileEntityType<?> type)
	{
		super(type);
	}

	public void setPlayerUUID(UUID uuid)
	{
		this.playerUUID = uuid;
		this.markDirty();
	}

	public UUID getPlayerUUID()
	{
		return playerUUID;
	}

	private PlayerEntity getLinkedPlayer()
	{
		if (playerUUID == null || this.world == null)
		{
			return null;
		}

		MinecraftServer server = this.world.getServer();
		return server != null ? server.getPlayerList().getPlayerByUUID(playerUUID) : null;
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side)
	{
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			PlayerEntity player = getLinkedPlayer();

			if (player == null)
			{
				return LazyOptional.of(() -> (IItemHandler) EmptyHandler.INSTANCE).cast();
			}

			InvWrapper full = new InvWrapper(new SlotValidatingInventory(player.inventory));

			IItemHandler ranged;
			if (side == Direction.UP)
			{
				ranged = new RangedWrapper(full, 36, 40);
			}
			else if (side == Direction.DOWN)
			{
				ranged = new RangedWrapper(full, 0, 9);
			}
			else if (side == Direction.NORTH)
			{
				ranged = new RangedWrapper(full, 40, 41);
			}
			else
			{
				ranged = new RangedWrapper(full, 9, 36);
			}

			return LazyOptional.of(() -> ranged).cast();
		}

		return super.getCapability(cap, side);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);

		if (playerUUID != null)
		{
			compound.putString("playerUUID", playerUUID.toString());
		}

		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);

		if (compound.contains("playerUUID"))
		{
			this.playerUUID = UUID.fromString(compound.getString("playerUUID"));
		}
	}

	/**
	 * Real bug, found 2026-09-28 (reported by user): a chestplate inserted
	 * from the UP side landed in the legs slot instead of the chest slot.
	 * Root cause: {@code InvWrapper(player.inventory)} wraps the raw {@code
	 * PlayerInventory} directly, whose {@code isItemValidForSlot} is never
	 * overridden - so it falls back to {@code IInventory}'s own default,
	 * which unconditionally returns {@code true} for every slot (confirmed
	 * via source). Without that restriction, {@code RangedWrapper}'s {@code
	 * insertItem} just fills the first empty slot in the 36-39 armor range
	 * regardless of item type. 1.12.2's own {@code TileEntityPlayerInterface}
	 * avoided this by wrapping the inventory in its own {@code IInventory}
	 * that delegated {@code isItemValidForSlot} to the real {@code
	 * ContainerPlayer}'s per-slot {@code Slot.isItemValid} - this is this
	 * version's equivalent, computing the same restriction directly via
	 * {@code MobEntity.getSlotForItemStack} instead of needing a Container at
	 * all.
	 */
	private static class SlotValidatingInventory implements IInventory
	{
		private final PlayerInventory inventory;

		SlotValidatingInventory(PlayerInventory inventory)
		{
			this.inventory = inventory;
		}

		@Override
		public boolean isItemValidForSlot(int index, ItemStack stack)
		{
			if (index < 36 || index >= 40)
			{
				return true;
			}

			EquipmentSlotType wanted = MobEntity.getSlotForItemStack(stack);

			return wanted.getSlotType() == EquipmentSlotType.Group.ARMOR && wanted.getIndex() == index - 36;
		}

		@Override
		public int getSizeInventory()
		{
			return inventory.getSizeInventory();
		}

		@Override
		public boolean isEmpty()
		{
			return inventory.isEmpty();
		}

		@Override
		public ItemStack getStackInSlot(int index)
		{
			return inventory.getStackInSlot(index);
		}

		@Override
		public ItemStack decrStackSize(int index, int count)
		{
			return inventory.decrStackSize(index, count);
		}

		@Override
		public ItemStack removeStackFromSlot(int index)
		{
			return inventory.removeStackFromSlot(index);
		}

		@Override
		public void setInventorySlotContents(int index, ItemStack stack)
		{
			inventory.setInventorySlotContents(index, stack);
		}

		@Override
		public void markDirty()
		{
			inventory.markDirty();
		}

		@Override
		public boolean isUsableByPlayer(PlayerEntity player)
		{
			return inventory.isUsableByPlayer(player);
		}

		@Override
		public void clear()
		{
			inventory.clear();
		}
	}
}
