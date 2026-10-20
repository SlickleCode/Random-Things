package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import java.util.UUID;

import lumien.randomthings.container.NotificationInterfaceContainer;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.network.messages.NotificationMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.network.chat.Component;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Links to whoever placed it and pops a toast on their screen (title,
 * description, and an icon item from its one slot) whenever it sees a
 * redstone rising edge.
 */
public class NotificationInterfaceTileEntity extends BlockEntity implements MenuProvider
{
	private UUID owner;

	private String title = "Title";
	private String description = "Description";

	private boolean powered;

	private final ItemStackHandler iconInventory = new ItemStackHandler(1)
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			setChanged();
		}
	};

	public NotificationInterfaceTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.NOTIFICATION_INTERFACE, pos, state);
    }

	public ItemStackHandler iconInventory()
	{
		return iconInventory;
	}

	public String getTitle()
	{
		return title;
	}

	public String getDescription()
	{
		return description;
	}

	public void setData(String title, String description)
	{
		this.title = title;
		this.description = description;
		this.setChanged();
	}

	public void setPlayerUUID(UUID uuid)
	{
		this.owner = uuid;
		this.setChanged();
	}

	public void updatePowerState(boolean newPowered)
	{
		boolean old = this.powered;
		this.powered = newPowered;

		if (!old && newPowered)
		{
			notifyOwner();
		}
	}

	private void notifyOwner()
	{
		if (owner == null || this.level == null)
		{
			return;
		}

		MinecraftServer server = this.level.getServer();

		if (server == null)
		{
			return;
		}

		ServerPlayer player = server.getPlayerList().getPlayer(owner);

		if (player != null)
		{
			RTPacketHandler.sendTo(new NotificationMessage(title, description, iconInventory.getStackInSlot(0)), player);
		}
	}

	@Override
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);

		compound.putString("title", title);
		compound.putString("description", description);

		if (owner != null)
		{
			compound.putString("owner", owner.toString());
		}

		compound.put("inventory", iconInventory.serializeNBT());

	}

	@Override
	public void load(CompoundTag compound)
	{
		super.load(compound);

		title = compound.getString("title");
		description = compound.getString("description");

		if (compound.contains("owner"))
		{
			owner = UUID.fromString(compound.getString("owner"));
		}

		iconInventory.deserializeNBT(compound.getCompound("inventory"));
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new NotificationInterfaceContainer(windowId, playerInventory, iconInventory, ContainerLevelAccess.create(this.level, worldPosition), title, description);
	}

	@Override
	public Component getDisplayName()
	{
		return Component.translatable("block.randomthings.notification_interface");
	}
}
