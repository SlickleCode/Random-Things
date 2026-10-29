package lumien.randomthings.debug.test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lumien.randomthings.debug.RTDebug;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.WorldType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;

/**
 * Runs the in-game test suite on the server thread, one step per tick (immediate steps are batched
 * within a tick). Started by {@code /rtdebug test [filter]}; developer-only (see
 * {@link lumien.randomthings.debug.DevMode}). Needs a flat world: the build area is wiped between tests.
 * Results: log lines ({@code [RT-DEBUG]}), chat, and {@code randomthings_tests.txt} in the game directory.
 */
public final class RTTestRunner
{
	private static final int MAX_STEPS_PER_TICK = 64;

	public static volatile boolean running;
	public static volatile boolean finished;
	public static volatile int failureCount;

	private static List<RTTest> queue;
	private static int testIndex;
	private static int stepIndex;
	private static int pollTicks;
	private static int stableCount;
	private static int wait;
	private static TestCtx ctx;
	private static ServerPlayerEntity player;
	private static boolean savedInstabuild;

	private static int passed;
	private static final List<String> failures = new ArrayList<>();
	private static final List<String> skipped = new ArrayList<>();

	private RTTestRunner()
	{
	}

	/** @return an error message, or null if the run started */
	public static String start(ServerPlayerEntity starter, String filter)
	{
		if (running)
		{
			return "tests already running";
		}

		ServerWorld world = starter.getServerWorld();

		if (world.getWorldType() != WorldType.FLAT)
		{
			return "refusing to run: the test area is wiped between tests, so use a flat world (e.g. the RTAutoTest world)";
		}

		controlWorld(world);

		List<RTTest> all = new ArrayList<>();
		all.addAll(BehaviorTests.create());
		all.addAll(MoreBehaviorTests.create());
		all.addAll(UtilityBehaviorTests.create());
		all.addAll(RemainingBehaviorTests.create());
		all.addAll(RetestBehaviorTests.create());
		all.addAll(ChecklistTests.create());
		all.addAll(CoremodTests.create());
		all.addAll(UseItemTests.create());
		all.addAll(WorldgenTests.create());
		all.addAll(RoundTripTests.create());

		String f = filter == null ? "" : filter.toLowerCase();
		queue = new ArrayList<>();

		for (RTTest test : all)
		{
			if (test.name.toLowerCase().contains(f))
			{
				queue.add(test);
			}
		}

		if (queue.isEmpty())
		{
			return "no tests match '" + filter + "'";
		}

		player = starter;
		// a real (creative) player in a client run would not use up items or durability; the dedicated-server runs use a survival fake player
		savedInstabuild = starter.abilities.isCreativeMode;
		starter.abilities.isCreativeMode = false;
		BlockPos base = starter.getPosition();
		ctx = new TestCtx(world, new BlockPos(base.getX() + 14, 100, base.getZ()), starter);
		ctx.clear();

		testIndex = 0;
		stepIndex = 0;
		wait = 0;
		passed = 0;
		failures.clear();
		skipped.clear();
		failureCount = 0;
		finished = false;
		running = true;
		return null;
	}

	public static int queued()
	{
		return queue == null ? 0 : queue.size();
	}

