package lumien.randomthings.block;

import lumien.randomthings.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

/**
 * Extends vanilla's CropsBlock, which already implements the age/growth/
 * bonemeal logic the 1.12.2 version hand-rolled. The one bit of custom
 * behavior kept is the original's "harvest without breaking": right-clicking
 * a fully grown sprout resets it to age 0 and drops beans, instead of
 * breaking it like a normal crop.
 */
public class BeanSproutBlock extends CropsBlock
{
	public BeanSproutBlock()
	{
		super(Block.Properties.create(Material.PLANTS).doesNotBlockMovement().tickRandomly().hardnessAndResistance(0.0F).sound(SoundType.PLANT));
	}

	@Override
	protected IItemProvider getSeedsItem()
	{
		return ModItems.BEANS;
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (this.isMaxAge(state))
		{
			if (!worldIn.isRemote)
			{
				worldIn.setBlockState(pos, this.withAge(0));

				ItemStack stack = new ItemStack(ModItems.BEANS, worldIn.rand.nextInt(2) + 1);

				if (!player.addItemStackToInventory(stack))
				{
					spawnAsEntity(worldIn, pos, stack);
				}
			}

			return true;
		}

		return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
	}
}
