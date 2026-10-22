package lumien.randomthings.client.util;

import com.mojang.blaze3d.platform.GlStateManager;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.GameRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;

public class RenderUtils
{
	public static void drawCube(PoseStack matrixStack, float posX, float posY, float posZ, float width, float length, float height, int red, int green, int blue, int alpha)
	{
		Tesselator t = Tesselator.getInstance();

		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		matrixStack.pushPose();
		matrixStack.translate(posX, posY, posZ);
		Matrix4f m = matrixStack.last().pose();

		BufferBuilder wr = t.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

		wr.addVertex(m, 0F, 0F, 0F).setColor(red, green, blue, alpha); // P1
		wr.addVertex(m, 0F, height, 0F).setColor(red, green, blue, alpha); // P2
		wr.addVertex(m, width, height, 0F).setColor(red, green, blue, alpha); // P3
		wr.addVertex(m, width, 0F, 0F).setColor(red, green, blue, alpha); // P4

		wr.addVertex(m, width, height, 0F).setColor(red, green, blue, alpha); // P1
		wr.addVertex(m, width, height, length).setColor(red, green, blue, alpha); // P2
		wr.addVertex(m, width, 0F, length).setColor(red, green, blue, alpha); // P3
		wr.addVertex(m, width, 0F, 0F).setColor(red, green, blue, alpha); // P4

		wr.addVertex(m, width, height, length).setColor(red, green, blue, alpha); // P1
		wr.addVertex(m, 0F, height, length).setColor(red, green, blue, alpha); // P1
		wr.addVertex(m, 0F, 0F, length).setColor(red, green, blue, alpha); // P1
		wr.addVertex(m, width, 0F, length).setColor(red, green, blue, alpha); // P1

		wr.addVertex(m, 0F, height, length).setColor(red, green, blue, alpha); // P1
		wr.addVertex(m, 0F, height, 0F).setColor(red, green, blue, alpha); // P1
		wr.addVertex(m, 0F, 0F, 0F).setColor(red, green, blue, alpha); // P1
		wr.addVertex(m, 0F, 0F, length).setColor(red, green, blue, alpha); // P1

		wr.addVertex(m, 0F, 0F, 0F).setColor(red, green, blue, alpha); // P1
		wr.addVertex(m, width, 0F, 0F).setColor(red, green, blue, alpha); // P1
		wr.addVertex(m, width, 0F, length).setColor(red, green, blue, alpha); // P1
		wr.addVertex(m, 0F, 0F, length).setColor(red, green, blue, alpha); // P1

		wr.addVertex(m, 0F, height, 0F).setColor(red, green, blue, alpha); // P1
		wr.addVertex(m, 0F, height, length).setColor(red, green, blue, alpha); // P2
		wr.addVertex(m, width, height, length).setColor(red, green, blue, alpha); // P3
		wr.addVertex(m, width, height, 0F).setColor(red, green, blue, alpha); // P4

		BufferUploader.drawWithShader(wr.buildOrThrow());

		matrixStack.popPose();
		}

	public static void drawCube(PoseStack matrixStack, float posX, float posY, float posZ, float size, int red, int green, int blue, int alpha)
	{
		drawCube(matrixStack, posX, posY, posZ, size, size, size, red, green, blue, alpha);
	}

	public static void drawCylinder(float x1, float y1, float z1, float x2, float y2, float z2)
	{
		Tesselator t = Tesselator.getInstance();
		Camera ari = Minecraft.getInstance().gameRenderer.getMainCamera();

		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		GL11.glPointSize(1.0f);
		BufferBuilder wr = t.begin(com.mojang.blaze3d.vertex.VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

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

			wr.addVertex(x1 + x, y1 + y, z1 + z).setColor(255, 0, 0, 255);
		}

		BufferUploader.drawWithShader(wr.buildOrThrow());



		}

	public static void enableDefaultBlending()
	{
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
}