	/**
	 * Same controlled conditions the manual playtest scenarios use (prod-test/desktop/scn.py): always day, no weather cycle, no mob
	 * spawning, no stray mobs, so a test never depends on the time of day, rain or a wandering mob.
	 */
	private static void controlWorld(ServerWorld world)
	{
		net.minecraft.world.GameRules rules = world.getGameRules();
		rules.get(net.minecraft.world.GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
		rules.get(net.minecraft.world.GameRules.DO_WEATHER_CYCLE).set(false, world.getServer());
		rules.get(net.minecraft.world.GameRules.DO_MOB_SPAWNING).set(false, world.getServer());
		world.setDayTime(6000);
		world.getWorldInfo().setRaining(false);
		world.getWorldInfo().setThundering(false);

		for (net.minecraft.entity.Entity entity : world.getEntities().collect(java.util.stream.Collectors.toList()))
		{
			if (entity instanceof net.minecraft.entity.MobEntity)
			{
				entity.remove();
			}
		}
	}

	public static void onServerTick(TickEvent.ServerTickEvent event)
	{
		if (!running || event.phase != TickEvent.Phase.END)
		{
			return;
		}

		if (ctx != null && ctx.world.getServer() != net.minecraftforge.fml.server.ServerLifecycleHooks.getCurrentServer())
		{
			// the world this run started in was closed (client: Save and Quit); ticking its stale level from the next world's server blocks forever
			running = false;
			player.abilities.isCreativeMode = savedInstabuild;
			return;
		}

		if (wait > 0)
		{
			wait--;
			return;
		}

		for (int i = 0; i < MAX_STEPS_PER_TICK && running && wait == 0; i++)
		{
			step();
		}
	}

	private static void step()
	{
		if (testIndex >= queue.size())
		{
			finish();
			return;
		}

		RTTest test = queue.get(testIndex);
		RTTest.Step step = test.steps.get(stepIndex);

		try
		{
			if (step.until != null)
			{
				if (!step.until.met(ctx))
				{
					stableCount = 0;

					if (++pollTicks > step.maxTicks)
					{
						String message = "timed out waiting for " + step.what + " (" + step.maxTicks + " ticks)";

						if (step.skipOnTimeout)
						{
							throw new TestCtx.Skip(message);
						}

						throw new TestCtx.Failure(message);
					}

					wait = 1;
					return;
				}

				if (++stableCount < step.stableTicks)
				{
					pollTicks++;
					wait = 1;
					return;
				}

				pollTicks = 0;
				stableCount = 0;
			}

			step.body.run(ctx);
		}
		catch (TestCtx.Skip s)
		{
			skipped.add("SKIP " + test.name + ": " + s.getMessage());
			endTest();
			return;
		}
		catch (Throwable t)
		{
			failures.add("FAIL " + test.name + " (step " + (stepIndex + 1) + "): " + describe(t));
			endTest();
			return;
		}

		wait = step.waitAfter;
		stepIndex++;

		if (stepIndex >= test.steps.size())
		{
			passed++;
			endTest();
		}
	}

	private static String describe(Throwable t)
	{
		if (t instanceof TestCtx.Failure)
		{
			return t.getMessage();
		}

		StackTraceElement[] trace = t.getStackTrace();
		return t + (trace.length > 0 ? " at " + trace[0] : "");
	}

	private static void endTest()
	{
		try
		{
			ctx.clear();
		}
		catch (Throwable t)
		{
			RTDebug.log("Tests: cleanup failed: {}", t.toString());
		}

		testIndex++;
		stepIndex = 0;
		pollTicks = 0;
		stableCount = 0;
	}

	private static void finish()
	{
		running = false;

		if (player != null)
		{
			player.abilities.isCreativeMode = savedInstabuild;
		}
		failureCount = failures.size();

		List<String> lines = new ArrayList<>();
		lines.add("Random Things in-game tests: " + passed + " passed, " + failures.size() + " failed, " + skipped.size() + " skipped (of " + queue.size() + ")");
		lines.addAll(failures);
		lines.addAll(skipped);
		lines.forEach(l -> RTDebug.log("Tests: {}", l));

		try
		{
			Files.write(Paths.get("randomthings_tests.txt"), lines, StandardCharsets.UTF_8);
		}
		catch (Exception e)
		{
			RTDebug.log("Tests: could not write report: {}", e.toString());
		}

		if (player != null)
		{
			player.sendMessage(new StringTextComponent("[rtdebug] " + lines.get(0) + " - details in randomthings_tests.txt"));
		}

		finished = true;
	}
}
