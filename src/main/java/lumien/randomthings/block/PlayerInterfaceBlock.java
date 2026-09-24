package lumien.randomthings.block;

import lumien.randomthings.tileentity.PlayerInterfaceTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * Links itself to whoever places it and exposes their inventory as a
 * capability, so hoppers etc. can pull from / push into that player directly.
 */
public class PlayerInterfaceBlock extends Block
{
	public PlayerInterfaceBlock()
	{
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(4.0F).sound(SoundType.STONE));
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new PlayerInterfaceTileEntity();
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		if (!worldIn.isRemote && placer instanceof ServerPlayerEntity)
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof PlayerInterfaceTileEntity)
			{
				((PlayerInterfaceTileEntity) te).setPlayerUUID(placer.getUniqueID());
			}
		}
	}
}
