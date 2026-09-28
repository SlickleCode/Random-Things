package lumien.randomthings.block.redstoneinterface;

import lumien.randomthings.tileentity.redstoneinterface.AdvancedRedstoneInterfaceTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
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
 * Multi-target half of the Redstone Interface family - see {@link
 * AdvancedRedstoneInterfaceTileEntity} for the actual wireless-power
 * mechanic. Direct port of 1.12.2's {@code BlockAdvancedRedstoneInterface}.
 */
public class AdvancedRedstoneInterfaceBlock extends Block {
    public AdvancedRedstoneInterfaceBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(2.0F));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new AdvancedRedstoneInterfaceTileEntity();
    }

    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity te = worldIn.getTileEntity(pos);

            if (te instanceof AdvancedRedstoneInterfaceTileEntity) {
                ItemStackHandler inventory = ((AdvancedRedstoneInterfaceTileEntity) te).getPositionInventory();

                for (int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack stack = inventory.getStackInSlot(i);

                    if (!stack.isEmpty()) {
                        InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack);
                    }
                }
            }
        }

        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);

            if (te instanceof AdvancedRedstoneInterfaceTileEntity) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (AdvancedRedstoneInterfaceTileEntity) te);
            }
        }

        return true;
    }
}
