package lumien.randomthings.client.screen;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;

/**
 * Minimal multi-line text box (1.14.4 has none): word-wrapped, fixed number of visible lines with
 * scrolling, Enter inserts a newline, cursor movement (arrows / Home / End / click), backspace,
 * delete and paste, and a hard character cap. Set {@link #setEditable(boolean)} to false for a
 * read-only display.
 */
public class MultilineTextFieldWidget extends Widget
{
	private static final int LINE_HEIGHT = 10;

	private final FontRenderer font;
	private final int maxLength;
	private final int visibleLines;

	private String text = "";
	private int cursor = 0;
	private int scroll = 0;
	private boolean editable = true;

	private List<int[]> lines = new ArrayList<>();
	private boolean dirty = true;

	public MultilineTextFieldWidget(FontRenderer font, int x, int y, int width, int height, int maxLength)
	{
		super(x, y, width, height, "");

		this.font = font;
		this.maxLength = maxLength;
		this.visibleLines = (height - 4) / LINE_HEIGHT;
	}

	public void setEditable(boolean editable)
	{
		this.editable = editable;
	}

	public String getText()
	{
		return this.text;
	}

	public void setText(String text)
	{
		this.text = text.length() > this.maxLength ? text.substring(0, this.maxLength) : text;
		this.cursor = this.text.length();
		this.dirty = true;
		this.scroll = 0;
	}

	public void setFocusedState(boolean focused)
	{
		this.setFocused(focused && this.editable);
	}

	private int wrapWidth()
	{
		return this.width - 4;
	}

	private void layout()
	{
		if (!this.dirty)
		{
			return;
		}

		this.dirty = false;
		this.lines = new ArrayList<>();

		int n = this.text.length();
		int start = 0;
		int i = 0;
		int lastSpace = -1;

		while (i < n)
		{
			char c = this.text.charAt(i);

			if (c == '\n')
			{
				this.lines.add(new int[] { start, i });
				start = i + 1;
				i = start;
				lastSpace = -1;
				continue;
			}

			if (c == ' ')
			{
				lastSpace = i;
			}

			if (i > start && this.font.getStringWidth(this.text.substring(start, i + 1)) > this.wrapWidth())
			{
				int brk = lastSpace > start ? lastSpace + 1 : i;
				this.lines.add(new int[] { start, brk });
				start = brk;
				i = start;
				lastSpace = -1;
				continue;
			}

			i++;
		}

		this.lines.add(new int[] { start, n });
	}

	private int cursorLine()
	{
		this.layout();

		int found = 0;

		for (int l = 0; l < this.lines.size(); l++)
		{
			int[] line = this.lines.get(l);

			if (this.cursor >= line[0] && this.cursor <= line[1])
			{
				found = l;
			}
		}

		return found;
	}

	private void scrollToCursor()
	{
		int line = this.cursorLine();

		if (line < this.scroll)
		{
			this.scroll = line;
		}
		else if (line >= this.scroll + this.visibleLines)
		{
			this.scroll = line - this.visibleLines + 1;
		}
	}

	private int indexAtX(int lineIndex, int pixelX)
	{
		int[] line = this.lines.get(lineIndex);
		int best = line[0];
		int bestDiff = Integer.MAX_VALUE;

		for (int i = line[0]; i <= line[1]; i++)
		{
			int diff = Math.abs(this.font.getStringWidth(this.text.substring(line[0], i)) - pixelX);

			if (diff < bestDiff)
			{
				bestDiff = diff;
				best = i;
			}
		}

		return best;
	}

	private void insert(String s)
	{
		int room = this.maxLength - this.text.length();

		if (room <= 0)
		{
			return;
		}

		if (s.length() > room)
		{
			s = s.substring(0, room);
		}

		this.text = this.text.substring(0, this.cursor) + s + this.text.substring(this.cursor);
		this.cursor += s.length();
		this.dirty = true;
		this.scrollToCursor();
	}

