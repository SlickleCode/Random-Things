package lumien.randomthings.block;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

/**
 * Shared physics for every Super Lubricent block (Ice/Platform/Stone):
 * clamp horizontal speed so the true-zero-friction movement (see
 * {@link SuperLubricentPlatformBlock}'s class javadoc for the
 * friction-formula derivation) doesn't accelerate forever - new behavior
 * beyond 1.12.2, not a port of it, see the callers for the
 * disclosed-deviation details.
 * <p>
 * Boots-negation used to live here too, but it was backwards: see
 * {@code SuperLubricentBootsMixin} for the real 1.12.2 behavior (the boots
 * make every surface maximally slippery, not just these three blocks) and
 * why that requires a Mixin instead of a per-block override.
 * <p>
 * {@link #capHorizontalSpeed} is public and called from a
 * {@code LivingUpdateEvent} listener in {@code RandomThings}'s constructor,
 * not from {@code onEntityCollision} on these blocks - that hook only fires
 * when an entity's hitbox actually overlaps the block's collision volume,
 * which never happens for an entity simply resting on top of one (confirmed
 * by the cap never engaging in testing despite a correct implementation).
 * The listener instead looks up the block directly underfoot using the same
 * position formula vanilla's own friction code uses in
 * {@code LivingEntity.travel} (one full block below the entity's bounding
 * box), confirmed via {@code javap -c} disassembly.
 */
public final class SuperLubricentPhysics
{
	/**
	 * Blocks/tick. Vanilla sprint speed is a well-established, community-
	 * measured ~5.612 m/s (0.2806 blocks/tick) - not something bytecode alone
	 * can confirm, since it's an emergent steady-state of the
	 * acceleration/friction simulation, not a fixed source constant. This is
	 * set modestly above that (~25%), matching "slightly faster than
	 * sprinting" per user direction. Tune freely.
	 */
	static final double MAX_HORIZONTAL_SPEED = 0.35D;

	private SuperLubricentPhysics()
	{
	}

	public static void capHorizontalSpeed(Entity entity)
	{
		Vec3d motion = entity.getMotion();
		double horizontalSpeedSq = motion.x * motion.x + motion.z * motion.z;
		double maxSpeedSq = MAX_HORIZONTAL_SPEED * MAX_HORIZONTAL_SPEED;

		if (horizontalSpeedSq > maxSpeedSq)
		{
			double scale = MAX_HORIZONTAL_SPEED / Math.sqrt(horizontalSpeedSq);
			entity.setMotion(motion.x * scale, motion.y, motion.z * scale);
		}
	}
}
