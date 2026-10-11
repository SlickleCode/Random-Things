package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.tileentity.BiomeRadarTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

/**
 * Renders the inserted {@link lumien.randomthings.item.BiomeCrystalItem}
 * hovering and slowly spinning above the radar. The 1.12.2 original
 * (`RenderBiomeRadar`) got this same "floating item" look by instantiating a
 * throwaway {@code EntityItem} purely to reuse its bob/spin math and vanilla
 * dropped-item renderer - simpler to just do the bob/spin transform directly
 * here and render the stack with {@code ItemRenderer}, same visual result.
 */
public class BiomeRadarTileEntityRenderer extends TileEntityRenderer<BiomeRadarTileEntity> {
    public BiomeRadarTileEntityRenderer(TileEntityRendererDispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void render(BiomeRadarTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer, int combinedLight, int combinedOverlay) {
        ItemStack currentCrystal = te.getCurrentCrystal();

        if (currentCrystal.isEmpty()) {
            return;
        }

        float age = (te.getWorld().getGameTime() % 360000L) + partialTicks;

        matrixStack.push();
        matrixStack.translate(0.5D, 1.1D + 0.1D * net.minecraft.util.math.MathHelper.sin(age / 10.0F), 0.5D);
        matrixStack.rotate(Vector3f.YP.rotationDegrees((age * 2.0F) % 360.0F));
        matrixStack.scale(0.75F, 0.75F, 0.75F);

        Minecraft.getInstance().getItemRenderer().renderItem(currentCrystal, TransformType.GROUND, net.minecraft.client.renderer.WorldRenderer.getCombinedLight(te.getWorld(), te.getPos().up()), combinedOverlay, matrixStack, buffer);

        matrixStack.pop();
    }
}
