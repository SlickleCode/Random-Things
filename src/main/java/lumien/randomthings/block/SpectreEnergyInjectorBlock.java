package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import lumien.randomthings.tileentity.SpectreEnergyInjectorTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

/**
 * Ported from 1.12.2's {@code BlockSpectreEnergyInjector} - a plain, non-directional block that exposes
 * the placing player's {@link lumien.randomthings.handler.spectrecoils.SpectreCoilHandler} pool for
 * external machines to push energy into (see {@link SpectreEnergyInjectorTileEntity}).
 */
public class SpectreEnergyInjectorBlock extends Block implements EntityBlock {
    public SpectreEnergyInjectorBlock() {
        super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(3.0F).sound(SoundType.GLASS));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new SpectreEnergyInjectorTileEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!worldIn.isClientSide && placer instanceof Player) {
            GameProfile profile = ((Player) placer).getGameProfile();

            if (profile != null) {
                UUID id = profile.getId();
                BlockEntity te = worldIn.getBlockEntity(pos);

                if (te instanceof SpectreEnergyInjectorTileEntity) {
                    ((SpectreEnergyInjectorTileEntity) te).setOwner(id);
                }
            }
        }
    }
}
