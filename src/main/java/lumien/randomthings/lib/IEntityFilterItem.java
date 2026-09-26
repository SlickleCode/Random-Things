package lumien.randomthings.lib;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

/**
 * Implemented by items that can be used as a per-block "which entities does
 * this apply to" filter (see {@code EntityDetectorTileEntity}'s CUSTOM
 * filter mode).
 */
public interface IEntityFilterItem {
    boolean apply(ItemStack me, Entity entity);
}
