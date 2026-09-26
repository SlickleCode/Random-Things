package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.RuneBaseTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

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
    public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, net.minecraft.client.util.ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        DyeColor[][] pattern = getPattern(stack);

        if (pattern == null) {
            tooltip.add(new TranslationTextComponent("tooltip.randomthings.general.empty"));
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
            tooltip.add(new StringTextComponent("- " + amounts[color.getId()] + "x " + color.getName()));
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand hand) {
        if (playerIn.isSneaking()) {
            return new ActionResult<>(ActionResultType.SUCCESS, new ItemStack(Items.PAPER));
        }

        return new ActionResult<>(ActionResultType.PASS, playerIn.getHeldItem(hand));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World worldIn = context.getWorld();
        BlockPos pos = context.getPos();
        BlockState state = worldIn.getBlockState(pos);

        if (state.getBlock() == ModBlocks.RUNE_BASE) {
            if (!worldIn.isRemote) {
                TileEntity te = worldIn.getTileEntity(pos);

                if (te instanceof RuneBaseTileEntity) {
                    setPattern(context.getItem(), ((RuneBaseTileEntity) te).getRuneData());
                }
            }

            return ActionResultType.SUCCESS;
        }

        if (context.getFace() != Direction.UP || !worldIn.isAirBlock(pos.up()) || !Block.hasSolidSide(state, worldIn, pos, Direction.UP)) {
            return ActionResultType.FAIL;
        }

        DyeColor[][] pattern = getPattern(context.getItem());

        if (pattern == null) {
            return ActionResultType.FAIL;
        }

        PlayerEntity player = context.getPlayer();
        boolean creative = player != null && player.abilities.isCreativeMode;

        DyeColor[][] actual = new DyeColor[4][4];
        boolean anything = creative;

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                DyeColor color = pattern[x][y];

                if (color != null && !creative) {
                    if (consumeMatchingDust(player, color, worldIn.isRemote)) {
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
            return ActionResultType.FAIL;
        }

        if (!worldIn.isRemote) {
            worldIn.setBlockState(pos.up(), ModBlocks.RUNE_BASE.getDefaultState());

            TileEntity te = worldIn.getTileEntity(pos.up());

            if (te instanceof RuneBaseTileEntity) {
                ((RuneBaseTileEntity) te).setRuneData(actual);
            }
        }

        return ActionResultType.SUCCESS;
    }

    private boolean consumeMatchingDust(PlayerEntity player, DyeColor color, boolean clientSide) {
        if (player == null) {
            return false;
        }

        Item dustItem = ModItems.RUNE_DUST.get(color);

        for (int s = 0; s < player.inventory.getSizeInventory(); s++) {
            ItemStack stack = player.inventory.getStackInSlot(s);

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
        CompoundNBT compound = stack.getTag();

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
