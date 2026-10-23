package lumien.randomthings.client.renderer;

import lumien.randomthings.entity.GoldenChickenEntity;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.model.ChickenModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Reuses vanilla's {@code ChickenModel} wholesale (same hitbox/proportions as
 * the original's own reuse of {@code ModelChicken}), just with the Golden
 * Chicken's own texture and wing-flap animation values. Direct port of
 * 1.12.2's {@code RenderGoldenChicken}.
 */
public class GoldenChickenEntityRenderer extends MobRenderer<GoldenChickenEntity, net.minecraft.client.renderer.entity.state.ChickenRenderState, ChickenModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/entity/golden_chicken.png");

    public GoldenChickenEntityRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context renderManager) {
        super(renderManager, new ChickenModel(renderManager.bakeLayer(net.minecraft.client.model.geom.ModelLayers.CHICKEN)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(net.minecraft.client.renderer.entity.state.ChickenRenderState state) {
        return TEXTURE;
    }

    @Override
    public net.minecraft.client.renderer.entity.state.ChickenRenderState createRenderState() {
        return new net.minecraft.client.renderer.entity.state.ChickenRenderState();
    }

    /** The vanilla chicken model derives the wing angle {@code (sin(flap) + 1) * flapSpeed} (this renderer's former getBob) from these two values. */
    @Override
    public void extractRenderState(GoldenChickenEntity entity, net.minecraft.client.renderer.entity.state.ChickenRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        state.flap = Mth.lerp(partialTicks, entity.oFlap, entity.wingRotation);
        state.flapSpeed = Mth.lerp(partialTicks, entity.oFlapSpeed, entity.destPos);
    }
}
