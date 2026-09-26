package lumien.randomthings.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraftforge.items.ItemStackHandler;

import java.util.List;

/**
 * The 3-slot fuel/potion/output filter shared by {@link PotionVaporizerTileEntity}
 * (the real, server-authoritative handler) and {@link
 * lumien.randomthings.container.PotionVaporizerContainer}'s client-side
 * reconstruction constructor (used by the registered {@code ContainerType}
 * factory to rebuild a local container from just a window id + player
 * inventory, with no access to the real tile entity).
 * <p>
 * Pulled out to its own class after finding that the container's
 * reconstruction path was using a bare {@code new ItemStackHandler(3)} - no
 * validation at all - while the real one restricts slot 2 (output) to
 * machine-only writes. Since a slot's manual-placement gate
 * ({@code Slot.isItemValid}) is checked against whichever handler instance
 * backs *that side's own* container, the client's copy never actually
 * enforced the output-only rule locally, which lines up with the reported
 * "player was able to manually fill the out slot".
 */
public class PotionVaporizerItemHandler extends ItemStackHandler {
    public PotionVaporizerItemHandler() {
        super(3);
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        switch (slot) {
            case 0:
                return AbstractFurnaceTileEntity.isFuel(stack);
            case 1:
                List<EffectInstance> effects = PotionUtils.getEffectsFromStack(stack);
                return stack.getItem() == Items.POTION && !effects.isEmpty() && !effects.get(0).getPotion().isInstant();
            case 2:
                return false;
            default:
                return false;
        }
    }
}
