package lumien.randomthings.container;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.BlockDestabilizerTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;

public class BlockDestabilizerContainer extends Container implements ISignalContainer {
    IWorldPosCallable pos;

    public IntReferenceHolder lazy = IntReferenceHolder.single();
    public IntReferenceHolder fuzzy = IntReferenceHolder.single();

    public BlockDestabilizerContainer(int windowId, IInventory playerInventory, PacketBuffer extraData) {
        this(windowId, IWorldPosCallable.DUMMY);
    }

    public BlockDestabilizerContainer(int windowId, IWorldPosCallable pos) {
        super(ModContainerTypes.BLOCK_DESTABILIZER, windowId);

        this.pos = pos;

        this.trackInt(lazy);
        this.trackInt(fuzzy);
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(this.pos, playerIn, ModBlocks.BLOCK_DESTABILIZER);
    }

    @Override
    public void detectAndSendChanges() {
        this.pos.consume((world, pos) -> {
            TileEntity te = world.getTileEntity(pos);

            if (te instanceof BlockDestabilizerTileEntity) {
                BlockDestabilizerTileEntity bd = (BlockDestabilizerTileEntity) te;

                this.lazy.set(bd.isLazy() ? 1 : 0);
                this.fuzzy.set(bd.isFuzzy() ? 1 : 0);
            }
        });

        super.detectAndSendChanges();
    }

    @Override
    public void handle(int id, PacketBuffer data) {
        this.pos.consume((world, pos) -> {
            TileEntity te = world.getTileEntity(pos);

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
}
