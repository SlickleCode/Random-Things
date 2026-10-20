package lumien.randomthings.item;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A bucket that picks up fluid by searching the whole connected body it's
 * pointed at (up to 2000 blocks via flood fill) for any position it can
 * actually drain from, rather than requiring an exact hit on a source block
 * - the original's "Ender" gimmick is reach into a lake/river's edge and
 * still get a real pickup. Holds exactly one 1000mB bucket's worth. Direct
 * port of 1.12.2's {@code ItemEnderBucket}; this port's first feature to
 * touch Forge's fluid capability API, ground-truthed fresh via {@code javap}
 * rather than assumed - 1.14.4 already uses the modern {@code LazyOptional}-
 * wrapped capability/{@code IFluidHandler} system essentially unchanged from
 * how it stayed for years afterward, so nothing here is provisional.
 */
public class EnderBucketItem extends Item {
    private static final int CAPACITY = 1000;

    public EnderBucketItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return getContainedFluid(stack) != null ? 1 : 16;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        return new FluidHandlerItemStackSimple(stack, CAPACITY);
    }

    @Override
    public Component getName(ItemStack stack) {
        FluidStack contained = getContainedFluid(stack);

        if (contained == null) {
            return super.getName(stack);
        }

        return Component.translatable(this.getDescriptionId(stack) + ".filled", contained.getDisplayName());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        FluidStack contained = getContainedFluid(stack);

        if (contained != null && contained.getAmount() >= CAPACITY) {
            return place(world, player, hand, stack, contained);
        }

        return pickUp(world, player, stack);
    }

    private InteractionResultHolder<ItemStack> place(Level world, Player player, InteractionHand hand, ItemStack stack, FluidStack contained) {
        HitResult result = getPlayerPOVHitResult(world, player, ClipContext.Fluid.NONE);

        if (result == null || result.getType() != HitResult.Type.BLOCK) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }

        BlockHitResult blockResult = (BlockHitResult) result;
        BlockPos clickPos = blockResult.getBlockPos();

        if (!world.mayInteract(player, clickPos)) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

        BlockPos targetPos = clickPos.relative(blockResult.getDirection());

        if (!player.mayUseItemAt(targetPos, blockResult.getDirection(), stack)) {
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

        FluidActionResult placeResult = FluidUtil.tryPlaceFluid(player, world, hand, targetPos, stack, contained);

        if (placeResult.isSuccess()) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, placeResult.getResult());
        }

        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    private InteractionResultHolder<ItemStack> pickUp(Level world, Player player, ItemStack stack) {
        HitResult result = getPlayerPOVHitResult(world, player, ClipContext.Fluid.ANY);

        if (result == null || result.getType() != HitResult.Type.BLOCK) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }

        BlockPos hitPos = ((BlockHitResult) result).getBlockPos();

        if (!isFluidBlock(world.getBlockState(hitPos).getBlock())) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }

        List<BlockPos> toCheck = new ArrayList<>();
        Set<BlockPos> checked = new HashSet<>();
        toCheck.add(hitPos);

        while (!toCheck.isEmpty()) {
            BlockPos next = toCheck.remove(0);
            checked.add(next);

            if (checked.size() > 2000) {
                break;
            }

            if (!world.hasChunkAt(next)) {
                continue;
            }

            BlockState state = world.getBlockState(next);
            Block block = state.getBlock();

            if (!isFluidBlock(block)) {
                continue;
            }

            FluidStack drained = drainSource(world, next, state, block);

            if (drained != null) {
                return finishPickup(player, stack, drained);
            }

            for (Direction facing : Direction.values()) {
                BlockPos adding = next.relative(facing);

                if (checked.add(adding)) {
                    toCheck.add(adding);
                }
            }
        }

        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    private static boolean isFluidBlock(Block block) {
        return block instanceof BucketPickup || block instanceof IFluidBlock;
    }

    /**
     * This Forge build's own {@code FluidUtil.tryPickUpFluid} (ground-truthed via {@code javap -c}) only
     * ever resolves a tile-entity-backed fluid capability at the target position, for both branches
     * (including its {@code IFluidBlock} check) - it has no code path at all for vanilla water/lava
     * ({@code IBucketPickupHandler}) or for a real modded fluid block without a TE, so pickup always
     * failed. Draining directly against each interface's own self-contained method instead - both mutate
     * the world themselves on success, no capability lookup needed.
     */
    private static FluidStack drainSource(Level world, BlockPos pos, BlockState state, Block block) {
        if (block instanceof IFluidBlock) {
            return ((IFluidBlock) block).drain(world, pos, FluidAction.EXECUTE);
        }

        if (block instanceof BucketPickup) {
            Fluid fluid = state.getFluidState().getType();
            ItemStack pickedUp = ((BucketPickup) block).pickupBlock(world, pos, state);
            if (pickedUp.isEmpty()) {
                fluid = Fluids.EMPTY;
            }

            return fluid == Fluids.EMPTY ? null : new FluidStack(fluid, CAPACITY);
        }

        return null;
    }

    private InteractionResultHolder<ItemStack> finishPickup(Player player, ItemStack stack, FluidStack drained) {
        ItemStack filled = stack.copy();
        filled.setCount(1);
        filled.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).ifPresent(handler -> handler.fill(drained, FluidAction.EXECUTE));

        if (stack.getCount() > 1) {
            ItemStack remaining = stack.copy();
            remaining.shrink(1);

            if (!player.getInventory().add(filled)) {
                player.drop(filled, false);
            }

            return new InteractionResultHolder<>(InteractionResult.SUCCESS, remaining);
        }

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, filled);
    }

    private static FluidStack getContainedFluid(ItemStack stack) {
        // Forge doesn't register its own built-in fluid-handler capability
        // until FMLCommonSetupEvent, but the client already needs every
        // registered item's getDisplayName() (this method's only caller)
        // for its search tree well before that, during Minecraft.init() -
        // confirmed via a real crash log, not assumed: FLUID_HANDLER_ITEM_CAPABILITY
        // is still null at that point, and calling getCapability at all
        // NPEs inside Forge's own code before this method is even entered.
        // Guard against it rather than the capability itself.
        if (ForgeCapabilities.FLUID_HANDLER_ITEM == null) {
            return null;
        }

        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM, null).map(handler -> handler.drain(CAPACITY, FluidAction.SIMULATE)).filter(fluid -> !fluid.isEmpty()).orElse(null);
    }
}
