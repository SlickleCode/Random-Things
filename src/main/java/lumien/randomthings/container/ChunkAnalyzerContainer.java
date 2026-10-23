package lumien.randomthings.container;

import lumien.randomthings.util.StackTag;

import lumien.randomthings.item.ChunkAnalyzerItem;
import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Scans the chunk the player is standing in (throttled across multiple
 * {@link #detectAndSendChanges()} calls - one per server tick this container
 * is open - so a large chunk never causes a visible hitch) and counts every
 * distinct block found, writing the sorted result into the held
 * {@link ChunkAnalyzerItem} stack's own NBT so it persists after closing the
 * GUI. Direct port of 1.12.2's {@code ContainerChunkAnalyzer}.
 * <p>
 * API updates from 1.12.2: {@code IBlockState} -&gt; {@code BlockState};
 * {@code Chunk.getHeightValue} -&gt; {@code Chunk.getTopBlockY(Heightmap.Type
 * .WORLD_SURFACE, ...)}; the old metadata/damage-value system that grouped
 * multiple "sub-blocks" under one {@code Item} with different damage values
 * is gone in 1.14.4 (each distinct item is its own registered {@code Item}
 * now), so the display {@code ItemStack} is just {@code state.getBlock()
 * .asItem()} with no damage value to look up; the original's
 * {@code sendWindowProperty}/{@code updateProgressBar} manual sync for the
 * "is scanning" flag is replaced by {@code Container.trackInt}, which syncs
 * automatically - the screen just reads {@code scanning.get()}.
 */
public class ChunkAnalyzerContainer extends AbstractContainerMenu implements ISignalContainer {
    private final Player player;

    public final DataSlot scanning = DataSlot.standalone();

    private boolean isScanning;
    private int chunkX;
    private int chunkZ;
    private int nextX;
    private int nextZ;
    private Map<BlockState, Integer> countMap;

    private static class BlockResult {
        BlockState state;
        int count;
        String name;
        ItemStack stack;
    }

    public ChunkAnalyzerContainer(int windowId, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(ModContainerTypes.CHUNK_ANALYZER, windowId);

        this.player = playerInventory.player;

        this.addDataSlot(scanning);
    }

    public void startScanning() {
        this.isScanning = true;
        this.scanning.set(1);
        this.countMap = new HashMap<>();

        LevelChunk targetChunk = player.level().getChunkAt(player.blockPosition());

        this.chunkX = targetChunk.getPos().x;
        this.chunkZ = targetChunk.getPos().z;
        this.nextX = 0;
        this.nextZ = 0;
    }

    @Override
    public void broadcastChanges() {
        // The player's own inventory/armor/crafting slots only get pushed to
        // the client via `openContainer.detectAndSendChanges()` in the vanilla
        // tick loop - while this GUI is open, `openContainer` is `this`, so
        // `player.container` (the always-present default PlayerContainer)
        // would never sync on its own. Force it every tick, matching
        // 1.12.2's unconditional `player.inventoryContainer.detectAndSendChanges()`.
        player.inventoryMenu.broadcastChanges();

        for (int q = 0; q < 10 && isScanning; q++) {
            LevelChunk c = player.level().getChunk(chunkX, chunkZ);
            int chunkBaseX = c.getPos().getMinBlockX();
            int chunkBaseZ = c.getPos().getMinBlockZ();

            // Chunk.getTopBlockY(...) returns Heightmap.getHeight(...) - 1, i.e. the
            // actual Y of the topmost tracked block - one less than 1.12.2's
            // getHeightValue(...), which returns one PAST the topmost block (the
            // convention this loop's `y < top` bound assumes). Without the +1 the
            // scan silently skips whatever the current topmost block in each
            // column is (confirmed in-game: missing superflat's own grass layer,
            // and missing a block freshly placed on top of it).
            int top = c.getHeight(Heightmap.Types.WORLD_SURFACE, nextX, nextZ) + 1;

            for (int y = c.getMinY(); y < top; y++) {
                BlockPos pos = new BlockPos(chunkBaseX + nextX, y, chunkBaseZ + nextZ);
                BlockState state = c.getBlockState(pos);

                if (!state.isAir()) {
                    countMap.merge(state, 1, Integer::sum);
                }
            }

            if (nextX == 15) {
                nextZ++;
                nextX = 0;

                if (nextZ == 16) {
                    finishScanning();
                    break;
                }
            } else {
                nextX++;
            }
        }

        super.broadcastChanges();
    }

    private void finishScanning() {
        List<BlockResult> resultList = new ArrayList<>();
        Map<String, BlockResult> nameMap = new HashMap<>();

        for (Entry<BlockState, Integer> entry : countMap.entrySet()) {
            BlockState state = entry.getKey();

            BlockResult br = new BlockResult();
            br.state = state;
            br.count = entry.getValue();

            Item item = state.getBlock().asItem();

            String name;

            if (item == Items.AIR) {
                // No real item for this block (e.g. fire, fluids) - fall back to
                // the block's own translated name, matching 1.12.2's
                // getLocalizedName() fallback.
                name = Component.translatable(state.getBlock().getDescriptionId()).getString();
                br.stack = ItemStack.EMPTY;
            } else {
                br.stack = new ItemStack(item);
                name = br.stack.getHoverName().getString();
            }

            br.name = name;

            BlockResult existing = nameMap.get(name);

            if (existing != null) {
                existing.count += br.count;
            } else {
                nameMap.put(name, br);
                resultList.add(br);
            }
        }

        resultList.sort((a, b) -> b.count - a.count);

        ChunkAnalyzerResult result = new ChunkAnalyzerResult();

        for (BlockResult br : resultList) {
            result.addBlock(br.stack, br.name, br.count);
        }

        ItemStack analyzer = player.getMainHandItem();

        if (!analyzer.isEmpty() && analyzer.getItem() instanceof ChunkAnalyzerItem) {
            CompoundTag tag = new CompoundTag();
            result.writeToNBT(tag);
            StackTag.edit(analyzer, t -> t.put("result", tag));

            player.inventoryMenu.broadcastChanges();
        }

        isScanning = false;
        scanning.set(0);
        countMap = null;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void handle(int id, FriendlyByteBuf data) {
        if (id == 0) {
            startScanning();
        }
    }
}
