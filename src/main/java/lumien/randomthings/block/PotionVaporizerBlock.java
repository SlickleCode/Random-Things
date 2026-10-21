package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import lumien.randomthings.tileentity.PotionVaporizerTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;

/**
 * Faces an enclosed room and vaporizes a stored potion into it over time,
 * fuelled the same way a furnace is.
 */
public class PotionVaporizerBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

	public PotionVaporizerBlock()
	{
		super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.METAL).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.IRON_XYLOPHONE).strength(3.0F));

		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
	    return new PotionVaporizerTileEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
	    return Tickers.of(type, ModTileEntityTypes.POTION_VAPORIZER, (l, p, s2, be) -> be.tick());
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
		if (!worldIn.isClientSide)
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof PotionVaporizerTileEntity)
			{
				((ServerPlayer) player).openMenu((PotionVaporizerTileEntity) te);
			}
		}

		return InteractionResult.SUCCESS;
	}
}
