package lumien.randomthings.block;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import lumien.randomthings.tileentity.SpectreEnergyInjectorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * Ported from 1.12.2's {@code BlockSpectreEnergyInjector} - a plain, non-directional block that exposes
 * the placing player's {@link lumien.randomthings.handler.spectrecoils.SpectreCoilHandler} pool for
 * external machines to push energy into (see {@link SpectreEnergyInjectorTileEntity}).
 */
public class SpectreEnergyInjectorBlock extends Block {
    public SpectreEnergyInjectorBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(3.0F).sound(SoundType.GLASS));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SpectreEnergyInjectorTileEntity();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!worldIn.isRemote && placer instanceof PlayerEntity) {
            GameProfile profile = ((PlayerEntity) placer).getGameProfile();

            if (profile != null) {
                UUID id = profile.getId();
                TileEntity te = worldIn.getTileEntity(pos);

                if (te instanceof SpectreEnergyInjectorTileEntity) {
                    ((SpectreEnergyInjectorTileEntity) te).setOwner(id);
                }
            }
        }
    }
}
