package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.RedstoneObserverTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;

/**
 * A "remote redstone wire": right-click with a Redstone Tool to link it to
 * any distant block, and it will mirror that block's weak/strong power.
 * Right-clicking the block itself (without the tool) opens a read-only
 * status GUI showing the current target.
 */
public class RedstoneObserverBlock extends Block implements EntityBlock {
	public RedstoneObserverBlock()
	{
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(2.0F));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
	    return new RedstoneObserverTileEntity(pos, state);
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
		return te instanceof RedstoneObserverTileEntity ? ((RedstoneObserverTileEntity) te).getWeakPower(side) : 0;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		BlockEntity te = blockAccess.getBlockEntity(pos);
		return te instanceof RedstoneObserverTileEntity ? ((RedstoneObserverTileEntity) te).getStrongPower(side) : 0;
	}

	@Override
	public void tick(BlockState state, net.minecraft.server.level.ServerLevel worldIn, BlockPos pos, net.minecraft.util.RandomSource rand)
	{
		BlockEntity te = worldIn.getBlockEntity(pos);

		if (te instanceof RedstoneObserverTileEntity)
		{
			((RedstoneObserverTileEntity) te).refreshAfterLoad();
		}
	}

	@Override
	public InteractionResult useItemOn(net.minecraft.world.item.ItemStack heldStack, BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		if (heldStack.getItem() instanceof lumien.randomthings.item.RedstoneToolItem)
		{
			// PASS skips useWithoutItem (the GUI) so the tool's useOn links the block
			return InteractionResult.PASS;
		}

		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
		if (!worldIn.isClientSide())
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof RedstoneObserverTileEntity)
			{
				((ServerPlayer) player).openMenu((RedstoneObserverTileEntity) te);
			}
		}

		return InteractionResult.SUCCESS;
	}
}
