package lumien.randomthings.block;

import lumien.randomthings.tileentity.ImbuingStationTileEntity;
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
 * Direct port of 1.12.2's {@code BlockImbuingStation}: a static (non-
 * directional) machine block, matching the original's lack of any facing
 * property.
 */
public class ImbuingStationBlock extends Block {
    public ImbuingStationBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.25F));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new ImbuingStationTileEntity();
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            TileEntity te = worldIn.getTileEntity(pos);

            if (te instanceof ImbuingStationTileEntity) {
                NetworkHooks.openGui((ServerPlayerEntity) player, (ImbuingStationTileEntity) te);
            }
        }

        return true;
    }
}
