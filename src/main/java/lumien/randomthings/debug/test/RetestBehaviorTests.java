package lumien.randomthings.debug.test;

import java.util.ArrayList;
import java.util.List;

import lumien.randomthings.entity.ModEntityTypes;

/**
 * Regression tests for bugs found by the 1.21.4 retest of the checklist (golden chicken ingot count, colored grass silk touch loot, summoning pendulum release).
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
			net.minecraft.core.BlockPos p = c.at(0, 1, 0);
			chicken.setPos(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
			chicken.setNoAi(true);
			c.world.addFreshEntity(chicken);
		}).waitTicks(3).run(c -> {
			lumien.randomthings.entity.GoldenChickenEntity chicken = c.world.getEntitiesOfClass(lumien.randomthings.entity.GoldenChickenEntity.class, c.area()).get(0);
			chicken.ingotDropTimer = 2;
		}).waitTicks(6).run(c -> {
			int ingots = 0;

			for (net.minecraft.world.entity.item.ItemEntity item : c.itemEntities())
			{
				if (item.getItem().getItem() == net.minecraft.world.item.Items.GOLD_INGOT)
				{
					ingots += item.getItem().getCount();
				}
			}

			c.eq(3, ingots, "three gold ingots dropped");
		}));

		tests.add(RTTest.named("retest.loot.colored_grass_drops_dirt_and_only_itself_with_silk_touch").run(c -> {
			c.floor();
			net.minecraft.core.BlockPos pos = c.at(0, 1, 0);
			net.minecraft.world.level.block.state.BlockState red = lumien.randomthings.block.ModBlocks.COLORED_GRASS.defaultBlockState().setValue(lumien.randomthings.block.ColoredGrassBlock.COLOR, net.minecraft.world.item.DyeColor.RED);
			c.setQuiet(pos, red);
			java.util.List<net.minecraft.world.item.ItemStack> plain = net.minecraft.world.level.block.Block.getDrops(red, c.world, pos, null, c.player, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SHOVEL));
			c.check(plain.size() == 1 && plain.get(0).getItem() == net.minecraft.world.item.Items.DIRT, "without silk touch: plain dirt, got " + plain);

			net.minecraft.world.item.ItemStack silk = new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SHOVEL);
			silk.enchant(c.world.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
			java.util.List<net.minecraft.world.item.ItemStack> silked = net.minecraft.world.level.block.Block.getDrops(red, c.world, pos, null, c.player, silk);
			c.check(silked.size() == 1 && silked.get(0).getItem() == lumien.randomthings.block.ModBlocks.COLORED_GRASS.asItem(), "with silk touch: itself, got " + silked);
		}));

		tests.add(RTTest.named("retest.summoning_pendulum.releasing_a_mob_removes_it_from_the_stack").run(c -> {
			c.floor();
			net.minecraft.world.item.ItemStack pendulum = new net.minecraft.world.item.ItemStack(lumien.randomthings.item.ModItems.SUMMONING_PENDULUM);
			c.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, pendulum);
			net.minecraft.world.entity.animal.Cow first = net.minecraft.world.entity.EntityType.COW.create(c.world);
			first.setPos(c.at(-2, 1, -3).getX() + 0.5, c.at(-2, 1, -3).getY(), c.at(-2, 1, -3).getZ() + 0.5);
			c.world.addFreshEntity(first);
			net.minecraft.world.entity.animal.Cow second = net.minecraft.world.entity.EntityType.COW.create(c.world);
			second.setPos(c.at(2, 1, -3).getX() + 0.5, c.at(2, 1, -3).getY(), c.at(2, 1, -3).getZ() + 0.5);
			c.world.addFreshEntity(second);
			pendulum.getItem().interactLivingEntity(pendulum, c.player, first, net.minecraft.world.InteractionHand.MAIN_HAND);
			pendulum.getItem().interactLivingEntity(pendulum, c.player, second, net.minecraft.world.InteractionHand.MAIN_HAND);
			c.eq(2, lumien.randomthings.util.StackTag.get(c.player.getMainHandItem()).getList("entitys", 10).size(), "captured");

			net.minecraft.core.BlockPos floor = c.at(0, 0, 0);
			c.player.getMainHandItem().getItem().useOn(new net.minecraft.world.item.context.UseOnContext(c.player, net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(floor), net.minecraft.core.Direction.UP, floor, false)));
			c.eq(1, lumien.randomthings.util.StackTag.get(c.player.getMainHandItem()).getList("entitys", 10).size(), "one left in the pendulum after releasing one");
			c.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
		}));

		return tests;
	}
}
