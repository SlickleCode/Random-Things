package lumien.randomthings.block;

import java.util.Collections;
import java.util.List;

import lumien.randomthings.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.util.BlockRenderLayer;

/**
 * Caps a fully-grown magic bean stalk. Drops beans directly via a
 * {@code getDrops} override instead of the 1.12.2 loot table JSON
 * ("beanpod.json") - simpler for a single fixed drop and avoids needing to
 * port the loot table system this early.
 */
public class PodBlock extends Block
{
	public PodBlock()
	{
		super(Block.Properties.create(Material.PLANTS).hardnessAndResistance(1.5F).doesNotBlockMovement());
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
	{
		int count = 2 + builder.getWorld().rand.nextInt(3);
		return Collections.singletonList(new ItemStack(ModItems.BEANS, count));
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}
}
