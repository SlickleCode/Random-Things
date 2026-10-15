package lumien.randomthings.item;

import lumien.randomthings.container.RedstoneRemoteEditContainer;
import lumien.randomthings.container.RedstoneRemoteUseContainer;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * Sneak-right-click to edit up to 9 bound Position Filter targets (stored in
 * this item's own NBT, not a tile entity); plain right-click opens a row of
 * buttons, one per bound target, to fire a strength-15 pulse at it (via
 * {@link lumien.randomthings.network.messages.RedstoneRemoteActivateMessage}).
 * Direct port of 1.12.2's {@code ItemRedstoneRemote}, including the edit screen's second row of
 * ghost camo-icon slots (stored under {@code camo}) that override each button's icon.
 */
public class RedstoneRemoteItem extends Item {
    public RedstoneRemoteItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
        ItemStack stack = playerIn.getItemInHand(hand);

        if (!worldIn.isClientSide) {
            if (playerIn.isShiftKeyDown()) {
                NetworkHooks.openScreen((ServerPlayer) playerIn, new MenuProvider() {
                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player p) {
                        return new RedstoneRemoteEditContainer(windowId, inv);
                    }

                    @Override
                    public Component getDisplayName() {
                        return Component.translatable("item.randomthings.redstone_remote");
                    }
                });
            } else {
                NetworkHooks.openScreen((ServerPlayer) playerIn, new MenuProvider() {
                    @Override
                    public AbstractContainerMenu createMenu(int windowId, Inventory inv, Player p) {
                        return new RedstoneRemoteUseContainer(windowId, inv);
                    }

                    @Override
                    public Component getDisplayName() {
                        return Component.translatable("item.randomthings.redstone_remote");
                    }
                });
            }
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }
}
