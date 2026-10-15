package lumien.randomthings.util;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

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
        return color != null ? rgb(color) : 0;
    }

    /**
     * The dye's plain 0xRRGGBB colour (what 1.16's {@code DyeColor#getColorValue} returned, e.g. 3949738 for blue). 1.17+ no longer
     * stores it: {@code getTextColor} is the sign-text colour and {@code getFireworkColor} the firework one, which are different
     * values, so it is rebuilt from the texture diffuse colours (which are exactly r/255, g/255, b/255 of it).
     */
    public static int rgb(DyeColor color) {
        float[] c = color.getTextureDiffuseColors();
        return Math.round(c[0] * 255F) << 16 | Math.round(c[1] * 255F) << 8 | Math.round(c[2] * 255F);
    }

    public static boolean isVanillaDye(ItemStack dyeStack) {
        return DyeColor.getColor(dyeStack) != null;
    }
}
