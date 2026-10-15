package lumien.randomthings.tileentity;

import net.minecraft.world.level.block.state.BlockState;

import lumien.randomthings.util.WeakSets;
import java.util.Set;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

/**
 * A small glowing block that forces its own chunk to always (when unpowered)
 * or never (when redstone-powered) allow slime spawns, overriding vanilla's
 * normal per-chunk slime-chunk RNG gate. Was ASM patch #... in 1.12.2
 * (`AsmHandler.overrideSlimeChunk`, injected into the vanilla slime-chunk
 * check); replaced here with a `LivingSpawnEvent.CheckSpawn` listener (see
 * `RandomThings.java`), the same clean 1.14.4 hook this plan's ASM audit
 * already identified for this behavior.
 * <p>
 * Simplification, disclosed here: 1.12.2's ASM patch overrode specifically
 * the deterministic per-chunk "is this a slime chunk" RNG gate used by the
 * cave slime-spawn path. `LivingSpawnEvent.CheckSpawn` fires for every mob
 * spawn attempt instead (natural, spawner, etc.), so this forces ALLOW/DENY
 * for any slime spawn attempt located in the cube's chunk, rather than
 * precisely re-targeting just that one RNG check - functionally very close
 * (the cube's chunk always/never gets slimes) but not a byte-for-byte
 * re-creation of the original injection point.
 */
public class SlimeCubeTileEntity extends BlockEntity
{
	public static final Set<SlimeCubeTileEntity> cubes = WeakSets.create();

	private boolean powered;

	public SlimeCubeTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.SLIME_CUBE, pos, state);
		cubes.add(this);
    }

	@Override
	public void setRemoved()
	{
		super.setRemoved();
		cubes.remove(this);
	}

	public boolean isPowered()
	{
		return powered;
	}

	public void updatePowerState(boolean newPowered)
	{
		this.powered = newPowered;
	}

	public boolean isInChunk(Level world, ChunkPos chunkPos)
	{
		if (this.level != world || this.worldPosition == null)
		{
			return false;
		}

		BlockPos p = this.worldPosition;
		return (p.getX() >> 4) == chunkPos.x && (p.getZ() >> 4) == chunkPos.z;
	}
}
