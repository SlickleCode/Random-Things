package lumien.randomthings.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;

public class StainedBrickBlock extends Block
{
	private final boolean luminous;

	public boolean isLuminous()
	{
		return luminous;
	}

	public StainedBrickBlock(boolean luminous)
	{
		super(withLight(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(2.0F, 10.0F).sound(SoundType.STONE), luminous));

		this.luminous = luminous;
	}

	private static Block.Properties withLight(Block.Properties properties, boolean luminous)
	{
		return luminous ? properties.lightLevel(state -> 15).noOcclusion() : properties;
	}
}
