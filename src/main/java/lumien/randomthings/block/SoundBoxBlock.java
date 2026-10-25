package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.tileentity.SoundBoxTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;

/**
 * Insert a {@link lumien.randomthings.item.SoundPatternItem} and power it
 * with redstone to play that sound. Direct port of 1.12.2's
 * {@code BlockSoundBox}.
 */
public class SoundBoxBlock extends Block implements EntityBlock {
    public static final BooleanProperty HAS_PATTERN = BooleanProperty.create("has_pattern");

    public SoundBoxBlock() {
        super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.WOOD).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS).ignitedByLava().sound(SoundType.WOOD).strength(0.8F));

        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_PATTERN, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(HAS_PATTERN);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new SoundBoxTileEntity(pos, state);
    }

    public void rtOnRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity te = worldIn.getBlockEntity(pos);

            if (te instanceof SoundBoxTileEntity) {
                ItemStack pattern = ((SoundBoxTileEntity) te).getPattern();

                if (!pattern.isEmpty()) {
                    Block.popResource(worldIn, pos, pattern);
                }
            }

        }
    }

    @Override
    public net.minecraft.world.InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = legacyUse(state, worldIn, pos, player, hand, hit);
        if (result == InteractionResult.SUCCESS) {
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        if (result == InteractionResult.CONSUME) {
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        if (result == InteractionResult.FAIL) {
            return net.minecraft.world.InteractionResult.FAIL;
        }
        return net.minecraft.world.InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    private InteractionResult legacyUse(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockEntity te = worldIn.getBlockEntity(pos);

        if (!(te instanceof SoundBoxTileEntity)) {
            return InteractionResult.PASS;
        }

        SoundBoxTileEntity soundBox = (SoundBoxTileEntity) te;

        if (soundBox.hasPattern()) {
            if (!worldIn.isClientSide()) {
                ItemStack pattern = soundBox.getPattern();
                soundBox.insertPattern(ItemStack.EMPTY);
                Block.popResource(worldIn, pos.above(), pattern);
            }

            return InteractionResult.SUCCESS;
        } else {
            ItemStack heldItem = player.getItemInHand(hand);

            if (heldItem.getItem() == ModItems.SOUND_PATTERN) {
                if (!worldIn.isClientSide()) {
                    ItemStack inserted = heldItem.copy();
                    inserted.setCount(1);
                    soundBox.insertPattern(inserted);

                    if (!player.getAbilities().instabuild) {
                        heldItem.shrink(1);
                    }
                }

                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, net.minecraft.world.level.redstone.Orientation fromPos, boolean isMoving) {
        BlockEntity te = worldIn.getBlockEntity(pos);

        if (te instanceof SoundBoxTileEntity) {
            boolean powered = worldIn.getBestNeighborSignal(pos) > 0;
            ((SoundBoxTileEntity) te).updatePowerState(powered);
        }
    }
}
