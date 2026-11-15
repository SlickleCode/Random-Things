package lumien.randomthings.worldgen;

import net.minecraft.core.registries.Registries;

import net.minecraft.util.RandomSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;

import lumien.randomthings.config.RTConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.minecraft.core.Registry;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
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
public class PeaceCandleChurchPool extends StructureTemplatePool {
    private static final String[] BIOMES = { "plains", "desert", "savanna", "snowy", "taiga" };

    /** Vanilla church template names all contain this ({@code plains_temple_4}, {@code desert_temple_1}, ...). */
    private static final String CHURCH = "_temple_";

    private static final java.lang.reflect.Field HOLDER_VALUE = ObfuscationReflectionHelper.findField(Holder.Reference.class, "value");
    private static final java.lang.reflect.Field ELEMENT_TEMPLATE = ObfuscationReflectionHelper.findField(SinglePoolElement.class, "template");
    private static final java.lang.reflect.Field ELEMENT_PROCESSORS = ObfuscationReflectionHelper.findField(SinglePoolElement.class, "processors");
    private static final java.lang.reflect.Field TEMPLATE_PALETTES = ObfuscationReflectionHelper.findField(StructureTemplate.class, "palettes");

    /** vanilla church piece -> the same piece with a candle; identity keys, the pool hands out its own instances. */
    private final Map<StructurePoolElement, StructurePoolElement> candleChurches;

    private final StructureTemplatePool vanilla;

    private PeaceCandleChurchPool(StructureTemplatePool vanilla, Map<StructurePoolElement, StructurePoolElement> candleChurches) {
        super(vanilla.getFallback(), java.util.Collections.<Pair<StructurePoolElement, Integer>>emptyList());
        this.vanilla = vanilla;
        this.candleChurches = candleChurches;
    }

    /**
     * Wraps the vanilla village houses pools in this world's dynamic registry. Called from
     * {@code ServerAboutToStartEvent}, which fires once per world load, so it is safe to call repeatedly.
     */
    public static void install(MinecraftServer server) {
        Registry<StructureTemplatePool> registry = server.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL);

