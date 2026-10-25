package lumien.randomthings.block;

import lumien.randomthings.lib.IRTBlockColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;

public class CompressedSlimeBlock extends Block implements IRTBlockColor
{
	protected static final VoxelShape SHAPE_0 = Block.box(0, 0, 0, 16, 8, 16);
	protected static final VoxelShape SHAPE_1 = Block.box(0, 0, 0, 16, 4, 16);
	protected static final VoxelShape SHAPE_2 = Block.box(0, 0, 0, 16, 2, 16);

	private static final int[] COMPRESSION_COLORS = new int[] { 0xC8C8C8, 0x969696, 0x646464 };

	public static final IntegerProperty COMPRESSION = IntegerProperty.create("compression", 0, 2);

	public CompressedSlimeBlock()
	{
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.COLOR_LIGHT_GRAY).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.FLUTE).sound(SoundType.SLIME_BLOCK));

		this.registerDefaultState(this.stateDefinition.any().setValue(COMPRESSION, 0));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(COMPRESSION);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		switch (state.getValue(COMPRESSION))
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
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn, net.minecraft.world.entity.InsideBlockEffectApplier rtApplier, boolean rtPrecise)
	{
		super.entityInside(state, worldIn, pos, entityIn, rtApplier, rtPrecise);

		if (entityIn.getDeltaMovement().y < 1)
		{
			int compression = state.getValue(COMPRESSION);

			entityIn.setOnGround(false);

			double motionY = 0.8 + compression * 0.4;
			entityIn.setDeltaMovement(entityIn.getDeltaMovement().x, motionY, entityIn.getDeltaMovement().z);
			entityIn.fallDistance = 0;
		}
	}

	@Override
	public int colorMultiplier(BlockState state, BlockAndTintGetter worldIn, BlockPos pos, int tintIndex)
	{
		return COMPRESSION_COLORS[state.getValue(COMPRESSION)];
	}
}
