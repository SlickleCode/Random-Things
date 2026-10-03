package lumien.randomthings.container.slot;

import lumien.randomthings.container.DyeingMachineContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.CraftingResultSlot;
import net.minecraft.item.ItemStack;

/**
 * Direct port of 1.12.2's {@code SlotDyeCrafting}: a result slot that (unlike
 * vanilla crafting) doesn't consume a full stack of each ingredient on take -
 * just 1 of whichever ingredient slots are occupied - and re-runs the
 * dye/enchantment-color preview instead of firing a real recipe-crafted event.
 */
public class DyeCraftingSlot extends CraftingResultSlot {
    private final CraftingInventory craftMatrix;
    private final DyeingMachineContainer container;

    public DyeCraftingSlot(PlayerEntity player, DyeingMachineContainer container, CraftingInventory craftingInventory, IInventory result, int index, int xPosition, int yPosition) {
        super(player, craftingInventory, result, index, xPosition, yPosition);

        this.craftMatrix = craftingInventory;
        this.container = container;
    }

    @Override
    protected void onCrafting(ItemStack stack) {
    }

    @Override
    public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
        for (int i = 0; i < this.craftMatrix.getSizeInventory(); ++i) {
            ItemStack ingredient = this.craftMatrix.getStackInSlot(i);

            if (!ingredient.isEmpty()) {
                this.craftMatrix.decrStackSize(i, 1);
            }
        }

        this.container.onCraftMatrixChanged(craftMatrix);

        return stack;
    }
}
