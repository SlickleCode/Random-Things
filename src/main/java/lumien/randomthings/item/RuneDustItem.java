package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.lib.IRTItemColor;
import lumien.randomthings.tileentity.RuneBaseTileEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Right-click the top of a solid block (or an existing Rune Base) to place a
 * colored pixel into its 4x4 grid, auto-placing a new Rune Base if needed.
 * One instance per {@link DyeColor}, matching this port's established
 * "each former metadata sub-item is its own registered Item" convention
 * (unlike most of those, this one still needs a runtime tint - the same
 * grayscale texture is reused for all 16, tinted per color via
 * {@link IRTItemColor}, since a rune's *color*, not its icon, is the whole
 * point). Direct port of 1.12.2's {@code ItemRuneDust}.
 */
public class RuneDustItem extends Item implements IRTItemColor {
    private final DyeColor color;

    public RuneDustItem(Item.Properties properties, DyeColor color) {
        super(properties);

        this.color = color;
    }

    public DyeColor getDyeColor() {
        return color;
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();

        if (world.isRemote || context.getFace() != Direction.UP) {
            return ActionResultType.PASS;
        }

        PlayerEntity player = context.getPlayer();
        BlockState targetState = world.getBlockState(pos);

        RuneBaseTileEntity te;

        if (targetState.getBlock() == ModBlocks.RUNE_BASE) {
            te = (RuneBaseTileEntity) world.getTileEntity(pos);
        } else {
            te = null;

            if (net.minecraft.block.Block.hasSolidSide(targetState, world, pos, Direction.UP)) {
                BlockPos abovePos = pos.up();
                BlockState toReplace = world.getBlockState(abovePos);

                if (toReplace.isAir() || toReplace.getMaterial().isReplaceable()) {
                    world.setBlockState(abovePos, ModBlocks.RUNE_BASE.getDefaultState());

                    TileEntity placedTe = world.getTileEntity(abovePos);

                    if (placedTe instanceof RuneBaseTileEntity) {
                        te = (RuneBaseTileEntity) placedTe;
                        pos = abovePos;
                    }
                }
            }
        }

        if (te == null) {
            return ActionResultType.FAIL;
        }

        Vec3d hitVec = context.getHitVec();
        int x = (int) Math.floor((hitVec.x - pos.getX()) * 4);
        int y = (int) Math.floor((hitVec.z - pos.getZ()) * 4);
        x = net.minecraft.util.math.MathHelper.clamp(x, 0, 3);
        y = net.minecraft.util.math.MathHelper.clamp(y, 0, 3);

        if (te.getRuneData()[x][y] == null) {
            te.getRuneData()[x][y] = color;
            te.syncTE();

            if (player != null && !player.abilities.isCreativeMode) {
                context.getItem().shrink(1);
            }

            return ActionResultType.SUCCESS;
        }

        return ActionResultType.FAIL;
    }

    @Override
    public int getColorFromItemstack(ItemStack stack, int tintIndex) {
        return color.getColorValue();
    }
}
