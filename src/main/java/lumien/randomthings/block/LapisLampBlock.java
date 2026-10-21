package lumien.randomthings.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;

/**
 * A static, always-on decorative light source that should never block
 * hostile mob spawns despite being bright - see {@code
 * AsmHandler#overrideSpawnResult} for the spawn-permitting half, which is
 * handled independently of this block's (now perfectly ordinary) light
 * value.
 * <p>
 * 1.12.2's real mechanism was a per-side {@code getLightValue(state, world,
 * pos)} light-value trick instead (report 0 to the server so the real
 * placement predicate naturally allows spawns) - tried porting that exactly
 * via {@code net.neoforged.fml.common.thread.EffectiveSide#get()} on
 * 2026-09-27, but reverted the same day: real in-game testing showed it
 * backwards (Lapis Lamp started blocking spawns instead of allowing them).
 * Root cause, found by reading {@code EffectiveSide}'s own source: it
 * returns {@code LogicalSide.CLIENT} for ANY thread not part of FML's own
 * {@code SidedThreadGroup} - and 1.14.4's block-light propagation for a
 * placed block doesn't reliably run on that specific thread (chunk/light
 * processing can happen on background worker threads FML doesn't tag),
 * so the "server" branch silently never fired where it mattered. Back to
 * the coremod force-ALLOW instead, now with an explicit peaceful-difficulty
 * check at the actual call site (see {@code AsmHandler#overrideSpawnResult})
 * rather than a side-detection trick that doesn't reliably work in this
 * Forge version for this code path.
 */
public class LapisLampBlock extends Block
{
	public LapisLampBlock()
	{
		super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.COLOR_BLUE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.HAT).strength(0.3F).lightLevel(state -> 15).sound(SoundType.GLASS));
	}
}
