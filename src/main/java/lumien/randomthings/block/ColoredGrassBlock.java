package lumien.randomthings.block;

import lumien.randomthings.util.StackTag;
import net.minecraft.util.RandomSource;
import java.util.Random;

import lumien.randomthings.lib.IRTBlockColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class ColoredGrassBlock extends Block implements IRTBlockColor
{
	public static final EnumProperty<DyeColor> COLOR = EnumProperty.create("color", DyeColor.class);

	public ColoredGrassBlock()
	{
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.GRASS).noOcclusion().strength(0.6F).sound(SoundType.GRASS).randomTicks());

		this.registerDefaultState(this.stateDefinition.any().setValue(COLOR, DyeColor.WHITE));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		builder.add(COLOR);
	}

	public static int getTint(DyeColor color)
	{
		float[] rgb = lumien.randomthings.util.DyeUtil.diffuse(color);
		return ((int) (rgb[0] * 255) << 16) | ((int) (rgb[1] * 255) << 8) | (int) (rgb[2] * 255);
	}

	/** Color stored in a Colored Grass item's {@code BlockStateTag} (Silk Touch drops), white if absent. */
	public static DyeColor getColor(ItemStack stack)
	{
		CompoundTag tag = StackTag.getElement(stack, "BlockStateTag");

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
	public net.minecraft.network.chat.MutableComponent getName()
	{
		return net.minecraft.network.chat.Component.translatable("block.randomthings.colored_grass.white");
	}

	@Override
	public int colorMultiplier(BlockState state, BlockAndTintGetter worldIn, BlockPos pos, int tintIndex)
	{
		return getTint(state.getValue(COLOR));
	}

	/** Port of 1.12.2's {@code updateTick}: reverts to dirt when smothered, otherwise spreads its color to nearby dirt. */
	/** 1.20.5 made {@code randomTick} a no-op by default (it used to call {@code tick}), so random ticks are forwarded explicitly. */
	@Override
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand)
	{
		this.tick(state, worldIn, pos, rand);
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand)
	{
		if (worldIn.isClientSide())
		{
			return;
		}

		BlockPos up = pos.above();
		BlockState upState = worldIn.getBlockState(up);

		if (worldIn.getMaxLocalRawBrightness(up) < 4 && upState.getLightBlock() > 2)
		{
			worldIn.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
		}
		else if (worldIn.getMaxLocalRawBrightness(up) >= 9)
		{
			for (int i = 0; i < 4; ++i)
			{
				BlockPos target = pos.offset(rand.nextInt(3) - 1, rand.nextInt(5) - 3, rand.nextInt(3) - 1);

				if (!worldIn.hasChunkAt(target))
				{
					return;
				}

				BlockPos targetUp = target.above();
				BlockState aboveTarget = worldIn.getBlockState(targetUp);

				if (worldIn.getBlockState(target).getBlock() == Blocks.DIRT && worldIn.getMaxLocalRawBrightness(targetUp) >= 4 && aboveTarget.getLightBlock() <= 2)
				{
					worldIn.setBlockAndUpdate(target, state);
				}
			}
		}
	}
}
