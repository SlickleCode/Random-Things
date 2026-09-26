package lumien.randomthings.tileentity;

import lumien.randomthings.block.EnderMailboxBlock;
import lumien.randomthings.container.EnderMailboxContainer;
import lumien.randomthings.handler.EnderLetterHandler;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.UUID;

/**
 * Direct port of 1.12.2's {@code TileEntityEnderMailbox}: just remembers
 * which player placed it (the actual mail is stored in {@link
 * EnderLetterHandler}, keyed by that UUID, not here) and every 10 seconds
 * checks whether that player currently has unread mail, toggling the
 * block's {@code ACTIVE} state (glow + portal particles) to match.
 */
public class EnderMailboxTileEntity extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    private UUID owner;
    private int tickCounter;

    public EnderMailboxTileEntity() {
        super(ModTileEntityTypes.ENDER_MAILBOX);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public UUID getOwner() {
        return owner;
    }

    @Override
    public void tick() {
        if (this.world.isRemote || this.owner == null) {
            return;
        }

        tickCounter++;

        if (tickCounter >= 20 * 10) {
            tickCounter = 0;

            BlockState state = this.world.getBlockState(this.pos);
            boolean active = state.get(EnderMailboxBlock.ACTIVE);

            EnderLetterHandler handler = EnderLetterHandler.get(this.world);
            boolean shouldBeActive = handler.hasInventoryFor(this.owner) && !handler.isInventoryEmpty(this.owner);

            if (shouldBeActive != active) {
                this.world.setBlockState(this.pos, state.with(EnderMailboxBlock.ACTIVE, shouldBeActive));
            }
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        if (this.owner != null) {
            compound.putString("owner", owner.toString());
        }

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        if (compound.contains("owner")) {
            this.owner = UUID.fromString(compound.getString("owner"));
        }
    }

    @Override
    public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
        return new EnderMailboxContainer(windowId, playerInventory, this.owner);
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("block.randomthings.ender_mailbox");
    }
}
