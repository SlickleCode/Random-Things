package lumien.randomthings.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class RainbowLampBlock extends Block
{
	public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

	public RainbowLampBlock()
	{
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.NONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.HAT).lightLevel(state -> 15).strength(0.3F).sound(SoundType.GLASS));

		this.registerDefaultState(this.stateDefinition.any().setValue(COLOR, DyeColor.WHITE));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);

		builder.add(COLOR);
	}

	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState state2, boolean what)
	{
		if (!worldIn.isClientSide())
		{
			updateColor(state, worldIn, pos);
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block neighborBlock, net.minecraft.world.level.redstone.Orientation changedPos, boolean what)
	{
		if (!worldIn.isClientSide())
		{
			updateColor(state, worldIn, pos);
		}
	}

	private void updateColor(BlockState state, Level worldIn, BlockPos pos)
	{
		int redstoneLevel = worldIn.getBestNeighborSignal(pos);

		int colorIndex = state.getValue(COLOR).ordinal();

		if (redstoneLevel != colorIndex)
		{
			worldIn.setBlock(pos, state.setValue(COLOR, DyeColor.values()[redstoneLevel]), 2);
		}
	}
}