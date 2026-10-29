package lumien.randomthings.tileentity.redstoneinterface;

import lumien.randomthings.container.AdvancedRedstoneInterfaceContainer;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.PositionFilterItem;
import lumien.randomthings.tileentity.ModTileEntityTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import lumien.randomthings.itemhandler.ItemStackHandler;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Multi-target half of the Redstone Interface family: a 9-slot Position
 * Filter inventory (matching {@link lumien.randomthings.item.PortkeyItem}/
 * {@code GoldenCompassItem}'s own use of {@code PositionFilterItem}) instead
 * of a single tool-bound target - this interface pushes its own sensed
 * weak/strong power onto every filter slot's stored position at once. Direct
 * port of 1.12.2's {@code TileEntityAdvancedRedstoneInterface}.
 */
public class AdvancedRedstoneInterfaceTileEntity extends RedstoneInterfaceTileEntity implements MenuProvider {
    private final ItemStackHandler positionInventory = new ItemStackHandler(9) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() == ModItems.POSITION_FILTER;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            refreshTargets();
        }
    };

    private Set<BlockPos> targets = new HashSet<>();

    public AdvancedRedstoneInterfaceTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.ADVANCED_REDSTONE_INTERFACE, pos, state);
    }

    public ItemStackHandler getPositionInventory() {
        return positionInventory;
    }

    public Set<BlockPos> getTargets() {
        return targets;
    }

    private void refreshTargets() {
        if (this.level == null) {
            return;
        }

        Set<BlockPos> newTargets = new HashSet<>();

        for (int i = 0; i < positionInventory.getSlots(); i++) {
            ItemStack stack = positionInventory.getStackInSlot(i);
            BlockPos target = stack.isEmpty() ? null : PositionFilterItem.getPosition(stack);

            // Position Filters only work within their own dimension (wiki: "does not work across dimensions")
            if (target != null && !PositionFilterItem.getDimension(stack).equals(this.level.dimension().identifier().toString())) {
                target = null;
            }

            if (target != null) {
                newTargets.add(target);
            }
        }

        if (this.level.isClientSide()) {
            this.targets = newTargets;
            return;
        }

        Set<BlockPos> changedPositions = new HashSet<>();

        for (BlockPos target : newTargets) {
            if (!targets.contains(target)) {
                changedPositions.add(target);
            }
        }

        for (BlockPos oldTarget : targets) {
            if (!newTargets.contains(oldTarget)) {
                changedPositions.add(oldTarget);
            }
        }

        this.targets = newTargets;

        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);

        for (BlockPos changedPos : changedPositions) {
            notifyOne(changedPos, Blocks.REDSTONE_BLOCK);
        }
    }

    private void notifyOne(BlockPos targetPos, Block neighborBlock) {
        this.level.getBlockState(targetPos).handleNeighborChanged(this.level, targetPos, neighborBlock, null, false);
        this.level.updateNeighborsAt(targetPos, neighborBlock);
    }

    @Override
    protected boolean isTargeting(BlockPos pos) {
        return targets.contains(pos);
    }

    @Override
    protected void notifyTargets(Block neighborBlock) {
        for (BlockPos target : targets) {
            notifyOne(target, neighborBlock);
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

        compound.put("inventory", positionInventory.serializeNBT(registries));

    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
    {
    	super.loadAdditional(rtIn);
    	rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
    }

    private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {

        positionInventory.deserializeNBT(registries, compound.getCompoundOrEmpty("inventory"));
        refreshTargets();
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
        return new AdvancedRedstoneInterfaceContainer(windowId, playerInventory, positionInventory, ContainerLevelAccess.create(this.level, worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.randomthings.advanced_redstone_interface");
    }

	@Override
	public void preRemoveSideEffects(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state)
	{
		super.preRemoveSideEffects(pos, state);

		// 1.21.5+: Block.onRemove is gone and the tile entity is already removed when the neighbour callback runs, so the block's old onRemove code runs from here
		if (this.level != null && state.getBlock() instanceof lumien.randomthings.block.redstoneinterface.AdvancedRedstoneInterfaceBlock block)
		{
			block.rtOnRemove(state, this.level, pos, this.level.getBlockState(pos), false);
		}
	}
}
