package lumien.randomthings.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

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
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
        playerIn.setActiveHand(hand);
        return new ActionResult<>(ActionResultType.SUCCESS, playerIn.getHeldItem(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 10;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        if (entityLiving instanceof PlayerEntity && !worldIn.isRemote) {
            int remaining = 3 + worldIn.rand.nextInt(5) + worldIn.rand.nextInt(5);

            while (remaining > 0) {
                int split = ExperienceOrbEntity.getXPSplit(remaining);
                remaining -= split;
                worldIn.addEntity(new ExperienceOrbEntity(worldIn, entityLiving.posX, entityLiving.posY, entityLiving.posZ, split));
            }
        }

        stack.shrink(1);

        return stack;
    }
}
