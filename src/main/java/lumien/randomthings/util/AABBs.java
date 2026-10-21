package lumien.randomthings.util;

import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;

/** 1.20.5 removed the {@code AABB(BlockPos, BlockPos)} constructor. */
public final class AABBs
{
	private AABBs()
	{
	}

	public static AABB of(Vec3i a, Vec3i b)
	{
		return new AABB(a.getX(), a.getY(), a.getZ(), b.getX(), b.getY(), b.getZ());
	}
}