	@Override
	public boolean charTyped(char c, int modifiers)
	{
		if (!this.editable || !this.isFocused() || c < 32 || c == 127)
		{
			return false;
		}

		this.insert(Character.toString(c));
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if (!this.editable || !this.isFocused())
		{
			return false;
		}

		this.layout();

		if (Screen.isPaste(keyCode))
		{
			String clip = Minecraft.getInstance().keyboardListener.getClipboardString();
			StringBuilder cleaned = new StringBuilder();

			for (char c : clip.toCharArray())
			{
				if (c == '\n' || (c >= 32 && c != 127))
				{
					cleaned.append(c);
				}
			}

			this.insert(cleaned.toString());
			return true;
		}

		int line = this.cursorLine();

		switch (keyCode)
		{
			case 257: // enter
			case 335: // keypad enter
				this.insert("\n");
				return true;
			case 259: // backspace
				if (this.cursor > 0)
				{
					this.text = this.text.substring(0, this.cursor - 1) + this.text.substring(this.cursor);
					this.cursor--;
					this.dirty = true;
					this.scrollToCursor();
				}
				return true;
			case 261: // delete
				if (this.cursor < this.text.length())
				{
					this.text = this.text.substring(0, this.cursor) + this.text.substring(this.cursor + 1);
					this.dirty = true;
					this.scrollToCursor();
				}
				return true;
			case 263: // left
				this.cursor = Math.max(0, this.cursor - 1);
				this.scrollToCursor();
				return true;
			case 262: // right
				this.cursor = Math.min(this.text.length(), this.cursor + 1);
				this.scrollToCursor();
				return true;
			case 265: // up
			case 264: // down
			{
				int target = line + (keyCode == 265 ? -1 : 1);

				if (target >= 0 && target < this.lines.size())
				{
					int x = this.font.getStringWidth(this.text.substring(this.lines.get(line)[0], this.cursor));
					this.cursor = this.indexAtX(target, x);
					this.scrollToCursor();
				}
				return true;
			}
			case 268: // home
				this.cursor = this.lines.get(line)[0];
				return true;
			case 269: // end
				this.cursor = this.lines.get(line)[1];
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button)
	{
		boolean inside = this.visible && mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height;

		this.setFocusedState(inside);

		if (inside && this.editable && button == 0)
		{
			this.layout();

			int line = Math.max(0, Math.min(this.lines.size() - 1, this.scroll + ((int) mouseY - (this.y + 2)) / LINE_HEIGHT));
			this.cursor = this.indexAtX(line, (int) mouseX - (this.x + 2));
		}

		return inside;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta)
	{
		if (!this.isMouseOver(mouseX, mouseY))
		{
			return false;
		}

		this.layout();
		this.scroll = Math.max(0, Math.min(Math.max(0, this.lines.size() - this.visibleLines), this.scroll - (int) Math.signum(delta)));
		return true;
	}

	@Override
	public void renderButton(int mouseX, int mouseY, float partialTicks)
	{
		this.layout();

		fill(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, this.isFocused() ? 0xFFFFFFFF : 0xFFA0A0A0);
		fill(this.x, this.y, this.x + this.width, this.y + this.height, 0xFF000000);

		int color = this.editable ? 0xE0E0E0 : 0x909090;

		for (int l = this.scroll; l < Math.min(this.lines.size(), this.scroll + this.visibleLines); l++)
		{
			int[] line = this.lines.get(l);
			int lineY = this.y + 2 + (l - this.scroll) * LINE_HEIGHT;

			this.font.drawStringWithShadow(this.text.substring(line[0], line[1]), this.x + 2, lineY, color);
		}

		if (this.isFocused() && (System.currentTimeMillis() / 500) % 2 == 0)
		{
			int cl = this.cursorLine();

			if (cl >= this.scroll && cl < this.scroll + this.visibleLines)
			{
				int cx = this.x + 2 + this.font.getStringWidth(this.text.substring(this.lines.get(cl)[0], this.cursor));
				int cy = this.y + 2 + (cl - this.scroll) * LINE_HEIGHT;

				fill(cx, cy - 1, cx + 1, cy + LINE_HEIGHT - 1, 0xFFD0D0D0);
			}
		}
	}
}
