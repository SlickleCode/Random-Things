package lumien.randomthings.client;

import lumien.randomthings.debug.RTDebug;
import lumien.randomthings.debug.RTDebugCommand;
import lumien.randomthings.debug.test.RTTestRunner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.FlatGenerationSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameRules;
import net.minecraft.util.datafix.codec.DatapackCodec;

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
	public static void onTick(TickEvent.ClientTickEvent event)
	{
		if (!enabled() || event.phase != TickEvent.Phase.END)
		{
			return;
		}

		Minecraft mc = Minecraft.getInstance();
		ticks++;

		if (!MANUAL && !Boolean.getBoolean("randomthings.autotest.sound"))
		{
			// runtime only (the saved options are untouched): the unattended run is otherwise noisy
			mc.getSoundHandler().setSoundLevel(net.minecraft.util.SoundCategory.MASTER, 0.0F);
		}

		switch (state)
		{
			case MENU:
				// an unattended run loses window focus; vanilla would then open the pause menu over every screenshot
				mc.gameSettings.pauseOnLostFocus = false;
				if (mc.currentScreen instanceof MainMenuScreen && ticks > 60)
				{
					RTDebug.log("AutoTest: opening world {}", WORLD);

					if (FIXTURE.equals("build"))
					{
						deleteMuseum(net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get().resolve("saves").resolve(WORLD));      // a fixture is always built into a fresh world
					}

					if (MANUAL)
					{
						// the museum is rebuilt from scratch on every start and removed again when the game closes
						java.nio.file.Path museum = net.minecraftforge.fml.loading.FMLPaths.GAMEDIR.get().resolve("saves").resolve(WORLD);
						deleteMuseum(museum);
						Runtime.getRuntime().addShutdownHook(new Thread(() -> deleteMuseum(museum)));
					}

					if (mc.getSaveLoader().canLoadWorld(WORLD))
					{
						mc.loadWorld(WORLD);
					}
					else
					{
						// a flat creative world: the behavior tests wipe and rebuild their test area
						DynamicRegistries.Impl registries = DynamicRegistries.func_239770_b_();
						Registry<DimensionType> types = registries.getRegistry(Registry.DIMENSION_TYPE_KEY);
						Registry<Biome> biomes = registries.getRegistry(Registry.BIOME_KEY);
						Registry<DimensionSettings> noiseSettings = registries.getRegistry(Registry.NOISE_SETTINGS_KEY);
						ChunkGenerator flat = new FlatChunkGenerator(FlatGenerationSettings.func_242869_a(biomes));
						DimensionGeneratorSettings generatorSettings = (MANUAL || !FIXTURE.isEmpty()) ? new DimensionGeneratorSettings(424242L, true, false, DimensionGeneratorSettings.func_242749_a(types, DimensionType.getDefaultSimpleRegistry(types, biomes, noiseSettings, 424242L), DimensionGeneratorSettings.func_242750_a(biomes, noiseSettings, 424242L))) : new DimensionGeneratorSettings(4242L, false, false, DimensionGeneratorSettings.func_242749_a(types, DimensionType.getDefaultSimpleRegistry(types, biomes, noiseSettings, 4242L), flat));
						WorldSettings settings = new WorldSettings(WORLD, GameType.CREATIVE, false, Difficulty.NORMAL, true, new GameRules(), DatapackCodec.VANILLA_CODEC);
						mc.createWorld(WORLD, settings, registries, generatorSettings);
					}

					state = State.LOADING;
					ticks = 0;
				}
				break;
			case LOADING:
				if (mc.currentScreen instanceof net.minecraft.client.gui.screen.ConfirmScreen && ticks % 40 == 20)
				{
					// 1.16 asks before opening some worlds (e.g. experimental settings); the autotest always accepts
					RTDebug.log("AutoTest: confirming dialog: {}", mc.currentScreen.getNarrationMessage());

					for (net.minecraft.client.gui.IGuiEventListener listener : mc.currentScreen.getEventListeners())
					{
						if (listener instanceof net.minecraft.client.gui.widget.button.Button)
						{
							((net.minecraft.client.gui.widget.button.Button) listener).onPress();
							break;
						}
					}
				}

				if (mc.currentScreen instanceof net.minecraft.client.gui.screen.ConfirmBackupScreen && ticks % 40 == 20)
				{
					// "this world uses experimental settings" on reopening: choose "load without backup" (second button)
					RTDebug.log("AutoTest: confirming backup dialog");

					int index = 0;

					for (net.minecraft.client.gui.IGuiEventListener listener : mc.currentScreen.getEventListeners())
					{
						if (listener instanceof net.minecraft.client.gui.widget.button.Button && index++ == 1)
						{
							((net.minecraft.client.gui.widget.button.Button) listener).onPress();
							break;
						}
					}
				}

				if (ticks % 200 == 0)
				{
					RTDebug.log("AutoTest: still loading, screen={}, server={}", mc.currentScreen, mc.getIntegratedServer());
				}
				if (mc.player != null && mc.world != null)
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
						mc.player.sendChatMessage("/gamemode creative");
						mc.player.sendChatMessage("/rtdebug fixture " + FIXTURE);
					}
					else if (ticks > 260)
					{
						// the anchors need a few ticks to register, then the world is saved by quitting
						state = State.DONE;
						mc.shutdown();
					}
					break;
				}

				if (MANUAL)
				{
					if (ticks > 100)
					{
						mc.player.sendChatMessage("/gamemode creative");
						mc.player.sendChatMessage("/rtdebug manual");
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
					mc.player.sendChatMessage("/gamemode creative");
					mc.player.sendChatMessage("/rtdebug test " + System.getProperty("randomthings.autotest.filter", "").trim());

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
							mc.shutdown();
						}
						break;
					}

					RTDebug.log("AutoTest: running /rtdebug allguis");
					mc.player.sendChatMessage("/rtdebug allguis 40");

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
						mc.shutdown();
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
						mc.shutdown();
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
						mc.shutdown();
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
