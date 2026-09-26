package lumien.randomthings.mixin;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import lumien.randomthings.item.SuperLubricentBootsItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldReader;

/**
 * Restores the real 1.12.2 Super Lubricent Boots behavior, ground-truthed
 * from {@code AsmHandler.slipFix} in the original source: while worn and not
 * sneaking, EVERY block is treated as maximally slippery, not just the three
 * Super Lubricent blocks - sneaking is the only way to walk normally. A
 * previous port attempt implemented this the other way around (a per-block
 * {@code getSlipperiness} override on the three Super Lubricent blocks that
 * CANCELLED their own slide while the boots were worn) - backwards from the
 * original, and incapable of producing the "every surface" part regardless,
 * since a per-block override can only ever affect those three blocks.
 * <p>
 * Reproducing "every surface" requires intercepting the single friction
 * lookup inside {@code LivingEntity.travel} itself - there's no Forge event
 * for block slipperiness in this version to hook instead, so a Mixin is the
 * only option (matching this project's "Mixin only when necessary"
 * principle). Confirmed via {@code javap -c} that {@code travel} calls
 * {@code BlockState.getSlipperiness(IWorldReader, BlockPos, Entity)} exactly
 * once, reading the block directly underfoot; redirecting that single call
 * site is sufficient. The zero-friction constant here matches the one baked
 * into the three Super Lubricent blocks' own {@code Properties.slipperiness}
 * (see {@code SuperLubricentPlatformBlock}'s class javadoc for the
 * {@code 1F / 0.91F} derivation), so standing on one of them with the boots
 * on is simply a no-op redirect (same value either way).
 */
@Mixin(LivingEntity.class)
public abstract class SuperLubricentBootsMixin
{
	// TEMPORARY diagnostic logging for TESTING_CHECKLIST.md #134 - remove once the boots are
	// confirmed working in-game. Throttled to ~once/sec/player so it stays readable instead of
	// spamming once per tick (travel() runs every tick regardless of movement).
	private static final Logger RT_BOOTS_DEBUG_LOG = LogManager.getLogger("RandomThings-SuperLubricentBootsMixin");
	private static int randomthings_debugCounter = 0;
	private static int randomthings_debugHeadCounter = 0;

	// TEMPORARY: unconditional entry-point probe, independent of the @Redirect below - proves
	// whether this Mixin is woven into LivingEntity's travel() at all, regardless of whether the
	// @Redirect's own @At(INVOKE) target ever matches.
	@Inject(method = "travel", at = @At("HEAD"))
	private void randomthings_bootsTravelHeadProbe(Vec3d motion, CallbackInfo ci)
	{
		if ((Object) this instanceof PlayerEntity && (randomthings_debugHeadCounter++ % 20 == 0))
		{
			RT_BOOTS_DEBUG_LOG.info("travel() HEAD probe fired for {}", ((PlayerEntity) (Object) this).getScoreboardName());
		}
	}

	//@Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getSlipperiness(Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)F"))
	private float randomthings_bootsMaxSlip(BlockState state, IWorldReader world, BlockPos pos, Entity entity)
	{
		float original = state.getSlipperiness(world, pos, entity);

		boolean logThisCall = entity instanceof PlayerEntity && (randomthings_debugCounter++ % 20 == 0);

		if (entity.isSneaking() || !(entity instanceof LivingEntity))
		{
			if (logThisCall)
			{
				RT_BOOTS_DEBUG_LOG.info("redirect fired for {}: sneaking={}, isLivingEntity={} -> returning original {}", entity.getScoreboardName(), entity.isSneaking(), entity instanceof LivingEntity, original);
			}
			return original;
		}

		ItemStack boots = ((LivingEntity) entity).getItemStackFromSlot(EquipmentSlotType.FEET);
		boolean wearingBoots = boots.getItem() instanceof SuperLubricentBootsItem;

		if (logThisCall)
		{
			RT_BOOTS_DEBUG_LOG.info("redirect fired for {}: sneaking=false, feetSlot={}, wearingBoots={}, original={}", entity.getScoreboardName(), boots, wearingBoots, original);
		}

		if (wearingBoots)
		{
			float boosted = 1F / 0.91F;
			if (logThisCall)
			{
				RT_BOOTS_DEBUG_LOG.info("boosting slipperiness for {}: {} -> {}", entity.getScoreboardName(), original, boosted);
			}
			return boosted;
		}

		return original;
	}
}
