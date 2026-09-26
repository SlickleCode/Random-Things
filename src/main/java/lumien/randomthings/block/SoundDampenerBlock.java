package lumien.randomthings.block;

import lumien.randomthings.tileentity.SoundDampenerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.ItemStackHandler;

/**
 * A 9-slot filter of {@link lumien.randomthings.item.SoundPatternItem}s -
 * every sound stamped into one mutes for anyone within 20 blocks. Direct
 * port of 1.12.2's {@code BlockSoundDampener}.
 */
public class SoundDampenerBlock extends Block {
    public SoundDampenerBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(2.0F));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SoundDampenerTileEntity();
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity te = worldIn.getTileEntity(pos);

            if (te instanceof SoundDampenerTileEntity) {
                ItemStackHandler handler = ((SoundDampenerTileEntity) te).getItemHandler();

                for (int i = 0; i < handler.getSlots(); i++) {
                    ItemStack stack = handler.getStackInSlot(i);

                    if (!stack.isEmpty()) {
                        Block.spawnAsEntity(worldIn, pos, stack);
                    }
                }
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);

            if (te instanceof SoundDampenerTileEntity) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (SoundDampenerTileEntity) te);
            }
        }

        return true;
    }
}
