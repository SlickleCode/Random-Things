package lumien.randomthings.block;

import java.util.List;

import javax.annotation.Nullable;

import lumien.randomthings.config.RTConfig;
import lumien.randomthings.handler.AnchorChunkLoader;
import lumien.randomthings.tileentity.EnderAnchorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Direct port of 1.12.2's {@code BlockEnderAnchor}. Like its {@code breakBlock},
 * {@link #onReplaced} releases the anchor's chunk-loading (see
 * {@link AnchorChunkLoader}).
 */
public class EnderAnchorBlock extends Block {
    public EnderAnchorBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5F));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new EnderAnchorTileEntity();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        if (RTConfig.ENDER_ANCHOR_CHUNKLOADING.get()) {
            tooltip.add(new TranslationTextComponent("tooltip.randomthings.ender_anchor.chunkloading"));
        }
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock() && worldIn instanceof ServerWorld) {
            AnchorChunkLoader.unregister((ServerWorld) worldIn, pos);
        }

        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }
}
