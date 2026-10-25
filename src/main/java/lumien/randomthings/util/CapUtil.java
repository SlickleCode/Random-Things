package lumien.randomthings.util;

import javax.annotation.Nullable;

import lumien.randomthings.itemhandler.IItemHandler;
import lumien.randomthings.itemhandler.ResourceHandlerItemAdapter;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.FluidResourceHandlerItemAdapter;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Capability lookups. NeoForge 21.9 replaced the item/fluid/energy handler capabilities by transaction based ones ({@code ResourceHandler}, {@code EnergyHandler});
 * these shortcuts hand the old slot style interfaces back (the mod's own {@code ItemStackHandler} is itself a {@code ResourceHandler}, anything else is wrapped).
 */
public final class CapUtil
{
	private CapUtil()
	{
	}

	@Nullable
	public static IItemHandler itemHandler(@Nullable BlockEntity be, @Nullable Direction side)
	{
		if (be == null || be.getLevel() == null)
		{
			return null;
		}

		ResourceHandler<ItemResource> handler = be.getLevel().getCapability(Capabilities.Item.BLOCK, be.getBlockPos(), side);
		return handler == null ? null : handler instanceof IItemHandler slotHandler ? slotHandler : new ResourceHandlerItemAdapter(handler);
	}

	@Nullable
	public static IEnergyStorage energy(@Nullable BlockEntity be, @Nullable Direction side)
	{
		if (be == null || be.getLevel() == null)
		{
			return null;
		}

		EnergyHandler handler = be.getLevel().getCapability(Capabilities.Energy.BLOCK, be.getBlockPos(), side);
		return handler == null ? null : IEnergyStorage.of(handler);
	}

	@Nullable
	public static IFluidHandler fluid(@Nullable BlockEntity be, @Nullable Direction side)
	{
		if (be == null || be.getLevel() == null)
		{
			return null;
		}

		ResourceHandler<FluidResource> handler = be.getLevel().getCapability(Capabilities.Fluid.BLOCK, be.getBlockPos(), side);
		return handler == null ? null : IFluidHandler.of(handler);
	}

	/** The fluid handler of an item stack (the changes are made to {@code stack} itself), or null. */
	@Nullable
	public static IFluidHandlerItem fluidItem(@Nullable ItemStack stack)
	{
		if (stack == null || stack.isEmpty())
		{
			return null;
		}

		ItemAccess access = ItemAccess.forStack(stack);
		ResourceHandler<FluidResource> handler = access.getCapability(Capabilities.Fluid.ITEM);
		return handler == null ? null : new FluidResourceHandlerItemAdapter(handler, access);
	}

	/** The energy storage of an item stack (the changes are made to {@code stack} itself), or null. */
	@Nullable
	public static IEnergyStorage energyItem(@Nullable ItemStack stack)
	{
		if (stack == null || stack.isEmpty())
		{
			return null;
		}

		EnergyHandler handler = ItemAccess.forStack(stack).getCapability(Capabilities.Energy.ITEM);
		return handler == null ? null : IEnergyStorage.of(handler);
	}
}
