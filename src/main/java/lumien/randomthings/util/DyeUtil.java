package lumien.randomthings.util;

import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;

/**
 * Direct port of 1.12.2's {@code DyeUtil}, simplified: 1.12.2 needed the ore
 * dictionary to identify a dye ItemStack (its 16 shades were all one
 * {@code ItemDye} with 16 metadata values). 1.14.4 gives every dye its own
 * top-level item and a real {@link DyeColor#getColor(ItemStack)} helper, so
 * no ore-dictionary lookup is needed any more.
 */
public class DyeUtil {
    public static int getDyeColor(ItemStack dyeStack) {
        DyeColor color = DyeColor.getColor(dyeStack);
        return color != null ? color.getColorValue() : 0;
    }

    public static boolean isVanillaDye(ItemStack dyeStack) {
        return DyeColor.getColor(dyeStack) != null;
    }
}
