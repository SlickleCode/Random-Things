package lumien.randomthings.item;

import lumien.randomthings.entity.ArtificialEndPortalEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Right-click an End Rod that's the top of a correctly-built ritual
 * structure (see {@link ArtificialEndPortalEntity#isValidPosition}) to spawn
 * an Artificial End Portal there, consuming the tear. 1.12.2's version was
 * one damage-value subtype of the catch-all {@code ItemIngredient}; this
 * port gives it its own registered item, matching this port's established
 * convention. Direct port of {@code ItemIngredient}'s {@code EVIL_TEAR}
 * special case in {@code onItemUse}.
 */
public class EvilTearItem extends Item {
    public EvilTearItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        ItemStack stack = context.getItem();

        BlockState state = world.getBlockState(pos);

        if (state.getBlock() == Blocks.END_ROD) {
            BlockPos portalCenter = pos.down(3);

            if (ArtificialEndPortalEntity.isValidPosition(world, portalCenter, true)) {
                if (!world.isRemote) {
                    world.addEntity(new ArtificialEndPortalEntity(world, portalCenter.getX() + 0.5, portalCenter.getY(), portalCenter.getZ() + 0.5));
                    stack.shrink(1);
                }

                return ActionResultType.SUCCESS;
            }
        }

        return ActionResultType.PASS;
    }
}
