package lumien.randomthings.item;

import lumien.randomthings.handler.redstonesignal.RedstoneSignalHandler;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.List;

/**
 * Right-click a block to broadcast a strength-15 "phantom" redstone signal
 * there for a fixed duration (see {@link RedstoneSignalHandler}); right-
 * click empty air to cycle the duration (2/20/100 ticks - shift reverses),
 * shown both in the tooltip and as a model-variant texture swap (same
 * {@code addPropertyOverride} pattern as {@link SoundRecorderItem}). Direct
 * port of 1.12.2's {@code ItemRedstoneActivator}.
 */
public class RedstoneActivatorItem extends Item {
    private static final int[] DURATIONS = new int[] { 2, 20, 100 };

    public RedstoneActivatorItem(Item.Properties properties) {
        super(properties);

        this.addPropertyOverride(new ResourceLocation("duration_index"), new IItemPropertyGetter() {
            @Override
            public float call(ItemStack stack, World worldIn, LivingEntity entityIn) {
                return getDurationIndex(stack);
            }
        });
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TranslationTextComponent("tooltip.randomthings.redstone_activator.duration", DURATIONS[getDurationIndex(stack)]));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
        ItemStack stack = playerIn.getHeldItem(hand);

        int current = getDurationIndex(stack);
        int next;

        if (playerIn.isSneaking()) {
            next = current - 1;
            next = next < 0 ? DURATIONS.length - 1 : next;
        } else {
            next = current + 1;
            next = next >= DURATIONS.length ? 0 : next;
        }

        setDurationIndex(stack, next);

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();

        if (!world.isRemote) {
            ItemStack stack = context.getItem();
            RedstoneSignalHandler.get(world).addSignal(world, context.getPos(), DURATIONS[getDurationIndex(stack)], 15);
        }

        return ActionResultType.SUCCESS;
    }

    public static int getDurationIndex(ItemStack stack) {
        CompoundNBT compound = stack.getTag();
        return compound != null && compound.contains("durationIndex") ? compound.getInt("durationIndex") : 1;
    }

    public static void setDurationIndex(ItemStack stack, int index) {
        stack.getOrCreateTag().putInt("durationIndex", index);
    }
}
