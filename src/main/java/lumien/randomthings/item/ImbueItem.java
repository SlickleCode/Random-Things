package lumien.randomthings.item;

import lumien.randomthings.potion.ModEffects;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.UseAction;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.function.Supplier;

/**
 * A drinkable bottle that grants its {@link Effect} for 20 minutes (24000
 * ticks), replacing whichever of the four imbue effects the player already
 * had active (only one can be active at a time - drinking a second kind
 * swaps it rather than stacking). One instance per imbue kind, matching this
 * port's established "shared class parameterized per variant" convention
 * (see {@link BeanItem}/{@link lumien.randomthings.item.RuneDustItem}).
 * Direct port of 1.12.2's {@code ItemImbue} (its Collapse subtype excluded -
 * abandoned content in the original, confirmed via its own {@code // TODO: Remove},
 * skipped per explicit user decision this slice).
 * <p>
 * Takes a {@code Supplier} rather than the {@link Effect} itself: items and
 * effects are two separate Forge registries, and their {@code @ObjectHolder}
 * fields are only guaranteed populated once each registry's own event has
 * fired - deferring the lookup avoids depending on which of the two happens
 * to register first.
 */
public class ImbueItem extends Item {
    private static final int DURATION = 60 * 20 * 20;

    private final Supplier<Effect> effect;

    public ImbueItem(Item.Properties properties, Supplier<Effect> effect) {
        super(properties);

        this.effect = effect;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
        ItemStack itemStackIn = playerIn.getHeldItem(hand);
        playerIn.setActiveHand(hand);
        return new ActionResult<>(ActionResultType.SUCCESS, itemStackIn);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity livingEntity) {
        if (livingEntity instanceof PlayerEntity) {
            PlayerEntity playerIn = (PlayerEntity) livingEntity;

            if (!playerIn.abilities.isCreativeMode) {
                stack.shrink(1);
            }

            if (!worldIn.isRemote) {
                clearImbues(playerIn);
                playerIn.addPotionEffect(new EffectInstance(effect.get(), DURATION, 0, false, false));
            }

            if (!playerIn.abilities.isCreativeMode) {
                if (stack.getCount() <= 0) {
                    return new ItemStack(Items.GLASS_BOTTLE);
                } else {
                    playerIn.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE));
                }
            }
        }

        return stack;
    }

    private void clearImbues(PlayerEntity player) {
        for (Effect imbue : new Effect[]{ModEffects.IMBUE_FIRE, ModEffects.IMBUE_POISON, ModEffects.IMBUE_EXPERIENCE, ModEffects.IMBUE_WITHER}) {
            if (player.isPotionActive(imbue)) {
                player.removePotionEffect(imbue);
            }
        }
    }
}
