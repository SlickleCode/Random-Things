package lumien.randomthings.item;

import lumien.randomthings.util.StackTag;
import lumien.randomthings.entity.EclipsedClockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;


/**
 * Right-click a wall to hang an {@link EclipsedClockEntity} on it. Its "time"
 * item-model property (still {@code IItemPropertyGetter}, unchanged from
 * 1.12.2 - vanilla's own clock uses the exact same mechanism in this Forge
 * version) drives which of the 64 dial-face model variants renders, reading
 * the stored NBT target time exactly like the placed entity does. Direct
 * port of 1.12.2's {@code ItemEclipsedClock}.
 */
public class EclipsedClockItem extends Item {
    public EclipsedClockItem(Item.Properties properties) {
        super(properties);

    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction facing = context.getClickedFace();
        ItemStack stack = context.getItemInHand();
        BlockPos placedPos = context.getClickedPos().relative(facing);

        if (facing == Direction.DOWN || facing == Direction.UP || context.getPlayer() == null || !context.getPlayer().mayUseItemAt(placedPos, facing, stack)) {
            return InteractionResult.FAIL;
        }

        EclipsedClockEntity hanging = new EclipsedClockEntity(context.getLevel(), placedPos, facing);

        if (StackTag.contains(stack, "targetTime")) {
            hanging.setTargetTime(StackTag.getInt(stack, "targetTime"));
        }

        if (hanging.survives()) {
            if (!context.getLevel().isClientSide()) {
                hanging.playPlacementSound();
                context.getLevel().addFreshEntity(hanging);
            }

            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }
}
