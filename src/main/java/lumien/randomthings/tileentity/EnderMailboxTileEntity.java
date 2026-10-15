package lumien.randomthings.tileentity;

import net.minecraft.core.BlockPos;

import lumien.randomthings.block.EnderMailboxBlock;
import lumien.randomthings.container.EnderMailboxContainer;
import lumien.randomthings.handler.EnderLetterHandler;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.MenuProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import java.util.UUID;

/**
 * Direct port of 1.12.2's {@code TileEntityEnderMailbox}: just remembers
 * which player placed it (the actual mail is stored in {@link
 * EnderLetterHandler}, keyed by that UUID, not here) and every 10 seconds
 * checks whether that player currently has unread mail, toggling the
 * block's {@code ACTIVE} state (glow + portal particles) to match.
 */
public class EnderMailboxTileEntity extends BlockEntity implements MenuProvider {
    private UUID owner;
    private int tickCounter;

    public EnderMailboxTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.ENDER_MAILBOX, pos, state);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    public void tick() {
        if (this.level.isClientSide || this.owner == null) {
            return;
        }

        tickCounter++;

        if (tickCounter >= 20 * 10) {
            tickCounter = 0;

            BlockState state = this.level.getBlockState(this.worldPosition);
            boolean active = state.getValue(EnderMailboxBlock.ACTIVE);

            EnderLetterHandler handler = EnderLetterHandler.get(this.level);
            boolean shouldBeActive = handler.hasInventoryFor(this.owner) && !handler.isInventoryEmpty(this.owner);

            if (shouldBeActive != active) {
                this.level.setBlockAndUpdate(this.worldPosition, state.setValue(EnderMailboxBlock.ACTIVE, shouldBeActive));
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        if (this.owner != null) {
            compound.putString("owner", owner.toString());
        }

    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        if (compound.contains("owner")) {
            this.owner = UUID.fromString(compound.getString("owner"));
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return new EnderMailboxContainer(windowId, playerInventory, this.owner);
    }

    @Override
    public Component getDisplayName() {
        return new TranslatableComponent("block.randomthings.ender_mailbox");
    }
}
