package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.block.OnlineDetectorBlock;
import lumien.randomthings.container.OnlineDetectorContainer;
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
import net.minecraft.world.level.block.state.BlockState;

public class OnlineDetectorTileEntity extends BlockEntity implements MenuProvider
{
	private String username = "";
	private boolean playerOnline = false;

	public OnlineDetectorTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.ONLINE_DETECTOR, pos, state);
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
		compound.putString("username", username);
	}

	@Override
	protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
	{
		super.loadAdditional(rtIn);
		rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
	}

	private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		this.username = compound.getStringOr("username", "");
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
		this.setChanged();
	}

	public void tick()
	{
		if (!this.level.isClientSide() && this.level.getGameTime() % 20 == 0)
		{
			MinecraftServer server = this.level.getServer();

			boolean playerCheck = false;

			if (server != null && !username.isEmpty())
			{
				// getPlayerByName ignores case, so confirm the exact (case sensitive) name afterwards
				ServerPlayer found = server.getPlayerList().getPlayerByName(username);
				playerCheck = found != null && found.getGameProfile().name().equals(username);
			}

			if (playerCheck != playerOnline)
			{
				this.playerOnline = playerCheck;
				this.level.setBlock(worldPosition, this.getBlockState().setValue(OnlineDetectorBlock.POWERED, playerCheck), 3);
			}
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new OnlineDetectorContainer(windowId, ContainerLevelAccess.create(this.level, worldPosition), this.username);
	}

	@Override
	public Component getDisplayName()
	{
		return Component.translatable("block.randomthings.online_detector");
	}
}
