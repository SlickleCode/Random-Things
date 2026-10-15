package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import lumien.randomthings.container.ImbuingStationContainer;
import lumien.randomthings.recipes.imbuing.ImbuingRecipeHandler;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Direct port of 1.12.2's {@code TileEntityImbuingStation}: once the 3
 * unordered ingredients and the item-to-imbue all match a registered
 * {@link lumien.randomthings.recipes.imbuing.ImbuingRecipe}, spends 200
 * ticks accumulating progress before consuming one of each input and
 * producing the result in the output slot.
 */
public class ImbuingStationTileEntity extends BlockEntity implements MenuProvider {
    public static final int IMBUING_LENGTH = 200;

    private int imbuingProgress;
    private ItemStack currentOutput = ItemStack.EMPTY;

    private final ImbuingStationItemHandler itemHandler = new ImbuingStationItemHandler() {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public ImbuingStationTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.IMBUING_STATION, pos, state);
    }

    public ImbuingStationItemHandler itemHandler() {
        return itemHandler;
    }

    public int getImbuingProgress() {
        return imbuingProgress;
    }

    public void tick() {
        if (this.level.isClientSide) {
            return;
        }

        ItemStack validOutput = ImbuingRecipeHandler.getRecipeOutput(itemHandler);

        if (!ItemStack.matches(validOutput, currentOutput) && canHandleOutput(validOutput)) {
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

        if (!ItemStack.isSame(currentInOutput, validOutput) || !ItemStack.tagMatches(currentInOutput, validOutput)) {
            return false;
        }

        return currentInOutput.getCount() + validOutput.getCount() <= currentInOutput.getMaxStackSize();
    }

    private void imbue() {
        // Not insertItem: the output slot rejects insertion on purpose (isItemValid), which would also reject the machine's own
        // result - ingredients were consumed and nothing came out (found by behavior.imbuing_station.*).
        ItemStack out = itemHandler.getStackInSlot(4);

        if (out.isEmpty()) {
            itemHandler.setStackInSlot(4, currentOutput.copy());
        } else {
            ItemStack merged = out.copy();
            merged.grow(currentOutput.getCount());
            itemHandler.setStackInSlot(4, merged);
        }

        for (int slot = 0; slot < 4; slot++) {
            itemHandler.extractItem(slot, 1, false);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        compound.putInt("imbuingProgress", imbuingProgress);

        if (!currentOutput.isEmpty()) {
            compound.put("output", currentOutput.save(new CompoundTag()));
        }

        compound.put("inventory", itemHandler.serializeNBT());

    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        imbuingProgress = compound.getInt("imbuingProgress");

        currentOutput = compound.contains("output") ? ItemStack.of(compound.getCompound("output")) : ItemStack.EMPTY;

        itemHandler.deserializeNBT(compound.getCompound("inventory"));
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
        return new ImbuingStationContainer(windowId, playerInventory, itemHandler, ContainerLevelAccess.create(this.level, worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.imbuing_station");
    }
}
