package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.IPlantable;
import net.neoforged.neoforge.common.PlantType;

public class LotusSeedsItem extends Item implements IPlantable {
    public LotusSeedsItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getClickedFace() != Direction.UP) {
            return InteractionResult.FAIL;
        }

        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockPos above = pos.above();
        ItemStack stack = context.getItemInHand();

        if (context.getPlayer() != null && !context.getPlayer().mayUseItemAt(above, Direction.UP, stack) || !world.isEmptyBlock(above)) {
            return InteractionResult.FAIL;
        }

        net.minecraft.world.level.block.state.BlockState groundState = world.getBlockState(pos);

        if (!groundState.getBlock().canSustainPlant(groundState, world, pos, Direction.UP, this)) {
            return InteractionResult.FAIL;
        }

        world.setBlockAndUpdate(above, ModBlocks.LOTUS.defaultBlockState());
        world.playSound(null, pos, SoundType.GRASS.getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);

        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public PlantType getPlantType(net.minecraft.world.level.BlockGetter world, BlockPos pos) {
        return PlantType.PLAINS;
    }

    @Override
    public net.minecraft.world.level.block.state.BlockState getPlant(net.minecraft.world.level.BlockGetter world, BlockPos pos) {
        return ModBlocks.LOTUS.defaultBlockState();
    }
}
