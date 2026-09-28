package lumien.randomthings.block;

import lumien.randomthings.tileentity.BlockDestabilizerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Ported from 1.12.2's {@code BlockBlockDestabilizer}. On a redstone pulse,
 * flood-fills every block connected to whatever's directly in front of it
 * (matching by exact {@code BlockState}, or just by {@code Block} type in
 * "fuzzy" mode) and drops the whole matched structure as falling-block
 * entities, top row first. See {@code BlockDestabilizerTileEntity} for the
 * search/drop state machine.
 */
public class BlockDestabilizerBlock extends Block {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

    public BlockDestabilizerBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(5.0F));

        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BlockDestabilizerTileEntity();
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getNearestLookingDirection());
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);

            if (te instanceof BlockDestabilizerTileEntity) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (BlockDestabilizerTileEntity) te);
            }
        }

        return true;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        TileEntity te = worldIn.getTileEntity(pos);

        if (te instanceof BlockDestabilizerTileEntity) {
            ((BlockDestabilizerTileEntity) te).neighborChanged(worldIn.isBlockPowered(pos));
        }
    }
}
