package lumien.randomthings.client.renderer;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lumien.randomthings.tileentity.DiaphanousBlockTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
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
 * 1.21.6+ renders in two steps (extract the state, then submit geometry). The quads are written by vanilla's own {@code ModelBlockRenderer.tesselateBlock}
 * (smooth lighting, ambient occlusion, per-face shading and face culling against solid neighbours, exactly as for a placed block) into a consumer that
 * replaces the alpha of every vertex.
 */
public class DiaphanousBlockTileEntityRenderer implements BlockEntityRenderer<DiaphanousBlockTileEntity, DiaphanousBlockTileEntityRenderer.State>
{
	public static class State extends BlockEntityRenderState
	{
		public final List<BlockModelPart> parts = new ArrayList<>();
		public BlockState display;
		public Level level;
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

		state.parts.clear();
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
		state.parts.addAll(model.collectParts(random));
		state.display = display;
		state.level = world;
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera)
	{
		if (state.alpha <= 0.004F || state.parts.isEmpty())
		{
			return;
		}

		int alpha = Math.round(Math.min(1.0F, state.alpha) * 255);
		BlockPos pos = state.blockPos;

		collector.submitCustomGeometry(poseStack, RenderTypes.translucentMovingBlock(), (pose, consumer) -> {
			PoseStack stack = new PoseStack();
			stack.last().set(pose);
			Minecraft.getInstance().getBlockRenderer().getModelRenderer().tesselateBlock(state.level, state.parts, state.display, pos, stack, new AlphaConsumer(consumer, alpha), true, OverlayTexture.NO_OVERLAY);
		});
	}

	/** Passes everything through to the real consumer but with a fixed alpha. */
	private static final class AlphaConsumer implements VertexConsumer
	{
		private final VertexConsumer delegate;
		private final int alpha;

		AlphaConsumer(VertexConsumer delegate, int alpha)
		{
			this.delegate = delegate;
			this.alpha = alpha;
		}

		@Override
		public VertexConsumer addVertex(float x, float y, float z)
		{
			delegate.addVertex(x, y, z);
			return this;
		}

		@Override
		public VertexConsumer setColor(int r, int g, int b, int a)
		{
			delegate.setColor(r, g, b, alpha);
			return this;
		}

		@Override
		public VertexConsumer setColor(int argb)
		{
			delegate.setColor((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, alpha);
			return this;
		}

		@Override
		public VertexConsumer setUv(float u, float v)
		{
			delegate.setUv(u, v);
			return this;
		}

		@Override
		public VertexConsumer setUv1(int u, int v)
		{
			delegate.setUv1(u, v);
			return this;
		}

		@Override
		public VertexConsumer setUv2(int u, int v)
		{
			delegate.setUv2(u, v);
			return this;
		}

		@Override
		public VertexConsumer setNormal(float x, float y, float z)
		{
			delegate.setNormal(x, y, z);
			return this;
		}

		@Override
		public VertexConsumer setLineWidth(float width)
		{
			delegate.setLineWidth(width);
			return this;
		}
	}
}
