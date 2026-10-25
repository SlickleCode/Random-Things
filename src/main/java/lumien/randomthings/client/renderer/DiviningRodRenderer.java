package lumien.randomthings.client.renderer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import org.joml.Vector3f;



import lumien.randomthings.client.util.RenderUtils;
import lumien.randomthings.item.DiviningRodItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;

public class DiviningRodRenderer
{
	public static DiviningRodRenderer INSTANCE;

	LinkedHashSet<BlockPos> positionsToCheck;

	List<Indicator> indicators;

	HashMap<Block, Color> blockColorMap;

	public DiviningRodRenderer()
	{
		positionsToCheck = new LinkedHashSet<BlockPos>();
		indicators = new ArrayList<Indicator>();
	}

	public boolean shouldGlow(DiviningRodItem rodType)
	{
		return !indicators.isEmpty() && (indicators.stream().anyMatch((i) -> i.type == rodType));
	}

	int modX, modY, modZ;

	public void render(PoseStack matrixStack)
	{
		if (indicators.isEmpty())
		{
			return;
		}

		Camera ari = Minecraft.getInstance().gameRenderer.getMainCamera();
		Vec3 pos = ari.position();

		net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
		com.mojang.blaze3d.vertex.VertexConsumer consumer = bufferSource.getBuffer(RenderTypes.debugQuads());

		matrixStack.pushPose();
		matrixStack.translate(-pos.x(), -pos.y(), -pos.z());

		for (Indicator indicator : indicators)
		{
			float size = (1 - (indicator.duration / 160F)) * 0.2F + 0.1F;
			Color c = indicator.color;

			RenderUtils.drawCube(matrixStack, consumer, (float) (indicator.target.getX() + 0.5 - size / 2), (float) (indicator.target.getY() + 0.5 - size / 2), (float) (indicator.target.getZ() + 0.5 - size / 2), size, c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
		}

		matrixStack.popPose();
		bufferSource.endBatch(RenderTypes.debugQuads());
	}

	public void tick()
	{
		Iterator<Indicator> indicatorIterator = indicators.iterator();

		while (indicatorIterator.hasNext())
		{
			Indicator i = indicatorIterator.next();

			i.duration--;

			if (i.duration == 0)
			{
				indicatorIterator.remove();
			}
		}

		Player player = Minecraft.getInstance().player;

		if (player != null)
		{
			Level world = player.level();

			if (world != null)
			{
				ItemStack main = player.getMainHandItem();
				ItemStack off = player.getOffhandItem();

				ItemStack rod = ItemStack.EMPTY;
				DiviningRodItem type;

				if (!main.isEmpty() && main.getItem() instanceof DiviningRodItem)
				{
					rod = main;
				}
				else if (!off.isEmpty() && off.getItem() instanceof DiviningRodItem)
				{
					rod = off;
				}

				if (!rod.isEmpty())
				{
					type = (DiviningRodItem) rod.getItem();
					BlockPos playerPos = player.blockPosition();

					for (int i = 0; i < 60; i++)
					{
						modX++;

						if (modX == 6)
						{
							modX = -5;
							modZ++;

							if (modZ == 6)
							{
								modZ = -5;
								modY++;

								if (modY == 6)
								{
									modY = -5;
								}
							}
						}

						BlockPos target = playerPos.offset(modX, modY, modZ);
						BlockState blockState = world.getBlockState(target);

						if (world.hasChunkAt(target))
						{
							int matchIndex = type.matches(blockState);
							if (matchIndex != -1)
							{
								Indicator indicator = new Indicator(target, 160, type.getColor(matchIndex), type);

								indicators.add(indicator);
							}
						}
					}
				}
			}
		}
	}

	private static class Indicator
	{
		BlockPos target;
		int duration;
		Color color;

		DiviningRodItem type;

		public Indicator(BlockPos target, int duration, Color color, DiviningRodItem type)
		{
			super();
			this.target = target;
			this.duration = duration;
			this.color = color;
			this.type = type;
		}
	}

	public static DiviningRodRenderer get()
	{
		if (INSTANCE == null)
		{
			INSTANCE = new DiviningRodRenderer();
		}
		return INSTANCE;
	}
}
