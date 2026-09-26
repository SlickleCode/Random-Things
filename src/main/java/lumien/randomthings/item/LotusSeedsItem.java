package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import net.minecraft.block.SoundType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

public class LotusSeedsItem extends Item implements IPlantable {
    public LotusSeedsItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        if (context.getFace() != Direction.UP) {
            return ActionResultType.FAIL;
        }

        World world = context.getWorld();
        BlockPos pos = context.getPos();
        BlockPos above = pos.up();
        ItemStack stack = context.getItem();

        if (context.getPlayer() != null && !context.getPlayer().canPlayerEdit(above, Direction.UP, stack) || !world.isAirBlock(above)) {
            return ActionResultType.FAIL;
        }

        net.minecraft.block.BlockState groundState = world.getBlockState(pos);

        if (!groundState.getBlock().canSustainPlant(groundState, world, pos, Direction.UP, this)) {
            return ActionResultType.FAIL;
        }

        world.setBlockState(above, ModBlocks.LOTUS.getDefaultState());
        world.playSound(null, pos, SoundType.PLANT.getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);

        stack.shrink(1);
        return ActionResultType.SUCCESS;
    }

    @Override
    public PlantType getPlantType(net.minecraft.world.IBlockReader world, BlockPos pos) {
        return PlantType.Plains;
    }

    @Override
    public net.minecraft.block.BlockState getPlant(net.minecraft.world.IBlockReader world, BlockPos pos) {
        return ModBlocks.LOTUS.getDefaultState();
    }
}
