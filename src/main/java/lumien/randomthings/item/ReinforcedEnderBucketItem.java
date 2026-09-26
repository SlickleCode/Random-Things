package lumien.randomthings.item;

import net.minecraft.block.Block;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

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
public class ReinforcedEnderBucketItem extends Item {
    private static final int CAPACITY = 1000 * 10;

    public ReinforcedEnderBucketItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new FluidHandlerItemStack(stack, CAPACITY);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        FluidStack contained = getContainedFluid(stack);
        float filledPercent = contained == null ? 0F : (float) contained.getAmount() / CAPACITY;

        return 1.0 - filledPercent;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        FluidStack contained = getContainedFluid(stack);

        if (contained != null) {
            return contained.getFluid().getAttributes().getColor(contained);
        }

        return super.getRGBDurabilityForDisplay(stack);
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        FluidStack contained = getContainedFluid(stack);

        if (contained == null) {
            return super.getDisplayName(stack);
        }

        return new TranslationTextComponent(this.getTranslationKey(stack) + ".filled", contained.getFluid().getAttributes().getDisplayName(contained));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);

        RayTraceResult result = rayTrace(world, player, RayTraceContext.FluidMode.ANY);

        if (result == null || result.getType() != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(ActionResultType.PASS, stack);
        }

        BlockRayTraceResult blockResult = (BlockRayTraceResult) result;
        Block hitBlock = world.getBlockState(blockResult.getPos()).getBlock();

        if (isFluidBlock(hitBlock)) {
            return pickUp(world, player, stack);
        }

        FluidStack contained = getContainedFluid(stack);

        if (contained != null && contained.getAmount() >= 1000) {
            return place(world, player, hand, stack, blockResult, contained);
        }

        return new ActionResult<>(ActionResultType.FAIL, stack);
    }

    private ActionResult<ItemStack> place(World world, PlayerEntity player, Hand hand, ItemStack stack, BlockRayTraceResult blockResult, FluidStack contained) {
        BlockPos clickPos = blockResult.getPos();

        if (!world.isBlockModifiable(player, clickPos)) {
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }

        BlockPos targetPos = clickPos.offset(blockResult.getFace());

        if (!player.canPlayerEdit(targetPos, blockResult.getFace(), stack)) {
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }

        FluidActionResult placeResult = FluidUtil.tryPlaceFluid(player, world, hand, targetPos, stack, contained);

        if (placeResult.isSuccess()) {
            return new ActionResult<>(ActionResultType.SUCCESS, placeResult.getResult());
        }

        return new ActionResult<>(ActionResultType.FAIL, stack);
    }

    private ActionResult<ItemStack> pickUp(World world, PlayerEntity player, ItemStack stackIn) {
        boolean collectAll = player.isSneaking();
        ItemStack stack = stackIn;

        RayTraceResult result = rayTrace(world, player, RayTraceContext.FluidMode.ANY);
        BlockPos startPos = ((BlockRayTraceResult) result).getPos();

        List<BlockPos> toCheck = new ArrayList<>();
        Set<BlockPos> checked = new HashSet<>();
        toCheck.add(startPos);

        while (!toCheck.isEmpty()) {
            BlockPos next = toCheck.remove(0);
            checked.add(next);

            if (checked.size() > 2000) {
                break;
            }

            if (!world.isBlockLoaded(next)) {
                continue;
            }

            Block block = world.getBlockState(next).getBlock();

            if (!isFluidBlock(block)) {
                continue;
            }

            FluidActionResult pickupResult = FluidUtil.tryPickUpFluid(stack, player, world, next, Direction.UP);

            if (pickupResult.isSuccess()) {
                stack = pickupResult.getResult();

                if (!collectAll) {
                    return new ActionResult<>(ActionResultType.SUCCESS, stack);
                }
            }

            for (Direction facing : Direction.values()) {
                BlockPos adding = next.offset(facing);

                if (checked.add(adding)) {
                    toCheck.add(adding);
                }
            }
        }

        return new ActionResult<>(collectAll && stack != stackIn ? ActionResultType.SUCCESS : ActionResultType.PASS, stack);
    }

    private static boolean isFluidBlock(Block block) {
        return block instanceof IBucketPickupHandler || block instanceof IFluidBlock;
    }

    private static FluidStack getContainedFluid(ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).map(handler -> handler.drain(CAPACITY, FluidAction.SIMULATE)).filter(fluid -> !fluid.isEmpty()).orElse(null);
    }
}
