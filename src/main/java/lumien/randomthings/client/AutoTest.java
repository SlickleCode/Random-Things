package lumien.randomthings.client;

import net.neoforged.neoforge.client.event.ClientTickEvent;

import net.minecraft.core.registries.Registries;

import lumien.randomthings.debug.RTDebug;
import lumien.randomthings.debug.RTDebugCommand;
import lumien.randomthings.debug.test.RTTestRunner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;

import net.neoforged.bus.api.SubscribeEvent;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.DataPackConfig;

/**
 * Hands-free GUI pass for dev runs: when started with {@code -Drandomthings.autotest=true} (the
 * gradle {@code runClient} config sets it from the {@code RT_AUTOTEST} environment variable) the
 * client opens - creating it on first use - a creative flat world called "RTAutoTest", waits for it
 * to settle, then runs {@code /rtdebug allguis}. Combined with the GUI screenshot option this yields
 * a screenshot of every GUI with nobody touching the game. {@code -Drandomthings.autotest.exit=true}
 * also quits the client when the run is finished.
 */
public class AutoTest
{
	/** {@code RT_MANUAL=true}: not a test run - open/create the normal-terrain manual-testing world and build the showroom ({@code /rtdebug manual}). */
	private static final boolean MANUAL = Boolean.getBoolean("randomthings.autotest.manual");
	/** {@code RT_FIXTURE=build|verify}: the old-world loading check, see {@code docs/testing/fixture-world.md}. */
	private static final String FIXTURE = System.getProperty("randomthings.autotest.fixture", "");
	private static final String WORLD = !FIXTURE.isEmpty() ? "RTFixture" : MANUAL ? "RTManual" : "RTAutoTest";

	private enum State
	{
		MENU, LOADING, SETTLING, TESTING, RUNNING, SCENES, CLICKS, DONE
	}

	private static State state = State.MENU;
	private static int ticks;

	private static boolean enabled()
	{
		return Boolean.getBoolean("randomthings.autotest");
	}

	private static void deleteMuseum(java.nio.file.Path dir)
	{
		if (!java.nio.file.Files.exists(dir))
		{
			return;
		}

		try (java.util.stream.Stream<java.nio.file.Path> files = java.nio.file.Files.walk(dir))
		{
			files.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
		}
		catch (java.io.IOException e)
		{
			RTDebug.log("AutoTest: could not delete the museum world: {}", e);
		}
	}

