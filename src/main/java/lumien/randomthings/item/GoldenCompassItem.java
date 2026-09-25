package lumien.randomthings.item;

import net.minecraft.item.Item;

/**
 * Points at a fixed world position instead of spawn/a lodestone - the target
 * is set by combining this with a {@link PositionFilterItem} at a crafting
 * table (see {@code recipes/GoldenCompassSetPositionRecipe}), never by the
 * item itself. A freshly-crafted one with no target set just wobbles
 * randomly, matching 1.12.2 exactly (its own {@code ItemGoldenCompass} never
 * set a target either - only the recipe did).
 */
public class GoldenCompassItem extends CompassItemBase
{
	public GoldenCompassItem(Item.Properties properties)
	{
		super(properties);
	}
}
