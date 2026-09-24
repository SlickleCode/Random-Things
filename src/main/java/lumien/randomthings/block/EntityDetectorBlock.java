package lumien.randomthings.block;

import lumien.randomthings.tileentity.EntityDetectorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Emits redstone when an entity matching a configurable filter is within a
 * configurable radius.
 */
public class EntityDetectorBlock extends Block
{
	public EntityDetectorBlock()
	{
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5F));
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new EntityDetectorTileEntity();
	}

	@Override
	public boolean canProvidePower(BlockState state)
	{
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
	{
		TileEntity te = blockAccess.getTileEntity(pos);
		return te instanceof EntityDetectorTileEntity && ((EntityDetectorTileEntity) te).isPowered() ? 15 : 0;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
	{
		TileEntity te = blockAccess.getTileEntity(pos);
		return te instanceof EntityDetectorTileEntity && ((EntityDetectorTileEntity) te).isPowered() && ((EntityDetectorTileEntity) te).strongOutput() ? 15 : 0;
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof EntityDetectorTileEntity)
			{
				InventoryHelper.dropItems(worldIn, pos, singleStackList((EntityDetectorTileEntity) te));
			}
		}

		super.onReplaced(state, worldIn, pos, newState, isMoving);
	}

	private static NonNullList<ItemStack> singleStackList(EntityDetectorTileEntity te)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		list.add(te.filterInventory().getStackInSlot(0));
		return list;
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (!worldIn.isRemote)
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof EntityDetectorTileEntity)
			{
				NetworkHooks.openGui((ServerPlayerEntity) player, (EntityDetectorTileEntity) te);
			}
		}

		return true;
	}
}
