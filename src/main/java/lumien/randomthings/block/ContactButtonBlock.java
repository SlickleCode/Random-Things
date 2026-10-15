package lumien.randomthings.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import java.util.Random;
import net.minecraft.server.level.ServerLevel;

/**
 * A pressure-plate-like button that is triggered by external code (e.g. an
 * entity-collision handler) rather than by a player click, unlike vanilla's
 * ButtonBlock.
 */
public class ContactButtonBlock extends Block {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public ContactButtonBlock() {
        super(Block.Properties.of(Material.STONE, MaterialColor.STONE).strength(1.5F).randomTicks());

        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    public int tickRate(Level worldIn) {
        return 20;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!isMoving && state.getBlock() != newState.getBlock()) {
            if (state.getValue(POWERED)) {
                this.notifyNeighbors(worldIn, pos, state.getValue(FACING));
            }

            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWERED) ? 15 : 0;
    }

    @Override
    @SuppressWarnings("deprecation")
    public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side) {
        return blockState.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        if (!worldIn.isClientSide && state.getValue(POWERED)) {
            worldIn.setBlockAndUpdate(pos, state.setValue(POWERED, false));
            this.notifyNeighbors(worldIn, pos, state.getValue(FACING));
            worldIn.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK, SoundSource.BLOCKS, 0.3F, 0.5F);
        }
    }

    private void notifyNeighbors(Level worldIn, BlockPos pos, Direction facing) {
        worldIn.updateNeighborsAt(pos, this);

        for (Direction f : Direction.values()) {
            worldIn.updateNeighborsAt(pos.relative(f), this);
        }
    }

    @Override
    public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
        LivingEntity placer = context.getPlayer();
        Direction facing = placer != null ? Direction.getNearest((float) placer.getLookAngle().x, (float) placer.getLookAngle().y, (float) placer.getLookAngle().z) : Direction.NORTH;

        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            Direction facing = Direction.getNearest((float) placer.getLookAngle().x, (float) placer.getLookAngle().y, (float) placer.getLookAngle().z);
            worldIn.setBlock(pos, state.setValue(FACING, facing), 2);
        }
    }

    /**
     * Called by an external trigger (e.g. an entity walking into the block) to
     * power this button for one {@link #tickRate} worth of ticks.
     */
    public void activate(Level world, BlockPos pos, Direction fromFacing) {
        BlockState state = world.getBlockState(pos);

        if (state.getValue(POWERED)) {
            return;
        }

        world.setBlock(pos, state.setValue(POWERED, true), 3);
        world.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK, SoundSource.BLOCKS, 0.3F, 0.6F);
        this.notifyNeighbors(world, pos, state.getValue(FACING));
        world.scheduleTick(pos, this, this.tickRate(world));
    }
}
