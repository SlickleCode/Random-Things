package lumien.randomthings.block;

import java.util.Random;

import lumien.randomthings.lib.IRTBlockColor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraft.world.World;
import net.minecraft.util.BlockRenderLayer;

public class ColoredGrassBlock extends Block implements IRTBlockColor
{
	public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

	public ColoredGrassBlock()
	{
		super(Block.Properties.create(Material.ORGANIC, MaterialColor.GRASS).harvestTool(net.minecraftforge.common.ToolType.SHOVEL).hardnessAndResistance(0.6F).sound(SoundType.PLANT).tickRandomly());

		this.setDefaultState(this.stateContainer.getBaseState().with(COLOR, DyeColor.WHITE));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(COLOR);
	}

	public static int getTint(DyeColor color)
	{
		float[] rgb = color.getColorComponentValues();
		return ((int) (rgb[0] * 255) << 16) | ((int) (rgb[1] * 255) << 8) | (int) (rgb[2] * 255);
	}

	/** Color stored in a Colored Grass item's {@code BlockStateTag} (Silk Touch drops), white if absent. */
	public static DyeColor getColor(ItemStack stack)
	{
		CompoundNBT tag = stack.getChildTag("BlockStateTag");

		if (tag != null && tag.contains(COLOR.getName()))
		{
			for (DyeColor color : DyeColor.values())
			{
				if (color.getName().equals(tag.getString(COLOR.getName())))
				{
					return color;
				}
			}
		}

		return DyeColor.WHITE;
	}

	@Override
	public net.minecraft.util.text.ITextComponent getNameTextComponent()
	{
		return new net.minecraft.util.text.TranslationTextComponent("block.randomthings.colored_grass.white");
	}

	@Override
	public int colorMultiplier(BlockState state, IEnviromentBlockReader worldIn, BlockPos pos, int tintIndex)
	{
		return getTint(state.get(COLOR));
	}

	/** Port of 1.12.2's {@code updateTick}: reverts to dirt when smothered, otherwise spreads its color to nearby dirt. */
	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random rand)
	{
		if (worldIn.isRemote)
		{
			return;
		}

		BlockPos up = pos.up();
		BlockState upState = worldIn.getBlockState(up);

		if (worldIn.getLight(up) < 4 && upState.getOpacity(worldIn, up) > 2)
		{
			worldIn.setBlockState(pos, Blocks.DIRT.getDefaultState());
		}
		else if (worldIn.getLight(up) >= 9)
		{
			for (int i = 0; i < 4; ++i)
			{
				BlockPos target = pos.add(rand.nextInt(3) - 1, rand.nextInt(5) - 3, rand.nextInt(3) - 1);

				if (!worldIn.isBlockLoaded(target))
				{
					return;
				}

				BlockPos targetUp = target.up();
				BlockState aboveTarget = worldIn.getBlockState(targetUp);

				if (worldIn.getBlockState(target).getBlock() == Blocks.DIRT && worldIn.getLight(targetUp) >= 4 && aboveTarget.getOpacity(worldIn, targetUp) <= 2)
				{
					worldIn.setBlockState(target, state);
				}
			}
		}
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}
}
