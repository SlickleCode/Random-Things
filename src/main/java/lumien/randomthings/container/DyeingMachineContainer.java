package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.container.slot.DyeCraftingSlot;
import lumien.randomthings.container.slot.DyeSlot;
import lumien.randomthings.container.slot.DyeableSlot;
import lumien.randomthings.util.DyeUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.ContainerLevelAccess;

/**
 * Direct port of 1.12.2's {@code ContainerDyeingMachine}: 2 input slots (item
 * to dye + a vanilla dye) feeding 2 preview/result slots - one tags the item
 * with {@code rtDye} (used by {@code ItemRenderer}/{@code ArmorLayer} coremod
 * hooks to recolor the item wherever it's rendered) and the other with
 * {@code enchantmentColor} (recolors the enchant glint the same way). Taking
 * either result consumes 1 of each occupied ingredient slot - there's no
 * persistent inventory backing this machine; unconsumed ingredients just drop
 * on container close, matching the original.
 */
public class DyeingMachineContainer extends AbstractContainerMenu {
    private final ContainerLevelAccess pos;

    private final CraftingContainer ingredients = new CraftingContainer(this, 2, 1);
    private final Container dyeResult = new ResultContainer();
    private final Container enchantmentResult = new ResultContainer();

    public DyeingMachineContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData) {
        this(windowId, (Inventory) playerInventory, ContainerLevelAccess.create(((Inventory) playerInventory).player.level, extraData.readBlockPos()));
    }

    public DyeingMachineContainer(int windowId, Inventory playerInventory, ContainerLevelAccess pos) {
        super(ModContainerTypes.DYEING_MACHINE, windowId);

        this.pos = pos;

        this.addSlot(new DyeableSlot(ingredients, 0, 27, 22));
        this.addSlot(new DyeSlot(ingredients, 1, 76, 22));
        this.addSlot(new DyeCraftingSlot(playerInventory.player, this, ingredients, dyeResult, 2, 133, 22));
        this.addSlot(new DyeCraftingSlot(playerInventory.player, this, ingredients, enchantmentResult, 2, 154, 22));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 59 + row * 18));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 117));
        }
    }

    @Override
    public void slotsChanged(Container inventoryIn) {
        ItemStack toDye = inventoryIn.getItem(0);
        ItemStack dye = inventoryIn.getItem(1);

        if (!toDye.isEmpty() && !dye.isEmpty()) {
            int dyeColor = DyeUtil.getDyeColor(dye);
            ItemStack copy = toDye.copy();

            if (copy.getTag() == null) {
                copy.setTag(new CompoundTag());
            }

            CompoundTag compound = copy.getTag();
            copy.setCount(1);
            compound.putInt("rtDye", dyeColor);
            this.dyeResult.setItem(0, copy);

            ItemStack enchantmentCopy = toDye.copy();
            enchantmentCopy.setCount(1);
            if (enchantmentCopy.getTag() == null) {
                enchantmentCopy.setTag(new CompoundTag());
            }

            enchantmentCopy.getTag().putInt("enchantmentColor", dyeColor);

            enchantmentResult.setItem(0, enchantmentCopy);
        } else if (!toDye.isEmpty() && dye.isEmpty()) {
            this.enchantmentResult.setItem(0, ItemStack.EMPTY);
            ItemStack copy = toDye.copy();
            copy.setCount(1);

            if (copy.getTag() != null) {
                CompoundTag compound = copy.getTag();

                if (compound.contains("rtDye")) {
                    compound.remove("rtDye");
                }

                if (compound.contains("enchantmentColor")) {
                    compound.remove("enchantmentColor");
                }

                if (compound.isEmpty()) {
                    copy.setTag(null);
                }

                this.dyeResult.setItem(0, copy);
            } else {
                this.dyeResult.setItem(0, ItemStack.EMPTY);
            }
        } else {
            this.dyeResult.setItem(0, ItemStack.EMPTY);
            this.enchantmentResult.setItem(0, ItemStack.EMPTY);
        }
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);

        this.pos.execute((world, blockPos) -> {
            if (!world.isClientSide) {
                for (int i = 0; i < 2; ++i) {
                    ItemStack itemstack = this.ingredients.removeItemNoUpdate(i);

                    if (!itemstack.isEmpty()) {
                        playerIn.drop(itemstack, false);
                    }
                }
            }
        });
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(this.pos, playerIn, ModBlocks.DYEING_MACHINE);
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack stackInSlot = slot.getItem();
            result = stackInSlot.copy();

            if (index < 4) {
                // 1.12.2's own transferStackInSlot merged out to index 37, not 40 - short by 3
                // of the real player-inventory range (4 machine slots + 36 player slots = 40).
                // No plausible intentional reason for a shift-click to silently refuse to land
                // in a player's last 3 hotbar slots specifically, so this is treated as an
                // original off-by-a-few slip and corrected here, not preserved.
                if (!this.moveItemStackTo(stackInSlot, 4, 40, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stackInSlot, 0, 2, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stackInSlot.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, stackInSlot);
        }

        return result;
    }

    /**
     * Direct port of 1.12.2's own override of this method - vanilla's default
     * dumps a whole incoming stack into the first empty valid slot it finds;
     * this machine's ingredient/result slots are meant to hold 1 item at a
     * time, so shift-clicking a stack in only ever moves 1 item per empty
     * slot found (subsequent items keep merging into the next slot/stacking
     * onto existing piles as normal). 1.13's item flattening removed the old
     * metadata-subtype check 1.12.2 had here (no more shared-Item/different-
     * damage stacking cases to account for).
     */
    @Override
    protected boolean moveItemStackTo(ItemStack stack, int startIndex, int endIndex, boolean reverseDirection) {
        boolean changed = false;
        int i = startIndex;

        if (reverseDirection) {
            i = endIndex - 1;
        }

        if (stack.isStackable()) {
            while (stack.getCount() > 0 && (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex)) {
                Slot slot = this.slots.get(i);
                ItemStack slotStack = slot.getItem();

                if (!slotStack.isEmpty() && slotStack.getItem() == stack.getItem() && ItemStack.tagMatches(stack, slotStack) && slot.mayPlace(stack)) {
                    int combined = slotStack.getCount() + stack.getCount();

                    if (combined <= stack.getMaxStackSize()) {
                        stack.setCount(0);
                        slotStack.setCount(combined);
                        slot.setChanged();
                        changed = true;
                    } else if (slotStack.getCount() < stack.getMaxStackSize()) {
                        stack.shrink(stack.getMaxStackSize() - slotStack.getCount());
                        slotStack.setCount(stack.getMaxStackSize());
                        slot.setChanged();
                        changed = true;
                    }
                }

                i += reverseDirection ? -1 : 1;
            }
        }

        if (stack.getCount() > 0) {
            i = reverseDirection ? endIndex - 1 : startIndex;

            while (!reverseDirection && i < endIndex || reverseDirection && i >= startIndex) {
                Slot slot = this.slots.get(i);
                ItemStack slotStack = slot.getItem();

                if (slotStack.isEmpty() && slot.mayPlace(stack)) {
                    if (stack.getCount() > 1) {
                        ItemStack copy = stack.copy();
                        copy.setCount(1);
                        slot.set(copy);

                        stack.shrink(1);
                        changed = true;
                        break;
                    } else {
                        slot.set(stack.copy());
                        slot.setChanged();
                        stack.setCount(0);
                        changed = true;
                        break;
                    }
                }

                i += reverseDirection ? -1 : 1;
            }
        }

        return changed;
    }
}
