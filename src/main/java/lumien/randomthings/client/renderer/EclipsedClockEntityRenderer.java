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

/**
 * Draws an {@link EclipsedClockEntity} as its own held-item model, hung flat
 * against the wall it's mounted on (mirrors vanilla's item-frame rendering
 * approach), plus a floating target-time nameplate while recently
 * interacted with. Direct port of 1.12.2's {@code RenderEclipsedClock},
 * minus its custom rotating color-triangle time-skip burst - see {@link
 * EclipsedClockEntity}'s javadoc for why that's dropped in favor of a plain
 * particle burst instead (spawned directly from the entity, not this
 * renderer).
 */
public class EclipsedClockEntityRenderer extends EntityRenderer<EclipsedClockEntity> {
    public EclipsedClockEntityRenderer(EntityRendererManager renderManager) {
        super(renderManager);
    }

    @Override
    public void doRender(EclipsedClockEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();

        BlockPos blockpos = entity.getHangingPosition();
        double d0 = (double) blockpos.getX() - entity.posX + x;
        double d1 = (double) blockpos.getY() - entity.posY + y;
        double d2 = (double) blockpos.getZ() - entity.posZ + z;

        GlStateManager.translated(d0 + 0.5D, d1 + 0.5D, d2 + 0.5D);
        GlStateManager.rotatef(180.0F - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
        GlStateManager.translatef(0.0F, 0.0F, 0.4375F);
        GlStateManager.scalef(0.5F, 0.5F, 0.5F);

        ItemStack stack = new ItemStack(ModItems.ECLIPSED_CLOCK);
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("targetTime", entity.getTargetTime());
        stack.setTag(tag);

        Minecraft.getInstance().getItemRenderer().renderItem(stack, ItemCameraTransforms.TransformType.FIXED);

        GlStateManager.popMatrix();

        if (entity.shouldDisplayTime()) {
            this.renderLivingLabel(entity, entity.getStringTargetTime(), x, y + 0.45, z, 64);
        }
    }

    /**
     * Also show the target time whenever this clock is the one under the
     * crosshair, even without a recent interaction - matches 1.12.2's own
     * separate {@code renderName} override. (1.14.4 has no static
     * {@code EntityRenderer.drawNameplate} helper the way 1.12.2 did, so both
     * this and the recent-interaction display above go through the same
     * {@code renderLivingLabel} instance method instead - a forced API
     * adaptation, not a deliberate behavior change.)
     */
    @Override
    protected void renderName(EclipsedClockEntity entity, double x, double y, double z) {
        if (this.renderManager.pointedEntity == entity) {
            double distanceSq = entity.getDistanceSq(Minecraft.getInstance().getRenderViewEntity());
            float range = entity.isSneaking() ? 32.0F : 64.0F;

            if (distanceSq < (double) (range * range)) {
                this.renderLivingLabel(entity, entity.getStringTargetTime(), x, y, z, 64);
            }
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(EclipsedClockEntity entity) {
        return null;
    }
}
