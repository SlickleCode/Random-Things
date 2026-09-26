package lumien.randomthings.network.messages;

import lumien.randomthings.entity.EclipsedClockEntity;
import lumien.randomthings.network.IRTMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent.Context;

/**
 * Server -> client: an {@link EclipsedClockEntity} just fast-forwarded the
 * world clock - play its client-side time-skip flourish. Direct port of
 * 1.12.2's {@code MessageEclipsedClock}.
 */
public class EclipsedClockAnimationMessage implements IRTMessage {
    private int entityId;

    public EclipsedClockAnimationMessage() {
    }

    public EclipsedClockAnimationMessage(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void read(PacketBuffer pb) {
        this.entityId = pb.readInt();
    }

    @Override
    public void write(PacketBuffer pb) {
        pb.writeInt(entityId);
    }

    @Override
    public void handle(Context ctx) {
        ctx.enqueueWork(() -> {
            World world = Minecraft.getInstance().world;

            if (world == null) {
                return;
            }

            Entity entity = world.getEntityByID(entityId);

            if (entity instanceof EclipsedClockEntity) {
                ((EclipsedClockEntity) entity).triggerAnimation();
            }
        });
    }
}
