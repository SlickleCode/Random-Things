package lumien.randomthings.item;

import lumien.randomthings.container.PortableSoundDampenerContainer;
import lumien.randomthings.util.ItemInventoryHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

/**
 * A carried 9-slot filter of {@link SoundPatternItem}s, muting every sound
 * stamped into one for its holder. Direct port of 1.12.2's
 * {@code ItemPortableSoundDampener} - originally a Baubles body-slot item;
 * since third-party compat (including Baubles) is dropped for this port,
 * this works purely by being carried anywhere in the player's inventory
 * (checked directly in {@code RandomThings}'s {@code PlaySoundEvent}
 * listener), the same fallback this port already used for Obsidian
 * Skull/Lava Charm.
 */
public class PortableSoundDampenerItem extends Item {
    public PortableSoundDampenerItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand hand) {
        ItemStack stack = playerIn.getItemInHand(hand);

        if (!worldIn.isClientSide() && hand == InteractionHand.MAIN_HAND) {
            ((ServerPlayer) playerIn).openMenu(new MenuProvider() {
                @Override
                public Component getDisplayName() {
                    return Component.translatable("item.randomthings.portable_sound_dampener");
                }

                @Override
                public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
                    return new PortableSoundDampenerContainer(windowId, playerInventory, stack);
                }
            });

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    public static ItemInventoryHandler getInventory(ItemStack dampener) {
        return new ItemInventoryHandler(dampener, "inventory", 9);
    }
}
