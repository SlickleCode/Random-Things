package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Field;

/**
 * Shared physics for every Super Lubricent block (Ice/Platform/Stone):
 * clamp horizontal speed so the true-zero-friction movement (see
 * {@link SuperLubricentPlatformBlock}'s class javadoc for the
 * friction-formula derivation) doesn't accelerate forever - new behavior
 * beyond 1.12.2, not a port of it, see the callers for the
 * disclosed-deviation details.
 * <p>
 * Boots-negation used to live here too, but it was backwards: see
 * {@code SuperLubricentBootsTransformer.js} (a coremod, see
 * {@code lumien.randomthings.asm.AsmHandler#bootsMaxSlip}) for the real
 * 1.12.2 behavior (the boots make every surface maximally slippery, not just
 * these three blocks) and why that requires intercepting {@code
 * LivingEntity.travel} itself instead of a per-block override.
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
 * <p>
 * Real bug found and fixed, 2026-09-28 (reported by user: a boat resting on
 * one of these blocks accelerates without bound until it crashes the game,
 * unless something physically stops it): {@code LivingUpdateEvent} only
 * fires for {@code LivingEntity} - a {@code BoatEntity} is a plain {@code
 * Entity}, so it was never capped at all. First attempt: added a second,
 * {@code Entity}-typed {@code WorldTickEvent} listener sweeping every
 * grounded entity in the world. <b>User reported this didn't work at all -
 * still unbounded acceleration, and even just turning the boat in place ran
 * away too.</b> Root cause of both: ground-truthed {@code BoatEntity
 * #updateMotion}/{@code #getBoatGlide()} - a boat ON_LAND multiplies its
 * *existing* velocity AND its *turn rate* by the average slipperiness of the
 * blocks underneath every single tick ({@code momentum = boatGlide; motion
 * *= momentum; deltaRotation *= momentum}), regardless of whether {@code
 * onGround} (a plain boat resting still, not falling, may never actually set
 * that flag the way a walking entity does - the sweep's gate on it likely
 * never even engaged) or whether any paddle input is held at all - not an
 * acceleration-vs-friction balance like a walking/sprinting entity has,
 * which at least settles toward a steady state. These blocks' slipperiness
 * ({@code 1F / 0.91F}, chosen to exactly cancel the fixed 0.91 multiplier
 * living entities decay by every tick) is just above 1.0, so a boat's
 * momentum multiplier is *also* just above 1.0 - with no offsetting term at
 * all, both motion and turn rate compound unconditionally every tick until
 * they overflow.
 * <p>
 * Second attempt, same day: capped the slipperiness these blocks report
 * specifically to a {@code BoatEntity} (via {@code Block#getSlipperiness
 * (BlockState, IWorldReader, BlockPos, Entity)}, the exact call {@code
 * getBoatGlide()} makes) to vanilla's own {@code Blocks.BLUE_ICE} value
 * (0.989, the highest vanilla itself ever ships). That worked, but per
 * explicit user request afterward, boats should instead match how living
 * entities already behave here: true zero friction (speed never decays on
 * its own, exactly like a player standing on these blocks) with an external
 * cap on top, not a slightly-lossy slipperiness. <b>Current fix</b>: the
 * blocks are back to reporting the real zero-friction value
 * unconditionally for every entity (no per-entity override at all - see
 * each block's own class javadoc), and a boat's motion/turn-rate are capped
 * externally instead, the same way {@link #capHorizontalSpeed} already caps
 * a living entity's motion - see {@link #capBoatMotion}. {@code
 * deltaRotation} is a private field with no public accessor at all
 * (confirmed via {@code javap -p}), so capping it needs reflection - same
 * established pattern as {@code lumien.randomthings.util.PotionMetadataUtil}
 * elsewhere in this project (a lazily-cached, {@code setAccessible(true)}
 * {@link Field}). The underfoot-block check this all gates on is
 * position-based ({@link #isOnLubricentBlock}), not {@code onGround} -
 * ground-truthed as unreliable for a resting boat in the very first attempt
 * above. (Fixed the same day: {@code WorldTickEvent} never actually fires
 * for the client world in this Forge version at all - confirmed straight
 * from {@code BasicEventHooks#onPostWorldTick}'s own source, which hardcodes
 * {@code LogicalSide.SERVER} regardless of the {@code World} passed in - so
 * the cap above only ever touched the server's own boat copy, never what the
 * controlling player's client independently simulates and shows on screen.
 * {@code RandomThings}'s constructor now also registers a {@code
 * ClientTickEvent} listener doing the identical sweep against {@code
 * Minecraft.getInstance().world}, the same fix this project's
 * {@code SpectreIlluminatorRelight} already needed for its own client-side
 * draining - see that class's own comment for the fuller story.)
 * <p>
 * Real bug found and fixed, 2026-09-28, same day, per explicit user
 * feedback ("the boat still speeds up at the smallest motion... I would like
 * the boat to not speed up at all and instead cruise at whatever speed it's
 * at, up to the max speed"): the external cap above was masking a real
 * remaining problem, not just backstopping a rare edge case - {@code 1F /
 * 0.91F} is *not* true zero friction for a boat the way it is for a
 * {@code LivingEntity}. Ground-truthed why: {@code LivingEntity.travel}
 * multiplies motion by {@code slipperiness * 0.91F} - an extra hardcoded
 * 0.91 baked into *that* formula specifically - so slipperiness has to be
 * {@code 1F / 0.91F} to cancel it out to a net {@code 1.0F}. {@code
 * BoatEntity#updateMotion} has no such extra constant at all - it uses
 * {@code momentum = boatGlide} (straight from {@code getSlipperiness()})
 * *directly* as the multiplier. So a boat on these blocks was seeing a real
 * momentum of {@code ~1.0989}, not {@code 1.0} - genuine (if slower than
 * before) exponential growth from literally any nonzero residual velocity,
 * which is exactly "speeds up at the smallest motion." <b>Real fix</b>: the
 * per-entity {@code getSlipperiness} override is back (see each block's own
 * class javadoc), but this time returning exactly {@code 1.0F} for a {@code
 * BoatEntity} specifically - true, exact momentum retention (multiplying by
 * {@code 1.0F} is lossless in IEEE754, not an approximation), so speed
 * neither grows nor decays on its own. A boat still gains speed exactly the
 * way paddling normally does - {@code BoatEntity#controlBoat} adds a fixed
 * velocity nudge per tick of held input, additively, completely independent
 * of {@code momentum} - and {@link #capBoatMotion} stays in place
 * unmodified as the backstop for "holds forward long enough to add past the
 * cap," matching "cruise... up to the max speed" precisely: no self-driven
 * acceleration, but sustained real input can still climb to (and gets
 * capped at) the limit.
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

	/**
	 * Degrees/tick, the same "cap it, don't let it decay away the fun"
	 * spirit as {@link #MAX_HORIZONTAL_SPEED} applied to {@code BoatEntity
	 * #deltaRotation} instead of linear motion - no vanilla or community
	 * reference value exists for boat spin rate the way sprint speed has
	 * one, so this is a hand-picked, generously fast but clearly finite
	 * value (a full 360-degree spin in ~1.2 seconds at this rate). Tune
	 * freely, same as {@link #MAX_HORIZONTAL_SPEED}.
	 */
	static final float MAX_TURN_RATE = 15F;

	/**
	 * Exact momentum retention for a {@code BoatEntity} specifically - see
	 * this class's own javadoc for why {@code 1F / 0.91F} (true zero
	 * friction for a {@code LivingEntity}) is actually still slow exponential
	 * growth for a boat, which multiplies by slipperiness directly with no
	 * equivalent extra constant to cancel out. Multiplying by exactly
	 * {@code 1.0F} is lossless (no floating-point drift either way), so this
	 * is genuine "neither grows nor decays," not just "close enough."
	 */
	static final float BOAT_CRUISE_SLIPPERINESS = 1.0F;

	private static Field deltaRotationField;
	private static boolean deltaRotationLookupFailed;

	private SuperLubricentPhysics()
	{
	}

	/**
	 * Shared implementation for each Super Lubricent block's override of
	 * {@code Block#getSlipperiness(BlockState, IWorldReader, BlockPos,
	 * Entity)} - {@code defaultValue} is that block's own normal (living-
	 * entity) zero-friction value, passed through unchanged for anything
	 * that isn't a {@code BoatEntity}.
	 */
	public static float slipperinessFor(Entity entity, float defaultValue)
	{
		return entity instanceof BoatEntity ? BOAT_CRUISE_SLIPPERINESS : defaultValue;
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

	/**
	 * Caps both a boat's linear speed ({@link #capHorizontalSpeed}) and its
	 * turn rate ({@code deltaRotation}, reflected - see this class's own
	 * javadoc) - the two independent things {@code BoatEntity#updateMotion}
	 * multiplies by the underfoot block's slipperiness every tick.
	 */
	public static void capBoatMotion(BoatEntity boat)
	{
		capHorizontalSpeed(boat);

		Field field = getDeltaRotationField();

		if (field == null)
		{
			return;
		}

		try
		{
			float deltaRotation = field.getFloat(boat);
			float clamped = MathHelper.clamp(deltaRotation, -MAX_TURN_RATE, MAX_TURN_RATE);

			if (clamped != deltaRotation)
			{
				field.setFloat(boat, clamped);
			}
		}
		catch (IllegalAccessException ignored)
		{
		}
	}

	private static Field getDeltaRotationField()
	{
		if (deltaRotationField != null || deltaRotationLookupFailed)
		{
			return deltaRotationField;
		}

		try
		{
			Field field = BoatEntity.class.getDeclaredField("deltaRotation");
			field.setAccessible(true);
			deltaRotationField = field;
		}
		catch (NoSuchFieldException e)
		{
			deltaRotationLookupFailed = true;
		}

		return deltaRotationField;
	}

	/**
	 * Underfoot-block check backing both the living-entity {@code
	 * LivingUpdateEvent} listener (which also has to handle the Boots case,
	 * so it can't just call {@link #capHorizontalSpeed} unconditionally) and
	 * the boat {@code WorldTickEvent} listener, both in {@code RandomThings}'s
	 * constructor - see the class javadoc's second paragraph for why this
	 * looks one full block below the entity, not at its own position.
	 * Deliberately not gated on {@code Entity#onGround} - confirmed
	 * unreliable for a resting boat (see this class's own javadoc).
	 */
	public static boolean isOnLubricentBlock(Entity entity)
	{
		BlockPos underfoot = new BlockPos(entity.posX, entity.getBoundingBox().minY - 1.0D, entity.posZ);
		Block block = entity.world.getBlockState(underfoot).getBlock();

		return block instanceof SuperLubricentIceBlock || block instanceof SuperLubricentPlatformBlock || block instanceof SuperLubricentStoneBlock;
	}
}
