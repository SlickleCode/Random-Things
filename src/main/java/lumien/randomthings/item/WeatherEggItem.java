package lumien.randomthings.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;

import lumien.randomthings.entity.ThrownWeatherEggEntity;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.level.Level;

/**
 * Throw it to summon a weather-changing cloud where it lands (see {@link
 * ThrownWeatherEggEntity}/{@code WeatherCloudEntity}). One instance per
 * weather type (Sun/Rain/Storm), matching this port's convention of a
 * separate registered item per former 1.12.2 metadata subtype, rather than
 * one item with a {@code TYPE} enum stored as damage value. Direct port of
 * 1.12.2's {@code ItemWeatherEgg}.
 */
public class WeatherEggItem extends Item {
    public WeatherEggItem(Item.Properties properties) {
        super(properties);

        DispenserBlock.registerBehavior(this, new AbstractProjectileDispenseBehavior() {
            @Override
            protected Projectile getProjectile(Level world, Position position, ItemStack stack) {
                return new ThrownWeatherEggEntity(world, position.x(), position.y(), position.z(), WeatherEggItem.this);
            }
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);

        if (!playerIn.getAbilities().instabuild) {
            itemstack.shrink(1);
        }

        worldIn.playSound(null, playerIn.getX(), playerIn.getY(), playerIn.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (worldIn.random.nextFloat() * 0.4F + 0.8F));

        if (!worldIn.isClientSide) {
            ThrownWeatherEggEntity entityegg = new ThrownWeatherEggEntity(worldIn, playerIn, this);
            entityegg.shootFromRotation(playerIn, playerIn.getXRot(), playerIn.getYRot(), 0.0F, 1.5F, 1.0F);
            worldIn.addFreshEntity(entityegg);
        }

        playerIn.awardStat(Stats.ITEM_USED.get(this));

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemstack);
    }
}
