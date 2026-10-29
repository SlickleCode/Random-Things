package lumien.randomthings.handler.floo;

import lumien.randomthings.tileentity.FlooBrickTileEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Direct port of 1.12.2's {@code FlooNetworkHandler}, restructured onto
 * 1.14.4's {@code WorldSavedData}/{@code DimensionSavedDataManager} (same
 * concept, renamed API - {@code world.getPerWorldStorage()} became
 * {@code ((ServerWorld) world).getSavedData()}). Two disclosed
 * simplifications, both matching precedent already established elsewhere in
 * this port:
 * <ul>
 * <li>The original used the third-party {@code info.debatty:java-string-similarity}
 * library (Levenshtein distance) to fuzzy-match a typed destination name
 * against registered fireplaces. Rather than add this port's first-ever
 * external dependency for one small, well-known algorithm, this is a plain
 * in-house Levenshtein implementation instead - same matching behavior, no
 * new dependency.</li>
 * <li>The original broadcast a dedicated network message
 * ({@code MessageFlooParticles}) to nearby players for the departure/arrival
 * particle burst. Matching {@code PotionVaporizerTileEntity}'s precedent,
 * {@code ServerWorld.spawnParticle} already broadcasts to nearby players on
 * its own, so no custom packet is needed - a vanilla {@code ParticleTypes.FLAME}
 * burst stands in for the original's bespoke Floo-flame particle.</li>
 * </ul>
 */
public class FlooNetworkHandler extends SavedData {
    private static final String ID = "randomthings_floo_handler";

    private final List<FlooFireplace> firePlaces = new ArrayList<>();

    public FlooNetworkHandler() {
    }

    public static FlooNetworkHandler loadFrom(CompoundTag nbt) {
        FlooNetworkHandler data = new FlooNetworkHandler();
        data.readData(nbt);
        return data;
    }

    public static FlooNetworkHandler get(Level world) {
        return ((ServerLevel) world).getDataStorage().computeIfAbsent(FlooNetworkHandler::loadFrom, FlooNetworkHandler::new, ID);
    }

    public String getNameFromUUID(UUID uuid) {
        for (FlooFireplace ff : firePlaces) {
            if (ff.getMasterUUID().equals(uuid)) {
                return ff.getName();
            }
        }

        return null;
    }

    public void addFirePlace(UUID creatorPlayerUUID, UUID masterUUID, String name, BlockPos currentPos) {
        firePlaces.add(new FlooFireplace(creatorPlayerUUID, masterUUID, name, currentPos));
        this.setDirty();
    }

    public boolean teleport(Level world, BlockPos originPos, FlooBrickTileEntity originTE, ServerPlayer player, String enteredDestination) {
        FlooFireplace targetFirePlace = null;
        int closest = Integer.MAX_VALUE;

        for (FlooFireplace firePlace : firePlaces) {
            String firePlaceName = firePlace.getName();

            if (firePlaceName != null) {
                int distance = levenshteinDistance(firePlaceName, enteredDestination);

                if (distance < closest) {
                    closest = distance;
                    targetFirePlace = firePlace;
                }
            }
        }

        if (targetFirePlace == null) {
            return false;
        }

        if (targetFirePlace.getLastKnownPosition().equals(originPos)) {
            player.sendMessage(new TranslatableComponent("floo.info.same"), net.minecraft.Util.NIL_UUID);
            return false;
        }

        BlockPos targetPos = targetFirePlace.getLastKnownPosition();

        if (!world.hasChunkAt(targetPos)) {
            // Unloaded chunk: we can't tell whether the fireplace still exists, so neither teleport nor touch the registry
            return false;
        }

        BlockEntity te = world.getBlockEntity(targetPos);

        if (te instanceof FlooBrickTileEntity && ((FlooBrickTileEntity) te).isMaster()) {
            FlooBrickTileEntity targetTE =  (FlooBrickTileEntity) te;
            Direction tpFacing = targetTE.getFacing();

            BlockPos teleportTarget = targetPos.above();
            player.connection.teleport(teleportTarget.getX() + 0.5, teleportTarget.getY(), teleportTarget.getZ() + 0.5, tpFacing.toYRot(), 0);

            player.sendMessage(new TranslatableComponent("floo.info.teleport", targetFirePlace.getName()), net.minecraft.Util.NIL_UUID);

            if (originPos != null && originTE != null) {
                spawnFlameBurst((ServerLevel) player.level, originPos, originTE.getChildren());
            }

            spawnFlameBurst((ServerLevel) player.level, targetPos, targetTE.getChildren());

            return true;
        }

        firePlaces.remove(targetFirePlace);
        this.setDirty();

        return teleport(world, originPos, originTE, player, enteredDestination);
    }

