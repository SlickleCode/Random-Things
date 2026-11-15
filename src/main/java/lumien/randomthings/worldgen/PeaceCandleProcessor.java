package lumien.randomthings.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

/**
 * Replaces the template's air block at one template-local position with a Peace Candle. It is added to the
 * processor list of a vanilla village church piece (see {@link PeaceCandleChurchPool}), so churches get their
 * candle without a template file of their own; being a registered processor type it survives the piece being
 * saved and reloaded, which matters because a piece is placed chunk by chunk.
 */
public class PeaceCandleProcessor extends StructureProcessor
{
	public static final MapCodec<PeaceCandleProcessor> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(BlockPos.CODEC.fieldOf("pos").forGetter(p -> p.pos)).apply(i, PeaceCandleProcessor::new));

	public static final StructureProcessorType<PeaceCandleProcessor> TYPE = () -> CODEC;

	/** Template-local position (before rotation and placement) of the block that becomes the candle. */
	public final BlockPos pos;

	public PeaceCandleProcessor(BlockPos pos)
	{
		this.pos = pos;
	}

	@Override
	public StructureBlockInfo processBlock(LevelReader level, BlockPos offset, BlockPos worldPos, StructureBlockInfo blockInfo, StructureBlockInfo relativeBlockInfo, StructurePlaceSettings settings)
	{
		if (blockInfo.pos().equals(pos) && blockInfo.state().isAir())
		{
			return new StructureBlockInfo(relativeBlockInfo.pos(), ModBlocks.PEACE_CANDLE.defaultBlockState(), null);
		}

		return relativeBlockInfo;
	}

	@Override
	protected StructureProcessorType<?> getType()
	{
		return TYPE;
	}
}
