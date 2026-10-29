package lumien.randomthings.debug.test;

import java.util.ArrayList;
import java.util.List;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.config.RTConfig;
import lumien.randomthings.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

/**
 * Tests for the rows of {@code TESTING_CHECKLIST.md} that used to be checked by hand: the three glass blocks (#15-28), tooltips and durability bars (#118,
 * #221, #223, #359, #362, #353), the config switches (#337, #340, #345, #353-#355, #358, #338) and the Lotus (#67). Each test names its rows.
 * Config values are changed in place and put back before the test ends.
 */
final class ChecklistTests
{
	private ChecklistTests()
	{
	}

	/** Values a multi-step test has to put back in its last step. */
	private static final java.util.Map<String, Object> SAVED = new java.util.HashMap<>();

	private static boolean client()
	{
		try
		{
			Object dist = Class.forName("net.minecraftforge.fml.loading.FMLEnvironment").getField("dist").get(null);
			return "CLIENT".equals(String.valueOf(dist));
		}
		catch (ReflectiveOperationException e)
		{
			throw new TestCtx.Failure("cannot tell the physical side: " + e);
		}
	}

	/** The tooltip lines of a stack as plain text, as the client builds them; dedicated servers have no tooltip code to run. */
	private static String tooltip(TestCtx c, ItemStack stack)
	{
		if (!client())
		{
			throw new TestCtx.Skip("tooltips are built on the client only");
		}

		return ClientSide.tooltip(c, stack);
	}

	private static String translated(String key)
	{
		return new net.minecraft.util.text.TranslationTextComponent(key).getString();
	}

	private static ItemStack bucketWith(Item bucket, net.minecraft.fluid.Fluid fluid, int amount)
	{
		ItemStack stack = new ItemStack(bucket);
		int filled = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).map(handler -> handler.fill(new FluidStack(fluid, amount), FluidAction.EXECUTE)).orElse(0);

		if (filled != amount)
		{
			throw new TestCtx.Failure("could not fill the bucket with " + amount + " (took " + filled + ")");
		}

