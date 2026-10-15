package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Wraps a vertex builder and scales every vertex's alpha. The 1.14.4 Diaphanous Block renderer patched the alpha
 * bytes of the already-built vertex buffer; 1.16 builds quads through {@link IVertexBuilder}'s default methods
 * (which all end in {@code color(int, int, int, int)}), so overriding that one method is enough.
 */
public class AlphaVertexBuilder implements VertexConsumer
{
	private final VertexConsumer delegate;
	private final float alpha;

	public AlphaVertexBuilder(VertexConsumer delegate, float alpha)
	{
		this.delegate = delegate;
		this.alpha = alpha;
	}

	@Override
	public VertexConsumer vertex(double x, double y, double z)
	{
		this.delegate.vertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha)
	{
		this.delegate.color(red, green, blue, Math.min(255, Math.max(0, Math.round(alpha * this.alpha))));
		return this;
	}

	@Override
	public VertexConsumer uv(float u, float v)
	{
		this.delegate.uv(u, v);
		return this;
	}

	@Override
	public VertexConsumer overlayCoords(int u, int v)
	{
		this.delegate.overlayCoords(u, v);
		return this;
	}

	@Override
	public VertexConsumer uv2(int u, int v)
	{
		this.delegate.uv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer normal(float x, float y, float z)
	{
		this.delegate.normal(x, y, z);
		return this;
	}

	@Override
	public void defaultColor(int red, int green, int blue, int alpha)
	{
		this.delegate.defaultColor(red, green, blue, Math.min(255, Math.max(0, Math.round(alpha * this.alpha))));
	}

	@Override
	public void unsetDefaultColor()
	{
		this.delegate.unsetDefaultColor();
	}

	@Override
	public void endVertex()
	{
		this.delegate.endVertex();
	}
}
