package lumien.randomthings.network.messages;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.SoundRecorderItem;
import lumien.randomthings.network.IRTMessage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Client -> server: "I just heard this sound and slot N of my inventory
 * holds a recording {@link SoundRecorderItem}" - sent from
 * {@code RandomThings}'s client-side {@code PlaySoundEvent} listener for
 * every sound slot found while scanning the player's whole inventory (not
 * just the held item), matching 1.12.2's {@code MessagePlayedSound}.
 */
public class PlayedSoundMessage implements IRTMessage {
    private String soundName;
    private int recorderSlot;

    public PlayedSoundMessage() {
    }

    public PlayedSoundMessage(String soundName, int recorderSlot) {
        this.soundName = soundName;
        this.recorderSlot = recorderSlot;
    }

    @Override
    public void read(FriendlyByteBuf pb) {
        this.soundName = pb.readUtf(32767);
        this.recorderSlot = pb.readInt();
    }

    @Override
    public void write(FriendlyByteBuf pb) {
        pb.writeUtf(soundName);
        pb.writeInt(recorderSlot);
    }

    @Override
    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayer player = (ServerPlayer) ctx.player();

            if (player == null || recorderSlot < 0 || recorderSlot >= player.getInventory().getContainerSize()) {
                return;
            }

            ItemStack recorderStack = player.getInventory().getItem(recorderSlot);

            if (!recorderStack.isEmpty() && recorderStack.getItem() == ModItems.SOUND_RECORDER) {
                SoundRecorderItem.recordSound(recorderStack, soundName);
            }
        });
    }
}
