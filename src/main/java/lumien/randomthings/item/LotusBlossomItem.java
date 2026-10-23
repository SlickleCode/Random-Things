package lumien.randomthings.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

/**
 * Eat it (10-tick eat animation) for 3-12 experience, split across orbs the
 * same way vanilla's own XP-granting items do. Direct port of 1.12.2's
 * {@code ItemIngredient}'s {@code LOTUS_BLOSSOM} special case - given its own
 * dedicated class here rather than porting the old multi-sub-item
 * {@code ItemIngredient} system, matching how this port already handles
 * every other former metadata sub-item.
 */
public class LotusBlossomItem extends Item {
    public LotusBlossomItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand hand) {
        playerIn.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity user) {
        return 10;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.EAT;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity entityLiving) {
        if (entityLiving instanceof Player && !worldIn.isClientSide) {
            int remaining = 3 + worldIn.random.nextInt(5) + worldIn.random.nextInt(5);

            while (remaining > 0) {
                int split = ExperienceOrb.getExperienceValue(remaining);
                remaining -= split;
                worldIn.addFreshEntity(new ExperienceOrb(worldIn, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(), split));
            }
        }

        stack.shrink(1);

        return stack;
    }
}
