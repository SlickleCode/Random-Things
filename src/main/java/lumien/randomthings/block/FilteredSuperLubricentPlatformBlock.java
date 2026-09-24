package lumien.randomthings.block;

import lumien.randomthings.tileentity.FilteredSuperLubricentPlatformTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Like {@link SuperLubricentPlatformBlock}, but dropped items matching a
 * configured filter fall straight through it instead of resting on top.
 */
public class FilteredSuperLubricentPlatformBlock extends SuperLubricentPlatformBlock
{
	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new FilteredSuperLubricentPlatformTileEntity();
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		Entity entity = context.getEntity();

		if (entity instanceof ItemEntity)
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof FilteredSuperLubricentPlatformTileEntity && ((FilteredSuperLubricentPlatformTileEntity) te).matchesFilter(((ItemEntity) entity).getItem()))
			{
				return VoxelShapes.empty();
			}
		}

		return super.getCollisionShape(state, worldIn, pos, context);
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof FilteredSuperLubricentPlatformTileEntity)
			{
				NonNullList<ItemStack> drops = NonNullList.create();
				drops.add(((FilteredSuperLubricentPlatformTileEntity) te).filterInventory().getStackInSlot(0));
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

			if (te instanceof FilteredSuperLubricentPlatformTileEntity)
			{
				NetworkHooks.openGui((ServerPlayerEntity) player, (FilteredSuperLubricentPlatformTileEntity) te);
			}
		}

		return true;
	}
}
