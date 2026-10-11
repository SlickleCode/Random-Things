package lumien.randomthings.client.renderer;

import lumien.randomthings.entity.WeatherCloudEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;

/**
 * {@link WeatherCloudEntity} has no visual of its own - it's entirely
 * particle-driven from the entity's own tick - so, like {@code
 * FlooFireplaceEntity}'s renderer, this one draws nothing. See {@link
 * WeatherCloudEntity}'s javadoc for the one dropped exception (the Sun
 * variant's decorative sunburst overlay).
 */
public class WeatherCloudEntityRenderer extends EntityRenderer<WeatherCloudEntity> {
    public WeatherCloudEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);

        this.shadowSize = 0F;
    }

    @Override
    public void render(WeatherCloudEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
    }

    @Override
    public ResourceLocation getEntityTexture(WeatherCloudEntity entity) {
        return null;
    }
}
