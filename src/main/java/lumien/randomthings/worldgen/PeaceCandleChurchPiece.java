package lumien.randomthings.worldgen;

import java.util.List;
import java.util.Random;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.jigsaw.IJigsawDeserializer;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;

/**
 * A vanilla village church piece that also places a Peace Candle at one template-local spot.
 * <p>
 * 1.14.4 church templates were saved without their air blocks, and the template placer only hands the blocks a
 * template does contain to a structure processor, so (unlike 1.16.5 and later) there is nothing to replace with a
 * processor; the candle is set after the template is placed instead. It is its own registered piece type, so a
 * saved village reloads it with the spot (a piece is placed chunk by chunk, so the candle is set by whichever
 * chunk contains it).
 */
public class PeaceCandleChurchPiece extends SingleJigsawPiece
{
	public static final IJigsawDeserializer TYPE = dynamic -> new PeaceCandleChurchPiece(dynamic);

	private static boolean registered;

	/** Registers the piece type (once) so villages holding it can be saved and loaded. */
	public static void register()
	{
		if (!registered)
		{
			registered = true;
			IJigsawDeserializer.register("randomthings:peace_candle_church", TYPE);
		}
	}

	/** Template-local position (before rotation and placement) of the cell that gets the candle. */
	public final BlockPos spot;

	public PeaceCandleChurchPiece(String location, List<StructureProcessor> processors, JigsawPattern.PlacementBehaviour behaviour, BlockPos spot)
	{
		super(location, processors, behaviour);
		this.spot = spot;
	}

	public PeaceCandleChurchPiece(Dynamic<?> dynamic)
	{
		super(dynamic);
		this.spot = new BlockPos(dynamic.get("candle_x").asInt(0), dynamic.get("candle_y").asInt(0), dynamic.get("candle_z").asInt(0));
	}

	@Override
	public boolean place(TemplateManager templates, IWorld world, BlockPos pos, Rotation rotation, MutableBoundingBox box, Random rand)
	{
		boolean placed = super.place(templates, world, pos, rotation, box, rand);

		if (placed)
		{
			BlockPos at = Template.transformedBlockPos(createPlacementSettings(rotation, box), spot).add(pos);

			if (box.isVecInside(at))
			{
				world.setBlockState(at, ModBlocks.PEACE_CANDLE.getDefaultState(), 2);
			}
		}

		return placed;
	}

	@Override
	public IJigsawDeserializer getType()
	{
		return TYPE;
	}

	@Override
	public <T> Dynamic<T> serialize0(DynamicOps<T> ops)
	{
		Dynamic<T> data = super.serialize0(ops);
		return data.set("candle_x", data.createInt(spot.getX())).set("candle_y", data.createInt(spot.getY())).set("candle_z", data.createInt(spot.getZ()));
	}
}
