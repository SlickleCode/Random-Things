package lumien.randomthings.asm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lumien.randomthings.tileentity.LightRedirectorTileEntity;
import lumien.randomthings.util.StackTag;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Same role as {@link AsmHandler} - a plain static redirect target for the class transformers - but split into its own class because every parameter type
 * these methods need is client-only. {@link AsmHandler} is loaded on both sides (several of its redirect targets run on a dedicated server) and only ever
 * references common types in its own method signatures; this class is only referenced from transformers that target client-only vanilla classes, which a
 * dedicated server never loads.
 */
public class ClientAsmHandler {
    private static final Set<BlockPos> posSet = new HashSet<>();

    /**
     * Redirect target for the head of {@code BlockRenderDispatcher.renderBatched(state, pos, level, poseStack, layerFunction, checkSides, parts)} (see
     * {@code RTTransformers.BlockRendererDispatcher}): when a Light Redirector sends the block at {@code pos} to another position, the model of that other
     * block is drawn here and vanilla's own draw is skipped. Direct port of 1.12.2's {@code AsmHandler#renderBlock}/{@code #getSwitchedPosition}.
     *
     * @return true when the block was drawn here
     */
    public static boolean renderBlock(BlockRenderDispatcher dispatcher, BlockState state, BlockPos pos, BlockAndTintGetter world, PoseStack matrixStack, Function<ChunkSectionLayer, VertexConsumer> layers, boolean checkSides,
            List<BlockModelPart> parts) {
        synchronized (LightRedirectorTileEntity.REDIRECTORS) {
            if (LightRedirectorTileEntity.REDIRECTORS.isEmpty()) {
                return false;
            }

            posSet.clear();
            BlockPos switched = getSwitchedPosition(world, pos);

            if (switched.equals(pos)) {
                return false;
            }

            lumien.randomthings.debug.HookProbe.hit("light_redirect_applied");
            BlockState switchedState = world.getBlockState(switched);

            if (switchedState.getRenderShape() != RenderShape.MODEL) {
                return true;
            }

            net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create();
            random.setSeed(switchedState.getSeed(switched));
            List<BlockModelPart> switchedParts = dispatcher.getBlockModel(switchedState).collectParts(random);
            dispatcher.getModelRenderer().tesselateBlock(world, switchedParts, switchedState, pos, matrixStack, layers, true, OverlayTexture.NO_OVERLAY);
            return true;
        }
    }

    private static BlockPos getSwitchedPosition(BlockAndTintGetter access, BlockPos pos) {
        Iterator<LightRedirectorTileEntity> iterator = LightRedirectorTileEntity.REDIRECTORS.iterator();

        while (iterator.hasNext()) {
            LightRedirectorTileEntity redirector = iterator.next();

            if (redirector.isRemoved()) {
                iterator.remove();
                continue;
            }

            if (!redirector.established || posSet.contains(redirector.getBlockPos())) {
                continue;
            }

            posSet.add(redirector.getBlockPos());

            if (redirector.targets.isEmpty()) {
                for (Direction facing : Direction.values()) {
                    if (redirector.isEnabled(facing)) {
                        redirector.targets.put(redirector.getBlockPos().relative(facing), redirector.getBlockPos().relative(facing.getOpposite()));
                    }
                }
            }

            BlockPos switched = redirector.targets.get(pos);

            if (switched != null && !access.getBlockState(switched).isAir()) {
                return getSwitchedPosition(access, switched);
            }
        }

        return pos;
    }

    /** The worn stack being drawn by {@code EquipmentLayerRenderer.renderLayers} (set at its head by the armor transformer, read by {@link #armorTintColor}). */
    private static ItemStack renderingStack = ItemStack.EMPTY;

    /** The enchant-glint colour of what is being drawn right now ({@link GlintColors#VANILLA} for none); read by {@link #tintGlint}. */
    private static int glintColor = GlintColors.VANILLA;

    /** Marks the two extra entries {@link #dyeItemLayers} appends to a layer's tint array: {@code [..., GLINT_MARKER, colour]}. */
    private static final int GLINT_MARKER = 0x52544C47;

    public static void setRenderingStack(ItemStack stack) {
        renderingStack = stack == null ? ItemStack.EMPTY : stack;
        glintColor = GlintColors.colorFor(renderingStack, GlintColors.VANILLA);
    }

    public static void clearRenderingStack() {
        renderingStack = ItemStack.EMPTY;
        glintColor = GlintColors.VANILLA;
    }

    /** Head of {@code ItemRenderer.renderItem} (item submit): a layer built for a stack with a custom glint carries its colour in the tint array. */
    public static void beginItemGlint(int[] tintLayers) {
        glintColor = tintLayers.length >= 2 && tintLayers[tintLayers.length - 2] == GLINT_MARKER ? tintLayers[tintLayers.length - 1] : GlintColors.VANILLA;
    }

