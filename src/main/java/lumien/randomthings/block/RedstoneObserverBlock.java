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
import net.minecraftforge.network.NetworkHooks;
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
		super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(2.0F));
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
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit)
	{
		if (!worldIn.isClientSide)
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof RedstoneObserverTileEntity)
			{
				NetworkHooks.openScreen((ServerPlayer) player, (RedstoneObserverTileEntity) te);
			}
		}

		return InteractionResult.SUCCESS;
	}
}
