package lumien.randomthings.client.util;

import com.mojang.blaze3d.platform.GlStateManager;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Matrix4f;

public class RenderUtils
{
	public static void drawCube(MatrixStack matrixStack, float posX, float posY, float posZ, float width, float length, float height, int red, int green, int blue, int alpha)
	{
		Tessellator t = Tessellator.getInstance();
		BufferBuilder wr = t.getBuffer();

		RenderSystem.disableTexture();

		matrixStack.push();
		matrixStack.translate(posX, posY, posZ);
		Matrix4f m = matrixStack.getLast().getMatrix();

		wr.begin(7, DefaultVertexFormats.POSITION_COLOR);

		wr.pos(m, 0F, 0F, 0F).color(red, green, blue, alpha).endVertex(); // P1
		wr.pos(m, 0F, height, 0F).color(red, green, blue, alpha).endVertex(); // P2
		wr.pos(m, width, height, 0F).color(red, green, blue, alpha).endVertex(); // P3
		wr.pos(m, width, 0F, 0F).color(red, green, blue, alpha).endVertex(); // P4

		wr.pos(m, width, height, 0F).color(red, green, blue, alpha).endVertex(); // P1
		wr.pos(m, width, height, length).color(red, green, blue, alpha).endVertex(); // P2
		wr.pos(m, width, 0F, length).color(red, green, blue, alpha).endVertex(); // P3
		wr.pos(m, width, 0F, 0F).color(red, green, blue, alpha).endVertex(); // P4

		wr.pos(m, width, height, length).color(red, green, blue, alpha).endVertex(); // P1
		wr.pos(m, 0F, height, length).color(red, green, blue, alpha).endVertex(); // P1
		wr.pos(m, 0F, 0F, length).color(red, green, blue, alpha).endVertex(); // P1
		wr.pos(m, width, 0F, length).color(red, green, blue, alpha).endVertex(); // P1

		wr.pos(m, 0F, height, length).color(red, green, blue, alpha).endVertex(); // P1
		wr.pos(m, 0F, height, 0F).color(red, green, blue, alpha).endVertex(); // P1
		wr.pos(m, 0F, 0F, 0F).color(red, green, blue, alpha).endVertex(); // P1
		wr.pos(m, 0F, 0F, length).color(red, green, blue, alpha).endVertex(); // P1

		wr.pos(m, 0F, 0F, 0F).color(red, green, blue, alpha).endVertex(); // P1
		wr.pos(m, width, 0F, 0F).color(red, green, blue, alpha).endVertex(); // P1
		wr.pos(m, width, 0F, length).color(red, green, blue, alpha).endVertex(); // P1
		wr.pos(m, 0F, 0F, length).color(red, green, blue, alpha).endVertex(); // P1

		wr.pos(m, 0F, height, 0F).color(red, green, blue, alpha).endVertex(); // P1
		wr.pos(m, 0F, height, length).color(red, green, blue, alpha).endVertex(); // P2
		wr.pos(m, width, height, length).color(red, green, blue, alpha).endVertex(); // P3
		wr.pos(m, width, height, 0F).color(red, green, blue, alpha).endVertex(); // P4

		t.draw();

		matrixStack.pop();
		RenderSystem.enableTexture();
	}

	public static void drawCube(MatrixStack matrixStack, float posX, float posY, float posZ, float size, int red, int green, int blue, int alpha)
	{
		drawCube(matrixStack, posX, posY, posZ, size, size, size, red, green, blue, alpha);
	}

	public static void drawCylinder(float x1, float y1, float z1, float x2, float y2, float z2)
	{
		RenderSystem.disableTexture();

		Tessellator t = Tessellator.getInstance();
		BufferBuilder wr = t.getBuffer();
		ActiveRenderInfo ari = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();

		GL11.glPointSize(1.0f);
		wr.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);

		float radius = 2;

		float oX = 2;
		float oY = 2;
		float oZ = 0;

		Random rng = new Random(5);

		for (int i = 0; i < 1; i++)
		{
			double alpha = rng.nextFloat() * Math.PI * 2;
			double beta = rng.nextFloat() * Math.PI;

			float x = (float) (radius * Math.sin(alpha)) + oX;
			float y = (float) (radius * Math.cos(beta) * Math.cos(alpha)) + oY;
			float z = (float) (radius * Math.sin(beta) * Math.cos(alpha)) + oZ;

			wr.pos(x1 + x, y1 + y, z1 + z).color(255, 0, 0, 255).endVertex();
		}

		t.draw();



		RenderSystem.enableTexture();
	}

	public static void enableDefaultBlending()
	{
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
}
