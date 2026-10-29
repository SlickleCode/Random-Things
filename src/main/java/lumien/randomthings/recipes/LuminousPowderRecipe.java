package lumien.randomthings.recipes;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.util.StackTag;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.ByteTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Luminous Powder + any enchanted item: the result is a copy of the item
 * with the {@code luminousEnchantment} NBT flag (the glint is meant to glow
 * in the dark). Direct port of 1.12.2's {@code luminousPowder} recipe: exactly
 * one powder and one enchanted, not yet luminous item; the powder is used up.
 * See {@link #isLuminous} for the flag, which the tooltip reads.
 */
public class LuminousPowderRecipe extends CustomRecipe {
    public static final RecipeSerializer<LuminousPowderRecipe> SERIALIZER = new CustomRecipe.Serializer<>(LuminousPowderRecipe::new);

    public static final String FLAG = "luminousEnchantment";

    public LuminousPowderRecipe(net.minecraft.world.item.crafting.CraftingBookCategory category) {
        super(category);
    }

    public static boolean isLuminous(ItemStack stack) {
        return StackTag.contains(stack, FLAG);
    }

    /** True if the stack carries at least one enchantment (level above 0 for any registered enchantment). */
    private static boolean isEnchantedStack(ItemStack stack, Level level) {
        net.minecraft.core.HolderLookup.Provider access = level.registryAccess();

        for (net.minecraft.resources.Identifier id : access.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).keySet()) {
            if (net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(access.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENCHANTMENT, id)), stack) > 0) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean matches(CraftingInput inv, Level worldIn) {
        ItemStack powder = ItemStack.EMPTY;
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getItem() == ModItems.LUMINOUS_POWDER) {
                if (!powder.isEmpty()) {
                    return false;
                }

                powder = stack;
            } else {
                if (!target.isEmpty() || !isEnchantedStack(stack, worldIn)) {
                    return false;
                }

                target = stack;
            }
        }

        return !powder.isEmpty() && !target.isEmpty() && !isLuminous(target);
    }

    @Override
    public ItemStack assemble(CraftingInput inv, net.minecraft.core.HolderLookup.Provider registryAccess) {
        ItemStack target = ItemStack.EMPTY;

        for (int i = 0; i < inv.size(); i++) {
            ItemStack stack = inv.getItem(i);

            if (!stack.isEmpty() && stack.getItem() != ModItems.LUMINOUS_POWDER) {
                target = stack;
            }
        }

        ItemStack result = target.copy();
        result.setCount(1);
        StackTag.edit(result, t -> t.put(FLAG, ByteTag.valueOf((byte) 0)));

        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inv) {
        return NonNullList.withSize(inv.size(), ItemStack.EMPTY);
    }


    @Override
    public RecipeSerializer<? extends CustomRecipe> getSerializer() {
        return SERIALIZER;
    }
}
