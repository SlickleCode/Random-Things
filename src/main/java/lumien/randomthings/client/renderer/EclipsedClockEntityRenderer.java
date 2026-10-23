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
import net.minecraft.world.phys.Vec3;
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
public class EclipsedClockEntityRenderer extends EntityRenderer<EclipsedClockEntity, EclipsedClockEntityRenderer.State> {
    public static class State extends net.minecraft.client.renderer.entity.state.EntityRenderState {
        public final net.minecraft.client.renderer.item.ItemStackRenderState item = new net.minecraft.client.renderer.item.ItemStackRenderState();
        public double offX;
        public double offY;
        public double offZ;
        public float yRot;
        /** The target time label, or null when it is not shown; {@code nameTagAttachment} carries its height. */
        public String label;
    }

    private final net.minecraft.client.renderer.item.ItemModelResolver itemModelResolver;

    public EclipsedClockEntityRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context renderManager)  {
        super(renderManager);

        this.itemModelResolver = renderManager.getItemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EclipsedClockEntity entity, State state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);

        // the matrix is translated to the entity; the clock hangs centered on its block
        BlockPos blockpos = entity.getPos();
        state.offX = (double) blockpos.getX() - entity.getX() + 0.5D;
        state.offY = (double) blockpos.getY() - entity.getY() + 0.5D;
        state.offZ = (double) blockpos.getZ() - entity.getZ() + 0.5D;
        state.yRot = entity.getYRot();

        ItemStack stack = new ItemStack(ModItems.ECLIPSED_CLOCK);
        CompoundTag tag = new CompoundTag();
        tag.putInt("targetTime", entity.getTargetTime());
        StackTag.set(stack, tag);
        this.itemModelResolver.updateForNonLiving(state.item, stack, ItemDisplayContext.FIXED, entity);

        state.label = null;

        if (entity.shouldDisplayTime()) {
            state.label = entity.getStringTargetTime();
            state.nameTagAttachment = new Vec3(0.0D, -0.55D + 0.5D, 0.0D);
        } else if (this.entityRenderDispatcher.crosshairPickEntity == entity) {
            // Also show the target time whenever this clock is the one under the crosshair, even without a
            // recent interaction - matches 1.12.2's separate renderName override.
            double distanceSq = entity.distanceToSqr(Minecraft.getInstance().getCameraEntity());
            float range = entity.isShiftKeyDown() ? 32.0F : 64.0F;

            if (distanceSq < (double) (range * range)) {
                state.label = entity.getStringTargetTime();
                state.nameTagAttachment = new Vec3(0.0D, -1.0D + 0.5D, 0.0D);
            }
        }
    }

    @Override
    public void render(State state, PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {
        matrixStack.pushPose();
        matrixStack.translate(state.offX, state.offY, state.offZ);
        matrixStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
        matrixStack.translate(0.0D, 0.0D, 0.4375D);
        matrixStack.scale(0.5F, 0.5F, 0.5F);

        state.item.render(matrixStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);

        matrixStack.popPose();

        if (state.label != null) {
            // renderNameTag lifts the text 0.5 above the attachment point (the 1.12.2 offsets were relative to the entity)
            this.renderNameTag(state, Component.literal(state.label), matrixStack, buffer, packedLight);
        }
    }
}
