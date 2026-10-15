package lumien.randomthings.asm;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * The colour of an item's enchant glint, as 1.12.2's {@code AsmHandler.enchantmentColorHook} decided it. Plain logic with no client types, so it is
 * unit-tested on both sides; {@code ClientAsmHandler} feeds it the stack the renderer is drawing.
 * Priority: fixed tints of Redstone Tool / Spectre Key / Portkey, then the stack's {@code enchantmentColor} tag (Dyeing Machine), then the Spectre
 * Sword and Escape Rope tints, otherwise vanilla's colour.
 * The 1.12.2 Precious Emerald's animated rainbow glint is deliberately not ported (the item itself is out of scope, see {@code docs/project/scope-decisions.md}).
 */
public final class GlintColors {
    public static final int VANILLA = -8372020;

    private GlintColors() {
    }

    public static int colorFor(ItemStack stack, int original) {
        if (stack == null || stack.isEmpty()) {
            return original;
        }

        Item item = stack.getItem();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(item);
        String path = id != null && "randomthings".equals(id.getNamespace()) ? id.getPath() : "";

        if (path.equals("redstone_tool")) {
            return 0xFFB20000;
        }

        if (path.equals("spectre_key")) {
            return 0xFF00B2B2;
        }

        if (path.equals("portkey")) {
            return 0xFFB200B2;
        }

        if (stack.hasTag() && stack.getTag().contains("enchantmentColor")) {
            return stack.getTag().getInt("enchantmentColor") | 0xFF000000;
        }

        if (path.equals("spectre_sword")) {
            return 0xFF7D7D7D;
        }

        if (path.equals("escape_rope")) {
            return 0xFFB2B200;
        }

        return original;
    }
}
