package lumien.randomthings.block;

import lumien.randomthings.tileentity.NotificationInterfaceTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Links to whoever placed it and pops a toast on their screen whenever it
 * sees a rising redstone edge.
 */
public class NotificationInterfaceBlock extends Block
{
	public NotificationInterfaceBlock()
	{
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(2.0F));
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new NotificationInterfaceTileEntity();
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if (!worldIn.isRemote && placer instanceof ServerPlayerEntity)
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof NotificationInterfaceTileEntity)
			{
				((NotificationInterfaceTileEntity) te).setPlayerUUID(placer.getUniqueID());
			}
		}
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
	{
		TileEntity te = worldIn.getTileEntity(pos);

		if (te instanceof NotificationInterfaceTileEntity)
		{
			((NotificationInterfaceTileEntity) te).updatePowerState(worldIn.isBlockPowered(pos));
		}
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof NotificationInterfaceTileEntity)
			{
				NonNullList<ItemStack> drops = NonNullList.create();
				drops.add(((NotificationInterfaceTileEntity) te).iconInventory().getStackInSlot(0));
				InventoryHelper.dropItems(worldIn, pos, drops);
			}
		}

		super.onReplaced(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (!worldIn.isRemote)
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof NotificationInterfaceTileEntity)
			{
				NotificationInterfaceTileEntity nite = (NotificationInterfaceTileEntity) te;

				NetworkHooks.openGui((ServerPlayerEntity) player, nite, buf -> {
					buf.writeBlockPos(pos);
					buf.writeString(nite.getTitle());
					buf.writeString(nite.getDescription());
				});
			}
		}

		return true;
	}
}
