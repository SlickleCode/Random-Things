package lumien.randomthings.block;

import lumien.randomthings.tileentity.PeaceCandleTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

/**
 * Direct port of 1.12.2's {@code BlockPeaceCandle}: a standalone decorative
 * candle that suppresses natural hostile-mob spawning in a 3-chunk radius -
 * see {@link lumien.randomthings.asm.AsmHandler#overrideSpawnResult}'s
 * {@code PeaceCandleTileEntity} check (reuses the existing {@code
 * SpawnPlacementTransformer} coremod hook shared with Lapis Lamp/Slime Cube,
 * rather than a new one). Needs solid ground beneath it, same as a torch.
 * <p>
 * 1.12.2 also generated it inside a custom village church; here {@code PeaceCandleChurchPool} (or the church piece on 1.14.4)
 * swaps the vanilla church for the same building with a Peace Candle in it, in code rather than NBT files.
 */
public class PeaceCandleBlock extends Block {
    private static final VoxelShape SHAPE = Block.makeCuboidShape(6, 0, 6, 10, 10, 10);

    public PeaceCandleBlock() {
        super(Block.Properties.create(Material.MISCELLANEOUS).notSolid().doesNotBlockMovement().hardnessAndResistance(0.0F));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.down()).isSolid();
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!isValidPosition(state, worldIn, pos)) {
            spawnDrops(state, worldIn, pos);
            worldIn.removeBlock(pos, isMoving);
        }
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new PeaceCandleTileEntity();
    }
}
