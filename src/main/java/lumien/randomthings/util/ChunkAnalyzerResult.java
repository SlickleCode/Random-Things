package lumien.randomthings.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;

import java.util.ArrayList;
import java.util.List;

/**
 * A completed chunk scan: one entry per distinct block found, each with a
 * representative {@link ItemStack} (for the icon), a display name, and how
 * many were found - sorted by count descending. Direct port of 1.12.2's
 * {@code ChunkAnalyzerResult}; serialized into the Chunk Analyzer item's own
 * NBT so results persist after closing the GUI.
 */
public class ChunkAnalyzerResult {
    public final List<ItemStack> displayStacks = new ArrayList<>();
    public final List<String> blockDescriptions = new ArrayList<>();
    public final List<Integer> blockCounts = new ArrayList<>();

    public void addBlock(ItemStack displayStack, String blockDescription, int count) {
        displayStacks.add(displayStack);
        blockDescriptions.add(blockDescription);
        blockCounts.add(count);
    }

    public void writeToNBT(CompoundNBT compound) {
        ListNBT tagList = new ListNBT();

        for (int i = 0; i < displayStacks.size(); i++) {
            CompoundNBT entryCompound = new CompoundNBT();

            entryCompound.put("stack", displayStacks.get(i).write(new CompoundNBT()));
            entryCompound.putString("description", blockDescriptions.get(i));
            entryCompound.putInt("count", blockCounts.get(i));

            tagList.add(entryCompound);
        }

        compound.put("entries", tagList);
    }

    public void readFromNBT(CompoundNBT compound) {
        ListNBT entryList = compound.getList("entries", 10);

        for (int i = 0; i < entryList.size(); i++) {
            CompoundNBT entryCompound = entryList.getCompound(i);

            displayStacks.add(ItemStack.read(entryCompound.getCompound("stack")));
            blockDescriptions.add(entryCompound.getString("description"));
            blockCounts.add(entryCompound.getInt("count"));
        }
    }
}
