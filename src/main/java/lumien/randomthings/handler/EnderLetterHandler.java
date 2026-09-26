package lumien.randomthings.handler;

import lumien.randomthings.item.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A world-persistent, per-player 9-slot mailbox inventory, keyed by player
 * UUID rather than tied to any one placed {@link
 * lumien.randomthings.block.EnderMailboxBlock} - every Ender Mailbox a
 * player places shows the exact same inbox, wherever it is. Direct port of
 * 1.12.2's {@code EnderLetterHandler}, with its hand-rolled {@code IInventory}
 * inner class replaced by a plain {@link ItemStackHandler} (this port's
 * standard capability-based inventory, restricted to {@code ItemEnderLetter}
 * stacks only, matching the original's own {@code isItemValidForSlot}).
 */
public class EnderLetterHandler extends WorldSavedData {
    private static final String ID = "randomthings_ender_letters";

    private final Map<UUID, ItemStackHandler> inventoryMap = new HashMap<>();

    public EnderLetterHandler() {
        super(ID);
    }

    public static EnderLetterHandler get(World world) {
        return ((ServerWorld) world).getSavedData().getOrCreate(EnderLetterHandler::new, ID);
    }

    public ItemStackHandler getOrCreateInventoryForPlayer(UUID playerUUID) {
        return inventoryMap.computeIfAbsent(playerUUID, uuid -> {
            markDirty();
            return newMailboxHandler();
        });
    }

    public boolean hasInventoryFor(UUID playerUUID) {
        return inventoryMap.containsKey(playerUUID);
    }

    public boolean isInventoryEmpty(UUID playerUUID) {
        ItemStackHandler handler = inventoryMap.get(playerUUID);

        if (handler == null) {
            return true;
        }

        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    private ItemStackHandler newMailboxHandler() {
        return new ItemStackHandler(9) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return stack.getItem() == ModItems.ENDER_LETTER;
            }

            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }
        };
    }

    @Override
    public void read(CompoundNBT nbt) {
        ListNBT entryList = nbt.getList("entryList", 10);

        for (int i = 0; i < entryList.size(); i++) {
            CompoundNBT entryCompound = entryList.getCompound(i);
            UUID uuid = UUID.fromString(entryCompound.getString("uuid"));

            ItemStackHandler handler = newMailboxHandler();
            handler.deserializeNBT(entryCompound.getCompound("inventory"));

            inventoryMap.put(uuid, handler);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT entryList = new ListNBT();

        for (Map.Entry<UUID, ItemStackHandler> entry : inventoryMap.entrySet()) {
            CompoundNBT entryCompound = new CompoundNBT();
            entryCompound.putString("uuid", entry.getKey().toString());
            entryCompound.put("inventory", entry.getValue().serializeNBT());
            entryList.add(entryCompound);
        }

        compound.put("entryList", entryList);

        return compound;
    }
}
