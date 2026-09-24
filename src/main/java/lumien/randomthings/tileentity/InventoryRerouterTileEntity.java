package lumien.randomthings.tileentity;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;

import javax.annotation.Nullable;

import lumien.randomthings.block.InventoryRerouterBlock;
import lumien.randomthings.block.ModBlocks;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;

/**
 * Redirects capability access on its non-facing sides to whatever inventory
 * sits on its facing side, on a per-requesting-side basis (each side can be
 * mapped to a different real side of the target, or disabled entirely).
 * <p>
 * Simplification, disclosed here: the original also drew small per-face
 * arrow decals (via a custom {@code IUnlistedProperty}/baked model) showing
 * each side's current mapping. That overlay rendering is dropped - the
 * block looks identical regardless of the current mapping, matching the
 * precedent already set by this mod's redirector/redstone plates.
 */
public class InventoryRerouterTileEntity extends TileEntity
{
	private static final HashSet<InventoryRerouterTileEntity> circleGuard = new HashSet<>();

	private final Map<Direction, Direction> facingMap = new EnumMap<>(Direction.class);

	public InventoryRerouterTileEntity()
	{
		super(ModTileEntityTypes.INVENTORY_REROUTER);

		for (Direction f : Direction.values())
		{
			facingMap.put(f, f);
		}
	}

	public Map<Direction, Direction> getFacingMap()
	{
		return facingMap;
	}

	public void rotateFacing(Direction facing)
	{
		Direction current = facingMap.get(facing);

		Direction next;
		if (current == null)
		{
			next = Direction.values()[0];
		}
		else if (current == Direction.EAST)
		{
			next = null;
		}
		else
		{
			next = Direction.values()[current.ordinal() + 1];
		}

		facingMap.put(facing, next);
		this.markDirty();

		if (this.world != null)
		{
			this.world.notifyNeighborsOfStateChange(this.pos, ModBlocks.INVENTORY_REROUTER);
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side)
	{
		if (circleGuard.contains(this))
		{
			return LazyOptional.empty();
		}

		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && this.world != null && side != null)
		{
			Direction myFacing = this.getBlockState().get(InventoryRerouterBlock.FACING);

			if (myFacing != side)
			{
				Direction override = facingMap.get(side);

				if (override == null)
				{
					return LazyOptional.empty();
				}

				TileEntity facingTE = this.world.getTileEntity(this.pos.offset(myFacing));

				if (facingTE == null)
				{
					return LazyOptional.empty();
				}

				circleGuard.add(this);
				LazyOptional<T> result = facingTE.getCapability(cap, override);
				circleGuard.remove(this);

				return result;
			}
		}

		return super.getCapability(cap, side);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);

		for (Direction f : Direction.values())
		{
			Direction override = facingMap.get(f);
			compound.putInt("facing" + f.ordinal(), override == null ? -1 : override.ordinal());
		}

		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);

		for (Direction f : Direction.values())
		{
			int idx = compound.getInt("facing" + f.ordinal());
			facingMap.put(f, idx == -1 ? null : Direction.values()[idx]);
		}
	}
}
