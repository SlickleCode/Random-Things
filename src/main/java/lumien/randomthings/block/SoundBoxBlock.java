package lumien.randomthings.block;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.tileentity.SoundBoxTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * Insert a {@link lumien.randomthings.item.SoundPatternItem} and power it
 * with redstone to play that sound. Direct port of 1.12.2's
 * {@code BlockSoundBox}.
 */
public class SoundBoxBlock extends Block {
    public static final BooleanProperty HAS_PATTERN = BooleanProperty.create("has_pattern");

    public SoundBoxBlock() {
        super(Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(0.8F));

        this.setDefaultState(this.stateContainer.getBaseState().with(HAS_PATTERN, false));
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(HAS_PATTERN);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SoundBoxTileEntity();
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity te = worldIn.getTileEntity(pos);

            if (te instanceof SoundBoxTileEntity) {
                ItemStack pattern = ((SoundBoxTileEntity) te).getPattern();

                if (!pattern.isEmpty()) {
                    Block.spawnAsEntity(worldIn, pos, pattern);
                }
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileEntity te = worldIn.getTileEntity(pos);

        if (!(te instanceof SoundBoxTileEntity)) {
            return false;
        }

        SoundBoxTileEntity soundBox = (SoundBoxTileEntity) te;

        if (soundBox.hasPattern()) {
            if (!worldIn.isRemote) {
                ItemStack pattern = soundBox.getPattern();
                soundBox.insertPattern(ItemStack.EMPTY);
                Block.spawnAsEntity(worldIn, pos.up(), pattern);
            }

            return true;
        } else {
            ItemStack heldItem = player.getHeldItem(hand);

            if (heldItem.getItem() == ModItems.SOUND_PATTERN) {
                if (!worldIn.isRemote) {
                    ItemStack inserted = heldItem.copy();
                    inserted.setCount(1);
                    soundBox.insertPattern(inserted);

                    if (!player.abilities.isCreativeMode) {
                        heldItem.shrink(1);
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        TileEntity te = worldIn.getTileEntity(pos);

        if (te instanceof SoundBoxTileEntity) {
            boolean powered = worldIn.getRedstonePowerFromNeighbors(pos) > 0;
            ((SoundBoxTileEntity) te).updatePowerState(powered);
        }
    }
}
