package lumien.randomthings.asm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.tileentity.LightRedirectorTileEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
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

            if (switched != null && access.getBlockState(switched).getMaterial() != Material.AIR) {
                return getSwitchedPosition(access, switched);
            }
        }

        return pos;
    }

    /**
     * Redirect target for {@code BipedArmorLayer}'s leather-branch {@code IDyeableArmorItem.getColor(stack)} call - see
     * {@code ArmorLayerTransformer.js}. Returns the Dyeing Machine's {@code rtDye} colour when the stack carries one,
     * so a dyed leather piece shows the machine colour instead of its vanilla dye colour (as 1.12.2's
     * {@code armorColorHook} overrode it for every armor piece).
     */
    public static int dyedColor(net.minecraft.world.item.DyeableLeatherItem item, ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("rtDye")) {
            return stack.getTag().getInt("rtDye");
        }

        return item.getColor(stack);
    }

    /**
     * Replaces the constant {@code 1.0F} colour channels of the plain (non-leather) armor draw: channel 0/1/2 =
     * red/green/blue of the stack's {@code rtDye} colour, or {@code defaultValue} when it has none. Lets the machine
     * recolor any armor piece, not just vanilla's dyeable leather. Colour is now a render argument rather than GL state.
     */
    public static float armorChannel(ItemStack stack, int channel, float defaultValue) {
        if (stack.hasTag() && stack.getTag().contains("rtDye")) {
            if (channel == 0) {
                lumien.randomthings.debug.HookProbe.hit("armor_dye_applied");
            }
            int rgb = stack.getTag().getInt("rtDye");
            int shift = channel == 0 ? 16 : (channel == 1 ? 8 : 0);

            return ((rgb >> shift) & 255) / 255F;
        }

        return defaultValue;
    }
}
