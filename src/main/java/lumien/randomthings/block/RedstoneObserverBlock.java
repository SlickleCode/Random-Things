package lumien.randomthings.block;

import lumien.randomthings.tileentity.RedstoneObserverTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * A "remote redstone wire": right-click with a Redstone Tool to link it to
 * any distant block, and it will mirror that block's weak/strong power.
 * Right-clicking the block itself (without the tool) opens a read-only
 * status GUI showing the current target.
 */
public class RedstoneObserverBlock extends Block
{
	public RedstoneObserverBlock()
	{
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(2.0F));
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new RedstoneObserverTileEntity();
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
		return te instanceof RedstoneObserverTileEntity ? ((RedstoneObserverTileEntity) te).getWeakPower(side) : 0;
	}

	@Override
	@SuppressWarnings("deprecation")
	public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side)
	{
		TileEntity te = blockAccess.getTileEntity(pos);
		return te instanceof RedstoneObserverTileEntity ? ((RedstoneObserverTileEntity) te).getStrongPower(side) : 0;
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (!worldIn.isRemote)
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof RedstoneObserverTileEntity)
			{
				NetworkHooks.openGui((ServerPlayerEntity) player, (RedstoneObserverTileEntity) te);
			}
		}

		return true;
	}
}
