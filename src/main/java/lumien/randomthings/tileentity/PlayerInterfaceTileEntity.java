package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;



import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Exposes the linked player's inventory as an {@code IItemHandler} capability
 * (hotbar from below, armor from above, offhand from the north, the rest of
 * the main inventory from every other side) so hoppers and similar can pull
 * from / push into a specific online player. No GUI - it links to whoever
 * placed it and works purely through the capability.
 */
public class PlayerInterfaceTileEntity extends BlockEntity
{
	private UUID playerUUID;

	public PlayerInterfaceTileEntity(BlockPos pos, BlockState state) {
        this(ModTileEntityTypes.PLAYER_INTERFACE, pos, state);
    }

	protected PlayerInterfaceTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

	public void setPlayerUUID(UUID uuid)
	{
		this.playerUUID = uuid;
		this.setChanged();
	}

	public UUID getPlayerUUID()
	{
		return playerUUID;
	}

	private Player getLinkedPlayer()
	{
		if (playerUUID == null || this.level == null)
		{
			return null;
		}

		MinecraftServer server = this.level.getServer();
		return server != null ? server.getPlayerList().getPlayer(playerUUID) : null;
	}

	public IItemHandler getItemHandler(@Nullable Direction side)
	{
		{
			Player player = getLinkedPlayer();

			if (player == null)
			{
				return new net.neoforged.neoforge.items.ItemStackHandler(0);
			}

			InvWrapper full = new InvWrapper(new SlotValidatingInventory(player.getInventory()));

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

			return ranged;
		}

	}

	@Override
	protected void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.saveAdditional(compound, registries);

		if (playerUUID != null)
		{
			compound.putString("playerUUID", playerUUID.toString());
		}

	}

	@Override
	public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.loadAdditional(compound, registries);

		if (compound.contains("playerUUID"))
		{
			this.playerUUID = UUID.fromString(compound.getString("playerUUID"));
		}
	}

	/**
	 * Real bug, found 2026-09-28 (reported by user): a chestplate inserted
	 * from the UP side landed in the legs slot instead of the chest slot.
	 * Root cause: {@code InvWrapper(player.getInventory())} wraps the raw {@code
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
	private static class SlotValidatingInventory implements Container
	{
		private final Inventory inventory;

		SlotValidatingInventory(Inventory inventory)
		{
			this.inventory = inventory;
		}

		@Override
		public boolean canPlaceItem(int index, ItemStack stack)
		{
			if (index < 36 || index >= 40)
			{
				return true;
			}

			EquipmentSlot wanted = Mob.getEquipmentSlotForItem(stack);

			return wanted.getType() == EquipmentSlot.Type.ARMOR && wanted.getIndex() == index - 36;
		}

		@Override
		public int getContainerSize()
		{
			return inventory.getContainerSize();
		}

		@Override
		public boolean isEmpty()
		{
			return inventory.isEmpty();
		}

		@Override
		public ItemStack getItem(int index)
		{
			return inventory.getItem(index);
		}

		@Override
		public ItemStack removeItem(int index, int count)
		{
			return inventory.removeItem(index, count);
		}

		@Override
		public ItemStack removeItemNoUpdate(int index)
		{
			return inventory.removeItemNoUpdate(index);
		}

		@Override
		public void setItem(int index, ItemStack stack)
		{
			inventory.setItem(index, stack);
		}

		@Override
		public void setChanged()
		{
			inventory.setChanged();
		}

		@Override
		public boolean stillValid(Player player)
		{
			return inventory.stillValid(player);
		}

		@Override
		public void clearContent()
		{
			inventory.clearContent();
		}
	}
}
