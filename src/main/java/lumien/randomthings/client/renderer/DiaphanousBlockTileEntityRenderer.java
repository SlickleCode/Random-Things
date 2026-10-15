package lumien.randomthings.client.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.tileentity.DiaphanousBlockTileEntity;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

/**
 * Draws {@link DiaphanousBlockTileEntity}'s displayed block as a real baked
 * model (full per-vertex lighting/AO from {@code BlockModelRenderer
 * .renderModel} itself, real face culling against solid neighbors for free
 * via its {@code checkSides} parameter - see {@link DiaphanousBlockTileEntity}'s
 * own javadoc for why 1.12.2's manual {@code renderMap} tracking isn't needed
 * here), then overrides every vertex's alpha to a value computed from
 * distance to the camera - close up it's mostly invisible, fading to fully
 * opaque-looking past 33 blocks, the opposite curve when {@link
 * DiaphanousBlockTileEntity#isInverted()}. Direct port of 1.12.2's {@code
 * RenderBlockDiaphanous}'s fade math.
 * <p>
 * 1.12.2 achieved the per-vertex alpha override by reflecting into {@code
 * BufferBuilder}'s then-private raw {@code IntBuffer} field. This version's
 * {@code BufferBuilder} exposes the same raw storage publicly instead (
 * {@link BufferBuilder#getByteBuffer()} plus the already-public {@link
 * BufferBuilder#getColorIndex(int)}/{@link BufferBuilder#putColorRGBA(int,
 * int, int, int, int)}) - ground-truthed from its own decompiled source
 * ({@code getColorIndex} returns an int-buffer index, matching exactly what
 * 1.12.2's reflected field expected), so {@link #overrideAlpha} needs no
 * reflection at all, unlike this port's usual fallback pattern (see {@code
 * SuperLubricentPhysics}'s {@code deltaRotation} field) for when no public
 * equivalent exists.
 */
public class DiaphanousBlockTileEntityRenderer implements BlockEntityRenderer<DiaphanousBlockTileEntity> {
    private final net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create();

    public DiaphanousBlockTileEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(DiaphanousBlockTileEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        BlockState display = te.getDisplayState();

        if (display.getRenderShape() != RenderShape.MODEL) {
            return;
        }

        Vec3 camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        double dx = te.getBlockPos().getX() + 0.5 - camera.x;
        double dy = te.getBlockPos().getY() + 0.5 - camera.y;
        double dz = te.getBlockPos().getZ() + 0.5 - camera.z;
        double distance = Math.max(0, dx * dx + dy * dy + dz * dz - 5);

        float curve = -0.5F * ((float) Math.cos(Math.PI * Math.min(Math.max(0, distance - 8), 25) / 25) - 1);
        float alpha = te.isInverted() ? 1 - curve : curve;

        if (alpha <= 0.004F) {
            return;
        }

        Level world = te.getLevel();
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
        BakedModel model = dispatcher.getBlockModel(display);

        VertexConsumer translucent = new AlphaVertexBuilder(buffer.getBuffer(RenderType.translucent()), alpha);

        dispatcher.getModelRenderer().tesselateBlock(world, model, display, te.getBlockPos(), matrixStack, translucent, true, random, display.getSeed(te.getBlockPos()), combinedOverlay);
    }
}
