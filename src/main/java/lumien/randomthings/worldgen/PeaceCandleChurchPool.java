package lumien.randomthings.worldgen;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;

import lumien.randomthings.config.RTConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.template.TemplateManager;
import org.apache.logging.log4j.LogManager;

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

    /** The vanilla plains church template, matched on its piece description (the template name is not otherwise readable). */
    private static final String VANILLA_CHURCH = "village/plains/houses/plains_temple_4";

    // RIGID matches what the vanilla plains village registers the houses pool with.
    private static final JigsawPiece PEACE_CANDLE_CHURCH = JigsawPiece.func_242859_b("randomthings:peace_candle_church").apply(JigsawPattern.PlacementBehaviour.RIGID);

    private final JigsawPattern vanilla;

    private PeaceCandleChurchPool(JigsawPattern vanilla) {
        super(vanilla.getName(), vanilla.getFallback(), java.util.Collections.<Pair<JigsawPiece, Integer>>emptyList());
        this.vanilla = vanilla;
    }

    /**
     * Wraps the vanilla plains houses pool in this world's dynamic registry (1.16 loads jigsaw pools as data, so the
     * 1.14.4 static-registry replacement is gone). Called from {@code FMLServerAboutToStartEvent}, which fires once
     * per world load, so it is safe to call repeatedly.
     */
    public static void install(MinecraftServer server) {
        Registry<JigsawPattern> registry = server.getDynamicRegistries().getRegistry(Registry.JIGSAW_POOL_KEY);
        JigsawPattern vanilla = registry.getOrDefault(HOUSES);

        if (vanilla instanceof PeaceCandleChurchPool) {
            return;
        }

        if (vanilla == null) {
            LogManager.getLogger().warn("Vanilla pool {} not found, Peace Candle churches won't generate", HOUSES);
            return;
        }

        if (registry instanceof MutableRegistry) {
            ((MutableRegistry<JigsawPattern>) registry).register(RegistryKey.getOrCreateKey(Registry.JIGSAW_POOL_KEY, HOUSES), new PeaceCandleChurchPool(vanilla), Lifecycle.stable());
        }
    }

    private static boolean shouldSwap(Random rand) {
        return RTConfig.PEACE_CANDLE.get() && rand.nextInt(3) == 0;
    }

    private static JigsawPiece swap(JigsawPiece piece) {
        return piece.toString().contains(VANILLA_CHURCH) ? PEACE_CANDLE_CHURCH : piece;
    }

    @Override
    public int getMaxSize(TemplateManager templateManager) {
        return vanilla.getMaxSize(templateManager);
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
