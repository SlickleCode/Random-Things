package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import lumien.randomthings.util.WeakSets;
import java.util.Set;
import java.util.UUID;

import lumien.randomthings.block.GlobalChatDetectorBlock;
import lumien.randomthings.container.GlobalChatDetectorContainer;
import lumien.randomthings.item.IdCardItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Like {@link ChatDetectorTileEntity}, but matches any player's chat instead
 * of only a linked owner's; consuming (hiding) the message additionally
 * requires that player to hold a matching {@link IdCardItem} in this
 * block's 9-slot whitelist, or be a server operator.
 */
public class GlobalChatDetectorTileEntity extends BlockEntity implements MenuProvider
{
	public static final Set<GlobalChatDetectorTileEntity> detectors = WeakSets.create();

	private String chatMessage = "";
	private boolean consume;

	private boolean pulsing;
	private int pulsingCounter;

	private final ItemStackHandler idCardInventory = new ItemStackHandler(9)
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			setChanged();
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack)
		{
			return stack.getItem() instanceof IdCardItem;
		}
	};

	public GlobalChatDetectorTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.GLOBAL_CHAT_DETECTOR, pos, state);
		detectors.add(this);
    }

	public ItemStackHandler idCardInventory()
	{
		return idCardInventory;
	}

	public void tick()
	{
		if (this.level.isClientSide || !pulsing)
		{
			return;
		}

		pulsingCounter--;

		if (pulsingCounter <= 0)
		{
			pulsing = false;
			this.level.setBlock(worldPosition, this.getBlockState().setValue(GlobalChatDetectorBlock.POWERED, false), 3);
		}
	}

	public boolean isPulsing()
	{
		return pulsing;
	}

	public String getChatMessage()
	{
		return chatMessage;
	}

	public void setChatMessage(String chatMessage)
	{
		this.chatMessage = chatMessage;
		this.setChanged();
	}

	public void setConsume(boolean consume)
	{
		this.consume = consume;
		this.setChanged();
	}

	public boolean consume()
	{
		return consume;
	}

	private void pulse()
	{
		pulsing = true;
		pulsingCounter = 20;

		this.level.setBlock(worldPosition, this.getBlockState().setValue(GlobalChatDetectorBlock.POWERED, true), 3);
	}

	/**
	 * @return true if this detector consumed (should hide) the message.
	 */
	public boolean checkMessage(ServerPlayer player, String message)
	{
		if (this.level.isClientSide || !this.chatMessage.equals(message))
		{
			return false;
		}

		pulse();

		if (!consume)
		{
			return false;
		}

		for (int i = 0; i < idCardInventory.getSlots(); i++)
		{
			ItemStack stack = idCardInventory.getStackInSlot(i);

			if (!stack.isEmpty() && stack.getItem() instanceof IdCardItem)
			{
				UUID cardOwner = IdCardItem.getUUID(stack);

				if (cardOwner != null && cardOwner.equals(player.getGameProfile().getId()))
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	protected void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.saveAdditional(compound, registries);

		compound.putBoolean("consume", consume);
		compound.putString("chatMessage", chatMessage);
		compound.putBoolean("pulsing", pulsing);
		compound.putInt("pulsingCounter", pulsingCounter);
		compound.put("inventory", idCardInventory.serializeNBT(registries));

	}

	@Override
	public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.loadAdditional(compound, registries);

		consume = compound.getBoolean("consume");
		chatMessage = compound.getString("chatMessage");
		pulsing = compound.getBoolean("pulsing");
		pulsingCounter = compound.getInt("pulsingCounter");
		idCardInventory.deserializeNBT(registries, compound.getCompound("inventory"));
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new GlobalChatDetectorContainer(windowId, playerInventory, idCardInventory, ContainerLevelAccess.create(this.level, worldPosition), this.chatMessage);
	}

	@Override
	public Component getDisplayName()
	{
		return Component.translatable("block.randomthings.global_chat_detector");
	}
}
