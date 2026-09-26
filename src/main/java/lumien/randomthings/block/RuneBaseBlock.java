package lumien.randomthings.block;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.RuneDustItem;
import lumien.randomthings.tileentity.RuneBaseTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * A paper-thin decal placed on top of a solid block, holding a 4x4 grid of
 * colored rune pixels ({@link RuneDustItem} places them, see that class).
 * Left-click (not break) removes one pixel at a time; the block itself
 * vanishes once emptied out, or if its support block is removed. Never drops
 * as an item itself (1.12.2's {@code INoItem}/{@code quantityDropped() == 0}
 * - only its individual rune-dust pixels do). Direct port of 1.12.2's
 * {@code BlockRuneBase}; the rendering itself (both the colored pixels and
 * the "connects to a matching color in the adjacent Rune Base" border strips)
 * moved from a custom baked model driven by {@code ExtendedBlockState} (not a
 * thing in 1.14.4) to {@link lumien.randomthings.client.renderer.RuneBaseTileEntityRenderer},
 * matching this port's established custom-TESR precedent for anything that
 * used to need per-instance dynamic render data.
 */
public class RuneBaseBlock extends Block {
    private static final VoxelShape SHAPE = Block.makeCuboidShape(0, 0, 0, 16, 0.1, 16);

    public RuneBaseBlock() {
        super(Block.Properties.create(Material.SAND).hardnessAndResistance(0.2F).doesNotBlockMovement());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    /**
     * No baked model at all - every visible pixel is drawn by
     * {@link lumien.randomthings.client.renderer.RuneBaseTileEntityRenderer},
     * which fires independently of this (TESR dispatch is keyed off the
     * registered tile entity type, not the block's render type - confirmed
     * already elsewhere in this port, e.g. Biome Radar's TESR runs
     * alongside its own separate real baked model). No blockstate/model JSON
     * exists for this block at all, matching how it needs none.
     */
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new RuneBaseTileEntity();
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!Block.hasSolidSide(worldIn.getBlockState(pos.down()), worldIn, pos.down(), Direction.UP)) {
            worldIn.removeBlock(pos, false);
        }
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity te = worldIn.getTileEntity(pos);

            if (te instanceof RuneBaseTileEntity) {
                DyeColor[][] runeData = ((RuneBaseTileEntity) te).getRuneData();

                for (int x = 0; x < 4; x++) {
                    for (int y = 0; y < 4; y++) {
                        if (runeData[x][y] != null) {
                            ItemStack dustStack = new ItemStack(ModItems.RUNE_DUST.get(runeData[x][y]));

                            ItemEntity itemEntity = new ItemEntity(worldIn, pos.getX() + x / 4.0 + 0.125, pos.getY() + 0.1, pos.getZ() + y / 4.0 + 0.125, dustStack);
                            itemEntity.setNoPickupDelay();
                            worldIn.addEntity(itemEntity);
                        }
                    }
                }
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn) {
        if (!worldIn.isRemote) {
            Vec3d start = playerIn.getEyePosition(1.0F);
            Vec3d end = start.add(playerIn.getLookVec().scale(6));

            RayTraceResult result = worldIn.rayTraceBlocks(new RayTraceContext(start, end, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, playerIn));

            if (result.getType() == RayTraceResult.Type.BLOCK && ((net.minecraft.util.math.BlockRayTraceResult) result).getPos().equals(pos)) {
                Vec3d hitVec = result.getHitVec().subtract(new Vec3d(pos.getX(), pos.getY(), pos.getZ()));

                TileEntity te = worldIn.getTileEntity(pos);

                if (!(te instanceof RuneBaseTileEntity)) {
                    return;
                }

                RuneBaseTileEntity runeTe = (RuneBaseTileEntity) te;
                DyeColor[][] runeData = runeTe.getRuneData();

                int x = net.minecraft.util.math.MathHelper.clamp((int) Math.floor(hitVec.x * 4), 0, 3);
                int y = net.minecraft.util.math.MathHelper.clamp((int) Math.floor(hitVec.z * 4), 0, 3);

                if (runeData[x][y] != null) {
                    ItemEntity itemEntity = new ItemEntity(worldIn, pos.getX() + hitVec.x, pos.getY() + 0.1, pos.getZ() + hitVec.z, new ItemStack(ModItems.RUNE_DUST.get(runeData[x][y])));
                    itemEntity.setNoPickupDelay();
                    worldIn.addEntity(itemEntity);

                    runeData[x][y] = null;
                    runeTe.syncTE();

                    worldIn.playSound(null, pos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1F, 0.8F);

                    if (runeTe.isEmpty()) {
                        worldIn.removeBlock(pos, false);
                    }
                }
            }
        }

        super.onBlockClicked(state, worldIn, pos, playerIn);
    }
}
