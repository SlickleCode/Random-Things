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

	// ---- Spirit spawn egg (museum report 2026-10-24) ---------------------------------------------------
	static void addMuseumTests(List<RTTest> tests)
	{
		tests.add(RTTest.named("retest.spirit_spawn_egg.using_it_on_the_ground_spawns_a_spirit").run(c -> {
			c.floor();
			net.minecraft.core.BlockPos stand = c.at(0, 1, 0);
			c.player.setPos(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5);
			net.minecraft.world.item.Item egg = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation("randomthings:spirit_spawn_egg"));
			c.check(egg != net.minecraft.world.item.Items.AIR, "the item is registered");
			net.minecraft.world.item.ItemStack held = new net.minecraft.world.item.ItemStack(egg);
			c.player.stopUsingItem();
			c.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, held);
			c.eq(0, c.world.getEntitiesOfClass(lumien.randomthings.entity.SpiritEntity.class, c.area().inflate(8)).size(), "no spirit before");
			net.minecraft.core.BlockPos clicked = c.at(2, 0, 0);
			c.check(egg.useOn(new net.minecraft.world.item.context.UseOnContext(c.player, net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(clicked), net.minecraft.core.Direction.UP, clicked, false))).consumesAction(), "using the egg");
			c.eq(1, c.world.getEntitiesOfClass(lumien.randomthings.entity.SpiritEntity.class, c.area().inflate(8)).size(), "one spirit after");

			for (lumien.randomthings.entity.SpiritEntity spirit : c.world.getEntitiesOfClass(lumien.randomthings.entity.SpiritEntity.class, c.area().inflate(8)))
			{
				spirit.discard();
			}

			c.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
			net.minecraft.core.BlockPos away = c.at(40, 1, 40);	// the player must not stand in the test area: later tests count drops there
			c.player.setPos(away.getX() + 0.5, away.getY(), away.getZ() + 0.5);
		}));
	}

	static List<RTTest> create()
	{
		List<RTTest> tests = new ArrayList<>();
		addMuseumTests(tests);

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
			silk.enchant(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH, 1);
			java.util.List<net.minecraft.world.item.ItemStack> silked = net.minecraft.world.level.block.Block.getDrops(red, c.world, pos, null, c.player, silk);
			c.check(silked.size() == 1 && silked.get(0).getItem() == lumien.randomthings.block.ModBlocks.COLORED_GRASS.asItem(), "with silk touch: itself, got " + silked);
		}));

		return tests;
	}
}
