package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.BlockDestabilizerTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;

public class BlockDestabilizerContainer extends AbstractContainerMenu implements ISignalContainer {
    ContainerLevelAccess pos;

    public DataSlot lazy = DataSlot.standalone();
    public DataSlot fuzzy = DataSlot.standalone();

    public BlockDestabilizerContainer(int windowId, Container playerInventory, FriendlyByteBuf extraData) {
        this(windowId, ContainerLevelAccess.NULL);
    }

    public BlockDestabilizerContainer(int windowId, ContainerLevelAccess pos) {
        super(ModContainerTypes.BLOCK_DESTABILIZER, windowId);

        this.pos = pos;

        this.addDataSlot(lazy);
        this.addDataSlot(fuzzy);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return stillValid(this.pos, playerIn, ModBlocks.BLOCK_DESTABILIZER);
    }

    @Override
    public void broadcastChanges() {
        this.pos.execute((world, pos) -> {
            BlockEntity te = world.getBlockEntity(pos);

            if (te instanceof BlockDestabilizerTileEntity) {
                BlockDestabilizerTileEntity bd = (BlockDestabilizerTileEntity) te;

                this.lazy.set(bd.isLazy() ? 1 : 0);
                this.fuzzy.set(bd.isFuzzy() ? 1 : 0);
            }
        });

        super.broadcastChanges();
    }

    @Override
    public void handle(int id, FriendlyByteBuf data) {
        this.pos.execute((world, pos) -> {
            BlockEntity te = world.getBlockEntity(pos);

            if (!(te instanceof BlockDestabilizerTileEntity)) {
                return;
            }

            BlockDestabilizerTileEntity bd = (BlockDestabilizerTileEntity) te;

            if (id == 0) {
                bd.toggleLazy();
            } else if (id == 1) {
                bd.toggleFuzzy();
            } else if (id == 2) {
                bd.resetLazy();
            }
        });
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(net.minecraft.world.entity.player.Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
