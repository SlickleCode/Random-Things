package lumien.randomthings.asm;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.tileentity.LightRedirectorTileEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.client.model.data.IModelData;

/**
 * Same role as {@link AsmHandler} - a plain static redirect target for a
 * coremod transformer - but split into its own class specifically because
 * every parameter type this one method needs ({@link BlockRendererDispatcher},
 * {@link BufferBuilder}, {@link IEnviromentBlockReader}) is {@code @OnlyIn(
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
    public static int renderBlock(BlockRendererDispatcher dispatcher, BlockState state, BlockPos pos, IEnviromentBlockReader world, BufferBuilder buffer, Random rand) {
        synchronized (LightRedirectorTileEntity.REDIRECTORS) {
            if (LightRedirectorTileEntity.REDIRECTORS.isEmpty()) {
                return 2;
            }

            posSet.clear();
            BlockPos switched = getSwitchedPosition(world, pos);

            if (switched.equals(pos)) {
                return 2;
            }

            BlockState switchedState = world.getBlockState(switched);
            BlockRenderType renderType = switchedState.getRenderType();

            if (renderType != BlockRenderType.MODEL) {
                return 0;
            }

            IBakedModel model = dispatcher.getModelForState(switchedState);
            boolean rendered = dispatcher.getBlockModelRenderer().renderModel(world, model, switchedState, pos, buffer, true, rand, switchedState.getPositionRandom(switched), net.minecraftforge.client.model.data.EmptyModelData.INSTANCE);

            return rendered ? 1 : 0;
        }
    }

    private static BlockPos getSwitchedPosition(IEnviromentBlockReader access, BlockPos pos) {
        Iterator<LightRedirectorTileEntity> iterator = LightRedirectorTileEntity.REDIRECTORS.iterator();

        while (iterator.hasNext()) {
            LightRedirectorTileEntity redirector = iterator.next();

            if (redirector.isRemoved()) {
                iterator.remove();
                continue;
            }

            if (!redirector.established || posSet.contains(redirector.getPos())) {
                continue;
            }

            posSet.add(redirector.getPos());

            if (redirector.targets.isEmpty()) {
                for (Direction facing : Direction.values()) {
                    if (redirector.isEnabled(facing)) {
                        redirector.targets.put(redirector.getPos().offset(facing), redirector.getPos().offset(facing.getOpposite()));
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
     * Redirect target for {@code ArmorLayer.renderArmorLayer}'s second (and,
     * for non-leather armor, only) {@code BipedModel.render} call - see
     * {@code ArmorLayerTransformer.js}. Direct port of 1.12.2's own {@code
     * AsmHandler#armorColorHook}: re-applies GL color right before the armor
     * model draws, overriding whatever the vanilla leather-dye branch (or
     * the plain white/undyed case) already set, whenever the worn item
     * carries the Dyeing Machine's {@code rtDye} tag - letting the machine
     * recolor any armor piece, not just vanilla's own dyeable leather. A
     * client-only body (touches {@code GlStateManager}), so - like {@code
     * renderBlock} above - this lives here rather than in the shared {@code
     * AsmHandler}: {@code ArmorLayer} is itself client-only and a dedicated
     * server never loads it, so the injected call is never reached there
     * either.
     */
    public static void armorColorHook(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("rtDye")) {
            int rgb = stack.getTag().getInt("rtDye");

            float r = ((rgb >> 16) & 255) / 255F;
            float g = ((rgb >> 8) & 255) / 255F;
            float b = (rgb & 255) / 255F;

            GlStateManager.color4f(r, g, b, 1.0F);
        }
    }
}
