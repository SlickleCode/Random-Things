package lumien.randomthings.block;

import java.util.ArrayList;
import java.util.List;

import lumien.randomthings.config.RTConfig;
import lumien.randomthings.item.ModItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

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
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.PLANT).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).noOcclusion().strength(1.5F).noCollission());
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, net.minecraft.world.level.storage.loot.LootParams.Builder builder)
	{
		List<ItemStack> drops = new ArrayList<>(super.getDrops(state, builder));

		if (RTConfig.GOLDEN_EGG.get())
		{
			Float radius = builder.getOptionalParameter(LootContextParams.EXPLOSION_RADIUS);

			if (radius == null || builder.getLevel().random.nextFloat() <= 1.0F / radius)
			{
				drops.add(new ItemStack(ModItems.GOLDEN_EGG));
			}
		}

		return drops;
	}
}
