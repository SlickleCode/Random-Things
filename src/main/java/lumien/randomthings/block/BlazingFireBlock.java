package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.SoundType;
import net.minecraft.world.IWorldReader;

/**
 * A faster-spreading fire variant. The 1.12.2 version was a near-total
 * reimplementation of vanilla's fire-spread algorithm (its own
 * tryCatchFire/getNeighborEncouragement copy-pasted from BlockFire, tuned
 * with a 4x higher catch-chance multiplier and faster per-catch age growth)
 * because 1.12's BlockFire didn't expose those pieces for reuse.
 *
 * In 1.14.4, FireBlock.tickRate(IWorldReader) is a clean public override
 * point - reusing it here (vanilla's default is 30; halving it here roughly
 * doubles the effective spread rate over time) gets most of the "blazing"
 * feel with zero duplicated logic. The catch-chance and age-growth tweaks
 * live inside FireBlock's private tryCatchFire/tick internals in this
 * version and aren't overridable without copying the whole algorithm, so
 * they're not reproduced - this trades exact vanilla-tweak fidelity for not
 * maintaining a second copy of vanilla's fire logic.
 */
public class BlazingFireBlock extends FireBlock
{
	public BlazingFireBlock()
	{
		super(Block.Properties.from(Blocks.FIRE).sound(SoundType.CLOTH));
	}

	@Override
	public int tickRate(IWorldReader worldIn)
	{
		return 15;
	}
}
