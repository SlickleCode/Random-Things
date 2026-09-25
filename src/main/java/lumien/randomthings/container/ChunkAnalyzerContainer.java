package lumien.randomthings.container;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lumien.randomthings.item.ChunkAnalyzerItem;
import lumien.randomthings.util.ChunkAnalyzerResult;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;

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
public class ChunkAnalyzerContainer extends Container implements ISignalContainer
{
	private final PlayerEntity player;

	public final IntReferenceHolder scanning = IntReferenceHolder.single();

	private boolean isScanning;
	private int chunkX;
	private int chunkZ;
	private int nextX;
	private int nextZ;
	private Map<BlockState, Integer> countMap;

	private static class BlockResult
	{
		BlockState state;
		int count;
		String name;
		ItemStack stack;
	}

	public ChunkAnalyzerContainer(int windowId, PlayerInventory playerInventory, PacketBuffer extraData)
	{
		super(ModContainerTypes.CHUNK_ANALYZER, windowId);

		this.player = playerInventory.player;

		this.trackInt(scanning);
	}

	public void startScanning()
	{
		this.isScanning = true;
		this.scanning.set(1);
		this.countMap = new HashMap<>();

		Chunk targetChunk = player.world.getChunkAt(player.getPosition());

		this.chunkX = targetChunk.getPos().x;
		this.chunkZ = targetChunk.getPos().z;
		this.nextX = 0;
		this.nextZ = 0;
	}

	@Override
	public void detectAndSendChanges()
	{
		// The player's own inventory/armor/crafting slots only get pushed to
		// the client via `openContainer.detectAndSendChanges()` in the vanilla
		// tick loop - while this GUI is open, `openContainer` is `this`, so
		// `player.container` (the always-present default PlayerContainer)
		// would never sync on its own. Force it every tick, matching
		// 1.12.2's unconditional `player.inventoryContainer.detectAndSendChanges()`.
		player.container.detectAndSendChanges();

		for (int q = 0; q < 10 && isScanning; q++)
		{
			Chunk c = player.world.getChunk(chunkX, chunkZ);
			int chunkBaseX = c.getPos().getXStart();
			int chunkBaseZ = c.getPos().getZStart();

			// Chunk.getTopBlockY(...) returns Heightmap.getHeight(...) - 1, i.e. the
			// actual Y of the topmost tracked block - one less than 1.12.2's
			// getHeightValue(...), which returns one PAST the topmost block (the
			// convention this loop's `y < top` bound assumes). Without the +1 the
			// scan silently skips whatever the current topmost block in each
			// column is (confirmed in-game: missing superflat's own grass layer,
			// and missing a block freshly placed on top of it).
			int top = c.getTopBlockY(Heightmap.Type.WORLD_SURFACE, nextX, nextZ) + 1;

			for (int y = 0; y < top; y++)
			{
				BlockPos pos = new BlockPos(chunkBaseX + nextX, y, chunkBaseZ + nextZ);
				BlockState state = c.getBlockState(pos);

				if (!state.getBlock().isAir(state, player.world, pos))
				{
					countMap.merge(state, 1, Integer::sum);
				}
			}

			if (nextX == 15)
			{
				nextZ++;
				nextX = 0;

				if (nextZ == 16)
				{
					finishScanning();
					break;
				}
			}
			else
			{
				nextX++;
			}
		}

		super.detectAndSendChanges();
	}

	private void finishScanning()
	{
		List<BlockResult> resultList = new ArrayList<>();
		Map<String, BlockResult> nameMap = new HashMap<>();

		for (Entry<BlockState, Integer> entry : countMap.entrySet())
		{
			BlockState state = entry.getKey();

			BlockResult br = new BlockResult();
			br.state = state;
			br.count = entry.getValue();

			Item item = state.getBlock().asItem();

			String name;

			if (item == Items.AIR)
			{
				// No real item for this block (e.g. fire, fluids) - fall back to
				// the block's own translated name, matching 1.12.2's
				// getLocalizedName() fallback.
				name = new TranslationTextComponent(state.getBlock().getTranslationKey()).getString();
				br.stack = ItemStack.EMPTY;
			}
			else
			{
				br.stack = new ItemStack(item);
				name = br.stack.getDisplayName().getString();
			}

			br.name = name;

			BlockResult existing = nameMap.get(name);

			if (existing != null)
			{
				existing.count += br.count;
			}
			else
			{
				nameMap.put(name, br);
				resultList.add(br);
			}
		}

		resultList.sort((a, b) -> b.count - a.count);

		ChunkAnalyzerResult result = new ChunkAnalyzerResult();

		for (BlockResult br : resultList)
		{
			result.addBlock(br.stack, br.name, br.count);
		}

		ItemStack analyzer = player.getHeldItemMainhand();

		if (!analyzer.isEmpty() && analyzer.getItem() instanceof ChunkAnalyzerItem)
		{
			CompoundNBT tag = analyzer.getOrCreateChildTag("result");
			result.writeToNBT(tag);

			player.container.detectAndSendChanges();
		}

		isScanning = false;
		scanning.set(0);
		countMap = null;
	}

	@Override
	public boolean canInteractWith(PlayerEntity playerIn)
	{
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
	{
		return ItemStack.EMPTY;
	}

	@Override
	public void handle(int id, PacketBuffer data)
	{
		if (id == 0)
		{
			startScanning();
		}
	}
}