		return stack;
	}

	private static boolean passable(TestCtx c, BlockState state, BlockPos pos, ISelectionContext context)
	{
		return state.getCollisionShape(c.world, pos, context).isEmpty();
	}

	/** Runs {@code body} with a config value changed, whatever happens inside. */
	private static <T> void with(ForgeConfigSpec.ConfigValue<T> value, T changed, Runnable body)
	{
		T old = value.get();
		value.set(changed);

		try
		{
			body.run();
		}
		finally
		{
			value.set(old);
		}
	}

	/** Whether the chunk loader holds an anchor at this position (it keeps its anchors in a private set). */
	private static boolean anchorRegistered(TestCtx c, BlockPos pos)
	{
		try
		{
			Object data = c.world.getSavedData().getOrCreate(lumien.randomthings.handler.AnchorChunkLoader::new, "randomthings_anchor_chunks");
			java.lang.reflect.Field field = lumien.randomthings.handler.AnchorChunkLoader.class.getDeclaredField("anchors");
			field.setAccessible(true);
			return ((java.util.Set<?>) field.get(data)).contains(pos.toLong());
		}
		catch (ReflectiveOperationException e)
		{
			throw new TestCtx.Failure("cannot read the anchor set: " + e);
		}
	}

	static List<RTTest> create()
	{
		List<RTTest> tests = new ArrayList<>();

		// ---- Trigger Glass: #16-20 ---------------------------------------------------------------------------
		tests.add(RTTest.named("checklist.trigger_glass.powered_is_passable_and_unsuffocating_and_solid_again_about_60_ticks_after_the_power_goes").run(c -> {
			c.floor();
			BlockPos glass = c.at(0, 1, 0);
			c.set(glass, ModBlocks.TRIGGER_GLASS);
			BlockState state = c.world.getBlockState(glass);
			c.check(!passable(c, state, glass, ISelectionContext.forEntity(c.player)), "#16: solid while unpowered");
			c.check(state.causesSuffocation(c.world, glass), "#17: suffocating while unpowered");
			c.set(c.at(1, 1, 0), Blocks.REDSTONE_BLOCK);
		}).waitTicks(2).run(c -> {
			BlockPos glass = c.at(0, 1, 0);
			BlockState state = c.world.getBlockState(glass);
			c.check(state.get(lumien.randomthings.block.TriggerGlassBlock.TRIGGERED), "#16: a redstone block next to it triggers it");
			c.check(passable(c, state, glass, ISelectionContext.forEntity(c.player)), "#16: no collision while triggered");
			c.check(!state.causesSuffocation(c.world, glass), "#17: no suffocation while triggered");
			c.remove(c.at(1, 1, 0));
		}).waitTicks(40).run(c -> {
			c.check(c.world.getBlockState(c.at(0, 1, 0)).get(lumien.randomthings.block.TriggerGlassBlock.TRIGGERED), "#19: still passable 40 ticks after the power went");
		}).waitTicks(40).run(c -> {
			c.check(!c.world.getBlockState(c.at(0, 1, 0)).get(lumien.randomthings.block.TriggerGlassBlock.TRIGGERED), "#19: solid again 80 ticks after the power went");
		}));

		tests.add(RTTest.named("checklist.trigger_glass.sand_falls_through_while_triggered_and_stops_on_it_when_not").run(c -> {
			c.floor();
			c.set(c.at(0, 3, 0), ModBlocks.TRIGGER_GLASS);
			c.set(c.at(1, 3, 0), Blocks.REDSTONE_BLOCK);
			c.set(c.at(4, 3, 0), ModBlocks.TRIGGER_GLASS);
			c.set(c.at(0, 6, 0), Blocks.SAND);
			c.set(c.at(4, 6, 0), Blocks.SAND);
		}).waitTicks(30).run(c -> {
			c.eq(Blocks.SAND, c.world.getBlockState(c.at(0, 1, 0)).getBlock(), "#20: the sand over the triggered glass fell all the way to the floor");
			c.eq(ModBlocks.TRIGGER_GLASS, c.world.getBlockState(c.at(0, 3, 0)).getBlock(), "#20: the glass is still there");
			c.eq(Blocks.SAND, c.world.getBlockState(c.at(4, 4, 0)).getBlock(), "the sand over the unpowered glass rests on it");
		}));

		tests.add(RTTest.named("checklist.trigger_glass.chain_limit_config_caps_how_many_touching_blocks_trigger").run(c -> {
			c.floor();

			for (int limit : new int[] { 3, 0 })
			{
				for (int x = 0; x < 6; x++)
				{
					c.setQuiet(c.at(x, 2, 0), ModBlocks.TRIGGER_GLASS.getDefaultState());
				}

				int[] triggered = { 0 };
				int expected = limit == 0 ? 6 : limit;

				with(RTConfig.TRIGGER_GLASS_CHAIN_LIMIT, limit, () -> {
					c.set(c.at(0, 3, 0), Blocks.REDSTONE_BLOCK);

					for (int x = 0; x < 6; x++)
					{
						if (c.world.getBlockState(c.at(x, 2, 0)).get(lumien.randomthings.block.TriggerGlassBlock.TRIGGERED))
						{
							triggered[0]++;
						}
					}
				});

				c.eq(expected, triggered[0], "#355: TriggerGlassChainLimit=" + limit + " triggers this many of 6 touching blocks");
				c.remove(c.at(0, 3, 0));

				for (int x = 0; x < 6; x++)
				{
					c.setQuiet(c.at(x, 2, 0), Blocks.AIR.getDefaultState());
				}
			}
		}));

		// ---- Lapis Glass and Quartz Glass: #21-28 ------------------------------------------------------------
		tests.add(RTTest.named("checklist.lapis_and_quartz_glass.collision_depends_on_who_is_asking_and_neither_suffocates").run(c -> {
			c.floor();
			BlockPos pos = c.at(0, 1, 0);
			ZombieEntity zombie = EntityType.ZOMBIE.create(c.world);
			ISelectionContext player = ISelectionContext.forEntity(c.player);
			ISelectionContext mob = ISelectionContext.forEntity(zombie);

			c.set(pos, ModBlocks.LAPIS_GLASS);
			BlockState lapis = c.world.getBlockState(pos);
			c.check(!passable(c, lapis, pos, player), "#21/#27: Lapis Glass is solid to the player");
			c.check(passable(c, lapis, pos, mob), "#22: Lapis Glass is passable to a mob");
			c.check(passable(c, lapis, pos, ISelectionContext.dummy()), "#23: the context-free check sees it as passable (no push-out, no suffocation)");
			c.check(!lapis.causesSuffocation(c.world, pos), "#23: Lapis Glass does not suffocate");

			c.set(pos, ModBlocks.QUARTZ_GLASS);
			BlockState quartz = c.world.getBlockState(pos);
			c.check(passable(c, quartz, pos, player), "#24: Quartz Glass is passable to the player");
			c.check(!passable(c, quartz, pos, mob), "#25: Quartz Glass is solid to a mob");
			c.check(passable(c, quartz, pos, ISelectionContext.dummy()), "#26: the context-free check sees it as passable");
			c.check(!quartz.causesSuffocation(c.world, pos), "#26: Quartz Glass does not suffocate");
		}));

		// ---- tooltips and bars ----------------------------------------------------------------------------------
		tests.add(RTTest.named("checklist.ender_bucket.name_shows_the_fluid_only_while_full").run(c -> {
			if (!client())
			{
				throw new TestCtx.Skip("item names are translated on the client only (the dedicated server has no mod language file)");
			}

			ItemStack empty = new ItemStack(ModItems.ENDER_BUCKET);
			ItemStack water = bucketWith(ModItems.ENDER_BUCKET, Fluids.WATER, 1000);
			String fluid = new FluidStack(Fluids.WATER, 1000).getDisplayName().getString();
			c.check(water.getDisplayName().getString().contains(fluid), "#221: the full bucket's name mentions " + fluid + ", got '" + water.getDisplayName().getString() + "'");
			c.check(!empty.getDisplayName().getString().contains(fluid), "#221: the empty bucket's name does not");
			c.check(!water.getDisplayName().getString().equals(empty.getDisplayName().getString()), "#221: the name changes when it fills");
		}));

		tests.add(RTTest.named("checklist.reinforced_ender_bucket.bar_shows_the_fill_and_takes_the_fluid_colour").run(c -> {
			Item bucket = ModItems.REINFORCED_ENDER_BUCKET;
			ItemStack empty = new ItemStack(bucket);
			ItemStack half = bucketWith(bucket, Fluids.WATER, 5000);
			ItemStack full = bucketWith(bucket, Fluids.WATER, 10000);
			c.check(bucket.showDurabilityBar(half), "#223: the bar is visible");
			c.eq(0, barWidth(empty), "#223: empty bucket, empty bar");
			c.eq(7, barWidth(half), "#223: half full, 13 px bar rounds to 7");
			c.eq(13, barWidth(full), "#223: full bucket, full bar");
			c.eq(Fluids.WATER.getAttributes().getColor(new FluidStack(Fluids.WATER, 5000)), bucket.getRGBDurabilityForDisplay(half), "#223: the bar takes the colour of the fluid");
		}));

		tests.add(RTTest.named("checklist.tooltips.ender_anchor_line_follows_the_chunkloading_config").run(c -> {
			ItemStack anchor = new ItemStack(ModBlocks.ENDER_ANCHOR);
			String line = translated("tooltip.randomthings.ender_anchor.chunkloading");
			with(RTConfig.ENDER_ANCHOR_CHUNKLOADING, true, () -> c.check(tooltip(c, anchor).contains(line), "#359: the tooltip says it keeps the chunk loaded"));
			with(RTConfig.ENDER_ANCHOR_CHUNKLOADING, false, () -> c.check(!tooltip(c, anchor).contains(line), "#359: the line is gone with EnderAnchorChunkloading = false"));
		}));

		tests.add(RTTest.named("checklist.tooltips.ender_letter_explains_how_to_send_only_while_unsent").run(c -> {
			ItemStack letter = new ItemStack(ModItems.ENDER_LETTER);
			String line = translated("item.randomthings.ender_letter.how_to_send");
			c.check(tooltip(c, letter).contains(line), "#362: an unsent letter says how to send it");
			letter.getOrCreateTag().putBoolean("received", true);
			c.check(!tooltip(c, letter).contains(line), "#362: a received letter does not");
		}));

		tests.add(RTTest.named("checklist.tooltips.portkey_shows_only_whether_a_target_is_set_with_hide_coordinates").run(c -> {
			ItemStack portkey = new ItemStack(ModItems.PORTKEY);
			net.minecraft.nbt.CompoundNBT tag = portkey.getOrCreateTag();
			tag.putBoolean("hasTarget", true);
			tag.putInt("dimension", 0);
			tag.putInt("targetX", 123);
			tag.putInt("targetY", 64);
			tag.putInt("targetZ", 456);
			String set = translated("tooltip.randomthings.portkey.set");
			with(RTConfig.HIDE_COORDINATES, true, () -> {
				String text = tooltip(c, portkey);
				c.check(text.contains(set), "#353: the Portkey says '<Target set>'");
				c.check(!text.contains("123") && !text.contains("456"), "#353: and never the coordinates");
			});
			with(RTConfig.HIDE_COORDINATES, false, () -> c.check(!tooltip(c, portkey).contains(set), "#353: without the option the line is not there"));
		}));

		// ---- config switches ---------------------------------------------------------------------------------------
		tests.add(RTTest.named("checklist.config.golden_egg_off_removes_the_egg_from_bean_pods").run(c -> {
			c.floor();
			BlockPos pos = c.at(0, 1, 0);
			BlockState pod = ModBlocks.BEAN_POD.getDefaultState();

			for (boolean on : new boolean[] { true, false })
			{
				with(RTConfig.GOLDEN_EGG, on, () -> {
					int eggs = 0;

					for (int i = 0; i < 20; i++)
					{
						for (ItemStack drop : pod.getDrops(new LootContext.Builder(c.world).withParameter(LootParameters.POSITION, pos).withParameter(LootParameters.TOOL, ItemStack.EMPTY)))
						{
							if (drop.getItem() == ModItems.GOLDEN_EGG)
							{
								eggs += drop.getCount();
							}
						}
					}

					c.eq(on ? 20 : 0, eggs, "#340: GoldenEgg = " + on + ": golden eggs out of 20 pods");
				});
			}
		}));

		tests.add(RTTest.named("checklist.config.magnetic_off_stops_the_pull_of_drops").run(c -> {
			c.floor();
			BlockPos pos = c.at(0, 1, 0);
			ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE);
			tool.addEnchantment(lumien.randomthings.enchantment.ModEnchantments.MAGNETIC, 1);
			c.player.inventory.clear();

			with(RTConfig.MAGNETIC_ENCHANTMENT, true, () -> {
				List<ItemStack> drops = stoneDrops(c, pos, tool);
				c.check(drops.isEmpty(), "#337: with the enchantment on, the drops are taken out of the world drop list, got " + drops);
				c.check(c.player.inventory.hasItemStack(new ItemStack(Items.COBBLESTONE)), "#337: and are in the player's inventory");
			});

			c.player.inventory.clear();

			with(RTConfig.MAGNETIC_ENCHANTMENT, false, () -> {
				List<ItemStack> drops = stoneDrops(c, pos, tool);
				c.check(!drops.isEmpty(), "#337: MagneticEnchantment = false: the drops stay in the drop list");
				c.check(!c.player.inventory.hasItemStack(new ItemStack(Items.COBBLESTONE)), "#337: and nothing reaches the inventory");
			});
		}));

		tests.add(RTTest.named("checklist.config.spirit_lifetime_sets_how_long_a_spirit_lives").run(c -> {
			lumien.randomthings.entity.SpiritEntity spirit = lumien.randomthings.entity.ModEntityTypes.SPIRIT.create(c.world);
			BlockPos p = c.at(0, 2, 0);
			spirit.setPosition(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
			c.world.addEntity(spirit);
			SAVED.put("spirit_old", RTConfig.SPIRIT_LIFETIME.get());
			SAVED.put("spirit_id", spirit.getEntityId());
			RTConfig.SPIRIT_LIFETIME.set(10);
		}).waitTicks(30).run(c -> {
			RTConfig.SPIRIT_LIFETIME.set((Integer) SAVED.get("spirit_old"));
			int id = (Integer) SAVED.get("spirit_id");
			net.minecraft.entity.Entity spirit = c.world.getEntityByID(id);
			c.check(spirit == null || !spirit.isAlive(), "#345: a Spirit lives about SpiritLifeTime ticks: with 10 it is gone after 30");
		}));

		tests.add(RTTest.named("checklist.config.block_destabilizer_limit_caps_how_many_blocks_fall").run(c -> {
			c.floor();
			c.set(c.at(-4, 2, 0), ModBlocks.BLOCK_DESTABILIZER.getDefaultState().with(lumien.randomthings.block.BlockDestabilizerBlock.FACING, Direction.EAST));

			for (int x = -3; x <= 4; x++)
			{
				c.set(c.at(x, 2, 0), Blocks.STONE);
			}

			SAVED.put("destab_old", RTConfig.BLOCK_DESTABILIZER_LIMIT.get());
			RTConfig.BLOCK_DESTABILIZER_LIMIT.set(3);
			c.set(c.at(-4, 2, 1), Blocks.REDSTONE_BLOCK);
		}).waitTicks(80).run(c -> {
			RTConfig.BLOCK_DESTABILIZER_LIMIT.set((Integer) SAVED.get("destab_old"));
			int still = 0;

			for (int x = -3; x <= 4; x++)
			{
				if (c.world.getBlockState(c.at(x, 2, 0)).getBlock() == Blocks.STONE)
				{
					still++;
				}
			}

			c.eq(5, still, "#354: BlockDestabilizerLimit = 3: three of the eight stones fell, five stayed");
		}));

		tests.add(RTTest.named("checklist.config.ender_anchor_chunkloading_off_registers_nothing_and_on_registers_and_releases_the_anchor").run(c -> {
			c.floor();
			SAVED.put("anchor_old", RTConfig.ENDER_ANCHOR_CHUNKLOADING.get());
			RTConfig.ENDER_ANCHOR_CHUNKLOADING.set(false);
			c.set(c.at(0, 1, 0), ModBlocks.ENDER_ANCHOR);
		}).waitTicks(5).run(c -> {
			boolean registered = anchorRegistered(c, c.at(0, 1, 0));
			c.remove(c.at(0, 1, 0));
			RTConfig.ENDER_ANCHOR_CHUNKLOADING.set(true);
			c.check(!registered, "#358: EnderAnchorChunkloading = false: the anchor registers no chunk to keep loaded");
			c.set(c.at(0, 1, 0), ModBlocks.ENDER_ANCHOR);
		}).waitTicks(5).run(c -> {
			boolean registered = anchorRegistered(c, c.at(0, 1, 0));
			c.remove(c.at(0, 1, 0));
			RTConfig.ENDER_ANCHOR_CHUNKLOADING.set((Boolean) SAVED.get("anchor_old"));
			c.check(registered, "#358: with it on, the anchor registers its chunk");
			c.check(!anchorRegistered(c, c.at(0, 1, 0)), "#358: and breaking the anchor releases it");
		}));

		tests.add(RTTest.named("checklist.config.chest_loot_pools_are_there_exactly_when_their_toggle_is_on").run(c -> {
			java.lang.reflect.Field poolsField = null;

			// the pool list is the only List field of a loot table (looked up by type: the field name differs between dev and release names)
			for (java.lang.reflect.Field field : net.minecraft.world.storage.loot.LootTable.class.getDeclaredFields())
			{
				if (List.class.isAssignableFrom(field.getType()))
				{
					poolsField = field;
					poolsField.setAccessible(true);
				}
			}

			if (poolsField == null)
			{
				throw new TestCtx.Skip("cannot find the pool list of a loot table");
			}

			java.lang.reflect.Field pools = poolsField;
			java.util.function.BiPredicate<net.minecraft.util.ResourceLocation, String> hasPool = (table, poolName) -> {
				net.minecraft.world.storage.loot.LootTable loot = c.world.getServer().getLootTableManager().getLootTableFromLocation(table);

				try
				{
					for (Object pool : (List<?>) pools.get(loot))
					{
						if (pool instanceof net.minecraft.world.storage.loot.LootPool && poolName.equals(((net.minecraft.world.storage.loot.LootPool) pool).getName()))
						{
							return true;
						}
					}
				}
				catch (IllegalAccessException e)
				{
					throw new TestCtx.Failure("cannot read the pools: " + e);
				}

				return false;
			};

			c.eq(RTConfig.SUMMONING_PENDULUM.get(), hasPool.test(lumien.randomthings.handler.LootHandler.SIMPLE_DUNGEON, "randomthings:summoning_pendulum"), "#338: Summoning Pendulum pool vs its toggle");
			c.eq(RTConfig.MAGIC_HOOD.get(), hasPool.test(lumien.randomthings.handler.LootHandler.VILLAGE_WEAPONSMITH, "randomthings:magic_hood"), "#338: Magic Hood pool vs its toggle");
			c.eq(RTConfig.BIOME_CRYSTAL.get(), hasPool.test(lumien.randomthings.handler.LootHandler.STRONGHOLD_CORRIDOR, "randomthings:biome_crystal"), "#338: Biome Crystal pool vs its toggle");
			c.eq(RTConfig.NUMBERED_SPECTRE_COILS.get(), hasPool.test(lumien.randomthings.handler.LootHandler.END_CITY_TREASURE, "randomthings:spectre_coil_number"), "Numbered Spectre Coil pool vs its toggle");
		}));

		// ---- Lotus: #67 ---------------------------------------------------------------------------------------------
		tests.add(RTTest.named("checklist.lotus.grows_through_four_stages_and_is_harvested_at_the_last_without_breaking").run(c -> {
			c.floor();
			BlockPos pos = c.at(0, 1, 0);
			c.setQuiet(pos.down(), Blocks.DIRT.getDefaultState());
			c.setQuiet(pos, ModBlocks.LOTUS.getDefaultState());
			java.util.Random random = new java.util.Random(7);
			int age = 0;

			for (int i = 0; i < 2000 && age < 3; i++)
			{
				BlockState state = c.world.getBlockState(pos);
				state.randomTick(c.world, pos, random);
				int now = c.world.getBlockState(pos).get(lumien.randomthings.block.LotusBlock.AGE);
				c.check(now == age || now == age + 1, "#67: the Lotus grows one stage at a time (" + age + " -> " + now + ")");
				age = now;
			}

			c.eq(3, age, "#67: it reaches the fourth stage");
			BlockState ripe = c.world.getBlockState(pos);
			c.check(ripe.onBlockActivated(c.world, c.player, Hand.MAIN_HAND, new BlockRayTraceResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, false)), "#67: right-clicking the ripe Lotus harvests it");
			c.eq(ModBlocks.LOTUS, c.world.getBlockState(pos).getBlock(), "#67: the plant is still there");
			c.eq(0, c.world.getBlockState(pos).get(lumien.randomthings.block.LotusBlock.AGE), "#67: and starts over");
			c.check(c.player.inventory.hasItemStack(new ItemStack(ModItems.LOTUS_BLOSSOM)) || c.itemEntities().stream().anyMatch(e -> e.getItem().getItem() == ModItems.LOTUS_BLOSSOM), "#67: a Lotus Blossom came out");
			BlockState young = c.world.getBlockState(pos);
			c.check(!young.onBlockActivated(c.world, c.player, Hand.MAIN_HAND, new BlockRayTraceResult(new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), Direction.UP, pos, false)), "#67: a young Lotus is not harvested");
		}));

		return tests;
	}

	/** The width the inventory draws for the durability bar (ItemRenderer: round(13 - damage * 13)). */
	private static int barWidth(ItemStack stack)
	{
		return Math.round(13.0F - (float) stack.getItem().getDurabilityForDisplay(stack) * 13.0F);
	}

	private static List<ItemStack> stoneDrops(TestCtx c, BlockPos pos, ItemStack tool)
	{
		return Blocks.STONE.getDefaultState().getDrops(new LootContext.Builder(c.world).withParameter(LootParameters.POSITION, pos).withParameter(LootParameters.TOOL, tool).withNullableParameter(LootParameters.THIS_ENTITY, c.player));
	}

	/** The client-only tooltip lookup, kept in its own class so dedicated servers never load it. */
	private static final class ClientSide
	{
		static String tooltip(TestCtx c, ItemStack stack)
		{
			StringBuilder text = new StringBuilder();

			for (net.minecraft.util.text.ITextComponent line : stack.getTooltip(c.player, net.minecraft.client.util.ITooltipFlag.TooltipFlags.NORMAL))
			{
				text.append(line.getString()).append('\n');
			}

			return text.toString();
		}
	}
}
