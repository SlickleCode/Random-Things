package lumien.randomthings.client.renderer;

import lumien.randomthings.entity.WeatherCloudEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * {@link WeatherCloudEntity} has no visual of its own - it's entirely
 * particle-driven from the entity's own tick - so, like {@code
 * FlooFireplaceEntity}'s renderer, this one draws nothing. See {@link
 * WeatherCloudEntity}'s javadoc for the one dropped exception (the Sun
 * variant's decorative sunburst overlay).
 */
public class WeatherCloudEntityRenderer extends EntityRenderer<WeatherCloudEntity, net.minecraft.client.renderer.entity.state.EntityRenderState> {
    public WeatherCloudEntityRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context renderManager)  {
        super(renderManager);

        this.shadowRadius = 0F;
    }

    @Override
    public net.minecraft.client.renderer.entity.state.EntityRenderState createRenderState() {
        return new net.minecraft.client.renderer.entity.state.EntityRenderState();
    }

    @Override
    public void render(net.minecraft.client.renderer.entity.state.EntityRenderState state, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
    }
}
