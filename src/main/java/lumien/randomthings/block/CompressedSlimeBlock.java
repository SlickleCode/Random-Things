package lumien.randomthings.block;

import lumien.randomthings.lib.IRTBlockColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;

public class CompressedSlimeBlock extends Block implements IRTBlockColor
{
	protected static final VoxelShape SHAPE_0 = Block.makeCuboidShape(0, 0, 0, 16, 8, 16);
	protected static final VoxelShape SHAPE_1 = Block.makeCuboidShape(0, 0, 0, 16, 4, 16);
	protected static final VoxelShape SHAPE_2 = Block.makeCuboidShape(0, 0, 0, 16, 2, 16);

	private static final int[] COMPRESSION_COLORS = new int[] { 0xC8C8C8, 0x969696, 0x646464 };

	public static final IntegerProperty COMPRESSION = IntegerProperty.create("compression", 0, 2);

	public CompressedSlimeBlock()
	{
		super(Block.Properties.create(Material.CLAY, MaterialColor.LIGHT_GRAY).sound(SoundType.SLIME));

		this.setDefaultState(this.stateContainer.getBaseState().with(COMPRESSION, 0));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(COMPRESSION);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		switch (state.get(COMPRESSION))
		{
			case 0:
				return SHAPE_0;
			case 1:
				return SHAPE_1;
			default:
				return SHAPE_2;
		}
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
	{
		super.onEntityCollision(state, worldIn, pos, entityIn);

		if (entityIn.getMotion().y < 1)
		{
			int compression = state.get(COMPRESSION);

			entityIn.onGround = false;

			double motionY = 0.8 + compression * 0.4;
			entityIn.setMotion(entityIn.getMotion().x, motionY, entityIn.getMotion().z);
			entityIn.fallDistance = 0;
		}
	}

	@Override
	public int colorMultiplier(BlockState state, IEnviromentBlockReader worldIn, BlockPos pos, int tintIndex)
	{
		return COMPRESSION_COLORS[state.get(COMPRESSION)];
	}
}
