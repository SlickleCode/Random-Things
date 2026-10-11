package lumien.randomthings.client.renderer;

import com.mojang.blaze3d.vertex.IVertexBuilder;

/**
 * Wraps a vertex builder and scales every vertex's alpha. The 1.14.4 Diaphanous Block renderer patched the alpha
 * bytes of the already-built vertex buffer; 1.16 builds quads through {@link IVertexBuilder}'s default methods
 * (which all end in {@code color(int, int, int, int)}), so overriding that one method is enough.
 */
public class AlphaVertexBuilder implements IVertexBuilder
{
	private final IVertexBuilder delegate;
	private final float alpha;

	public AlphaVertexBuilder(IVertexBuilder delegate, float alpha)
	{
		this.delegate = delegate;
		this.alpha = alpha;
	}

	@Override
	public IVertexBuilder pos(double x, double y, double z)
	{
		this.delegate.pos(x, y, z);
		return this;
	}

	@Override
	public IVertexBuilder color(int red, int green, int blue, int alpha)
	{
		this.delegate.color(red, green, blue, Math.min(255, Math.max(0, Math.round(alpha * this.alpha))));
		return this;
	}

	@Override
	public IVertexBuilder tex(float u, float v)
	{
		this.delegate.tex(u, v);
		return this;
	}

	@Override
	public IVertexBuilder overlay(int u, int v)
	{
		this.delegate.overlay(u, v);
		return this;
	}

	@Override
	public IVertexBuilder lightmap(int u, int v)
	{
		this.delegate.lightmap(u, v);
		return this;
	}

	@Override
	public IVertexBuilder normal(float x, float y, float z)
	{
		this.delegate.normal(x, y, z);
		return this;
	}

	@Override
	public void endVertex()
	{
		this.delegate.endVertex();
	}
}
