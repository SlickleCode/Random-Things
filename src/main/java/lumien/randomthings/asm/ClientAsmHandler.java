package lumien.randomthings.asm;

import lumien.randomthings.util.StackTag;
import net.minecraft.nbt.CompoundTag;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.tileentity.LightRedirectorTileEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import com.mojang.blaze3d.vertex.BufferBuilder;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Same role as {@link AsmHandler} - a plain static redirect target for a
 * coremod transformer - but split into its own class specifically because
 * every parameter type this one method needs ({@link BlockRendererDispatcher},
 * {@link BufferBuilder}, {@link IBlockDisplayReader}) is {@code @OnlyIn(
 * Dist.CLIENT)}. {@link AsmHandler} is loaded on both sides (several of its
 * other redirect targets, e.g. {@code overrideRedstonePower}, run on a
 * dedicated server) and only ever references common types in its own method
 * signatures - putting a client-only signature there risks a dedicated
 * server failing to verify/load that class at all the first time anything
 * touches it. This class, by contrast, is only ever referenced from a coremod
 * transform that itself targets a client-only vanilla class ({@code
 * BlockRendererDispatcher}, confirmed {@code @OnlyIn(CLIENT)} via {@code
 * javap}) - a dedicated server never loads that class, so the injected call
 * to this one never gets reached or resolved there either.
 */
public class ClientAsmHandler {
    private static final Set<BlockPos> posSet = new HashSet<>();

    /** The stack ItemRenderer.renderItem / BipedArmorLayer is currently drawing (set and cleared by the coremods); decides the enchant-glint tint. */
    private static ItemStack renderingStack = ItemStack.EMPTY;

    public static void setRenderingStack(ItemStack stack) {
        renderingStack = stack == null ? ItemStack.EMPTY : stack;
    }

    public static void setRenderingStackFromSlot(net.minecraft.world.entity.LivingEntity entity, net.minecraft.world.entity.EquipmentSlot slot) {
        renderingStack = entity.getItemBySlot(slot);
    }

    /** Head of {@code ItemStackRenderState.render} (ItemStackRenderTransformer): the stack this state was built from decides its glint tint. */
    public static void beginItemRender(net.minecraft.client.renderer.item.ItemStackRenderState state) {
        renderingStack = glintStacks.getOrDefault(state, ItemStack.EMPTY);
    }

    private static final java.util.Map<net.minecraft.client.renderer.item.ItemStackRenderState, ItemStack> glintStacks = new java.util.WeakHashMap<>();

    public static void clearRenderingStack() {
        renderingStack = ItemStack.EMPTY;
    }

    /**
     * Port of 1.12.2's {@code enchantmentColorHook}/{@code armorEnchantmentHook} for 1.16: called from the {@code RenderType} glint getters by the
     * {@code RenderTypeTransformer} coremod. Returns the glint type to use for the stack being drawn: the original, or a tinted wrapper when
     * {@link GlintColors} gives that stack a colour other than vanilla's.
     */
    public static net.minecraft.client.renderer.RenderType tintGlint(net.minecraft.client.renderer.RenderType original) {
        int color = GlintColors.colorFor(renderingStack, GlintColors.VANILLA);

        if (color != GlintColors.VANILLA) {
            lumien.randomthings.debug.HookProbe.hit("glint_tint_applied");
        }

        return color == GlintColors.VANILLA ? original : lumien.randomthings.client.renderer.TintedGlintRenderType.of(original, color);
    }

