package lumien.randomthings.handler.spectre;

import net.minecraft.core.registries.Registries;

import lumien.randomthings.handler.ModDimensions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;

/**
 * Direct port of 1.12.2's {@code SpectreHandler}, restructured onto 1.14.4's
 * {@code WorldSavedData}/{@code DimensionSavedDataManager} the same way
 * {@code FlooNetworkHandler} already was ({@code world.getPerWorldStorage()}
 * became {@code ((ServerWorld) world).getSavedData()}). {@code getInstance}
 * takes an explicit {@link MinecraftServer} - the original relied on
 * {@code FMLCommonHandler.instance().getMinecraftServerInstance()}'s global
 * static access, which doesn't exist in this Forge version; every call site
 * already has a player or world handy to get one from.
 */
public class SpectreHandler extends SavedData {
    private static final String ID = "randomthings_spectre_handler";

    private final Map<UUID, SpectreCube> cubes = new HashMap<>();
    private int positionCounter;

    public SpectreHandler() {
    }

    public static SpectreHandler loadFrom(CompoundTag nbt, net.minecraft.core.HolderLookup.Provider registries) {
        SpectreHandler data = new SpectreHandler();
        data.readData(nbt);
        return data;
    }

    public static SpectreHandler getInstance(MinecraftServer server) {
        ServerLevel world = ModDimensions.getSpectreWorld(server);

        if (world == null) {
            return null;
        }

        return world.getDataStorage().computeIfAbsent(new net.minecraft.world.level.saveddata.SavedData.Factory<>(SpectreHandler::new, SpectreHandler::loadFrom, null), ID);
    }

    public ServerLevel getWorld() {
        return ModDimensions.getSpectreWorld(ServerLifecycleHooks.getCurrentServer());
    }

    /**
     * Finds the cube whose footprint contains {@code pos}. A cube with offset {@code position} (0, 16, 32, ...) is generated
     * at block X {@code position * 16} and is 16 blocks wide (see {@link SpectreCube#generate}), so the match is on that
     * X range. An earlier version of this port compared {@code floor(x / 16) * 16} with {@code position}, which only ever
     * matched the first cube: every later cube was "not found" and {@link #checkPosition} pinned its owner to the spawn
     * tile (found by the behavior test {@code behavior.spectre.*}).
     */
    public SpectreCube getSpectreCubeFromPos(Level world, BlockPos pos) {
        if (!ModDimensions.isSpectre(world) || pos.getZ() > 16 || pos.getZ() < 0) {
            return null;
        }

        for (SpectreCube cube : cubes.values()) {
            int minX = cube.getPosition() * 16;

            if (pos.getX() >= minX && pos.getX() <= minX + 15) {
                if (pos.getY() <= 0 || pos.getY() > cube.getHeight() + 1) {
                    return null;
                }

                return cube;
            }
        }

        return null;
    }

    public void teleportPlayerToSpectreCube(ServerPlayer player) {
        CompoundTag persistentData = player.getPersistentData();
        persistentData.putDouble("spectrePosX", player.getX());
        persistentData.putDouble("spectrePosY", player.getY());
        persistentData.putDouble("spectrePosZ", player.getZ());
        persistentData.putString("spectreDimension", player.level().dimension().location().toString());

        UUID uuid = player.getGameProfile().getId();
        SpectreCube spectreCube = cubes.get(uuid);

        if (spectreCube == null) {
            spectreCube = generateSpectreCube(uuid);
        }

        BlockPos spawn = spectreCube.getSpawnBlock();

        ServerLevel spectreWorld = getWorld();

        if (!ModDimensions.isSpectre(player.level()) && spectreWorld != null) {
            player.teleport(SpectreTeleporter.transition(player, spectreWorld));
        }

        player.connection.teleport(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
    }

    public void teleportPlayerBack(ServerPlayer player) {
        CompoundTag persistentData = player.getPersistentData();

        if (!persistentData.contains("spectrePosX")) {
            return;
        }

        double spectrePosX = persistentData.getDouble("spectrePosX");
        double spectrePosY = persistentData.getDouble("spectrePosY");
        double spectrePosZ = persistentData.getDouble("spectrePosZ");
        MinecraftServer server = player.getServer();
        ServerLevel destination = null;

        if (persistentData.contains("spectreDimension", 8)) {
            destination = server.getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(persistentData.getString("spectreDimension"))));
        }

        if (destination == null) {
            destination = server.getLevel(Level.OVERWORLD);
        }

        if (player.level() != destination) {
            player.teleport(SpectreTeleporter.transition(player, destination));
        }

        player.connection.teleport(spectrePosX, spectrePosY, spectrePosZ, player.getYRot(), player.getXRot());

        while (!player.level().noCollision(player, player.getBoundingBox()) && player.getY() < 256.0D) {
            player.setPos(player.getX(), player.getY() + 1.0D, player.getZ());
        }

        player.connection.resetPosition();
    }

    public void checkPosition(ServerPlayer player) {
        SpectreCube cube = getSpectreCubeFromPos(player.level(), player.blockPosition());

        if (player.getAbilities().instabuild || (cube != null && cube.getOwner().equals(player.getGameProfile().getId()))) {
            return;
        }

        SpectreCube playerCube = cubes.get(player.getGameProfile().getId());

        if (playerCube != null) {
            BlockPos spawn = playerCube.getSpawnBlock();
            player.connection.teleport(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5, player.getYRot(), player.getXRot());
        } else {
            teleportPlayerBack(player);
        }
    }

    private SpectreCube generateSpectreCube(UUID uuid) {
        SpectreCube cube = new SpectreCube(uuid, positionCounter);
        positionCounter += 16;

        cube.generate(getWorld());
        cubes.put(uuid, cube);
        this.setDirty();
        return cube;
    }

    public void readData(CompoundTag nbt) {
        ListTag cubeTags = nbt.getList("cubes", 10);

        for (int i = 0; i < cubeTags.size(); i++) {
            SpectreCube cube = SpectreCube.readFrom(cubeTags.getCompound(i));
            this.cubes.put(cube.getOwner(), cube);
        }

        this.positionCounter = nbt.getInt("positionCounter");
    }

    @Override
    public CompoundTag save(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        ListTag cubeTags = new ListTag();

        for (SpectreCube cube : cubes.values()) {
            CompoundTag cubeCompound = new CompoundTag();
            cube.writeTo(cubeCompound);
            cubeTags.add(cubeCompound);
        }

        compound.put("cubes", cubeTags);
        compound.putInt("positionCounter", positionCounter);

        return compound;
    }
}
