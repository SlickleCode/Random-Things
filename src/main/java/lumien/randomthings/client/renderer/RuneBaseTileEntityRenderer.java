package lumien.randomthings.client.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import lumien.randomthings.config.RTConfig;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.tileentity.RuneBaseTileEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Draws each placed rune as a small colored square, plus a thin connecting
 * strip toward any adjacent cell (in this same Rune Base, or across the edge
 * into a neighboring one) sharing its color - reading directly from the tile
 * entity's own data and, for edge connections, its loaded neighbors', when the
 * render state is extracted. Replaces 1.12.2's {@code ModelRune}, a custom baked
 * model driven by {@code ExtendedBlockState}/{@code IUnlistedProperty} (removed
 * in 1.14.4) - this port's established fix for exactly that kind of per-instance
 * dynamic render data is a custom block entity renderer instead.
 * <p>
 * Like the original, each pixel/strip samples a random 2x2 (or 2x1) sub-region
 * of a 16x16 texture, tinted by the rune color, so no two pixels look quite
 * identical. The regions are drawn from a {@link Random} seeded by the block
 * position (consumed in the original's exact order) so they stay stable from
 * frame to frame. The {@code FlatRunes} config option swaps the noisy
 * {@code rune_base.png} for the plain {@code rune_base_flat.png}, as in 1.12.2.
 * The quads are drawn fully bright (the original switched the light layer off).
 */
public class RuneBaseTileEntityRenderer implements BlockEntityRenderer<RuneBaseTileEntity, RuneBaseTileEntityRenderer.State>
{
	private static final float UNIT = 1F / 16F;
	private static final Identifier NOISY = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/block/rune_base.png");
	private static final Identifier FLAT = Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, "textures/block/rune_base_flat.png");
	private static final Direction[] SIDE_ORDER = { Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST };

	private record Quad(float x1, float x2, float z1, float z2, float u1, float u2, float v1, float v2, float r, float g, float b)
	{
	}

	public static class State extends BlockEntityRenderState
	{
		private final List<Quad> quads = new ArrayList<>();
		private boolean flat;
	}

	public RuneBaseTileEntityRenderer(BlockEntityRendererProvider.Context context)
	{
	}

	@Override
	public State createRenderState()
	{
		return new State();
	}

	@Override
	public void extractRenderState(RuneBaseTileEntity te, State state, float partialTicks, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumbling)
	{
		BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, cameraPos, crumbling);

		state.quads.clear();
		state.flat = RTConfig.FLAT_RUNES.get();

		if (te.isEmpty())
		{
			return;
		}

		DyeColor[][] runeData = te.getRuneData();
		Random rng = new Random(te.getBlockPos().asLong());

		for (int cx = 0; cx < 4; cx++)
		{
			for (int cy = 0; cy < 4; cy++)
			{
				DyeColor color = runeData[cx][cy];

				float u = rng.nextInt(8) * 2;
				float v = rng.nextInt(8) * 2;

				if (color == null)
				{
					// Keep the random sequence aligned with the original's, which still rolled the four side samples for empty cells.
					for (int i = 0; i < 8; i++)
					{
						rng.nextInt(8);
					}

					continue;
				}

				float[] rgb = lumien.randomthings.util.DyeUtil.diffuse(color);

				// The pixel itself: 2 units wide, centered in its 4-unit cell.
				addQuad(state, cx * 4 + 1, cx * 4 + 3, cy * 4 + 1, cy * 4 + 3, rgb, u, v);

				for (Direction dir : SIDE_ORDER)
				{
					u = rng.nextInt(8) * 2;
					v = rng.nextInt(8) * 2;

					if (isConnected(te, runeData, cx, cy, color, dir))
					{
						addConnectionStrip(state, cx, cy, dir, rgb, u, v);
					}
				}
			}
		}
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera)
	{
		if (state.quads.isEmpty())
		{
			return;
		}

		collector.submitCustomGeometry(poseStack, RenderTypes.entityCutoutNoCull(state.flat ? FLAT : NOISY), (pose, buffer) -> {
			for (Quad q : state.quads)
			{
				float y = 0.02F;

				vertex(pose, buffer, q.x1, y, q.z1, q.u1, q.v1, q);
				vertex(pose, buffer, q.x1, y, q.z2, q.u1, q.v2, q);
				vertex(pose, buffer, q.x2, y, q.z2, q.u2, q.v2, q);
				vertex(pose, buffer, q.x2, y, q.z1, q.u2, q.v1, q);
			}
		});
	}

	private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u, float v, Quad q)
	{
		buffer.addVertex(pose, x, y, z).setColor(q.r, q.g, q.b, 1.0F).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(LightTexture.FULL_BRIGHT).setNormal(pose, 0.0F, 1.0F, 0.0F);
	}

	private boolean isConnected(RuneBaseTileEntity te, DyeColor[][] runeData, int cx, int cy, DyeColor color, Direction dir)
	{
		int nx = cx + dir.getStepX();
		int ny = cy + dir.getStepZ();

		if (nx >= 0 && nx < 4 && ny >= 0 && ny < 4)
		{
			return runeData[nx][ny] == color;
		}

		BlockEntity neighborTe = te.getLevel().getBlockEntity(te.getBlockPos().relative(dir));

		if (!(neighborTe instanceof RuneBaseTileEntity))
		{
			return false;
		}

		int mirroredX = nx < 0 ? 3 : (nx >= 4 ? 0 : nx);
		int mirroredY = ny < 0 ? 3 : (ny >= 4 ? 0 : ny);

		return ((RuneBaseTileEntity) neighborTe).getRuneData()[mirroredX][mirroredY] == color;
	}

	private void addConnectionStrip(State state, int cx, int cy, Direction dir, float[] rgb, float u, float v)
	{
		int baseX = cx * 4;
		int baseY = cy * 4;

		int x1, x2, y1, y2;

		switch (dir)
		{
			case NORTH:
				x1 = baseX + 1;
				x2 = baseX + 3;
				y1 = baseY;
				y2 = baseY + 1;
				break;
			case SOUTH:
				x1 = baseX + 1;
				x2 = baseX + 3;
				y1 = baseY + 3;
				y2 = baseY + 4;
				break;
			case WEST:
				x1 = baseX;
				x2 = baseX + 1;
				y1 = baseY + 1;
				y2 = baseY + 3;
				break;
			case EAST:
			default:
				x1 = baseX + 3;
				x2 = baseX + 4;
				y1 = baseY + 1;
				y2 = baseY + 3;
				break;
		}

		addQuad(state, x1, x2, y1, y2, rgb, u, v);
	}

	/** {@code u}/{@code v} pick the top-left texel of a region as big as the quad, in 1/16 texture units. */
	private void addQuad(State state, int x1, int x2, int y1, int y2, float[] rgb, float u, float v)
	{
		state.quads.add(new Quad(x1 * UNIT, x2 * UNIT, y1 * UNIT, y2 * UNIT, u / 16F, (u + (x2 - x1)) / 16F, v / 16F, (v + (y2 - y1)) / 16F, rgb[0], rgb[1], rgb[2]));
	}
}
