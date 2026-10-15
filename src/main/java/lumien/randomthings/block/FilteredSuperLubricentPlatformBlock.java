package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.FilteredSuperLubricentPlatformTileEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.InteractionResult;

/**
 * Like {@link SuperLubricentPlatformBlock}, but dropped items matching a
 * configured filter fall straight through it instead of resting on top.
 */
public class FilteredSuperLubricentPlatformBlock extends SuperLubricentPlatformBlock implements EntityBlock {
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
	    return new FilteredSuperLubricentPlatformTileEntity(pos, state);
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		Entity entity = context instanceof net.minecraft.world.phys.shapes.EntityCollisionContext ecc ? ecc.getEntity() : null;

		if (entity instanceof ItemEntity)
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof FilteredSuperLubricentPlatformTileEntity && ((FilteredSuperLubricentPlatformTileEntity) te).matchesFilter(((ItemEntity) entity).getItem()))
			{
				return Shapes.empty();
			}
		}

		return super.getCollisionShape(state, worldIn, pos, context);
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof FilteredSuperLubricentPlatformTileEntity)
			{
				NonNullList<ItemStack> drops = NonNullList.create();
				drops.add(((FilteredSuperLubricentPlatformTileEntity) te).filterInventory().getStackInSlot(0));
				Containers.dropContents(worldIn, pos, drops);
			}
		}

		super.onRemove(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit)
	{
		if (!worldIn.isClientSide)
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof FilteredSuperLubricentPlatformTileEntity)
			{
				NetworkHooks.openGui((ServerPlayer) player, (FilteredSuperLubricentPlatformTileEntity) te);
			}
		}

		return InteractionResult.SUCCESS;
	}
}
