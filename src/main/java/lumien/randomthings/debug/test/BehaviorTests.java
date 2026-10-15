package lumien.randomthings.debug.test;

import java.util.ArrayList;
import java.util.List;

import lumien.randomthings.block.AnalogEmitterBlock;
import lumien.randomthings.block.BlockBreakerBlock;
import lumien.randomthings.block.IgniterBlock;
import lumien.randomthings.block.IronDropperBlock;
import lumien.randomthings.block.ItemCollectorBlock;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.recipes.SpectreAnchorCombineRecipe;
import lumien.randomthings.tileentity.AnalogEmitterTileEntity;
import lumien.randomthings.tileentity.IgniterTileEntity;
import lumien.randomthings.tileentity.IronDropperTileEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

/**
 * Gameplay tests: build a small contraption in the world, let it run, and assert on the result.
 * Each test gets a clean build area; {@code c.at(0, 1, 0)} is the first block above the floor.
 */
final class BehaviorTests
{
	private BehaviorTests()
	{
	}

	static List<RTTest> create()
	{
		List<RTTest> tests = new ArrayList<>();

		// ---- Item Collector -------------------------------------------------------------------
		tests.add(RTTest.named("behavior.item_collector.moves_nearby_items_into_inventory").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), Blocks.CHEST);
			c.set(c.at(0, 2, 0), ModBlocks.ITEM_COLLECTOR.defaultBlockState().setValue(ItemCollectorBlock.FACING, Direction.UP));
			c.spawnItem(0.5, 4.5, 0.5, new ItemStack(Items.COBBLESTONE, 4));
		}).waitTicks(70).run(c -> {
			c.eq(4, TestCtx.count(c.inventory(c.at(0, 1, 0)), Items.COBBLESTONE), "cobblestone in chest");
			c.eq(0, c.itemEntities().size(), "loose item entities left");
		}));

		// ---- Iron Dropper ---------------------------------------------------------------------
		tests.add(RTTest.named("behavior.iron_dropper.pulse_mode_drops_one_item_per_rising_edge_into_inventory").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), Blocks.CHEST);
			c.set(c.at(0, 2, 0), ModBlocks.IRON_DROPPER.defaultBlockState().setValue(IronDropperBlock.FACING, Direction.DOWN));
			IronDropperTileEntity dropper = c.tile(c.at(0, 2, 0), IronDropperTileEntity.class);
			dropper.itemHandler().insertItem(0, new ItemStack(Items.COBBLESTONE, 5), false);
			dropper.rotateRedstoneMode();
			c.eq(IronDropperTileEntity.REDSTONE_MODE.PULSE, dropper.redstoneMode(), "mode after rotate");
			c.set(c.at(1, 2, 0), Blocks.REDSTONE_BLOCK);
		}).waitTicks(3).run(c -> {
			c.eq(1, TestCtx.count(c.inventory(c.at(0, 1, 0)), Items.COBBLESTONE), "cobblestone in chest after first pulse");
			c.remove(c.at(1, 2, 0));
		}).waitTicks(3).run(c -> {
			c.eq(1, TestCtx.count(c.inventory(c.at(0, 1, 0)), Items.COBBLESTONE), "falling edge must not drop");
			c.set(c.at(1, 2, 0), Blocks.REDSTONE_BLOCK);
		}).waitTicks(3).run(c -> {
			c.eq(2, TestCtx.count(c.inventory(c.at(0, 1, 0)), Items.COBBLESTONE), "cobblestone in chest after second pulse");
			c.eq(3, c.tile(c.at(0, 2, 0), IronDropperTileEntity.class).itemHandler().getStackInSlot(0).getCount(), "cobblestone left in dropper");
		}));

		int[] unpoweredSnapshot = new int[1];
		tests.add(RTTest.named("behavior.iron_dropper.repeat_mode_keeps_dropping_while_powered_and_stops_when_not").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), Blocks.CHEST);
			c.set(c.at(0, 2, 0), ModBlocks.IRON_DROPPER.defaultBlockState().setValue(IronDropperBlock.FACING, Direction.DOWN));
			c.tile(c.at(0, 2, 0), IronDropperTileEntity.class).itemHandler().insertItem(0, new ItemStack(Items.COBBLESTONE, 16), false);
			c.set(c.at(1, 2, 0), Blocks.REDSTONE_BLOCK);
		}).waitTicks(30).run(c -> {
			int inChest = TestCtx.count(c.inventory(c.at(0, 1, 0)), Items.COBBLESTONE);
			int inDropper = TestCtx.count(c.tile(c.at(0, 2, 0), IronDropperTileEntity.class).itemHandler(), Items.COBBLESTONE);
			c.check(inChest >= 4, "expected at least 4 dropped in 30 ticks, chest has " + inChest);
			c.eq(16, inChest + inDropper, "items must not be lost or duplicated");
			c.remove(c.at(1, 2, 0));
		}).waitTicks(2).run(c -> {
			unpoweredSnapshot[0] = TestCtx.count(c.inventory(c.at(0, 1, 0)), Items.COBBLESTONE);
		}).waitTicks(12).run(c -> {
			c.eq(unpoweredSnapshot[0], TestCtx.count(c.inventory(c.at(0, 1, 0)), Items.COBBLESTONE), "chest count must stop growing once unpowered");
		}));

		tests.add(RTTest.named("behavior.iron_dropper.drops_item_entity_when_nothing_is_in_front").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.IRON_DROPPER.defaultBlockState().setValue(IronDropperBlock.FACING, Direction.EAST));
			IronDropperTileEntity dropper = c.tile(c.at(0, 1, 0), IronDropperTileEntity.class);
			dropper.itemHandler().insertItem(0, new ItemStack(Items.COBBLESTONE, 5), false);
			dropper.rotateRedstoneMode();
			c.set(c.at(0, 2, 0), Blocks.REDSTONE_BLOCK);
		}).waitTicks(3).run(c -> {
			c.eq(1, c.itemEntities().size(), "dropped item entities");
			c.eq(Items.COBBLESTONE, c.itemEntities().get(0).getItem().getItem(), "dropped item");
			c.eq(4, c.tile(c.at(0, 1, 0), IronDropperTileEntity.class).itemHandler().getStackInSlot(0).getCount(), "cobblestone left in dropper");
		}));

		// ---- Igniter --------------------------------------------------------------------------
		tests.add(RTTest.named("behavior.igniter.toggle_mode_lights_fire_when_powered_and_removes_it_when_unpowered").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.IGNITER.defaultBlockState().setValue(IgniterBlock.FACING, Direction.EAST));
			c.set(c.at(0, 2, 0), Blocks.REDSTONE_BLOCK);
		}).waitTicks(2).run(c -> {
			c.eq(Blocks.FIRE, c.world.getBlockState(c.at(1, 1, 0)).getBlock(), "block in front when powered");
			c.remove(c.at(0, 2, 0));
		}).waitTicks(2).run(c -> {
			c.check(c.world.isEmptyBlock(c.at(1, 1, 0)), "fire should be removed in TOGGLE mode, found " + c.world.getBlockState(c.at(1, 1, 0)).getBlock());
		}));

		tests.add(RTTest.named("behavior.igniter.ignite_mode_leaves_fire_when_unpowered").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.IGNITER.defaultBlockState().setValue(IgniterBlock.FACING, Direction.EAST));
			IgniterTileEntity igniter = c.tile(c.at(0, 1, 0), IgniterTileEntity.class);
			igniter.rotateMode();
			c.eq(IgniterTileEntity.MODE.IGNITE, igniter.mode(), "mode after rotate");
			c.set(c.at(0, 2, 0), Blocks.REDSTONE_BLOCK);
		}).waitTicks(2).run(c -> {
			c.eq(Blocks.FIRE, c.world.getBlockState(c.at(1, 1, 0)).getBlock(), "block in front when powered");
			c.remove(c.at(0, 2, 0));
		}).waitTicks(2).run(c -> {
			c.eq(Blocks.FIRE, c.world.getBlockState(c.at(1, 1, 0)).getBlock(), "fire must stay in IGNITE mode");
		}));

		// ---- Analog Emitter -------------------------------------------------------------------
		tests.add(RTTest.named("behavior.analog_emitter.outputs_configured_level_only_while_input_is_powered").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.ANALOG_EMITTER.defaultBlockState().setValue(AnalogEmitterBlock.FACING, Direction.EAST));
			c.tile(c.at(0, 1, 0), AnalogEmitterTileEntity.class).setEmitLevel(9);
			c.eq(0, c.tile(c.at(0, 1, 0), AnalogEmitterTileEntity.class).getOutput(), "output with no input");
			c.set(c.at(1, 1, 0), Blocks.REDSTONE_BLOCK);
		}).waitTicks(2).run(c -> {
			AnalogEmitterTileEntity te = c.tile(c.at(0, 1, 0), AnalogEmitterTileEntity.class);
			c.eq(9, te.getOutput(), "output with input");
			c.eq(9, c.world.getBlockState(c.at(0, 1, 0)).getSignal(c.world, c.at(0, 1, 0), Direction.UP), "weak power towards UP");
			c.eq(0, c.world.getBlockState(c.at(0, 1, 0)).getSignal(c.world, c.at(0, 1, 0), Direction.EAST), "weak power towards the input side");
			te.setEmitLevel(3);
			c.eq(3, te.getOutput(), "output after changing the level");
			c.remove(c.at(1, 1, 0));
		}).waitTicks(2).run(c -> {
			c.eq(0, c.tile(c.at(0, 1, 0), AnalogEmitterTileEntity.class).getOutput(), "output after input removed");
		}));

		// ---- Block Breaker (also covers the Magnetic enchantment + fake player path) ------------
		tests.add(RTTest.named("behavior.block_breaker.mines_front_block_into_rear_inventory").run(c -> {
			c.floor();
			c.set(c.at(-1, 1, 0), Blocks.CHEST);
			c.set(c.at(1, 1, 0), Blocks.DIRT);
			c.set(c.at(0, 1, 0), ModBlocks.BLOCK_BREAKER.defaultBlockState().setValue(BlockBreakerBlock.FACING, Direction.EAST));
		}).waitTicks(80).run(c -> {
			c.check(c.world.isEmptyBlock(c.at(1, 1, 0)), "target dirt should be mined, found " + c.world.getBlockState(c.at(1, 1, 0)).getBlock());
			c.eq(1, TestCtx.count(c.inventory(c.at(-1, 1, 0)), Items.DIRT), "dirt delivered to the chest behind the breaker (loose items: " + c.itemEntities() + ")");
			c.eq(0, c.itemEntities().size(), "loose item entities (drops should be redirected into the chest)");
		}));

		tests.add(RTTest.named("behavior.block_breaker.does_nothing_while_redstone_powered").run(c -> {
			c.floor();
			c.set(c.at(1, 1, 0), Blocks.DIRT);
			c.set(c.at(0, 2, 0), Blocks.REDSTONE_BLOCK);
			c.set(c.at(0, 1, 0), ModBlocks.BLOCK_BREAKER.defaultBlockState().setValue(BlockBreakerBlock.FACING, Direction.EAST));
		}).waitTicks(60).run(c -> {
			c.eq(Blocks.DIRT, c.world.getBlockState(c.at(1, 1, 0)).getBlock(), "target block while the breaker is powered");
		}));

		// ---- Redstone Observer ----------------------------------------------------------------
		tests.add(RTTest.named("behavior.redstone_observer.repeats_the_power_of_its_bound_target_block").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.REDSTONE_OBSERVER);
			c.set(c.at(1, 1, 0), Blocks.REDSTONE_LAMP);
			c.set(c.at(4, 1, 0), Blocks.STONE);
			c.tile(c.at(0, 1, 0), lumien.randomthings.tileentity.RedstoneObserverTileEntity.class).setTarget(c.at(4, 1, 0));
		}).waitTicks(3).run(c -> {
			c.check(!c.world.getBlockState(c.at(1, 1, 0)).getValue(net.minecraft.world.level.block.RedstoneLampBlock.LIT), "lamp must be dark while the target emits nothing");
			c.set(c.at(4, 1, 0), Blocks.REDSTONE_BLOCK);
		}).waitTicks(4).run(c -> {
			c.check(c.world.getBlockState(c.at(1, 1, 0)).getValue(net.minecraft.world.level.block.RedstoneLampBlock.LIT), "lamp must light when the target becomes a redstone block");
			c.set(c.at(4, 1, 0), Blocks.STONE);
		}).waitTicks(10).run(c -> {
			c.check(!c.world.getBlockState(c.at(1, 1, 0)).getValue(net.minecraft.world.level.block.RedstoneLampBlock.LIT), "lamp must go dark when the target stops emitting");
		}));

		// ---- Special crafting recipes ---------------------------------------------------------
		tests.add(RTTest.named("behavior.recipes.spectre_anchor_combine_tags_single_unstackable_item").run(c -> {
			SpectreAnchorCombineRecipe recipe = new SpectreAnchorCombineRecipe(new ResourceLocation("randomthings", "test"));
			ItemStack anchor = new ItemStack(ModItems.SPECTRE_ANCHOR);

			CraftingContainer good = grid(3, 3, anchor, new ItemStack(Items.IRON_SWORD));
			c.check(recipe.matches(good, c.world), "anchor + sword should match");
			ItemStack result = recipe.assemble(good);
			c.eq(Items.IRON_SWORD, result.getItem(), "result item");
			c.check(result.hasTag() && result.getTag().contains("spectreAnchor"), "result must carry the spectreAnchor tag");
			recipe.getRemainingItems(good).forEach(s -> c.check(s.isEmpty(), "anchor and target are both consumed"));

			c.check(!recipe.matches(grid(3, 3, anchor, new ItemStack(Items.COBBLESTONE)), c.world), "stackable items must not match");
			c.check(!recipe.matches(grid(3, 3, anchor, result), c.world), "an already anchored item must not match again");
			c.check(!recipe.matches(grid(3, 3, anchor, new ItemStack(Items.IRON_SWORD), new ItemStack(Items.IRON_AXE)), c.world), "two targets must not match");
			c.check(!recipe.matches(grid(3, 3, new ItemStack(Items.IRON_SWORD)), c.world), "no anchor must not match");
		}));

		tests.add(RTTest.named("behavior.recipes.every_shaped_and_shapeless_data_recipe_crafts_its_output").run(c -> {
			List<String> problems = new ArrayList<>();
			int checked = 0;

			for (Recipe<?> recipe : c.world.getServer().getRecipeManager().getRecipes())
			{
				if (!"randomthings".equals(recipe.getId().getNamespace()) || recipe.getType() != RecipeType.CRAFTING || recipe.isSpecial() || !(recipe instanceof CraftingRecipe))
				{
					continue;
				}

				checked++;
				CraftingRecipe crafting = (CraftingRecipe) recipe;
				CraftingContainer inv = new CraftingContainer(new NoopContainer(), 3, 3);
				List<Ingredient> ingredients = recipe.getIngredients();
				int width = recipe instanceof ShapedRecipe ? ((ShapedRecipe) recipe).getRecipeWidth() : 3;

				for (int i = 0; i < ingredients.size(); i++)
				{
					ItemStack[] options = ingredients.get(i).getItems();

					if (options.length == 0)
					{
						continue;
					}

					int slot = recipe instanceof ShapedRecipe ? (i / width) * 3 + (i % width) : i;
					ItemStack stack = options[0].copy();
					stack.setCount(1);
					inv.setItem(slot, stack);
				}

				if (!crafting.matches(inv, c.world))
				{
					problems.add(recipe.getId().getPath() + " does not match its own ingredients");
					continue;
				}

				ItemStack out = crafting.assemble(inv);
				ItemStack expected = recipe.getResultItem();

				if (out.getItem() != expected.getItem() || out.getCount() != expected.getCount())
				{
					problems.add(recipe.getId().getPath() + " crafts " + out + " but declares " + expected);
				}
			}

			c.check(checked > 0, "no data recipes found to check");
			c.check(problems.isEmpty(), problems.size() + " of " + checked + " recipes broken: " + problems);
		}));

		return tests;
	}

	private static CraftingContainer grid(int w, int h, ItemStack... stacks)
	{
		CraftingContainer inv = new CraftingContainer(new NoopContainer(), w, h);

		for (int i = 0; i < stacks.length; i++)
		{
			inv.setItem(i, stacks[i].copy());
		}

		return inv;
	}

	private static final class NoopContainer extends AbstractContainerMenu
	{
		NoopContainer()
		{
			super(null, 0);
		}

		@Override
		public boolean stillValid(Player player)
		{
			return true;
		}
	}
}
