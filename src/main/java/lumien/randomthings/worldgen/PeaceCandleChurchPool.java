package lumien.randomthings.worldgen;

import com.google.common.collect.ImmutableList;
import lumien.randomthings.config.RTConfig;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.jigsaw.JigsawManager;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.SingleJigsawPiece;
import net.minecraft.world.gen.feature.structure.DesertVillagePools;
import net.minecraft.world.gen.feature.structure.PlainsVillagePools;
import net.minecraft.world.gen.feature.structure.SavannaVillagePools;
import net.minecraft.world.gen.feature.structure.SnowyVillagePools;
import net.minecraft.world.gen.feature.structure.TaigaVillagePools;
import net.minecraft.world.gen.feature.template.StructureProcessor;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Peace Candle's village-church generation. 1.12.2 patched {@code
 * StructureVillagePieces$Church#addComponentParts} to drop a Peace Candle into a third of generated
 * churches ({@code WorldGenPeaceCandle}); 1.14.4 has no hardcoded church class to patch, since
 * villages are jigsaw pools. Those pools are registered from Java ({@code JigsawManager.REGISTRY}), so this
 * wraps every village {@code houses} pool (all five biomes, plus the zombie variants) instead of using a
 * coremod: whenever the assembler asks the pool for candidates, a third of the time every vanilla church
 * ({@code *_temple_*}) is swapped for the same template with a {@link PeaceCandleChurchPiece} (it sets the candle after placing the template; 1.14.4 templates have no air blocks for a processor to replace). 
 * No template file is involved: the candle position is picked from the vanilla template
 * itself ({@link #candleSpot}), so the footprint is exactly the vanilla church's.
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

    // SRG names (findField takes SRG ids; in the dev environment they are mapped back). 1.14.4 splits a template's blocks into several lists (full blocks, other blocks, tile entities), hence the merge in candleSpot.
    private static final java.lang.reflect.Field ELEMENT_LOCATION = ObfuscationReflectionHelper.findField(SingleJigsawPiece.class, "field_214861_a");
    private static final java.lang.reflect.Field ELEMENT_PROCESSORS = ObfuscationReflectionHelper.findField(SingleJigsawPiece.class, "field_214862_b");
    private static final java.lang.reflect.Field TEMPLATE_PALETTES = ObfuscationReflectionHelper.findField(Template.class, "field_204769_a");

    /**
     * vanilla church piece -> the same piece with a candle; identity keys, the pool hands out its own instances.
     * Filled on first use, not at install: in 1.14.4 the data packs (and so the templates) are not readable yet when
     * {@code FMLServerAboutToStartEvent} fires.
     */
    private Map<JigsawPiece, JigsawPiece> candleChurches;

    private final MinecraftServer server;

    private final JigsawPattern vanilla;

    private PeaceCandleChurchPool(JigsawPattern vanilla, MinecraftServer server) {
        super(vanilla.func_214947_b(), vanilla.func_214948_a(), ImmutableList.of(), JigsawPattern.PlacementBehaviour.RIGID);
        this.vanilla = vanilla;
        this.server = server;
    }

    /**
     * Wraps the vanilla village houses pools. Must run on the server thread (the registry is a plain
     * {@code HashMap}); called from {@code FMLServerAboutToStartEvent}, which fires once per world
     * load, so it's safe to call repeatedly.
     */
    public static void install(MinecraftServer server) {
        PeaceCandleChurchPiece.register();

        // The vanilla pools are registered by these classes' static initializers; init() itself is an
        // empty method that just forces it (the village generator calls it before every village too).
        PlainsVillagePools.init();
        DesertVillagePools.init();
        SavannaVillagePools.init();
        SnowyVillagePools.init();
        TaigaVillagePools.init();

        for (String biome : BIOMES) {
            wrap(server, "village/" + biome + "/houses");
            wrap(server, "village/" + biome + "/zombie/houses");
        }
    }

    private static void wrap(MinecraftServer server, String path) {
        ResourceLocation id = new ResourceLocation(path);
        JigsawPattern vanilla = JigsawManager.REGISTRY.get(id);

        if (vanilla instanceof PeaceCandleChurchPool) {
            return;
        }

        if (vanilla == JigsawPattern.INVALID) {
            LogManager.getLogger().warn("Vanilla pool {} not found, Peace Candle churches won't generate there", id);
            return;
        }

        // only pools that hold a church are wrapped (every element of the pool shows up in a shuffled list)
        for (JigsawPiece element : vanilla.getShuffledPieces(new Random(0))) {
            if (element instanceof SingleJigsawPiece && element.toString().contains(CHURCH)) {
                JigsawManager.REGISTRY.register(new PeaceCandleChurchPool(vanilla, server));
                return;
            }
        }
    }

    /** The same church as a {@link PeaceCandleChurchPiece}, or null if it is not a vanilla church or has no spot for a candle. */
    @SuppressWarnings("unchecked")
    private static JigsawPiece candleVariant(TemplateManager templates, JigsawPiece piece) {
        if (!(piece instanceof SingleJigsawPiece) || !piece.toString().contains(CHURCH)) {
            return null;
        }

        try {
            ResourceLocation id = (ResourceLocation) ELEMENT_LOCATION.get(piece);
            Template loaded = templates.getTemplate(id);
            BlockPos spot = loaded == null ? null : candleSpot(loaded);

            if (spot == null) {
                LogManager.getLogger().warn("No spot for a Peace Candle in {}", piece);
                return null;
            }

            List<StructureProcessor> list = new ArrayList<>((List<StructureProcessor>) ELEMENT_PROCESSORS.get(piece));

            return new PeaceCandleChurchPiece(id.toString(), list, piece.getPlacementBehaviour(), spot);
        } catch (IllegalAccessException e) {
            LogManager.getLogger().error("Could not build the Peace Candle variant of " + piece, e);
            return null;
        }
    }

    /**
     * Where the candle goes: a floor cell inside the building, i.e. an air cell with a full block under it and air
     * above (a cell the template does not list counts as air), a roof block somewhere higher in its column and a wall block within 7 cells in each of the four
     * directions; of those the most central one, then the lowest. Null when the template has none.
     */
    @SuppressWarnings("unchecked")
    public static BlockPos candleSpot(Template template) {
        Map<BlockPos, BlockState> blocks = new HashMap<>();

        try {
            List<List<Template.BlockInfo>> palettes = (List<List<Template.BlockInfo>>) TEMPLATE_PALETTES.get(template);

            if (palettes.isEmpty()) {
                return null;
            }

            for (List<Template.BlockInfo> part : palettes) for (Template.BlockInfo info : part) {
                blocks.put(info.pos, info.state);
            }
        } catch (IllegalAccessException e) {
            return null;
        }

        BlockPos size = template.getSize();
        BlockPos best = null;
        int bestCentral = -1;

        // cells the template does not list are empty (1.14.4 saved no air); bottom layer first, so the lowest of the equally central cells wins
        for (int y = 1; y < size.getY(); y++) {
            for (int x = 0; x < size.getX(); x++) {
                for (int z = 0; z < size.getZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    if (!isEmpty(blocks, pos) || !isEmpty(blocks, pos.up()) || !isFloor(blocks, pos.down()) || !roofed(blocks, pos, size.getY()) || !walled(blocks, pos)) {
                        continue;
                    }

                    int central = Math.min(Math.min(x, size.getX() - 1 - x), Math.min(z, size.getZ() - 1 - z));

                    if (central > bestCentral) {
                        best = pos;
                        bestCentral = central;
                    }
                }
            }
        }

        return best;
    }

    private static boolean isEmpty(Map<BlockPos, BlockState> blocks, BlockPos pos) {
        BlockState state = blocks.get(pos);
        return state == null || state.isAir();
    }

    private static boolean isFloor(Map<BlockPos, BlockState> blocks, BlockPos pos) {
        BlockState state = blocks.get(pos);
        return state != null && net.minecraft.block.Block.isOpaque(state.getCollisionShape(net.minecraft.world.EmptyBlockReader.INSTANCE, BlockPos.ZERO));
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

    /** True if the piece is a village church that gets a candle (also after a save and reload, which rebuilds the piece). */
    public static boolean hasCandle(JigsawPiece piece) {
        return piece instanceof PeaceCandleChurchPiece;
    }

    /** The {@code PeaceCandle} config switch; a supplier so the automated tests can flip it without rewriting the config file. */
    public static java.util.function.BooleanSupplier enabled = () -> RTConfig.PEACE_CANDLE.get();

    private static boolean shouldSwap(Random rand) {
        return enabled.getAsBoolean() && rand.nextInt(3) == 0;
    }

    private synchronized JigsawPiece swap(JigsawPiece piece) {
        if (candleChurches == null) {
            // a throwaway manager that reads the vanilla templates from the (by now loaded) data packs
            TemplateManager templates = new TemplateManager(server, new java.io.File("randomthings_templates_unused"), server.getDataFixer());
            candleChurches = new IdentityHashMap<>();

            for (JigsawPiece element : vanilla.getShuffledPieces(new Random(0))) {
                if (!candleChurches.containsKey(element)) {
                    JigsawPiece variant = candleVariant(templates, element);

                    if (variant != null) {
                        candleChurches.put(element, variant);
                    }
                }
            }
        }

        return candleChurches.getOrDefault(piece, piece);
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

        return pieces.stream().map(this::swap).collect(Collectors.toList());
    }

    @Override
    public int getNumberOfPieces() {
        return vanilla.getNumberOfPieces();
    }
}
