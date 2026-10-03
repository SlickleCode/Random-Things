package lumien.randomthings.tileentity;

import lumien.randomthings.block.EnderBridgeBlockBase;
import lumien.randomthings.block.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
public class EnderBridgeTileEntity extends TileEntity implements ITickableTileEntity {
    private static final Set<Class<? extends Entity>> ENTITY_WHITELIST = new HashSet<>();

    static {
        ENTITY_WHITELIST.add(ServerPlayerEntity.class);
        ENTITY_WHITELIST.add(ItemEntity.class);
        ENTITY_WHITELIST.add(AbstractMinecartEntity.class);
    }

    private enum BridgeState {
        IDLE, SCANNING, WAITING
    }

    private BridgeState state = BridgeState.IDLE;
    private boolean redstonePowered = false;
    private int scanningCounter = 2;

    public EnderBridgeTileEntity() {
        this(ModTileEntityTypes.ENDER_BRIDGE);
    }

    protected EnderBridgeTileEntity(TileEntityType<?> type) {
        super(type);
    }

    protected int getScansPerTick() {
        return 1;
    }

    @Override
    public void tick() {
        if (this.world.isRemote) {
            return;
        }

        for (int i = 0; i < getScansPerTick() && state == BridgeState.SCANNING; i++) {
            BlockState blockState = this.world.getBlockState(this.pos);
            Direction facing = blockState.get(EnderBridgeBlockBase.FACING);

            BlockPos nextPos = this.pos.offset(facing, scanningCounter);

            if (this.world.isBlockLoaded(nextPos)) {
                BlockState nextState = this.world.getBlockState(nextPos);

                if (nextState.getBlock() == ModBlocks.ENDER_ANCHOR) {
                    List<Entity> entityList = this.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.pos.getX() - 2, this.pos.getY() - 2, this.pos.getZ() - 2, this.pos.getX() + 2, this.pos.getY() + 2, this.pos.getZ() + 2));

                    if (!entityList.isEmpty()) {
                        BlockPos target = nextPos.up();

                        for (Entity e : entityList) {
                            if (ENTITY_WHITELIST.stream().anyMatch(c -> c.isAssignableFrom(e.getClass()))) {
                                teleportEntity(e, target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
                            }
                        }
                    }

                    state = BridgeState.WAITING;
                    this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(EnderBridgeBlockBase.ACTIVE, false));
                } else if (!nextState.isAir()) {
                    state = BridgeState.WAITING;
                    this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).with(EnderBridgeBlockBase.ACTIVE, false));
                }
            }

            scanningCounter++;
        }
    }

    private static void teleportEntity(Entity e, double x, double y, double z) {
        if (e instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) e;
            player.connection.setPlayerLocation(x, y, z, player.rotationYaw, player.rotationPitch);
        } else {
            e.setPositionAndUpdate(x, y, z);
        }
    }

    public void neighborChanged(World worldIn, BlockPos pos) {
        boolean powered = worldIn.isBlockPowered(pos);

        if (powered != redstonePowered) {
            if (state == BridgeState.IDLE && powered) {
                scanningCounter = 2;
                state = BridgeState.SCANNING;
                worldIn.setBlockState(pos, worldIn.getBlockState(pos).with(EnderBridgeBlockBase.ACTIVE, true));
            } else if (state == BridgeState.SCANNING && !powered) {
                state = BridgeState.IDLE;
                worldIn.setBlockState(pos, worldIn.getBlockState(pos).with(EnderBridgeBlockBase.ACTIVE, false));
            } else if (state == BridgeState.WAITING && !powered) {
                state = BridgeState.IDLE;
            }
            redstonePowered = powered;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        compound.putInt("state", state.ordinal());
        compound.putBoolean("redstonePowered", redstonePowered);
        compound.putInt("scanningCounter", scanningCounter);

        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        state = BridgeState.values()[compound.getInt("state")];
        redstonePowered = compound.getBoolean("redstonePowered");
        scanningCounter = compound.getInt("scanningCounter");
    }
}
