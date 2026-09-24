package lumien.randomthings.tileentity;

import java.util.List;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.container.EntityDetectorContainer;
import lumien.randomthings.lib.IEntityFilterItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Emits redstone based on whether any entity matching a configurable filter
 * (and class, or an item-based CUSTOM filter via {@link IEntityFilterItem})
 * is within a configurable radius. CUSTOM mode matches whatever item sits in
 * its one slot; with nothing in the slot, or a non-filter item, it behaves
 * like "no entities ever match". Simplification, disclosed here: 1.12.2's
 * ANIMAL/MONSTER filters matched the marker interfaces
 * {@code IAnimals}/{@code IMob}; {@code IAnimals} was removed entirely in
 * 1.14.4, and {@code IMob} isn't itself an {@code Entity} subtype so it
 * can't be used as a {@code Class<? extends Entity>} filter here either -
 * both were substituted with their closest concrete equivalents,
 * {@code AnimalEntity} and {@code MonsterEntity}.
 */
public class EntityDetectorTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider
{
	public enum FILTER
	{
		ALL(Entity.class), LIVING(LivingEntity.class), ANIMAL(AnimalEntity.class), MONSTER(MonsterEntity.class), PLAYER(PlayerEntity.class), ITEMS(ItemEntity.class), CUSTOM(null);

		final Class<? extends Entity> filterClass;

		FILTER(Class<? extends Entity> filterClass)
		{
			this.filterClass = filterClass;
		}
	}

	private static final int MAX_RANGE = 10;

	private boolean powered;

	private int rangeX = 5;
	private int rangeY = 5;
	private int rangeZ = 5;

	private boolean invert;
	private boolean strongOutput;

	private FILTER filter = FILTER.ALL;

	private final ItemStackHandler filterInventory = new ItemStackHandler(1)
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			markDirty();
		}
	};

	public EntityDetectorTileEntity()
	{
		super(ModTileEntityTypes.ENTITY_DETECTOR);
	}

	public ItemStackHandler filterInventory()
	{
		return filterInventory;
	}

	public boolean isPowered()
	{
		return powered;
	}

	public boolean strongOutput()
	{
		return strongOutput;
	}

	public boolean invert()
	{
		return invert;
	}

	public FILTER getFilter()
	{
		return filter;
	}

	public int getRangeX()
	{
		return rangeX;
	}

	public int getRangeY()
	{
		return rangeY;
	}

	public int getRangeZ()
	{
		return rangeZ;
	}

	public void adjustRangeX(int amount)
	{
		rangeX = clampRange(rangeX + amount);
		this.markDirty();
	}

	public void adjustRangeY(int amount)
	{
		rangeY = clampRange(rangeY + amount);
		this.markDirty();
	}

	public void adjustRangeZ(int amount)
	{
		rangeZ = clampRange(rangeZ + amount);
		this.markDirty();
	}

	private static int clampRange(int value)
	{
		return Math.max(0, Math.min(MAX_RANGE, value));
	}

	public void cycleFilter()
	{
		int next = (filter.ordinal() + 1) % FILTER.values().length;
		filter = FILTER.values()[next];
		this.markDirty();
	}

	public void toggleInvert()
	{
		invert = !invert;
		this.markDirty();
	}

	public void toggleStrongOutput()
	{
		strongOutput = !strongOutput;
		this.markDirty();

		notifyAllSides();
	}

	private void notifyAllSides()
	{
		this.world.notifyNeighborsOfStateChange(pos, ModBlocks.ENTITY_DETECTOR);

		if (strongOutput)
		{
			for (Direction facing : Direction.values())
			{
				this.world.notifyNeighborsOfStateChange(pos.offset(facing), ModBlocks.ENTITY_DETECTOR);
			}
		}
	}

	@Override
	public void tick()
	{
		if (this.world.isRemote)
		{
			return;
		}

		boolean newPowered = checkSupposedPoweredState();

		if (newPowered != powered)
		{
			powered = newPowered;
			this.markDirty();
			notifyAllSides();
		}
	}

	private boolean checkSupposedPoweredState()
	{
		AxisAlignedBB box = new AxisAlignedBB(pos, pos.add(1, 1, 1)).grow(rangeX, rangeY, rangeZ);

		if (filter.filterClass == null)
		{
			ItemStack filterItem = filterInventory.getStackInSlot(0);
			IEntityFilterItem filterInstance = !filterItem.isEmpty() && filterItem.getItem() instanceof IEntityFilterItem ? (IEntityFilterItem) filterItem.getItem() : null;

			if (filterInstance == null)
			{
				return invert;
			}

			List<Entity> entities = this.world.getEntitiesWithinAABB(Entity.class, box, e -> filterInstance.apply(filterItem, e));
			return invert != !entities.isEmpty();
		}

		List<? extends Entity> entities = this.world.getEntitiesWithinAABB(filter.filterClass, box, e -> true);

		return invert != !entities.isEmpty();
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);

		compound.putBoolean("powered", powered);
		compound.putInt("rangeX", rangeX);
		compound.putInt("rangeY", rangeY);
		compound.putInt("rangeZ", rangeZ);
		compound.putInt("filter", filter.ordinal());
		compound.putBoolean("invert", invert);
		compound.putBoolean("strongOutput", strongOutput);
		compound.put("inventory", filterInventory.serializeNBT());

		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);

		powered = compound.getBoolean("powered");
		rangeX = compound.getInt("rangeX");
		rangeY = compound.getInt("rangeY");
		rangeZ = compound.getInt("rangeZ");
		filter = FILTER.values()[compound.getInt("filter")];
		invert = compound.getBoolean("invert");
		strongOutput = compound.getBoolean("strongOutput");
		filterInventory.deserializeNBT(compound.getCompound("inventory"));
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new EntityDetectorContainer(windowId, playerInventory, filterInventory, IWorldPosCallable.of(this.world, pos));
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.entity_detector");
	}
}
