package lumien.randomthings.tileentity;

import lumien.randomthings.handler.floo.FlooNetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.world.level.block.state.BlockState;

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
public class FlooBrickTileEntity extends BlockEntity {
    private boolean amMaster;

    // Master-only fields
    private UUID uuid;
    private Direction facing = Direction.WEST;
    private List<BlockPos> children = new ArrayList<>();

    // Child-only field
    private UUID masterUUID;

    public FlooBrickTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.FLOO_BRICK, pos, state);
    }

    @Override
    public void onLoad() {
        if (!this.level.isClientSide() && uuid != null) {
            FlooNetworkHandler.get(this.level).updatePosition(uuid, worldPosition);
        }
    }

    @Override
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput rtOut)
    {
    	super.saveAdditional(rtOut);
    	CompoundTag rtTag = new CompoundTag();
    	rtSaveAdditional(rtTag, lumien.randomthings.util.StackTag.provider());
    	lumien.randomthings.util.TagIO.write(rtOut, rtTag);
    }

    private void rtSaveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {

        compound.putBoolean("amMaster", amMaster);

        if (amMaster) {
            compound.put("uuid", lumien.randomthings.util.TagIO.writeUuid(uuid));
            compound.putInt("facing", facing.ordinal());

            ListTag childrenTagList = new ListTag();

            for (BlockPos childPos : children) {
                childrenTagList.add(lumien.randomthings.util.BlockPosNbt.write(childPos));
            }

            compound.put("children", childrenTagList);
        } else if (masterUUID != null) {
            compound.put("masterUUID", lumien.randomthings.util.TagIO.writeUuid(masterUUID));
        }

    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
    {
    	super.loadAdditional(rtIn);
    	rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
    }

    private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {

        this.amMaster = compound.getBooleanOr("amMaster", false);

        if (amMaster) {
            this.uuid = lumien.randomthings.util.TagIO.readUuid(compound.get("uuid"));
            this.facing = Direction.from3DDataValue(compound.getIntOr("facing", 0));

            children.clear();
            ListTag childrenTagList = compound.getListOrEmpty("children");

            for (int i = 0; i < childrenTagList.size(); i++) {
                children.add(lumien.randomthings.util.BlockPosNbt.read(childrenTagList.getCompoundOrEmpty(i)));
            }
        } else if (compound.contains("masterUUID")) {
            this.masterUUID = lumien.randomthings.util.TagIO.readUuid(compound.get("masterUUID"));
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

	@Override
	public void preRemoveSideEffects(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state)
	{
		super.preRemoveSideEffects(pos, state);

		// 1.21.5+: Block.onRemove is gone and the tile entity is already removed when the neighbour callback runs, so the block's old onRemove code runs from here
		if (this.level != null && state.getBlock() instanceof lumien.randomthings.block.FlooBrickBlock block)
		{
			block.rtOnRemove(state, this.level, pos, this.level.getBlockState(pos), false);
		}
	}
}
