package lumien.randomthings.item;

import lumien.randomthings.container.EnderLetterContainer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

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
    public boolean hasEffect(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean("received");
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, world, tooltip, flagIn);

        if (stack.hasTag()) {
            if (stack.getTag().contains("sender")) {
                tooltip.add(new TranslationTextComponent("item.randomthings.ender_letter.sender", stack.getTag().getString("sender")));
            }

            if (stack.getTag().contains("receiver")) {
                tooltip.add(new TranslationTextComponent("item.randomthings.ender_letter.receiver", stack.getTag().getString("receiver")));
            }
        }
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, IWorldReader world, BlockPos pos, PlayerEntity player) {
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
        ItemStack stack = playerIn.getHeldItem(hand);

        if (!worldIn.isRemote && hand == Hand.MAIN_HAND) {
            NetworkHooks.openGui((ServerPlayerEntity) playerIn, new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    return new TranslationTextComponent("item.randomthings.ender_letter");
                }

                @Override
                public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
                    return new EnderLetterContainer(windowId, playerInventory, stack);
                }
            });

            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        return new ActionResult<>(ActionResultType.FAIL, stack);
    }
}
