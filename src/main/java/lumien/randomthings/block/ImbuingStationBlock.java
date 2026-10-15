package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import lumien.randomthings.tileentity.ImbuingStationTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
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
 * Direct port of 1.12.2's {@code BlockImbuingStation}: a static (non-
 * directional) machine block, matching the original's lack of any facing
 * property.
 */
public class ImbuingStationBlock extends Block implements EntityBlock {
    public ImbuingStationBlock() {
        super(Block.Properties.of(Material.STONE).strength(1.25F));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new ImbuingStationTileEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return Tickers.of(type, ModTileEntityTypes.IMBUING_STATION, (l, p, s2, be) -> be.tick());
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (!worldIn.isClientSide) {
            BlockEntity te = worldIn.getBlockEntity(pos);

            if (te instanceof ImbuingStationTileEntity) {
                NetworkHooks.openScreen((ServerPlayer) player, (ImbuingStationTileEntity) te);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
