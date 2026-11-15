package lumien.randomthings.tileentity;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import lumien.randomthings.block.BlockBreakerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

/**
 * Ported from 1.12.2's {@code TileEntityBlockBreaker}: continuously mines the
 * block in front of it (real per-block hardness/tool speed, matching
 * survival mining exactly) unless redstone-powered, via a {@code FakePlayer}
 * holding an unbreakable pickaxe.
 *
 * <p>The fake player's pickaxe carries no enchantment: a mined block breaks exactly like survival mining and its
 * drops spawn in the world in front of the breaker. Nothing is collected or pushed into an inventory.
 *
 * <p>{@code FakePlayer} never gets a real {@code ServerPlayerEntity#connection} assigned, and
 * {@code ForgeHooks#onBlockBreakEvent} unconditionally calls {@code entityPlayer.connection
 * .sendPacket(...)} when breaking a block with no tile entity, which NPEs without a workaround.
 * 1.12.2's original handled this by assigning a no-op net handler in {@code initFakePlayer()};
 * this port does the same (see below).
 */
public class BlockBreakerTileEntity extends TileEntity implements ITickableTileEntity {
    private static final GameProfile BREAKER_PROFILE = new GameProfile(UUID.nameUUIDFromBytes("RTBlockBreaker".getBytes(StandardCharsets.UTF_8)), "RTBlockBreaker");

    private UUID uuid;
    private boolean mining;
    private boolean canMine = true;
    private float curBlockDamage;
    private boolean firstTick = true;

    private WeakReference<FakePlayer> fakePlayer;

    public BlockBreakerTileEntity() {
        super(ModTileEntityTypes.BLOCK_BREAKER);
    }

    private FakePlayer initFakePlayer() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
            markDirty();
        }

        FakePlayer player = FakePlayerFactory.get((ServerWorld) world, BREAKER_PROFILE);

        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        pickaxe.getOrCreateTag().putBoolean("Unbreakable", true);


        player.setHeldItem(Hand.MAIN_HAND, pickaxe);
        player.setOnGround(true);

        // ForgeHooks#onBlockBreakEvent unconditionally does
        // entityPlayer.connection.sendPacket(...) when breaking a block with no tile
        // entity, but FakePlayer never gets a real ServerPlayNetHandler assigned - it's
        // null, which NPEs tryHarvestBlock. 1.12.2 worked around this the same way: give
        // it a real net handler whose sendPacket is a no-op.
        player.connection = new ServerPlayNetHandler(world.getServer(), new NetworkManager(PacketDirection.SERVERBOUND), player) {
            @Override
            public void sendPacket(IPacket<?> packetIn) {
            }
        };

        fakePlayer = new WeakReference<>(player);
        return player;
    }

    @Override
    public void tick() {
        if (world == null || world.isRemote) {
            return;
        }

        if (firstTick) {
            firstTick = false;
            initFakePlayer();
            neighborChanged();
        }

        if (!mining) {
            return;
        }

        Direction facing = world.getBlockState(pos).get(BlockBreakerBlock.FACING);
        BlockPos targetPos = pos.offset(facing);
        BlockState targetState = world.getBlockState(targetPos);

        FakePlayer player = fakePlayer != null ? fakePlayer.get() : null;
        if (player == null) {
            player = initFakePlayer();
        }

        curBlockDamage += targetState.getPlayerRelativeBlockHardness(player, world, targetPos);

        if (curBlockDamage < 1.0F) {
            world.sendBlockBreakProgress(uuid.hashCode(), targetPos, (int) (curBlockDamage * 10.0F) - 1);
            return;
        }

        mining = false;
        resetProgress(world.getBlockState(pos));

        player.interactionManager.tryHarvestBlock(targetPos);
    }

    public void neighborChanged() {
        if (world == null) {
            return;
        }

        BlockState state = world.getBlockState(pos);
        Direction facing = state.get(BlockBreakerBlock.FACING);
        BlockPos targetPos = pos.offset(facing);

        canMine = !world.isBlockPowered(pos);

        if (canMine && !world.isAirBlock(targetPos)) {
            if (!mining) {
                mining = true;
                curBlockDamage = 0;
            }
        } else if (mining) {
            mining = false;
            resetProgress(state);
        }
    }

    public void onBreakerBroken() {
        if (mining && uuid != null) {
            resetProgress(world.getBlockState(pos));
        }
    }

    private void resetProgress(BlockState state) {
        if (uuid == null || world == null) {
            return;
        }

        BlockPos targetPos = pos.offset(state.get(BlockBreakerBlock.FACING));
        world.sendBlockBreakProgress(uuid.hashCode(), targetPos, -1);
        curBlockDamage = 0;
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        if (uuid != null) {
            compound.put("uuid", NBTUtil.func_240626_a_(uuid));
        }
        compound.putBoolean("mining", mining);
        compound.putBoolean("canMine", canMine);
        compound.putFloat("curBlockDamage", curBlockDamage);
        return compound;
    }

    @Override
    public void read(BlockState tileState, CompoundNBT compound) {
        super.read(tileState, compound);

        if (compound.contains("uuid")) {
            uuid = lumien.randomthings.util.UuidTag.read(compound.get("uuid"));
        }
        mining = compound.getBoolean("mining");
        canMine = compound.getBoolean("canMine");
        curBlockDamage = compound.getFloat("curBlockDamage");
    }
}
