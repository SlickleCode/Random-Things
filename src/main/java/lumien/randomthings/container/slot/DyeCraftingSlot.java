package lumien.randomthings.container.slot;

import lumien.randomthings.container.DyeingMachineContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Direct port of 1.12.2's {@code SlotDyeCrafting}: a result slot that (unlike
 * vanilla crafting) doesn't consume a full stack of each ingredient on take -
 * just 1 of whichever ingredient slots are occupied - and re-runs the
 * dye/enchantment-color preview instead of firing a real recipe-crafted event.
 */
public class DyeCraftingSlot extends ResultSlot {
    private final CraftingContainer craftMatrix;
    private final DyeingMachineContainer container;

    public DyeCraftingSlot(Player player, DyeingMachineContainer container, CraftingContainer craftingInventory, Container result, int index, int xPosition, int yPosition) {
        super(player, craftingInventory, result, index, xPosition, yPosition);

        this.craftMatrix = craftingInventory;
        this.container = container;
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
    }

    @Override
    public void onTake(Player thePlayer, ItemStack stack) {
        for (int i = 0; i < this.craftMatrix.getContainerSize(); ++i) {
            ItemStack ingredient = this.craftMatrix.getItem(i);

            if (!ingredient.isEmpty()) {
                this.craftMatrix.removeItem(i, 1);
            }
        }

        this.container.slotsChanged(craftMatrix);

    }
}
