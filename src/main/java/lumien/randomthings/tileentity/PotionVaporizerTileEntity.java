package lumien.randomthings.tileentity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import lumien.randomthings.block.PotionVaporizerBlock;
import lumien.randomthings.container.PotionVaporizerContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
public class PotionVaporizerTileEntity extends BlockEntity implements MenuProvider
{
	private static final int MAX_BLOCKS = 100;

	private final HashSet<BlockPos> affectedBlocks = new HashSet<>();

	private MobEffectInstance currentPotionEffect;
	private int durationLeft = 0;

	private int fuelBurn;
	private int fuelBurnTime;

	private final HashSet<BlockPos> validBlocks = new HashSet<>();
	private final HashSet<BlockPos> checkedBlocks = new HashSet<>();
	private final ArrayList<BlockPos> toBeChecked = new ArrayList<>();
	private int checkCounter;
	private boolean firstCheck = true;

	private final ItemStackHandler itemHandler = new PotionVaporizerItemHandler()
	{
		@Override
		protected void onContentsChanged(int slot)
		{
			setChanged();
		}
	};

	public PotionVaporizerTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.POTION_VAPORIZER, pos, state);
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
		return currentPotionEffect != null ? currentPotionEffect.getEffect().getColor() : 0;
	}

	public int getFuelBurnTime()
	{
		return fuelBurnTime;
	}

	public int getFuelBurn()
	{
		return fuelBurn;
	}

	public void tick()
	{
		if (this.level.isClientSide)
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
				Integer burnTime = AbstractFurnaceBlockEntity.getFuel().get(fuelStack.getItem());
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

		List<MobEffectInstance> effects = PotionUtils.getMobEffects(newPotion);

		if (effects.isEmpty() || effects.get(0).getEffect().isInstantenous())
		{
			return;
		}

		currentPotionEffect = new MobEffectInstance(effects.get(0));
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

		AABB[] boxes = new AABB[affectedBlocks.size()];
		int i = 0;
		for (BlockPos pos : affectedBlocks)
		{
			boxes[i++] = new AABB(pos, pos.offset(1, 1, 1));
		}

		AABB bounds = null;
		for (AABB box : boxes)
		{
			bounds = bounds == null ? box : bounds.minmax(box);
		}

		if (bounds != null)
		{
			for (LivingEntity entity : this.level.getEntitiesOfClass(LivingEntity.class, bounds))
			{
				boolean inside = false;
				for (AABB box : boxes)
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

				MobEffectInstance activeEffect = entity.getEffect(currentPotionEffect.getEffect());
				boolean isNightVision = currentPotionEffect.getEffect() == MobEffects.NIGHT_VISION;

				if (activeEffect == null || activeEffect.getDuration() < (isNightVision ? 205 : 3))
				{
					entity.addEffect(new MobEffectInstance(currentPotionEffect.getEffect(), isNightVision ? 205 : 80, currentPotionEffect.getAmplifier(), currentPotionEffect.isAmbient(), currentPotionEffect.isVisible()));
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
		if (currentPotionEffect == null || this.level.getGameTime() % 5 != 0)
		{
			return;
		}

		int color = currentPotionEffect.getEffect().getColor();
		float r = ((color >> 16) & 0xFF) / 255F;
		float g = ((color >> 8) & 0xFF) / 255F;
		float b = (color & 0xFF) / 255F;

		DustParticleOptions data = new DustParticleOptions(new com.mojang.math.Vector3f(r, g, b), 1.0F);

		for (BlockPos p : affectedBlocks)
		{
			((ServerLevel) this.level).sendParticles(data, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 1, 0.4, 0.4, 0.4, 0.0);
		}
	}

	private void stepRoomDetection()
	{
		if (firstCheck)
		{
			Direction facing = this.getBlockState().getValue(PotionVaporizerBlock.FACING);
			toBeChecked.add(this.worldPosition.relative(facing));
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

				if (this.level.hasChunkAt(toCheck) && isRoomSpace(toCheck))
				{
					validBlocks.add(toCheck);
					checkCounter++;

					for (Direction facing : Direction.values())
					{
						BlockPos next = toCheck.relative(facing);

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

	/**
	 * Whether the flood-fill should treat this position as part of the
	 * enclosed room (continuing the search through it, and including it in
	 * {@link #affectedBlocks} so entities standing in it get the potion
	 * effect). 1.12.2's original (and this port, until now) used a plain
	 * {@code world.isAirBlock(pos)} check here - confirmed via
	 * {@code git show origin/1.12.2}, not a porting gap - so a torch, sign,
	 * button, or any other non-full block placed inside the room would stop
	 * the flood-fill right there, treating it like a solid wall. Changed per
	 * explicit user request ("ignore non-full blocks like torches") to also
	 * accept any block with no collision shape (torches, signs, buttons,
	 * levers, flowers, tripwire, etc. all qualify; slabs/stairs/fences do
	 * not, since they DO have real collision) - a deliberate divergence from
	 * both the original and this port's own prior behavior, not a bug fix.
	 */
	private boolean isRoomSpace(BlockPos pos)
	{
		BlockState state = this.level.getBlockState(pos);

		return state.isAir() || state.getCollisionShape(this.level, pos).isEmpty();
	}

	private void resetSearch()
	{
		affectedBlocks.clear();
		affectedBlocks.addAll(validBlocks);

		checkCounter = 0;
		toBeChecked.clear();
		validBlocks.clear();
		checkedBlocks.clear();

		Direction facing = this.getBlockState().getValue(PotionVaporizerBlock.FACING);
		toBeChecked.add(this.worldPosition.relative(facing));
	}

	@Override
	protected void saveAdditional(CompoundTag compound)
	{
		super.saveAdditional(compound);

		compound.putInt("durationLeft", durationLeft);
		compound.putInt("fuelBurn", fuelBurn);
		compound.putInt("fuelBurnTime", fuelBurnTime);

		if (currentPotionEffect != null)
		{
			compound.put("currentPotionEffect", currentPotionEffect.save(new CompoundTag()));
		}

		ListTag affectedList = new ListTag();
		for (BlockPos p : affectedBlocks)
		{
			CompoundTag posCompound = new CompoundTag();
			posCompound.putInt("posX", p.getX());
			posCompound.putInt("posY", p.getY());
			posCompound.putInt("posZ", p.getZ());
			affectedList.add(posCompound);
		}
		compound.put("affectedBlocks", affectedList);

		compound.put("inventory", itemHandler.serializeNBT());

	}

	@Override
	public void load(CompoundTag compound)
	{
		super.load(compound);

		durationLeft = compound.getInt("durationLeft");
		fuelBurn = compound.getInt("fuelBurn");
		fuelBurnTime = compound.getInt("fuelBurnTime");

		if (compound.contains("currentPotionEffect"))
		{
			currentPotionEffect = MobEffectInstance.load(compound.getCompound("currentPotionEffect"));
		}

		affectedBlocks.clear();
		ListTag affectedList = compound.getList("affectedBlocks", 10);
		for (int i = 0; i < affectedList.size(); i++)
		{
			CompoundTag posCompound = affectedList.getCompound(i);
			affectedBlocks.add(new BlockPos(posCompound.getInt("posX"), posCompound.getInt("posY"), posCompound.getInt("posZ")));
		}

		itemHandler.deserializeNBT(compound.getCompound("inventory"));
	}

	@Override
	public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity)
	{
		return new PotionVaporizerContainer(windowId, playerInventory, itemHandler, ContainerLevelAccess.create(this.level, worldPosition));
	}

	@Override
	public Component getDisplayName()
	{
		return Component.translatable("block.randomthings.potion_vaporizer");
	}
}
