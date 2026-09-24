package lumien.randomthings.block;

import java.util.Collections;
import java.util.List;

import lumien.randomthings.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.common.IShearable;
import net.minecraft.util.BlockRenderLayer;

/**
 * Hangs from the underside of a giant brown mushroom cap. The 1.12.2 version
 * also applied a custom "collapse" potion effect to nearby living entities
 * each tick (lumien.randomthings.potion.ModPotions.collapse) - dropped here
 * since that potion effect hasn't been ported yet.
 */
public class SakanadeBlock extends Block implements IShearable
{
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(0, 15, 0, 16, 16, 16);

	public SakanadeBlock()
	{
		super(Block.Properties.create(Material.PLANTS).hardnessAndResistance(0.0F).sound(SoundType.PLANT).doesNotBlockMovement().noDrops());
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return VoxelShapes.empty();
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos)
	{
		return worldIn.getBlockState(pos.up()).getBlock() == Blocks.BROWN_MUSHROOM_BLOCK;
	}

	@Override
	public BlockState updatePostPlacement(BlockState stateIn, net.minecraft.util.Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos)
	{
		return !stateIn.isValidPosition(worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public boolean isShearable(ItemStack item, IWorldReader world, BlockPos pos)
	{
		return true;
	}

	@Override
	public List<ItemStack> onSheared(ItemStack item, IWorld world, BlockPos pos, int fortune)
	{
		return Collections.singletonList(new ItemStack(ModItems.SAKANADE_SPORES));
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}
}