    /**
     * Port of 1.12.2's {@code enchantmentColorHook}/{@code armorEnchantmentHook}: called by the transformer of the {@code RenderTypes} glint getters.
     * Returns the glint type to use for what is being drawn: the original, or a tinted copy when {@link GlintColors} gives it a colour other than vanilla's.
     */
    public static net.minecraft.client.renderer.rendertype.RenderType tintGlint(net.minecraft.client.renderer.rendertype.RenderType original) {
        if (glintColor == GlintColors.VANILLA) {
            return original;
        }

        lumien.randomthings.debug.HookProbe.hit(renderingStack.isEmpty() ? "glint_tint_applied" : "glint_tint_armor");
        return lumien.randomthings.client.renderer.TintedGlintRenderType.of(original, glintColor);
    }

    /** Hook of {@code RenderType.draw}: the colour modulator uniform (white) of a tinted glint copy is its tint. */
    public static org.joml.Vector4f modulate(org.joml.Vector4f white, net.minecraft.client.renderer.rendertype.RenderType type) {
        return lumien.randomthings.client.renderer.TintedGlintRenderType.modulatorOf(white, type);
    }

    /**
     * Replaces the colour (ARGB int) of the armor draw (see {@code RTTransformers.ArmorLayer}) with the stack's {@code rtDye} colour when it has one. Lets
     * the Dyeing Machine recolor any armor piece, not just vanilla's dyeable leather.
     */
    public static int armorTintColor(int color) {
        if (color != 0 && renderingStack != null && StackTag.contains(renderingStack, "rtDye")) {
            lumien.randomthings.debug.HookProbe.hit("armor_dye_applied");
            return StackTag.getInt(renderingStack, "rtDye") | 0xFF000000;
        }

        return color;
    }

    private static java.lang.reflect.Field layersField;
    private static java.lang.reflect.Field activeLayerCountField;
    private static java.lang.reflect.Field tintLayersField;

    /**
     * Redirect target for {@code ItemModelResolver.appendItemLayers} (see {@code RTTransformers.ItemRenderer}): when the stack carries the Dyeing Machine
     * colour ({@code rtDye}), every layer of its render state is drawn in that colour. 1.12.2 did this per quad regardless of the model's tint index; the
     * item renderer only knows tint arrays, so every quad of the layer gets tint index 0 and tint 0 is set to the colour.
     */
    public static void dyeItemLayers(net.minecraft.client.renderer.item.ItemStackRenderState state, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }

        int glint = GlintColors.colorFor(stack, GlintColors.VANILLA);
        boolean dye = StackTag.contains(stack, "rtDye");

        if (glint == GlintColors.VANILLA && !dye) {
            return;
        }

        // GUI items are drawn into a cache keyed by this identity: a dyed or glint-tinted item must not share the entry of the plain one
        if (dye) {
            state.appendModelIdentityElement(StackTag.getInt(stack, "rtDye"));
        }

        if (glint != GlintColors.VANILLA) {
            state.appendModelIdentityElement(glint);
        }

        try {
            if (layersField == null) {
                layersField = net.minecraft.client.renderer.item.ItemStackRenderState.class.getDeclaredField("layers");
                activeLayerCountField = net.minecraft.client.renderer.item.ItemStackRenderState.class.getDeclaredField("activeLayerCount");
                layersField.setAccessible(true);
                activeLayerCountField.setAccessible(true);
            }

            Object[] layers = (Object[]) layersField.get(state);
            int active = activeLayerCountField.getInt(state);
            int color = StackTag.getInt(stack, "rtDye") | 0xFF000000;

            if (tintLayersField == null) {
                tintLayersField = net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState.class.getDeclaredField("tintLayers");
                tintLayersField.setAccessible(true);
            }

            for (int i = 0; i < active; i++) {
                net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState layer = (net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState) layers[i];

                if (!dye) {
                    appendGlintColor(layer, glint);
                    continue;
                }

                List<BakedQuad> quads = layer.prepareQuadList();

                for (int q = 0; q < quads.size(); q++) {
                    BakedQuad quad = quads.get(q);

                    if (quad.tintIndex() != 0) {
                        quads.set(q, new BakedQuad(quad.position0(), quad.position1(), quad.position2(), quad.position3(), quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(), 0, quad.direction(), quad.sprite(),
                                quad.shade(), quad.lightEmission(), quad.bakedNormals(), quad.bakedColors(), quad.hasAmbientOcclusion()));
                    }
                }

                layer.prepareTintLayers(1)[0] = color;

                if (glint != GlintColors.VANILLA) {
                    appendGlintColor(layer, glint);
                }

                lumien.randomthings.debug.HookProbe.hit("item_dye_applied");
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot dye the item layers", e);
        }
    }

    /** Appends {@code [GLINT_MARKER, colour]} to the tint array of the layer (the array is the only per-layer data that reaches {@code ItemRenderer.renderItem}). */
    private static void appendGlintColor(net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState layer, int glint) throws ReflectiveOperationException {
        int[] old = (int[]) tintLayersField.get(layer);

        // the marker must not sit on a tint index the quads use (a plain item's quads use index 0): pad with white up to the highest one
        int base = old.length;

        for (BakedQuad quad : layer.prepareQuadList()) {
            base = Math.max(base, quad.tintIndex() + 1);
        }

        int[] extended = new int[base + 2];

        java.util.Arrays.fill(extended, -1);
        System.arraycopy(old, 0, extended, 0, old.length);
        extended[base] = GLINT_MARKER;
        extended[base + 1] = glint;
        tintLayersField.set(layer, extended);
    }
}
