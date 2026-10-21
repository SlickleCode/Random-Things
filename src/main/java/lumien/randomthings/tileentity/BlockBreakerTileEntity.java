package lumien.randomthings.tileentity;

import lumien.randomthings.util.StackTag;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import lumien.randomthings.block.BlockBreakerBlock;
import lumien.randomthings.enchantment.ModEnchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * Ported from 1.12.2's {@code TileEntityBlockBreaker}: continuously mines the
 * block in front of it (real per-block hardness/tool speed, matching
 * survival mining exactly) unless redstone-powered, via a {@code FakePlayer}
 * holding an unbreakable pickaxe.
 *
 * <p>1.12.2 caught the fake player's drops with its own dedicated ASM hook
 * (a separate {@code ItemCatcher} path). This port reuses the block's own
 * enchantment for that instead: the fake player's pickaxe carries {@link
 * ModEnchantments#MAGNETIC}, and the global Magnetic listener in {@code
 * RandomThings} (a plain {@code BlockEvent.HarvestDropsEvent} hook, not tied
 * to a real player) already redirects any harvester's drops straight into
 * its inventory - the fake player included. So after {@code tryHarvestBlock}
 * returns, this class only has to drain that inventory into the target side,
 * not intercept anything itself.
 *
 * <p>{@code FakePlayer} never gets a real {@code ServerPlayerEntity#connection} assigned, and
 * {@code ForgeHooks#onBlockBreakEvent} unconditionally calls {@code entityPlayer.connection
 * .sendPacket(...)} when breaking a block with no tile entity, which NPEs without a workaround.
 * 1.12.2's original handled this by assigning a no-op net handler in {@code initFakePlayer()};
 * this port does the same (see below).
 */
public class BlockBreakerTileEntity extends BlockEntity {
    private static final GameProfile BREAKER_PROFILE = new GameProfile(UUID.nameUUIDFromBytes("RTBlockBreaker".getBytes(StandardCharsets.UTF_8)), "RTBlockBreaker");

    private UUID uuid;
    private boolean mining;
    private boolean canMine = true;
    private float curBlockDamage;
    private boolean firstTick = true;

    private WeakReference<FakePlayer> fakePlayer;

    public BlockBreakerTileEntity(BlockPos pos, BlockState state) {
        super(ModTileEntityTypes.BLOCK_BREAKER, pos, state);
    }

    private FakePlayer initFakePlayer() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
            setChanged();
        }

        FakePlayer player = FakePlayerFactory.get((ServerLevel) level, BREAKER_PROFILE);

        ItemStack pickaxe = new ItemStack(Items.IRON_PICKAXE);
        pickaxe.set(net.minecraft.core.component.DataComponents.UNBREAKABLE, new net.minecraft.world.item.component.Unbreakable(false));
        pickaxe.enchant(ModEnchantments.MAGNETIC, 1);

        player.setItemInHand(InteractionHand.MAIN_HAND, pickaxe);
        player.setOnGround(true);

        // ForgeHooks#onBlockBreakEvent unconditionally does
        // entityPlayer.connection.sendPacket(...) when breaking a block with no tile
        // entity, but FakePlayer never gets a real ServerPlayNetHandler assigned - it's
        // null, which NPEs tryHarvestBlock. 1.12.2 worked around this the same way: give
        // it a real net handler whose sendPacket is a no-op.
        player.connection = new ServerGamePacketListenerImpl(level.getServer(), new Connection(PacketFlow.SERVERBOUND), player, net.minecraft.server.network.CommonListenerCookie.createInitial(BREAKER_PROFILE, false)) {
            @Override
            public void send(Packet<?> packetIn) {
            }
        };

        fakePlayer = new WeakReference<>(player);
        return player;
    }

    public void tick() {
        if (level == null || level.isClientSide) {
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

        Direction facing = level.getBlockState(worldPosition).getValue(BlockBreakerBlock.FACING);
        BlockPos targetPos = worldPosition.relative(facing);
        BlockState targetState = level.getBlockState(targetPos);

        FakePlayer player = fakePlayer != null ? fakePlayer.get() : null;
        if (player == null) {
            player = initFakePlayer();
        }

        curBlockDamage += targetState.getDestroyProgress(player, level, targetPos);

        if (curBlockDamage < 1.0F) {
            level.destroyBlockProgress(uuid.hashCode(), targetPos, (int) (curBlockDamage * 10.0F) - 1);
            return;
        }

        mining = false;
        resetProgress(level.getBlockState(worldPosition));

        player.gameMode.destroyBlock(targetPos);

        IItemHandler target = null;
        BlockEntity neighborTe = level.getBlockEntity(worldPosition.relative(facing.getOpposite()));
        if (neighborTe != null) {
            target = lumien.randomthings.util.CapUtil.itemHandler(neighborTe, facing);
        }

        for (int i = 1; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            player.getInventory().setItem(i, ItemStack.EMPTY);

            if (stack.isEmpty()) {
                continue;
            }

            ItemStack remainder = target != null ? ItemHandlerHelper.insertItemStacked(target, stack, false) : stack;

            if (!remainder.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + facing.getStepX(), worldPosition.getY() + facing.getStepY(), worldPosition.getZ() + facing.getStepZ(), remainder);
            }
        }
    }

    public void neighborChanged() {
        if (level == null) {
            return;
        }

        BlockState state = level.getBlockState(worldPosition);
        Direction facing = state.getValue(BlockBreakerBlock.FACING);
        BlockPos targetPos = worldPosition.relative(facing);

        canMine = !level.hasNeighborSignal(worldPosition);

        if (canMine && !level.isEmptyBlock(targetPos)) {
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
            resetProgress(level.getBlockState(worldPosition));
        }
    }

    private void resetProgress(BlockState state) {
        if (uuid == null || level == null) {
            return;
        }

        BlockPos targetPos = worldPosition.relative(state.getValue(BlockBreakerBlock.FACING));
        level.destroyBlockProgress(uuid.hashCode(), targetPos, -1);
        curBlockDamage = 0;
    }

    @Override
    protected void saveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(compound, registries);

        if (uuid != null) {
            compound.put("uuid", NbtUtils.createUUID(uuid));
        }
        compound.putBoolean("mining", mining);
        compound.putBoolean("canMine", canMine);
        compound.putFloat("curBlockDamage", curBlockDamage);
    }

    @Override
    public void loadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {
        super.loadAdditional(compound, registries);

        if (compound.contains("uuid")) {
            uuid = NbtUtils.loadUUID(compound.get("uuid"));
        }
        mining = compound.getBoolean("mining");
        canMine = compound.getBoolean("canMine");
        curBlockDamage = compound.getFloat("curBlockDamage");
    }
}
