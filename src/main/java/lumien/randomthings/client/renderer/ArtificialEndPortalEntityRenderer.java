package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import lumien.randomthings.entity.ArtificialEndPortalEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import java.nio.FloatBuffer;
import java.util.Random;

/**
 * The swirling end-portal starfield effect, keyed off {@code actionTimer}
 * instead of a tile entity - a near-verbatim port of vanilla's own {@code
 * EndPortalTileEntityRenderer} (which 1.12.2's {@code RenderEndPortal}/this
 * mod's own {@code RenderArtificialEndPortal} were themselves copied from),
 * still built on the same fixed-function {@code GL_TEXTURE_GEN} tricks in
 * this Forge version - ground-truthed directly against vanilla's own
 * (unchanged) renderer bytecode rather than guessed at, since the immediate-
 * mode GL surface this needs is otherwise fully deprecated.
 */
public class ArtificialEndPortalEntityRenderer extends EntityRenderer<ArtificialEndPortalEntity> {
    private static final ResourceLocation END_SKY_TEXTURE = new ResourceLocation("textures/environment/end_sky.png");
    private static final ResourceLocation END_PORTAL_TEXTURE = new ResourceLocation("textures/entity/end_portal.png");
    private static final Random RANDOM = new Random(31100L);
    private static final FloatBuffer MODELVIEW = GLAllocation.createDirectFloatBuffer(16);
    private static final FloatBuffer PROJECTION = GLAllocation.createDirectFloatBuffer(16);
    private final FloatBuffer buffer = GLAllocation.createDirectFloatBuffer(16);

    public ArtificialEndPortalEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);

        this.shadowSize = 0F;
    }

    @Override
    public void doRender(ArtificialEndPortalEntity portal, double x, double y, double z, float entityYaw, float partialTicks) {
        double size = Math.min(3, 3.0 / 115 * (portal.getActionTimer() + partialTicks - 85));

        if (portal.getActionTimer() > 85) {
            GlStateManager.disableLighting();
            RANDOM.setSeed(31100L);
            GlStateManager.getMatrix(2982, MODELVIEW);
            GlStateManager.getMatrix(2983, PROJECTION);
            double d0 = x * x + y * y + z * z;
            int i = this.getPasses(d0);
            float f = this.getOffset();
            boolean flag = false;

            for (int j = 0; j < i; ++j) {
                GlStateManager.pushMatrix();
                float f1 = 2.0F / (18 - j);

                if (j == 0) {
                    this.bindTexture(END_SKY_TEXTURE);
                    f1 = 0.15F;
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                }

                if (j >= 1) {
                    this.bindTexture(END_PORTAL_TEXTURE);
                    flag = true;
                    Minecraft.getInstance().gameRenderer.setupFogColor(true);
                }

                if (j == 1) {
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                }

                GlStateManager.enableTexGen(GlStateManager.TexGen.S);
                GlStateManager.enableTexGen(GlStateManager.TexGen.T);
                GlStateManager.enableTexGen(GlStateManager.TexGen.R);
                GlStateManager.texGenMode(GlStateManager.TexGen.S, 9216);
                GlStateManager.texGenMode(GlStateManager.TexGen.T, 9216);
                GlStateManager.texGenMode(GlStateManager.TexGen.R, 9216);
                GlStateManager.texGenParam(GlStateManager.TexGen.S, 9474, this.getBuffer(1.0F, 0.0F, 0.0F, 0.0F));
                GlStateManager.texGenParam(GlStateManager.TexGen.T, 9474, this.getBuffer(0.0F, 1.0F, 0.0F, 0.0F));
                GlStateManager.texGenParam(GlStateManager.TexGen.R, 9474, this.getBuffer(0.0F, 0.0F, 1.0F, 0.0F));
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(5890);
                GlStateManager.pushMatrix();
                GlStateManager.loadIdentity();
                GlStateManager.translatef(0.5F, 0.5F, 0.0F);
                GlStateManager.scalef(0.5F, 0.5F, 1.0F);
                float f2 = j + 1;
                GlStateManager.translatef(17.0F / f2, (2.0F + f2 / 1.5F) * (System.currentTimeMillis() % 800000L / 800000.0F), 0.0F);
                GlStateManager.rotatef((f2 * f2 * 4321.0F + f2 * 9.0F) * 2.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.scalef(4.5F - f2 / 4.0F, 4.5F - f2 / 4.0F, 1.0F);
                GlStateManager.multMatrix(PROJECTION);
                GlStateManager.multMatrix(MODELVIEW);

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuffer();
                bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
                float f3 = (RANDOM.nextFloat() * 0.5F + 0.1F) * f1;
                float f4 = (RANDOM.nextFloat() * 0.5F + 0.4F) * f1;
                float f5 = (RANDOM.nextFloat() * 0.5F + 0.5F) * f1;

                double sizeHalf = size / 2;

                bufferbuilder.pos(x - sizeHalf, y + f, z + sizeHalf).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x + sizeHalf, y + f, z + sizeHalf).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x + sizeHalf, y + f, z - sizeHalf).color(f3, f4, f5, 1.0F).endVertex();
                bufferbuilder.pos(x - sizeHalf, y + f, z - sizeHalf).color(f3, f4, f5, 1.0F).endVertex();

                tessellator.draw();
                GlStateManager.popMatrix();
                GlStateManager.matrixMode(5888);
                this.bindTexture(END_SKY_TEXTURE);
            }

            GlStateManager.disableBlend();
            GlStateManager.disableTexGen(GlStateManager.TexGen.S);
            GlStateManager.disableTexGen(GlStateManager.TexGen.T);
            GlStateManager.disableTexGen(GlStateManager.TexGen.R);
            GlStateManager.enableLighting();

            if (flag) {
                Minecraft.getInstance().gameRenderer.setupFogColor(false);
            }
        }
    }

    protected int getPasses(double distanceSq) {
        if (distanceSq > 36864.0D) {
            return 1;
        } else if (distanceSq > 25600.0D) {
            return 3;
        } else if (distanceSq > 16384.0D) {
            return 5;
        } else if (distanceSq > 9216.0D) {
            return 7;
        } else if (distanceSq > 4096.0D) {
            return 9;
        } else if (distanceSq > 1024.0D) {
            return 11;
        } else if (distanceSq > 576.0D) {
            return 13;
        } else if (distanceSq > 256.0D) {
            return 14;
        } else {
            return 15;
        }
    }

    protected float getOffset() {
        return 1F;
    }

    private FloatBuffer getBuffer(float p1, float p2, float p3, float p4) {
        this.buffer.clear();
        this.buffer.put(p1).put(p2).put(p3).put(p4);
        this.buffer.flip();
        return this.buffer;
    }

    @Override
    protected ResourceLocation getEntityTexture(ArtificialEndPortalEntity entity) {
        return null;
    }
}
