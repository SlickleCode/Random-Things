package lumien.randomthings.item;

import lumien.randomthings.potion.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;

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

    private final Supplier<net.minecraft.core.Holder<MobEffect>> effect;

    public ImbueItem(Item.Properties properties, Supplier<net.minecraft.core.Holder<MobEffect>> effect) {
        super(properties);

        this.effect = effect;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
        ItemStack itemStackIn = playerIn.getItemInHand(hand);
        playerIn.startUsingItem(hand);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, itemStackIn);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level worldIn, LivingEntity livingEntity) {
        if (livingEntity instanceof Player) {
            Player playerIn = (Player) livingEntity;

            if (!playerIn.getAbilities().instabuild) {
                stack.shrink(1);
            }

            if (!worldIn.isClientSide) {
                clearImbues(playerIn);
                // particles off like 1.12.2, but the HUD/inventory icon on: the 5-argument constructor ties showIcon to showParticles and hid it
                playerIn.addEffect(new MobEffectInstance(effect.get(), DURATION, 0, false, false, true));
            }

            if (!playerIn.getAbilities().instabuild) {
                if (stack.getCount() <= 0) {
                    return new ItemStack(Items.GLASS_BOTTLE);
                } else {
                    playerIn.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
                }
            }
        }

        return stack;
    }

    private void clearImbues(Player player) {
        for (net.minecraft.core.Holder<MobEffect> imbue : new net.minecraft.core.Holder[]{ModEffects.IMBUE_FIRE, ModEffects.IMBUE_POISON, ModEffects.IMBUE_EXPERIENCE, ModEffects.IMBUE_WITHER}) {
            if (player.hasEffect(imbue)) {
                player.removeEffect(imbue);
            }
        }
    }
}
