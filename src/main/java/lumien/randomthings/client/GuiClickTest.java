package lumien.randomthings.client;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.mojang.blaze3d.vertex.PoseStack;

import lumien.randomthings.client.screen.MultilineTextFieldWidget;
import lumien.randomthings.client.screen.TooltipButton;
import lumien.randomthings.debug.RTDebug;
import lumien.randomthings.debug.RTDebugCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;

/**
 * Developer-only click-through of every Random Things GUI ({@code RT_AUTOTEST_CLICKS=true}). For each GUI it opens the
 * screen the way a player would, clicks every button through the screen's own {@code mouseClicked}, renders it with the
 * mouse over each button (so tooltips are drawn), types into every text field, closes the screen and reopens it to check
 * the text survived the round trip to the server. Server state (the block entity's NBT, or the held item's tag) is read
 * before and after every click so a button that does nothing is reported. Results go to {@code run/randomthings_clicks.txt}.
 */
public final class GuiClickTest
{
	private static final String TYPED = "Zq7";

	private enum Phase
	{
		START, NEXT, WAIT_OPEN, RUN_STEPS, DONE
	}

	private static Phase phase = Phase.START;
	private static final List<String> report = new ArrayList<>();
	private static final Deque<Step> steps = new ArrayDeque<>();
	private static String[][] guis;
	private static int index;
	private static int wait;
	private static int openTimeout;
	private static String currentName;
	private static String currentId;
	private static boolean currentSneak;
	private static boolean reopening;
	private static BlockPos statePos;
	private static int failures;
	private static int clicks;
	private static int silent;
	private static int tooltipRequests;
	private static int slotTooltips;

	private static final class Step
	{
		final int delay;
		final Runnable action;

		Step(int delay, Runnable action)
		{
			this.delay = delay;
			this.action = action;
		}
	}

	private GuiClickTest()
	{
	}

	/** @return true when every GUI has been tested */
	public static boolean tick(Minecraft mc)
	{
		switch (phase)
		{
			case START:
				guis = RTDebugCommand.guiList();
				mc.player.connection.sendUnsignedCommand("gamemode creative");
				// something in the player inventory, so every container GUI has a filled slot to hover
				mc.player.connection.sendUnsignedCommand("give @s minecraft:stone 16");
				net.minecraftforge.common.MinecraftForge.EVENT_BUS.addListener((net.minecraftforge.event.entity.player.ItemTooltipEvent event) -> tooltipRequests++);
				wait = 20;
				phase = Phase.NEXT;
				return false;
			case NEXT:
				if (wait-- > 0)
				{
					return false;
				}

				if (index >= guis.length)
				{
					finish();
					return true;
				}

				currentId = guis[index][0];
				currentSneak = "sneak".equals(guis[index][1]);
				currentName = currentId + (currentSneak ? " (sneak)" : "");
				index++;
				reopening = false;
				openGui(mc);
				return false;
			case WAIT_OPEN:
				if (isOurScreen(mc.screen))
				{
					statePos = mc.player.blockPosition().relative(mc.player.getDirection(), 3);

					if (!reopening)
					{
						plan(mc, mc.screen);
					}
					else
					{
						checkReopened(mc.screen);
					}

					phase = Phase.RUN_STEPS;
					wait = 5;
					return false;
				}

				if (openTimeout-- <= 0)
				{
					log("FAIL " + currentName + ": the GUI did not open");
					failures++;
					phase = Phase.NEXT;
					wait = 5;
				}

				return false;
			case RUN_STEPS:
				if (wait-- > 0)
				{
					return false;
				}

				Step step = steps.poll();

				if (step == null)
				{
					mc.player.closeContainer();
					phase = Phase.NEXT;
					wait = 30;
					return false;
				}

				try
				{
					step.action.run();
				}
				catch (Throwable t)
				{
					log("FAIL " + currentName + ": exception " + t);
					failures++;
					RTDebug.log("GuiClickTest exception in {}", currentName, t);
				}

				wait = step.delay;

				if (phase == Phase.WAIT_OPEN)
				{
					return false;
				}

				return false;
			default:
				return true;
		}
	}

	private static boolean isOurScreen(Screen screen)
	{
		return screen != null && screen.getClass().getName().startsWith("lumien.randomthings.");
	}

	private static void openGui(Minecraft mc)
	{
		mc.player.closeContainer();
		mc.player.connection.sendUnsignedCommand("rtdebug gui " + currentId + (currentSneak ? " sneak" : ""));
		phase = Phase.WAIT_OPEN;
		openTimeout = 80;
	}

