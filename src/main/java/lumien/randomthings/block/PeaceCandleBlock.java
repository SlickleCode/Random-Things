package lumien.randomthings.block;

import lumien.randomthings.tileentity.PeaceCandleTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
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
 * Disclosed simplification: 1.12.2 also had a ~33% chance to generate inside
 * a custom "village church" building it added as a brand-new village piece
 * (hardcoded into {@code StructureVillagePieces$Church} via ASM). 1.14.4's
 * village generator is entirely data-driven (jigsaw structure templates, no
 * hardcoded Java piece classes to hook into), so reproducing a whole new
 * custom building would mean hand-authoring NBT structure data and a
 * template-pool override with no in-game structure-block tooling available
 * to build/export it correctly - deferred rather than guessed at. The block
 * and its mob-suppression are otherwise fully functional; it's creative-menu
 * only until natural generation exists.
 */
public class PeaceCandleBlock extends Block {
    private static final VoxelShape SHAPE = Block.makeCuboidShape(6, 0, 6, 10, 10, 10);

    public PeaceCandleBlock() {
        super(Block.Properties.create(Material.MISCELLANEOUS).doesNotBlockMovement().hardnessAndResistance(0.0F));
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
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
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
