package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.handler.floo.FlooNetworkHandler;
import lumien.randomthings.tileentity.FlooBrickTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Right-click a vanilla Brick block with this (its own display name, if any,
 * becomes the fireplace's destination name) to convert the contiguous group
 * of bricks it's touching - scanned horizontally only, up to 20 blocks - into
 * a fresh {@link lumien.randomthings.block.FlooBrickBlock} fireplace. Direct
 * port of 1.12.2's {@code ItemFlooSign}.
 */
public class FlooSignItem extends Item {
    private static final int MAX_BRICKS = 20;

    public FlooSignItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World worldIn = context.getWorld();
        BlockPos pos = context.getPos();
        BlockState state = worldIn.getBlockState(pos);

        if (state.getBlock() != Blocks.BRICKS) {
            return ActionResultType.PASS;
        }

        if (worldIn.isRemote) {
            return ActionResultType.SUCCESS;
        }

        PlayerEntity player = context.getPlayer();
        ItemStack flooSign = context.getItem();

        String signName = flooSign.hasDisplayName() ? flooSign.getDisplayName().getString() : null;

        Direction teleportFacing = context.getFace();

        if (!teleportFacing.getAxis().isHorizontal()) {
            teleportFacing = player != null ? player.getHorizontalFacing().getOpposite() : Direction.NORTH;
        }

        List<BlockPos> brickList = new ArrayList<>();
        List<BlockPos> checkStack = new ArrayList<>();
        checkStack.add(pos);

        HashSet<BlockPos> checkedPositions = new HashSet<>();

        while (!checkStack.isEmpty()) {
            BlockPos take = checkStack.remove(checkStack.size() - 1);

            if (checkedPositions.contains(take)) {
                continue;
            }

            checkedPositions.add(take);

            BlockState takeState = worldIn.getBlockState(take);

            if (takeState.getBlock() == Blocks.BRICKS) {
                if (brickList.size() > MAX_BRICKS) {
                    return ActionResultType.FAIL;
                }

                brickList.add(take);

                for (Direction offsetFacing : Direction.Plane.HORIZONTAL) {
                    checkStack.add(take.offset(offsetFacing));
                }
            } else if (takeState.getBlock() == ModBlocks.FLOO_BRICK) {
                return ActionResultType.FAIL;
            }
        }

        brickList.remove(pos);

        UUID uuid = UUID.randomUUID();

        boolean valid = FlooNetworkHandler.get(worldIn).createFireplace(uuid, signName, player, pos, brickList);

        if (!valid) {
            return ActionResultType.FAIL;
        }

        worldIn.setBlockState(pos, ModBlocks.FLOO_BRICK.getDefaultState());
        TileEntity masterTe = worldIn.getTileEntity(pos);

        if (masterTe instanceof FlooBrickTileEntity) {
            ((FlooBrickTileEntity) masterTe).initToMaster(brickList, teleportFacing, uuid);
        }

        for (BlockPos brickPos : brickList) {
            worldIn.setBlockState(brickPos, ModBlocks.FLOO_BRICK.getDefaultState());
            TileEntity te = worldIn.getTileEntity(brickPos);

            if (te instanceof FlooBrickTileEntity) {
                ((FlooBrickTileEntity) te).initToChild(uuid);
            }
        }

        if (player != null && !player.abilities.isCreativeMode) {
            flooSign.shrink(1);
        }

        return ActionResultType.SUCCESS;
    }
}
