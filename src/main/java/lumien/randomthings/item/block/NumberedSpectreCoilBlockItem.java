package lumien.randomthings.item.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Port of the {@code ItemBlockSpectreCoil} name behaviour for the loot-only Numbered Spectre Coil: the display
 * name is "Spectre Coil Nr. %d" with a number from 1 to 499 derived from the stack's identity hash, like 1.12.2.
 */
public class NumberedSpectreCoilBlockItem extends BlockItem {
    public NumberedSpectreCoilBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        Random rnd = new Random(System.identityHashCode(stack));

        return new TranslationTextComponent(this.getTranslationKey(stack), rnd.nextInt(499) + 1);
    }
}
