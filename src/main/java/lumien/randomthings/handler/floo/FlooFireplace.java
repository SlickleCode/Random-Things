package lumien.randomthings.handler.floo;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;

import java.util.UUID;

/**
 * One registered Floo fireplace: its master brick's UUID (stable even if the
 * bricks move/reload), a player-chosen destination name, and its last-known
 * position (kept in sync by {@link lumien.randomthings.tileentity.FlooBrickTileEntity#onLoad()}
 * so a chunk reload doesn't strand the network's record of where it is).
 * Direct port of 1.12.2's {@code FlooFireplace}.
 */
public class FlooFireplace {
    private UUID creatorPlayerUUID;
    private UUID masterUUID;
    private String name;
    private BlockPos lastKnownPosition;

    public FlooFireplace() {
    }

    public FlooFireplace(UUID creatorPlayerUUID, UUID masterUUID, String name, BlockPos position) {
        this.creatorPlayerUUID = creatorPlayerUUID;
        this.masterUUID = masterUUID;
        this.name = name;
        this.lastKnownPosition = position;
    }

    public void write(CompoundNBT compound) {
        if (creatorPlayerUUID != null) {
            compound.put("creatorPlayerUUID", NBTUtil.writeUniqueId(creatorPlayerUUID));
        }

        compound.put("masterUUID", NBTUtil.writeUniqueId(masterUUID));

        if (name != null) {
            compound.putString("name", name);
        }

        compound.put("position", NBTUtil.writeBlockPos(lastKnownPosition));
    }

    public void read(CompoundNBT compound) {
        if (compound.contains("creatorPlayerUUID")) {
            this.creatorPlayerUUID = NBTUtil.readUniqueId(compound.getCompound("creatorPlayerUUID"));
        }

        this.masterUUID = NBTUtil.readUniqueId(compound.getCompound("masterUUID"));

        if (compound.contains("name")) {
            this.name = compound.getString("name");
        }

        this.lastKnownPosition = NBTUtil.readBlockPos(compound.getCompound("position"));
    }

    public String getName() {
        return name;
    }

    public BlockPos getLastKnownPosition() {
        return lastKnownPosition;
    }

    public UUID getMasterUUID() {
        return masterUUID;
    }

    public void setPos(BlockPos pos) {
        this.lastKnownPosition = pos;
    }

    public UUID getCreatorUUID() {
        return creatorPlayerUUID;
    }
}
