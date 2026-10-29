package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.handler.floo.FlooNetworkHandler;
import lumien.randomthings.tileentity.FlooBrickTileEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

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
    public InteractionResult useOn(UseOnContext context) {
        Level worldIn = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = worldIn.getBlockState(pos);

        if (state.getBlock() != Blocks.BRICKS) {
            return InteractionResult.PASS;
        }

        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        Player player = context.getPlayer();
        ItemStack flooSign = context.getItemInHand();

        String signName = flooSign.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME) ? flooSign.getHoverName().getString() : null;

        Direction teleportFacing = context.getClickedFace();

        if (!teleportFacing.getAxis().isHorizontal()) {
            teleportFacing = player != null ? player.getDirection().getOpposite() : Direction.NORTH;
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
                    return InteractionResult.FAIL;
                }

                brickList.add(take);

                for (Direction offsetFacing : Direction.Plane.HORIZONTAL) {
                    checkStack.add(take.relative(offsetFacing));
                }
            } else if (takeState.getBlock() == ModBlocks.FLOO_BRICK) {
                return InteractionResult.FAIL;
            }
        }

        brickList.remove(pos);

        UUID uuid = UUID.randomUUID();

        boolean valid = FlooNetworkHandler.get(worldIn).createFireplace(uuid, signName, player, pos, brickList);

        if (!valid) {
            if (player != null && FlooNetworkHandler.get(worldIn).hasFireplaceNamed(signName)) {
                player.displayClientMessage(Component.translatable("floo.info.duplicate_name", signName), true);
            }

            return InteractionResult.FAIL;
        }

        worldIn.setBlockAndUpdate(pos, ModBlocks.FLOO_BRICK.defaultBlockState());
        BlockEntity masterTe = worldIn.getBlockEntity(pos);

        if (masterTe instanceof FlooBrickTileEntity) {
            ((FlooBrickTileEntity) masterTe).initToMaster(brickList, teleportFacing, uuid);
        }

        for (BlockPos brickPos : brickList) {
            worldIn.setBlockAndUpdate(brickPos, ModBlocks.FLOO_BRICK.defaultBlockState());
            BlockEntity te = worldIn.getBlockEntity(brickPos);

            if (te instanceof FlooBrickTileEntity) {
                ((FlooBrickTileEntity) te).initToChild(uuid);
            }
        }

        if (player != null && !player.getAbilities().instabuild) {
            flooSign.shrink(1);
        }

        return InteractionResult.SUCCESS;
    }
}
