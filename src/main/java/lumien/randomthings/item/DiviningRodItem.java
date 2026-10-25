package lumien.randomthings.item;

import net.minecraft.core.registries.Registries;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;

import java.awt.*;

/**
 * DiviningRodItem
 */
public class DiviningRodItem extends Item {
    String[] detectingTags;
    Color[] colors;

    public DiviningRodItem(Properties properties, Color[] colors, String[] detectingTags) {
        super(properties.stacksTo(1));

        this.detectingTags = detectingTags;
        this.colors = colors;
    }

    public Color getColor(int index) {
        return colors[index];
    }

    public String[] getDetectedTags() {
        return detectingTags;
    }

    public int matches(BlockState blockState) {
        for (int i = 0; i < detectingTags.length; i++) {
            net.minecraft.tags.TagKey<Block> t = net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BLOCK, Identifier.parse(detectingTags[i]));

            if (blockState.is(t)) {
                return i;
            }
        }
        return -1;
    }
}