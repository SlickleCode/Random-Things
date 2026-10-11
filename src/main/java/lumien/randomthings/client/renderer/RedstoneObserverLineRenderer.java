package lumien.randomthings.client.renderer;

import lumien.randomthings.util.WeakSets;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.item.RedstoneToolItem;
import lumien.randomthings.tileentity.RedstoneObserverTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

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

	public static void render(MatrixStack matrixStack)
	{
		PlayerEntity player = Minecraft.getInstance().player;

		if (player == null)
		{
			return;
		}

		ItemStack main = player.getHeldItemMainhand();
		ItemStack off = player.getHeldItemOffhand();

		if (!(main.getItem() instanceof RedstoneToolItem) && !(off.getItem() instanceof RedstoneToolItem))
		{
			return;
		}

		ActiveRenderInfo ari = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();
		Vector3d camera = ari.getProjectedView();
		BlockPos playerPos = player.getPosition();

		RenderSystem.disableTexture();
		RenderSystem.lineWidth(4F);

		matrixStack.push();
		matrixStack.translate(-camera.x, -camera.y, -camera.z);
		Matrix4f matrix = matrixStack.getLast().getMatrix();

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

		for (RedstoneObserverTileEntity observer : WeakSets.snapshot(RedstoneObserverTileEntity.loadedObservers))
		{
			if (observer.isRemoved() || observer.getWorld() != player.world || observer.getTarget() == null)
			{
				continue;
			}

			BlockPos pos = observer.getPos();
			BlockPos target = observer.getTarget();

			if (pos.distanceSq(playerPos) > MAX_DISTANCE_SQ && target.distanceSq(playerPos) > MAX_DISTANCE_SQ)
			{
				continue;
			}

			buffer.pos(matrix, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F).color(1F, 0F, 0F, 1F).endVertex();
			buffer.pos(matrix, target.getX() + 0.5F, target.getY() + 0.5F, target.getZ() + 0.5F).color(1F, 0F, 0F, 1F).endVertex();
		}

		tessellator.draw();

		matrixStack.pop();
		RenderSystem.enableTexture();
	}
}
