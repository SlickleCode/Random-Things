package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * Wraps a vertex builder and scales every vertex's alpha. The 1.14.4 Diaphanous Block renderer patched the alpha
 * bytes of the already-built vertex buffer; since 1.16 quads are built through {@link VertexConsumer}'s default methods
 * (which all end in {@code setColor(int, int, int, int)}), so overriding that one method is enough.
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
	public VertexConsumer addVertex(float x, float y, float z)
	{
		this.delegate.addVertex(x, y, z);
		return this;
	}

	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha)
	{
		this.delegate.setColor(red, green, blue, Math.min(255, Math.max(0, Math.round(alpha * this.alpha))));
		return this;
	}

	@Override
	public VertexConsumer setUv(float u, float v)
	{
		this.delegate.setUv(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv1(int u, int v)
	{
		this.delegate.setUv1(u, v);
		return this;
	}

	@Override
	public VertexConsumer setUv2(int u, int v)
	{
		this.delegate.setUv2(u, v);
		return this;
	}

	@Override
	public VertexConsumer setNormal(float x, float y, float z)
	{
		this.delegate.setNormal(x, y, z);
		return this;
	}
}
