package lumien.randomthings.item;

import lumien.randomthings.util.StackTag;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.awt.*;
import java.util.List;

/**
 * Sneak-right-click to top itself up to 128 charge from any Floo Powder in
 * the holder's inventory (used elsewhere by {@link FlooSignItem}/{@link
 * FlooTokenItem} - this pouch is purely a portable stockpile). Its charge
 * shows as a vanilla durability bar. Direct port of 1.12.2's {@code ItemFlooPouch}.
 */
public class FlooPouchItem extends Item {
    private static final int MAX_CHARGE = 128;

    public FlooPouchItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldInCtx, net.minecraft.world.item.component.TooltipDisplay rtDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag flagIn) {
        Level worldIn = lumien.randomthings.client.ClientHooks.level();
        tooltip.accept(Component.literal(getFlooCount(stack) + " / " + MAX_CHARGE));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * (1.0F - (float) (1.0 - (double) getFlooCount(stack) / MAX_CHARGE)));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Color.GREEN.getRGB();
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (!playerIn.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }

        ItemStack me = playerIn.getItemInHand(handIn);
        int flooCount = getFlooCount(me);

        if (flooCount < MAX_CHARGE) {
            for (int slot = 0; slot < playerIn.getInventory().getContainerSize() && flooCount < MAX_CHARGE; slot++) {
                ItemStack slotItem = playerIn.getInventory().getItem(slot);

                if (!slotItem.isEmpty() && slotItem.getItem() == ModItems.FLOO_POWDER) {
                    int used = Math.min(slotItem.getCount(), MAX_CHARGE - flooCount);

                    flooCount += used;
                    slotItem.shrink(used);
                }
            }

            setFlooCount(me, flooCount);
        }

        return InteractionResult.SUCCESS;
    }

    public static void setFlooCount(ItemStack stack, int count) {
        StackTag.edit(stack, t -> t.putInt("flooCount", count));
    }

    public static int getFlooCount(ItemStack stack) {
        return StackTag.has(stack) ? StackTag.getInt(stack, "flooCount") : 0;
    }
}
