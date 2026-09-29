package lumien.randomthings.item.block;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Direct port of 1.12.2's {@code ItemBlockBlockDiaphanous}: the creative-tab
 * entry defaults to a plain Stone-displaying, non-inverted stack (real
 * per-block variants come from {@link lumien.randomthings.recipes.DiaphanousSetRecipe}
 * instead, same as 1.12.2), and the display name gets a {@code <Block Name>}
 * suffix so a stack in an inventory shows what it's actually set to. NBT
 * transfer onto the placed tile entity happens in {@link
 * lumien.randomthings.block.DiaphanousBlock#onBlockPlacedBy} (which also
 * gets the {@code ItemStack}), matching where 1.12.2 did it.
 */
public class DiaphanousBlockItem extends BlockItem {
    public DiaphanousBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (!this.isInGroup(group)) {
            return;
        }

        ItemStack stack = new ItemStack(this);
        CompoundNBT tag = new CompoundNBT();
        tag.putString("block", Blocks.STONE.getRegistryName().toString());
        tag.putBoolean("inverted", false);
        stack.setTag(tag);

        items.add(stack);
    }

    /**
     * Real bug, found 2026-09-28 (reported by user): the inverted variant's
     * name showed as a raw untranslated key. Root cause: {@code
     * BlockItem#getTranslationKey()} (which {@code super.getTranslationKey
     * (stack)} bottoms out at, confirmed via source) delegates to the
     * *block's* own translation key ({@code block.randomthings.diaphanous_block}),
     * not an {@code item.}-prefixed one the way a plain {@code Item} would -
     * the lang entry this was appending "_inverted" onto had the wrong
     * prefix.
     */
    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey(stack) + (stack.hasTag() && stack.getTag().getBoolean("inverted") ? "_inverted" : "");
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        ITextComponent name = super.getDisplayName(stack);

        Block displayBlock = Blocks.STONE;

        if (stack.hasTag() && stack.getTag().contains("block")) {
            Block resolved = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(stack.getTag().getString("block")));

            if (resolved != null) {
                displayBlock = resolved;
            }
        }

        return name.appendText(" <").appendSibling(new ItemStack(displayBlock).getDisplayName()).appendText(">");
    }
}
