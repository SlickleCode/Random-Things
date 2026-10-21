package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.NotificationInterfaceTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;

/**
 * Links to whoever placed it and pops a toast on their screen whenever it
 * sees a rising redstone edge.
 */
public class NotificationInterfaceBlock extends Block implements EntityBlock {
	public NotificationInterfaceBlock()
	{
		super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(2.0F));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
	    return new NotificationInterfaceTileEntity(pos, state);
	}

	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if (!worldIn.isClientSide && placer instanceof ServerPlayer)
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof NotificationInterfaceTileEntity)
			{
				((NotificationInterfaceTileEntity) te).setPlayerUUID(placer.getUUID());
			}
		}
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
	{
		BlockEntity te = worldIn.getBlockEntity(pos);

		if (te instanceof NotificationInterfaceTileEntity)
		{
			((NotificationInterfaceTileEntity) te).updatePowerState(worldIn.hasNeighborSignal(pos));
		}
	}

	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof NotificationInterfaceTileEntity)
			{
				NonNullList<ItemStack> drops = NonNullList.create();
				drops.add(((NotificationInterfaceTileEntity) te).iconInventory().getStackInSlot(0));
				Containers.dropContents(worldIn, pos, drops);
			}
		}

		super.onRemove(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
		if (!worldIn.isClientSide)
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof NotificationInterfaceTileEntity)
			{
				NotificationInterfaceTileEntity nite = (NotificationInterfaceTileEntity) te;

				((ServerPlayer) player).openMenu(nite, buf -> {
					buf.writeBlockPos(pos);
					buf.writeUtf(nite.getTitle());
					buf.writeUtf(nite.getDescription());
				});
			}
		}

		return InteractionResult.SUCCESS;
	}
}
