package lumien.randomthings.tileentity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * A re-skinned vanilla chest. Extends {@link ChestTileEntity} directly -
 * `javap` confirmed its lid-angle animation, player-use-counting, inventory
 * read/write and {@code createMenu} (which builds a plain, non-merging
 * vanilla {@code ChestContainer.createGeneric9X3}, with zero reference to
 * {@code ChestBlock}) are all already exactly what this block needs, so none
 * of that ~150 lines of 1.12.2 hand-rolled animation/open-close-sound logic
 * needed porting by hand. Only {@code chestType} (which texture the block's
 * custom renderer should use) is added.
 */
public class SpecialChestTileEntity extends ChestTileEntity
{
	private int chestType;

	public SpecialChestTileEntity()
	{
		super(ModTileEntityTypes.SPECIAL_CHEST);
	}

	public SpecialChestTileEntity(int chestType)
	{
		this();
		this.chestType = chestType;
	}

	public int getChestType()
	{
		return chestType;
	}

	public void setChestType(int chestType)
	{
		this.chestType = chestType;
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);
		compound.putInt("chestType", chestType);
		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);
		this.chestType = compound.getInt("chestType");
	}

	@Override
	protected ITextComponent getDefaultName()
	{
		return new TranslationTextComponent(chestType == 1 ? "block.randomthings.special_chest_water" : "block.randomthings.special_chest_nature");
	}
}
