package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import lumien.randomthings.entity.TimeAcceleratorEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;

/**
 * Draws the spinning wheel of 1.12.2's {@code RenderTimeAccelerator} on every face of the accelerated block: more rings the higher the time rate (see
 * {@link TimeWheel}, which holds the geometry and colours and is the same file on every branch). It replaces the floating Time in a Bottle icons, which the
 * 1.14 port had drawn instead of the wheel.
 */
public class TimeAcceleratorEntityRenderer extends EntityRenderer<TimeAcceleratorEntity> {
    /**
     * Unlit, untextured POSITION_COLOR quads with the standard alpha blend and no culling. Defined in a RenderType subclass because the render state
     * constants are protected.
     */
    private static abstract class WheelType extends RenderType {
        private WheelType(String name, int bufferSize) {
            super(name, DefaultVertexFormats.POSITION_COLOR, 7, bufferSize, false, false, () -> {}, () -> {});
        }

        static final RenderType WHEEL = makeType("randomthings_time_wheel", DefaultVertexFormats.POSITION_COLOR, 7, 256, false, true,
                RenderType.State.getBuilder().texture(NO_TEXTURE).transparency(TRANSLUCENT_TRANSPARENCY).diffuseLighting(DIFFUSE_LIGHTING_DISABLED).alpha(DEFAULT_ALPHA).depthTest(DEPTH_LEQUAL).cull(CULL_DISABLED).lightmap(LIGHTMAP_DISABLED).build(false));
    }

    public TimeAcceleratorEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);

        this.shadowSize = 0F;
    }

    @Override
    public void render(TimeAcceleratorEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        float progress = 2 * (entity.ticksExisted + partialTicks);
        float[][] faces = TimeWheel.build(entity.getTimeRate(), progress);
        Direction[] directions = Direction.values();
        IVertexBuilder builder = buffer.getBuffer(WheelType.WHEEL);

        for (int face = 0; face < 6; face++) {
            Direction direction = directions[face];
            float[] triangles = faces[face];

            matrixStack.push();
            matrixStack.translate(direction.getXOffset() * TimeWheel.OFFSET, direction.getYOffset() * TimeWheel.OFFSET, direction.getZOffset() * TimeWheel.OFFSET);
            // 1.12.2: rotate(90, z, y, x) - the figure lies in the XZ plane and is turned so that it faces outwards - then spin around its own normal
            matrixStack.rotate(new Vector3f(direction.getZOffset(), direction.getYOffset(), direction.getXOffset()).rotationDegrees(90));
            matrixStack.rotate(Vector3f.YP.rotationDegrees(progress));

            Matrix4f matrix = matrixStack.getLast().getMatrix();

            for (int i = 0; i + 2 * TimeWheel.STRIDE < triangles.length; i += 3 * TimeWheel.STRIDE) {
                // the quad type takes four vertices: the last one is repeated, which leaves a triangle
                for (int v = 0; v < 4; v++) {
                    int at = i + Math.min(v, 2) * TimeWheel.STRIDE;
                    builder.pos(matrix, triangles[at], triangles[at + 1], triangles[at + 2]).color(triangles[at + 3], triangles[at + 4], triangles[at + 5], triangles[at + 6]).endVertex();
                }
            }

            matrixStack.pop();
        }
    }

    @Override
    public ResourceLocation getEntityTexture(TimeAcceleratorEntity entity) {
        return null;
    }
}
