package lumien.randomthings.block.plates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DirectionalAcceleratorPlateBlock extends PlateBlock
{
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

	public DirectionalAcceleratorPlateBlock()
	{
		super(Block.Properties.create(Material.EARTH, MaterialColor.STONE).hardnessAndResistance(0.3F).sound(SoundType.STONE));

		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, net.minecraft.item.ItemStack stack)
	{
		if (placer != null)
		{
			worldIn.setBlockState(pos, state.with(FACING, placer.getHorizontalFacing().getOpposite()), 2);
		}
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
	{
		super.onEntityCollision(state, worldIn, pos, entityIn);

		Direction acceleratingFacing = state.get(FACING).getOpposite();

		Vec3d accVector = new Vec3d(acceleratingFacing.getDirectionVec()).scale(0.1);

		Vec3d motionVec = entityIn.getMotion().add(accVector);

		if (motionVec.lengthSquared() > 0.5 * 0.5)
		{
			motionVec = motionVec.normalize().scale(0.5);
		}

		entityIn.setMotion(motionVec);
	}
}
