package lumien.randomthings.client.renderer;

import lumien.randomthings.util.WeakSets;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.item.RedstoneToolItem;
import lumien.randomthings.tileentity.RedstoneObserverTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;

/**
 * New feature, not a 1.12.2 port: draws a red line from every loaded, linked
 * Redstone Observer to its target while holding the Redstone Tool.
 * <p>
 * Checked the actual 1.12.2 source first: its line-drawing code
 * ({@code ClientProxy.drawInterfaceLines}) only ever covered the wireless
 * Redstone Interface, a separate, still-deferred subsystem (needs Mixin
 * work) that happens to share the same Redstone Tool item for linking.
 * Redstone Observer's real 1.12.2 feedback was just its read-only GUI
 * showing the target's coordinates - this renderer is a deliberate
 * deviation added per explicit user request, not a missed port detail.
 */
public class RedstoneObserverLineRenderer
{
	private static final double MAX_DISTANCE = 20;
	private static final double MAX_DISTANCE_SQ = MAX_DISTANCE * MAX_DISTANCE;

	public static void render(PoseStack matrixStack)
	{
		Player player = Minecraft.getInstance().player;

		if (player == null)
		{
			return;
		}

		ItemStack main = player.getMainHandItem();
		ItemStack off = player.getOffhandItem();

		if (!(main.getItem() instanceof RedstoneToolItem) && !(off.getItem() instanceof RedstoneToolItem))
		{
			return;
		}

		Camera ari = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 camera = ari.getPosition();
		BlockPos playerPos = player.blockPosition();

		RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
		RenderSystem.lineWidth(4F);

		matrixStack.pushPose();
		matrixStack.translate(-camera.x, -camera.y, -camera.z);
		Matrix4f matrix = matrixStack.last().pose();

		Tesselator tessellator = Tesselator.getInstance();
		BufferBuilder buffer = tessellator.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);

		for (RedstoneObserverTileEntity observer : WeakSets.snapshot(RedstoneObserverTileEntity.loadedObservers))
		{
			if (observer.isRemoved() || observer.getLevel() != player.level() || observer.getTarget() == null)
			{
				continue;
			}

			BlockPos pos = observer.getBlockPos();
			BlockPos target = observer.getTarget();

			if (pos.distSqr(playerPos) > MAX_DISTANCE_SQ && target.distSqr(playerPos) > MAX_DISTANCE_SQ)
			{
				continue;
			}

			float dx = target.getX() - pos.getX();
			float dy = target.getY() - pos.getY();
			float dz = target.getZ() - pos.getZ();
			float length = Math.max(1.0E-4F, (float) Math.sqrt(dx * dx + dy * dy + dz * dz));
			dx /= length;
			dy /= length;
			dz /= length;

			// the 1.17+ line shader widens each line along its normal, so the normal is the line direction
			buffer.addVertex(matrix, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F).setColor(1F, 0F, 0F, 1F).setNormal(matrixStack.last(), dx, dy, dz);
			buffer.addVertex(matrix, target.getX() + 0.5F, target.getY() + 0.5F, target.getZ() + 0.5F).setColor(1F, 0F, 0F, 1F).setNormal(matrixStack.last(), dx, dy, dz);
		}

		BufferUploader.drawWithShader(buffer.buildOrThrow());

		matrixStack.popPose();
		}
}
