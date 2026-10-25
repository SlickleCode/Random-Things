package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import lumien.randomthings.tileentity.GlobalChatDetectorTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import lumien.randomthings.itemhandler.ItemStackHandler;
import net.minecraft.world.InteractionResult;

/**
 * Like {@link ChatDetectorBlock}, but listens for any player's chat instead
 * of only whoever placed it - see {@link GlobalChatDetectorTileEntity}.
 */
public class GlobalChatDetectorBlock extends Block implements EntityBlock {
	public static final BooleanProperty POWERED = BooleanProperty.create("powered");

	public GlobalChatDetectorBlock()
	{
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(2.0F));

		this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(POWERED);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
	    return new GlobalChatDetectorTileEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
	{
	    return Tickers.of(type, ModTileEntityTypes.GLOBAL_CHAT_DETECTOR, (l, p, s2, be) -> be.tick());
	}

	public void rtOnRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof GlobalChatDetectorTileEntity)
			{
				ItemStackHandler idCards = ((GlobalChatDetectorTileEntity) te).idCardInventory();
				NonNullList<ItemStack> drops = NonNullList.create();

				for (int i = 0; i < idCards.getSlots(); i++)
				{
					drops.add(idCards.getStackInSlot(i));
				}

				Containers.dropContents(worldIn, pos, drops);
			}
		}

	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
		if (!worldIn.isClientSide())
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof GlobalChatDetectorTileEntity)
			{
				GlobalChatDetectorTileEntity gcte = (GlobalChatDetectorTileEntity) te;

				((ServerPlayer) player).openMenu(gcte, buf -> {
					buf.writeBlockPos(pos);
					buf.writeUtf(gcte.getChatMessage());
				});
			}
		}

		return InteractionResult.SUCCESS;
	}

	@Override
	public boolean isSignalSource(BlockState state)
	{
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		return blockState.getValue(POWERED) ? 15 : 0;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		return blockState.getValue(POWERED) ? 15 : 0;
	}
}
