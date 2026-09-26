package lumien.randomthings.block;

import lumien.randomthings.handler.floo.FlooNetworkHandler;
import lumien.randomthings.tileentity.FlooBrickTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The individual bricks a Floo Sign converts a contiguous group of vanilla
 * Bricks into, forming one fireplace. Internal-only - never obtainable as an
 * item (no {@code BlockItem} registered, matching {@code RuneBaseBlock}'s
 * precedent for a block that's only ever placed by other game logic, never
 * given directly) and deliberately given no loot table, so breaking one
 * drops nothing (matches 1.12.2's {@code quantityDropped() == 0}).
 * <p>
 * Direct port of 1.12.2's {@code BlockFlooBrick}, with its
 * {@code ILuminousBlock} full-bright tint-overlay rendering trick dropped in
 * favor of this port's already-established {@code StainedBrickBlock}
 * precedent for the exact same problem (a base texture plus a second,
 * normally-lit tint layer) - {@code ILuminousBlock}'s "render this layer at
 * full brightness regardless of world light" behavior was never carried over
 * for any of that trick's other 1.12.2 users either (see {@code LuminousBlock}
 * javadoc), so this isn't a new divergence.
 */
public class FlooBrickBlock extends Block {
    public FlooBrickBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(2.0F, 10.0F));
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FlooBrickTileEntity();
    }

    /**
     * Breaking any single brick in a fireplace (master or child) invalidates
     * the whole group - direct port of 1.12.2's {@code TileEntityFlooBrick
     * .breakBlock}, restructured onto {@code onReplaced} (this port's
     * established replacement for that hook, see {@code RuneBaseBlock}) and
     * fixed to convert the master's own blockstate first, before any child -
     * the original converted children first, which meant each child's own
     * recursive "find and break the master" lookup still found a fully
     * live master TE and re-ran the whole conversion again per child. Not a
     * behavior change a player could observe (converting an already-plain-
     * brick block to plain brick again is a no-op either way) - just removes
     * pointless repeated work.
     */
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity te = worldIn.getTileEntity(pos);

            if (te instanceof FlooBrickTileEntity) {
                FlooBrickTileEntity flooTE = (FlooBrickTileEntity) te;

                if (flooTE.isMaster()) {
                    breakFireplace(worldIn, pos, flooTE);
                } else {
                    UUID masterUid = flooTE.getFirePlaceUid();

                    if (masterUid != null) {
                        TileEntity masterTe = FlooNetworkHandler.get(worldIn).getFirePlaceTE(worldIn, masterUid);

                        if (masterTe instanceof FlooBrickTileEntity) {
                            breakFireplace(worldIn, masterTe.getPos(), (FlooBrickTileEntity) masterTe);
                        }
                    }
                }
            }
        }

        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    private static void breakFireplace(World world, BlockPos masterPos, FlooBrickTileEntity masterTE) {
        List<BlockPos> children = new ArrayList<>(masterTE.getChildren());
        UUID masterUid = masterTE.getUid();

        world.setBlockState(masterPos, Blocks.BRICKS.getDefaultState());

        FlooNetworkHandler.get(world).brokenMaster(masterUid);

        for (BlockPos childPos : children) {
            world.setBlockState(childPos, Blocks.BRICKS.getDefaultState());
        }
    }
}
