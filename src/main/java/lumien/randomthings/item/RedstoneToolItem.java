package lumien.randomthings.item;

import lumien.randomthings.util.StackTag;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.RedstoneObserverTileEntity;
import lumien.randomthings.tileentity.redstoneinterface.BasicRedstoneInterfaceTileEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * Right-click a Redstone Observer or a Basic Redstone Interface to start
 * linking, then right-click any block to set that as its target. The
 * wireless Redstone Interface case (deferred when this javadoc was first
 * written, pending the coremod {@code RedstoneSignalHandler}/{@code
 * RedstoneInterfaceTileEntity} needed) is now wired up alongside the
 * Redstone Observer one.
 */
public class RedstoneToolItem extends Item {
    public RedstoneToolItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        CompoundTag tag = StackTag.get(stack);
        return tag != null && tag.getBooleanOr("linking", false);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        BlockState state = world.getBlockState(pos);

        if (!StackTag.has(stack) && !isLinkStart(state.getBlock())) {
            return InteractionResult.FAIL;
        }

        CompoundTag tag = StackTag.getOrCreate(stack);
        boolean linking = tag.getBooleanOr("linking", false);

        if (linking) {
            BlockPos linkingFrom = new BlockPos(tag.getIntOr("oX", 0), tag.getIntOr("oY", 0), tag.getIntOr("oZ", 0));

            if (!linkingFrom.equals(pos)) {
                BlockState linkingState = world.getBlockState(linkingFrom);
                BlockEntity te = world.getBlockEntity(linkingFrom);

                if (linkingState.getBlock() == ModBlocks.REDSTONE_OBSERVER && te instanceof RedstoneObserverTileEntity) {
                    ((RedstoneObserverTileEntity) te).setTarget(pos);
                } else if (linkingState.getBlock() == ModBlocks.BASIC_REDSTONE_INTERFACE && te instanceof BasicRedstoneInterfaceTileEntity) {
                    ((BasicRedstoneInterfaceTileEntity) te).setTarget(pos);
                }
            }

            tag.putBoolean("linking", false);
            StackTag.set(stack, tag);
            return InteractionResult.SUCCESS;
        } else if (isLinkStart(state.getBlock())) {
            tag.putBoolean("linking", true);
            tag.putInt("oX", pos.getX());
            tag.putInt("oY", pos.getY());
            tag.putInt("oZ", pos.getZ());
            StackTag.set(stack, tag);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    private static boolean isLinkStart(net.minecraft.world.level.block.Block block) {
        return block == ModBlocks.REDSTONE_OBSERVER || block == ModBlocks.BASIC_REDSTONE_INTERFACE;
    }
}
