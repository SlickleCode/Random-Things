package lumien.randomthings.client.renderer;

import net.minecraft.world.item.ItemDisplayContext;

import com.mojang.math.Axis;

import com.mojang.blaze3d.platform.GlStateManager;
import lumien.randomthings.entity.TimeAcceleratorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3f;
import net.minecraft.client.renderer.texture.OverlayTexture;

/**
 * Draws the Time in a Bottle item icon as a camera-facing billboard on all 6
 * faces of the target block, close to each face's own surface - per user
 * request, 2026-09-27 ("make every side have the floating icon... closer to
 * the surface"). Replaces a brief first pass that reused vanilla's own
 * {@code SpriteRenderer} directly, which can only billboard at one fixed
 * position - fine for a single marker, not for "every side." The per-icon
 * transform sequence below (translate, rescale-normal, scale, the same
 * three rotations, then the item render call itself) is copied from {@code
 * SpriteRenderer#doRender} (confirmed via {@code javap -c}) so each of the
 * 6 icons billboards identically to how vanilla's own item-entity rendering
 * does it, just repeated once per {@link Direction} with a small offset
 * instead of once at the entity's own position.
 */
public class TimeAcceleratorEntityRenderer extends EntityRenderer<TimeAcceleratorEntity, TimeAcceleratorEntityRenderer.State> {
    /** The icon of the entity's item, resolved once per frame in {@link #extractRenderState}. */
    public static class State extends net.minecraft.client.renderer.entity.state.EntityRenderState {
        public final net.minecraft.client.renderer.item.ItemStackRenderState item = new net.minecraft.client.renderer.item.ItemStackRenderState();
    }

    /** How far past the block's own half-width (0.5) each icon sits. */
    private static final float SURFACE_GAP = 0.05F;

    private final net.minecraft.client.renderer.item.ItemModelResolver itemModelResolver;

    public TimeAcceleratorEntityRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context renderManager)  {
        super(renderManager);

        this.itemModelResolver = renderManager.getItemModelResolver();

        this.shadowRadius = 0F;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(TimeAcceleratorEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        this.itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.GROUND, entity);
    }

    @Override
    public void render(State state, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        float offset = 0.5F + SURFACE_GAP;

        for (Direction direction : Direction.values()) {
            matrixStack.pushPose();
            matrixStack.translate(direction.getStepX() * offset, direction.getStepY() * offset, direction.getStepZ() * offset);
            // billboard: face the camera, then flip like the 1.14.4 renderer did
            matrixStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            state.item.render(matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

            matrixStack.popPose();
        }
    }
}
