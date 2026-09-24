package lumien.randomthings.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

import lumien.randomthings.block.BlazingFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.FireBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

/**
 * Restores the two 1.12.2 BlazingFireBlock catch-chance/growth tweaks that
 * live inside FireBlock.tryCatchFire's *private* internals in this Forge
 * version and therefore aren't reachable by a normal override (see
 * BlazingFireBlock's class comment for the ground-truth bytecode this was
 * derived from). Every injection point is guarded so it only changes
 * behavior when `this` is actually a BlazingFireBlock - vanilla fire and any
 * other FireBlock subclass are left completely untouched.
 */
@Mixin(value = FireBlock.class, remap = false)
public abstract class FireBlockMixin
{
	// random.nextInt(chance) < flammability -> < flammability * 4
	@Redirect(method = "tryCatchFire", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getFlammability(Lnet/minecraft/world/IBlockReader;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/Direction;)I"))
	private int randomthings_boostFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face)
	{
		int flammability = state.getFlammability(world, pos, face);
		return (Object) this instanceof BlazingFireBlock ? flammability * 4 : flammability;
	}

	// random.nextInt(age + 10) -> random.nextInt(age / 2 + 1): reconstruct
	// age from the already-computed "age + 10" argument value.
	@ModifyArg(method = "tryCatchFire", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 1))
	private int randomthings_lowerDieOutBound(int agePlusTen)
	{
		if (!((Object) this instanceof BlazingFireBlock))
		{
			return agePlusTen;
		}

		int age = agePlusTen - 10;
		return age / 2 + 1;
	}

	// random.nextInt(5) / 4 -> random.nextInt(2): nextInt(8) / 4 has the
	// exact same 50/50 {0, 1} distribution as nextInt(2), so widening the
	// bound to 8 reproduces it without touching the division that follows.
	@ModifyArg(method = "tryCatchFire", at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I", ordinal = 2))
	private int randomthings_fasterAgeGrowth(int original)
	{
		return (Object) this instanceof BlazingFireBlock ? 8 : original;
	}
}
