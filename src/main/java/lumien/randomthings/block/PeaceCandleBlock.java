package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.PeaceCandleTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

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
public class PeaceCandleBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(6, 0, 6, 10, 10, 10);

    public PeaceCandleBlock() {
        super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.NONE).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).noOcclusion().noCollision().strength(0.0F));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos.below()).canOcclude();
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, net.minecraft.world.level.redstone.Orientation fromPos, boolean isMoving) {
        if (!canSurvive(state, worldIn, pos)) {
            dropResources(state, worldIn, pos);
            worldIn.removeBlock(pos, isMoving);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new PeaceCandleTileEntity(pos, state);
    }
}
