package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;

/**
 * Looks dark (no light emitted) but still prevents hostile mob spawns nearby
 * - see {@code AsmHandler#overrideSpawnResult} for the spawn-prevention
 * half, which is handled independently of this block's (now perfectly
 * ordinary) light value. See {@link LapisLampBlock}'s javadoc for why this
 * isn't a per-side {@code getLightValue} trick like 1.12.2's original - that
 * was tried and reverted the same day (2026-09-27) after real testing showed
 * it backwards, root-caused to {@code EffectiveSide.get()} not reliably
 * detecting "server" for 1.14.4's block-light propagation.
 */
public class QuartzLampBlock extends Block
{
	public QuartzLampBlock()
	{
		super(Block.Properties.create(Material.GLASS, MaterialColor.QUARTZ).hardnessAndResistance(0.3F).sound(SoundType.GLASS));
	}
}
