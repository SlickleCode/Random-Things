package lumien.randomthings.item;

import lumien.randomthings.entity.ThrownGoldenEggEntity;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.core.Position;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;

/**
 * Throw it to hatch a {@link lumien.randomthings.entity.GoldenChickenEntity}
 * where it lands. 1.12.2's version was one damage-value subtype of the
 * catch-all {@code ItemIngredient}; this port gives it its own registered
 * item, matching the convention already used for Weather Eggs/Time in a
 * Bottle/etc. Direct port of {@code ItemIngredient}'s {@code GOLDEN_EGG}
 * special case in {@code onItemRightClick} and its dispenser behavior.
 */
public class GoldenEggItem extends Item implements net.minecraft.world.item.ProjectileItem {
    public GoldenEggItem(Item.Properties properties) {
        super(properties);

        DispenserBlock.registerProjectileBehavior(this);
    }

    @Override
    public net.minecraft.world.entity.projectile.Projectile asProjectile(Level world, net.minecraft.core.Position position, ItemStack stack, net.minecraft.core.Direction direction) {
        return new ThrownGoldenEggEntity(world, position.x(), position.y(), position.z());
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);

        if (!playerIn.getAbilities().instabuild) {
            itemstack.shrink(1);
        }

        worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (worldIn.random.nextFloat() * 0.4F + 1F));

        if (!worldIn.isClientSide()) {
            ThrownGoldenEggEntity entityegg = new ThrownGoldenEggEntity(worldIn, playerIn);
            entityegg.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), 0.0F, 1.5F, 1.0F);
            worldIn.addFreshEntity(entityegg);
        }

        playerIn.awardStat(Stats.ITEM_USED.get(this));

        return InteractionResult.SUCCESS;
    }
}
