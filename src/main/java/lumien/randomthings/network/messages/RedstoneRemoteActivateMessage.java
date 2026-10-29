package lumien.randomthings.network.messages;

import lumien.randomthings.util.StackTag;
import lumien.randomthings.container.RedstoneRemoteEditContainer;
import lumien.randomthings.handler.redstonesignal.RedstoneSignalHandler;
import lumien.randomthings.item.PositionFilterItem;
import lumien.randomthings.network.IRTMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.items.ItemStackHandler;

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
    public void read(FriendlyByteBuf pb) {
        this.slot = pb.readInt();
    }

    @Override
    public void write(FriendlyByteBuf pb) {
        pb.writeInt(slot);
    }

    @Override
    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();

            if (player == null || slot < 0 || slot >= 9) {
                return;
            }

            ItemStack remoteStack = RedstoneRemoteEditContainer.findRemoteStack(player);

            if (remoteStack.isEmpty() || !StackTag.has(remoteStack) || !StackTag.contains(remoteStack, "positions")) {
                return;
            }

            ItemStackHandler positionInventory = new ItemStackHandler(9);
            positionInventory.deserializeNBT(StackTag.provider(), StackTag.get(remoteStack).getCompound("positions"));

            ItemStack positionFilter = positionInventory.getStackInSlot(slot);

            if (positionFilter.isEmpty()) {
                return;
            }

            BlockPos target = PositionFilterItem.getPosition(positionFilter);

            if (target != null && player.level().dimension().location().toString().equals(PositionFilterItem.getDimension(positionFilter))) {
                RedstoneSignalHandler.get(player.level()).addSignal(player.level(), target, 20, 15);
            }
        });
    }
}
