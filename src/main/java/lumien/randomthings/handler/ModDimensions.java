package lumien.randomthings.handler;

import io.netty.buffer.Unpooled;
import lumien.randomthings.handler.spectre.SpectreDimension;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.ModDimension;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.RegisterDimensionsEvent;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Direct port of 1.12.2's {@code ModDimensions}, restructured onto 1.14.4's
 * two-step dimension registration: {@code ModDimension} is itself a Forge
 * registry entry (registered during the normal, once-per-process registry-
 * event phase, same as every other registry in this mod), but the actual
 * {@link DimensionType} - the thing with a real numeric ID - has to be
 * (re-)created per world load, on {@link RegisterDimensionsEvent}, not once
 * at mod-loading time.
 * <p>
 * Real bug found and fixed (reported by user: a
 * {@code NullPointerException: Dimension type must not be null} crash on
 * joining a world that had previously visited the Spectre dimension).
 * Ground-truthed from {@code DimensionManager}'s own source: every world
 * load calls {@code readRegistry}, which unconditionally clears the *entire*
 * dimension-type registry back down to vanilla-only and then repopulates it
 * strictly from whatever *that specific world's own save data* has recorded
 * - nothing else. {@code RegisterDimensionsEvent} fires immediately after,
 * specifically so mods can (re-)register whatever their own dimension(s)
 * that particular world doesn't already have saved. Calling {@code
 * DimensionManager.registerDimension} once from {@code FMLCommonSetupEvent}
 * (mod-loading time, long before any world/save is even chosen) got wiped
 * out the moment any world actually loaded, for any world that hadn't
 * already saved a {@code randomthings:spectre} registry entry - leaving
 * {@link #SPECTRE_TYPE} a dangling reference no longer present in the live
 * registry, so any later {@code DimensionType.getById} lookup for it (e.g.
 * resolving a player's own persisted "last dimension" NBT field, itself
 * stored as a raw int ID - confirmed via {@code Entity#read}) returned null.
 * {@link #registerDimension} now runs on {@code RegisterDimensionsEvent}
 * instead, using {@code registerOrGetDimension} (idempotent - returns the
 * existing entry if this world already has one) rather than the plain
 * {@code registerDimension} used before, matching the API's own documented
 * intent for this hook.
 */
public class ModDimensions {
    public static ModDimension SPECTRE_MOD_DIMENSION;
    public static DimensionType SPECTRE_TYPE;

    public static void registerModDimensions(RegistryEvent.Register<ModDimension> event) {
        IForgeRegistry<ModDimension> registry = event.getRegistry();

        SPECTRE_MOD_DIMENSION = ModDimension.withFactory(SpectreDimension::new).setRegistryName("spectre");
        registry.register(SPECTRE_MOD_DIMENSION);
    }

    public static void registerDimension(RegisterDimensionsEvent event) {
        SPECTRE_TYPE = DimensionManager.registerOrGetDimension(new ResourceLocation("randomthings", "spectre"), SPECTRE_MOD_DIMENSION, new PacketBuffer(Unpooled.buffer()), false);
    }
}
