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

		tests.add(RTTest.named("parity.spirit_spawn_egg.sets_a_spawner_and_spawns_from_a_dispenser").run(c -> {
			c.floor();
			net.minecraft.core.BlockPos stand = c.at(0, 1, 0);
			c.player.setPos(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5);
			net.minecraft.world.item.Item egg = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation("randomthings:spirit_spawn_egg"));
			net.minecraft.world.item.ItemStack eggStack = new net.minecraft.world.item.ItemStack(egg);

			// on a spawner: the spawner switches to the Spirit and the egg is used up
			net.minecraft.core.BlockPos spawnerPos = c.at(-4, 1, 4);
			c.set(spawnerPos, net.minecraft.world.level.block.Blocks.SPAWNER.defaultBlockState());
			c.player.stopUsingItem();
			c.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(egg));
			c.check(egg.useOn(new net.minecraft.world.item.context.UseOnContext(c.player, net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.phys.BlockHitResult(net.minecraft.world.phys.Vec3.atCenterOf(spawnerPos), net.minecraft.core.Direction.UP, spawnerPos, false))).consumesAction(), "using the egg on a spawner");
			c.check(c.tile(spawnerPos, net.minecraft.world.level.block.entity.SpawnerBlockEntity.class).getUpdateTag().toString().contains("randomthings:spirit"), "the spawner now spawns Spirits");
			c.remove(spawnerPos);	// a live spawner would add spirits of its own to the count below
			c.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);

			// from a dispenser: the egg is shot out and a Spirit appears in front of it
			net.minecraft.core.BlockPos dispenserPos = c.at(4, 2, 4);
			c.set(dispenserPos, net.minecraft.world.level.block.Blocks.DISPENSER.defaultBlockState().setValue(net.minecraft.world.level.block.DispenserBlock.FACING, net.minecraft.core.Direction.EAST));
			c.tile(dispenserPos, net.minecraft.world.level.block.entity.DispenserBlockEntity.class).setItem(0, eggStack);
			c.eq(0, c.world.getEntitiesOfClass(lumien.randomthings.entity.SpiritEntity.class, c.area().inflate(8)).size(), "no spirit before");
			net.minecraft.world.level.block.state.BlockState dispenserState = c.world.getBlockState(dispenserPos);
			((net.minecraft.world.level.block.DispenserBlock) dispenserState.getBlock()).tick(dispenserState, c.world, dispenserPos, c.world.random);
			c.eq(1, c.world.getEntitiesOfClass(lumien.randomthings.entity.SpiritEntity.class, c.area().inflate(8)).size(), "the dispenser spawned a spirit");

			for (lumien.randomthings.entity.SpiritEntity spirit : c.world.getEntitiesOfClass(lumien.randomthings.entity.SpiritEntity.class, c.area().inflate(8)))
			{
				spirit.discard();
			}

			net.minecraft.core.BlockPos away = c.at(40, 1, 40);	// the player must not stand in the test area: later tests count drops there
			c.player.setPos(away.getX() + 0.5, away.getY(), away.getZ() + 0.5);
		}));

		tests.add(RTTest.named("parity.spirit.moon_bonus_applies_in_the_end").run(c -> {
			net.minecraft.world.level.Level end = c.world.getServer().getLevel(net.minecraft.world.level.Level.END);
			c.check(end != null, "the End is loaded");
			c.check(lumien.randomthings.RandomThings.spiritMoonTime(end), "the Spirit moon bonus applies in the End (fixed time), on every version");
		}));

		tests.add(RTTest.named("parity.spectre_rock_blocks.need_a_pickaxe_to_drop").run(c -> {
			c.floor();
			net.minecraft.world.level.block.Block[] blocks = { lumien.randomthings.block.ModBlocks.SPECTRE_ENERGY_INJECTOR, lumien.randomthings.block.ModBlocks.SPECTRE_COIL_NORMAL, lumien.randomthings.block.ModBlocks.SPECTRE_COIL_REDSTONE, lumien.randomthings.block.ModBlocks.SPECTRE_COIL_ENDER, lumien.randomthings.block.ModBlocks.SPECTRE_COIL_NUMBER, lumien.randomthings.block.ModBlocks.SPECTRE_COIL_GENESIS };
			net.minecraft.core.BlockPos p = c.at(0, 1, 0);

			for (net.minecraft.world.level.block.Block block : blocks)
			{
				c.set(p, block.defaultBlockState());
				net.minecraft.world.level.block.state.BlockState state = c.world.getBlockState(p);
				c.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
				c.check(!c.player.hasCorrectToolForDrops(state), block + ": mined by hand it drops nothing (1.12.2: Material.ROCK needs a pickaxe)");
				c.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.WOODEN_PICKAXE));
				c.check(c.player.hasCorrectToolForDrops(state), block + ": any pickaxe makes it drop");
			}

			c.player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, net.minecraft.world.item.ItemStack.EMPTY);
			c.remove(p);
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
