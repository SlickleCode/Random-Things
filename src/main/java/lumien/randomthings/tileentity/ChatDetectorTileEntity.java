package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import lumien.randomthings.util.WeakSets;
import java.util.Set;
import java.util.UUID;

import lumien.randomthings.block.ChatDetectorBlock;
import lumien.randomthings.container.ChatDetectorContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Watches for its linked player to send a specific chat message, then
 * emits a short redstone pulse (and optionally consumes/hides that message).
 */
public class ChatDetectorTileEntity extends BlockEntity implements MenuProvider
{
	public static final Set<ChatDetectorTileEntity> detectors = WeakSets.create();

	private UUID playerUUID;
	private String chatMessage = "";
	private boolean consume;

	private boolean pulsing;
	private int pulsingCounter;

	public ChatDetectorTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.CHAT_DETECTOR, pos, state);
		detectors.add(this);
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
			this.level.setBlock(worldPosition, this.getBlockState().setValue(ChatDetectorBlock.POWERED, false), 3);
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

	public void setPlayerUUID(UUID playerUUID)
	{
		this.playerUUID = playerUUID;
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

		this.level.setBlock(worldPosition, this.getBlockState().setValue(ChatDetectorBlock.POWERED, true), 3);
	}

	/**
	 * @return true if this detector consumed (should hide) the message.
	 */
	public boolean checkMessage(ServerPlayer player, String message)
	{
		if (this.level.isClientSide)
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
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);

		compound.putBoolean("consume", consume);
		compound.putString("chatMessage", chatMessage);

		if (playerUUID != null)
		{
			compound.putString("playerUUID", playerUUID.toString());
		}

		compound.putBoolean("pulsing", pulsing);
		compound.putInt("pulsingCounter", pulsingCounter);

	}

	@Override
	public void load(CompoundTag compound)
	{
		super.load(compound);

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
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new ChatDetectorContainer(windowId, ContainerLevelAccess.create(this.level, worldPosition), this.chatMessage);
	}

	@Override
	public Component getDisplayName()
	{
		return Component.translatable("block.randomthings.chat_detector");
	}
}
