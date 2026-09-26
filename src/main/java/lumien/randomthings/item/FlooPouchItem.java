package lumien.randomthings.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

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
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new StringTextComponent(getFlooCount(stack) + " / " + MAX_CHARGE));
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0 - (double) getFlooCount(stack) / MAX_CHARGE;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return Color.GREEN.getRGB();
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!playerIn.isSneaking()) {
            return new ActionResult<>(ActionResultType.PASS, playerIn.getHeldItem(handIn));
        }

        ItemStack me = playerIn.getHeldItem(handIn);
        int flooCount = getFlooCount(me);

        if (flooCount < MAX_CHARGE) {
            for (int slot = 0; slot < playerIn.inventory.getSizeInventory() && flooCount < MAX_CHARGE; slot++) {
                ItemStack slotItem = playerIn.inventory.getStackInSlot(slot);

                if (!slotItem.isEmpty() && slotItem.getItem() == ModItems.FLOO_POWDER) {
                    int used = Math.min(slotItem.getCount(), MAX_CHARGE - flooCount);

                    flooCount += used;
                    slotItem.shrink(used);
                }
            }

            setFlooCount(me, flooCount);
        }

        return new ActionResult<>(ActionResultType.SUCCESS, me);
    }

    public static void setFlooCount(ItemStack stack, int count) {
        stack.getOrCreateTag().putInt("flooCount", count);
    }

    public static int getFlooCount(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getInt("flooCount") : 0;
    }
}
