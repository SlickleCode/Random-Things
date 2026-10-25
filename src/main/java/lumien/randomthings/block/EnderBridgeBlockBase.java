package lumien.randomthings.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Shared facing/active state and placement behavior for {@link
 * EnderBridgeBlock} and {@link PrismarineEnderBridgeBlock} - 1.12.2 had these
 * as two near-identical copy-pasted classes (down to duplicated {@code
 * FACING}/{@code ACTIVE} property fields); this port keeps one shared pair of
 * properties and one shared base class instead, with only {@code
 * createTileEntity} differing per subclass.
 */
public abstract class EnderBridgeBlockBase extends Block {
    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, Direction.values());
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    protected EnderBridgeBlockBase() {
        super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(1.5F));

        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, net.minecraft.world.level.redstone.Orientation fromPos, boolean isMoving) {
        BlockEntity te = worldIn.getBlockEntity(pos);

        if (te instanceof lumien.randomthings.tileentity.EnderBridgeTileEntity) {
            ((lumien.randomthings.tileentity.EnderBridgeTileEntity) te).neighborChanged(worldIn, pos);
        }
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, worldIn, pos, oldState, isMoving);
        setDefaultFacing(worldIn, pos, state);
    }

    private void setDefaultFacing(Level worldIn, BlockPos pos, BlockState state) {
        if (worldIn.isClientSide()) {
            return;
        }

        BlockState north = worldIn.getBlockState(pos.north());
        BlockState south = worldIn.getBlockState(pos.south());
        BlockState west = worldIn.getBlockState(pos.west());
        BlockState east = worldIn.getBlockState(pos.east());
        Direction facing = state.getValue(FACING);

        if (facing == Direction.NORTH && north.canOcclude() && !south.canOcclude()) {
            facing = Direction.SOUTH;
        } else if (facing == Direction.SOUTH && south.canOcclude() && !north.canOcclude()) {
            facing = Direction.NORTH;
        } else if (facing == Direction.WEST && west.canOcclude() && !east.canOcclude()) {
            facing = Direction.EAST;
        } else if (facing == Direction.EAST && east.canOcclude() && !west.canOcclude()) {
            facing = Direction.WEST;
        }

        worldIn.setBlock(pos, state.setValue(FACING, facing), 2);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LivingEntity placer = context.getPlayer();
        Direction facing = placer != null ? Direction.getApproximateNearest((float) placer.getLookAngle().x, (float) placer.getLookAngle().y, (float) placer.getLookAngle().z) : Direction.NORTH;

        return this.defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (placer != null) {
            Direction facing = Direction.getApproximateNearest((float) placer.getLookAngle().x, (float) placer.getLookAngle().y, (float) placer.getLookAngle().z);
            worldIn.setBlock(pos, state.setValue(FACING, facing), 2);
        }
    }
}
