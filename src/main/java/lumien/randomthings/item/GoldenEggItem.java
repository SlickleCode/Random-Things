package lumien.randomthings.item;

import lumien.randomthings.entity.ThrownGoldenEggEntity;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;

/**
 * Throw it to hatch a {@link lumien.randomthings.entity.GoldenChickenEntity}
 * where it lands. 1.12.2's version was one damage-value subtype of the
 * catch-all {@code ItemIngredient}; this port gives it its own registered
 * item, matching the convention already used for Weather Eggs/Time in a
 * Bottle/etc. Direct port of {@code ItemIngredient}'s {@code GOLDEN_EGG}
 * special case in {@code onItemRightClick} and its dispenser behavior.
 */
public class GoldenEggItem extends Item {
    public GoldenEggItem(Item.Properties properties) {
        super(properties);

        DispenserBlock.registerDispenseBehavior(this, new ProjectileDispenseBehavior() {
            @Override
            protected IProjectile getProjectileEntity(World world, IPosition position, ItemStack stack) {
                return new ThrownGoldenEggEntity(world, position.getX(), position.getY(), position.getZ());
            }
        });
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);

        if (!playerIn.abilities.isCreativeMode) {
            itemstack.shrink(1);
        }

        worldIn.playSound(null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 1F));

        if (!worldIn.isRemote) {
            ThrownGoldenEggEntity entityegg = new ThrownGoldenEggEntity(worldIn, playerIn);
            entityegg.shoot(playerIn, playerIn.rotationPitch, playerIn.rotationYaw, 0.0F, 1.5F, 1.0F);
            worldIn.addEntity(entityegg);
        }

        playerIn.addStat(Stats.ITEM_USED.get(this));

        return new ActionResult<>(ActionResultType.SUCCESS, itemstack);
    }
}
