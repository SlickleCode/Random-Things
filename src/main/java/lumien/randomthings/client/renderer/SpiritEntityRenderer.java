package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.entity.SpiritEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;

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
public class SpiritEntityRenderer extends MobRenderer<SpiritEntity, net.minecraft.client.renderer.entity.state.SlimeRenderState, SlimeModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath("randomthings", "textures/entity/spirit.png");

    public SpiritEntityRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context renderManager) {
        super(renderManager, new SlimeModel(renderManager.bakeLayer(net.minecraft.client.model.geom.ModelLayers.SLIME)), 0.125F);

        this.addLayer(new SlimeOuterLayer(this, renderManager.getModelSet()));
    }

    /** 1.14.4 switched lighting and the lightmap off while drawing the spirit (it glows); full block light does the same here. */
    @Override
    protected int getBlockLightLevel(SpiritEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected void scale(net.minecraft.client.renderer.entity.state.SlimeRenderState state, PoseStack matrixStack) {
        matrixStack.scale(0.5F, 0.5F, 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(net.minecraft.client.renderer.entity.state.SlimeRenderState state) {
        return TEXTURE;
    }

    @Override
    public net.minecraft.client.renderer.entity.state.SlimeRenderState createRenderState() {
        return new net.minecraft.client.renderer.entity.state.SlimeRenderState();
    }
}
