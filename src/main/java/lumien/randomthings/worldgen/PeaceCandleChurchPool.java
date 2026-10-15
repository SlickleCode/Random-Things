package lumien.randomthings.worldgen;

import net.minecraft.util.RandomSource;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;

import lumien.randomthings.config.RTConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Holder;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraft.core.Registry;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
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
public class PeaceCandleChurchPool extends StructureTemplatePool {
    private static final ResourceLocation HOUSES = new ResourceLocation("village/plains/houses");

    /** The vanilla plains church template, matched on its piece description (the template name is not otherwise readable). */
    private static final String VANILLA_CHURCH = "village/plains/houses/plains_temple_4";

    // RIGID matches what the vanilla plains village registers the houses pool with.
    // The piece's (empty) processor list must be this world's own registry holder, otherwise the jigsaw assembler
    // rejects the piece as "not valid in current registry set"; hence one instance per installed pool, not a constant.
    private final StructurePoolElement peaceCandleChurch;

    private final StructureTemplatePool vanilla;

    private PeaceCandleChurchPool(StructureTemplatePool vanilla, StructurePoolElement peaceCandleChurch) {
        super(vanilla.getName(), vanilla.getFallback(), java.util.Collections.<Pair<StructurePoolElement, Integer>>emptyList());
        this.vanilla = vanilla;
        this.peaceCandleChurch = peaceCandleChurch;
    }

    /**
     * Wraps the vanilla plains houses pool in this world's dynamic registry (1.16 loads jigsaw pools as data, so the
     * 1.14.4 static-registry replacement is gone). Called from {@code ServerAboutToStartEvent}, which fires once
     * per world load, so it is safe to call repeatedly.
     */
    public static void install(MinecraftServer server) {
        Registry<StructureTemplatePool> registry = server.registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        Holder<StructureTemplatePool> holder = registry.getHolder(ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, HOUSES)).orElse(null);

        if (holder == null) {
            LogManager.getLogger().warn("Vanilla pool {} not found, Peace Candle churches won't generate", HOUSES);
            return;
        }

        StructureTemplatePool vanilla = holder.value();

        if (vanilla instanceof PeaceCandleChurchPool) {
            return;
        }

        try {
            // the pool registry is frozen by the time the server starts, so the holder's value is swapped in place instead
            Holder<StructureProcessorList> noProcessors = server.registryAccess().registryOrThrow(Registry.PROCESSOR_LIST_REGISTRY).getHolderOrThrow(ResourceKey.create(Registry.PROCESSOR_LIST_REGISTRY, new ResourceLocation("empty")));
            StructurePoolElement church = StructurePoolElement.single("randomthings:peace_candle_church", noProcessors).apply(StructureTemplatePool.Projection.RIGID);

            HOLDER_VALUE.set(holder, new PeaceCandleChurchPool(vanilla, church));
        } catch (IllegalAccessException e) {
            LogManager.getLogger().error("Could not install the Peace Candle church pool", e);
        }
    }

    private static final java.lang.reflect.Field HOLDER_VALUE = ObfuscationReflectionHelper.findField(Holder.Reference.class, "f_205752_");

    /** The {@code PeaceCandle} config switch; a supplier so the automated tests can flip it without rewriting the config file. */
    public static java.util.function.BooleanSupplier enabled = () -> RTConfig.PEACE_CANDLE.get();

    private static boolean shouldSwap(RandomSource rand) {
        return enabled.getAsBoolean() && rand.nextInt(3) == 0;
    }

    private StructurePoolElement swap(StructurePoolElement piece) {
        return piece.toString().contains(VANILLA_CHURCH) ? peaceCandleChurch : piece;
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