        for (String biome : BIOMES) {
            wrap(server, registry, "village/" + biome + "/houses");
            wrap(server, registry, "village/" + biome + "/zombie/houses");
        }
    }

    private static void wrap(MinecraftServer server, Registry<StructureTemplatePool> registry, String path) {
        Holder<StructureTemplatePool> holder = registry.getHolder(ResourceKey.create(Registries.TEMPLATE_POOL, ResourceLocation.parse(path))).orElse(null);

        if (holder == null) {
            LogManager.getLogger().warn("Vanilla pool {} not found, Peace Candle churches won't generate there", path);
            return;
        }

        StructureTemplatePool vanilla = holder.value();

        if (vanilla instanceof PeaceCandleChurchPool) {
            return;
        }

        Map<StructurePoolElement, StructurePoolElement> churches = new IdentityHashMap<>();

        // every element of the pool (the shuffled list holds each once per weight point)
        for (StructurePoolElement element : vanilla.getShuffledTemplates(RandomSource.create(0))) {
            if (!churches.containsKey(element)) {
                StructurePoolElement variant = candleVariant(server, element);

                if (variant != null) {
                    churches.put(element, variant);
                }
            }
        }

        if (churches.isEmpty()) {
            return;
        }

        try {
            // the pool registry is frozen by the time the server starts, so the holder's value is swapped in place instead
            HOLDER_VALUE.set(holder, new PeaceCandleChurchPool(vanilla, churches));
        } catch (IllegalAccessException e) {
            LogManager.getLogger().error("Could not install the Peace Candle church pool for " + path, e);
        }
    }

    /** The piece with a {@link PeaceCandleProcessor} appended to its processors, or null if it is not a vanilla church or has no spot for a candle. */
    @SuppressWarnings("unchecked")
    private static StructurePoolElement candleVariant(MinecraftServer server, StructurePoolElement piece) {
        if (!(piece instanceof SinglePoolElement single) || !piece.toString().contains(CHURCH)) {
            return null;
        }

        try {
            Either<ResourceLocation, StructureTemplate> template = (Either<ResourceLocation, StructureTemplate>) ELEMENT_TEMPLATE.get(single);
            ResourceLocation id = template.left().orElse(null);
            StructureTemplate loaded = id == null ? null : server.getStructureManager().get(id).orElse(null);
            BlockPos spot = loaded == null ? null : candleSpot(loaded);

            if (spot == null) {
                LogManager.getLogger().warn("No spot for a Peace Candle in {}", piece);
                return null;
            }

            Holder<StructureProcessorList> processors = (Holder<StructureProcessorList>) ELEMENT_PROCESSORS.get(single);
            List<StructureProcessor> list = new ArrayList<>(processors.value().list());
            list.add(new PeaceCandleProcessor(spot));

            return StructurePoolElement.single(id.toString(), Holder.direct(new StructureProcessorList(list))).apply(piece.getProjection());
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
    public static BlockPos candleSpot(StructureTemplate template) {
        Map<BlockPos, BlockState> blocks = new HashMap<>();

        try {
            List<?> palettes = (List<?>) TEMPLATE_PALETTES.get(template);

            if (palettes.isEmpty()) {
                return null;
            }

            for (StructureBlockInfo info : ((StructureTemplate.Palette) palettes.get(0)).blocks()) {
                blocks.put(info.pos(), info.state());
            }
        } catch (IllegalAccessException e) {
            return null;
        }

        Vec3i size = template.getSize();
        BlockPos best = null;
        int bestCentral = -1;

        for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
            BlockPos pos = entry.getKey();

            if (!entry.getValue().isAir() || !isAir(blocks, pos.above()) || !isFloor(blocks, pos.below()) || !roofed(blocks, pos, size.getY()) || !walled(blocks, pos)) {
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
        return state != null && state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
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
                wall = solidAt(blocks, pos.relative(dir, k));
            }

            if (!wall) {
                return false;
            }
        }

        return true;
    }

    /** True if the piece is a village church carrying the candle processor (also after a save and reload, which rebuilds the piece). */
    @SuppressWarnings("unchecked")
    public static boolean hasCandle(StructurePoolElement piece) {
        if (!(piece instanceof SinglePoolElement single)) {
            return false;
        }

        try {
            return ((Holder<StructureProcessorList>) ELEMENT_PROCESSORS.get(single)).value().list().stream().anyMatch(p -> p instanceof PeaceCandleProcessor);
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    /** The {@code PeaceCandle} config switch; a supplier so the automated tests can flip it without rewriting the config file. */
    public static java.util.function.BooleanSupplier enabled = () -> RTConfig.PEACE_CANDLE.get();

    private static boolean shouldSwap(RandomSource rand) {
        return enabled.getAsBoolean() && rand.nextInt(3) == 0;
    }

    private StructurePoolElement swap(StructurePoolElement piece) {
        return candleChurches.getOrDefault(piece, piece);
    }

    @Override
    public int getMaxSize(StructureTemplateManager templateManager) {
        return vanilla.getMaxSize(templateManager);
    }

    @Override
    public StructurePoolElement getRandomTemplate(RandomSource rand) {
        StructurePoolElement piece = vanilla.getRandomTemplate(rand);
        return shouldSwap(rand) ? swap(piece) : piece;
    }

    @Override
    public List<StructurePoolElement> getShuffledTemplates(RandomSource rand) {
        List<StructurePoolElement> pieces = vanilla.getShuffledTemplates(rand);

        if (!shouldSwap(rand)) {
            return pieces;
        }

        return pieces.stream().map(this::swap).collect(Collectors.toList());
    }

    @Override
    public int size() {
        return vanilla.size();
    }
}
