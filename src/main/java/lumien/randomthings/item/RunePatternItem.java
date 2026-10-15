package lumien.randomthings.item;

import net.minecraft.world.item.Items;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.RuneBaseTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Right-click a Rune Base to copy its 4x4 pattern onto this (a "stencil");
 * right-click empty space on top of a solid block to stamp that saved
 * pattern down as a brand new Rune Base, consuming matching
 * {@link RuneDustItem}s from the placer's own inventory for each colored
 * cell (creative mode skips this). Sneak-right-click turns it back into
 * paper. Direct port of 1.12.2's {@code ItemRunePattern}.
 */
public class RunePatternItem extends Item {
    public RunePatternItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        DyeColor[][] pattern = getPattern(stack);

        if (pattern == null) {
            tooltip.add(Component.translatable("tooltip.randomthings.general.empty"));
            return;
        }

        int[] amounts = new int[DyeColor.values().length];

        for (DyeColor[] row : pattern) {
            for (DyeColor color : row) {
                if (color != null) {
                    amounts[color.getId()]++;
                }
            }
        }

        List<DyeColor> present = new ArrayList<>();

        for (DyeColor color : DyeColor.values()) {
            if (amounts[color.getId()] > 0) {
                present.add(color);
            }
        }

        present.sort(Comparator.comparingInt((DyeColor c) -> amounts[c.getId()]).reversed());

        for (DyeColor color : present) {
            tooltip.add(Component.literal("- " + amounts[color.getId()] + "x " + color.getName()));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand hand) {
        if (playerIn.isShiftKeyDown()) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, new ItemStack(Items.PAPER));
        }

        return new InteractionResultHolder<>(InteractionResult.PASS, playerIn.getItemInHand(hand));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level worldIn = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = worldIn.getBlockState(pos);

        if (state.getBlock() == ModBlocks.RUNE_BASE) {
            if (!worldIn.isClientSide) {
                BlockEntity te = worldIn.getBlockEntity(pos);

                if (te instanceof RuneBaseTileEntity) {
                    setPattern(context.getItemInHand(), ((RuneBaseTileEntity) te).getRuneData());
                }
            }

            return InteractionResult.SUCCESS;
        }

        if (context.getClickedFace() != Direction.UP || !worldIn.isEmptyBlock(pos.above()) || !Block.canSupportCenter(worldIn, pos, Direction.UP)) {
            return InteractionResult.FAIL;
        }

        DyeColor[][] pattern = getPattern(context.getItemInHand());

        if (pattern == null) {
            return InteractionResult.FAIL;
        }

        Player player = context.getPlayer();
        boolean creative = player != null && player.getAbilities().instabuild;

        DyeColor[][] actual = new DyeColor[4][4];
        boolean anything = creative;

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                DyeColor color = pattern[x][y];

                if (color != null && !creative) {
                    if (consumeMatchingDust(player, color, worldIn.isClientSide)) {
                        anything = true;
                    } else {
                        color = null;
                    }
                } else if (color != null) {
                    anything = true;
                }

                actual[x][y] = color;
            }
        }

        if (!anything) {
            return InteractionResult.FAIL;
        }

        if (!worldIn.isClientSide) {
            worldIn.setBlockAndUpdate(pos.above(), ModBlocks.RUNE_BASE.defaultBlockState());

            BlockEntity te = worldIn.getBlockEntity(pos.above());

            if (te instanceof RuneBaseTileEntity) {
                ((RuneBaseTileEntity) te).setRuneData(actual);
            }
        }

        return InteractionResult.SUCCESS;
    }

    private boolean consumeMatchingDust(Player player, DyeColor color, boolean clientSide) {
        if (player == null) {
            return false;
        }

        Item dustItem = RuneDustItems.BY_COLOR.get(color);

        for (int s = 0; s < player.getInventory().getContainerSize(); s++) {
            ItemStack stack = player.getInventory().getItem(s);

            if (!stack.isEmpty() && stack.getItem() == dustItem) {
                if (!clientSide) {
                    stack.shrink(1);
                }

                return true;
            }
        }

        return false;
    }

    public static DyeColor[][] getPattern(ItemStack stack) {
        CompoundTag compound = stack.getTag();

        if (compound == null || !compound.contains("runeData")) {
            return null;
        }

        byte[] flat = compound.getByteArray("runeData");
        DyeColor[][] pattern = new DyeColor[4][4];

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                byte id = flat[x * 4 + y];
                pattern[x][y] = id >= 0 ? DyeColor.byId(id) : null;
            }
        }

        return pattern;
    }

    public static void setPattern(ItemStack stack, DyeColor[][] pattern) {
        byte[] flat = new byte[16];

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                flat[x * 4 + y] = (byte) (pattern[x][y] != null ? pattern[x][y].getId() : -1);
            }
        }

        stack.getOrCreateTag().putByteArray("runeData", flat);
    }
}
