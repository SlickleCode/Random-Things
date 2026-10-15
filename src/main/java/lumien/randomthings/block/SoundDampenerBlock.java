package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.SoundDampenerTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraft.world.InteractionResult;

/**
 * A 9-slot filter of {@link lumien.randomthings.item.SoundPatternItem}s -
 * every sound stamped into one mutes for anyone within 20 blocks. Direct
 * port of 1.12.2's {@code BlockSoundDampener}.
 */
public class SoundDampenerBlock extends Block implements EntityBlock {
    public SoundDampenerBlock() {
        super(Block.Properties.of(Material.STONE).strength(2.0F));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new SoundDampenerTileEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity te = worldIn.getBlockEntity(pos);

            if (te instanceof SoundDampenerTileEntity) {
                ItemStackHandler handler = ((SoundDampenerTileEntity) te).getItemHandler();

                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);

                    if (!stack.isEmpty()) {
                        Block.popResource(worldIn, pos, stack);
                    }
                }
            }

            super.onRemove(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!worldIn.isClientSide) {
            BlockEntity te = worldIn.getBlockEntity(pos);

            if (te instanceof SoundDampenerTileEntity) {
                NetworkHooks.openScreen((ServerPlayer) player, (SoundDampenerTileEntity) te);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
