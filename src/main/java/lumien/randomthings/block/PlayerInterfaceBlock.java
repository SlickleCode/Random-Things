package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.PlayerInterfaceTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

/**
 * Links itself to whoever places it and exposes their inventory as a
 * capability, so hoppers etc. can pull from / push into that player directly.
 */
public class PlayerInterfaceBlock extends Block implements EntityBlock {
	public PlayerInterfaceBlock()
	{
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(4.0F).sound(SoundType.STONE));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
	    return new PlayerInterfaceTileEntity(pos, state);
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if (!worldIn.isClientSide && placer instanceof ServerPlayer)
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof PlayerInterfaceTileEntity)
			{
				((PlayerInterfaceTileEntity) te).setPlayerUUID(placer.getUUID());
			}
		}
	}
}
