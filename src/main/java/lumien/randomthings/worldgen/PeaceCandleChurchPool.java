package lumien.randomthings.worldgen;

import com.google.common.collect.ImmutableList;
import lumien.randomthings.config.RTConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.PlainsVillagePools;
import net.minecraft.world.gen.feature.template.TemplateManager;
import org.apache.logging.log4j.LogManager;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Peace Candle's village-church generation. 1.12.2 patched {@code
 * StructureVillagePieces$Church#addComponentParts} to drop a Peace Candle into a third of generated
 * churches ({@code WorldGenPeaceCandle}); 1.14.4 has no hardcoded church class to patch, since
 * villages are jigsaw pools. Those pools are still registered from Java ({@code
 * JigsawManager.REGISTRY}, not data files as the plan file once assumed), so this wraps the plains
 * {@code village/plains/houses} pool instead of using a coremod: whenever the assembler asks the
 * pool for candidates, a third of the time the vanilla tall church ({@code plains_temple_4}) is
 * swapped for {@code randomthings:peace_candle_church}, an NBT template of the same footprint
 * built around a Peace Candle.
 * <p>
 * Deliberately doing the swap at pick time rather than adding the church as a second pool entry:
 * it makes "about a third of churches" exact regardless of the pool's own weights, and lets the
 * {@code PeaceCandle} config option be honored per world load without a restart. With the option
 * off (or the dice not landing) the vanilla list is returned untouched and no extra randomness is
 * consumed, so worlds generate exactly as vanilla.
 * <p>
 * Only the plains pool is wrapped, since the template is cobblestone-styled like the plains church;
 * the other biomes' churches and the zombie-village pools are untouched.
 */
public class PeaceCandleChurchPool extends JigsawPattern {
    private static final ResourceLocation HOUSES = new ResourceLocation("village/plains/houses");

    /** {@code SingleJigsawPiece#toString}'s exact format, the only public way to read a piece's template name. */
    private static final String VANILLA_CHURCH = "Single[minecraft:village/plains/houses/plains_temple_4]";

    // RIGID matches what PlainsVillagePools registers the houses pool with.
    private static final JigsawPiece PEACE_CANDLE_CHURCH = new SingleJigsawPiece("randomthings:peace_candle_church").setPlacementBehaviour(JigsawPattern.PlacementBehaviour.RIGID);

    private final JigsawPattern vanilla;

    private PeaceCandleChurchPool(JigsawPattern vanilla) {
        super(vanilla.func_214947_b(), vanilla.func_214948_a(), ImmutableList.of(), JigsawPattern.PlacementBehaviour.RIGID);
        this.vanilla = vanilla;
    }

    /**
     * Wraps the vanilla plains houses pool. Must run on the server thread (the registry is a plain
     * {@code HashMap}); called from {@code FMLServerAboutToStartEvent}, which fires once per world
     * load, so it's safe to call repeatedly.
     */
    public static void install() {
        // The vanilla pools are registered by this class's static initializer; init() itself is an
        // empty method that just forces it (the village generator calls it before every village too).
        PlainsVillagePools.init();

        JigsawPattern vanilla = JigsawManager.REGISTRY.get(HOUSES);

        if (vanilla instanceof PeaceCandleChurchPool) {
            return;
        }

        if (vanilla == JigsawPattern.INVALID) {
            LogManager.getLogger().warn("Vanilla pool {} not found, Peace Candle churches won't generate", HOUSES);
            return;
        }

        JigsawManager.REGISTRY.register(new PeaceCandleChurchPool(vanilla));
    }

    private static boolean shouldSwap(Random rand) {
        return RTConfig.PEACE_CANDLE.get() && rand.nextInt(3) == 0;
    }

    private static JigsawPiece swap(JigsawPiece piece) {
        return VANILLA_CHURCH.equals(piece.toString()) ? PEACE_CANDLE_CHURCH : piece;
    }

    @Override
    public int func_214945_a(TemplateManager templateManager) {
        return vanilla.func_214945_a(templateManager);
    }

    @Override
    public JigsawPiece getRandomPiece(Random rand) {
        JigsawPiece piece = vanilla.getRandomPiece(rand);
        return shouldSwap(rand) ? swap(piece) : piece;
    }

    @Override
    public List<JigsawPiece> getShuffledPieces(Random rand) {
        List<JigsawPiece> pieces = vanilla.getShuffledPieces(rand);

        if (!shouldSwap(rand)) {
            return pieces;
        }

        return pieces.stream().map(PeaceCandleChurchPool::swap).collect(Collectors.toList());
    }

    @Override
    public int getNumberOfPieces() {
        return vanilla.getNumberOfPieces();
    }
}
