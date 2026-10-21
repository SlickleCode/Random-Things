package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import lumien.randomthings.tileentity.EntityDetectorTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;

/**
 * Emits redstone when an entity matching a configurable filter is within a
 * configurable radius.
 */
public class EntityDetectorBlock extends Block implements EntityBlock {
	public EntityDetectorBlock()
	{
		super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(1.5F));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
	    return new EntityDetectorTileEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
	    return Tickers.of(type, ModTileEntityTypes.ENTITY_DETECTOR, (l, p, s2, be) -> be.tick());
	}

	@Override
	public boolean isSignalSource(BlockState state)
	{
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		BlockEntity te = blockAccess.getBlockEntity(pos);
		return te instanceof EntityDetectorTileEntity && ((EntityDetectorTileEntity) te).isPowered() ? 15 : 0;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		BlockEntity te = blockAccess.getBlockEntity(pos);
		return te instanceof EntityDetectorTileEntity && ((EntityDetectorTileEntity) te).isPowered() && ((EntityDetectorTileEntity) te).strongOutput() ? 15 : 0;
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof EntityDetectorTileEntity)
			{
				Containers.dropContents(worldIn, pos, singleStackList((EntityDetectorTileEntity) te));
			}
		}

		super.onRemove(state, worldIn, pos, newState, isMoving);
	}

	private static NonNullList<ItemStack> singleStackList(EntityDetectorTileEntity te)
	{
		NonNullList<ItemStack> list = NonNullList.create();
		list.add(te.filterInventory().getStackInSlot(0));
		return list;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
		if (!worldIn.isClientSide)
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof EntityDetectorTileEntity)
			{
				((ServerPlayer) player).openMenu((EntityDetectorTileEntity) te);
			}
		}

		return InteractionResult.SUCCESS;
	}
}
