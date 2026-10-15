package lumien.randomthings.item;

import lumien.randomthings.entity.EclipsedClockEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Right-click a wall to hang an {@link EclipsedClockEntity} on it. Its "time"
 * item-model property (still {@code IItemPropertyGetter}, unchanged from
 * 1.12.2 - vanilla's own clock uses the exact same mechanism in this Forge
 * version) drives which of the 64 dial-face model variants renders, reading
 * the stored NBT target time exactly like the placed entity does. Direct
 * port of 1.12.2's {@code ItemEclipsedClock}.
 */
public class EclipsedClockItem extends Item implements IClientItemProperties{
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

        HangingEntity hanging = new EclipsedClockEntity(context.getLevel(), placedPos, facing);

        if (hanging.survives()) {
            if (!context.getLevel().isClientSide) {
                hanging.playPlacementSound();
                context.getLevel().addFreshEntity(hanging);
            }

            stack.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void registerClientProperties()
	{
		net.minecraft.client.renderer.item.ItemProperties.register(this, new ResourceLocation("time"), (stack, world, entity, seed) -> {
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
}
