package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.entity.EclipsedClockEntity;
import lumien.randomthings.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.text.StringTextComponent;

/**
 * Draws an {@link EclipsedClockEntity} as its own held-item model, hung flat
 * against the wall it's mounted on (mirrors vanilla's item-frame rendering
 * approach), plus a floating target-time nameplate while recently
 * interacted with. Direct port of 1.12.2's {@code RenderEclipsedClock},
 * minus its custom rotating color-triangle time-skip burst - see {@link
 * EclipsedClockEntity}'s javadoc for why that's dropped in favor of a plain
 * particle burst instead (spawned directly from the entity, not this
 * renderer).
 * <p>
 * Real bug found and fixed, 2026-09-27 (reported by user: the time display
 * sits about a block too high). {@code renderLivingLabel}'s own vanilla
 * implementation already adds {@code entity.getHeight() + 0.5} - 1.0 blocks,
 * for this entity's registered 0.5-tall size - on top of whatever {@code y}
 * it's given (confirmed via {@code javap -c}), so both call sites below were
 * stacking their own manual offset on top of that baked-in one instead of
 * accounting for it, landing the label roughly a block higher than intended.
 * Both now subtract that same 1.0 to compensate.
 */
public class EclipsedClockEntityRenderer extends EntityRenderer<EclipsedClockEntity> {
    public EclipsedClockEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void render(EclipsedClockEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        matrixStack.push();

        // the matrix is already translated to the entity; the clock hangs centered on its block
        BlockPos blockpos = entity.getHangingPosition();
        double d0 = (double) blockpos.getX() - entity.getPosX();
        double d1 = (double) blockpos.getY() - entity.getPosY();
        double d2 = (double) blockpos.getZ() - entity.getPosZ();

        matrixStack.translate(d0 + 0.5D, d1 + 0.5D, d2 + 0.5D);
        matrixStack.rotate(Vector3f.YP.rotationDegrees(180.0F - entity.rotationYaw));
        matrixStack.translate(0.0D, 0.0D, 0.4375D);
        matrixStack.scale(0.5F, 0.5F, 0.5F);

        ItemStack stack = new ItemStack(ModItems.ECLIPSED_CLOCK);
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("targetTime", entity.getTargetTime());
        stack.setTag(tag);

        Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.FIXED, packedLight, OverlayTexture.NO_OVERLAY, matrixStack, buffer);

        matrixStack.pop();

        if (entity.shouldDisplayTime()) {
            drawLabel(entity, -0.55D, matrixStack, buffer, packedLight);
        } else if (this.renderManager.pointedEntity == entity) {
            // Also show the target time whenever this clock is the one under the crosshair, even without a
            // recent interaction - matches 1.12.2's separate renderName override.
            double distanceSq = entity.getDistanceSq(Minecraft.getInstance().getRenderViewEntity());
            float range = entity.isSneaking() ? 32.0F : 64.0F;

            if (distanceSq < (double) (range * range)) {
                drawLabel(entity, -1.0D, matrixStack, buffer, packedLight);
            }
        }
    }

    private void drawLabel(EclipsedClockEntity entity, double yOffset, MatrixStack matrixStack, IRenderTypeBuffer buffer, int packedLight) {
        matrixStack.push();
        matrixStack.translate(0.0D, yOffset, 0.0D);
        this.renderName(entity, new StringTextComponent(entity.getStringTargetTime()), matrixStack, buffer, packedLight);
        matrixStack.pop();
    }

    @Override
    public ResourceLocation getEntityTexture(EclipsedClockEntity entity) {
        return null;
    }
}
