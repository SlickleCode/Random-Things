package lumien.randomthings.worldgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;

import lumien.randomthings.config.RTConfig;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.StructureProcessorList;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;

/**
 * Peace Candle's village-church generation. 1.12.2 patched {@code
 * StructureVillagePieces$Church#addComponentParts} to drop a Peace Candle into a third of generated
 * churches ({@code WorldGenPeaceCandle}); here villages are jigsaw pools, so this wraps every village
 * {@code houses} pool (all five biomes, plus the zombie variants) instead of using a coremod: whenever the
 * assembler asks the pool for candidates, a third of the time every vanilla church ({@code *_temple_*}) is
 * swapped for the same template with a {@link PeaceCandleProcessor} added to its processor list. No template
 * file is involved: the candle position is picked from the vanilla template itself ({@link #candleSpot}), so
 * the footprint is exactly the vanilla church's.
 * <p>
 * Deliberately doing the swap at pick time rather than adding the church as a second pool entry:
 * it makes "about a third of churches" exact regardless of the pool's own weights, and lets the
 * {@code PeaceCandle} config option be honored per world load without a restart. With the option
 * off (or the dice not landing) the vanilla list is returned untouched and no extra randomness is
 * consumed, so worlds generate exactly as vanilla.
 */
public class PeaceCandleChurchPool extends JigsawPattern {
    private static final String[] BIOMES = { "plains", "desert", "savanna", "snowy", "taiga" };

    /** Vanilla church template names all contain this ({@code plains_temple_4}, {@code desert_temple_1}, ...). */
    private static final String CHURCH = "_temple_";

    // SRG names (findField takes SRG ids; in the dev environment they are mapped back)
    private static final java.lang.reflect.Field ELEMENT_TEMPLATE = ObfuscationReflectionHelper.findField(SingleJigsawPiece.class, "field_236839_c_");
    private static final java.lang.reflect.Field ELEMENT_PROCESSORS = ObfuscationReflectionHelper.findField(SingleJigsawPiece.class, "field_214862_b");
    private static final java.lang.reflect.Field TEMPLATE_PALETTES = ObfuscationReflectionHelper.findField(Template.class, "field_204769_a");

    /** vanilla church piece -> the same piece with a candle; identity keys, the pool hands out its own instances. */
    private final Map<JigsawPiece, JigsawPiece> candleChurches;

    private final JigsawPattern vanilla;

    private PeaceCandleChurchPool(JigsawPattern vanilla, Map<JigsawPiece, JigsawPiece> candleChurches) {
        super(vanilla.getName(), vanilla.getFallback(), java.util.Collections.<Pair<JigsawPiece, Integer>>emptyList());
        this.vanilla = vanilla;
        this.candleChurches = candleChurches;
    }

    /**
     * Wraps the vanilla village houses pools in this world's dynamic registry (1.16 loads jigsaw pools as data).
     * Called from {@code FMLServerAboutToStartEvent}, which fires once per world load, so it is safe to call repeatedly.
     */
    public static void install(MinecraftServer server) {
        Registry<JigsawPattern> registry = server.getDynamicRegistries().getRegistry(Registry.JIGSAW_POOL_KEY);

        for (String biome : BIOMES) {
            wrap(server, registry, "village/" + biome + "/houses");
            wrap(server, registry, "village/" + biome + "/zombie/houses");
        }
    }

    private static void wrap(MinecraftServer server, Registry<JigsawPattern> registry, String path) {
        ResourceLocation id = new ResourceLocation(path);
        JigsawPattern vanilla = registry.getOrDefault(id);

        if (vanilla instanceof PeaceCandleChurchPool) {
            return;
        }

        if (vanilla == null) {
            LogManager.getLogger().warn("Vanilla pool {} not found, Peace Candle churches won't generate there", id);
            return;
        }

        Map<JigsawPiece, JigsawPiece> churches = new IdentityHashMap<>();

        // every element of the pool (the shuffled list holds each once per weight point)
        for (JigsawPiece element : vanilla.getShuffledPieces(new Random(0))) {
            if (!churches.containsKey(element)) {
                JigsawPiece variant = candleVariant(server, element);

                if (variant != null) {
                    churches.put(element, variant);
                }
            }
        }

        if (churches.isEmpty()) {
            return;
        }

        if (registry instanceof MutableRegistry) {
            ((MutableRegistry<JigsawPattern>) registry).register(RegistryKey.getOrCreateKey(Registry.JIGSAW_POOL_KEY, id), new PeaceCandleChurchPool(vanilla, churches), Lifecycle.stable());
        }
    }

