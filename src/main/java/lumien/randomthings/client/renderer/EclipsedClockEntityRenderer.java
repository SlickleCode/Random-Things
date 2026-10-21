package lumien.randomthings.client.renderer;

import lumien.randomthings.util.StackTag;
import net.minecraft.world.item.ItemDisplayContext;

import com.mojang.math.Axis;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.entity.EclipsedClockEntity;
import lumien.randomthings.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Vector3f;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;

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
    public EclipsedClockEntityRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context renderManager)  {
        super(renderManager);
    }

    @Override
    public void render(EclipsedClockEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();

        // the matrix is already translated to the entity; the clock hangs centered on its block
        BlockPos blockpos = entity.getPos();
        double d0 = (double) blockpos.getX() - entity.getX();
        double d1 = (double) blockpos.getY() - entity.getY();
        double d2 = (double) blockpos.getZ() - entity.getZ();

        matrixStack.translate(d0 + 0.5D, d1 + 0.5D, d2 + 0.5D);
        matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F - entity.getYRot()));
        matrixStack.translate(0.0D, 0.0D, 0.4375D);
        matrixStack.scale(0.5F, 0.5F, 0.5F);

        ItemStack stack = new ItemStack(ModItems.ECLIPSED_CLOCK);
        CompoundTag tag = new CompoundTag();
        tag.putInt("targetTime", entity.getTargetTime());
        StackTag.set(stack, tag);

        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, packedLight, OverlayTexture.NO_OVERLAY, matrixStack, buffer, entity.level(), 0);

        matrixStack.popPose();

        if (entity.shouldDisplayTime()) {
            drawLabel(entity, -0.55D, matrixStack, buffer, packedLight);
        } else if (this.entityRenderDispatcher.crosshairPickEntity == entity) {
            // Also show the target time whenever this clock is the one under the crosshair, even without a
            // recent interaction - matches 1.12.2's separate renderName override.
            double distanceSq = entity.distanceToSqr(Minecraft.getInstance().getCameraEntity());
            float range = entity.isShiftKeyDown() ? 32.0F : 64.0F;

            if (distanceSq < (double) (range * range)) {
                drawLabel(entity, -1.0D, matrixStack, buffer, packedLight);
            }
        }
    }

    private void drawLabel(EclipsedClockEntity entity, double yOffset, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        matrixStack.translate(0.0D, yOffset, 0.0D);
        this.renderNameTag(entity, Component.literal(entity.getStringTargetTime()), matrixStack, buffer, packedLight, 0.0F);
        matrixStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(EclipsedClockEntity entity) {
        return null;
    }
}
