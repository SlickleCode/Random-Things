package lumien.randomthings.item.block;

import java.util.Random;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * Port of the {@code ItemBlockSpectreCoil} name behaviour for the loot-only Numbered Spectre Coil: the display
 * name is "Spectre Coil Nr. %d" with a number from 1 to 499 derived from the stack's identity hash, like 1.12.2.
 */
public class NumberedSpectreCoilBlockItem extends BlockItem {
    public NumberedSpectreCoilBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Random rnd = new Random(System.identityHashCode(stack));

        return Component.translatable(this.getDescriptionId(stack), rnd.nextInt(499) + 1);
    }
}
