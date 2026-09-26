package lumien.randomthings.network.messages;

import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.SoundRecorderItem;
import lumien.randomthings.network.IRTMessage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

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
    public void read(PacketBuffer pb) {
        this.soundName = pb.readString();
        this.recorderSlot = pb.readInt();
    }

    @Override
    public void write(PacketBuffer pb) {
        pb.writeString(soundName);
        pb.writeInt(recorderSlot);
    }

    @Override
    public void handle(Context ctx) {
        ctx.enqueueWork(() -> {
            ServerPlayerEntity player = ctx.getSender();

            if (player == null || recorderSlot < 0 || recorderSlot >= player.inventory.getSizeInventory()) {
                return;
            }

            ItemStack recorderStack = player.inventory.getStackInSlot(recorderSlot);

            if (!recorderStack.isEmpty() && recorderStack.getItem() == ModItems.SOUND_RECORDER) {
                SoundRecorderItem.recordSound(recorderStack, soundName);
            }
        });
    }
}
