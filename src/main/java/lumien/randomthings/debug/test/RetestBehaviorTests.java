package lumien.randomthings.debug.test;

import java.util.ArrayList;
import java.util.List;

import lumien.randomthings.entity.ModEntityTypes;

/**
 * Regression tests for bugs found by the 1.21.4 retest of the checklist (golden chicken ingot count, colored grass silk touch loot).
 */
final class RetestBehaviorTests
{
	private RetestBehaviorTests()
	{
	}

	static List<RTTest> create()
	{
		List<RTTest> tests = new ArrayList<>();

		tests.add(RTTest.named("retest.golden_chicken.drops_three_gold_ingots_when_the_timer_runs_out").run(c -> {
			c.floor();
			lumien.randomthings.entity.GoldenChickenEntity chicken = ModEntityTypes.GOLDEN_CHICKEN.create(c.world);
			net.minecraft.util.math.BlockPos p = c.at(0, 1, 0);
			chicken.setPosition(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
			chicken.setNoAI(true);
			c.world.addEntity(chicken);
		}).waitTicks(3).run(c -> {
			lumien.randomthings.entity.GoldenChickenEntity chicken = c.world.getEntitiesWithinAABB(lumien.randomthings.entity.GoldenChickenEntity.class, c.area()).get(0);
			chicken.ingotDropTimer = 2;
		}).waitTicks(6).run(c -> {
			int ingots = 0;

			for (net.minecraft.entity.item.ItemEntity item : c.itemEntities())
			{
				if (item.getItem().getItem() == net.minecraft.item.Items.GOLD_INGOT)
				{
					ingots += item.getItem().getCount();
				}
			}

			c.eq(3, ingots, "three gold ingots dropped");
		}));

		tests.add(RTTest.named("retest.loot.colored_grass_drops_dirt_and_only_itself_with_silk_touch").run(c -> {
			c.floor();
			net.minecraft.util.math.BlockPos pos = c.at(0, 1, 0);
			net.minecraft.block.BlockState red = lumien.randomthings.block.ModBlocks.COLORED_GRASS.getDefaultState().with(lumien.randomthings.block.ColoredGrassBlock.COLOR, net.minecraft.item.DyeColor.RED);
			c.setQuiet(pos, red);
			java.util.List<net.minecraft.item.ItemStack> plain = net.minecraft.block.Block.getDrops(red, c.world, pos, null, c.player, new net.minecraft.item.ItemStack(net.minecraft.item.Items.DIAMOND_SHOVEL));
			c.check(plain.size() == 1 && plain.get(0).getItem() == net.minecraft.item.Items.DIRT, "without silk touch: plain dirt, got " + plain);

			net.minecraft.item.ItemStack silk = new net.minecraft.item.ItemStack(net.minecraft.item.Items.DIAMOND_SHOVEL);
			silk.addEnchantment(net.minecraft.enchantment.Enchantments.SILK_TOUCH, 1);
			java.util.List<net.minecraft.item.ItemStack> silked = net.minecraft.block.Block.getDrops(red, c.world, pos, null, c.player, silk);
			c.check(silked.size() == 1 && silked.get(0).getItem() == lumien.randomthings.block.ModBlocks.COLORED_GRASS.asItem(), "with silk touch: itself, got " + silked);
		}));

		return tests;
	}
}
