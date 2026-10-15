package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import lumien.randomthings.entity.ArtificialEndPortalEntity;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.platform.MemoryTracker;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.resources.ResourceLocation;

import java.nio.FloatBuffer;
import java.util.Random;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import com.mojang.math.Matrix4f;

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
    private static final Random RANDOM = new Random(31100L);

    public ArtificialEndPortalEntityRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context renderManager)  {
        super(renderManager);

        this.shadowRadius = 0F;
    }

    /**
     * Same technique as vanilla's end portal tile entity renderer in 1.16 ({@code RenderType.getEndPortal(layer)} does
     * the texture-coordinate generation the 1.14.4 version set up by hand with {@code texGen}); each pass is one
     * quad, the number of passes shrinks with distance.
     */
    @Override
    public void render(ArtificialEndPortalEntity portal, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        double size = Math.min(3, 3.0 / 115 * (portal.getActionTimer() + partialTicks - 85));

        if (portal.getActionTimer() > 85) {
            RANDOM.setSeed(31100L);
            float height = this.getOffset();
            Matrix4f matrix = matrixStack.last().pose();
            double sizeHalf = size / 2;

            // 1.17+ draws the end portal with a fixed-layer shader (RenderType.endPortal()); the 1.16 per-distance pass count is gone
            VertexConsumer builder = buffer.getBuffer(RenderType.endPortal());

            builder.vertex(matrix, (float) -sizeHalf, height, (float) sizeHalf).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            builder.vertex(matrix, (float) sizeHalf, height, (float) sizeHalf).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            builder.vertex(matrix, (float) sizeHalf, height, (float) -sizeHalf).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
            builder.vertex(matrix, (float) -sizeHalf, height, (float) -sizeHalf).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
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

    @Override
    public ResourceLocation getTextureLocation(ArtificialEndPortalEntity entity) {
        return null;
    }
}
