package lumien.randomthings.client.renderer;

import lumien.randomthings.entity.FlooFireplaceEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;

/**
 * {@link FlooFireplaceEntity} has no visual of its own - its flame effect is
 * plain particles spawned from the entity's own {@code tick()} - but every
 * registered {@link net.minecraft.world.entity.EntityType} still needs a bound
 * renderer client-side, so this one simply draws nothing.
 */
public class FlooFireplaceEntityRenderer extends EntityRenderer<FlooFireplaceEntity, net.minecraft.client.renderer.entity.state.EntityRenderState> {
    public FlooFireplaceEntityRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context renderManager)  {
        super(renderManager);

        this.shadowRadius = 0F;
    }

    @Override
    public net.minecraft.client.renderer.entity.state.EntityRenderState createRenderState() {
        return new net.minecraft.client.renderer.entity.state.EntityRenderState();
    }

}