    /** The piece with a {@link PeaceCandleProcessor} appended to its processors, or null if it is not a vanilla church or has no spot for a candle. */
    @SuppressWarnings("unchecked")
    private static JigsawPiece candleVariant(MinecraftServer server, JigsawPiece piece) {
        if (!(piece instanceof SingleJigsawPiece) || !piece.toString().contains(CHURCH)) {
            return null;
        }

        try {
            Either<ResourceLocation, Template> template = (Either<ResourceLocation, Template>) ELEMENT_TEMPLATE.get(piece);
            ResourceLocation id = template.left().orElse(null);
            Template loaded = id == null ? null : server.getTemplateManager().getTemplate(id);
            BlockPos spot = loaded == null ? null : candleSpot(loaded);

            if (spot == null) {
                LogManager.getLogger().warn("No spot for a Peace Candle in {}", piece);
                return null;
            }

            java.util.function.Supplier<StructureProcessorList> processors = (java.util.function.Supplier<StructureProcessorList>) ELEMENT_PROCESSORS.get(piece);
            List<StructureProcessor> list = new ArrayList<>(processors.get().func_242919_a());
            list.add(new PeaceCandleProcessor(spot));

            return JigsawPiece.func_242861_b(id.toString(), new StructureProcessorList(list)).apply(piece.getPlacementBehaviour());
        } catch (IllegalAccessException e) {
            LogManager.getLogger().error("Could not build the Peace Candle variant of " + piece, e);
            return null;
        }
    }

    /**
     * Where the candle goes: a floor cell inside the building, i.e. an air cell with a full block under it and air
     * above, a roof block somewhere higher in its column and a wall block within 7 cells in each of the four
     * directions; of those the most central one, then the lowest. Null when the template has none.
     */
    @SuppressWarnings("unchecked")
    public static BlockPos candleSpot(Template template) {
        Map<BlockPos, BlockState> blocks = new HashMap<>();

        try {
            List<Template.Palette> palettes = (List<Template.Palette>) TEMPLATE_PALETTES.get(template);

            if (palettes.isEmpty()) {
                return null;
            }

            for (Template.BlockInfo info : palettes.get(0).func_237157_a_()) {
                blocks.put(info.pos, info.state);
            }
        } catch (IllegalAccessException e) {
            return null;
        }

        BlockPos size = template.getSize();
        BlockPos best = null;
        int bestCentral = -1;

        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();

            if (!entry.getValue().isAir() || !isAir(blocks, pos.up()) || !isFloor(blocks, pos.down()) || !roofed(blocks, pos, size.getY()) || !walled(blocks, pos)) {
                continue;
            }

            int central = Math.min(Math.min(pos.getX(), size.getX() - 1 - pos.getX()), Math.min(pos.getZ(), size.getZ() - 1 - pos.getZ()));

            if (central > bestCentral || (central == bestCentral && pos.getY() < best.getY())) {
                best = pos;
                bestCentral = central;
            }
        }

        return best;
    }

    private static boolean isAir(Map<BlockPos, BlockState> blocks, BlockPos pos) {
        BlockState state = blocks.get(pos);
        return state != null && state.isAir();
    }

    private static boolean isFloor(Map<BlockPos, BlockState> blocks, BlockPos pos) {
        BlockState state = blocks.get(pos);
        return state != null && state.isSolid() && net.minecraft.block.Block.isOpaque(state.getCollisionShape(net.minecraft.world.EmptyBlockReader.INSTANCE, BlockPos.ZERO));
    }

    private static boolean solidAt(Map<BlockPos, BlockState> blocks, BlockPos pos) {
        BlockState state = blocks.get(pos);
        return state != null && !state.isAir();
    }

    private static boolean roofed(Map<BlockPos, BlockState> blocks, BlockPos pos, int height) {
        for (int y = pos.getY() + 2; y < height; y++) {
            if (solidAt(blocks, new BlockPos(pos.getX(), y, pos.getZ()))) {
                return true;
            }
        }

        return false;
    }

    private static boolean walled(Map<BlockPos, BlockState> blocks, BlockPos pos) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            boolean wall = false;

            for (int k = 1; k <= 7 && !wall; k++) {
                wall = solidAt(blocks, pos.offset(dir, k));
            }

            if (!wall) {
                return false;
            }
        }

        return true;
    }

    /** True if the piece is a village church carrying the candle processor (also after a save and reload, which rebuilds the piece). */
    @SuppressWarnings("unchecked")
    public static boolean hasCandle(JigsawPiece piece) {
        if (!(piece instanceof SingleJigsawPiece)) {
            return false;
        }

        try {
            return ((java.util.function.Supplier<StructureProcessorList>) ELEMENT_PROCESSORS.get(piece)).get().func_242919_a().stream().anyMatch(p -> p instanceof PeaceCandleProcessor);
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    /** The {@code PeaceCandle} config switch; a supplier so the automated tests can flip it without rewriting the config file. */
    public static java.util.function.BooleanSupplier enabled = () -> RTConfig.PEACE_CANDLE.get();

    private static boolean shouldSwap(Random rand) {
        return enabled.getAsBoolean() && rand.nextInt(3) == 0;
    }

    private JigsawPiece swap(JigsawPiece piece) {
        return candleChurches.getOrDefault(piece, piece);
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

        return pieces.stream().map(this::swap).collect(Collectors.toList());
    }

    @Override
    public int getNumberOfPieces() {
        return vanilla.getNumberOfPieces();
    }
}
