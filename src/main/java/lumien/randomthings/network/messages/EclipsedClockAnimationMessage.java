package lumien.randomthings.network.messages;

import lumien.randomthings.entity.EclipsedClockEntity;
import lumien.randomthings.network.IRTMessage;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent.Context;

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
    public void read(FriendlyByteBuf pb) {
        this.entityId = pb.readInt();
    }

    @Override
    public void write(FriendlyByteBuf pb) {
        pb.writeInt(entityId);
    }

    @Override
    public void handle(Context ctx) {
        ctx.enqueueWork(() -> {
            Level world = lumien.randomthings.client.ClientHooks.level();

            if (world == null) {
                return;
            }

            Entity entity = world.getEntity(entityId);

            if (entity instanceof EclipsedClockEntity) {
                ((EclipsedClockEntity) entity).triggerAnimation();
            }
        });
    }
}
