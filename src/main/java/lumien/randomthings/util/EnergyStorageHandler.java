package lumien.randomthings.util;

import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Exposes an {@link IEnergyStorage} (the mod's Spectre coil pools) as the transaction based {@link EnergyHandler} NeoForge 21.9 expects. The energy is moved at
 * once: an aborted transaction does not give it back (the pool is shared state a snapshot cannot restore).
 */
public class EnergyStorageHandler implements EnergyHandler
{
	private final IEnergyStorage storage;

	public EnergyStorageHandler(IEnergyStorage storage)
	{
		this.storage = storage;
	}

	@Override
	public long getAmountAsLong()
	{
		return storage.getEnergyStored();
	}

	@Override
	public long getCapacityAsLong()
	{
		return storage.getMaxEnergyStored();
	}

	@Override
	public int insert(int amount, TransactionContext transaction)
	{
		return amount <= 0 ? 0 : storage.receiveEnergy(amount, false);
	}

	@Override
	public int extract(int amount, TransactionContext transaction)
	{
		return amount <= 0 ? 0 : storage.extractEnergy(amount, false);
	}
}
