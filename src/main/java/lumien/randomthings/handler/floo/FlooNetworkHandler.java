package lumien.randomthings.handler.floo;

import lumien.randomthings.tileentity.FlooBrickTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldSavedData;

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
public class FlooNetworkHandler extends WorldSavedData {
    private static final String ID = "randomthings_floo_handler";

    private final List<FlooFireplace> firePlaces = new ArrayList<>();

    public FlooNetworkHandler() {
        super(ID);
    }

    public static FlooNetworkHandler get(World world) {
        return ((ServerWorld) world).getSavedData().getOrCreate(FlooNetworkHandler::new, ID);
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
        this.markDirty();
    }

    public boolean teleport(World world, BlockPos originPos, FlooBrickTileEntity originTE, ServerPlayerEntity player, String enteredDestination) {
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
            player.sendMessage(new TranslationTextComponent("floo.info.same"));
            return false;
        }

        BlockPos targetPos = targetFirePlace.getLastKnownPosition();
        TileEntity te = world.getTileEntity(targetPos);

        if (te instanceof FlooBrickTileEntity && ((FlooBrickTileEntity) te).isMaster()) {
            FlooBrickTileEntity targetTE =  (FlooBrickTileEntity) te;
            Direction tpFacing = targetTE.getFacing();

            BlockPos teleportTarget = targetPos.up();
            player.connection.setPlayerLocation(teleportTarget.getX() + 0.5, teleportTarget.getY(), teleportTarget.getZ() + 0.5, tpFacing.getHorizontalAngle(), 0);

            player.sendMessage(new TranslationTextComponent("floo.info.teleport", targetFirePlace.getName()));

            if (originPos != null && originTE != null) {
                spawnFlameBurst((ServerWorld) player.world, originPos, originTE.getChildren());
            }

            spawnFlameBurst((ServerWorld) player.world, targetPos, targetTE.getChildren());

            return true;
        }

        firePlaces.remove(targetFirePlace);
        this.markDirty();

        return teleport(world, originPos, originTE, player, enteredDestination);
    }

    private static void spawnFlameBurst(ServerWorld world, BlockPos center, List<BlockPos> children) {
        spawnFlameBurst(world, center);

        for (BlockPos child : children) {
            spawnFlameBurst(world, child);
        }
    }

    private static void spawnFlameBurst(ServerWorld world, BlockPos pos) {
        world.spawnParticle(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 8, 0.3, 0.3, 0.3, 0.02);
    }

    @Override
    public void read(CompoundNBT nbt) {
        ListNBT firePlaceTags = nbt.getList("firePlaces", 10);

        for (int i = 0; i < firePlaceTags.size(); i++) {
            FlooFireplace firePlace = new FlooFireplace();
            firePlace.read(firePlaceTags.getCompound(i));
            this.firePlaces.add(firePlace);
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        ListNBT firePlaceTags = new ListNBT();

        for (FlooFireplace firePlace : firePlaces) {
            CompoundNBT firePlaceCompound = new CompoundNBT();
            firePlace.write(firePlaceCompound);
            firePlaceTags.add(firePlaceCompound);
        }

        compound.put("firePlaces", firePlaceTags);

        return compound;
    }

    public boolean createFireplace(UUID uuid, String name, PlayerEntity player, BlockPos pos, List<BlockPos> brickList) {
        for (FlooFireplace firePlace : firePlaces) {
            if (brickList.contains(firePlace.getLastKnownPosition()) || (name != null && name.equalsIgnoreCase(firePlace.getName()))) {
                return false;
            }
        }

        addFirePlace(player.getUniqueID(), uuid, name, pos);

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

        this.markDirty();
    }

    public TileEntity getFirePlaceTE(World world, UUID uuid) {
        for (FlooFireplace firePlace : firePlaces) {
            if (firePlace.getMasterUUID().equals(uuid)) {
                return world.getTileEntity(firePlace.getLastKnownPosition());
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
