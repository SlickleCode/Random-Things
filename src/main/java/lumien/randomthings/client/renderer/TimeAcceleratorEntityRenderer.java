package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import lumien.randomthings.entity.TimeAcceleratorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

/**
 * Draws the spinning wheel of 1.12.2's {@code RenderTimeAccelerator} on every face of the accelerated block: more rings the higher the time rate (see
 * {@link TimeWheel}, which holds the geometry and colours and is the same file on every branch). It replaces the floating Time in a Bottle icons, which the
 * 1.14 port had drawn instead of the wheel.
 */
public class TimeAcceleratorEntityRenderer extends EntityRenderer<TimeAcceleratorEntity> {
    public TimeAcceleratorEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);

        this.shadowSize = 0F;
    }

    @Override
    public void doRender(TimeAcceleratorEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        float progress = 2 * (entity.ticksExisted + partialTicks);
        float[][] faces = TimeWheel.build(entity.getTimeRate(), progress);
        Direction[] directions = Direction.values();

        Minecraft.getInstance().gameRenderer.disableLightmap();
        GlStateManager.disableTexture();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        for (int face = 0; face < 6; face++) {
            Direction direction = directions[face];
            float[] triangles = faces[face];

            GlStateManager.pushMatrix();
            GlStateManager.translated(x, y, z);
            GlStateManager.translated(direction.getXOffset() * TimeWheel.OFFSET, direction.getYOffset() * TimeWheel.OFFSET, direction.getZOffset() * TimeWheel.OFFSET);
            // 1.12.2: rotate(90, z, y, x) - the figure lies in the XZ plane and is turned so that it faces outwards - then spin around its own normal
            GlStateManager.rotatef(90.0F, direction.getZOffset(), direction.getYOffset(), direction.getXOffset());
            GlStateManager.rotatef(progress, 0.0F, 1.0F, 0.0F);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(4, DefaultVertexFormats.POSITION_COLOR);

            for (int i = 0; i + TimeWheel.STRIDE <= triangles.length; i += TimeWheel.STRIDE) {
                buffer.pos(triangles[i], triangles[i + 1], triangles[i + 2]).color(triangles[i + 3], triangles[i + 4], triangles[i + 5], triangles[i + 6]).endVertex();
            }

            tessellator.draw();
            GlStateManager.popMatrix();
        }

        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture();
        Minecraft.getInstance().gameRenderer.enableLightmap();
    }

    @Override
    protected ResourceLocation getEntityTexture(TimeAcceleratorEntity entity) {
        return null;
    }
}
