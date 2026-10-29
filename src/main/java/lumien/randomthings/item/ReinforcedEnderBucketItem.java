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
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Same flood-fill pickup as {@link EnderBucketItem}, but holds 10x the
 * fluid (10 buckets' worth) and, if you're sneaking while picking up,
 * keeps draining the whole connected body instead of stopping after the
 * first successful drain (until it either runs out of connected fluid or
 * fills up). Shows its fill level and the contained fluid's own color as a
 * durability bar. Direct port of 1.12.2's {@code ItemReinforcedEnderBucket}.
 * <p>
 * Modernization, not a behavior change: the original hardcoded a color
 * switch for exactly Water (blue) and Lava (orange), falling back to a
 * vanilla default for anything else (including a specific check for a
 * third-party mod's fluid by name). 1.14.4's {@code FluidAttributes} already
 * lets any fluid - vanilla, this port's own, or a third party's - report its
 * own display color generically, so this port asks the fluid directly
 * instead of hardcoding a lookup table that would silently miss anything
 * not in it.
 */
public class ReinforcedEnderBucketItem extends Item implements lumien.randomthings.item.IClientItemProperties, lumien.randomthings.lib.IRTItemColor {
    public static final int CAPACITY = 1000 * 10;

    public ReinforcedEnderBucketItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        FluidStack contained = getContainedFluid(stack);
        float filledPercent = contained == null ? 0F : (float) contained.getAmount() / CAPACITY;

        return Math.round(13.0F * (1.0F - (float) (1.0 - filledPercent)));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        FluidStack contained = getContainedFluid(stack);

        if (contained != null) {
            return net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(contained.getFluid()).getTintColor(contained);
        }

        return super.getBarColor(stack);
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

        HitResult result = getPlayerPOVHitResult(world, player, ClipContext.Fluid.ANY);

        if (result == null || result.getType() != HitResult.Type.BLOCK) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }

        BlockHitResult blockResult = (BlockHitResult) result;
        Block hitBlock = world.getBlockState(blockResult.getBlockPos()).getBlock();

        if (isFluidBlock(hitBlock)) {
            return pickUp(world, player, stack, blockResult.getBlockPos());
        }

        FluidStack contained = getContainedFluid(stack);

        if (contained != null && contained.getAmount() >= 1000) {
            return place(world, player, hand, stack, blockResult, contained);
        }

        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    private InteractionResultHolder<ItemStack> place(Level world, Player player, InteractionHand hand, ItemStack stack, BlockHitResult blockResult, FluidStack contained) {
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

    private InteractionResultHolder<ItemStack> pickUp(Level world, Player player, ItemStack stack, BlockPos startPos) {
        boolean collectAll = player.isShiftKeyDown();
        boolean pickedAny = false;

        List<BlockPos> toCheck = new ArrayList<>();
        Set<BlockPos> checked = new HashSet<>();
        toCheck.add(startPos);

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

            FluidStack alreadyContained = getContainedFluid(stack);

            if (alreadyContained != null && alreadyContained.getAmount() >= CAPACITY) {
                break;
            }

            FluidStack drained = drainSource(world, next, state, block);

            if (drained != null) {
                net.neoforged.neoforge.fluids.capability.IFluidHandlerItem fillHandler = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);

                // drainSource already removed the source block: put it back unless the whole amount fits (other fluid / too little room)
                int fits = fillHandler == null ? 0 : fillHandler.fill(drained, FluidAction.SIMULATE);

                if (fits < drained.getAmount()) {
                    world.setBlock(next, state, 3);
                } else {
                    int filled = fillHandler.fill(drained, FluidAction.EXECUTE);

                    if (filled > 0) {
                        pickedAny = true;

                        if (!collectAll || filled < drained.getAmount()) {
                            break;
                        }
                    }
                }
            }

            for (Direction facing : Direction.values()) {
                BlockPos adding = next.relative(facing);

                if (checked.add(adding)) {
                    toCheck.add(adding);
                }
            }
        }

        return new InteractionResultHolder<>(pickedAny ? InteractionResult.SUCCESS : InteractionResult.PASS, stack);
    }

    private static boolean isFluidBlock(Block block) {
        return block instanceof BucketPickup;
    }

    /**
     * See {@link EnderBucketItem#drainSource} - this Forge build's own {@code FluidUtil.tryPickUpFluid}
     * never actually handles a plain fluid block (ground-truthed via {@code javap -c}), so pickup is done
     * directly against each interface's own self-contained drain method instead.
     */
    private static FluidStack drainSource(Level world, BlockPos pos, BlockState state, Block block) {
        if (block instanceof BucketPickup) {
            Fluid fluid = state.getFluidState().getType();
            ItemStack pickedUp = ((BucketPickup) block).pickupBlock(null, world, pos, state);
            if (pickedUp.isEmpty()) {
                fluid = Fluids.EMPTY;
            }

            return fluid == Fluids.EMPTY ? null : new FluidStack(fluid, 1000);
        }

        return null;
    }

    private static FluidStack getContainedFluid(ItemStack stack) {
        // See EnderBucketItem's identical guard: Forge's fluid-handler
        // capability isn't registered until FMLCommonSetupEvent, but
        // getDisplayName() (this method's caller) runs earlier than that
        // during the client's initial search-tree build - confirmed via a
        // real crash log.
        net.neoforged.neoforge.fluids.capability.IFluidHandlerItem handler = stack.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);
        if (handler == null) {
            return null;
        }
        FluidStack drained = handler.drain(CAPACITY, FluidAction.SIMULATE);
        return drained.isEmpty() ? null : drained;
    }

    @Override
    public void registerClientProperties() {
        // the bucket shows its fluid: models/item/*.json switches to the model with the fluid layer while this is 1
        net.minecraft.client.renderer.item.ItemProperties.register(this, new net.minecraft.resources.ResourceLocation("randomthings", "fluid_filled"), (stack, level, entity, seed) -> lumien.randomthings.client.BucketFluidModel.filled(stack) ? 1.0F : 0.0F);
    }

    @Override
    public int getColorFromItemstack(net.minecraft.world.item.ItemStack stack, int tintIndex) {
        return lumien.randomthings.client.BucketFluidModel.color(stack, tintIndex);
    }
}
