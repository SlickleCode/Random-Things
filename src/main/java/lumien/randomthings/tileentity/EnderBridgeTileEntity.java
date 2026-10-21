package lumien.randomthings.tileentity;

import lumien.randomthings.block.EnderBridgeBlockBase;
import lumien.randomthings.block.ModBlocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Redstone-triggered "teleport pad": power it, and every tick (or, for
 * {@link lumien.randomthings.tileentity.PrismarineEnderBridgeTileEntity},
 * every tick x10 - see {@link #getScansPerTick}) it looks one block further
 * out along its facing direction for a matching {@link
 * lumien.randomthings.block.EnderAnchorBlock}. If it finds one, it teleports
 * any whitelisted entity within a small radius of itself to just above the
 * anchor; if it instead hits any other solid block first, it gives up and
 * waits for redstone to cycle off and back on. Direct port of 1.12.2's
 * {@code TileEntityEnderBridge}/{@code TileEntityPrismarineEnderBridge} -
 * unified into one class since the two were otherwise identical (the
 * Prismarine variant just scans 10x faster).
 * <p>
 * The scan skips unloaded chunks, so an Anchor's chunk must be loaded for the
 * bridge to find it: like 1.12.2 (where {@code EnderAnchorChunkloading}
 * defaults to on), each Anchor keeps its own chunk force-loaded, via
 * {@link lumien.randomthings.handler.AnchorChunkLoader} rather than the
 * removed {@code ForgeChunkManager} tickets - see
 * {@link EnderAnchorTileEntity}.
 * {@code EntityEnderConnection}, 1.12.2's own dead/non-functional stub entity
 * (its {@code onCollideWithPlayer} built a list and never used it), isn't
 * ported either - it did nothing to port.
 */
public class EnderBridgeTileEntity extends BlockEntity {
    private static final Set<Class<? extends Entity>> ENTITY_WHITELIST = new HashSet<>();

    static {
        ENTITY_WHITELIST.add(ServerPlayer.class);
        ENTITY_WHITELIST.add(ItemEntity.class);
        ENTITY_WHITELIST.add(AbstractMinecart.class);
    }

    private enum BridgeState {
        IDLE, SCANNING, WAITING
    }

    private BridgeState state = BridgeState.IDLE;
    private boolean redstonePowered = false;
    private int scanningCounter = 2;

    public EnderBridgeTileEntity(BlockPos pos, BlockState state) {
        this(ModTileEntityTypes.ENDER_BRIDGE, pos, state);
    }

    protected EnderBridgeTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected int getScansPerTick() {
        return 1;
    }

    public void tick() {
        if (this.level.isClientSide) {
            return;
        }

        for (int i = 0; i < getScansPerTick() && state == BridgeState.SCANNING; i++) {
            BlockState blockState = this.level.getBlockState(this.worldPosition);
            Direction facing = blockState.getValue(EnderBridgeBlockBase.FACING);

            BlockPos nextPos = this.worldPosition.relative(facing, scanningCounter);

            if (this.level.hasChunkAt(nextPos)) {
                BlockState nextState = this.level.getBlockState(nextPos);

                if (nextState.getBlock() == ModBlocks.ENDER_ANCHOR) {
                    List<Entity> entityList = this.level.getEntitiesOfClass(Entity.class, new AABB(this.worldPosition.getX() - 2, this.worldPosition.getY() - 2, this.worldPosition.getZ() - 2, this.worldPosition.getX() + 2, this.worldPosition.getY() + 2, this.worldPosition.getZ() + 2));

                    if (!entityList.isEmpty()) {
                        BlockPos target = nextPos.above();

                        for (Entity e : entityList) {
                            if (ENTITY_WHITELIST.stream().anyMatch(c -> c.isAssignableFrom(e.getClass()))) {
                                teleportEntity(e, target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
                            }
                        }
                    }

                    state = BridgeState.WAITING;
                    this.level.setBlockAndUpdate(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(EnderBridgeBlockBase.ACTIVE, false));
                } else if (!nextState.isAir()) {
                    state = BridgeState.WAITING;
                    this.level.setBlockAndUpdate(this.worldPosition, this.level.getBlockState(this.worldPosition).setValue(EnderBridgeBlockBase.ACTIVE, false));
                }
            }

            scanningCounter++;
        }
    }

    private static void teleportEntity(Entity e, double x, double y, double z) {
        if (e instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) e;
            player.connection.teleport(x, y, z, player.getYRot(), player.getXRot());
        } else {
            e.teleportTo(x, y, z);
        }
    }

    public void neighborChanged(Level worldIn, BlockPos pos) {
        boolean powered = worldIn.hasNeighborSignal(pos);

        if (powered != redstonePowered) {
            if (state == BridgeState.IDLE && powered) {
                scanningCounter = 2;
                state = BridgeState.SCANNING;
                worldIn.setBlockAndUpdate(pos, worldIn.getBlockState(pos).setValue(EnderBridgeBlockBase.ACTIVE, true));
            } else if (state == BridgeState.SCANNING && !powered) {
                state = BridgeState.IDLE;
                worldIn.setBlockAndUpdate(pos, worldIn.getBlockState(pos).setValue(EnderBridgeBlockBase.ACTIVE, false));
            } else if (state == BridgeState.WAITING && !powered) {
                state = BridgeState.IDLE;
            }
            redstonePowered = powered;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);

        compound.putInt("state", state.ordinal());
        compound.putBoolean("redstonePowered", redstonePowered);
        compound.putInt("scanningCounter", scanningCounter);

    }

    @Override
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);

        state = BridgeState.values()[compound.getInt("state")];
        redstonePowered = compound.getBoolean("redstonePowered");
        scanningCounter = compound.getInt("scanningCounter");
    }
}
