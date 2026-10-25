package lumien.randomthings.debug.test;

import lumien.randomthings.util.StackTag;
import net.minecraft.core.registries.Registries;

import lumien.randomthings.util.RegUtil;
import java.util.ArrayList;
import java.util.List;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.minecraft.core.registries.BuiltInRegistries;

/**
 * Persistence tests for every registered tile entity, entity type and item: write to NBT, read
 * into a fresh instance, write again and require identical NBT, and require the item / fluid /
 * energy capabilities (filled with test content first) to come back unchanged.
 */
final class RoundTripTests
{
	private static final Direction[] BLOCK_SIDES;
	private static final Direction[] STACK_SIDES = { null };

	static
	{
		Direction[] values = Direction.values();
		BLOCK_SIDES = new Direction[values.length + 1];
		System.arraycopy(values, 0, BLOCK_SIDES, 1, values.length);
	}

	private RoundTripTests()
	{
	}

	static List<RTTest> create()
	{
		List<RTTest> tests = new ArrayList<>();

		for (BlockEntityType<?> type : BuiltInRegistries.BLOCK_ENTITY_TYPE)
		{
			if (RegUtil.id(type) != null && ModConstants.MOD_ID.equals(RegUtil.id(type).getNamespace()))
			{
				tests.add(RTTest.named("nbt.tile_entity." + RegUtil.id(type).getPath()).run(c -> tileEntity(c, type)));
			}
		}

		for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE)
		{
			if (RegUtil.id(type) != null && ModConstants.MOD_ID.equals(RegUtil.id(type).getNamespace()))
			{
				tests.add(RTTest.named("nbt.entity." + RegUtil.id(type).getPath()).run(c -> entity(c, type)));
			}
		}

		for (Item item : BuiltInRegistries.ITEM)
		{
			if (RegUtil.id(item) != null && ModConstants.MOD_ID.equals(RegUtil.id(item).getNamespace()))
			{
				tests.add(RTTest.named("nbt.item." + RegUtil.id(item).getPath()).run(c -> item(c, item)));
			}
		}

