package lumien.randomthings.tileentity;

import lumien.randomthings.util.StackTag;
import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import lumien.randomthings.block.BlockBreakerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
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
        pickaxe.set(net.minecraft.core.component.DataComponents.UNBREAKABLE, net.minecraft.util.Unit.INSTANCE);

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
        if (level == null || level.isClientSide()) {
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
    protected void saveAdditional(net.minecraft.world.level.storage.ValueOutput rtOut)
    {
    	super.saveAdditional(rtOut);
    	CompoundTag rtTag = new CompoundTag();
    	rtSaveAdditional(rtTag, lumien.randomthings.util.StackTag.provider());
    	lumien.randomthings.util.TagIO.write(rtOut, rtTag);
    }

    private void rtSaveAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {

        if (uuid != null) {
            compound.put("uuid", lumien.randomthings.util.TagIO.writeUuid(uuid));
        }
        compound.putBoolean("mining", mining);
        compound.putBoolean("canMine", canMine);
        compound.putFloat("curBlockDamage", curBlockDamage);
    }

    @Override
    protected void loadAdditional(net.minecraft.world.level.storage.ValueInput rtIn)
    {
    	super.loadAdditional(rtIn);
    	rtLoadAdditional(lumien.randomthings.util.TagIO.read(rtIn), rtIn.lookup());
    }

    private void rtLoadAdditional(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries) {

        if (compound.contains("uuid")) {
            uuid = lumien.randomthings.util.TagIO.readUuid(compound.get("uuid"));
        }
        mining = compound.getBooleanOr("mining", false);
        canMine = compound.getBooleanOr("canMine", false);
        curBlockDamage = compound.getFloatOr("curBlockDamage", 0.0F);
    }

	@Override
	public void preRemoveSideEffects(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState state)
	{
		super.preRemoveSideEffects(pos, state);

		// 1.21.5+: Block.onRemove is gone and the tile entity is already removed when the neighbour callback runs, so the block's old onRemove code runs from here
		if (this.level != null && state.getBlock() instanceof lumien.randomthings.block.BlockBreakerBlock block)
		{
			block.rtOnRemove(state, this.level, pos, this.level.getBlockState(pos), false);
		}
	}
}
