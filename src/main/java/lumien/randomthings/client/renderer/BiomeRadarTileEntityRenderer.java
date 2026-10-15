package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.tileentity.BiomeRadarTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;

/**
 * Renders the inserted {@link lumien.randomthings.item.BiomeCrystalItem}
 * spinning in place inside the radar (the 1.12.2 pose). The 1.12.2 original
 * (`RenderBiomeRadar`) got this same "floating item" look by instantiating a
 * throwaway {@code EntityItem} purely to reuse its bob/spin math and vanilla
 * dropped-item renderer - simpler to just do the bob/spin transform directly
 * here and render the stack with {@code ItemRenderer}, same visual result.
 */
public class BiomeRadarTileEntityRenderer implements BlockEntityRenderer<BiomeRadarTileEntity> {
    public BiomeRadarTileEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BiomeRadarTileEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        ItemStack currentCrystal = te.getCurrentCrystal();

        if (currentCrystal.isEmpty()) {
            return;
        }

        float age = (te.getLevel().getGameTime() % 360000L) + partialTicks;

        matrixStack.pushPose();
        // 1.12.2 pinned the item's bob phase (hoverStart) so it spun in place INSIDE the block, about a third of the way up - no bobbing, no hovering above it
        matrixStack.translate(0.5D, 0.4D, 0.5D);
        matrixStack.mulPose(Vector3f.YN.rotationDegrees((age * 2.9F) % 360.0F));
        matrixStack.scale(0.75F, 0.75F, 0.75F);

        Minecraft.getInstance().getItemRenderer().renderStatic(currentCrystal, TransformType.GROUND, net.minecraft.client.renderer.LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().above()), combinedOverlay, matrixStack, buffer, 0);

        matrixStack.popPose();
    }
}
