package lumien.randomthings.item;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Plants {@link #plantedBlock} in the air space above the clicked block, like
 * a seed item. Shared by the three 1.12.2 "beans" metadata variants (plain
 * bean, lesser magic bean, magic bean), each now its own registered item
 * pointing at a different block per the discrete-block-per-variant
 * convention used throughout this port.
 */
public class BeanItem extends Item {
    private final Block plantedBlock;
    private final boolean scheduleGrowth;

    public BeanItem(Item.Properties properties, Block plantedBlock, boolean scheduleGrowth) {
        super(properties);

        this.plantedBlock = plantedBlock;
        this.scheduleGrowth = scheduleGrowth;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.FAIL;
        }

        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos().above();
        ItemStack stack = context.getItemInHand();

        if (context.getPlayer() != null && !context.getPlayer().mayUseItemAt(pos, Direction.UP, stack)) {
            return InteractionResult.FAIL;
        }

        if (!world.isEmptyBlock(pos)) {
            return InteractionResult.FAIL;
        }

        if (!plantedBlock.defaultBlockState().canSurvive(world, pos)) {
            return InteractionResult.FAIL;
        }

        world.setBlockAndUpdate(pos, plantedBlock.defaultBlockState());

        if (scheduleGrowth) {
            world.scheduleTick(pos, plantedBlock, 20);
        }

        world.playSound(null, pos, plantedBlock.getSoundType(plantedBlock.defaultBlockState()).getPlaceSound(), SoundSource.BLOCKS, 1, 1);

        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }
}
