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
			net.minecraft.util.math.BlockPos stand = c.at(0, 1, 0);
			c.player.setPosition(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5);
			net.minecraft.item.Item egg = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.util.ResourceLocation("randomthings:spirit_spawn_egg"));
			c.check(egg != null && egg != net.minecraft.item.Items.AIR, "the item is registered");
			net.minecraft.item.ItemStack held = new net.minecraft.item.ItemStack(egg);
			c.player.stopActiveHand();
			c.player.setHeldItem(net.minecraft.util.Hand.MAIN_HAND, held);
			c.eq(0, c.world.getEntitiesWithinAABB(lumien.randomthings.entity.SpiritEntity.class, c.area().grow(8)).size(), "no spirit before");
			net.minecraft.util.math.BlockPos clicked = c.at(2, 0, 0);
			c.check((egg.onItemUse(new net.minecraft.item.ItemUseContext(c.player, net.minecraft.util.Hand.MAIN_HAND, new net.minecraft.util.math.BlockRayTraceResult(new net.minecraft.util.math.Vec3d(clicked.getX() + 0.5, clicked.getY() + 0.5, clicked.getZ() + 0.5), net.minecraft.util.Direction.UP, clicked, false)))) == net.minecraft.util.ActionResultType.SUCCESS, "using the egg");
			c.eq(1, c.world.getEntitiesWithinAABB(lumien.randomthings.entity.SpiritEntity.class, c.area().grow(8)).size(), "one spirit after");

			for (lumien.randomthings.entity.SpiritEntity spirit : c.world.getEntitiesWithinAABB(lumien.randomthings.entity.SpiritEntity.class, c.area().grow(8)))
			{
				spirit.remove();
			}

			c.player.setHeldItem(net.minecraft.util.Hand.MAIN_HAND, net.minecraft.item.ItemStack.EMPTY);
			net.minecraft.util.math.BlockPos away = c.at(40, 1, 40);	// the player must not stand in the test area: later tests count drops there
			c.player.setPosition(away.getX() + 0.5, away.getY(), away.getZ() + 0.5);
		}));

		tests.add(RTTest.named("parity.spirit_spawn_egg.sets_a_spawner_and_spawns_from_a_dispenser").run(c -> {
			c.floor();
			net.minecraft.util.math.BlockPos stand = c.at(0, 1, 0);
			c.player.setPosition(stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5);
			net.minecraft.item.Item egg = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.util.ResourceLocation("randomthings:spirit_spawn_egg"));
			net.minecraft.item.ItemStack eggStack = new net.minecraft.item.ItemStack(egg);

			// on a spawner: the spawner switches to the Spirit and the egg is used up
			net.minecraft.util.math.BlockPos spawnerPos = c.at(-4, 1, 4);
			c.set(spawnerPos, net.minecraft.block.Blocks.SPAWNER.getDefaultState());
			c.player.stopActiveHand();
			c.player.setHeldItem(net.minecraft.util.Hand.MAIN_HAND, new net.minecraft.item.ItemStack(egg));
			c.check((egg.onItemUse(new net.minecraft.item.ItemUseContext(c.player, net.minecraft.util.Hand.MAIN_HAND, new net.minecraft.util.math.BlockRayTraceResult(new net.minecraft.util.math.Vec3d(spawnerPos.getX() + 0.5, spawnerPos.getY() + 0.5, spawnerPos.getZ() + 0.5), net.minecraft.util.Direction.UP, spawnerPos, false)))) == net.minecraft.util.ActionResultType.SUCCESS, "using the egg on a spawner");
			c.check(c.tile(spawnerPos, net.minecraft.tileentity.MobSpawnerTileEntity.class).getUpdateTag().toString().contains("randomthings:spirit"), "the spawner now spawns Spirits");
			c.remove(spawnerPos);	// a live spawner would add spirits of its own to the count below
			c.player.setHeldItem(net.minecraft.util.Hand.MAIN_HAND, net.minecraft.item.ItemStack.EMPTY);

			// from a dispenser: the egg is shot out and a Spirit appears in front of it
			net.minecraft.util.math.BlockPos dispenserPos = c.at(4, 2, 4);
			c.set(dispenserPos, net.minecraft.block.Blocks.DISPENSER.getDefaultState().with(net.minecraft.block.DispenserBlock.FACING, net.minecraft.util.Direction.EAST));
			c.tile(dispenserPos, net.minecraft.tileentity.DispenserTileEntity.class).setInventorySlotContents(0, eggStack);
			int spiritsBefore = c.world.getEntitiesWithinAABB(lumien.randomthings.entity.SpiritEntity.class, c.area().grow(8)).size();	// a spirit removed by the previous test can still be listed this tick
			net.minecraft.block.BlockState dispenserState = c.world.getBlockState(dispenserPos);
			((net.minecraft.block.DispenserBlock) dispenserState.getBlock()).tick(dispenserState, c.world, dispenserPos, c.world.rand);
			c.eq(spiritsBefore + 1, c.world.getEntitiesWithinAABB(lumien.randomthings.entity.SpiritEntity.class, c.area().grow(8)).size(), "the dispenser spawned a spirit");

			for (lumien.randomthings.entity.SpiritEntity spirit : c.world.getEntitiesWithinAABB(lumien.randomthings.entity.SpiritEntity.class, c.area().grow(8)))
			{
				spirit.remove();
			}

			net.minecraft.util.math.BlockPos away = c.at(40, 1, 40);	// the player must not stand in the test area: later tests count drops there
			c.player.setPosition(away.getX() + 0.5, away.getY(), away.getZ() + 0.5);
		}));

		tests.add(RTTest.named("parity.spirit.moon_bonus_applies_in_the_end").run(c -> {
			net.minecraft.world.World end = c.world.getServer().getWorld(net.minecraft.world.dimension.DimensionType.THE_END);
			c.check(end != null, "the End is loaded");
			c.check(lumien.randomthings.RandomThings.spiritMoonTime(end), "the Spirit moon bonus applies in the End (fixed time), on every version");
		}));

		tests.add(RTTest.named("parity.spectre_rock_blocks.need_a_pickaxe_to_drop").run(c -> {
			c.floor();
			net.minecraft.block.Block[] blocks = { lumien.randomthings.block.ModBlocks.SPECTRE_ENERGY_INJECTOR, lumien.randomthings.block.ModBlocks.SPECTRE_COIL_NORMAL, lumien.randomthings.block.ModBlocks.SPECTRE_COIL_REDSTONE, lumien.randomthings.block.ModBlocks.SPECTRE_COIL_ENDER, lumien.randomthings.block.ModBlocks.SPECTRE_COIL_NUMBER, lumien.randomthings.block.ModBlocks.SPECTRE_COIL_GENESIS };
			net.minecraft.util.math.BlockPos p = c.at(0, 1, 0);

			for (net.minecraft.block.Block block : blocks)
			{
				c.set(p, block.getDefaultState());
				net.minecraft.block.BlockState state = c.world.getBlockState(p);
				c.player.setHeldItem(net.minecraft.util.Hand.MAIN_HAND, net.minecraft.item.ItemStack.EMPTY);
				c.check(!net.minecraftforge.common.ForgeHooks.canHarvestBlock(state, c.player, c.world, p), block.getRegistryName() + ": mined by hand it drops nothing (1.12.2: Material.ROCK needs a pickaxe)");
				c.player.setHeldItem(net.minecraft.util.Hand.MAIN_HAND, new net.minecraft.item.ItemStack(net.minecraft.item.Items.WOODEN_PICKAXE));
				c.check(net.minecraftforge.common.ForgeHooks.canHarvestBlock(state, c.player, c.world, p), block.getRegistryName() + ": any pickaxe makes it drop");
			}

			c.player.setHeldItem(net.minecraft.util.Hand.MAIN_HAND, net.minecraft.item.ItemStack.EMPTY);
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
			java.util.List<net.minecraft.item.ItemStack> plain = red.getDrops(new net.minecraft.world.storage.loot.LootContext.Builder(c.world).withParameter(net.minecraft.world.storage.loot.LootParameters.POSITION, pos).withParameter(net.minecraft.world.storage.loot.LootParameters.TOOL, new net.minecraft.item.ItemStack(net.minecraft.item.Items.DIAMOND_SHOVEL)));
			c.check(plain.size() == 1 && plain.get(0).getItem() == net.minecraft.item.Items.DIRT, "without silk touch: plain dirt, got " + plain);

			net.minecraft.item.ItemStack silk = new net.minecraft.item.ItemStack(net.minecraft.item.Items.DIAMOND_SHOVEL);
			silk.addEnchantment(net.minecraft.enchantment.Enchantments.SILK_TOUCH, 1);
			java.util.List<net.minecraft.item.ItemStack> silked = red.getDrops(new net.minecraft.world.storage.loot.LootContext.Builder(c.world).withParameter(net.minecraft.world.storage.loot.LootParameters.POSITION, pos).withParameter(net.minecraft.world.storage.loot.LootParameters.TOOL, silk));
			c.check(silked.size() == 1 && silked.get(0).getItem() == lumien.randomthings.block.ModBlocks.COLORED_GRASS.asItem(), "with silk touch: itself, got " + silked);
		}));

		return tests;
	}
}
