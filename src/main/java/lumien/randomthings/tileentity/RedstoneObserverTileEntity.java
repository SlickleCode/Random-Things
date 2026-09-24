package lumien.randomthings.tileentity;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.container.RedstoneObserverContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.world.BlockEvent.NeighborNotifyEvent;

/**
 * A "remote redstone wire": mirrors the weak/strong power of a distant
 * targeted block (set via {@link lumien.randomthings.item.RedstoneToolItem})
 * onto itself, refreshed whenever that target's neighbors change.
 */
public class RedstoneObserverTileEntity extends TileEntity implements INamedContainerProvider
{
	public static final Set<RedstoneObserverTileEntity> loadedObservers = Collections.newSetFromMap(new WeakHashMap<>());

	private BlockPos target;

	private final Map<Direction, Integer> weakPower = new EnumMap<>(Direction.class);
	private final Map<Direction, Integer> strongPower = new EnumMap<>(Direction.class);

	public RedstoneObserverTileEntity()
	{
		super(ModTileEntityTypes.REDSTONE_OBSERVER);

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
		for (RedstoneObserverTileEntity observer : loadedObservers)
		{
			if (observer.world == event.getWorld() && !observer.isRemoved() && observer.target != null && observer.target.equals(event.getPos()))
			{
				observer.updateRedstoneState();
			}
		}
	}

	private boolean updating = false;

	private void updateRedstoneState()
	{
		if (updating || this.world == null)
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
			BlockState targetState = this.world.getBlockState(target);

			for (Direction f : Direction.values())
			{
				strongPower.put(f, targetState.getStrongPower(this.world, target, f));
				weakPower.put(f, targetState.getWeakPower(this.world, target, f));
			}

			this.world.notifyNeighborsOfStateChange(this.pos, ModBlocks.REDSTONE_OBSERVER);
		}

		this.markDirty();
		updating = false;
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
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);

		if (target != null)
		{
			compound.putInt("targetX", target.getX());
			compound.putInt("targetY", target.getY());
			compound.putInt("targetZ", target.getZ());
		}

		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);

		if (compound.contains("targetX"))
		{
			target = new BlockPos(compound.getInt("targetX"), compound.getInt("targetY"), compound.getInt("targetZ"));
		}
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new RedstoneObserverContainer(windowId, IWorldPosCallable.of(this.world, pos));
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.redstone_observer");
	}
}
