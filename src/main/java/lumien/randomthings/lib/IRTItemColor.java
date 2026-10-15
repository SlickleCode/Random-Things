package lumien.randomthings.lib;

import net.minecraft.world.item.ItemStack;

/**
 * Implemented by items that need a runtime tint on their inventory icon.
 * Wired up generically by a single {@code RegisterColorHandlersEvent.Item} listener in
 * {@code RandomThings}.
 */
public interface IRTItemColor {
    int getColorFromItemstack(ItemStack stack, int tintIndex);
}
