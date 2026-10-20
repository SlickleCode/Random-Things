package lumien.randomthings.debug.test;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.registries.ForgeRegistries;

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

		for (BlockEntityType<?> type : ForgeRegistries.BLOCK_ENTITY_TYPES)
		{
			if (RegUtil.id(type) != null && ModConstants.MOD_ID.equals(RegUtil.id(type).getNamespace()))
			{
				tests.add(RTTest.named("nbt.tile_entity." + RegUtil.id(type).getPath()).run(c -> tileEntity(c, type)));
			}
		}

		for (EntityType<?> type : ForgeRegistries.ENTITY_TYPES)
		{
			if (RegUtil.id(type) != null && ModConstants.MOD_ID.equals(RegUtil.id(type).getNamespace()))
			{
				tests.add(RTTest.named("nbt.entity." + RegUtil.id(type).getPath()).run(c -> entity(c, type)));
			}
		}

		for (Item item : ForgeRegistries.ITEMS)
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

		for (Block candidate : ForgeRegistries.BLOCKS)
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

		CompoundTag first = original.saveWithoutMetadata();

		BlockEntity copy = type.create(pos, block.defaultBlockState());
		copy.setLevel(c.world);
		copy.load(first);
		CompoundTag second = copy.saveWithoutMetadata();

		c.eq(first, second, "NBT after write -> read -> write (populated " + populated + " capability slots)");
		c.eq(snapshot(original, BLOCK_SIDES), snapshot(copy, BLOCK_SIDES), "capability contents after reload");
	}

	private static void entity(TestCtx c, EntityType<?> type)
	{
		Entity original = type.create(c.world);

		if (original == null)
		{
			throw new TestCtx.Skip("entity type cannot be created without special context");
		}

		original.setPos(c.origin.getX() + 0.5, c.origin.getY() + 1, c.origin.getZ() + 0.5);

		CompoundTag first = original.saveWithoutId(new CompoundTag());
		Entity copy = type.create(c.world);
		copy.load(first);
		CompoundTag second = copy.saveWithoutId(new CompoundTag());

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

		CompoundTag first = stack.save(new CompoundTag());
		ItemStack copy = ItemStack.of(first);
		CompoundTag second = copy.save(new CompoundTag());

		c.check(ItemStack.matches(stack, copy), "stack differs after reload: " + stack + " vs " + copy);
		c.check(ItemStack.isSameItemSameTags(stack, copy), "stack tag differs after reload: " + stack.getTag() + " vs " + copy.getTag());
		c.eq(first, second, "NBT after write -> read -> write (populated " + populated + " capability slots)");
		c.eq(snapshot(stack, STACK_SIDES), snapshot(copy, STACK_SIDES), "capability contents after reload");
	}

	// ---------------------------------------------------------------------------------------------

	private static final ItemStack[] FILLERS = { new ItemStack(Items.COBBLESTONE, 7), new ItemStack(Items.APPLE, 3), new ItemStack(Items.STICK, 5), new ItemStack(Items.IRON_INGOT, 2), new ItemStack(Items.PAPER, 4) };

	/** Puts test content into every item / fluid / energy capability the provider exposes; returns how much it managed to place. */
	private static int populate(ICapabilityProvider provider, Direction[] sides)
	{
		int placed = 0;

		for (Direction side : sides)
		{
			net.minecraftforge.items.IItemHandler items = provider.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, side).orElse(null);

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

			IFluidHandler fluids = provider.getCapability(ForgeCapabilities.FLUID_HANDLER, side).orElse(null);

			if (fluids == null && provider instanceof ItemStack)
			{
				fluids = provider.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, side).orElse(null);
			}

			if (fluids != null && (fluids.fill(new FluidStack(Fluids.WATER, 600), IFluidHandler.FluidAction.EXECUTE) > 0 || fluids.fill(new FluidStack(Fluids.LAVA, 600), IFluidHandler.FluidAction.EXECUTE) > 0))
			{
				placed++;
			}

			net.minecraftforge.energy.IEnergyStorage energy = provider.getCapability(ForgeCapabilities.ENERGY, side).orElse(null);

			if (energy != null && energy.canReceive() && energy.receiveEnergy(500, false) > 0)
			{
				placed++;
			}
		}

		return placed;
	}

	/** A comparable text dump of everything the provider's capabilities currently hold. */
	private static String snapshot(ICapabilityProvider provider, Direction[] sides)
	{
		StringBuilder sb = new StringBuilder();

		for (Direction side : sides)
		{
			net.minecraftforge.items.IItemHandler items = provider.getCapability(net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER, side).orElse(null);

			if (items != null)
			{
				for (int slot = 0; slot < items.getSlots(); slot++)
				{
					ItemStack stack = items.getStackInSlot(slot);
					sb.append("items[").append(side).append(',').append(slot).append("]=").append(stack.save(new CompoundTag())).append(';');
				}
			}

			IFluidHandler fluids = provider.getCapability(ForgeCapabilities.FLUID_HANDLER, side).orElse(null);

			if (fluids == null && provider instanceof ItemStack)
			{
				fluids = provider.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, side).orElse(null);
			}

			if (fluids != null)
			{
				for (int tank = 0; tank < fluids.getTanks(); tank++)
				{
					FluidStack fluid = fluids.getFluidInTank(tank);
					ResourceLocation id = fluid.isEmpty() ? null : RegUtil.id(fluid.getFluid());
					sb.append("fluid[").append(side).append(',').append(tank).append("]=").append(id).append('x').append(fluid.getAmount()).append(';');
				}
			}

			net.minecraftforge.energy.IEnergyStorage energy = provider.getCapability(ForgeCapabilities.ENERGY, side).orElse(null);

			if (energy != null)
			{
				sb.append("energy[").append(side).append("]=").append(energy.getEnergyStored()).append(';');
			}
		}

		return sb.toString();
	}
}
