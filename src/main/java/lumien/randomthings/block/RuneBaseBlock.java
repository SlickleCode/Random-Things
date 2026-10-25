package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.item.RuneDustItem;
import lumien.randomthings.item.RuneDustItems;
import lumien.randomthings.tileentity.RuneBaseTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

/**
 * A paper-thin decal placed on top of a solid block, holding a 4x4 grid of
 * colored rune pixels ({@link RuneDustItem} places them, see that class).
 * Left-click (not break) drops every pixel at once and removes the block
 * (see {@link #onBlockClicked}'s javadoc - a deliberate 2026-09-27 deviation
 * from 1.12.2's real one-pixel-per-click behavior, per user request); it
 * also vanishes if its support block is removed. Never drops
 * as an item itself (1.12.2's {@code INoItem}/{@code quantityDropped() == 0}
 * - only its individual rune-dust pixels do). Direct port of 1.12.2's
 * {@code BlockRuneBase}; the rendering itself (both the colored pixels and
 * the "connects to a matching color in the adjacent Rune Base" border strips)
 * moved from a custom baked model driven by {@code ExtendedBlockState} (not a
 * thing in 1.14.4) to {@link lumien.randomthings.client.renderer.RuneBaseTileEntityRenderer},
 * matching this port's established custom-TESR precedent for anything that
 * used to need per-instance dynamic render data.
 */
public class RuneBaseBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 0.1, 16);

    public RuneBaseBlock() {
        super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.SAND).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.SNARE).strength(0.2F).noCollision());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /**
     * No real baked model - every visible pixel is drawn by
     * {@link lumien.randomthings.client.renderer.RuneBaseTileEntityRenderer},
     * which fires independently of this (TESR dispatch is keyed off the
     * registered tile entity type, not the block's render type - confirmed
     * already elsewhere in this port, e.g. Biome Radar's TESR runs
     * alongside its own separate real baked model). The blockstate/model JSON
     * that does exist (an empty-elements model, reusing Rune Dust's own
     * texture only for the "particle" field) draws nothing - it's there
     * purely so {@code ModelBakery} doesn't log a spurious "missing model"
     * warning on every load and so breaking this block shows a themed
     * particle effect instead of the default missing-texture one.
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new RuneBaseTileEntity(pos, state);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, net.minecraft.world.level.redstone.Orientation fromPos, boolean isMoving) {
        if (!Block.canSupportCenter(worldIn, pos.below(), Direction.UP)) {
            worldIn.removeBlock(pos, false);
        }
    }

    public void rtOnRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity te = worldIn.getBlockEntity(pos);

            if (te instanceof RuneBaseTileEntity) {
                DyeColor[][] runeData = ((RuneBaseTileEntity) te).getRuneData();

                for (int x = 0; x < 4; x++) {
                    for (int y = 0; y < 4; y++) {
                        if (runeData[x][y] != null) {
                            ItemStack dustStack = new ItemStack(RuneDustItems.BY_COLOR.get(runeData[x][y]));

                            ItemEntity itemEntity = new ItemEntity(worldIn, pos.getX() + x / 4.0 + 0.125, pos.getY() + 0.1, pos.getZ() + y / 4.0 + 0.125, dustStack);
                            itemEntity.setNoPickUpDelay();
                            worldIn.addFreshEntity(itemEntity);
                        }
                    }
                }
            }

        }
    }

    /**
     * Left-click (not fully breaking) drops every pixel on the block at once
     * and removes it, rather than one pixel per click - per explicit user
     * request, 2026-09-27, a deliberate deviation from 1.12.2's real
     * behavior (ground-truthed from the actual {@code BlockRuneBase
     * #onBlockClicked} source: it only ever removed the single cell under
     * the cursor, same as this port originally matched). Reuses {@link
     * #onReplaced}'s existing "drop every pixel" loop via {@code
     * world.removeBlock} instead of duplicating it - that method already
     * runs whenever this block is removed for any reason (support gone,
     * explicit break, this), so there's one single source of truth for the
     * drop logic.
     */
    @Override
    public void attack(BlockState state, Level worldIn, BlockPos pos, Player playerIn) {
        if (!worldIn.isClientSide()) {
            Vec3 start = playerIn.getEyePosition(1.0F);
            Vec3 end = start.add(playerIn.getLookAngle().scale(6));

            HitResult result = worldIn.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, playerIn));

            if (result.getType() == HitResult.Type.BLOCK && ((net.minecraft.world.phys.BlockHitResult) result).getBlockPos().equals(pos)) {
                BlockEntity te = worldIn.getBlockEntity(pos);

                if (!(te instanceof RuneBaseTileEntity) || ((RuneBaseTileEntity) te).isEmpty()) {
                    return;
                }

                worldIn.playSound(null, pos, SoundEvents.STONE_BREAK, SoundSource.BLOCKS, 1F, 0.8F);
                worldIn.removeBlock(pos, false);
            }
        }

        super.attack(state, worldIn, pos, playerIn);
    }
}
