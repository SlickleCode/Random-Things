package lumien.randomthings.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.gen.feature.template.IStructureProcessorType;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template.BlockInfo;

/**
 * Replaces the template's air block at one template-local position with a Peace Candle. It is added to the
 * processor list of a vanilla village church piece (see {@link PeaceCandleChurchPool}), so churches get their
 * candle without a template file of their own; being a registered processor type it survives the piece being
 * saved and reloaded, which matters because a piece is placed chunk by chunk.
 */
public class PeaceCandleProcessor extends StructureProcessor
{
	public static final Codec<PeaceCandleProcessor> CODEC = RecordCodecBuilder.create(i -> i.group(BlockPos.CODEC.fieldOf("pos").forGetter(p -> p.pos)).apply(i, PeaceCandleProcessor::new));

	public static final IStructureProcessorType<PeaceCandleProcessor> TYPE = () -> CODEC;

	/** Template-local position (before rotation and placement) of the block that becomes the candle. */
	public final BlockPos pos;

	public PeaceCandleProcessor(BlockPos pos)
	{
		this.pos = pos;
	}

	@Override
	public BlockInfo func_230386_a_(IWorldReader level, BlockPos offset, BlockPos worldPos, BlockInfo blockInfo, BlockInfo relativeBlockInfo, PlacementSettings settings)
	{
		if (blockInfo.pos.equals(pos) && blockInfo.state.isAir())
		{
			return new BlockInfo(relativeBlockInfo.pos, ModBlocks.PEACE_CANDLE.getDefaultState(), null);
		}

		return relativeBlockInfo;
	}

	@Override
	protected IStructureProcessorType<?> getType()
	{
		return TYPE;
	}
}
