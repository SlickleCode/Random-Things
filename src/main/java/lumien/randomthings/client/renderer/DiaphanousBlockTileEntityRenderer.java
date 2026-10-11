package lumien.randomthings.client.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.tileentity.DiaphanousBlockTileEntity;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.math.vector.Vector3d;

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
public class DiaphanousBlockTileEntityRenderer extends TileEntityRenderer<DiaphanousBlockTileEntity> {
    private final Random random = new Random();

    public DiaphanousBlockTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(DiaphanousBlockTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        BlockState display = te.getDisplayState();

        if (display.getRenderType() != BlockRenderType.MODEL) {
            return;
        }

        Vector3d camera = this.renderDispatcher.renderInfo.getProjectedView();
        double dx = te.getPos().getX() + 0.5 - camera.x;
        double dy = te.getPos().getY() + 0.5 - camera.y;
        double dz = te.getPos().getZ() + 0.5 - camera.z;
        double distance = Math.max(0, dx * dx + dy * dy + dz * dz - 5);

        float curve = -0.5F * ((float) Math.cos(Math.PI * Math.min(Math.max(0, distance - 8), 25) / 25) - 1);
        float alpha = te.isInverted() ? 1 - curve : curve;

        if (alpha <= 0.004F) {
            return;
        }

        World world = te.getWorld();
        BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        IBakedModel model = dispatcher.getModelForState(display);

        IVertexBuilder translucent = new AlphaVertexBuilder(buffer.getBuffer(RenderType.getTranslucent()), alpha);

        dispatcher.getBlockModelRenderer().renderModel(world, model, display, te.getPos(), matrixStack, translucent, true, random, display.getPositionRandom(te.getPos()), combinedOverlay);
    }
}
