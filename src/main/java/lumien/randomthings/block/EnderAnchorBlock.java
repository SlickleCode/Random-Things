package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import java.util.List;

import javax.annotation.Nullable;

import lumien.randomthings.config.RTConfig;
import lumien.randomthings.handler.AnchorChunkLoader;
import lumien.randomthings.tileentity.EnderAnchorTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Direct port of 1.12.2's {@code BlockEnderAnchor}. Like its {@code breakBlock},
 * {@link #onReplaced} releases the anchor's chunk-loading (see
 * {@link AnchorChunkLoader}).
 */
public class EnderAnchorBlock extends Block implements EntityBlock {
    public EnderAnchorBlock() {
        super(Block.Properties.of(Material.STONE).strength(1.5F));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new EnderAnchorTileEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return Tickers.of(type, ModTileEntityTypes.ENDER_ANCHOR, (l, p, s2, be) -> be.tick());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if (RTConfig.ENDER_ANCHOR_CHUNKLOADING.get()) {
            tooltip.add(Component.translatable("tooltip.randomthings.ender_anchor.chunkloading"));
        }
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock() && worldIn instanceof ServerLevel) {
            AnchorChunkLoader.unregister((ServerLevel) worldIn, pos);
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
}
