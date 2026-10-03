package lumien.randomthings.block;

import java.util.ArrayList;
import java.util.List;

import lumien.randomthings.config.RTConfig;
import lumien.randomthings.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraft.util.BlockRenderLayer;

/**
 * Caps a fully-grown magic bean stalk. Drops come from the
 * {@code loot_tables/blocks/bean_pod.json} table (port of 1.12.2's
 * "beanpod.json" - iron, gold, diamond, emerald, beans) plus a Golden Egg
 * added here in code, gated by the {@code GoldenEgg} config option like
 * 1.12.2's {@code Features.GOLDEN_EGG}. 1.12.2 applied the explosion
 * survival chance to every stack including the egg; here the table's
 * {@code survives_explosion} conditions cover the pools and the egg rolls
 * the same {@code 1 / radius} chance itself.
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
		List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));

		if (RTConfig.GOLDEN_EGG.get())
		{
			Float radius = builder.get(LootParameters.EXPLOSION_RADIUS);

			if (radius == null || builder.getWorld().rand.nextFloat() <= 1.0F / radius)
			{
				drops.add(new ItemStack(ModItems.GOLDEN_EGG));
			}
		}

		return drops;
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}
}