	// ---- planning ---------------------------------------------------------------------------

	private static void plan(Minecraft mc, Screen screen)
	{
		steps.clear();
		List<AbstractWidget> widgets = new ArrayList<>();

		for (GuiEventListener listener : screen.children())
		{
			if (listener instanceof AbstractWidget)
			{
				widgets.add((AbstractWidget) listener);
			}
		}

		StringBuilder summary = new StringBuilder(currentName + " (" + screen.getClass().getSimpleName() + "): " + widgets.size() + " widget(s)");
		List<TextFieldRef> fields = new ArrayList<>();

		for (AbstractWidget widget : widgets)
		{
			if (widget instanceof Button)
			{
				Button button = (Button) widget;
				String label = describe(button);
				summary.append(" [").append(label).append("]");

				// hover (tooltip) render, then the click, then compare server state
				steps.add(new Step(2, () -> render(screen, center(button))));
				String[] before = new String[1];
				steps.add(new Step(0, () -> before[0] = state(mc, screen)));
				steps.add(new Step(10, () -> click(screen, center(button))));
				steps.add(new Step(2, () -> {
					clicks++;

					if (!isOurScreen(mc.screen))
					{
						log("INFO " + currentName + ": clicking '" + label + "' closed the screen");
						return;
					}

					String after = state(mc, screen);

					if (after.equals(before[0]))
					{
						silent++;
						log("INFO " + currentName + ": '" + label + "' changed nothing (maybe at a limit)");
					}
					else
					{
						log("ok   " + currentName + ": '" + label + "' changed the server state");
					}

					if (button instanceof TooltipButton && ((TooltipButton) button).getTooltipText() == null)
					{
						log("INFO " + currentName + ": '" + label + "' has no tooltip text");
					}
				}));
			}
			else if (widget instanceof EditBox || widget instanceof MultilineTextFieldWidget)
			{
				fields.add(new TextFieldRef(widget));
				summary.append(" [text field]");
			}
		}

		log(summary.toString());

		if (screen instanceof AbstractContainerScreen)
		{
			planSlotTooltip((AbstractContainerScreen<?>) screen);
		}

		for (int i = 0; i < fields.size(); i++)
		{
			final int fieldIndex = i;
			TextFieldRef ref = fields.get(i);
			steps.add(new Step(2, () -> {
				click(screen, center(ref.widget));

				for (char c : TYPED.toCharArray())
				{
					screen.charTyped(c, 0);
				}
			}));
			steps.add(new Step(2, () -> {
				if (!ref.text().contains(TYPED))
				{
					log("FAIL " + currentName + ": text field " + fieldIndex + " did not accept typing (text '" + ref.text() + "')");
					failures++;
				}
				else
				{
					log("ok   " + currentName + ": text field " + fieldIndex + " accepts typing");
				}
			}));
		}

		if (!fields.isEmpty())
		{
			// close and reopen: the text must have been saved on the server
			steps.add(new Step(10, () -> {
				mc.player.closeContainer();
				reopening = true;
				pendingFields = fields.size();
			}));
			steps.add(new Step(30, () -> {
				mc.player.connection.sendUnsignedCommand("rtdebug gui " + currentId + (currentSneak ? " sneak" : ""));
				phase = Phase.WAIT_OPEN;
				openTimeout = 80;
			}));
		}
	}

	/**
	 * Hovers a slot that holds an item and expects the screen to ask for its tooltip (Forge posts {@code ItemTooltipEvent}
	 * whenever a stack's tooltip is built). A container screen that forgets {@code renderHoveredToolTip} fails here.
	 */
	private static void planSlotTooltip(AbstractContainerScreen<?> screen)
	{
		Slot filled = null;

		for (Slot slot : screen.getMenu().slots)
		{
			if (slot.hasItem())
			{
				filled = slot;
			}
		}

		if (filled == null)
		{
			log("INFO " + currentName + ": no filled slot to hover, tooltip not checked");
			return;
		}

		final Slot hovered = filled;
		int[] before = new int[1];
		double[] at = { screen.getGuiLeft() + hovered.x + 8, screen.getGuiTop() + hovered.y + 8 };
		steps.add(new Step(2, () -> {
			before[0] = tooltipRequests;
			render(screen, at);
		}));
		steps.add(new Step(2, () -> {
			if (tooltipRequests > before[0])
			{
				slotTooltips++;
				log("ok   " + currentName + ": hovering a filled slot shows its item tooltip");
			}
			else
			{
				failures++;
				log("FAIL " + currentName + ": hovering a filled slot (" + hovered.getItem().getHoverName().getString() + ") shows no item tooltip - does the screen call renderHoveredToolTip?");
			}
		}));
	}

