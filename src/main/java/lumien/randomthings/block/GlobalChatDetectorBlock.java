package lumien.randomthings.block;

import lumien.randomthings.tileentity.GlobalChatDetectorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Like {@link ChatDetectorBlock}, but listens for any player's chat instead
 * of only whoever placed it - see {@link GlobalChatDetectorTileEntity}.
 */
public class GlobalChatDetectorBlock extends Block
{
	public static final BooleanProperty POWERED = BooleanProperty.create("powered");

	public GlobalChatDetectorBlock()
	{
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(2.0F));

		this.setDefaultState(this.stateContainer.getBaseState().with(POWERED, false));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(POWERED);
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new GlobalChatDetectorTileEntity();
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof GlobalChatDetectorTileEntity)
			{
				ItemStackHandler idCards = ((GlobalChatDetectorTileEntity) te).idCardInventory();
				NonNullList<ItemStack> drops = NonNullList.create();

				for (int i = 0; i < idCards.getSlots(); i++)
				{
					drops.add(idCards.getStackInSlot(i));
				}

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

			if (te instanceof GlobalChatDetectorTileEntity)
			{
				GlobalChatDetectorTileEntity gcte = (GlobalChatDetectorTileEntity) te;

				NetworkHooks.openGui((ServerPlayerEntity) player, gcte, buf -> {
					buf.writeBlockPos(pos);
					buf.writeString(gcte.getChatMessage());
				});
			}
		}

		return true;
	}

	@Override
	public boolean canProvidePower(BlockState state)
	{
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
	{
		return blockState.get(POWERED) ? 15 : 0;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
	{
		return blockState.get(POWERED) ? 15 : 0;
	}
}
