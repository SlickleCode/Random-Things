package lumien.randomthings.client.util;

import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class RenderUtils
{
	/** An axis aligned box as 6 quads in the colour format of the debug quad render type (1.21.6+ draws through a {@link VertexConsumer} of a render type). */
	public static void drawCube(PoseStack matrixStack, VertexConsumer wr, float posX, float posY, float posZ, float width, float length, float height, int red, int green, int blue, int alpha)
	{
		matrixStack.pushPose();
		matrixStack.translate(posX, posY, posZ);

		Matrix4f m = matrixStack.last().pose();

		wr.addVertex(m, 0F, 0F, 0F).setColor(red, green, blue, alpha);
		wr.addVertex(m, 0F, height, 0F).setColor(red, green, blue, alpha);
		wr.addVertex(m, width, height, 0F).setColor(red, green, blue, alpha);
		wr.addVertex(m, width, 0F, 0F).setColor(red, green, blue, alpha);

		wr.addVertex(m, width, height, 0F).setColor(red, green, blue, alpha);
		wr.addVertex(m, width, height, length).setColor(red, green, blue, alpha);
		wr.addVertex(m, width, 0F, length).setColor(red, green, blue, alpha);
		wr.addVertex(m, width, 0F, 0F).setColor(red, green, blue, alpha);

		wr.addVertex(m, width, height, length).setColor(red, green, blue, alpha);
		wr.addVertex(m, 0F, height, length).setColor(red, green, blue, alpha);
		wr.addVertex(m, 0F, 0F, length).setColor(red, green, blue, alpha);
		wr.addVertex(m, width, 0F, length).setColor(red, green, blue, alpha);

		wr.addVertex(m, 0F, height, length).setColor(red, green, blue, alpha);
		wr.addVertex(m, 0F, height, 0F).setColor(red, green, blue, alpha);
		wr.addVertex(m, 0F, 0F, 0F).setColor(red, green, blue, alpha);
		wr.addVertex(m, 0F, 0F, length).setColor(red, green, blue, alpha);

		wr.addVertex(m, 0F, 0F, 0F).setColor(red, green, blue, alpha);
		wr.addVertex(m, width, 0F, 0F).setColor(red, green, blue, alpha);
		wr.addVertex(m, width, 0F, length).setColor(red, green, blue, alpha);
		wr.addVertex(m, 0F, 0F, length).setColor(red, green, blue, alpha);

		wr.addVertex(m, 0F, height, 0F).setColor(red, green, blue, alpha);
		wr.addVertex(m, 0F, height, length).setColor(red, green, blue, alpha);
		wr.addVertex(m, width, height, length).setColor(red, green, blue, alpha);
		wr.addVertex(m, width, height, 0F).setColor(red, green, blue, alpha);

		matrixStack.popPose();
	}

	public static void drawCube(PoseStack matrixStack, VertexConsumer wr, float posX, float posY, float posZ, float size, int red, int green, int blue, int alpha)
	{
		drawCube(matrixStack, wr, posX, posY, posZ, size, size, size, red, green, blue, alpha);
	}
}
