package lumien.randomthings.block.redstoneinterface;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.redstoneinterface.AdvancedRedstoneInterfaceTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
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
 * Multi-target half of the Redstone Interface family - see {@link
 * AdvancedRedstoneInterfaceTileEntity} for the actual wireless-power
 * mechanic. Direct port of 1.12.2's {@code BlockAdvancedRedstoneInterface}.
 */
public class AdvancedRedstoneInterfaceBlock extends Block implements EntityBlock {
    public AdvancedRedstoneInterfaceBlock() {
        super(Block.Properties.of(Material.STONE).strength(2.0F));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new AdvancedRedstoneInterfaceTileEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity te = worldIn.getBlockEntity(pos);

            if (te instanceof AdvancedRedstoneInterfaceTileEntity) {
                ItemStackHandler inventory = ((AdvancedRedstoneInterfaceTileEntity) te).getPositionInventory();

                for (int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack stack = inventory.getStackInSlot(i);

                    if (!stack.isEmpty()) {
                        Containers.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
            }
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        BlockEntity te = worldIn.getBlockEntity(pos);

        if (te instanceof lumien.randomthings.tileentity.redstoneinterface.RedstoneInterfaceTileEntity) {
            ((lumien.randomthings.tileentity.redstoneinterface.RedstoneInterfaceTileEntity) te).updateRedstoneState();
        }
    }

    @Override
    public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
        BlockEntity te = worldIn.getBlockEntity(pos);

        if (te instanceof lumien.randomthings.tileentity.redstoneinterface.RedstoneInterfaceTileEntity) {
            ((lumien.randomthings.tileentity.redstoneinterface.RedstoneInterfaceTileEntity) te).updateRedstoneState();
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (!worldIn.isClientSide) {
            BlockEntity te = worldIn.getBlockEntity(pos);

            if (te instanceof AdvancedRedstoneInterfaceTileEntity) {
                NetworkHooks.openScreen((ServerPlayer) player, (AdvancedRedstoneInterfaceTileEntity) te);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
