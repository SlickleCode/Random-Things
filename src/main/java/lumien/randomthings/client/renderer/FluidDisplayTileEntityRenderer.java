package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import lumien.randomthings.tileentity.FluidDisplayTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Draws the displayed fluid as a full unit cube textured with its still/
 * flowing sprite (read straight off the block atlas, exactly like
 * vanilla's own fluid rendering does), tinted with the fluid's own color, and
 * spun around Y in 90-degree steps for the rotation property. Replaces
 * 1.12.2's {@code ModelFluidDisplay}, a custom baked model driven by {@code
 * ExtendedBlockState} (not a thing in 1.14.4) - this port's established
 * substitute for that kind of per-instance dynamic render data is a custom
 * block entity renderer instead (see {@code RuneBaseTileEntityRenderer}).
 * <p>
 * Disclosed simplification: the original's rotation property remapped which
 * baked quad's UVs faced which direction on an otherwise-static cube; this
 * instead physically rotates the whole rendered cube in world space each
 * 90-degree step, which looks equivalent for the fully-opaque single-texture
 * cube case but isn't byte-for-byte the same transform.
 * <p>
 * When empty it draws the same cube with the mod's own "empty tank" sprite
 * ({@code block/fluid_display}) and no tint, as the original did (the block's
 * baked model is empty geometry - this renderer covers 100% of its visuals).
 * Each face carries the direction based brightness vanilla bakes into block
 * quads (UP 1.0, DOWN 0.5, NORTH/SOUTH 0.8, WEST/EAST 0.6) in its vertex color.
 * 1.21.6+: the cube is submitted as custom geometry of the moving-block render
 * type (block atlas, block vertex format) after the state was extracted.
 */
public class FluidDisplayTileEntityRenderer implements BlockEntityRenderer<FluidDisplayTileEntity, FluidDisplayTileEntityRenderer.State>
{
	private static final Identifier EMPTY_SPRITE = Identifier.fromNamespaceAndPath("randomthings", "block/fluid_display");

	public static class State extends BlockEntityRenderState
	{
		public TextureAtlasSprite sprite;
		public float r = 1.0F;
		public float g = 1.0F;
		public float b = 1.0F;
		public float rotation;
	}

	public FluidDisplayTileEntityRenderer(BlockEntityRendererProvider.Context context)
	{
	}

	@Override
	public State createRenderState()
	{
		return new State();
	}

	@Override
	public void extractRenderState(FluidDisplayTileEntity te, State state, float partialTicks, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumbling)
	{
		BlockEntityRenderer.super.extractRenderState(te, state, partialTicks, cameraPos, crumbling);

		FluidStack fluidStack = te.getFluidStack();
		boolean empty = fluidStack == null || fluidStack.isEmpty();
		net.minecraft.client.renderer.texture.TextureAtlas atlas = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(AtlasIds.BLOCKS);

		if (empty)
		{
			state.sprite = atlas.getSprite(EMPTY_SPRITE);
			state.r = state.g = state.b = 1.0F;
		}
		else
		{
			IClientFluidTypeExtensions attributes = IClientFluidTypeExtensions.of(fluidStack.getFluid());
			Identifier spriteLocation = te.flowing() ? attributes.getFlowingTexture(fluidStack) : attributes.getStillTexture(fluidStack);

			state.sprite = atlas.getSprite(spriteLocation);
			int color = attributes.getTintColor(fluidStack);
			state.r = (color >> 16 & 0xFF) / 255F;
			state.g = (color >> 8 & 0xFF) / 255F;
			state.b = (color & 0xFF) / 255F;
		}

		state.rotation = te.getRotation().ordinal() * 90F;
	}

	@Override
	public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera)
	{
		if (state.sprite == null)
		{
			return;
		}

		poseStack.pushPose();
		poseStack.translate(0.5, 0, 0.5);
		poseStack.mulPose(Axis.YP.rotationDegrees(state.rotation));
		poseStack.translate(-0.5, 0, -0.5);

		float minU = state.sprite.getU0();
		float maxU = state.sprite.getU1();
		float minV = state.sprite.getV0();
		float maxV = state.sprite.getV1();
		int light = state.lightCoords;
		float r = state.r;
		float g = state.g;
		float b = state.b;

		collector.submitCustomGeometry(poseStack, RenderTypes.solidMovingBlock(), (pose, buffer) -> {
			// -Y (bottom), DOWN brightness (0.5)
			quad(pose, buffer, light, 0, -1, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, minU, maxU, minV, maxV, r, g, b, 0.5F);
			// +Y (top), UP brightness (1.0)
			quad(pose, buffer, light, 0, 1, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0, minU, maxU, minV, maxV, r, g, b, 1.0F);
			// -Z (north), NORTH brightness (0.8)
			quad(pose, buffer, light, 0, 0, -1, 0, 1, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, minU, maxU, minV, maxV, r, g, b, 0.8F);
			// +Z (south), SOUTH brightness (0.8)
			quad(pose, buffer, light, 0, 0, 1, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, minU, maxU, minV, maxV, r, g, b, 0.8F);
			// -X (west), WEST brightness (0.6)
			quad(pose, buffer, light, -1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0, 1, 0, minU, maxU, minV, maxV, r, g, b, 0.6F);
			// +X (east), EAST brightness (0.6)
			quad(pose, buffer, light, 1, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 0, 0, minU, maxU, minV, maxV, r, g, b, 0.6F);
		});

		poseStack.popPose();
	}

	private static void quad(PoseStack.Pose pose, VertexConsumer buffer, int light, float nx, float ny, float nz, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float minU, float maxU, float minV, float maxV, float r, float g, float b, float brightness)
	{
		float fr = r * brightness;
		float fg = g * brightness;
		float fb = b * brightness;

		buffer.addVertex(pose, x1, y1, z1).setColor(fr, fg, fb, 1.0F).setUv(minU, minV).setLight(light).setNormal(pose, nx, ny, nz);
		buffer.addVertex(pose, x2, y2, z2).setColor(fr, fg, fb, 1.0F).setUv(minU, maxV).setLight(light).setNormal(pose, nx, ny, nz);
		buffer.addVertex(pose, x3, y3, z3).setColor(fr, fg, fb, 1.0F).setUv(maxU, maxV).setLight(light).setNormal(pose, nx, ny, nz);
		buffer.addVertex(pose, x4, y4, z4).setColor(fr, fg, fb, 1.0F).setUv(maxU, minV).setLight(light).setNormal(pose, nx, ny, nz);
	}
}
