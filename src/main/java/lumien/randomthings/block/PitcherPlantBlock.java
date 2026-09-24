package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.util.BlockRenderLayer;

/**
 * The 1.12.2 version also let this block fill held water bottles/fluid
 * containers and slowly fill neighboring cauldrons/fluid tanks with water.
 * That relied on Forge's legacy {@code net.minecraftforge.fluids.Fluid}
 * wrapper API, which in 1.14.4 is a vestigial compatibility layer disconnected
 * from vanilla's real fluid system ({@code net.minecraft.fluid.Fluids}) -
 * porting it correctly needs proper research into 1.14.4 fluid handling
 * (relevant to several later tile-entity blocks too), so it's deferred rather
 * than guessed at here. This is the plant's standalone placement/shape
 * behavior only.
 */
public class PitcherPlantBlock extends Block
{
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(4.8, 0, 4.8, 11.2, 12.8, 11.2);

	public PitcherPlantBlock()
	{
		super(Block.Properties.create(Material.PLANTS).hardnessAndResistance(0.0F).sound(SoundType.PLANT).tickRandomly().doesNotBlockMovement());
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
		return canPlaceOn(worldIn.getBlockState(pos.down()).getBlock());
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
	{
		super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
		this.checkAndDropBlock(worldIn, pos, state);
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, java.util.Random rand)
	{
		this.checkAndDropBlock(worldIn, pos, state);
	}

	protected void checkAndDropBlock(World worldIn, BlockPos pos, BlockState state)
	{
		if (!canPlaceOn(worldIn.getBlockState(pos.down()).getBlock()))
		{
			spawnDrops(state, worldIn, pos);
			worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
		}
	}

	protected static boolean canPlaceOn(Block ground)
	{
		return ground == Blocks.GRASS_BLOCK || ground == Blocks.DIRT;
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}
}