    private static void spawnFlameBurst(ServerLevel world, BlockPos center, List<BlockPos> children) {
        spawnFlameBurst(world, center);

        for (BlockPos child : children) {
            spawnFlameBurst(world, child);
        }
    }

    private static void spawnFlameBurst(ServerLevel world, BlockPos pos) {
        // Raised from +0.6 (just above the brick) to +1.2 (roughly waist height on a standing
        // player) per user request - more visible at a glance than a burst hugging the floor.
        world.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.02);
    }

    public void readData(CompoundTag nbt) {
        ListTag firePlaceTags = nbt.getList("firePlaces", 10);

        for (int i = 0; i < firePlaceTags.size(); i++) {
            FlooFireplace firePlace = new FlooFireplace();
            firePlace.read(firePlaceTags.getCompound(i));
            this.firePlaces.add(firePlace);
        }
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag firePlaceTags = new ListTag();

        for (FlooFireplace firePlace : firePlaces) {
            CompoundTag firePlaceCompound = new CompoundTag();
            firePlace.write(firePlaceCompound);
            firePlaceTags.add(firePlaceCompound);
        }

        compound.put("firePlaces", firePlaceTags);

        return compound;
    }

    public boolean hasFireplaceNamed(String name) {
        if (name == null) {
            return false;
        }

        for (FlooFireplace firePlace : firePlaces) {
            if (name.equalsIgnoreCase(firePlace.getName())) {
                return true;
            }
        }

        return false;
    }

    public boolean createFireplace(UUID uuid, String name, Player player, BlockPos pos, List<BlockPos> brickList) {
        for (FlooFireplace firePlace : firePlaces) {
            if (brickList.contains(firePlace.getLastKnownPosition()) || (name != null && name.equalsIgnoreCase(firePlace.getName()))) {
                return false;
            }
        }

        addFirePlace(player.getUUID(), uuid, name, pos);

        return true;
    }

    public void updatePosition(UUID uuid, BlockPos pos) {
        for (FlooFireplace firePlace : firePlaces) {
            if (firePlace.getMasterUUID().equals(uuid)) {
                firePlace.setPos(pos);
                break;
            }
        }
    }

    public void brokenMaster(UUID masterUuid) {
        if (masterUuid == null) {
            return;
        }

        Iterator<FlooFireplace> iterator = firePlaces.iterator();

        while (iterator.hasNext()) {
            if (iterator.next().getMasterUUID().equals(masterUuid)) {
                iterator.remove();
            }
        }

        this.setDirty();
    }

    public BlockEntity getFirePlaceTE(Level world, UUID uuid) {
        for (FlooFireplace firePlace : firePlaces) {
            if (firePlace.getMasterUUID().equals(uuid)) {
                return world.getBlockEntity(firePlace.getLastKnownPosition());
            }
        }

        return null;
    }

    /**
     * Plain iterative Levenshtein (edit) distance - see the class javadoc for
     * why this is in-house rather than a library dependency.
     */
    private static int levenshteinDistance(String a, String b) {
        int[] previous = new int[b.length() + 1];
        int[] current = new int[b.length() + 1];

        for (int j = 0; j <= b.length(); j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= a.length(); i++) {
            current[0] = i;

            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), previous[j - 1] + cost);
            }

            int[] swap = previous;
            previous = current;
            current = swap;
        }

        return previous[b.length()];
    }
}
