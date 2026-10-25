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
import lumien.randomthings.itemhandler.ItemStackHandler;
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
	protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput rtOut)
	{
		super.saveAdditional(rtOut);
		CompoundTag rtTag = new CompoundTag();
		rtSaveAdditional(rtTag, lumien.randomthings.util.StackTag.provider());
		lumien.randomthings.util.TagIO.write(rtOut, rtTag);
	}

	private void rtSaveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{

		compound.putString("title", title);
		compound.putString("description", description);

		if (owner != null)
		{
			compound.putString("owner", owner.toString());
		}

		compound.put("inventory", iconInventory.serializeNBT(registries));

	}

	@Override
	protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
	{
		super.loadAdditional(rtIn);
		rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
	}

	private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{

		title = compound.getStringOr("title", "");
		description = compound.getStringOr("description", "");

		if (compound.contains("owner"))
		{
			owner = UUID.fromString(compound.getStringOr("owner", ""));
		}

		iconInventory.deserializeNBT(registries, compound.getCompoundOrEmpty("inventory"));
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

	@Override
	public void preRemoveSideEffects(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state)
	{
		super.preRemoveSideEffects(pos, state);

		// 1.21.5+: Block.onRemove is gone and the tile entity is already removed when the neighbour callback runs, so the block's old onRemove code runs from here
		if (this.level != null && state.getBlock() instanceof lumien.randomthings.block.NotificationInterfaceBlock block)
		{
			block.rtOnRemove(state, this.level, pos, this.level.getBlockState(pos), false);
		}
	}
}
