package lumien.randomthings.handler;

import lumien.randomthings.item.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.items.ItemStackHandler;

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
public class EnderLetterHandler extends SavedData {
    private static final String ID = "randomthings_ender_letters";

    private final Map<UUID, ItemStackHandler> inventoryMap = new HashMap<>();

    public EnderLetterHandler() {
    }

    public static EnderLetterHandler loadFrom(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        EnderLetterHandler data = new EnderLetterHandler();
        data.readData(nbt, registries);
        return data;
    }

    public static EnderLetterHandler get(Level world) {
        // always the overworld's store so letters and mailboxes work across dimensions
        return world.getServer().getLevel(Level.OVERWORLD).getDataStorage().computeIfAbsent(new net.minecraft.world.level.saveddata.SavedData.Factory<>(EnderLetterHandler::new, EnderLetterHandler::loadFrom, null), ID);
    }

    public ItemStackHandler getOrCreateInventoryForPlayer(UUID playerUUID) {
        return inventoryMap.computeIfAbsent(playerUUID, uuid -> {
            setDirty();
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
                setDirty();
            }
        };
    }

    public void readData(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag entryList = nbt.getList("entryList", 10);

        for (int i = 0; i < entryList.size(); i++) {
            CompoundTag entryCompound = entryList.getCompound(i);
            UUID uuid = UUID.fromString(entryCompound.getString("uuid"));

            ItemStackHandler handler = newMailboxHandler();
            handler.deserializeNBT(registries, entryCompound.getCompound("inventory"));

            inventoryMap.put(uuid, handler);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag entryList = new ListTag();

        for (Map.Entry<UUID, ItemStackHandler> entry : inventoryMap.entrySet()) {
            CompoundTag entryCompound = new CompoundTag();
            entryCompound.putString("uuid", entry.getKey().toString());
            entryCompound.put("inventory", entry.getValue().serializeNBT(registries));
            entryList.add(entryCompound);
        }

        compound.put("entryList", entryList);

        return compound;
    }
}
