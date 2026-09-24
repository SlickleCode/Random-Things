package lumien.randomthings.tileentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import lumien.randomthings.block.PotionVaporizerBlock;
import lumien.randomthings.container.PotionVaporizerContainer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.AbstractFurnaceTileEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Flood-fills the enclosed air space it faces (up to 100 blocks) and applies
 * a potion effect (burning fuel + a potion item over time) to every living
 * entity inside that space.
 * <p>
 * Simplification, disclosed here: the original broadcast a dedicated
 * network message to nearby players for the per-block ambient particles;
 * this port just calls {@code ServerWorld.spawnParticle}, which Forge/vanilla
 * already broadcasts to nearby players on its own, so no custom packet is
 * needed. Colored dust particles ({@code RedstoneParticleData}, tinted to
 * the active potion's color) stand in for the original's bespoke potion-
 * vapor particle.
 */
public class PotionVaporizerTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider
{
	private static final int MAX_BLOCKS = 100;

	private final HashSet<BlockPos> affectedBlocks = new HashSet<>();

	private EffectInstance currentPotionEffect;
	private int durationLeft = 0;

	private int fuelBurn;
	private int fuelBurnTime;

	private final HashSet<BlockPos> validBlocks = new HashSet<>();
	private final HashSet<BlockPos> checkedBlocks = new HashSet<>();
	private final ArrayList<BlockPos> toBeChecked = new ArrayList<>();
	private int checkCounter;
	private boolean firstCheck = true;

	private final ItemStackHandler itemHandler = new ItemStackHandler(3)
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			markDirty();
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack)
		{
			switch (slot)
			{
				case 0:
					return AbstractFurnaceTileEntity.isFuel(stack);
				case 1:
					List<EffectInstance> effects = PotionUtils.getEffectsFromStack(stack);
					return stack.getItem() == Items.POTION && !effects.isEmpty() && !effects.get(0).getPotion().isInstant();
				case 2:
					return false;
				default:
					return false;
			}
		}
	};

	public PotionVaporizerTileEntity()
	{
		super(ModTileEntityTypes.POTION_VAPORIZER);
	}

	public ItemStackHandler itemHandler()
	{
		return itemHandler;
	}

	public int getDurationLeft()
	{
		return durationLeft;
	}

	public int getDuration()
	{
		return currentPotionEffect != null ? currentPotionEffect.getDuration() : 0;
	}

	public int getColor()
	{
		return currentPotionEffect != null ? currentPotionEffect.getPotion().getLiquidColor() : 0;
	}

	public int getFuelBurnTime()
	{
		return fuelBurnTime;
	}

	public int getFuelBurn()
	{
		return fuelBurn;
	}

	@Override
	public void tick()
	{
		if (this.world.isRemote)
		{
			return;
		}

		int roomSteps = affectedBlocks.isEmpty() ? 5 : 2;
		for (int i = 0; i < roomSteps; i++)
		{
			stepRoomDetection();
		}

		stepPotionTank();
		stepFuel();

		if (fuelBurnTime > 0 && !affectedBlocks.isEmpty())
		{
			stepPotionEffect();
			spawnParticles();
		}
	}

	private void stepFuel()
	{
		if (fuelBurnTime > 0)
		{
			fuelBurnTime--;
		}
		else if (currentPotionEffect != null && !affectedBlocks.isEmpty())
		{
			ItemStack fuelStack = itemHandler.getStackInSlot(0);

			if (!fuelStack.isEmpty() && durationLeft > 0)
			{
				Integer burnTime = AbstractFurnaceTileEntity.getBurnTimes().get(fuelStack.getItem());
				fuelBurnTime = fuelBurn = burnTime != null ? burnTime : 0;

				itemHandler.extractItem(0, 1, false);
			}
		}
	}

	private void stepPotionTank()
	{
		if (currentPotionEffect != null)
		{
			return;
		}

		ItemStack newPotion = itemHandler.getStackInSlot(1);

		if (newPotion.isEmpty())
		{
			return;
		}

		ItemStack output = itemHandler.getStackInSlot(2);

		if (!output.isEmpty() && output.getCount() >= 64)
		{
			return;
		}

		List<EffectInstance> effects = PotionUtils.getEffectsFromStack(newPotion);

		if (effects.isEmpty() || effects.get(0).getPotion().isInstant())
		{
			return;
		}

		currentPotionEffect = new EffectInstance(effects.get(0));
		durationLeft = currentPotionEffect.getDuration();

		itemHandler.extractItem(1, 1, false);

		if (!output.isEmpty())
		{
			output.grow(1);
			itemHandler.setStackInSlot(2, output);
		}
		else
		{
			itemHandler.setStackInSlot(2, new ItemStack(Items.GLASS_BOTTLE));
		}
	}

	private void stepPotionEffect()
	{
		if (currentPotionEffect == null)
		{
			return;
		}

		durationLeft--;

		AxisAlignedBB[] boxes = new AxisAlignedBB[affectedBlocks.size()];
		int i = 0;
		for (BlockPos pos : affectedBlocks)
		{
			boxes[i++] = new AxisAlignedBB(pos, pos.add(1, 1, 1));
		}

		AxisAlignedBB bounds = null;
		for (AxisAlignedBB box : boxes)
		{
			bounds = bounds == null ? box : bounds.union(box);
		}

		if (bounds != null)
		{
			for (LivingEntity entity : this.world.getEntitiesWithinAABB(LivingEntity.class, bounds))
			{
				boolean inside = false;
				for (AxisAlignedBB box : boxes)
				{
					if (box.intersects(entity.getBoundingBox()))
					{
						inside = true;
						break;
					}
				}

				if (!inside)
				{
					continue;
				}

				EffectInstance activeEffect = entity.getActivePotionEffect(currentPotionEffect.getPotion());
				boolean isNightVision = currentPotionEffect.getPotion() == Effects.NIGHT_VISION;

				if (activeEffect == null || activeEffect.getDuration() < (isNightVision ? 205 : 3))
				{
					entity.addPotionEffect(new EffectInstance(currentPotionEffect.getPotion(), isNightVision ? 205 : 80, currentPotionEffect.getAmplifier(), currentPotionEffect.isAmbient(), currentPotionEffect.doesShowParticles()));
				}
			}
		}

		if (durationLeft <= 0)
		{
			currentPotionEffect = null;
		}
	}

	private void spawnParticles()
	{
		if (currentPotionEffect == null || this.world.getGameTime() % 5 != 0)
		{
			return;
		}

		int color = currentPotionEffect.getPotion().getLiquidColor();
		float r = ((color >> 16) & 0xFF) / 255F;
		float g = ((color >> 8) & 0xFF) / 255F;
		float b = (color & 0xFF) / 255F;

		RedstoneParticleData data = new RedstoneParticleData(r, g, b, 1.0F);

		for (BlockPos p : affectedBlocks)
		{
			((ServerWorld) this.world).spawnParticle(data, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 1, 0.4, 0.4, 0.4, 0.0);
		}
	}

	private void stepRoomDetection()
	{
		if (firstCheck)
		{
			Direction facing = this.getBlockState().get(PotionVaporizerBlock.FACING);
			toBeChecked.add(this.pos.offset(facing));
			firstCheck = false;
		}

		if (checkCounter > MAX_BLOCKS)
		{
			affectedBlocks.clear();
			validBlocks.clear();
			resetSearch();
			return;
		}

		if (!toBeChecked.isEmpty())
		{
			BlockPos toCheck = toBeChecked.remove(0);

			if (!checkedBlocks.contains(toCheck))
			{
				checkedBlocks.add(toCheck);

				if (this.world.isBlockLoaded(toCheck) && this.world.isAirBlock(toCheck))
				{
					validBlocks.add(toCheck);
					checkCounter++;

					for (Direction facing : Direction.values())
					{
						BlockPos next = toCheck.offset(facing);

						if (!checkedBlocks.contains(next))
						{
							toBeChecked.add(next);
						}
					}
				}
			}
		}
		else
		{
			resetSearch();
		}
	}

	private void resetSearch()
	{
		affectedBlocks.clear();
		affectedBlocks.addAll(validBlocks);

		checkCounter = 0;
		toBeChecked.clear();
		validBlocks.clear();
		checkedBlocks.clear();

		Direction facing = this.getBlockState().get(PotionVaporizerBlock.FACING);
		toBeChecked.add(this.pos.offset(facing));
	}

	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		super.write(compound);

		compound.putInt("durationLeft", durationLeft);
		compound.putInt("fuelBurn", fuelBurn);
		compound.putInt("fuelBurnTime", fuelBurnTime);

		if (currentPotionEffect != null)
		{
			compound.put("currentPotionEffect", currentPotionEffect.write(new CompoundNBT()));
		}

		ListNBT affectedList = new ListNBT();
		for (BlockPos p : affectedBlocks)
		{
			CompoundNBT posCompound = new CompoundNBT();
			posCompound.putInt("posX", p.getX());
			posCompound.putInt("posY", p.getY());
			posCompound.putInt("posZ", p.getZ());
			affectedList.add(posCompound);
		}
		compound.put("affectedBlocks", affectedList);

		compound.put("inventory", itemHandler.serializeNBT());

		return compound;
	}

	@Override
	public void read(CompoundNBT compound)
	{
		super.read(compound);

		durationLeft = compound.getInt("durationLeft");
		fuelBurn = compound.getInt("fuelBurn");
		fuelBurnTime = compound.getInt("fuelBurnTime");

		if (compound.contains("currentPotionEffect"))
		{
			currentPotionEffect = EffectInstance.read(compound.getCompound("currentPotionEffect"));
		}

		affectedBlocks.clear();
		ListNBT affectedList = compound.getList("affectedBlocks", 10);
		for (int i = 0; i < affectedList.size(); i++)
		{
			CompoundNBT posCompound = affectedList.getCompound(i);
			affectedBlocks.add(new BlockPos(posCompound.getInt("posX"), posCompound.getInt("posY"), posCompound.getInt("posZ")));
		}

		itemHandler.deserializeNBT(compound.getCompound("inventory"));
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity)
	{
		return new PotionVaporizerContainer(windowId, playerInventory, itemHandler, IWorldPosCallable.of(this.world, pos));
	}

	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent("block.randomthings.potion_vaporizer");
	}
}
