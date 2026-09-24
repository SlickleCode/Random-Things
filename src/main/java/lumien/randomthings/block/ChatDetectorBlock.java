package lumien.randomthings.block;

import lumien.randomthings.tileentity.ChatDetectorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Links itself to whoever places it and pulses redstone whenever they send
 * a specific chat message (optionally hiding it from chat).
 */
public class ChatDetectorBlock extends Block
{
	public static final BooleanProperty POWERED = BooleanProperty.create("powered");

	public ChatDetectorBlock()
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
		return new ChatDetectorTileEntity();
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if (!worldIn.isRemote && placer instanceof ServerPlayerEntity)
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof ChatDetectorTileEntity)
			{
				((ChatDetectorTileEntity) te).setPlayerUUID(placer.getUniqueID());
			}
		}
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (!worldIn.isRemote)
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof ChatDetectorTileEntity)
			{
				ChatDetectorTileEntity cte = (ChatDetectorTileEntity) te;

				NetworkHooks.openGui((ServerPlayerEntity) player, cte, buf -> {
					buf.writeBlockPos(pos);
					buf.writeString(cte.getChatMessage());
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
