package lumien.randomthings.item;

import lumien.randomthings.entity.ThrownWeatherEggEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.World;

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

        DispenserBlock.registerDispenseBehavior(this, new ProjectileDispenseBehavior() {
            @Override
            protected IProjectile getProjectileEntity(World world, IPosition position, ItemStack stack) {
                return new ThrownWeatherEggEntity(world, position.getX(), position.getY(), position.getZ(), WeatherEggItem.this);
            }
        });
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        if (!playerIn.abilities.isCreativeMode) {
            itemstack.shrink(1);
        }

        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));

        if (!worldIn.isRemote) {
            ThrownWeatherEggEntity entityegg = new ThrownWeatherEggEntity(worldIn, playerIn, this);
            entityegg.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 1.0F);
            worldIn.addEntity(entityegg);
        }

        playerIn.addStat(Stats.ITEM_USED.get(this));

        return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
    }
}
