package lumien.randomthings.tileentity;

import lumien.randomthings.handler.floo.FlooNetworkHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Direct port of 1.12.2's {@code TileEntityFlooBrick}. One "master" brick per
 * fireplace holds the real data (its stable {@link FlooNetworkHandler} UUID,
 * the facing a teleporting player should end up looking, and every other
 * brick in the group); every other brick in the group just points back at
 * the master's UUID. The actual "convert back to plain bricks on break"
 * cleanup lives on {@link lumien.randomthings.block.FlooBrickBlock#onReplaced}
 * (which still has a live tile entity to read from at that point), matching
 * this port's {@code RuneBaseBlock} precedent for the same kind of
 * TE-data-read-during-break problem.
 */
public class FlooBrickTileEntity extends TileEntity {
    private boolean amMaster;

    // Master-only fields
    private UUID uuid;
    private Direction facing = Direction.WEST;
    private List<BlockPos> children = new ArrayList<>();

    // Child-only field
    private UUID masterUUID;

    public FlooBrickTileEntity() {
        super(ModTileEntityTypes.FLOO_BRICK);
    }

    @Override
    public void onLoad() {
        if (!this.world.isRemote && uuid != null) {
            FlooNetworkHandler.get(this.world).updatePosition(uuid, pos);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        compound.putBoolean("amMaster", amMaster);

        if (amMaster) {
            compound.put("uuid", NBTUtil.writeUniqueId(uuid));
            compound.putInt("facing", facing.ordinal());

            ListNBT childrenTagList = new ListNBT();

            for (BlockPos childPos : children) {
                childrenTagList.add(NBTUtil.writeBlockPos(childPos));
            }

            compound.put("children", childrenTagList);
        } else if (masterUUID != null) {
            compound.put("masterUUID", NBTUtil.writeUniqueId(masterUUID));
        }

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        this.amMaster = compound.getBoolean("amMaster");

        if (amMaster) {
            this.uuid = NBTUtil.readUniqueId(compound.getCompound("uuid"));
            this.facing = Direction.byIndex(compound.getInt("facing"));

            children.clear();
            ListNBT childrenTagList = compound.getList("children", 10);

            for (int i = 0; i < childrenTagList.size(); i++) {
                children.add(NBTUtil.readBlockPos(childrenTagList.getCompound(i)));
            }
        } else if (compound.contains("masterUUID")) {
            this.masterUUID = NBTUtil.readUniqueId(compound.getCompound("masterUUID"));
        }
    }

    public UUID getFirePlaceUid() {
        return amMaster ? this.uuid : masterUUID;
    }

    public Direction getFacing() {
        return facing;
    }

    public boolean isMaster() {
        return amMaster;
    }

    public UUID getUid() {
        return uuid;
    }

    public void initToChild(UUID masterUUID) {
        this.amMaster = false;
        this.masterUUID = masterUUID;
    }

    public void initToMaster(List<BlockPos> brickList, Direction teleportFacing, UUID uuid) {
        this.uuid = uuid;
        this.amMaster = true;
        this.children = brickList;
        this.facing = teleportFacing;
    }

    public List<BlockPos> getChildren() {
        return children;
    }
}
