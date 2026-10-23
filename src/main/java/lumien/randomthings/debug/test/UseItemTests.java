package lumien.randomthings.debug.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import lumien.randomthings.block.ColoredGrassBlock;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.entity.EclipsedClockEntity;
import lumien.randomthings.entity.ThrownGoldenEggEntity;
import lumien.randomthings.entity.ThrownWeatherEggEntity;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.util.EscapeRopeHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * "Use item" tests: right-clicking items the way a player does ({@code Item.use} / {@code useOn} /
 * {@code onItemUseFinish}) with the test player: Escape Rope (search, teleport, abort, dead end, durability),
 * Blaze and Steel, beans, seeds, Lotus items, Bottle of Air, thrown eggs and the Eclipsed Clock.
 */
final class UseItemTests
{
	private UseItemTests()
	{
	}

	private static void ensureConnection(TestCtx c)
	{
		if (c.player.connection == null || c.player.connection.getClass() != ServerPlayNetHandler.class)
		{
			new ServerPlayNetHandler(c.world.getServer(), new NetworkManager(PacketDirection.SERVERBOUND), c.player);
		}
	}

	private static Item item(String path)
	{
		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("randomthings", path));
		return item;
	}

	private static List<?> ropeTasks() throws Exception
	{
		Field f = EscapeRopeHandler.class.getDeclaredField("runningTasks");
		f.setAccessible(true);
		return (List<?>) f.get(EscapeRopeHandler.getInstance());
	}

	/** Drops every pending Escape Rope search (they outlive a test when the player lets go of the rope). */
	private static void resetRopeTasks() throws Exception
	{
		ropeTasks().clear();
		lumien.randomthings.util.EscapeRopeHandler.maxChecked = lumien.randomthings.util.EscapeRopeHandler.DEFAULT_MAX_CHECKED;
	}

	private static String sky(TestCtx c)
	{
		BlockPos p = c.player.getPosition();
		return "pos " + p + ", canBlockSeeSky " + c.world.canBlockSeeSky(p) + ", canSeeSky " + c.world.canSeeSky(p) + ", empty " + c.world.isAirBlock(p)
				+ ", surfaceHeight " + c.world.getHeight(net.minecraft.world.gen.Heightmap.Type.WORLD_SURFACE, p.getX(), p.getZ()) + ", block above " + c.world.getBlockState(p.up()).getBlock()
				+ ", roof " + c.world.getBlockState(c.at(0, 3, 0)).getBlock();
	}

	private static void standAt(TestCtx c, int x, int y, int z)
	{
		BlockPos p = c.at(x, y, z);
		c.player.setPosition(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
		c.player.setMotion(Vector3d.ZERO);

		if (c.player.connection != null)
		{
			// a real client player keeps sending its own position and would pull the server-side position straight back
			c.player.connection.setPlayerLocation(p.getX() + 0.5, p.getY(), p.getZ() + 0.5, c.player.rotationYaw, c.player.rotationPitch);
		}
	}

	private static void hold(TestCtx c, ItemStack stack)
	{
		c.player.stopActiveHand();
		c.player.setHeldItem(Hand.MAIN_HAND, stack);
	}

	private static void release(TestCtx c)
	{
		c.player.stopActiveHand();
		c.player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
	}

	private static ActionResultType useOn(TestCtx c, BlockPos clicked, Direction face)
	{
		ItemStack held = c.player.getHeldItemMainhand();
		return held.getItem().onItemUse(new ItemUseContext(c.player, Hand.MAIN_HAND, new BlockRayTraceResult(Vector3d.copyCentered(clicked), face, clicked, false)));
	}

	/**
	 * True once no air block under the rope room's roof is reported as seeing the sky (and, for a room with a doorway, the cell
	 * outside it does). The sky light is recomputed asynchronously, and the rope's search treats any stale "sky" cell inside the
	 * room as a way out.
	 */
	/**
	 * A 2-high, 1-wide serpentine tunnel (97 cells long) carved in a solid stone block, starting at x -6, z -6 and ending at x 6, z 6
	 * where a hole in the roof lets daylight in. Nothing else in the area can see the sky, so the only way out is to follow it.
	 */
	private static void buildTunnel(TestCtx c)
	{
		c.floor();

		for (int x = -7; x <= 7; x++)
		{
			for (int z = -7; z <= 7; z++)
			{
				for (int y = 1; y <= 3; y++)
				{
					c.setQuiet(c.at(x, y, z), Blocks.STONE.getDefaultState());
				}
			}
		}

		for (int row = 0; row < 7; row++)
		{
			int z = -6 + row * 2;

			for (int x = -6; x <= 6; x++)
			{
				carve(c, x, z);
			}

			if (row < 6)
			{
				carve(c, row % 2 == 0 ? 6 : -6, z + 1);
			}
		}

		c.setQuiet(c.at(6, 3, 6), Blocks.AIR.getDefaultState());
	}

	private static void carve(TestCtx c, int x, int z)
	{
		c.setQuiet(c.at(x, 1, z), Blocks.AIR.getDefaultState());
		c.setQuiet(c.at(x, 2, z), Blocks.AIR.getDefaultState());
	}

	/** The tunnel is dark except its mouth: nothing but the exit column may see the sky, and the exit column does. */
	private static boolean tunnelReady(TestCtx c)
	{
		for (int x = -6; x <= 6; x++)
		{
			for (int z = -6; z <= 6; z++)
			{
				BlockPos pos = c.at(x, 1, z);

				if (c.world.isAirBlock(pos) && c.world.canBlockSeeSky(pos) != (x == 6 && z == 6))
				{
					return false;
				}
			}
		}

		return true;
	}

	private static int stalePolls;
	private static int rebuilds;

	private static boolean roomShaded(TestCtx c, boolean doorway)
	{
		boolean shaded = true;

		for (int x = -3; x <= 3; x++)
		{
			for (int z = -3; z <= 3; z++)
			{
				for (int y = 1; y <= 2; y++)
				{
					BlockPos pos = c.at(x, y, z);

					if (c.world.isAirBlock(pos) && c.world.canBlockSeeSky(pos))
					{
						shaded = false;
					}
				}
			}
		}

		if (!shaded)
		{
			// the engine sometimes never recomputes a cell (seen at chunk borders); rebuilding the room from scratch re-queues all of it
			if (++stalePolls % 100 == 0 && rebuilds < 2)
			{
				rebuilds++;

				for (int x = -3; x <= 3; x++)
				{
					for (int z = -3; z <= 3; z++)
					{
						for (int y = 1; y <= 3; y++)
						{
							c.setQuiet(c.at(x, y, z), Blocks.AIR.getDefaultState());
						}
					}
				}

				buildRoom(c, doorway);
			}

			return false;
		}

		stalePolls = 0;

		// the daylight just outside the doorway is the rope's destination; it must be lit again after the previous test's cleanup
		return !doorway || c.world.canBlockSeeSky(c.at(4, 1, 0));
	}

	/**
	 * A stone room (inside x -2..2, z -2..2, y 1..2) with a stone roof at y 3 and a 1-wide doorway in the +x wall at z 0.
	 * Sky is visible only outside the roof footprint, so the nearest daylight is the block just outside the doorway.
	 */
	private static void ropeRoom(TestCtx c, boolean doorway)
	{
		stalePolls = 0;
		rebuilds = 0;
		buildRoom(c, doorway);
	}

	private static void buildRoom(TestCtx c, boolean doorway)
	{
		c.floor();

		for (int x = -3; x <= 3; x++)
		{
			for (int z = -3; z <= 3; z++)
			{
				c.setQuiet(c.at(x, 3, z), Blocks.STONE.getDefaultState());

				if (Math.abs(x) == 3 || Math.abs(z) == 3)
				{
					for (int y = 1; y <= 2; y++)
					{
						c.setQuiet(c.at(x, y, z), Blocks.STONE.getDefaultState());
					}
				}
			}
		}

		if (doorway)
		{
			c.setQuiet(c.at(3, 1, 0), Blocks.AIR.getDefaultState());
			c.setQuiet(c.at(3, 2, 0), Blocks.AIR.getDefaultState());
		}
	}

	static List<RTTest> create()
	{
		List<RTTest> tests = new ArrayList<>();
		Item rope = item("escape_rope");

		// ---- Escape Rope ------------------------------------------------------------------------
		tests.add(RTTest.named("use_item.escape_rope.refuses_to_start_under_open_sky").run(c -> {
			resetRopeTasks();
			c.floor();
		}).waitTicks(20).run(c -> {
			standAt(c, 0, 1, 0);
			hold(c, new ItemStack(rope));

			ActionResult<ItemStack> result = rope.onItemRightClick(c.world, c.player, Hand.MAIN_HAND);

			c.check(result.getType() == ActionResultType.FAIL, "use result with sky overhead should be FAIL but was " + result.getType() + " (pos " + c.player.getPosition() + ", canBlockSeeSky " + c.world.canBlockSeeSky(c.player.getPosition()) + ", canSeeSky " + c.world.canSeeSky(c.player.getPosition()) + ", empty " + c.world.isAirBlock(c.player.getPosition()) + ", hasSkyLight " + c.world.getDimensionType().hasSkyLight() + ")");
			c.check(!c.player.isHandActive(), "the player must not start charging the rope under open sky");
			c.eq(0, ropeTasks().size(), "no search task may be queued");
			release(c);
		}));

		tests.add(RTTest.named("use_item.escape_rope.charges_then_teleports_out_through_the_doorway_and_costs_one_durability").run(c -> {
			resetRopeTasks();
			ensureConnection(c);
			ropeRoom(c, true);
		}).waitUntil("the room under the roof to lose its sky light (the old lighting engine sometimes never does; the test is skipped then)", c -> roomShaded(c, true), 300, 20).skipIfNeverMet().run(c -> {
			standAt(c, 0, 1, 0);
			hold(c, new ItemStack(rope));

			ActionResult<ItemStack> result = rope.onItemRightClick(c.world, c.player, Hand.MAIN_HAND);

			c.check(result.getType() == ActionResultType.SUCCESS, "use inside a roofed room should succeed but was " + result.getType() + " (" + sky(c) + ")");
			c.check(c.player.isHandActive(), "the player must be charging the rope");
			c.eq(1, ropeTasks().size(), "one search task queued");
		}).waitTicks(120).run(c -> {
			double dx = c.player.getPosX() - (c.origin.getX() + 4.5);
			double dz = c.player.getPosZ() - (c.origin.getZ() + 0.5);

			c.check(Math.abs(dx) < 0.01 && Math.abs(dz) < 0.01, "must land just outside the doorway at x+4.5, z+0.5; was " + c.player.getPosX() + ", " + c.player.getPosZ() + " (origin " + c.origin + ")");
			c.eq((double) c.origin.getY() + 1, c.player.getPosY(), "landing height (on top of the floor)");
			c.check(!c.player.isHandActive(), "using the rope must have stopped after teleporting");
			c.eq(1, c.player.getHeldItemMainhand().getDamage(), "one durability used");
			c.eq(0, ropeTasks().size(), "the finished search task must be removed");
			release(c);
		}));

		tests.add(RTTest.named("use_item.escape_rope.letting_go_before_the_search_finishes_cancels_the_teleport").run(c -> {
			resetRopeTasks();
			ensureConnection(c);
			ropeRoom(c, true);
		}).waitUntil("the room under the roof to lose its sky light (the old lighting engine sometimes never does; the test is skipped then)", c -> roomShaded(c, true), 300, 20).skipIfNeverMet().run(c -> {
			standAt(c, 0, 1, 0);
			hold(c, new ItemStack(rope));
			rope.onItemRightClick(c.world, c.player, Hand.MAIN_HAND);
			c.check(c.player.isHandActive(), "precondition: charging");
			c.player.stopActiveHand();
		}).waitTicks(60).run(c -> {
			c.check(Math.abs(c.player.getPosX() - (c.origin.getX() + 0.5)) < 0.01 && Math.abs(c.player.getPosZ() - (c.origin.getZ() + 0.5)) < 0.01, "a released rope must not move the player; was at " + c.player.getPosX() + ", " + c.player.getPosZ());
			c.eq(0, c.player.getHeldItemMainhand().getDamage(), "an aborted use costs no durability");
			release(c);
			resetRopeTasks();
		}));

		tests.add(RTTest.named("use_item.escape_rope.a_sealed_room_has_no_way_out_so_the_rope_is_dropped").run(c -> {
			resetRopeTasks();
			ensureConnection(c);
			ropeRoom(c, false);
		}).waitUntil("the room under the roof to lose its sky light (the old lighting engine sometimes never does; the test is skipped then)", c -> roomShaded(c, false), 300, 20).skipIfNeverMet().run(c -> {
			standAt(c, 0, 1, 0);
			hold(c, new ItemStack(rope));
			rope.onItemRightClick(c.world, c.player, Hand.MAIN_HAND);
			c.check(c.player.isHandActive(), "precondition: charging");
		}).waitTicks(120).run(c -> {
			c.check(Math.abs(c.player.getPosX() - (c.origin.getX() + 0.5)) < 0.01 && Math.abs(c.player.getPosZ() - (c.origin.getZ() + 0.5)) < 0.01, "no exit means no teleport; was at " + c.player.getPosX() + ", " + c.player.getPosZ());
			c.check(c.player.getHeldItemMainhand().isEmpty(), "the failed rope leaves the hand");
			boolean dropped = false;

			for (net.minecraft.entity.item.ItemEntity e : c.itemEntities())
			{
				dropped |= e.getItem().getItem() == rope;
			}

			c.check(dropped, "the failed rope must be dropped as an item");
			c.eq(0, ropeTasks().size(), "the failed search task must be removed");
			release(c);
		}));

		// ---- Escape Rope search limit: a tunnel far longer than the limit ------------------------
		final String tunnelWait = "the tunnel to be dark except its mouth (the old lighting engine sometimes never settles; the test is skipped then)";
		tests.add(RTTest.named("use_item.escape_rope.gives_up_when_daylight_is_further_than_the_search_limit").run(c -> {
			resetRopeTasks();
			ensureConnection(c);
			buildTunnel(c);
		}).waitUntil(tunnelWait, UseItemTests::tunnelReady, 300, 20).skipIfNeverMet().run(c -> {
			standAt(c, -6, 1, -6);
			hold(c, new ItemStack(rope));
			lumien.randomthings.util.EscapeRopeHandler.maxChecked = 40;
			rope.onItemRightClick(c.world, c.player, Hand.MAIN_HAND);
			c.check(c.player.isHandActive(), "precondition: charging");
		}).waitTicks(60).run(c -> {
			lumien.randomthings.util.EscapeRopeHandler.maxChecked = lumien.randomthings.util.EscapeRopeHandler.DEFAULT_MAX_CHECKED;
			c.check(Math.abs(c.player.getPosX() - (c.origin.getX() - 5.5)) < 0.01 && Math.abs(c.player.getPosZ() - (c.origin.getZ() - 5.5)) < 0.01, "a search that hits its limit must not teleport; was at " + c.player.getPosX() + ", " + c.player.getPosZ());
			c.check(c.player.getHeldItemMainhand().isEmpty(), "the abandoned rope leaves the hand");
			boolean dropped = false;

			for (net.minecraft.entity.item.ItemEntity e : c.itemEntities())
			{
				dropped |= e.getItem().getItem() == rope;
			}

			c.check(dropped, "the rope must be dropped when the search gives up");
			c.eq(0, ropeTasks().size(), "the abandoned search task must be removed");
			release(c);
		}));

		tests.add(RTTest.named("use_item.escape_rope.follows_a_long_tunnel_to_daylight_when_the_limit_allows_it").run(c -> {
			resetRopeTasks();
			ensureConnection(c);
			buildTunnel(c);
		}).waitUntil(tunnelWait, UseItemTests::tunnelReady, 300, 20).skipIfNeverMet().run(c -> {
			standAt(c, -6, 1, -6);
			hold(c, new ItemStack(rope));
			rope.onItemRightClick(c.world, c.player, Hand.MAIN_HAND);
			c.check(c.player.isHandActive(), "precondition: charging");
		}).waitTicks(200).run(c -> {
			c.check(Math.abs(c.player.getPosX() - (c.origin.getX() + 6.5)) < 0.01 && Math.abs(c.player.getPosZ() - (c.origin.getZ() + 6.5)) < 0.01, "the rope must lead out through the whole tunnel to its mouth; was at " + c.player.getPosX() + ", " + c.player.getPosZ());
			c.eq(1, c.player.getHeldItemMainhand().getDamage(), "one durability used");
			c.eq(0, ropeTasks().size(), "the finished search task must be removed");
			release(c);
		}));

		tests.add(RTTest.named("use_item.escape_rope.the_twentieth_successful_use_breaks_it").run(c -> {
			resetRopeTasks();
			ensureConnection(c);
			c.eq(20, rope.getMaxDamage(), "durability");
			ropeRoom(c, true);
		}).waitUntil("the room under the roof to lose its sky light (the old lighting engine sometimes never does; the test is skipped then)", c -> roomShaded(c, true), 300, 20).skipIfNeverMet().run(c -> {
			standAt(c, 0, 1, 0);
			ItemStack worn = new ItemStack(rope);
			worn.setDamage(19);
			hold(c, worn);
			rope.onItemRightClick(c.world, c.player, Hand.MAIN_HAND);
		}).waitTicks(120).run(c -> {
			c.check(Math.abs(c.player.getPosX() - (c.origin.getX() + 4.5)) < 0.01, "the last use still teleports; x was " + c.player.getPosX());
			c.check(c.player.getHeldItemMainhand().isEmpty(), "the rope must break after its 20th use");
			release(c);
		}));

		// ---- Blaze and Steel --------------------------------------------------------------------
		tests.add(RTTest.named("use_item.blaze_and_steel.lights_blazing_fire_on_the_clicked_face_and_wears_down").run(c -> {
			c.floor();
			hold(c, new ItemStack(ModItems.BLAZE_AND_STEEL));

			ActionResultType result = useOn(c, c.at(2, 0, 2), Direction.UP);

			c.eq(ActionResultType.SUCCESS, result, "use result");
			c.eq(ModBlocks.BLAZING_FIRE, c.world.getBlockState(c.at(2, 1, 2)).getBlock(), "block above the clicked stone");
			c.eq(1, c.player.getHeldItemMainhand().getDamage(), "durability used");
			release(c);
		}));

		// ---- Beans ------------------------------------------------------------------------------
		tests.add(RTTest.named("use_item.beans.plant_only_on_top_of_soil_and_use_up_one_bean").run(c -> {
			c.floor();
			c.setQuiet(c.at(2, 0, 2), Blocks.FARMLAND.getDefaultState());
			hold(c, new ItemStack(ModItems.BEANS, 3));

			c.eq(ActionResultType.FAIL, useOn(c, c.at(2, 0, 2), Direction.NORTH), "clicking the side of the soil");
			c.eq(3, c.player.getHeldItemMainhand().getCount(), "a refused bean is not consumed");
			c.check(c.world.isAirBlock(c.at(2, 1, 2)) && c.world.isAirBlock(c.at(2, 1, 1)), "nothing planted by a side click");

			c.eq(ActionResultType.SUCCESS, useOn(c, c.at(2, 0, 2), Direction.UP), "clicking the top of farmland");
			c.eq(ModBlocks.BEAN_SPROUT, c.world.getBlockState(c.at(2, 1, 2)).getBlock(), "planted block");
			c.eq(2, c.player.getHeldItemMainhand().getCount(), "one bean used");

			c.eq(ActionResultType.FAIL, useOn(c, c.at(2, 0, 2), Direction.UP), "planting into an occupied space");
			c.eq(2, c.player.getHeldItemMainhand().getCount(), "no bean used on the occupied space");
			release(c);
		}));

		// ---- Lotus ------------------------------------------------------------------------------
		tests.add(RTTest.named("use_item.lotus_seeds.plant_on_grass_but_not_on_stone").run(c -> {
			c.floor();
			c.setQuiet(c.at(2, 0, 2), Blocks.GRASS_BLOCK.getDefaultState());
			hold(c, new ItemStack(ModItems.LOTUS_SEEDS, 2));

			c.eq(ActionResultType.FAIL, useOn(c, c.at(-2, 0, -2), Direction.UP), "seeds on stone");
			c.check(c.world.isAirBlock(c.at(-2, 1, -2)), "nothing planted on stone");
			c.eq(2, c.player.getHeldItemMainhand().getCount(), "seeds kept after the refusal");

			c.eq(ActionResultType.SUCCESS, useOn(c, c.at(2, 0, 2), Direction.UP), "seeds on grass");
			c.eq(ModBlocks.LOTUS, c.world.getBlockState(c.at(2, 1, 2)).getBlock(), "planted block");
			c.eq(1, c.player.getHeldItemMainhand().getCount(), "one seed used");
			release(c);
		}));

		tests.add(RTTest.named("use_item.lotus_blossom.eating_it_drops_three_to_eleven_experience").run(c -> {
			standAt(c, 0, 1, 0);
			c.floor();
			ItemStack stack = new ItemStack(ModItems.LOTUS_BLOSSOM, 2);
			hold(c, stack);

			ActionResult<ItemStack> result = stack.getItem().onItemRightClick(c.world, c.player, Hand.MAIN_HAND);

			c.eq(ActionResultType.SUCCESS, result.getType(), "use result");
			c.check(c.player.isHandActive(), "eating starts");
			stack.getItem().onItemUseFinish(stack, c.world, c.player);

			int xp = 0;

			for (ExperienceOrbEntity orb : c.world.getEntitiesWithinAABB(ExperienceOrbEntity.class, c.area()))
			{
				xp += orb.getXpValue();
			}

			c.check(xp >= 3 && xp <= 11, "experience dropped should be 3..11, was " + xp);
			c.eq(1, stack.getCount(), "one blossom eaten");
			release(c);
		}));

		// ---- Grass seeds ------------------------------------------------------------------------
		tests.add(RTTest.named("use_item.grass_seeds.turn_dirt_into_grass_and_the_dyed_ones_into_coloured_grass").run(c -> {
			c.floor();
			c.setQuiet(c.at(2, 1, 2), Blocks.DIRT.getDefaultState());
			c.setQuiet(c.at(-2, 1, -2), Blocks.DIRT.getDefaultState());
			hold(c, new ItemStack(item("grass_seeds"), 2));

			c.eq(ActionResultType.FAIL, useOn(c, c.at(1, 0, 1), Direction.UP), "seeds on stone");
			c.eq(2, c.player.getHeldItemMainhand().getCount(), "refused seeds are kept");
			c.eq(ActionResultType.SUCCESS, useOn(c, c.at(2, 1, 2), Direction.UP), "seeds on dirt");
			c.eq(Blocks.GRASS_BLOCK, c.world.getBlockState(c.at(2, 1, 2)).getBlock(), "dirt after plain seeds");
			c.eq(1, c.player.getHeldItemMainhand().getCount(), "one seed used");

			hold(c, new ItemStack(item("grass_seeds_red")));
			c.eq(ActionResultType.SUCCESS, useOn(c, c.at(-2, 1, -2), Direction.UP), "red seeds on dirt");
			c.eq(ModBlocks.COLORED_GRASS, c.world.getBlockState(c.at(-2, 1, -2)).getBlock(), "dirt after red seeds");
			c.eq(DyeColor.RED, c.world.getBlockState(c.at(-2, 1, -2)).get(ColoredGrassBlock.COLOR), "grass colour");
			release(c);
		}));

		// ---- Bottle of Air ----------------------------------------------------------------------
		tests.add(RTTest.named("use_item.bottle_of_air.needs_water_to_start_and_refills_breath_when_finished").run(c -> {
			standAt(c, 0, 1, 0);
			c.floor();
			ItemStack stack = new ItemStack(ModItems.BOTTLE_OF_AIR, 2);
			hold(c, stack);

			c.eq(ActionResultType.FAIL, stack.getItem().onItemRightClick(c.world, c.player, Hand.MAIN_HAND).getType(), "use out of water");
			c.check(!c.player.isHandActive(), "no drinking out of water");

			c.player.setAir(0);
			stack.getItem().onItemUseFinish(stack, c.world, c.player);

			c.eq(c.player.getMaxAir(), c.player.getAir(), "air supply after drinking");
			c.eq(1, stack.getCount(), "one bottle used");
			release(c);
		}));

		// ---- Thrown eggs ------------------------------------------------------------------------
		tests.add(RTTest.named("use_item.golden_egg.throwing_it_spawns_the_projectile_and_uses_one_egg").run(c -> {
			standAt(c, 0, 1, 0);
			c.floor();
			ItemStack stack = new ItemStack(ModItems.GOLDEN_EGG, 2);
			hold(c, stack);

			c.eq(ActionResultType.SUCCESS, stack.getItem().onItemRightClick(c.world, c.player, Hand.MAIN_HAND).getType(), "use result");
			c.eq(1, stack.getCount(), "one egg thrown");
			c.eq(1, c.world.getEntitiesWithinAABB(ThrownGoldenEggEntity.class, c.area()).size(), "thrown egg entities");
			release(c);
		}));

		tests.add(RTTest.named("use_item.weather_egg.throwing_it_spawns_the_projectile_and_uses_one_egg").run(c -> {
			standAt(c, 0, 1, 0);
			c.floor();
			ItemStack stack = new ItemStack(ModItems.WEATHER_EGG_RAIN, 2);
			hold(c, stack);

			c.eq(ActionResultType.SUCCESS, stack.getItem().onItemRightClick(c.world, c.player, Hand.MAIN_HAND).getType(), "use result");
			c.eq(1, stack.getCount(), "one egg thrown");
			c.eq(1, c.world.getEntitiesWithinAABB(ThrownWeatherEggEntity.class, c.area()).size(), "thrown egg entities");
			release(c);
		}));

		// ---- Eclipsed Clock ---------------------------------------------------------------------
		tests.add(RTTest.named("use_item.eclipsed_clock.hangs_on_a_wall_but_not_on_a_floor").run(c -> {
			c.floor();
			c.setQuiet(c.at(0, 2, 0), Blocks.STONE.getDefaultState());
			hold(c, new ItemStack(ModItems.ECLIPSED_CLOCK, 2));

			c.eq(ActionResultType.FAIL, useOn(c, c.at(2, 0, 2), Direction.UP), "clicking a floor");
			c.eq(2, c.player.getHeldItemMainhand().getCount(), "nothing used on a floor");
			c.eq(0, c.world.getEntitiesWithinAABB(EclipsedClockEntity.class, c.area()).size(), "no clock entity from a floor click");

			c.eq(ActionResultType.SUCCESS, useOn(c, c.at(0, 2, 0), Direction.SOUTH), "clicking the south face of a wall block");
			c.eq(1, c.world.getEntitiesWithinAABB(EclipsedClockEntity.class, c.area()).size(), "clock entities on the wall");
			c.eq(1, c.player.getHeldItemMainhand().getCount(), "one clock hung");
			release(c);
		}));

		return tests;
	}
}
