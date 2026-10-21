package lumien.randomthings.item.block;

import lumien.randomthings.util.StackTag;
import net.minecraft.core.registries.Registries;

import lumien.randomthings.util.RegUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;

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

    public void addCreativeStacks(net.minecraft.world.item.CreativeModeTab.Output output) {
        ItemStack stack = new ItemStack(this);
        CompoundTag tag = new CompoundTag();
        tag.putString("block", RegUtil.id(Blocks.STONE).toString());
        tag.putBoolean("inverted", false);
        StackTag.set(stack, tag);

        output.accept(stack);
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
    public String getDescriptionId(ItemStack stack) {
        return super.getDescriptionId(stack) + (StackTag.getBoolean(stack, "inverted") ? "_inverted" : "");
    }

    @Override
    public Component getName(ItemStack stack) {
        Component name = super.getName(stack);

        Block displayBlock = Blocks.STONE;

        if (StackTag.contains(stack, "block")) {
            Block resolved = BuiltInRegistries.BLOCK.get(new ResourceLocation(StackTag.getString(stack, "block")));

            if (resolved != null) {
                displayBlock = resolved;
            }
        }

        return net.minecraft.network.chat.Component.literal("").append(name).append(" <").append(new ItemStack(displayBlock).getHoverName()).append(">");
    }
}
