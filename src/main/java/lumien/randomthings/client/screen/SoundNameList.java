package lumien.randomthings.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;

import java.util.List;
import java.util.function.Consumer;

/**
 * Scrollable list of recorded sound names, one clickable row per sound -
 * clicking sends the name back via {@code onSelect}. Port of 1.12.2's
 * {@code GuiStringList} (used by {@code GuiSoundRecorder}), rewritten against
 * vanilla's modern {@code ExtendedList} the same way
 * {@link ChunkAnalyzerScanResultList} already was - including the same
 * {@code render()} override, since {@code AbstractList.render()} bakes a
 * full-screen-menu-style dirt/fade background directly into its own
 * bytecode rather than behind a cleanly overridable hook (see that class's
 * javadoc for the full story - same fix applies here verbatim).
 */
public class SoundNameList extends ExtendedList<SoundNameList.SoundNameEntry> {
    private final Consumer<String> onSelect;

    public SoundNameList(Minecraft client, int width, int height, int top, int bottom, int left, List<String> sounds, Consumer<String> onSelect) {
        super(client, width, height, top, bottom, 14);

        this.setLeftPos(left);

        this.onSelect = onSelect;

        for (String sound : sounds) {
            this.addEntry(new SoundNameEntry(sound));
        }
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        int rowLeft = this.getRowLeft();
        int rowTop = this.y0 + 4 - (int) this.getScrollAmount();

        this.renderList(rowLeft, rowTop, mouseX, mouseY, partialTicks);
    }

    public class SoundNameEntry extends ExtendedList.AbstractListEntry<SoundNameEntry> {
        private final String sound;

        public SoundNameEntry(String sound) {
            this.sound = sound;
        }

        @Override
        public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTicks) {
            Minecraft.getInstance().fontRenderer.drawString(sound, left + 2, top + 2, isMouseOver ? 0xFFFF00 : 0xFFFFFF);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            onSelect.accept(sound);
            return true;
        }
    }
}
