package lumien.randomthings.item;

import java.awt.Color;
import java.util.ArrayList;

/**
 * Scratch accumulators used only while {@link ModItems#registerItems} builds
 * the combined "vanilla" Divining Rod out of the 6 individual ore rods'
 * colors/tags (cleared again immediately after).
 * <p>
 * Deliberately NOT fields on {@code ModItems} itself - see {@link
 * RuneDustItems}'s javadoc for why: that class carries a class-level
 * {@code @ObjectHolder}, which makes Forge's {@code ObjectHolderRegistry}
 * scan every static field on it and hard-crash at startup on the first one
 * that isn't a registry type (found the hard way - this exact pair was one
 * of two fields that would have crashed the game back-to-back, right after
 * the {@code RUNE_DUST} map).
 */
class DiviningRodScratch
{
	static final ArrayList<Color> COLOR_HOLDER = new ArrayList<>();
	static final ArrayList<String> TAG_HOLDER = new ArrayList<>();
}
