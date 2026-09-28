package lumien.randomthings.block.redstoneinterface;

import lumien.randomthings.tileentity.redstoneinterface.BasicRedstoneInterfaceTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Single-target half of the Redstone Interface family - see {@link
 * BasicRedstoneInterfaceTileEntity}/{@link
 * lumien.randomthings.tileentity.redstoneinterface.RedstoneInterfaceTileEntity}
 * for the actual wireless-power mechanic. Right-click with a Redstone Tool
 * to bind a target; right-click bare opens a read-only status GUI, matching
 * {@link lumien.randomthings.block.RedstoneObserverBlock}'s own convention.
 * Direct port of 1.12.2's {@code BlockBasicRedstoneInterface}.
 */
public class BasicRedstoneInterfaceBlock extends Block {
    public BasicRedstoneInterfaceBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(2.0F));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new BasicRedstoneInterfaceTileEntity();
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);

            if (te instanceof BasicRedstoneInterfaceTileEntity) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (BasicRedstoneInterfaceTileEntity) te);
            }
        }

        return true;
    }
}
