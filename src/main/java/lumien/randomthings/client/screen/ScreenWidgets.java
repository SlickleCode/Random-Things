package lumien.randomthings.client.screen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

/** 1.18 removed {@code Screen.buttons}; the widgets are now found through {@code children()}. */
public final class ScreenWidgets
{
	private ScreenWidgets()
	{
	}

	public static List<AbstractWidget> buttons(Screen screen)
	{
		List<AbstractWidget> out = new ArrayList<>();

		for (GuiEventListener child : screen.children())
		{
			if (child instanceof AbstractWidget)
			{
				out.add((AbstractWidget) child);
			}
		}

		return out;
	}
}
