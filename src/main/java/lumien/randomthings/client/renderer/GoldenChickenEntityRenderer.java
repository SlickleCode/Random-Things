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
public class GoldenChickenEntityRenderer extends MobRenderer<GoldenChickenEntity, ChickenModel<GoldenChickenEntity>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/entity/golden_chicken.png");

    public GoldenChickenEntityRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context renderManager) {
        super(renderManager, new ChickenModel<>(renderManager.bakeLayer(net.minecraft.client.model.geom.ModelLayers.CHICKEN)), 0.3F);
    }

    @Override
    public ResourceLocation getTextureLocation(GoldenChickenEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float getBob(GoldenChickenEntity livingBase, float partialTicks) {
        float f = livingBase.oFlap + (livingBase.wingRotation - livingBase.oFlap) * partialTicks;
        float f1 = livingBase.oFlapSpeed + (livingBase.destPos - livingBase.oFlapSpeed) * partialTicks;
        return (Mth.sin(f) + 1.0F) * f1;
    }
}
