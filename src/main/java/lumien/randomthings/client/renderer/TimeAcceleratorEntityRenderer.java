package lumien.randomthings.client.renderer;

import lumien.randomthings.entity.TimeAcceleratorEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;

/**
 * {@link TimeAcceleratorEntity} is invisible and entirely particle-driven
 * from its own tick (see its javadoc for the dropped rotating-ring
 * decoration this replaces), so this renderer draws nothing, matching
 * {@code WeatherCloudEntityRenderer}/{@code FlooFireplaceEntityRenderer}.
 */
public class TimeAcceleratorEntityRenderer extends EntityRenderer<TimeAcceleratorEntity> {
    public TimeAcceleratorEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);

        this.shadowSize = 0F;
    }

    @Override
    public void doRender(TimeAcceleratorEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
    }

    @Override
    protected ResourceLocation getEntityTexture(TimeAcceleratorEntity entity) {
        return null;
    }
}
