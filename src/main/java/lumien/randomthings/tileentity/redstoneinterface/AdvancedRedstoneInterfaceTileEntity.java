package lumien.randomthings.tileentity.redstoneinterface;

import lumien.randomthings.container.AdvancedRedstoneInterfaceContainer;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.PositionFilterItem;
import lumien.randomthings.tileentity.ModTileEntityTypes;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.items.ItemStackHandler;

import java.util.HashSet;
import java.util.Set;

/**
 * Multi-target half of the Redstone Interface family: a 9-slot Position
 * Filter inventory (matching {@link lumien.randomthings.item.PortkeyItem}/
 * {@code GoldenCompassItem}'s own use of {@code PositionFilterItem}) instead
 * of a single tool-bound target - this interface pushes its own sensed
 * weak/strong power onto every filter slot's stored position at once. Direct
 * port of 1.12.2's {@code TileEntityAdvancedRedstoneInterface}.
 */
public class AdvancedRedstoneInterfaceTileEntity extends RedstoneInterfaceTileEntity implements INamedContainerProvider {
    private final ItemStackHandler positionInventory = new ItemStackHandler(9) {
        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.getItem() == ModItems.POSITION_FILTER;
        }

        @Override
        protected void onContentsChanged(int slot) {
            markDirty();
            refreshTargets();
        }
    };

    private Set<BlockPos> targets = new HashSet<>();

    public AdvancedRedstoneInterfaceTileEntity() {
        super(ModTileEntityTypes.ADVANCED_REDSTONE_INTERFACE);
    }

    public ItemStackHandler getPositionInventory() {
        return positionInventory;
    }

    public Set<BlockPos> getTargets() {
        return targets;
    }

    private void refreshTargets() {
        if (this.world == null) {
            return;
        }

        Set<BlockPos> newTargets = new HashSet<>();

        for (int i = 0; i < positionInventory.getSlots(); i++) {
            ItemStack stack = positionInventory.getStackInSlot(i);
            BlockPos target = stack.isEmpty() ? null : PositionFilterItem.getPosition(stack);

            if (target != null) {
                newTargets.add(target);
            }
        }

        if (this.world.isRemote) {
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

        this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 3);

        for (BlockPos changedPos : changedPositions) {
            notifyOne(changedPos, Blocks.REDSTONE_BLOCK);
        }
    }

    private void notifyOne(BlockPos targetPos, Block neighborBlock) {
        this.world.getBlockState(targetPos).neighborChanged(this.world, targetPos, neighborBlock, this.pos, false);
        this.world.notifyNeighborsOfStateChange(targetPos, neighborBlock);
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
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        compound.put("inventory", positionInventory.serializeNBT());

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        positionInventory.deserializeNBT(compound.getCompound("inventory"));
        refreshTargets();
    }

    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new AdvancedRedstoneInterfaceContainer(windowId, playerInventory, positionInventory, IWorldPosCallable.of(this.world, pos));
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.randomthings.advanced_redstone_interface");
    }
}
