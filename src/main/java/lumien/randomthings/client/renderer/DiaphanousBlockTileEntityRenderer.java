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

    @Override
    public void render(DiaphanousBlockTileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
        BlockState display = te.getDisplayState();

        if (display.getRenderType() != BlockRenderType.MODEL) {
            return;
        }

        double dx = te.getPos().getX() + 0.5 - TileEntityRendererDispatcher.staticPlayerX;
        double dy = te.getPos().getY() + 0.5 - TileEntityRendererDispatcher.staticPlayerY;
        double dz = te.getPos().getZ() + 0.5 - TileEntityRendererDispatcher.staticPlayerZ;
        double distance = Math.max(0, dx * dx + dy * dy + dz * dz - 5);

        float curve = -0.5F * ((float) Math.cos(Math.PI * Math.min(Math.max(0, distance - 8), 25) / 25) - 1);
        float alpha = te.isInverted() ? 1 - curve : curve;

        if (alpha <= 0.004F) {
            return;
        }

        World world = te.getWorld();
        Minecraft mc = Minecraft.getInstance();
        BlockRendererDispatcher dispatcher = mc.getBlockRendererDispatcher();
        IBakedModel model = dispatcher.getModelForState(display);

        this.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

        GlStateManager.pushMatrix();
        GlStateManager.translated(x - te.getPos().getX(), y - te.getPos().getY(), z - te.getPos().getZ());
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(7, DefaultVertexFormats.BLOCK);

        dispatcher.getBlockModelRenderer().renderModel(world, model, display, te.getPos(), buffer, true, random, display.getPositionRandom(te.getPos()));

        overrideAlpha(buffer, alpha);

        tessellator.draw();

        GlStateManager.popMatrix();
    }

    private static void overrideAlpha(BufferBuilder buffer, float alpha) {
        int alphaByte = MathHelper.clamp((int) (alpha * 255F), 0, 255);
        int vertexCount = buffer.getVertexCount();

        if (vertexCount == 0) {
            return;
        }

        ByteBuffer byteBuffer = buffer.getByteBuffer();
        byteBuffer.order(ByteOrder.nativeOrder());
        boolean littleEndian = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

        for (int v = 0; v < vertexCount; v++) {
            int colorIndex = buffer.getColorIndex(v);
            int packed = byteBuffer.getInt(colorIndex * 4);

            int r, g, b;

            if (littleEndian) {
                r = packed & 0xFF;
                g = (packed >> 8) & 0xFF;
                b = (packed >> 16) & 0xFF;
            } else {
                r = (packed >> 24) & 0xFF;
                g = (packed >> 16) & 0xFF;
                b = (packed >> 8) & 0xFF;
            }

            buffer.putColorRGBA(colorIndex, r, g, b, alphaByte);
        }
    }
}
