package lumien.randomthings.client.renderer;

import lumien.randomthings.entity.FlooFireplaceEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;

/**
 * {@link FlooFireplaceEntity} has no visual of its own - its flame effect is
 * plain particles spawned from the entity's own {@code tick()} - but every
 * registered {@link net.minecraft.entity.EntityType} still needs a bound
 * renderer client-side, so this one simply draws nothing.
 */
public class FlooFireplaceEntityRenderer extends EntityRenderer<FlooFireplaceEntity> {
    public FlooFireplaceEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);

        this.shadowSize = 0F;
    }

    @Override
    public void render(FlooFireplaceEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
    }

    @Override
    public ResourceLocation getEntityTexture(FlooFireplaceEntity entity) {
        return null;
    }
}
