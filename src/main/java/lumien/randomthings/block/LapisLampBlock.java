package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

/**
 * A static, always-on decorative light source that should never block
 * hostile mob spawns despite being bright - see the dedicated
 * {@code LivingSpawnEvent.CheckSpawn} listener in {@code RandomThings} for
 * the spawn-permitting half, which is handled independently of this block's
 * (now perfectly ordinary) light value.
 */
public class LapisLampBlock extends Block
{
	public LapisLampBlock()
	{
		super(Block.Properties.create(Material.GLASS, MaterialColor.BLUE).hardnessAndResistance(0.3F).lightValue(15).sound(SoundType.GLASS));
	}
}