		return tests;
	}

	// ---------------------------------------------------------------------------------------------

	private static void tileEntity(TestCtx c, BlockEntityType<?> type)
	{
		Block block = null;

		for (Block candidate : BuiltInRegistries.BLOCK)
		{
			if (RegUtil.id(candidate) != null && ModConstants.MOD_ID.equals(RegUtil.id(candidate).getNamespace()) && type.isValid(candidate.defaultBlockState()))
			{
				block = candidate;
				break;
			}
		}

		if (block == null)
		{
			throw new TestCtx.Skip("no mod block uses this tile entity type");
		}

		BlockPos pos = c.at(0, 1, 0);
		c.floor();
		c.setQuiet(pos, block.defaultBlockState());
		BlockEntity original = c.world.getBlockEntity(pos);

		if (original == null)
		{
			throw new TestCtx.Failure("placing " + RegUtil.id(block) + " created no tile entity");
		}

		int populated = populate(original, BLOCK_SIDES);

		CompoundTag first = original.saveWithoutMetadata(c.world.registryAccess());

		BlockEntity copy = type.create(pos, block.defaultBlockState());
		copy.setLevel(c.world);
		copy.loadWithComponents(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, c.world.registryAccess(), first));
		CompoundTag second = copy.saveWithoutMetadata(c.world.registryAccess());

		c.eq(first, second, "NBT after write -> read -> write (populated " + populated + " capability slots)");
		c.eq(snapshot(original, BLOCK_SIDES), snapshot(copy, BLOCK_SIDES), "capability contents after reload");
	}

	private static void entity(TestCtx c, EntityType<?> type)
	{
		Entity original = type.create(c.world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);

		if (original == null)
		{
			throw new TestCtx.Skip("entity type cannot be created without special context");
		}

		original.setPos(c.origin.getX() + 0.5, c.origin.getY() + 1, c.origin.getZ() + 0.5);

		CompoundTag first = lumien.randomthings.util.TagIO.saveEntity(original);
		Entity copy = type.create(c.world, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
		copy.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, c.world.registryAccess(), first));
		CompoundTag second = lumien.randomthings.util.TagIO.saveEntity(copy);

		c.eq(first, second, "NBT after write -> read -> write");
	}

	private static void item(TestCtx c, Item item)
	{
		ItemStack stack = new ItemStack(item);

		if (stack.isDamageableItem())
		{
			stack.setDamageValue(stack.getMaxDamage() / 2);
		}

		int populated = populate(stack, STACK_SIDES);

		CompoundTag first = StackTag.saveStack(stack);
		ItemStack copy = lumien.randomthings.util.StackTag.loadStack(first);
		CompoundTag second = StackTag.saveStack(copy);

		c.check(ItemStack.matches(stack, copy), "stack differs after reload: " + stack + " vs " + copy);
		c.check(ItemStack.isSameItemSameComponents(stack, copy), "stack tag differs after reload: " + StackTag.get(stack) + " vs " + StackTag.get(copy));
		c.eq(first, second, "NBT after write -> read -> write (populated " + populated + " capability slots)");
		c.eq(snapshot(stack, STACK_SIDES), snapshot(copy, STACK_SIDES), "capability contents after reload");
	}

	// ---------------------------------------------------------------------------------------------

	private static final ItemStack[] FILLERS = { new ItemStack(Items.COBBLESTONE, 7), new ItemStack(Items.APPLE, 3), new ItemStack(Items.STICK, 5), new ItemStack(Items.IRON_INGOT, 2), new ItemStack(Items.PAPER, 4) };

	private static lumien.randomthings.itemhandler.IItemHandler itemsOf(Object provider, Direction side)
	{
		return provider instanceof net.minecraft.world.level.block.entity.BlockEntity be ? lumien.randomthings.util.CapUtil.itemHandler(be, side) : null;
	}

	private static IFluidHandler fluidsOf(Object provider, Direction side)
	{
		return provider instanceof net.minecraft.world.level.block.entity.BlockEntity be ? lumien.randomthings.util.CapUtil.fluid(be, side) : provider instanceof ItemStack stack ? lumien.randomthings.util.CapUtil.fluidItem(stack) : null;
	}

	private static net.neoforged.neoforge.energy.IEnergyStorage energyOf(Object provider, Direction side)
	{
		return provider instanceof net.minecraft.world.level.block.entity.BlockEntity be ? lumien.randomthings.util.CapUtil.energy(be, side) : provider instanceof ItemStack stack ? lumien.randomthings.util.CapUtil.energyItem(stack) : null;
	}

	/** Puts test content into every item / fluid / energy capability the provider exposes; returns how much it managed to place. */
	private static int populate(Object provider, Direction[] sides)
	{
		int placed = 0;

		for (Direction side : sides)
		{
			lumien.randomthings.itemhandler.IItemHandler items = itemsOf(provider, side);

			if (items != null)
			{
				for (int slot = 0; slot < items.getSlots(); slot++)
				{
					for (int attempt = 0; attempt < FILLERS.length; attempt++)
					{
						ItemStack filler = FILLERS[(slot + attempt) % FILLERS.length];

						if (items.insertItem(slot, filler.copy(), false).getCount() < filler.getCount())
						{
							placed++;
							break;
						}
					}
				}
			}

			IFluidHandler fluids = fluidsOf(provider, side);

			if (fluids != null && (fluids.fill(new FluidStack(Fluids.WATER, 600), IFluidHandler.FluidAction.EXECUTE) > 0 || fluids.fill(new FluidStack(Fluids.LAVA, 600), IFluidHandler.FluidAction.EXECUTE) > 0))
			{
				placed++;
			}

			net.neoforged.neoforge.energy.IEnergyStorage energy = energyOf(provider, side);

			if (energy != null && energy.canReceive() && energy.receiveEnergy(500, false) > 0)
			{
				placed++;
			}
		}

		return placed;
	}

	/** A comparable text dump of everything the provider's capabilities currently hold. */
	private static String snapshot(Object provider, Direction[] sides)
	{
		StringBuilder sb = new StringBuilder();

		for (Direction side : sides)
		{
			lumien.randomthings.itemhandler.IItemHandler items = itemsOf(provider, side);

			if (items != null)
			{
				for (int slot = 0; slot < items.getSlots(); slot++)
				{
					ItemStack stack = items.getStackInSlot(slot);
					sb.append("items[").append(side).append(',').append(slot).append("]=").append(StackTag.saveStack(stack)).append(';');
				}
			}

			IFluidHandler fluids = fluidsOf(provider, side);

			if (fluids != null)
			{
				for (int tank = 0; tank < fluids.getTanks(); tank++)
				{
					FluidStack fluid = fluids.getFluidInTank(tank);
					Identifier id = fluid.isEmpty() ? null : RegUtil.id(fluid.getFluid());
					sb.append("fluid[").append(side).append(',').append(tank).append("]=").append(id).append('x').append(fluid.getAmount()).append(';');
				}
			}

			net.neoforged.neoforge.energy.IEnergyStorage energy = energyOf(provider, side);

			if (energy != null)
			{
				sb.append("energy[").append(side).append("]=").append(energy.getEnergyStored()).append(';');
			}
		}

		return sb.toString();
	}
}
