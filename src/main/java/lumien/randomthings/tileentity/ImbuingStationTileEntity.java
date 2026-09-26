package lumien.randomthings.tileentity;

import lumien.randomthings.container.ImbuingStationContainer;
import lumien.randomthings.recipes.imbuing.ImbuingRecipeHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * Direct port of 1.12.2's {@code TileEntityImbuingStation}: once the 3
 * unordered ingredients and the item-to-imbue all match a registered
 * {@link lumien.randomthings.recipes.imbuing.ImbuingRecipe}, spends 200
 * ticks accumulating progress before consuming one of each input and
 * producing the result in the output slot.
 */
public class ImbuingStationTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    public static final int IMBUING_LENGTH = 200;

    private int imbuingProgress;
    private ItemStack currentOutput = ItemStack.EMPTY;

    private final ImbuingStationItemHandler itemHandler = new ImbuingStationItemHandler() {
        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
        }
    };

    public ImbuingStationTileEntity() {
        super(ModTileEntityTypes.IMBUING_STATION);
    }

    public ImbuingStationItemHandler itemHandler() {
        return itemHandler;
    }

    public int getImbuingProgress() {
        return imbuingProgress;
    }

    @Override
    public void tick() {
        if (this.world.isRemote) {
            return;
        }

        ItemStack validOutput = ImbuingRecipeHandler.getRecipeOutput(itemHandler);

        if (!ItemStack.areItemStacksEqual(validOutput, currentOutput) && canHandleOutput(validOutput)) {
            this.imbuingProgress = 0;
            currentOutput = validOutput;
        }

        if (!this.currentOutput.isEmpty()) {
            this.imbuingProgress++;

            if (this.imbuingProgress >= IMBUING_LENGTH) {
                imbuingProgress = 0;
                imbue();
            }
        } else {
            this.imbuingProgress = 0;
        }
    }

    private boolean canHandleOutput(ItemStack validOutput) {
        ItemStack currentInOutput = itemHandler.getStackInSlot(4);

        if (validOutput.isEmpty() || currentInOutput.isEmpty()) {
            return true;
        }

        if (!ItemStack.areItemsEqual(currentInOutput, validOutput) || !ItemStack.areItemStackTagsEqual(currentInOutput, validOutput)) {
            return false;
        }

        return currentInOutput.getCount() + validOutput.getCount() <= currentInOutput.getMaxStackSize();
    }

    private void imbue() {
        itemHandler.insertItem(4, currentOutput.copy(), false);

        for (int slot = 0; slot < 4; slot++) {
            itemHandler.extractItem(slot, 1, false);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        compound.putInt("imbuingProgress", imbuingProgress);

        if (!currentOutput.isEmpty()) {
            compound.put("output", currentOutput.write(new CompoundNBT()));
        }

        compound.put("inventory", itemHandler.serializeNBT());

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        imbuingProgress = compound.getInt("imbuingProgress");

        currentOutput = compound.contains("output") ? ItemStack.read(compound.getCompound("output")) : ItemStack.EMPTY;

        itemHandler.deserializeNBT(compound.getCompound("inventory"));
    }

    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ImbuingStationContainer(windowId, playerInventory, itemHandler, IWorldPosCallable.of(this.world, pos));
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.randomthings.imbuing_station");
    }
}
