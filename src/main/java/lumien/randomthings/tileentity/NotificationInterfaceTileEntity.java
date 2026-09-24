package lumien.randomthings.tileentity;

import java.util.UUID;

import lumien.randomthings.container.NotificationInterfaceContainer;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.network.messages.NotificationMessage;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Links to whoever placed it and pops a toast on their screen (title,
 * description, and an icon item from its one slot) whenever it sees a
 * redstone rising edge.
 */
public class NotificationInterfaceTileEntity extends TileEntity implements INamedContainerProvider
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
			markDirty();
		}
	};

	public NotificationInterfaceTileEntity()
	{
		super(ModTileEntityTypes.NOTIFICATION_INTERFACE);
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
		this.markDirty();
	}

	public void setPlayerUUID(UUID uuid)
	{
		this.owner = uuid;
		this.markDirty();
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
		if (owner == null || this.world == null)
		{
			return;
		}

		MinecraftServer server = this.world.getServer();

		if (server == null)
		{
			return;
		}

		ServerPlayerEntity player = server.getPlayerList().getPlayerByUUID(owner);

		if (player != null)
		{
			RTPacketHandler.sendTo(new NotificationMessage(title, description, iconInventory.getStackInSlot(0)), player);
		}
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);

		compound.putString("title", title);
		compound.putString("description", description);

		if (owner != null)
		{
			compound.putString("owner", owner.toString());
		}

		compound.put("inventory", iconInventory.serializeNBT());

		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);

		title = compound.getString("title");
		description = compound.getString("description");

		if (compound.contains("owner"))
		{
			owner = UUID.fromString(compound.getString("owner"));
		}

		iconInventory.deserializeNBT(compound.getCompound("inventory"));
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new NotificationInterfaceContainer(windowId, playerInventory, iconInventory, IWorldPosCallable.of(this.world, pos), title, description);
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.notification_interface");
	}
}
