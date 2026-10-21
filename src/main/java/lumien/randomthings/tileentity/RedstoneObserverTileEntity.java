package lumien.randomthings.tileentity;

import lumien.randomthings.util.StackTag;
import lumien.randomthings.util.WeakSets;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.container.RedstoneObserverContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.level.BlockEvent.NeighborNotifyEvent;

/**
 * A "remote redstone wire": mirrors the weak/strong power of a distant
 * targeted block (set via {@link lumien.randomthings.item.RedstoneToolItem})
 * onto itself, refreshed whenever that target's neighbors change.
 */
public class RedstoneObserverTileEntity extends BlockEntity implements MenuProvider
{
	public static final Set<RedstoneObserverTileEntity> loadedObservers = WeakSets.create();

	private BlockPos target;

	private final Map<Direction, Integer> weakPower = new EnumMap<>(Direction.class);
	private final Map<Direction, Integer> strongPower = new EnumMap<>(Direction.class);

	public RedstoneObserverTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.REDSTONE_OBSERVER, pos, state);

		loadedObservers.add(this);

		for (Direction f : Direction.values())
		{
			weakPower.put(f, 0);
			strongPower.put(f, 0);
    }
	}

	public BlockPos getTarget()
	{
		return target;
	}

	public void setTarget(BlockPos newTarget)
	{
		if (!newTarget.equals(target))
		{
			this.target = newTarget;
			updateRedstoneState();
		}
	}

	public static void notifyNeighbor(NeighborNotifyEvent event)
	{
		for (RedstoneObserverTileEntity observer : WeakSets.snapshot(loadedObservers))
		{
			if (observer.level == event.getLevel() && !observer.isRemoved() && observer.target != null && observer.target.equals(event.getPos()))
			{
				observer.updateRedstoneState();
			}
		}
	}

	private boolean updating = false;

	private void updateRedstoneState()
	{
		if (updating || this.level == null)
		{
			return;
		}

		updating = true;

		if (this.target == null)
		{
			for (Direction f : Direction.values())
			{
				weakPower.put(f, 0);
				strongPower.put(f, 0);
			}
		}
		else
		{
			BlockState targetState = this.level.getBlockState(target);

			for (Direction f : Direction.values())
			{
				strongPower.put(f, targetState.getDirectSignal(this.level, target, f));
				weakPower.put(f, this.level.getSignal(target, f));
			}

			this.level.updateNeighborsAt(this.worldPosition, ModBlocks.REDSTONE_OBSERVER);
		}

		this.setChanged();

		// markDirty() alone only flags the chunk for a disk resave - it does
		// not push anything to already-connected clients. A real dedicated
		// server would never tell a watching client about a newly-set target
		// without this. (Correction, 2026-09-27: the comment this replaced
		// claimed singleplayer's integrated server shares this exact TE
		// object with the client, making a live update unnecessary there -
		// that was never actually true, client and integrated server always
		// have separate World/TileEntity instances even over the loopback
		// connection; see onDataPacket below for the real reason a live
		// update silently did nothing without further changes.)
		if (this.level != null)
		{
			this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
		}

		updating = false;
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket()
	{
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public int getWeakPower(Direction side)
	{
		return weakPower.getOrDefault(side, 0);
	}

	public int getStrongPower(Direction side)
	{
		return strongPower.getOrDefault(side, 0);
	}

	@Override
	protected void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.saveAdditional(compound, registries);

		if (target != null)
		{
			compound.putInt("targetX", target.getX());
			compound.putInt("targetY", target.getY());
			compound.putInt("targetZ", target.getZ());
		}

	}

	/**
	 * {@code TileEntity.getUpdateTag()}'s default implementation calls the
	 * *private* {@code writeInternal()} directly (confirmed via `javap -c`),
	 * not the public, overridable {@code write()} - so it silently carries
	 * only the base id/position, never a subclass's own fields, unless a
	 * subclass overrides this too. That's exactly why a target set *during*
	 * an active session rendered fine (singleplayer's integrated server
	 * shares the same TE object between "client" and "server" logic, no
	 * networking involved) but reloading the world lost it: a genuine reload
	 * re-syncs tile entities to the client over the loopback channel using
	 * this method, and the target was never actually in that payload.
	 */
	@Override
	public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries)
	{
		return saveWithoutMetadata(registries);
	}

	@Override
	public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries)
	{
		super.loadAdditional(compound, registries);

		if (compound.contains("targetX"))
		{
			target = new BlockPos(compound.getInt("targetX"), compound.getInt("targetY"), compound.getInt("targetZ"));
		}
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new RedstoneObserverContainer(windowId, ContainerLevelAccess.create(this.level, worldPosition));
	}

	@Override
	public Component getDisplayName()
	{
		return Component.translatable("block.randomthings.redstone_observer");
	}
}
