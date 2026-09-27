package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Shared facing/active state and placement behavior for {@link
 * EnderBridgeBlock} and {@link PrismarineEnderBridgeBlock} - 1.12.2 had these
 * as two near-identical copy-pasted classes (down to duplicated {@code
 * FACING}/{@code ACTIVE} property fields); this port keeps one shared pair of
 * properties and one shared base class instead, with only {@code
 * createTileEntity} differing per subclass.
 */
public abstract class EnderBridgeBlockBase extends Block {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    protected EnderBridgeBlockBase() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5F));

        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH).with(ACTIVE, false));
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        TileEntity te = worldIn.getTileEntity(pos);

        if (te instanceof lumien.randomthings.tileentity.EnderBridgeTileEntity) {
            ((lumien.randomthings.tileentity.EnderBridgeTileEntity) te).neighborChanged(worldIn, pos);
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, worldIn, pos, oldState, isMoving);
        setDefaultFacing(worldIn, pos, state);
    }

    private void setDefaultFacing(World worldIn, BlockPos pos, BlockState state) {
        if (worldIn.isRemote) {
            return;
        }

        BlockState north = worldIn.getBlockState(pos.north());
        BlockState south = worldIn.getBlockState(pos.south());
        BlockState west = worldIn.getBlockState(pos.west());
        BlockState east = worldIn.getBlockState(pos.east());
        Direction facing = state.get(FACING);

        if (facing == Direction.NORTH && north.isSolid() && !south.isSolid()) {
            facing = Direction.SOUTH;
        } else if (facing == Direction.SOUTH && south.isSolid() && !north.isSolid()) {
            facing = Direction.NORTH;
        } else if (facing == Direction.WEST && west.isSolid() && !east.isSolid()) {
            facing = Direction.EAST;
        } else if (facing == Direction.EAST && east.isSolid() && !west.isSolid()) {
            facing = Direction.WEST;
        }

        worldIn.setBlockState(pos, state.with(FACING, facing), 2);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
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
}
