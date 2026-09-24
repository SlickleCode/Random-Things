package lumien.randomthings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import lumien.randomthings.item.SuperLubricentBootsItem;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.BlockPos;
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
@Mixin(value = LivingEntity.class, remap = false)
public abstract class SuperLubricentBootsMixin
{
	@Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getSlipperiness(Lnet/minecraft/world/IWorldReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)F"))
	private float randomthings_bootsMaxSlip(BlockState state, IWorldReader world, BlockPos pos, Entity entity)
	{
		float original = state.getSlipperiness(world, pos, entity);

		if (entity.isSneaking() || !(entity instanceof LivingEntity))
		{
			return original;
		}

		if (((LivingEntity) entity).getItemStackFromSlot(EquipmentSlotType.FEET).getItem() instanceof SuperLubricentBootsItem)
		{
			return 1F / 0.91F;
		}

		return original;
	}
}
