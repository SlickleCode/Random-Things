package lumien.randomthings.item;

import lumien.randomthings.block.ColoredGrassBlock;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.lib.IRTItemColor;
import net.minecraft.block.Blocks;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Right-click a plain Dirt block to turn it into (colored) grass, consuming one seed. A {@code null}
 * color is the plain "Grass Seeds" variant (makes vanilla grass); otherwise one instance per
 * {@link DyeColor} makes that colored grass, matching this port's "each former metadata sub-item is
 * its own registered Item" convention. Direct port of 1.12.2's {@code ItemGrassSeeds}. Unlike 1.12.2
 * it isn't consumed in creative mode.
 */
public class GrassSeedsItem extends Item implements IRTItemColor {
    /** 1.12.2's tint for the uncolored variant. */
    private static final int NORMAL_COLOR = 3512880;

    private final DyeColor color;

    public GrassSeedsItem(Item.Properties properties, DyeColor color) {
        super(properties);

        this.color = color;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();

        if (world.getBlockState(pos).getBlock() != Blocks.DIRT) {
            return ActionResultType.FAIL;
        }

        if (!world.isRemote) {
            if (color == null) {
                world.setBlockState(pos, Blocks.GRASS_BLOCK.getDefaultState());
            } else {
                world.setBlockState(pos, ModBlocks.COLORED_GRASS.getDefaultState().with(ColoredGrassBlock.COLOR, color));
            }

            if (context.getPlayer() == null || !context.getPlayer().abilities.isCreativeMode) {
                context.getItem().shrink(1);
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public int getColorFromItemstack(ItemStack stack, int tintIndex) {
        return color == null ? NORMAL_COLOR : ColoredGrassBlock.getTint(color);
    }
}
