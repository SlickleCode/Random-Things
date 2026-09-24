package lumien.randomthings.block.plates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class CollectionPlateBlock extends PlateBlock
{
	public CollectionPlateBlock()
	{
		super(Block.Properties.create(Material.EARTH, MaterialColor.STONE).hardnessAndResistance(0.3F).sound(SoundType.STONE));
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
	{
		super.onEntityCollision(state, worldIn, pos, entityIn);

		if (!worldIn.isRemote && entityIn instanceof ItemEntity)
		{
			ItemStack stack = ((ItemEntity) entityIn).getItem();

			for (Direction facing : Direction.Plane.HORIZONTAL)
			{
				TileEntity te = worldIn.getTileEntity(pos.offset(facing));

				if (te != null && te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite()).isPresent())
				{
					IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite()).orElse(null);

					if (itemHandler != null)
					{
						ItemStack remains = ItemHandlerHelper.insertItemStacked(itemHandler, stack, false);

						((ItemEntity) entityIn).setItem(remains);

						if (remains.isEmpty())
						{
							entityIn.remove();
						}

						break;
					}
				}
			}
		}
	}
}
