package lumien.randomthings.item;

import lumien.randomthings.container.PortableSoundDampenerContainer;
import lumien.randomthings.util.ItemInventoryHandler;
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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

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
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
        ItemStack stack = playerIn.getHeldItem(hand);

        if (!worldIn.isRemote && hand == Hand.MAIN_HAND) {
            NetworkHooks.openGui((ServerPlayerEntity) playerIn, new INamedContainerProvider() {
                @Override
                public ITextComponent getDisplayName() {
                    return new TranslationTextComponent("item.randomthings.portable_sound_dampener");
                }

                @Override
                public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
                    return new PortableSoundDampenerContainer(windowId, playerInventory, stack);
                }
            });

            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }

        return new ActionResult<>(ActionResultType.FAIL, stack);
    }

    public static ItemInventoryHandler getInventory(ItemStack dampener) {
        return new ItemInventoryHandler(dampener, "inventory", 9);
    }
}
