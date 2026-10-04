package lumien.randomthings.client;

import lumien.randomthings.debug.RTDebug;
import lumien.randomthings.debug.RTDebugCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraft.world.GameType;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Hands-free GUI pass for dev runs: when started with {@code -Drandomthings.autotest=true} (the
 * gradle {@code runClient} config sets it from the {@code RT_AUTOTEST} environment variable) the
 * client opens - creating it on first use - a creative flat world called "RTAutoTest", waits for it
 * to settle, then runs {@code /rtdebug allguis}. Combined with the GUI screenshot option this yields
 * a screenshot of every GUI with nobody touching the game. {@code -Drandomthings.autotest.exit=true}
 * also quits the client when the run is finished.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class AutoTest
{
	private static final String WORLD = "RTAutoTest";

	private enum State
	{
		MENU, LOADING, SETTLING, RUNNING, DONE
	}

	private static State state = State.MENU;
	private static int ticks;

	private static boolean enabled()
	{
		return Boolean.getBoolean("randomthings.autotest");
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

		switch (state)
		{
			case MENU:
				if (mc.currentScreen instanceof MainMenuScreen && ticks > 60)
				{
					RTDebug.log("AutoTest: opening world {}", WORLD);

					WorldSettings settings = mc.getSaveLoader().canLoadWorld(WORLD) ? null : new WorldSettings(4242L, GameType.CREATIVE, false, false, WorldType.FLAT).enableCommands();
					mc.launchIntegratedServer(WORLD, WORLD, settings);

					state = State.LOADING;
					ticks = 0;
				}
				break;
			case LOADING:
				if (mc.player != null && mc.world != null)
				{
					state = State.SETTLING;
					ticks = 0;
				}
				break;
			case SETTLING:
				if (ticks > 200)
				{
					RTDebug.log("AutoTest: world loaded, running /rtdebug allguis");
					mc.player.sendChatMessage("/gamemode creative");
					mc.player.sendChatMessage("/rtdebug allguis");

					state = State.RUNNING;
					ticks = 0;
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