    /**
     * Redirect target for {@code BlockRendererDispatcher.renderBlock}'s
     * method entry (the 6-arg, real - not the deprecated 5-arg {@code
     * func_215330_a} - overload; confirmed via source that this is the one
     * {@code ChunkRender} actually calls) - see {@code
     * BlockRendererDispatcherTransformer.js}. Direct port of 1.12.2's own
     * {@code AsmHandler#renderBlock}/{@code #getSwitchedPosition}, adapted to
     * this version's {@code boolean}-returning method (1.12.2's returned
     * {@code int}, with {@code LIQUID} as a third case - {@link
     * BlockRenderType} only has 3 values here, none of them {@code LIQUID}
     * anymore, fluids render through a completely separate path in this
     * version). Same "sentinel value that means let the original method body
     * run" trick as 1.12.2's, translated to this version's return type: this
     * returns {@code 2} for "not a redirected position, run vanilla
     * normally," or the real {@code 0}/{@code 1} boolean answer (as an int,
     * since a JVM {@code boolean} return is just a 0/1 {@code IRETURN}
     * either way) when a redirect applies - see the transformer JS for the
     * exact bytecode that interprets this.
     */
    public static int renderBlock(BlockRenderDispatcher dispatcher, BlockState state, BlockPos pos, BlockAndTintGetter world, PoseStack matrixStack, VertexConsumer buffer, boolean checkSides, net.minecraft.util.RandomSource rand) {
        synchronized (LightRedirectorTileEntity.REDIRECTORS) {
            if (LightRedirectorTileEntity.REDIRECTORS.isEmpty()) {
                return 2;
            }

            posSet.clear();
            BlockPos switched = getSwitchedPosition(world, pos);

            if (switched.equals(pos)) {
                return 2;
            }

            lumien.randomthings.debug.HookProbe.hit("light_redirect_applied");
            BlockState switchedState = world.getBlockState(switched);
            RenderShape renderType = switchedState.getRenderShape();

            if (renderType != RenderShape.MODEL) {
                return 0;
            }

            BakedModel model = dispatcher.getBlockModel(switchedState);
            dispatcher.getModelRenderer().tesselateBlock(world, model, switchedState, pos, matrixStack, buffer, true, rand, switchedState.getSeed(switched), net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);

            // 1.19: tesselateBlock/renderBatched return void; the coremod protocol is reworked with the transformers (plan M5)
            return 1;
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

    /**
     * Replaces the colour (ARGB int) of the armor draw (see {@code ArmorLayerTransformer.js}) with the stack's {@code rtDye}
     * colour when it has one. Lets the Dyeing Machine recolor any armor piece, not just vanilla's dyeable leather.
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
    private static java.lang.reflect.Field layerModelField;

    /**
     * Redirect target for {@code ItemModelResolver.appendItemLayers} (see {@code ItemRendererTransformer.js}): when the stack carries the Dyeing
     * Machine colour ({@code rtDye}), every layer of its render state is drawn in that colour. 1.12.2 did this per quad regardless of the model's
     * tint index; 1.21.4's item renderer only knows tint arrays, so the layer's model is wrapped to give all its quads tint index 0 and tint 0 is set.
     */
    public static void dyeItemLayers(net.minecraft.client.renderer.item.ItemStackRenderState state, ItemStack stack) {
        // the enchant glint is chosen later, inside ItemStackRenderState.render, which no longer sees the stack: remember it per render state
        if (!stack.isEmpty() && GlintColors.colorFor(stack, GlintColors.VANILLA) != GlintColors.VANILLA) {
            glintStacks.put(state, stack);
        } else {
            glintStacks.remove(state);
        }

        if (stack.isEmpty() || !StackTag.contains(stack, "rtDye")) {
            return;
        }

        try {
            if (layersField == null) {
                layersField = net.minecraft.client.renderer.item.ItemStackRenderState.class.getDeclaredField("layers");
                activeLayerCountField = net.minecraft.client.renderer.item.ItemStackRenderState.class.getDeclaredField("activeLayerCount");
                layerModelField = net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState.class.getDeclaredField("model");
                layersField.setAccessible(true);
                activeLayerCountField.setAccessible(true);
                layerModelField.setAccessible(true);
            }

            Object[] layers = (Object[]) layersField.get(state);
            int active = activeLayerCountField.getInt(state);
            int color = StackTag.getInt(stack, "rtDye") | 0xFF000000;

            for (int i = 0; i < active; i++) {
                net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState layer = (net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState) layers[i];
                Object model = layerModelField.get(layer);

                if (model instanceof BakedModel baked && !(model instanceof lumien.randomthings.client.DyedModel)) {
                    layerModelField.set(layer, new lumien.randomthings.client.DyedModel(baked));
                }

                java.util.Arrays.fill(layer.prepareTintLayers(1), color);
                lumien.randomthings.debug.HookProbe.hit("item_dye_applied");
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot dye the item layers", e);
        }
    }
}
