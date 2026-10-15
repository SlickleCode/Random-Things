package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import java.util.List;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.container.EntityDetectorContainer;
import lumien.randomthings.lib.IEntityFilterItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraft.world.level.block.state.BlockState;

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
public class EntityDetectorTileEntity extends BlockEntity implements MenuProvider
{
	public enum FILTER
	{
		ALL(Entity.class), LIVING(LivingEntity.class), ANIMAL(Animal.class), MONSTER(Monster.class), PLAYER(Player.class), ITEMS(ItemEntity.class), CUSTOM(null);

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
			setChanged();
		}
	};

	public EntityDetectorTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.ENTITY_DETECTOR, pos, state);
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
		this.setChanged();
	}

	public void adjustRangeY(int amount)
	{
		rangeY = clampRange(rangeY + amount);
		this.setChanged();
	}

	public void adjustRangeZ(int amount)
	{
		rangeZ = clampRange(rangeZ + amount);
		this.setChanged();
	}

	private static int clampRange(int value)
	{
		return Math.max(0, Math.min(MAX_RANGE, value));
	}

	public void cycleFilter()
	{
		int next = (filter.ordinal() + 1) % FILTER.values().length;
		filter = FILTER.values()[next];
		this.setChanged();
	}

	public void toggleInvert()
	{
		invert = !invert;
		this.setChanged();
	}

	public void toggleStrongOutput()
	{
		strongOutput = !strongOutput;
		this.setChanged();

		notifyAllSides();
	}

	private void notifyAllSides()
	{
		this.level.updateNeighborsAt(worldPosition, ModBlocks.ENTITY_DETECTOR);

		if (strongOutput)
		{
			for (Direction facing : Direction.values())
			{
				this.level.updateNeighborsAt(worldPosition.relative(facing), ModBlocks.ENTITY_DETECTOR);
			}
		}
	}

	public void tick()
	{
		if (this.level.isClientSide)
		{
			return;
		}

		boolean newPowered = checkSupposedPoweredState();

		if (newPowered != powered)
		{
			powered = newPowered;
			this.setChanged();
			notifyAllSides();
		}
	}

	private boolean checkSupposedPoweredState()
	{
		AABB box = new AABB(worldPosition, worldPosition.offset(1, 1, 1)).inflate(rangeX, rangeY, rangeZ);

		if (filter.filterClass == null)
		{
			ItemStack filterItem = filterInventory.getStackInSlot(0);
			IEntityFilterItem filterInstance = !filterItem.isEmpty() && filterItem.getItem() instanceof IEntityFilterItem ? (IEntityFilterItem) filterItem.getItem() : null;

			if (filterInstance == null)
			{
				return invert;
			}

			List<Entity> entities = this.level.getEntitiesOfClass(Entity.class, box, e -> filterInstance.apply(filterItem, e));
			return invert != !entities.isEmpty();
		}

		List<? extends Entity> entities = this.level.getEntitiesOfClass(filter.filterClass, box, e -> true);

		return invert != !entities.isEmpty();
	}

	@Override
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);

		compound.putBoolean("powered", powered);
		compound.putInt("rangeX", rangeX);
		compound.putInt("rangeY", rangeY);
		compound.putInt("rangeZ", rangeZ);
		compound.putInt("filter", filter.ordinal());
		compound.putBoolean("invert", invert);
		compound.putBoolean("strongOutput", strongOutput);
		compound.put("inventory", filterInventory.serializeNBT());

	}

	@Override
	public void load(CompoundTag compound)
	{
		super.load(compound);

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
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new EntityDetectorContainer(windowId, playerInventory, filterInventory, ContainerLevelAccess.create(this.level, worldPosition));
	}

	@Override
	public Component getDisplayName()
	{
		return new TranslatableComponent("block.randomthings.entity_detector");
	}
}
