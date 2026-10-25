package lumien.randomthings.item;

import lumien.randomthings.util.StackTag;
import lumien.randomthings.handler.redstonesignal.RedstoneSignalHandler;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.List;
import net.neoforged.api.distmarker.Dist;


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

    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldCtx, net.minecraft.world.item.component.TooltipDisplay rtDisplay, java.util.function.Consumer<Component> tooltip, TooltipFlag flag) {
        Level world = lumien.randomthings.client.ClientHooks.level();
        tooltip.accept(Component.translatable("tooltip.randomthings.redstone_activator.duration", DURATIONS[getDurationIndex(stack)]));
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand hand) {
        ItemStack stack = playerIn.getItemInHand(hand);

        int current = getDurationIndex(stack);
        int next;

        if (playerIn.isShiftKeyDown()) {
            next = current - 1;
            next = next < 0 ? DURATIONS.length - 1 : next;
        } else {
            next = current + 1;
            next = next >= DURATIONS.length ? 0 : next;
        }

        setDurationIndex(stack, next);

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();

        if (!world.isClientSide()) {
            ItemStack stack = context.getItemInHand();
            RedstoneSignalHandler.get(world).addSignal(world, context.getClickedPos(), DURATIONS[getDurationIndex(stack)], 15);
        }

        return InteractionResult.SUCCESS;
    }

    public static int getDurationIndex(ItemStack stack) {
        CompoundTag compound = StackTag.get(stack);
        return compound != null && compound.contains("durationIndex") ? compound.getIntOr("durationIndex", 0) : 1;
    }

    public static void setDurationIndex(ItemStack stack, int index) {
        StackTag.edit(stack, t -> t.putInt("durationIndex", index));
    }
}
