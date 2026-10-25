package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import net.minecraft.core.BlockPos;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import lumien.randomthings.tileentity.BlockBreakerTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

/**
 * Ported from 1.12.2's {@code BlockBlockBreaker}. A machine that continuously
 * mines whatever block sits in front of it (matching the target's own real
 * hardness/tool-speed) unless redstone-powered, using a {@code FakePlayer}
 * holding an unbreakable, Magnetic-enchanted pickaxe - see
 * {@code BlockBreakerTileEntity} for why the Magnetic enchantment (needed as
 * this feature's own listed prerequisite) is what makes drops land in an
 * inventory instead of spawning on the ground.
 */
public class BlockBreakerBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, Direction.values());

    public BlockBreakerBlock() {
        super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(3.5F));

        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new BlockBreakerTileEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return Tickers.of(type, ModTileEntityTypes.BLOCK_BREAKER, (l, p, s2, be) -> be.tick());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, net.minecraft.core.BlockPos pos, Block blockIn, net.minecraft.world.level.redstone.Orientation fromPos, boolean isMoving) {
        BlockEntity te = worldIn.getBlockEntity(pos);

        if (te instanceof BlockBreakerTileEntity) {
            ((BlockBreakerTileEntity) te).neighborChanged();
        }
    }

    public void rtOnRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity te = worldIn.getBlockEntity(pos);

            if (te instanceof BlockBreakerTileEntity) {
                ((BlockBreakerTileEntity) te).onBreakerBroken();
            }
        }

    }
}
