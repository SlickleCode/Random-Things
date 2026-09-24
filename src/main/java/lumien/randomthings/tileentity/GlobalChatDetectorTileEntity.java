package lumien.randomthings.tileentity;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import lumien.randomthings.block.GlobalChatDetectorBlock;
import lumien.randomthings.container.GlobalChatDetectorContainer;
import lumien.randomthings.item.IdCardItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Like {@link ChatDetectorTileEntity}, but matches any player's chat instead
 * of only a linked owner's; consuming (hiding) the message additionally
 * requires that player to hold a matching {@link IdCardItem} in this
 * block's 9-slot whitelist, or be a server operator.
 */
public class GlobalChatDetectorTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider
{
	public static final Set<GlobalChatDetectorTileEntity> detectors = Collections.newSetFromMap(new WeakHashMap<>());

	private String chatMessage = "";
	private boolean consume;

	private boolean pulsing;
	private int pulsingCounter;

	private final ItemStackHandler idCardInventory = new ItemStackHandler(9)
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			markDirty();
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack)
		{
			return stack.getItem() instanceof IdCardItem;
		}
	};

	public GlobalChatDetectorTileEntity()
	{
		super(ModTileEntityTypes.GLOBAL_CHAT_DETECTOR);
		detectors.add(this);
	}

	public ItemStackHandler idCardInventory()
	{
		return idCardInventory;
	}

	@Override
	public void tick()
	{
		if (this.world.isRemote || !pulsing)
		{
			return;
		}

		pulsingCounter--;

		if (pulsingCounter <= 0)
		{
			pulsing = false;
			this.world.setBlockState(pos, this.getBlockState().with(GlobalChatDetectorBlock.POWERED, false), 3);
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
		this.markDirty();
	}

	public void setConsume(boolean consume)
	{
		this.consume = consume;
		this.markDirty();
	}

	public boolean consume()
	{
		return consume;
	}

	private void pulse()
	{
		pulsing = true;
		pulsingCounter = 20;

		this.world.setBlockState(pos, this.getBlockState().with(GlobalChatDetectorBlock.POWERED, true), 3);
	}

	/**
	 * @return true if this detector consumed (should hide) the message.
	 */
	public boolean checkMessage(ServerPlayerEntity player, String message)
	{
		if (this.world.isRemote || !this.chatMessage.equals(message))
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

		return player.hasPermissionLevel(2);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);

		compound.putBoolean("consume", consume);
		compound.putString("chatMessage", chatMessage);
		compound.putBoolean("pulsing", pulsing);
		compound.putInt("pulsingCounter", pulsingCounter);
		compound.put("inventory", idCardInventory.serializeNBT());

		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);

		consume = compound.getBoolean("consume");
		chatMessage = compound.getString("chatMessage");
		pulsing = compound.getBoolean("pulsing");
		pulsingCounter = compound.getInt("pulsingCounter");
		idCardInventory.deserializeNBT(compound.getCompound("inventory"));
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new GlobalChatDetectorContainer(windowId, playerInventory, idCardInventory, IWorldPosCallable.of(this.world, pos), this.chatMessage);
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.global_chat_detector");
	}
}
