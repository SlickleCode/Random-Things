package lumien.randomthings.handler.spectre;

import lumien.randomthings.handler.ModDimensions;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
        ServerWorld world = DimensionManager.getWorld(server, ModDimensions.SPECTRE_TYPE, true, true);

        if (world == null) {
            return null;
        }

        return world.getSavedData().getOrCreate(SpectreHandler::new, ID);
    }

    public World getWorld() {
        return DimensionManager.getWorld(ServerLifecycleHooks.getCurrentServer(), ModDimensions.SPECTRE_TYPE, true, true);
    }

    /**
     * Real bug found in 1.12.2's own source while porting this method
     * (ground-truthed, not a "looks wrong but is intentional" case): the
     * original derived its cube-matching key from {@code Chunk.x / 16}
     * (chunk coordinate, i.e. {@code blockX >> 4}, divided by 16 again) and
     * compared it against {@code cube.position / 16} (block-coordinate cube
     * offsets - 0, 16, 32, ...). Tracing actual numbers: cube 0 happens to
     * "work" only because its bounds check afterward narrows a much-too-wide
     * initial match back down to X 0-15, but every cube after it (position
     * 16, 32, ...) never matches at all - {@code (16 >> 4) / 16 == 0} while
     * {@code cube.position / 16 == 1}), so {@code checkPosition} could never
     * recognize a player standing anywhere in their own second-or-later cube,
     * constantly snapping them back to their spawn tile. This port uses the
     * evidently-intended block-coordinate comparison directly instead.
     */
    public SpectreCube getSpectreCubeFromPos(World world, BlockPos pos) {
        if (world.getDimension().getType() != ModDimensions.SPECTRE_TYPE || pos.getZ() > 16 || pos.getZ() < 0) {
            return null;
        }

        int position = Math.floorDiv(pos.getX(), 16) * 16;

        for (SpectreCube cube : cubes.values()) {
            if (cube.getPosition() == position) {
                if (pos.getY() <= 0 || pos.getY() > cube.getHeight() + 1 || pos.getX() < position || pos.getX() > position + 15) {
                    return null;
                }

                return cube;
            }
        }

        return null;
    }

    public void teleportPlayerToSpectreCube(ServerPlayerEntity player) {
        CompoundNBT persistentData = player.getPersistentData();
        persistentData.putDouble("spectrePosX", player.posX);
        persistentData.putDouble("spectrePosY", player.posY);
        persistentData.putDouble("spectrePosZ", player.posZ);
        persistentData.putInt("spectreDimension", player.dimension.getId());

        UUID uuid = player.getGameProfile().getId();
        SpectreCube spectreCube = cubes.get(uuid);

        if (spectreCube == null) {
            spectreCube = generateSpectreCube(uuid);
        }

        BlockPos spawn = spectreCube.getSpawnBlock();

        if (player.dimension != ModDimensions.SPECTRE_TYPE) {
            player.changeDimension(ModDimensions.SPECTRE_TYPE);
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
        DimensionType spectreDimension = DimensionType.getById(persistentData.getInt("spectreDimension"));

        if (player.dimension != spectreDimension) {
            player.changeDimension(spectreDimension);
        }

        player.connection.setPlayerLocation(spectrePosX, spectrePosY, spectrePosZ, player.rotationYaw, player.rotationPitch);

        while (!player.world.isCollisionBoxesEmpty(player, player.getBoundingBox()) && player.posY < 256.0D) {
            player.setPosition(player.posX, player.posY + 1.0D, player.posZ);
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
