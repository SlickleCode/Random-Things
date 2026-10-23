package lumien.randomthings.client;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/** A model whose every quad has tint index 0, so the Dyeing Machine colour (tint 0 of the item's render layer) reaches quads that declare no tint. */
public class DyedModel implements BakedModel
{
	private final BakedModel original;

	public DyedModel(BakedModel original)
	{
		this.original = original;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand)
	{
		return this.original.getQuads(state, side, rand).stream().map(quad -> quad.getTintIndex() == 0 ? quad : new BakedQuad(quad.getVertices(), 0, quad.getDirection(), quad.getSprite(), quad.isShade(), quad.getLightEmission())).collect(Collectors.toList());
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return this.original.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return this.original.isGui3d();
	}

	@Override
	public boolean usesBlockLight()
	{
		return this.original.usesBlockLight();
	}

	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		return this.original.getParticleIcon();
	}

	@Override
	public ItemTransforms getTransforms()
	{
		return this.original.getTransforms();
	}
}
