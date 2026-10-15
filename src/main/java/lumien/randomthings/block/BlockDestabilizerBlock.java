package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import lumien.randomthings.tileentity.BlockDestabilizerTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.InteractionResult;

/**
 * Ported from 1.12.2's {@code BlockBlockDestabilizer}. On a redstone pulse,
 * flood-fills every block connected to whatever's directly in front of it
 * (matching by exact {@code BlockState}, or just by {@code Block} type in
 * "fuzzy" mode) and drops the whole matched structure as falling-block
 * entities, top row first. See {@code BlockDestabilizerTileEntity} for the
 * search/drop state machine.
 */
public class BlockDestabilizerBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

    public BlockDestabilizerBlock() {
        super(Block.Properties.of(Material.STONE).strength(5.0F));

        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new BlockDestabilizerTileEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return Tickers.of(type, ModTileEntityTypes.BLOCK_DESTABILIZER, (l, p, s2, be) -> be.tick());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getNearestLookingDirection());
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (!worldIn.isClientSide) {
            BlockEntity te = worldIn.getBlockEntity(pos);

            if (te instanceof BlockDestabilizerTileEntity) {
                NetworkHooks.openScreen((ServerPlayer) player, (BlockDestabilizerTileEntity) te);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        BlockEntity te = worldIn.getBlockEntity(pos);

        if (te instanceof BlockDestabilizerTileEntity) {
            ((BlockDestabilizerTileEntity) te).neighborChanged(worldIn.hasNeighborSignal(pos));
        }
    }
}
