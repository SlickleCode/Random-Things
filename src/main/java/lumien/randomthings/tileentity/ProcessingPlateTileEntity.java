package lumien.randomthings.tileentity;

import lumien.randomthings.block.plates.ProcessingPlateBlock;
import lumien.randomthings.container.ProcessingPlateContainer;
import net.minecraft.entity.item.ItemEntity;
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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

/**
 * Combines an extraction feed (pulls items out of the inventory below and
 * ejects them toward its output side, on a timer) with an insertion mouth
 * (any dropped item that touches it gets pushed into the inventory below via
 * its insert side). 1.12.2's cosmetic item-bounce/redirect trajectory math
 * on collision is not reproduced - only the actual item movement is.
 */
public class ProcessingPlateTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider
{
	private Direction insertFacing = Direction.UP;
	private Direction extractFacing = Direction.DOWN;

	public ProcessingPlateTileEntity()
	{
		super(ModTileEntityTypes.PROCESSING_PLATE);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		compound.putInt("insertFacing", insertFacing.ordinal());
		compound.putInt("extractFacing", extractFacing.ordinal());
		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		this.insertFacing = Direction.byIndex(compound.getInt("insertFacing"));
		this.extractFacing = Direction.byIndex(compound.getInt("extractFacing"));
	}

	public Direction insertFacing()
	{
		return insertFacing;
	}

	public Direction extractFacing()
	{
		return extractFacing;
	}

	public void rotateInsertFacing()
	{
		this.insertFacing = Direction.byIndex(insertFacing.ordinal() + 1);
		this.markDirty();
	}

	public void rotateExtractFacing()
	{
		this.extractFacing = Direction.byIndex(extractFacing.ordinal() + 1);
		this.markDirty();
	}

	@Override
	public void tick()
	{
		if (this.world.isRemote || this.world.getGameTime() % 10 != 0)
		{
			return;
		}

		TileEntity target = this.world.getTileEntity(this.pos.down());

		if (target == null)
		{
			return;
		}

		LazyOptional<IItemHandler> capability = target.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, extractFacing);
		IItemHandler itemHandler = capability.orElse(null);

		if (itemHandler == null)
		{
			return;
		}

		for (int slot = 0; slot < itemHandler.getSlots(); slot++)
		{
			ItemStack extracted = itemHandler.extractItem(slot, 64, false);

			if (!extracted.isEmpty())
			{
				Direction outputFacing = this.getBlockState().get(ProcessingPlateBlock.OUTPUT_FACING);

				Vec3d dirVec = new Vec3d(outputFacing.getDirectionVec());
				Vec3d spawnPos = dirVec.scale(0.53).add(new Vec3d(pos).add(0.5, 0, 0.5));
				Vec3d motion = dirVec.scale(0.1);

				ItemEntity itemEntity = new ItemEntity(this.world, spawnPos.x, spawnPos.y, spawnPos.z, extracted);
				itemEntity.setMotion(motion);
				itemEntity.setPickupDelay(10);

				((ServerWorld) this.world).addEntity(itemEntity);
				return;
			}
		}
	}

	/**
	 * Called by the block whenever a dropped item entity touches it: feeds
	 * the item into the inventory below via the insert side, if possible.
	 */
	public void tryInsert(ItemEntity itemEntity)
	{
		if (this.world.isRemote)
		{
			return;
		}

		TileEntity below = this.world.getTileEntity(this.pos.down());

		if (below == null)
		{
			return;
		}

		LazyOptional<IItemHandler> capability = below.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, insertFacing);
		IItemHandler itemHandler = capability.orElse(null);

		if (itemHandler == null)
		{
			return;
		}

		ItemStack remaining = ItemHandlerHelper.insertItem(itemHandler, itemEntity.getItem(), false);

		if (remaining.isEmpty())
		{
			itemEntity.remove();
		}
		else
		{
			itemEntity.setItem(remaining);
		}
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new ProcessingPlateContainer(windowId, IWorldPosCallable.of(this.world, pos));
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.plate_processing");
	}
}
