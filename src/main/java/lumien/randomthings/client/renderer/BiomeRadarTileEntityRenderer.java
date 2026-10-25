package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import lumien.randomthings.tileentity.BiomeRadarTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

/**
 * Renders the inserted {@link lumien.randomthings.item.BiomeCrystalItem}
 * spinning in place inside the radar (the 1.12.2 pose). The 1.12.2 original
 * (`RenderBiomeRadar`) got this same "floating item" look by instantiating a
 * throwaway {@code EntityItem} purely to reuse its bob/spin math and vanilla
 * dropped-item renderer - simpler to just do the bob/spin transform directly
 * here and render the stack through its item model state, same visual result.
 */
public class BiomeRadarTileEntityRenderer implements BlockEntityRenderer<BiomeRadarTileEntity, BiomeRadarTileEntityRenderer.State>
{
	public static class State extends BlockEntityRenderState
	{
		public final ItemStackRenderState item = new ItemStackRenderState();
		public float age;
	}

	public BiomeRadarTileEntityRenderer(net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context context)
	{
	}

	@Override
	public State createRenderState()
	{
		return new State();
	}

	@Override
	public void extractRenderState(BiomeRadarTileEntity te, State state, float partialTicks, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumbling)
	{
		BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, cameraPos, crumbling);

		state.item.clear();
		ItemStack currentCrystal = te.getCurrentCrystal();

		if (!currentCrystal.isEmpty() && te.getLevel() != null)
		{
			Minecraft.getInstance().getItemModelResolver().updateForTopItem(state.item, currentCrystal, ItemDisplayContext.GROUND, te.getLevel(), null, 0);
			state.age = (te.getLevel().getGameTime() % 360000L) + partialTicks;
			state.lightCoords = net.minecraft.client.renderer.LevelRenderer.getLightColor(te.getLevel(), te.getBlockPos().above());
		}
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera)
	{
		if (state.item.isEmpty())
		{
			return;
		}

		poseStack.pushPose();

		// 1.12.2 pinned the item's bob phase (hoverStart) so it spun in place INSIDE the block, about a third of the way up - no bobbing, no hovering above it
		poseStack.translate(0.5D, 0.4D, 0.5D);
		poseStack.mulPose(Axis.YN.rotationDegrees((state.age * 2.9F) % 360.0F));
		poseStack.scale(0.75F, 0.75F, 0.75F);

		state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

		poseStack.popPose();
	}
}
