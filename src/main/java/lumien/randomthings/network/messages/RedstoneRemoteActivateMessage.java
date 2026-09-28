package lumien.randomthings.network.messages;

import lumien.randomthings.container.RedstoneRemoteEditContainer;
import lumien.randomthings.handler.redstonesignal.RedstoneSignalHandler;
import lumien.randomthings.item.PositionFilterItem;
import lumien.randomthings.network.IRTMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent.Context;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Client -> server: "the player clicked the Nth Position Filter button on
 * their held Redstone Remote's Use screen" - broadcasts a strength-15
 * signal at that slot's stored position, same as {@link
 * lumien.randomthings.item.RedstoneActivatorItem}. Direct port of 1.12.2's
 * {@code MessageRedstoneRemote}, minus its "guess which hand" fallback -
 * {@link RedstoneRemoteUseScreen} already knows which hand opened it, so
 * this port re-resolves the same way {@link
 * RedstoneRemoteEditContainer#findRemoteStack} does (main-then-offhand),
 * which lands on the same stack either way.
 */
public class RedstoneRemoteActivateMessage implements IRTMessage {
    private int slot;

    public RedstoneRemoteActivateMessage() {
    }

    public RedstoneRemoteActivateMessage(int slot) {
        this.slot = slot;
    }

    @Override
    public void read(PacketBuffer pb) {
        this.slot = pb.readInt();
    }

    @Override
    public void write(PacketBuffer pb) {
        pb.writeInt(slot);
    }

    @Override
    public void handle(Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();

            if (player == null || slot < 0 || slot >= 9) {
                return;
            }

            ItemStack remoteStack = RedstoneRemoteEditContainer.findRemoteStack(player);

            if (remoteStack.isEmpty() || !remoteStack.hasTag() || !remoteStack.getTag().contains("positions")) {
                return;
            }

            ItemStackHandler positionInventory = new ItemStackHandler(9);
            positionInventory.deserializeNBT(remoteStack.getTag().getCompound("positions"));

            ItemStack positionFilter = positionInventory.getStackInSlot(slot);

            if (positionFilter.isEmpty()) {
                return;
            }

            BlockPos target = PositionFilterItem.getPosition(positionFilter);

            if (target != null) {
                RedstoneSignalHandler.get(player.world).addSignal(player.world, target, 20, 15);
            }
        });
    }
}
