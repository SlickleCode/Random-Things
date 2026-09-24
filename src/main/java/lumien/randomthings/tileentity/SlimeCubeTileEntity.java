package lumien.randomthings.tileentity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

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
public class SlimeCubeTileEntity extends TileEntity
{
	public static final Set<SlimeCubeTileEntity> cubes = Collections.newSetFromMap(new WeakHashMap<>());

	private boolean powered;

	public SlimeCubeTileEntity()
	{
		super(ModTileEntityTypes.SLIME_CUBE);
		cubes.add(this);
	}

	@Override
	public void remove()
	{
		super.remove();
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

	public boolean isInChunk(World world, ChunkPos chunkPos)
	{
		if (this.world != world || this.pos == null)
		{
			return false;
		}

		BlockPos p = this.pos;
		return (p.getX() >> 4) == chunkPos.x && (p.getZ() >> 4) == chunkPos.z;
	}
}
