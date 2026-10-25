package lumien.randomthings.client;

import net.minecraftforge.event.TickEvent;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import lumien.randomthings.debug.RTDebug;
import net.minecraft.client.Minecraft;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Dev-only: lets the scripted playtest (docs/testing/desktop-helper) run chat commands without typing them key by key. While the game is running it keeps
 * an empty marker file {@code rt_cmd_ready} in the game directory; the playtest writes a command (without the slash) into {@code rt_cmd.txt}, this class
 * deletes the file on the next client tick and sends the command to the server exactly as the chat box would. Typing a 90-character command took about a
 * second of simulated key presses; this takes one tick. Registered from {@link DevToolsClient} only.
 */
public final class CommandChannel
{
	private static final String MARKER = "rt_cmd_ready";
	private static final String COMMAND = "rt_cmd.txt";

	private static boolean marked;

	private CommandChannel()
	{
	}

	@SubscribeEvent
	public static void onClientTick(TickEvent.ClientTickEvent event)
	{
		if (event.phase != TickEvent.Phase.END)
		{
			return;
		}

		Minecraft mc = Minecraft.getInstance();

		if (!marked)
		{
			try
			{
				new File(mc.gameDirectory, MARKER).createNewFile();
				new File(mc.gameDirectory, COMMAND).delete();
			}
			catch (java.io.IOException e)
			{
				return;
			}

			marked = true;
			Runtime.getRuntime().addShutdownHook(new Thread(() -> new File(mc.gameDirectory, MARKER).delete()));
		}

		if (mc.player == null || mc.player.connection == null)
		{
			return;
		}

		File file = new File(mc.gameDirectory, COMMAND);

		if (!file.exists())
		{
			return;
		}

		String text;

		try
		{
			text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8).trim();
		}
		catch (java.io.IOException e)
		{
			return;		// still being written: try again next tick
		}

		if (text.isEmpty() || !file.delete())
		{
			return;
		}

		if (text.startsWith("/"))
		{
			text = text.substring(1);
		}

		RTDebug.log("command channel: {}", text);
		mc.player.chat("/" + text);
	}
}
