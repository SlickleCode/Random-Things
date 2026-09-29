package lumien.randomthings.handler.spectrecoils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Ported from 1.12.2's {@code SpectreCoilHandler} - a per-player energy pool (keyed by player UUID,
 * matching the original's 1,000,000 cap) that the Spectre Energy Injector, Spectre Coil, and Spectre
 * Charger item all tap into. Restructured onto 1.14.4's {@code WorldSavedData}/
 * {@code DimensionSavedDataManager} the same way {@code SpectreHandler}/{@code FlooNetworkHandler}
 * already were ({@code world.getMapStorage()} became {@code ((ServerWorld) world).getSavedData()}).
 *
 * <p>Kept the original's exact per-dimension scoping ({@code getMapStorage()}, not the cross-dimension
 * {@code getPerWorldStorage()} that {@code SpectreLensHandler} deliberately used) rather than "fixing"
 * it into a single global pool - a player's coil energy is scoped to whichever dimension they're
 * pulling it in, matching 1.12.2 exactly. Flagging this only because it's a real behavioral choice, not
 * because it looked like a bug.
 */
public class SpectreCoilHandler extends WorldSavedData {
    private static final String ID = "randomthings_spectre_coil_handler";

    private static final int MAX_ENERGY = 1000000;

    private final Map<UUID, Integer> coilEntries = new HashMap<>();

    public SpectreCoilHandler() {
        super(ID);
    }

    public static SpectreCoilHandler get(World world) {
        return ((ServerWorld) world).getSavedData().getOrCreate(SpectreCoilHandler::new, ID);
    }

    /** Receive-only - used by the Spectre Energy Injector block (external machines push energy in). */
    public IEnergyStorage getStorage(UUID owner) {
        return new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                int currentEnergy = coilEntries.getOrDefault(owner, 0);
                int newEnergy = Math.min(MAX_ENERGY, currentEnergy + maxReceive);

                if (!simulate && newEnergy != currentEnergy) {
                    coilEntries.put(owner, newEnergy);
                    markDirty();
                }

                return newEnergy - currentEnergy;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                return 0;
            }

            @Override
            public int getEnergyStored() {
                return coilEntries.getOrDefault(owner, 0);
            }

            @Override
            public int getMaxEnergyStored() {
                return MAX_ENERGY;
            }

            @Override
            public boolean canExtract() {
                return false;
            }

            @Override
            public boolean canReceive() {
                return true;
            }
        };
    }

    /** Receive + extract - used by the Spectre Coil block and the Spectre Charger item. */
    public IEnergyStorage getStorageCoil(UUID owner) {
        return new IEnergyStorage() {
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                int currentEnergy = coilEntries.getOrDefault(owner, 0);
                int newEnergy = Math.min(MAX_ENERGY, currentEnergy + maxReceive);

                if (!simulate && newEnergy != currentEnergy) {
                    coilEntries.put(owner, newEnergy);
                    markDirty();
                }

                return newEnergy - currentEnergy;
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                int currentEnergy = coilEntries.getOrDefault(owner, 0);
                int newEnergy = Math.max(0, currentEnergy - maxExtract);

                if (!simulate && newEnergy != currentEnergy) {
                    coilEntries.put(owner, newEnergy);
                    markDirty();
                }

                return currentEnergy - newEnergy;
            }

            @Override
            public int getEnergyStored() {
                return coilEntries.getOrDefault(owner, 0);
            }

            @Override
            public int getMaxEnergyStored() {
                return MAX_ENERGY;
            }

            @Override
            public boolean canExtract() {
                return true;
            }

            @Override
            public boolean canReceive() {
                return true;
            }
        };
    }

    @Override
    public void read(CompoundNBT nbt) {
        coilEntries.clear();

        ListNBT list = nbt.getList("coilEntries", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundNBT entry = list.getCompound(i);
            coilEntries.put(entry.getUniqueId("uuid"), entry.getInt("energy"));
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT list = new ListNBT();

        for (Map.Entry<UUID, Integer> entry : coilEntries.entrySet()) {
            CompoundNBT entryCompound = new CompoundNBT();
            entryCompound.putUniqueId("uuid", entry.getKey());
            entryCompound.putInt("energy", entry.getValue());
            list.add(entryCompound);
        }

        compound.put("coilEntries", list);
        return compound;
    }
}
