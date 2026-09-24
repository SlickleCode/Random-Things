package lumien.randomthings.client.renderer;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;

import lumien.randomthings.item.RedstoneToolItem;
import lumien.randomthings.tileentity.RedstoneObserverTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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

	public static void render()
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
		Vec3d camera = ari.getProjectedView();
		BlockPos playerPos = player.getPosition();

		GlStateManager.disableTexture();
		GlStateManager.disableLighting();
		GlStateManager.lineWidth(4F);
		GlStateManager.translated(-camera.x, -camera.y, -camera.z);

		GlStateManager.begin(GL11.GL_LINES);

		for (RedstoneObserverTileEntity observer : RedstoneObserverTileEntity.loadedObservers)
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

			GlStateManager.color4f(1F, 0F, 0F, 1F);
			GlStateManager.vertex3f(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
			GlStateManager.color4f(1F, 0F, 0F, 1F);
			GlStateManager.vertex3f(target.getX() + 0.5F, target.getY() + 0.5F, target.getZ() + 0.5F);
		}

		GlStateManager.end();

		GlStateManager.translated(camera.x, camera.y, camera.z);
		GlStateManager.enableLighting();
		GlStateManager.enableTexture();
	}
}
