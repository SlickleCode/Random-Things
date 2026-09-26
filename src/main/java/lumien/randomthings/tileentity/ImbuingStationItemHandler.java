package lumien.randomthings.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;

/**
 * The 5-slot inventory shared by {@link ImbuingStationTileEntity} (the real,
 * server-authoritative handler) and {@link
 * lumien.randomthings.container.ImbuingStationContainer}'s client-side
 * reconstruction constructor - slots 0-2 are the three unordered ingredients,
 * slot 3 is the "item to imbue", slot 4 is the machine-only output (matching
 * the validated-handler convention established by
 * {@link PotionVaporizerItemHandler} after the output-slot exploit found
 * there this session).
 */
public class ImbuingStationItemHandler extends ItemStackHandler {
    public ImbuingStationItemHandler() {
        super(5);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot != 4;
    }
}
