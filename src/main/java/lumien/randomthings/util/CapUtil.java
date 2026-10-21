package lumien.randomthings.util;

import javax.annotation.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

/** Block-entity shortcuts for the 1.20.6 capability lookup (the capability now lives on the level, keyed by position; the result is null when absent). */
public final class CapUtil
{
	private CapUtil()
	{
	}

	@Nullable
	public static IItemHandler itemHandler(@Nullable BlockEntity be, @Nullable Direction side)
	{
		return be == null || be.getLevel() == null ? null : be.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, be.getBlockPos(), side);
	}

	@Nullable
	public static IEnergyStorage energy(@Nullable BlockEntity be, @Nullable Direction side)
	{
		return be == null || be.getLevel() == null ? null : be.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK, be.getBlockPos(), side);
	}

	@Nullable
	public static IFluidHandler fluid(@Nullable BlockEntity be, @Nullable Direction side)
	{
		return be == null || be.getLevel() == null ? null : be.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, be.getBlockPos(), side);
	}
}
