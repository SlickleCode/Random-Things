package lumien.randomthings.block;

import lumien.randomthings.lib.IRTBlockColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.DyeColor;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.util.BlockRenderLayer;

public class ColoredGrassBlock extends Block implements IRTBlockColor
{
	public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

	public ColoredGrassBlock()
	{
		super(Block.Properties.create(Material.ORGANIC, MaterialColor.GRASS).hardnessAndResistance(0.6F).sound(SoundType.PLANT).tickRandomly());

		this.setDefaultState(this.stateContainer.getBaseState().with(COLOR, DyeColor.WHITE));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(COLOR);
	}

	@Override
	public int colorMultiplier(BlockState state, IEnviromentBlockReader worldIn, BlockPos pos, int tintIndex)
	{
		float[] rgb = state.get(COLOR).getColorComponentValues();
		return ((int) (rgb[0] * 255) << 16) | ((int) (rgb[1] * 255) << 8) | (int) (rgb[2] * 255);
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}
}
