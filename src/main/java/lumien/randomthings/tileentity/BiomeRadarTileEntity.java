package lumien.randomthings.tileentity;

import lumien.randomthings.util.StackTag;
import net.minecraft.core.registries.Registries;

import lumien.randomthings.item.BiomeCrystalItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.PositionFilterItem;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.network.messages.BiomeRadarAntennaMessage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Searches outward from its own position (an Archimedean-spiral column walk,
 * 48 blocks per ring) for the biome bound to its inserted
 * {@link BiomeCrystalItem}, when redstone-powered and a small iron-bars
 * "antenna" is built above it. Direct port of 1.12.2's
 * {@code TileEntityBiomeRadar}.
 * <p>
 * **Disclosed simplification:** the original's antenna glow was a bespoke
 * client-side smoke-puff {@code Particle} subclass
 * ({@code EntityColoredSmokeFX}) reusing vanilla's smoke sprite frames -
 * nothing like that (or any particle-registration infra at all) exists yet
 * in this port. Swapped for vanilla's own colored dust particle
 * ({@link RedstoneParticleData}, the same one redstone wire and sniffer eggs
 * use) instead of building a whole new sprite-based particle class just for
 * this - same "colored puff above the antenna" feedback, different particle
 * shape. The state machine, spiral search, and network sync are otherwise a
 * faithful port.
 */
public class BiomeRadarTileEntity extends BlockEntity {
    private ItemStack currentCrystal = ItemStack.EMPTY;

    private boolean powered;
    private STATE state = STATE.IDLE;

    private Biome biomeToSearch;
    private int searchCounter = 0;

    private int antennaCounter = 0;
    private String[] antennaBiomes = new String[4];

    private int testCounter = 0;

    public enum STATE {
        IDLE, SEARCHING, FINISHED
    }

