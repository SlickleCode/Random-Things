package lumien.randomthings.block.redstoneinterface;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.redstoneinterface.BasicRedstoneInterfaceTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.InteractionResult;

/**
 * Single-target half of the Redstone Interface family - see {@link
 * BasicRedstoneInterfaceTileEntity}/{@link
 * lumien.randomthings.tileentity.redstoneinterface.RedstoneInterfaceTileEntity}
 * for the actual wireless-power mechanic. Right-click with a Redstone Tool
 * to bind a target; right-click bare opens a read-only status GUI, matching
 * {@link lumien.randomthings.block.RedstoneObserverBlock}'s own convention.
 * Direct port of 1.12.2's {@code BlockBasicRedstoneInterface}.
 */
public class BasicRedstoneInterfaceBlock extends Block implements EntityBlock {
    public BasicRedstoneInterfaceBlock() {
        super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(2.0F));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new BasicRedstoneInterfaceTileEntity(pos, state);
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
        if (player.getItemInHand(handIn).getItem() instanceof lumien.randomthings.item.RedstoneToolItem) {
            return InteractionResult.PASS;
        }

        if (!worldIn.isClientSide) {
            BlockEntity te = worldIn.getBlockEntity(pos);

            if (te instanceof BasicRedstoneInterfaceTileEntity) {
                NetworkHooks.openScreen((ServerPlayer) player, (BasicRedstoneInterfaceTileEntity) te);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
