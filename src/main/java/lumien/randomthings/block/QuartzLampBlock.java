package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

/**
 * Looks dark (no light emitted) but still prevents hostile mob spawns nearby
 * - see the dedicated {@code LivingSpawnEvent.CheckSpawn} listener in
 * {@code RandomThings} for the spawn-prevention half, which is handled
 * independently of this block's (now perfectly ordinary) light value.
 */
public class QuartzLampBlock extends Block
{
	public QuartzLampBlock()
	{
		super(Block.Properties.create(Material.GLASS, MaterialColor.QUARTZ).hardnessAndResistance(0.3F).sound(SoundType.GLASS));
	}
}
