package lumien.randomthings.item;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;

/**
 * The 16 registered {@link RuneDustItem} instances, one per {@link DyeColor},
 * filled directly during {@link ModItems#registerItems} (16 distinct
 * registry names, not one fixed name {@code @ObjectHolder} can cover).
 * <p>
 * Deliberately NOT a field on {@code ModItems} itself: that class carries a
 * class-level {@code @ObjectHolder("randomthings")}, and Forge's {@code
 * ObjectHolderRegistry} scans *every* static field on such a class - a plain
 * {@code Map} field (not a Block/Item/etc.) makes it throw {@code
 * IllegalStateException: The ObjectHolder annotation cannot apply to a
 * field that does not map to a registry} and hard-crash the game at
 * startup, which is exactly what happened here (only surfaces when actually
 * running the game - a plain compile/build never exercises
 * {@code ObjectHolderRegistry} at all). Living in its own unannotated class
 * sidesteps the scan entirely.
 */
public class RuneDustItems
{
	public static final Map<DyeColor, Item> BY_COLOR = new EnumMap<>(DyeColor.class);
}
