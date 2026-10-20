package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.handler.floo.FlooNetworkHandler;
import lumien.randomthings.tileentity.FlooBrickTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

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
public class FlooBrickBlock extends Block implements EntityBlock {
    public FlooBrickBlock() {
        super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).noOcclusion().strength(2.0F, 10.0F));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new FlooBrickTileEntity(pos, state);
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
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity te = worldIn.getBlockEntity(pos);

            if (te instanceof FlooBrickTileEntity) {
                FlooBrickTileEntity flooTE = (FlooBrickTileEntity) te;

                if (flooTE.isMaster()) {
                    breakFireplace(worldIn, pos, flooTE);
                } else {
                    UUID masterUid = flooTE.getFirePlaceUid();

                    if (masterUid != null) {
                        BlockEntity masterTe = FlooNetworkHandler.get(worldIn).getFirePlaceTE(worldIn, masterUid);

                        if (masterTe instanceof FlooBrickTileEntity) {
                            breakFireplace(worldIn, masterTe.getBlockPos(), (FlooBrickTileEntity) masterTe);
                        }
                    }
                }
            }
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    private static void breakFireplace(Level world, BlockPos masterPos, FlooBrickTileEntity masterTE) {
        List<BlockPos> children = new ArrayList<>(masterTE.getChildren());
        UUID masterUid = masterTE.getUid();

        world.setBlockAndUpdate(masterPos, Blocks.BRICKS.defaultBlockState());

        FlooNetworkHandler.get(world).brokenMaster(masterUid);

        for (BlockPos childPos : children) {
            world.setBlockAndUpdate(childPos, Blocks.BRICKS.defaultBlockState());
        }
    }
}
