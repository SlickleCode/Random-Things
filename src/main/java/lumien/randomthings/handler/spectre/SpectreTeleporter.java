package lumien.randomthings.handler.spectre;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

/**
 * Moves an entity into or out of the Spectre dimension without searching for or building a portal. 1.14.4's Forge had
 * no teleporter hook, so this was a coremod on {@code Teleporter.makePortal}; Forge 36-1.20.1 passed an
 * {@code ITeleporter} to {@code changeDimension}, and since 1.21 {@code Entity.changeDimension} (1.21.2: {@code Entity.teleport}) takes a ready-made
 * {@link TeleportTransition}. The position is only a filler: {@link SpectreHandler} immediately overwrites it with the
 * real target via {@code connection.teleport}, as 1.12.2 did.
 */
public class SpectreTeleporter {
    public static TeleportTransition transition(Entity entity, ServerLevel destination) {
        return new TeleportTransition(destination, new Vec3(8.5D, 1.0D, 8.5D), Vec3.ZERO, entity.getYRot(), entity.getXRot(), TeleportTransition.DO_NOTHING);
    }
}
