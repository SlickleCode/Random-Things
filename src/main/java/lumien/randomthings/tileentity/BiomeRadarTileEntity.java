package lumien.randomthings.tileentity;

import lumien.randomthings.item.BiomeCrystalItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.PositionFilterItem;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.network.messages.BiomeRadarAntennaMessage;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

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
public class BiomeRadarTileEntity extends TileEntity implements ITickableTileEntity {
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

    public BiomeRadarTileEntity() {
        super(ModTileEntityTypes.BIOME_RADAR);
    }

    @Override
    public void tick() {
        if (!this.world.isRemote) {
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
                    Biome testBiome = this.world.getBiome(testPos);
                    ResourceLocation registryName = ForgeRegistries.BIOMES.getKey(testBiome);

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

                    if (testBiome == biomeToSearch) {
                        this.state = STATE.FINISHED;

                        this.syncTE();
                        break;
                    }

                    searchCounter++;
                }

                if (searchCounter % 100 == 0 && changedColor) {
                    RTPacketHandler.sendToAllNear(new BiomeRadarAntennaMessage(this.pos, antennaBiomes), this.world, this.pos, 64);
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
                    Biome biome = ForgeRegistries.BIOMES.getValue(new ResourceLocation(antennaBiomes[i]));

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

                int rgb = lumien.randomthings.util.BiomeColorUtil.getBiomeColor(null, biome, this.pos);
                this.world.addParticle(new RedstoneParticleData(((rgb >> 16 & 255) / 255F), ((rgb >> 8 & 255) / 255F), ((rgb & 255) / 255F), 1.0F), this.pos.getX() + 0.5, this.pos.getY() + 3.1, this.pos.getZ() + 0.5, 0, 0, 0);
            }
        }
    }

    private void spawnAntennaParticle(int i, Biome biome) {
        int rgb = lumien.randomthings.util.BiomeColorUtil.getBiomeColor(null, biome, this.pos);

        double x = i < 2 ? this.pos.getX() + 1.5 - i * 2 : this.pos.getX() + 0.5;
        double z = i > 1 ? this.pos.getZ() + 1.5 - (i - 2) * 2 : this.pos.getZ() + 0.5;

        this.world.addParticle(new RedstoneParticleData(((rgb >> 16 & 255) / 255F), ((rgb >> 8 & 255) / 255F), ((rgb & 255) / 255F), 1.0F), x, this.pos.getY() + 4.1, z, 0, 0, 0);
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

        return new BlockPos(this.pos.getX() + x * 48, this.pos.getY(), this.pos.getZ() + z * 48);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        compound.put("currentCrystal", currentCrystal.write(new CompoundNBT()));

        compound.putInt("state", state.ordinal());
        compound.putBoolean("powered", powered);

        if (biomeToSearch != null) {
            compound.putString("biomeToSearch", ForgeRegistries.BIOMES.getKey(biomeToSearch).toString());
        }

        compound.putInt("searchCounter", searchCounter);
        compound.putInt("antennaCounter", antennaCounter);

        for (int i = 0; i < antennaBiomes.length; i++) {
            if (antennaBiomes[i] != null) {
                compound.putString("antennaBiome" + i, antennaBiomes[i]);
            }
        }

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        this.currentCrystal = ItemStack.read(compound.getCompound("currentCrystal"));
        this.state = STATE.values()[compound.getInt("state")];
        this.powered = compound.getBoolean("powered");

        if (compound.contains("biomeToSearch")) {
            this.biomeToSearch = ForgeRegistries.BIOMES.getValue(new ResourceLocation(compound.getString("biomeToSearch")));
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
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, getUpdateTag());
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
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());
    }

    private void syncTE() {
        if (this.world != null) {
            this.markDirty();
            this.world.notifyBlockUpdate(this.pos, this.getBlockState(), this.getBlockState(), 3);
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
        boolean newPowered = this.world.getRedstonePowerFromNeighbors(this.pos) > 0;
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

        PositionFilterItem.setPosition(positionFilter, this.world.getDimension().getType().getId(), foundPos);

        return positionFilter;
    }

    private boolean isValid() {
        List<BlockPos> posToCheck = new ArrayList<>();

        posToCheck.add(this.pos.offset(Direction.UP));
        posToCheck.add(this.pos.offset(Direction.UP, 2));

        posToCheck.add(this.pos.add(1, 2, 0));
        posToCheck.add(this.pos.add(-1, 2, 0));
        posToCheck.add(this.pos.add(1, 3, 0));
        posToCheck.add(this.pos.add(-1, 3, 0));

        posToCheck.add(this.pos.add(0, 2, 1));
        posToCheck.add(this.pos.add(0, 2, -1));
        posToCheck.add(this.pos.add(0, 3, 1));
        posToCheck.add(this.pos.add(0, 3, -1));

        for (BlockPos p : posToCheck) {
            if (this.world.getBlockState(p).getBlock() != Blocks.IRON_BARS) {
                return false;
            }
        }

        return true;
    }
}
