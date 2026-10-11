package lumien.randomthings.debug.test;

import lumien.randomthings.debug.RTDebug;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/**
 * Hands-free test run on a dedicated server ({@code RT_AUTOTEST=true ./gradlew runServer}): once the world is up,
 * starts the in-game test suite with a fake player and, with {@code randomthings.autotest.exit=true}, stops the
 * server when it is done. The world must be flat (the gradle task prepares {@code server.properties}). Developer-only.
 */
public final class ServerAutoTest
{
	private static final int WARMUP_TICKS = 100;

	private static int ticks;
	private static boolean started;
	private static boolean stopped;

	private ServerAutoTest()
	{
	}

	public static void init()
	{
		if (FMLEnvironment.dist == Dist.DEDICATED_SERVER && Boolean.getBoolean("randomthings.autotest"))
		{
			MinecraftForge.EVENT_BUS.addListener(ServerAutoTest::onTick);
		}
	}

	private static void onTick(TickEvent.ServerTickEvent event)
	{
		if (event.phase != TickEvent.Phase.END || stopped)
		{
			return;
		}

		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

		if (!started)
		{
			if (++ticks < WARMUP_TICKS)
			{
				return;
			}

			ServerWorld world = server.getWorld(DimensionType.OVERWORLD);
			ServerPlayerEntity fake = FakePlayerFactory.getMinecraft(world);
			BlockPos spawn = world.getSpawnPoint();
			fake.setPosition(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5);

			// A world with no real player and no forced chunk stops ticking entities and tile entities after 300 ticks
			// (vanilla idle timeout), which silently starves long tests; force the chunks around the test area.
			int cx = (spawn.getX() + 14) >> 4;
			int cz = spawn.getZ() >> 4;

			for (int dx = -2; dx <= 2; dx++)
			{
				for (int dz = -2; dz <= 2; dz++)
				{
					world.forceChunk(cx + dx, cz + dz, true);
				}
			}

			String error = RTTestRunner.start(fake, System.getProperty("randomthings.autotest.filter", "").trim());
			started = true;

			if (error != null)
			{
				RTDebug.log("Server autotest: not started: {}", error);
				stop(server);
			}

			return;
		}

		if (RTTestRunner.finished)
		{
			stop(server);
		}
	}

	private static void stop(MinecraftServer server)
	{
		stopped = true;

		if (Boolean.getBoolean("randomthings.autotest.exit"))
		{
			server.initiateShutdown(false);
		}
	}
}
