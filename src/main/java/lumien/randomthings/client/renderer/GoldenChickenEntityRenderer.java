package lumien.randomthings.client.renderer;

import lumien.randomthings.entity.GoldenChickenEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.ChickenModel;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

/**
 * Reuses vanilla's {@code ChickenModel} wholesale (same hitbox/proportions as
 * the original's own reuse of {@code ModelChicken}), just with the Golden
 * Chicken's own texture and wing-flap animation values. Direct port of
 * 1.12.2's {@code RenderGoldenChicken}.
 */
public class GoldenChickenEntityRenderer extends MobRenderer<GoldenChickenEntity, ChickenModel<GoldenChickenEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("randomthings", "textures/entity/golden_chicken.png");

    public GoldenChickenEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager, new ChickenModel<>(), 0.3F);
    }

    @Override
    protected ResourceLocation getEntityTexture(GoldenChickenEntity entity) {
        return TEXTURE;
    }

    @Override
    protected float handleRotationFloat(GoldenChickenEntity livingBase, float partialTicks) {
        float f = livingBase.oFlap + (livingBase.wingRotation - livingBase.oFlap) * partialTicks;
        float f1 = livingBase.oFlapSpeed + (livingBase.destPos - livingBase.oFlapSpeed) * partialTicks;
        return (MathHelper.sin(f) + 1.0F) * f1;
    }
}
