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
    public int getItemStackLimit(ItemStack stack) {
        return getContainedFluid(stack) != null ? 1 : 16;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt) {
        return new FluidHandlerItemStackSimple(stack, CAPACITY);
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
        FluidStack contained = getContainedFluid(stack);

        if (contained != null && contained.getAmount() >= CAPACITY) {
            return place(world, player, hand, stack, contained);
        }

        return pickUp(world, player, stack);
    }

    private ActionResult<ItemStack> place(World world, PlayerEntity player, Hand hand, ItemStack stack, FluidStack contained) {
        RayTraceResult result = rayTrace(world, player, RayTraceContext.FluidMode.NONE);

        if (result == null || result.getType() != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(ActionResultType.PASS, stack);
        }

        BlockRayTraceResult blockResult = (BlockRayTraceResult) result;
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

    private ActionResult<ItemStack> pickUp(World world, PlayerEntity player, ItemStack stack) {
        RayTraceResult result = rayTrace(world, player, RayTraceContext.FluidMode.ANY);

        if (result == null || result.getType() != RayTraceResult.Type.BLOCK) {
            return new ActionResult<>(ActionResultType.PASS, stack);
        }

        BlockPos hitPos = ((BlockRayTraceResult) result).getPos();

        if (!isFluidBlock(world.getBlockState(hitPos).getBlock())) {
            return new ActionResult<>(ActionResultType.PASS, stack);
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

            if (!world.isBlockLoaded(next)) {
                continue;
            }

            Block block = world.getBlockState(next).getBlock();

            if (!isFluidBlock(block)) {
                continue;
            }

            FluidActionResult pickupResult = FluidUtil.tryPickUpFluid(stack, player, world, next, Direction.UP);

            if (pickupResult.isSuccess()) {
                return new ActionResult<>(ActionResultType.SUCCESS, pickupResult.getResult());
            }

            for (Direction facing : Direction.values()) {
                BlockPos adding = next.offset(facing);

                if (checked.add(adding)) {
                    toCheck.add(adding);
                }
            }
        }

        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    private static boolean isFluidBlock(Block block) {
        return block instanceof IBucketPickupHandler || block instanceof IFluidBlock;
    }

    private static FluidStack getContainedFluid(ItemStack stack) {
        return stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).map(handler -> handler.drain(CAPACITY, FluidAction.SIMULATE)).filter(fluid -> !fluid.isEmpty()).orElse(null);
    }
}
