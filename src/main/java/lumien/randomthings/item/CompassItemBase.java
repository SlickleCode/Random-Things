package lumien.randomthings.item;

import lumien.randomthings.util.StackTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;


/**
 * Shared needle-angle logic for {@link GoldenCompassItem} and
 * {@link EmeraldCompassItem} - a direct port of vanilla's own compass
 * "wobble towards a target" math (from {@code ItemGoldenCompass}/
 * {@code ItemEmeraldCompass} in 1.12.2, themselves near-identical copies of
 * vanilla's {@code ItemCompass}), reading a custom {@code targetX}/
 * {@code targetZ} NBT pair instead of vanilla's lodestone/spawn position.
 * Registered under the same {@code minecraft:angle} property key vanilla's
 * own compass uses, so the item model JSON's {@code overrides} predicates
 * work exactly like vanilla's 32-frame compass model.
 */
public abstract class CompassItemBase extends Item {
	public CompassItemBase(Item.Properties properties)
	{
		super(properties);

	}
}
