package lumien.randomthings.handler.spectre;

import java.util.function.Function;

import net.minecraft.block.PortalInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.ITeleporter;

/**
 * Moves an entity into or out of the Spectre dimension without searching for or building a portal. 1.14.4's Forge had
 * no teleporter hook, so this was a coremod on {@code Teleporter.makePortal} (the old {@code TeleporterTransformer});
 * Forge 36 passes an {@link ITeleporter} to {@code changeDimension}, which makes that coremod unnecessary. The
 * position returned here is only a filler: {@link SpectreHandler} immediately overwrites it with the real target via
 * {@code connection.setPlayerLocation}, as 1.12.2 did.
 */
public class SpectreTeleporter implements ITeleporter {
    public static final SpectreTeleporter INSTANCE = new SpectreTeleporter();

    @Override
    public PortalInfo getPortalInfo(Entity entity, ServerWorld destWorld, Function<ServerWorld, PortalInfo> defaultPortalInfo) {
        return new PortalInfo(new Vector3d(8.5D, 1.0D, 8.5D), Vector3d.ZERO, entity.rotationYaw, entity.rotationPitch);
    }

    @Override
    public boolean isVanilla() {
        return false;
    }
}
