package lumien.randomthings.handler.spectre;

import lumien.randomthings.handler.ModDimensions;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

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
public class SpectreHandler extends WorldSavedData {
    private static final String ID = "randomthings_spectre_handler";

    private final Map<UUID, SpectreCube> cubes = new HashMap<>();
    private int positionCounter;

    public SpectreHandler() {
        super(ID);
    }

    public static SpectreHandler getInstance(MinecraftServer server) {
        ServerWorld world = ModDimensions.getSpectreWorld(server);

        if (world == null) {
            return null;
        }

        return world.getSavedData().getOrCreate(SpectreHandler::new, ID);
    }

    public ServerWorld getWorld() {
        return ModDimensions.getSpectreWorld(ServerLifecycleHooks.getCurrentServer());
    }

    /**
     * Finds the cube whose footprint contains {@code pos}. A cube with offset {@code position} (0, 16, 32, ...) is generated
     * at block X {@code position * 16} and is 16 blocks wide (see {@link SpectreCube#generate}), so the match is on that
     * X range. An earlier version of this port compared {@code floor(x / 16) * 16} with {@code position}, which only ever
     * matched the first cube: every later cube was "not found" and {@link #checkPosition} pinned its owner to the spawn
     * tile (found by the behavior test {@code behavior.spectre.*}).
     */
    public SpectreCube getSpectreCubeFromPos(World world, BlockPos pos) {
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

    public void teleportPlayerToSpectreCube(ServerPlayerEntity player) {
        CompoundNBT persistentData = player.getPersistentData();
        persistentData.putDouble("spectrePosX", player.getPosX());
        persistentData.putDouble("spectrePosY", player.getPosY());
        persistentData.putDouble("spectrePosZ", player.getPosZ());
        persistentData.putString("spectreDimension", player.world.getDimensionKey().getLocation().toString());

        UUID uuid = player.getGameProfile().getId();
        SpectreCube spectreCube = cubes.get(uuid);

        if (spectreCube == null) {
            spectreCube = generateSpectreCube(uuid);
        }

        BlockPos spawn = spectreCube.getSpawnBlock();

        ServerWorld spectreWorld = getWorld();

        if (!ModDimensions.isSpectre(player.world) && spectreWorld != null) {
            player.changeDimension(spectreWorld, SpectreTeleporter.INSTANCE);
        }

        player.connection.setPlayerLocation(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5, player.rotationYaw, player.rotationPitch);
    }

    public void teleportPlayerBack(ServerPlayerEntity player) {
        CompoundNBT persistentData = player.getPersistentData();

        if (!persistentData.contains("spectrePosX")) {
            return;
        }

        double spectrePosX = persistentData.getDouble("spectrePosX");
        double spectrePosY = persistentData.getDouble("spectrePosY");
        double spectrePosZ = persistentData.getDouble("spectrePosZ");
        MinecraftServer server = player.getServer();
        ServerWorld destination = null;

        if (persistentData.contains("spectreDimension", 8)) {
            destination = server.getWorld(RegistryKey.getOrCreateKey(Registry.WORLD_KEY, new ResourceLocation(persistentData.getString("spectreDimension"))));
        }

        if (destination == null) {
            destination = server.getWorld(World.OVERWORLD);
        }

        if (player.world != destination) {
            player.changeDimension(destination, SpectreTeleporter.INSTANCE);
        }

        player.connection.setPlayerLocation(spectrePosX, spectrePosY, spectrePosZ, player.rotationYaw, player.rotationPitch);

        while (!player.world.hasNoCollisions(player, player.getBoundingBox()) && player.getPosY() < 256.0D) {
            player.setPosition(player.getPosX(), player.getPosY() + 1.0D, player.getPosZ());
        }

        player.connection.captureCurrentPosition();
    }

    public void checkPosition(ServerPlayerEntity player) {
        SpectreCube cube = getSpectreCubeFromPos(player.world, player.getPosition());

        if (player.abilities.isCreativeMode || (cube != null && cube.getOwner().equals(player.getGameProfile().getId()))) {
            return;
        }

        SpectreCube playerCube = cubes.get(player.getGameProfile().getId());

        if (playerCube != null) {
            BlockPos spawn = playerCube.getSpawnBlock();
            player.connection.setPlayerLocation(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5, player.rotationYaw, player.rotationPitch);
        } else {
            teleportPlayerBack(player);
        }
    }

    private SpectreCube generateSpectreCube(UUID uuid) {
        SpectreCube cube = new SpectreCube(uuid, positionCounter);
        positionCounter += 16;

        cube.generate(getWorld());
        cubes.put(uuid, cube);
        this.markDirty();
        return cube;
    }

    @Override
    public void read(CompoundNBT nbt) {
        ListNBT cubeTags = nbt.getList("cubes", 10);

        for (int i = 0; i < cubeTags.size(); i++) {
            SpectreCube cube = SpectreCube.readFrom(cubeTags.getCompound(i));
            this.cubes.put(cube.getOwner(), cube);
        }

        this.positionCounter = nbt.getInt("positionCounter");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT cubeTags = new ListNBT();

        for (SpectreCube cube : cubes.values()) {
            CompoundNBT cubeCompound = new CompoundNBT();
            cube.writeTo(cubeCompound);
            cubeTags.add(cubeCompound);
        }

        compound.put("cubes", cubeTags);
        compound.putInt("positionCounter", positionCounter);

        return compound;
    }
}
