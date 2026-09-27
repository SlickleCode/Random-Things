package lumien.randomthings.block;

import lumien.randomthings.tileentity.RainShieldTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.Random;

/**
 * A candle-on-a-stick that stops rain/snow from mechanically affecting
 * anything within its (config-free, fixed) radius - see {@link
 * lumien.randomthings.asm.AsmHandler#overrideIsRainingAt} for how. Needs a
 * solid block underneath (auto-breaks and drops otherwise, like a torch -
 * enforced by {@link #isValidPosition}, not manual neighbor-change bookkeeping).
 * Direct port of 1.12.2's {@code BlockRainShield}.
 * <p>
 * Disclosed simplification: this only fixes the mechanical side (no wetting,
 * no snow/ice formation, no fire extinguishing near the shield) - the purely
 * cosmetic falling-rain/snow visual itself isn't locally suppressed. 1.12.2's
 * own client-side half needed a separate ASM patch into {@code
 * EntityRenderer}'s rain-rendering loop; confirmed via {@code javap -c} that
 * 1.14.4's equivalent ({@code GameRenderer.renderRainSnow}) computes its
 * per-column skip condition inline (not through the same method this port's
 * server-side redirect targets), so reproducing it means patching deep inside
 * a ~300-line rendering method's own loop body, not wrapping a return value
 * like every other Batch 7 redirect so far - not worth it for a purely
 * cosmetic mismatch. The block's own flame/smoke swirl (see {@link
 * #animateTick}) still visibly signals "this is warding off the rain" to a
 * player standing near it.
 */
public class RainShieldBlock extends Block {
    private static final VoxelShape SHAPE = Block.makeCuboidShape(6, 0, 6, 10, 16, 10);

    public RainShieldBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(2.0F));
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
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
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new RainShieldTileEntity();
    }

    @Override
    public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        TileEntity te = worldIn.getTileEntity(pos);

        if (te instanceof RainShieldTileEntity) {
            ((RainShieldTileEntity) te).updatePowerState(worldIn.isBlockPowered(pos));
        }
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        TileEntity te = worldIn.getTileEntity(pos);

        if (te instanceof RainShieldTileEntity) {
            ((RainShieldTileEntity) te).updatePowerState(worldIn.isBlockPowered(pos));
        }
    }

    @Override
    public void animateTick(BlockState state, World worldIn, BlockPos pos, Random rand) {
        if (!worldIn.isRaining()) {
            return;
        }

        for (double mod = 0; mod < 1; mod += 0.1) {
            for (double a = 0; a <= Math.PI * 2D; a += (Math.PI * 2D) / 3D) {
                double x = pos.getX() + 0.5 + (1 - mod) * Math.cos(a);
                double z = pos.getZ() + 0.5 + (1 - mod) * Math.sin(a);

                worldIn.addParticle(ParticleTypes.FLAME, x, pos.getY() + 0.7 + mod, z, 0, 0, 0);
                worldIn.addParticle(ParticleTypes.SMOKE, x, pos.getY() + 0.6 + mod, z, 0, 0, 0);
            }
        }
    }
}
