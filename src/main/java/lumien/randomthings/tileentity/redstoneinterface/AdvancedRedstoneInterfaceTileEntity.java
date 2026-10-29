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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.items.ItemStackHandler;

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
            if (target != null && !PositionFilterItem.getDimension(stack).equals(this.level.dimension().location().toString())) {
                target = null;
            }

            if (target != null) {
                newTargets.add(target);
            }
        }

        if (this.level.isClientSide) {
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
        this.level.getBlockState(targetPos).neighborChanged(this.level, targetPos, neighborBlock, this.worldPosition, false);
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
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        compound.put("inventory", positionInventory.serializeNBT());


    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        positionInventory.deserializeNBT(compound.getCompound("inventory"));
        refreshTargets();
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player playerEntity) {
        return new AdvancedRedstoneInterfaceContainer(windowId, playerInventory, positionInventory, ContainerLevelAccess.create(this.level, worldPosition));
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("block.randomthings.advanced_redstone_interface");
    }
}
