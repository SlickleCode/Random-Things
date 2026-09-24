package lumien.randomthings.tileentity;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

import lumien.randomthings.block.ChatDetectorBlock;
import lumien.randomthings.container.ChatDetectorContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Watches for its linked player to send a specific chat message, then
 * emits a short redstone pulse (and optionally consumes/hides that message).
 */
public class ChatDetectorTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider
{
	public static final Set<ChatDetectorTileEntity> detectors = Collections.newSetFromMap(new WeakHashMap<>());

	private UUID playerUUID;
	private String chatMessage = "";
	private boolean consume;

	private boolean pulsing;
	private int pulsingCounter;

	public ChatDetectorTileEntity()
	{
		super(ModTileEntityTypes.CHAT_DETECTOR);
		detectors.add(this);
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
			this.world.setBlockState(pos, this.getBlockState().with(ChatDetectorBlock.POWERED, false), 3);
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

	public void setPlayerUUID(UUID playerUUID)
	{
		this.playerUUID = playerUUID;
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

		this.world.setBlockState(pos, this.getBlockState().with(ChatDetectorBlock.POWERED, true), 3);
	}

	/**
	 * @return true if this detector consumed (should hide) the message.
	 */
	public boolean checkMessage(ServerPlayerEntity player, String message)
	{
		if (this.world.isRemote)
		{
			return false;
		}

		UUID senderUUID = player.getGameProfile().getId();

		if (senderUUID == null || !senderUUID.equals(this.playerUUID))
		{
			return false;
		}

		if (!this.chatMessage.equals(message))
		{
			return false;
		}

		pulse();
		return consume;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);

		compound.putBoolean("consume", consume);
		compound.putString("chatMessage", chatMessage);

		if (playerUUID != null)
		{
			compound.putString("playerUUID", playerUUID.toString());
		}

		compound.putBoolean("pulsing", pulsing);
		compound.putInt("pulsingCounter", pulsingCounter);

		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);

		consume = compound.getBoolean("consume");
		chatMessage = compound.getString("chatMessage");

		if (compound.contains("playerUUID"))
		{
			playerUUID = UUID.fromString(compound.getString("playerUUID"));
		}

		pulsing = compound.getBoolean("pulsing");
		pulsingCounter = compound.getInt("pulsingCounter");
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new ChatDetectorContainer(windowId, IWorldPosCallable.of(this.world, pos), this.chatMessage);
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.chat_detector");
	}
}
