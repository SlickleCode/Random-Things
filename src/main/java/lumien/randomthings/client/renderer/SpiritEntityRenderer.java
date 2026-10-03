package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.entity.SpiritEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeGelLayer;
import net.minecraft.client.renderer.entity.model.SlimeModel;
import net.minecraft.util.ResourceLocation;

/**
 * Reuses vanilla's own {@code SlimeModel}/{@code SlimeGelLayer} wholesale
 * (same "opaque inner body + translucent outer gel" two-layer trick vanilla's
 * own Slime uses), just with the Spirit's own texture and lighting always
 * disabled for a flat, ghostly, unshaded look - direct port of 1.12.2's
 * {@code RenderSpirit}/{@code LayerSpiritGel}, simplified: {@code
 * SlimeGelLayer} already renders its translucent pass against whatever
 * texture this renderer's own {@link #getEntityTexture} returns, so no
 * separate hand-rolled layer class is needed here the way 1.12.2's bespoke
 * {@code LayerSpiritGel} was.
 */
public class SpiritEntityRenderer extends MobRenderer<SpiritEntity, SlimeModel<SpiritEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("randomthings", "textures/entity/spirit.png");

    public SpiritEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager, new SlimeModel<>(16), 0.125F);

        this.addLayer(new SlimeGelLayer<>(this));
    }

    @Override
    public void doRender(SpiritEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        super.doRender(entity, x, y, z, entityYaw, partialTicks);

        GlStateManager.enableLighting();
        Minecraft.getInstance().gameRenderer.enableLightmap();
    }

    @Override
    protected void preRenderCallback(SpiritEntity entity, float partialTickTime) {
        GlStateManager.scaled(0.5, 0.5, 0.5);
        GlStateManager.disableLighting();
        Minecraft.getInstance().gameRenderer.disableLightmap();
    }

    @Override
    protected ResourceLocation getEntityTexture(SpiritEntity entity) {
        return TEXTURE;
    }
}
