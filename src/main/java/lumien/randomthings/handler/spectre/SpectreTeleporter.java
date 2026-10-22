package lumien.randomthings.handler.spectre;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

/**
 * Moves an entity into or out of the Spectre dimension without searching for or building a portal. 1.14.4's Forge had
 * no teleporter hook, so this was a coremod on {@code Teleporter.makePortal}; Forge 36-1.20.1 passed an
 * {@code ITeleporter} to {@code changeDimension}, and since 1.21 {@code Entity.changeDimension} takes a ready-made
 * {@link DimensionTransition}. The position is only a filler: {@link SpectreHandler} immediately overwrites it with the
 * real target via {@code connection.teleport}, as 1.12.2 did.
 */
public class SpectreTeleporter {
    public static DimensionTransition transition(Entity entity, ServerLevel destination) {
        return new DimensionTransition(destination, new Vec3(8.5D, 1.0D, 8.5D), Vec3.ZERO, entity.getYRot(), entity.getXRot(), DimensionTransition.DO_NOTHING);
    }
}
