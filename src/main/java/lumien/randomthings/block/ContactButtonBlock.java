package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.Random;

/**
 * A pressure-plate-like button that is triggered by external code (e.g. an
 * entity-collision handler) rather than by a player click, unlike vanilla's
 * ButtonBlock.
 */
public class ContactButtonBlock extends Block {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public ContactButtonBlock() {
        super(Block.Properties.create(Material.ROCK, MaterialColor.STONE).hardnessAndResistance(1.5F).tickRandomly());

        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(POWERED, false));
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    public int tickRate(World worldIn) {
        return 20;
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && state.getBlock() != newState.getBlock()) {
            if (state.get(POWERED)) {
                this.notifyNeighbors(worldIn, pos, state.get(FACING));
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.get(POWERED) ? 15 : 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getStrongPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.get(POWERED) ? 15 : 0;
    }

    @Override
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public void tick(BlockState state, World worldIn, BlockPos pos, Random rand) {
        if (!worldIn.isRemote && state.get(POWERED)) {
            worldIn.setBlockState(pos, state.with(POWERED, false));
            this.notifyNeighbors(worldIn, pos, state.get(FACING));
            worldIn.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 0.5F);
        }
    }

    private void notifyNeighbors(World worldIn, BlockPos pos, Direction facing) {
        worldIn.notifyNeighborsOfStateChange(pos, this);

        for (Direction f : Direction.values()) {
            worldIn.notifyNeighborsOfStateChange(pos.offset(f), this);
        }
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.item.BlockItemUseContext context) {
        LivingEntity placer = context.getPlayer();
        Direction facing = placer != null ? Direction.getFacingFromVector((float) placer.getLookVec().x, (float) placer.getLookVec().y, (float) placer.getLookVec().z) : Direction.NORTH;

        return this.getDefaultState().with(FACING, facing);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            Direction facing = Direction.getFacingFromVector((float) placer.getLookVec().x, (float) placer.getLookVec().y, (float) placer.getLookVec().z);
            worldIn.setBlockState(pos, state.with(FACING, facing), 2);
        }
    }

    /**
     * Called by an external trigger (e.g. an entity walking into the block) to
     * power this button for one {@link #tickRate} worth of ticks.
     */
    public void activate(World world, BlockPos pos, Direction fromFacing) {
        BlockState state = world.getBlockState(pos);

        if (state.get(POWERED)) {
            return;
        }

        world.setBlockState(pos, state.with(POWERED, true), 3);
        world.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK, SoundCategory.BLOCKS, 0.3F, 0.6F);
        this.notifyNeighbors(world, pos, state.get(FACING));
        world.getPendingBlockTicks().scheduleTick(pos, this, this.tickRate(world));
    }
}
