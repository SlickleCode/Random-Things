package lumien.randomthings.block;

import lumien.randomthings.tileentity.BlockBreakerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * Ported from 1.12.2's {@code BlockBlockBreaker}. A machine that continuously
 * mines whatever block sits in front of it (matching the target's own real
 * hardness/tool-speed) unless redstone-powered, using a {@code FakePlayer}
 * holding an unbreakable, Magnetic-enchanted pickaxe - see
 * {@code BlockBreakerTileEntity} for why the Magnetic enchantment (needed as
 * this feature's own listed prerequisite) is what makes drops land in an
 * inventory instead of spawning on the ground.
 */
public class BlockBreakerBlock extends Block {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

    public BlockBreakerBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3.5F));

        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BlockBreakerTileEntity();
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getNearestLookingDirection());
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, net.minecraft.util.math.BlockPos pos, Block blockIn, net.minecraft.util.math.BlockPos fromPos, boolean isMoving) {
        TileEntity te = worldIn.getTileEntity(pos);

        if (te instanceof BlockBreakerTileEntity) {
            ((BlockBreakerTileEntity) te).neighborChanged();
        }
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, net.minecraft.util.math.BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity te = worldIn.getTileEntity(pos);

            if (te instanceof BlockBreakerTileEntity) {
                ((BlockBreakerTileEntity) te).onBreakerBroken();
            }
        }

        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }
}
