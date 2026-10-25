package lumien.randomthings.client.renderer;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import lumien.randomthings.tileentity.DiaphanousBlockTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Draws {@link DiaphanousBlockTileEntity}'s displayed block as its baked model, with every vertex's alpha set to a value computed from the distance to the
 * camera - close up it's mostly invisible, fading to fully opaque-looking past 33 blocks, the opposite curve when {@link DiaphanousBlockTileEntity#isInverted()}.
 * Direct port of 1.12.2's {@code RenderBlockDiaphanous}'s fade math.
 * <p>
 * 1.21.6+ renders in two steps (extract the state, then submit geometry), so the alpha is simply handed to {@code putBulkData} while the quads are written
 * (no vertex consumer wrapper is needed any more); faces that touch a solid neighbour are skipped, as the original's face culling did.
 */
public class DiaphanousBlockTileEntityRenderer implements BlockEntityRenderer<DiaphanousBlockTileEntity, DiaphanousBlockTileEntityRenderer.State>
{
	public static class State extends BlockEntityRenderState
	{
		public final List<BakedQuad> quads = new ArrayList<>();
		public float alpha;
	}

	private final RandomSource random = RandomSource.create();

	public DiaphanousBlockTileEntityRenderer(BlockEntityRendererProvider.Context context)
	{
	}

	@Override
	public State createRenderState()
	{
		return new State();
	}

	@Override
	public void extractRenderState(DiaphanousBlockTileEntity te, State state, float partialTicks, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumbling)
	{
		BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, cameraPos, crumbling);

		state.quads.clear();
		state.alpha = 0;

		BlockState display = te.getDisplayState();
		Level world = te.getLevel();

		if (world == null || display.getRenderShape() != RenderShape.MODEL)
		{
			return;
		}

		double dx = te.getBlockPos().getX() + 0.5 - cameraPos.x;
		double dy = te.getBlockPos().getY() + 0.5 - cameraPos.y;
		double dz = te.getBlockPos().getZ() + 0.5 - cameraPos.z;
		double distance = Math.max(0, dx * dx + dy * dy + dz * dz - 5);
		float curve = -0.5F * ((float) Math.cos(Math.PI * Math.min(Math.max(0, distance - 8), 25) / 25) - 1);
		state.alpha = te.isInverted() ? 1 - curve : curve;

		if (state.alpha <= 0.004F)
		{
			return;
		}

		BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(display);
		random.setSeed(display.getSeed(te.getBlockPos()));
		List<BlockModelPart> parts = model.collectParts(random);

		for (BlockModelPart part : parts)
		{
			for (Direction side : Direction.values())
			{
				if (!world.getBlockState(te.getBlockPos().relative(side)).isSolidRender())
				{
					state.quads.addAll(part.getQuads(side));
				}
			}

			state.quads.addAll(part.getQuads(null));
		}
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera)
	{
		if (state.alpha <= 0.004F || state.quads.isEmpty())
		{
			return;
		}

		float alpha = state.alpha;
		int light = state.lightCoords;

		collector.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), (pose, consumer) -> {
			for (BakedQuad quad : state.quads)
			{
				consumer.putBulkData(pose, quad, 1.0F, 1.0F, 1.0F, alpha, light, OverlayTexture.NO_OVERLAY);
			}
		});
	}
}
