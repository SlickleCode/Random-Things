package lumien.randomthings.tileentity;

import lumien.randomthings.block.plates.ExtractionPlateBlock;
import lumien.randomthings.container.ExtractionPlateContainer;
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

/**
 * Pulls items out of the inventory it (or its output-side neighbour) sits on
 * top of and spits them out as dropped items toward its output facing.
 */
public class ExtractionPlateTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider
{
	private Direction extractFacing = Direction.DOWN;

	public ExtractionPlateTileEntity()
	{
		super(ModTileEntityTypes.EXTRACTION_PLATE);
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		compound.putInt("extractFacing", extractFacing.ordinal());
		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		this.extractFacing = Direction.byIndex(compound.getInt("extractFacing"));
	}

	public Direction extractFacing()
	{
		return extractFacing;
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

		Direction outputFacing = this.getBlockState().get(ExtractionPlateBlock.OUTPUT_FACING);

		for (int i = 0; i < 2; i++)
		{
			Direction targetFacing = i == 0 ? Direction.DOWN : outputFacing.getOpposite();
			TileEntity target = this.world.getTileEntity(this.pos.offset(targetFacing));

			if (target == null)
			{
				continue;
			}

			LazyOptional<IItemHandler> capability = target.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, extractFacing);
			IItemHandler itemHandler = capability.orElse(null);

			if (itemHandler == null)
			{
				continue;
			}

			for (int slot = 0; slot < itemHandler.getSlots(); slot++)
			{
				ItemStack extracted = itemHandler.extractItem(slot, 64, false);

				if (!extracted.isEmpty())
				{
					spitOut(extracted, outputFacing);
					return;
				}
			}
		}
	}

	private void spitOut(ItemStack stack, Direction outputFacing)
	{
		Vec3d dirVec = new Vec3d(outputFacing.getDirectionVec());
		Vec3d spawnPos = dirVec.scale(0.53).add(new Vec3d(pos).add(0.5, 0, 0.5));
		Vec3d motion = dirVec.scale(0.1);

		ItemEntity itemEntity = new ItemEntity(this.world, spawnPos.x, spawnPos.y, spawnPos.z, stack);
		itemEntity.setMotion(motion);
		itemEntity.setPickupDelay(10);

		((ServerWorld) this.world).addEntity(itemEntity);
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new ExtractionPlateContainer(windowId, IWorldPosCallable.of(this.world, pos));
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.plate_extraction");
	}
}
