package lumien.randomthings.tileentity;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
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

			InvWrapper full = new InvWrapper(player.inventory);

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
}
