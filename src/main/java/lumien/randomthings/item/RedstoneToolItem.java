package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.RedstoneObserverTileEntity;
import lumien.randomthings.tileentity.redstoneinterface.BasicRedstoneInterfaceTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public boolean hasEffect(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return tag != null && tag.getBoolean("linking");
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        ItemStack stack = context.getItem();
        BlockState state = world.getBlockState(pos);

        if (!stack.hasTag()) {
            if (!isLinkStart(state.getBlock())) {
                return ActionResultType.FAIL;
            }

            stack.setTag(new CompoundNBT());
        }

        CompoundNBT tag = stack.getTag();
        boolean linking = tag.getBoolean("linking");

        if (linking) {
            BlockPos linkingFrom = new BlockPos(tag.getInt("oX"), tag.getInt("oY"), tag.getInt("oZ"));

            if (!linkingFrom.equals(pos)) {
                BlockState linkingState = world.getBlockState(linkingFrom);
                TileEntity te = world.getTileEntity(linkingFrom);

                if (linkingState.getBlock() == ModBlocks.REDSTONE_OBSERVER && te instanceof RedstoneObserverTileEntity) {
                    ((RedstoneObserverTileEntity) te).setTarget(pos);
                } else if (linkingState.getBlock() == ModBlocks.BASIC_REDSTONE_INTERFACE && te instanceof BasicRedstoneInterfaceTileEntity) {
                    ((BasicRedstoneInterfaceTileEntity) te).setTarget(pos);
                }
            }

            tag.putBoolean("linking", false);
            return ActionResultType.SUCCESS;
        } else if (isLinkStart(state.getBlock())) {
            tag.putBoolean("linking", true);
            tag.putInt("oX", pos.getX());
            tag.putInt("oY", pos.getY());
            tag.putInt("oZ", pos.getZ());
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.FAIL;
    }

    private static boolean isLinkStart(net.minecraft.block.Block block) {
        return block == ModBlocks.REDSTONE_OBSERVER || block == ModBlocks.BASIC_REDSTONE_INTERFACE;
    }
}
