package lumien.randomthings.client;

import java.util.ArrayDeque;
import java.util.Deque;

import lumien.randomthings.debug.RTDebug;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Debug aid (needs {@code DebugLogging}): remembers the last few key presses, mouse clicks and
 * world interactions (right-click block/item/entity) so that when a Random Things GUI opens,
 * {@link GuiDebugScreenshots} can log what the player actually did to open it.
 */
public class GuiOpenTriggerLog
{
	private static final long WINDOW_MS = 3000;
	private static final int MAX_ENTRIES = 5;

	private static final Deque<long[]> times = new ArrayDeque<>();
	private static final Deque<String> inputs = new ArrayDeque<>();

	private static synchronized void record(String what)
	{
		if (!RTDebug.isLogging())
		{
			return;
		}

		times.addLast(new long[] { System.currentTimeMillis() });
		inputs.addLast(what);

		while (inputs.size() > MAX_ENTRIES)
		{
			times.removeFirst();
			inputs.removeFirst();
		}
	}

	/** Inputs from the last few seconds, oldest first, as a log-friendly string. */
	public static synchronized String recentInputs()
	{
		long now = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		java.util.Iterator<long[]> t = times.iterator();

		for (String input : inputs)
		{
			long age = now - t.next()[0];

			if (age <= WINDOW_MS)
			{
				sb.append(sb.length() > 0 ? "; " : "").append(input).append(" (").append(age).append("ms ago)");
			}
		}

		return sb.length() == 0 ? "no recent input recorded (opened by a server/other trigger?)" : sb.toString();
	}

	private static String button(int button)
	{
		return button == 0 ? "left click" : button == 1 ? "right click" : button == 2 ? "middle click" : "mouse button " + button;
	}

	@SubscribeEvent
	public static void onKey(InputEvent.KeyInputEvent event)
	{
		if (event.getAction() == 1)
		{
			record("key " + InputConstants.getKey(event.getKey(), event.getScanCode()).getName());
		}
	}

	@SubscribeEvent
	public static void onMouse(InputEvent.MouseInputEvent event)
	{
		if (event.getAction() == 1)
		{
			record(button(event.getButton()) + " (in world)");
		}
	}

	@SubscribeEvent
	public static void onGuiClick(ScreenEvent.MouseClickedEvent.Pre event)
	{
		record(button(event.getButton()) + " in " + event.getScreen().getClass().getSimpleName() + " at " + (int) event.getMouseX() + "," + (int) event.getMouseY());
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
	{
		if (event.getWorld().isClientSide)
		{
			record("interact block " + event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName() + " at " + event.getPos().getX() + "," + event.getPos().getY() + "," + event.getPos().getZ() + " face " + event.getFace() + " with " + event.getItemStack().getItem().getRegistryName() + " in " + event.getHand());
		}
	}

	@SubscribeEvent
	public static void onRightClickItem(PlayerInteractEvent.RightClickItem event)
	{
		if (event.getWorld().isClientSide)
		{
			record("use item " + event.getItemStack().getItem().getRegistryName() + " in " + event.getHand());
		}
	}

	@SubscribeEvent
	public static void onEntityInteract(PlayerInteractEvent.EntityInteract event)
	{
		if (event.getWorld().isClientSide)
		{
			record("interact entity " + event.getTarget().getType().getRegistryName() + " with " + event.getItemStack().getItem().getRegistryName() + " in " + event.getHand());
		}
	}
}
