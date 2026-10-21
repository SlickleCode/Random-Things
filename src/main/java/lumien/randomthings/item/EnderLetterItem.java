package lumien.randomthings.item;

import lumien.randomthings.util.StackTag;
import lumien.randomthings.container.EnderLetterContainer;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Right-click to open a 9-slot "write a letter" GUI (the slots live in the
 * letter's own NBT via {@link lumien.randomthings.util.ItemInventoryHandler},
 * matching this port's established item-carries-its-own-inventory
 * convention) plus a receiver name field. Once addressed, sneak-right-click
 * any {@link lumien.randomthings.block.EnderMailboxBlock} to actually send it
 * - see that class for the delivery logic. A delivered letter (received by
 * you) has its slots become output-only, and disappears once fully emptied.
 * Direct port of 1.12.2's {@code ItemEnderLetter}.
 */
public class EnderLetterItem extends Item {
    public EnderLetterItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return StackTag.getBoolean(stack, "received");
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void appendHoverText(ItemStack stack, net.minecraft.world.item.Item.TooltipContext worldCtx, List<Component> tooltip, TooltipFlag flagIn) {
        Level world = lumien.randomthings.client.ClientHooks.level();
        super.appendHoverText(stack, worldCtx, tooltip, flagIn);

        if (!StackTag.has(stack) || !StackTag.getBoolean(stack, "received")) {
            tooltip.add(Component.translatable("item.randomthings.ender_letter.how_to_send").withStyle(ChatFormatting.GRAY));
        }

        if (StackTag.has(stack)) {
            if (StackTag.contains(stack, "sender")) {
                tooltip.add(Component.translatable("item.randomthings.ender_letter.sender", StackTag.getString(stack, "sender")));
            }

            if (StackTag.contains(stack, "receiver")) {
                tooltip.add(Component.translatable("item.randomthings.ender_letter.receiver", StackTag.getString(stack, "receiver")));
            }
        }
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader world, BlockPos pos, Player player) {
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
        ItemStack stack = playerIn.getItemInHand(hand);

        if (!worldIn.isClientSide && hand == InteractionHand.MAIN_HAND) {
            ((ServerPlayer) playerIn).openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("item.randomthings.ender_letter");
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
                    return new EnderLetterContainer(windowId, playerInventory, stack);
                }
            });

            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }
}