	@SubscribeEvent
	public static void onTick(ClientTickEvent.Post event)
	{
		if (!enabled())
		{
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		ticks++;

		if (!MANUAL && !Boolean.getBoolean("randomthings.autotest.sound"))
		{
			// runtime only (the saved options are untouched): the unattended run is otherwise noisy
			mc.options.getSoundSourceOptionInstance(net.minecraft.sounds.SoundSource.MASTER).set(0.0);
		}

		switch (state)
		{
			case MENU:
				// an unattended run loses window focus; vanilla would then open the pause menu over every screenshot
				mc.options.pauseOnLostFocus = false;
				if (mc.screen instanceof TitleScreen && ticks > 60)
				{
					RTDebug.log("AutoTest: opening world {}", WORLD);

					if (FIXTURE.equals("build"))
					{
						deleteMuseum(mc.getLevelSource().getBaseDir().resolve(WORLD));      // a fixture is always built into a fresh world
					}

					if (MANUAL)
					{
						// the museum is rebuilt from scratch on every start and removed again when the game closes
						deleteMuseum(mc.getLevelSource().getBaseDir().resolve(WORLD));
						Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteMuseum(mc.getLevelSource().getBaseDir().resolve(WORLD))));
					}

					if (mc.getLevelSource().levelExists(WORLD))
					{
						mc.createWorldOpenFlows().openWorld(WORLD, () -> mc.setScreen(new TitleScreen()));
					}
					else
					{
						// a flat creative world: the behavior tests wipe and rebuild their test area
						net.minecraft.world.level.levelgen.WorldOptions options = new net.minecraft.world.level.levelgen.WorldOptions(MANUAL ? 424242L : 4242L, MANUAL, false);
						LevelSettings settings = new LevelSettings(WORLD, GameType.CREATIVE, false, Difficulty.NORMAL, true, new GameRules(net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS), net.minecraft.world.level.WorldDataConfiguration.DEFAULT);
						mc.createWorldOpenFlows().createFreshLevel(WORLD, settings, options, registries -> {
							if (MANUAL)
							{
								return net.minecraft.world.level.levelgen.presets.WorldPresets.createNormalWorldDimensions(registries);
							}
							ChunkGenerator flat = new FlatLevelSource(FlatLevelGeneratorSettings.getDefault(registries.lookupOrThrow(Registries.BIOME), registries.lookupOrThrow(Registries.STRUCTURE_SET), registries.lookupOrThrow(Registries.PLACED_FEATURE)));
							return net.minecraft.world.level.levelgen.presets.WorldPresets.createNormalWorldDimensions(registries).replaceOverworldGenerator(registries, flat);
						}, mc.screen);
					}

					state = State.LOADING;
					ticks = 0;
				}
				break;
			case LOADING:
				if (mc.screen instanceof net.minecraft.client.gui.screens.ConfirmScreen && ticks % 40 == 20)
				{
					// 1.16 asks before opening some worlds (e.g. experimental settings); the autotest always accepts
					RTDebug.log("AutoTest: confirming dialog: {}", mc.screen.getNarrationMessage());

					for (net.minecraft.client.gui.components.events.GuiEventListener listener : mc.screen.children())
					{
						if (listener instanceof net.minecraft.client.gui.components.Button)
						{
							((net.minecraft.client.gui.components.Button) listener).onPress(new net.minecraft.client.input.MouseButtonEvent(0, 0, new net.minecraft.client.input.MouseButtonInfo(0, 0)));
							break;
						}
					}
				}

				if (mc.screen instanceof net.minecraft.client.gui.screens.BackupConfirmScreen && ticks % 40 == 20)
				{
					// "this world uses experimental settings" on reopening: choose "load without backup" (second button)
					RTDebug.log("AutoTest: confirming backup dialog");

					int index = 0;

					for (net.minecraft.client.gui.components.events.GuiEventListener listener : mc.screen.children())
					{
						if (listener instanceof net.minecraft.client.gui.components.Button && index++ == 1)
						{
							((net.minecraft.client.gui.components.Button) listener).onPress(new net.minecraft.client.input.MouseButtonEvent(0, 0, new net.minecraft.client.input.MouseButtonInfo(0, 0)));
							break;
						}
					}
				}

				if (ticks % 200 == 0)
				{
					RTDebug.log("AutoTest: still loading, screen={}, server={}", mc.screen, mc.getSingleplayerServer());
				}
				if (mc.player != null && mc.level != null)
				{
					state = State.SETTLING;
					ticks = 0;
				}
				break;
			case SETTLING:
				if (!FIXTURE.isEmpty())
				{
					if (ticks == 100)
					{
						mc.player.connection.sendCommand("gamemode creative");
						mc.player.connection.sendCommand("rtdebug fixture " + FIXTURE);
					}
					else if (ticks > 260)
					{
						// the anchors need a few ticks to register, then the world is saved by quitting
						state = State.DONE;
						mc.stop();
					}
					break;
				}

				if (MANUAL)
				{
					if (ticks > 100)
					{
						mc.player.connection.sendCommand("gamemode creative");
						mc.player.connection.sendCommand("rtdebug manual");
						state = State.DONE;
						ticks = 0;
					}
					break;
				}

				if (ticks > 200 && Boolean.getBoolean("randomthings.autotest.clicks"))
				{
					state = State.CLICKS;
					ticks = 0;
					break;
				}

				if (ticks > 200 && Boolean.getBoolean("randomthings.autotest.scenes"))
				{
					state = State.SCENES;
					ticks = 0;
					break;
				}

				if (ticks > 200)
				{
					ResourceAudit audit = ResourceAudit.run();
					RTDebug.log("AutoTest: resource audit done, {} failure(s)", audit.failures.size());

					RTDebug.log("AutoTest: running /rtdebug test");
					mc.player.connection.sendCommand("gamemode creative");
					mc.player.connection.sendCommand(("rtdebug test " + System.getProperty("randomthings.autotest.filter", "")).trim());

					state = State.TESTING;
					ticks = 0;
				}
				break;
			case TESTING:
				if (RTTestRunner.finished)
				{
					RTDebug.log("AutoTest: in-game tests finished, {} failure(s)", RTTestRunner.failureCount);

					if (Boolean.getBoolean("randomthings.autotest.noguis"))
					{
						state = State.DONE;

						if (Boolean.getBoolean("randomthings.autotest.exit"))
						{
							mc.stop();
						}
						break;
					}

					RTDebug.log("AutoTest: running /rtdebug allguis");
					mc.player.connection.sendCommand("rtdebug allguis 40");

					state = State.RUNNING;
					ticks = 0;
				}
				else if (ticks > 20 * 60 * 10)
				{
					RTDebug.log("AutoTest: timed out waiting for the in-game tests (is the player op and the world flat?)");
					state = State.DONE;
				}
				break;
			case CLICKS:
				if (GuiClickTest.tick(mc))
				{
					RTDebug.log("AutoTest: GUI click test finished");
					state = State.DONE;

					if (Boolean.getBoolean("randomthings.autotest.exit"))
					{
						mc.stop();
					}
				}
				break;
			case SCENES:
				if (SceneShots.tick(mc))
				{
					RTDebug.log("AutoTest: scenes finished");
					state = State.DONE;

					if (Boolean.getBoolean("randomthings.autotest.exit"))
					{
						mc.stop();
					}
				}
				break;
			case RUNNING:
				if (RTDebugCommand.allGuisFinished)
				{
					RTDebug.log("AutoTest: finished");
					state = State.DONE;

					if (Boolean.getBoolean("randomthings.autotest.exit"))
					{
						mc.stop();
					}
				}
				else if (ticks > 20 * 60 * 5)
				{
					RTDebug.log("AutoTest: timed out waiting for allguis (is the debug flag on and is the player op?)");
					state = State.DONE;
				}
				break;
			default:
				break;
		}
	}
}
