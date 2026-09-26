package lumien.randomthings.item;

import lumien.randomthings.entity.EclipsedClockEntity;
import net.minecraft.entity.item.HangingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

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

        this.addPropertyOverride(new ResourceLocation("time"), (stack, world, entity) -> {
            int time = 6000;

            if (stack.hasTag() && stack.getTag().contains("targetTime")) {
                time = stack.getTag().getInt("targetTime");
            }

            int i = time % 24000;
            float f = ((float) i) / 24000.0F - 0.25F;

            if (f < 0.0F) {
                ++f;
            }

            if (f > 1.0F) {
                --f;
            }

            float f1 = 1.0F - (float) ((Math.cos((double) f * Math.PI) + 1.0D) / 2.0D);
            f = f + (f1 - f) / 3.0F;
            return f;
        });
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        Direction facing = context.getFace();
        ItemStack stack = context.getItem();
        BlockPos placedPos = context.getPos().offset(facing);

        if (facing == Direction.DOWN || facing == Direction.UP || context.getPlayer() == null || !context.getPlayer().canPlayerEdit(placedPos, facing, stack)) {
            return ActionResultType.FAIL;
        }

        HangingEntity hanging = new EclipsedClockEntity(context.getWorld(), placedPos, facing);

        if (hanging.onValidSurface()) {
            if (!context.getWorld().isRemote) {
                hanging.playPlaceSound();
                context.getWorld().addEntity(hanging);
            }

            stack.shrink(1);
        }

        return ActionResultType.SUCCESS;
    }
}