	private static int pendingFields;

	private static final class TextFieldRef
	{
		final AbstractWidget widget;

		TextFieldRef(AbstractWidget widget)
		{
			this.widget = widget;
		}

		String text()
		{
			return widget instanceof EditBox ? ((EditBox) widget).getValue() : ((MultilineTextFieldWidget) widget).getText();
		}
	}

	private static void checkReopened(Screen screen)
	{
		int found = 0;

		for (GuiEventListener listener : screen.children())
		{
			if (listener instanceof EditBox && ((EditBox) listener).getValue().contains(TYPED))
			{
				found++;
			}
			else if (listener instanceof MultilineTextFieldWidget && ((MultilineTextFieldWidget) listener).getText().contains(TYPED))
			{
				found++;
			}
		}

		if (found == 0)
		{
			log("INFO " + currentName + ": typed text is not shown after reopening (field may not be saved, or the GUI re-places its block)");
		}
		else
		{
			log("ok   " + currentName + ": typed text survived closing and reopening (" + found + " field(s))");
		}

		steps.clear();
		reopening = false;
	}

	// ---- helpers ----------------------------------------------------------------------------

	private static double[] center(AbstractWidget widget)
	{
		return new double[] { widget.getX() + widget.getWidth() / 2.0, widget.getY() + widget.getHeight() / 2.0 };
	}

	private static String describe(Button button)
	{
		String text = button.getMessage().getString();

		if (text == null || text.isEmpty())
		{
			return button.getClass().getSimpleName().replace("Button", "") + "@" + button.getX() + "," + button.getY();
		}

		return text;
	}

	private static void render(Screen screen, double[] at)
	{
		screen.render(new net.minecraft.client.gui.GuiGraphics(Minecraft.getInstance(), Minecraft.getInstance().renderBuffers().bufferSource()), (int) at[0], (int) at[1], 0.0F);
	}

	private static void click(Screen screen, double[] at)
	{
		screen.mouseClicked(at[0], at[1], 0);
		screen.mouseReleased(at[0], at[1], 0);
	}

	/** Server-side state the GUI edits: the block entity at the opened block, the held item's tag, plus the widget labels. */
	private static String state(Minecraft mc, Screen screen)
	{
		StringBuilder sb = new StringBuilder();

		for (GuiEventListener listener : screen.children())
		{
			if (listener instanceof AbstractWidget)
			{
				sb.append(((AbstractWidget) listener).getMessage().getString()).append('|');
			}
		}

		MinecraftServer server = mc.getSingleplayerServer();

		if (server == null)
		{
			return sb.toString();
		}

		BlockPos pos = statePos;

		try
		{
			sb.append(server.submit(() -> {
				StringBuilder inner = new StringBuilder();
				BlockEntity te = server.getLevel(mc.level.dimension()).getBlockEntity(pos);

				if (te != null)
				{
					inner.append(te.saveWithoutMetadata());
				}

				inner.append(server.getPlayerList().getPlayer(mc.player.getUUID()).getMainHandItem().getTag());
				return inner.toString();
			}).get(5, TimeUnit.SECONDS));
		}
		catch (Exception e)
		{
			sb.append("<state unreadable: ").append(e).append('>');
		}

		return sb.toString();
	}

	private static void log(String line)
	{
		report.add(line);
		RTDebug.log("GuiClickTest: {}", line);
	}

	private static void finish()
	{
		phase = Phase.DONE;
		report.add(0, "GUI click test: " + guis.length + " GUIs, " + clicks + " button clicks, " + failures + " failure(s), " + silent + " click(s) that changed nothing, " + slotTooltips + " slot tooltip(s) verified");

		try (PrintWriter out = new PrintWriter(new File(Minecraft.getInstance().gameDirectory, "randomthings_clicks.txt"), "UTF-8"))
		{
			for (String line : report)
			{
				out.println(line);
			}
		}
		catch (Exception e)
		{
			RTDebug.log("GuiClickTest: could not write the report: {}", e.toString());
		}

		RTDebug.log("GuiClickTest: {}", report.get(0));
	}

	public static int failures()
	{
		return failures;
	}
}