    public BiomeRadarTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.BIOME_RADAR, pos, state);
    }

    public void tick() {
        if (!this.level.isClientSide) {
            testCounter++;

            // Neighbor-change notifications only reach directly-adjacent
            // blocks, but the iron-bars antenna sits 2-3 blocks above - break
            // one of those bars and this TE would never hear about it any
            // other way. Poll instead, matching the original.
            if (testCounter % 60 == 0 && this.state != STATE.IDLE && !isValid()) {
                this.state = STATE.IDLE;
                syncTE();
            }

            if (this.state == STATE.SEARCHING) {
                boolean changedColor = false;

                for (int i = 0; i < 5; i++) {
                    BlockPos testPos = getPos(searchCounter);
                    Biome testBiome = this.level.getBiome(testPos).value();
                    ResourceLocation registryName = biomeName(testBiome);

                    if (registryName == null) {
                        searchCounter++;
                        continue;
                    }

                    boolean exists = false;

                    for (int s = 0; s < 4; s++) {
                        if (registryName.toString().equals(antennaBiomes[s])) {
                            exists = true;
                        }
                    }

                    if (!exists) {
                        antennaBiomes[antennaCounter] = registryName.toString();

                        changedColor = true;

                        if (antennaCounter >= 3) {
                            antennaCounter = 0;
                        } else {
                            antennaCounter++;
                        }
                    }

                    if (registryName.equals(biomeName(biomeToSearch))) {
                        this.state = STATE.FINISHED;

                        this.syncTE();
                        break;
                    }

                    searchCounter++;
                }

                if (searchCounter % 100 == 0 && changedColor) {
                    RTPacketHandler.sendToAllNear(new BiomeRadarAntennaMessage(this.worldPosition, antennaBiomes), this.level, this.worldPosition, 64);
                }
            }
        } else {
            spawnParticles();
        }
    }

    private void spawnParticles() {
        if (this.state == STATE.SEARCHING) {
            for (int i = 0; i < 4; i++) {
                if (antennaBiomes[i] != null) {
                    Biome biome = lumien.randomthings.util.BiomeUtil.byId(new ResourceLocation(antennaBiomes[i]));

                    if (biome != null) {
                        spawnAntennaParticle(i, biome);
                    }
                }
            }
        } else if (this.state == STATE.FINISHED) {
            Biome biome = BiomeCrystalItem.getBiome(currentCrystal);

            if (biome != null) {
                for (int i = 0; i < 4; i++) {
                    spawnAntennaParticle(i, biome);
                }

                int rgb = lumien.randomthings.util.BiomeColorUtil.getBiomeColor(null, biome, this.worldPosition);
                this.level.addParticle(new DustParticleOptions(new org.joml.Vector3f(((rgb >> 16 & 255) / 255F), ((rgb >> 8 & 255) / 255F), ((rgb & 255) / 255F)), 1.0F), this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 3.1, this.worldPosition.getZ() + 0.5, 0, 0, 0);
            }
        }
    }

    private void spawnAntennaParticle(int i, Biome biome) {
        int rgb = lumien.randomthings.util.BiomeColorUtil.getBiomeColor(null, biome, this.worldPosition);

        double x = i < 2 ? this.worldPosition.getX() + 1.5 - i * 2 : this.worldPosition.getX() + 0.5;
        double z = i > 1 ? this.worldPosition.getZ() + 1.5 - (i - 2) * 2 : this.worldPosition.getZ() + 0.5;

        this.level.addParticle(new DustParticleOptions(new org.joml.Vector3f(((rgb >> 16 & 255) / 255F), ((rgb >> 8 & 255) / 255F), ((rgb & 255) / 255F)), 1.0F), x, this.worldPosition.getY() + 4.1, z, 0, 0, 0);
    }

    /**
     * 1.16: the biomes a world actually uses are decoded from its data packs, so they are NOT the instances in {@code ForgeRegistries.BIOMES}
     * (that lookup returns null for them, and {@code ==} against the crystal's biome is never true). Fall back to the world's own biome registry.
     */
    private ResourceLocation biomeName(Biome biome) {
        if (biome == null) {
            return null;
        }

        ResourceLocation key = lumien.randomthings.util.BiomeUtil.id(biome);

        if (key == null && this.level instanceof net.minecraft.server.level.ServerLevel) {
            key = ((net.minecraft.server.level.ServerLevel) this.level).registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME).getKey(biome);
        }

        return key;
    }

    private BlockPos getPos(int n) {
        double x = 0;
        double z = 0;

        // given n an index in the squared spiral, p the sum of points in the
        // inner square, a the position on the current square, n = p + a
        double r = Math.floor((Math.sqrt(n + 1) - 1) / 2) + 1;

        // radius: inverse arithmetic sum of 8+16+24+... total points on
        // radius - 1
        double p = (8 * r * (r - 1)) / 2;

        double en = r * 2;
        // points by face

        double a = (1 + n - p) % (r * 8);
        // position, shifted so the first is (-r,-r) not (-r+1,-r) so squares
        // connect

        switch ((int) Math.floor(a / (r * 2))) {
            // which face: 0 top, 1 right, 2 bottom, 3 left
            case 0:
                x = a - r;
                z = -r;
                break;
            case 1:
                x = r;
                z = (a % en) - r;
                break;
            case 2:
                x = r - (a % en);
                z = r;
                break;
            case 3:
                x = -r;
                z = r - (a % en);
                break;
        }

        return BlockPos.containing(this.worldPosition.getX() + x * 48, this.worldPosition.getY(), this.worldPosition.getZ() + z * 48);
    }

    @Override
    protected void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);

        compound.put("currentCrystal", StackTag.saveStack(currentCrystal));

        compound.putInt("state", state.ordinal());
        compound.putBoolean("powered", powered);

        if (biomeToSearch != null) {
            compound.putString("biomeToSearch", lumien.randomthings.util.BiomeUtil.id(biomeToSearch).toString());
        }

        compound.putInt("searchCounter", searchCounter);
        compound.putInt("antennaCounter", antennaCounter);

        for (int i = 0; i < antennaBiomes.length; i++) {
            if (antennaBiomes[i] != null) {
                compound.putString("antennaBiome" + i, antennaBiomes[i]);
            }
        }

    }

    @Override
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);

        this.currentCrystal = ItemStack.parseOptional(StackTag.provider(), compound.getCompound("currentCrystal"));
        this.state = STATE.values()[compound.getInt("state")];
        this.powered = compound.getBoolean("powered");

        if (compound.contains("biomeToSearch")) {
            this.biomeToSearch = lumien.randomthings.util.BiomeUtil.byId(new ResourceLocation(compound.getString("biomeToSearch")));
        }

        this.searchCounter = compound.getInt("searchCounter");
        this.antennaCounter = compound.getInt("antennaCounter");

        for (int i = 0; i < antennaBiomes.length; i++) {
            if (compound.contains("antennaBiome" + i)) {
                antennaBiomes[i] = compound.getString("antennaBiome" + i);
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

/**
     * {@code TileEntity.getUpdateTag()}'s default implementation calls the
     * *private* {@code writeInternal()} directly (confirmed via `javap -c`),
     * not the public, overridable {@code write()} - so without this override
     * the sync packet above would carry only the base id/position, never
     * this TE's own state/crystal/search fields. Found while chasing an
     * identical pre-existing bug in `RedstoneObserverTileEntity` (its target
     * line wasn't surviving a world reload) - fixed here proactively before
     * it could cause the same silent desync for a reconnecting/reloading
     * client.
     */
    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private void syncTE() {
        if (this.level != null) {
            this.setChanged();
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public ItemStack getCurrentCrystal() {
        return currentCrystal;
    }

    public void setCrystal(ItemStack crystal) {
        this.currentCrystal = crystal;
        this.syncTE();
    }

    public STATE getState() {
        return state;
    }

    public void neighborChanged(Block neighborBlock) {
        boolean newPowered = this.level.getBestNeighborSignal(this.worldPosition) > 0;
        boolean changed = false;

        if (!this.powered && newPowered && this.state == STATE.IDLE && !this.currentCrystal.isEmpty() && isValid()) {
            Biome biome = BiomeCrystalItem.getBiome(this.currentCrystal);

            if (biome != null) {
                this.state = STATE.SEARCHING;
                this.searchCounter = 0;
                this.biomeToSearch = biome;
                changed = true;
            }
        } else if (this.state == STATE.SEARCHING && !newPowered && powered) {
            this.state = STATE.IDLE;
            changed = true;
        } else if (this.state == STATE.FINISHED && powered && !newPowered) {
            this.state = STATE.IDLE;
            changed = true;
        }

        this.powered = newPowered;

        if (changed) {
            this.syncTE();
        }
    }

    public void setAntennaBiomes(String[] antennaBiomes) {
        this.antennaBiomes = antennaBiomes;
    }

    public ItemStack generatePositionFilter() {
        BlockPos foundPos = getPos(searchCounter);
        ItemStack positionFilter = new ItemStack(ModItems.POSITION_FILTER);

        PositionFilterItem.setPosition(positionFilter, this.level.dimension().location().toString(), foundPos);

        return positionFilter;
    }

    private boolean isValid() {
        List<BlockPos> posToCheck = new ArrayList<>();

        posToCheck.add(this.worldPosition.relative(Direction.UP));
        posToCheck.add(this.worldPosition.relative(Direction.UP, 2));

        posToCheck.add(this.worldPosition.offset(1, 2, 0));
        posToCheck.add(this.worldPosition.offset(-1, 2, 0));
        posToCheck.add(this.worldPosition.offset(1, 3, 0));
        posToCheck.add(this.worldPosition.offset(-1, 3, 0));

        posToCheck.add(this.worldPosition.offset(0, 2, 1));
        posToCheck.add(this.worldPosition.offset(0, 2, -1));
        posToCheck.add(this.worldPosition.offset(0, 3, 1));
        posToCheck.add(this.worldPosition.offset(0, 3, -1));

        for (BlockPos p : posToCheck) {
            if (this.level.getBlockState(p).getBlock() != Blocks.IRON_BARS) {
                return false;
            }
        }

        return true;
    }
}
