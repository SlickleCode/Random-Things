package lumien.randomthings.tileentity;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.block.OnlineDetectorBlock;
import lumien.randomthings.container.OnlineDetectorContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class OnlineDetectorTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider
{
	private String username = "";
	private boolean playerOnline = false;

	public OnlineDetectorTileEntity()
	{
		super(ModTileEntityTypes.ONLINE_DETECTOR);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		compound.putString("username", username);
		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		this.username = compound.getString("username");
	}

	public String getUsername()
	{
		return username;
	}

	public void setUsername(String username)
	{
		this.username = username;
		this.markDirty();
	}

	@Override
	public void tick()
	{
		if (!this.world.isRemote && this.world.getGameTime() % 20 == 0)
		{
			MinecraftServer server = this.world.getServer();

			boolean playerCheck = server != null && !username.isEmpty() && server.getPlayerList().getPlayerByUsername(username) != null;

			if (playerCheck != playerOnline)
			{
				this.playerOnline = playerCheck;
				this.world.setBlockState(pos, this.getBlockState().with(OnlineDetectorBlock.POWERED, playerCheck), 3);
			}
		}
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new OnlineDetectorContainer(windowId, IWorldPosCallable.of(this.world, pos), this.username);
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.online_detector");
	}
}
