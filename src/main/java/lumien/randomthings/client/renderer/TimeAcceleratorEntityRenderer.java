package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import lumien.randomthings.entity.TimeAcceleratorEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import org.joml.Quaternionf;

/**
 * Draws the spinning wheel of 1.12.2's {@code RenderTimeAccelerator} on every face of the accelerated block: more rings the higher the time rate (see
 * {@link TimeWheel}, which holds the geometry and colours and is the same file on every branch). It replaces the floating Time in a Bottle icons, which the
 * 1.14 port had drawn instead of the wheel.
 */
public class TimeAcceleratorEntityRenderer extends EntityRenderer<TimeAcceleratorEntity, TimeAcceleratorEntityRenderer.State> {
    public static class State extends net.minecraft.client.renderer.entity.state.EntityRenderState {
        public int timeRate;
    }

    public TimeAcceleratorEntityRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context renderManager) {
        super(renderManager);

        this.shadowRadius = 0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TimeAcceleratorEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        state.timeRate = entity.getTimeRate();
    }

    @Override
    public void submit(State state, PoseStack matrixStack, net.minecraft.client.renderer.SubmitNodeCollector collector, net.minecraft.client.renderer.state.CameraRenderState camera) {
        float progress = 2 * state.ageInTicks;
        float[][] faces = TimeWheel.build(state.timeRate, progress);
        Direction[] directions = Direction.values();

        for (int face = 0; face < 6; face++) {
            Direction direction = directions[face];
            float[] triangles = faces[face];

            matrixStack.pushPose();
            matrixStack.translate(direction.getStepX() * TimeWheel.OFFSET, direction.getStepY() * TimeWheel.OFFSET, direction.getStepZ() * TimeWheel.OFFSET);
            // 1.12.2: rotate(90, z, y, x) - the figure lies in the XZ plane and is turned so that it faces outwards - then spin around its own normal
            matrixStack.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(90), direction.getStepZ(), direction.getStepY(), direction.getStepX()));
            matrixStack.mulPose(Axis.YP.rotationDegrees(progress));

            collector.submitCustomGeometry(matrixStack, RenderTypes.debugQuads(), (pose, builder) -> {
                for (int i = 0; i + 2 * TimeWheel.STRIDE < triangles.length; i += 3 * TimeWheel.STRIDE) {
                    // the quad type takes four vertices: the last one is repeated, which leaves a triangle
                    for (int v = 0; v < 4; v++) {
                        int at = i + Math.min(v, 2) * TimeWheel.STRIDE;
                        builder.addVertex(pose, triangles[at], triangles[at + 1], triangles[at + 2]).setColor(triangles[at + 3], triangles[at + 4], triangles[at + 5], triangles[at + 6]);
                    }
                }
            });

            matrixStack.popPose();
        }
    }
}
