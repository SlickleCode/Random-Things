package lumien.randomthings.item;

import lumien.randomthings.entity.ArtificialEndPortalEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

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
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        BlockState state = world.getBlockState(pos);

        if (state.getBlock() == Blocks.END_ROD && lumien.randomthings.config.RTConfig.ARTIFICIAL_END_PORTAL.get()) {
            BlockPos portalCenter = pos.below(3);

            if (ArtificialEndPortalEntity.isValidPosition(world, portalCenter, true)) {
                if (!world.isClientSide) {
                    world.addFreshEntity(new ArtificialEndPortalEntity(world, portalCenter.getX() + 0.5, portalCenter.getY(), portalCenter.getZ() + 0.5));
                    stack.shrink(1);
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}
