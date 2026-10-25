package lumien.randomthings.debug.test;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

import lumien.randomthings.debug.RTDebug;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.common.NeoForge;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.minecraft.world.level.Level;

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
		if (FMLEnvironment.getDist() == Dist.DEDICATED_SERVER && Boolean.getBoolean("randomthings.autotest"))
		{
			NeoForge.EVENT_BUS.addListener(ServerAutoTest::onTick);
		}
	}

	private static void onTick(ServerTickEvent.Post event)
	{
		if (stopped)
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

			ServerLevel world = server.getLevel(Level.OVERWORLD);
			ServerPlayer fake = new RTTestPlayer(world);
			BlockPos spawn = world.getRespawnData().pos();
			fake.setPos(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5);

			// A world with no real player and no forced chunk stops ticking entities and tile entities after 300 ticks
			// (vanilla idle timeout), which silently starves long tests; force the chunks around the test area.
			int cx = (fake.blockPosition().getX() + 14) >> 4;
			int cz = fake.blockPosition().getZ() >> 4;

			for (int dx = -2; dx <= 2; dx++)
			{
				for (int dz = -2; dz <= 2; dz++)
				{
					world.setChunkForced(cx + dx, cz + dz, true);
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
			server.halt(false);
		}
	}
}
