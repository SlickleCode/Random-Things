package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import lumien.randomthings.entity.TimeAcceleratorEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Vector3f;
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
public class TimeAcceleratorEntityRenderer extends EntityRenderer<TimeAcceleratorEntity> {
    /** How far past the block's own half-width (0.5) each icon sits. */
    private static final float SURFACE_GAP = 0.05F;

    public TimeAcceleratorEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);

        this.shadowSize = 0F;
    }

    @Override
    public void render(TimeAcceleratorEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        ItemStack stack = entity.getItem();
        float offset = 0.5F + SURFACE_GAP;

        for (Direction direction : Direction.values()) {
            matrixStack.push();
            matrixStack.translate(direction.getXOffset() * offset, direction.getYOffset() * offset, direction.getZOffset() * offset);
            // billboard: face the camera, then flip like the 1.14.4 renderer did
            matrixStack.rotate(this.renderManager.getCameraOrientation());
            matrixStack.rotate(Vector3f.YP.rotationDegrees(180.0F));

            Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.GROUND, 15728880, OverlayTexture.NO_OVERLAY, matrixStack, buffer);

            matrixStack.pop();
        }
    }

    @Override
    public ResourceLocation getEntityTexture(TimeAcceleratorEntity entity) {
        return null;
    }
}
