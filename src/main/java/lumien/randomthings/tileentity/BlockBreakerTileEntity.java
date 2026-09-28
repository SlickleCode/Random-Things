package lumien.randomthings.tileentity;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import lumien.randomthings.block.BlockBreakerBlock;
import lumien.randomthings.enchantment.ModEnchantments;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

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
 * <p>Not build-verified in this sandbox (network policy blocks the Forge/
 * Mojang Maven hosts {@code ./gradlew build} needs, so no {@code javap}
 * ground-truthing was possible for {@code PlayerInteractionManager
 * #tryHarvestBlock}/{@code FakePlayerFactory} in this exact Forge build) -
 * flag for retest on a real build.
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

        Map<Enchantment, Integer> enchantments = new HashMap<>();
        enchantments.put(ModEnchantments.MAGNETIC, 1);
        EnchantmentHelper.setEnchantments(enchantments, pickaxe);

        player.setHeldItem(Hand.MAIN_HAND, pickaxe);
        player.onGround = true;

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

        IItemHandler target = null;
        TileEntity neighborTe = world.getTileEntity(pos.offset(facing.getOpposite()));
        if (neighborTe != null) {
            target = neighborTe.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing).orElse(null);
        }

        for (int i = 1; i < player.inventory.mainInventory.size(); i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);

            if (stack.isEmpty()) {
                continue;
            }

            ItemStack remainder = target != null ? ItemHandlerHelper.insertItemStacked(target, stack, false) : stack;

            if (!remainder.isEmpty()) {
                InventoryHelper.spawnItemStack(world, pos.getX() + facing.getXOffset(), pos.getY() + facing.getYOffset(), pos.getZ() + facing.getZOffset(), remainder);
            }
        }
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
            compound.put("uuid", NBTUtil.writeUniqueId(uuid));
        }
        compound.putBoolean("mining", mining);
        compound.putBoolean("canMine", canMine);
        compound.putFloat("curBlockDamage", curBlockDamage);
        return compound;
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);

        if (compound.contains("uuid")) {
            uuid = NBTUtil.readUniqueId(compound.getCompound("uuid"));
        }
        mining = compound.getBoolean("mining");
        canMine = compound.getBoolean("canMine");
        curBlockDamage = compound.getFloat("curBlockDamage");
    }
}
