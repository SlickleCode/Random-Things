package lumien.randomthings.item;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.lib.IRTItemColor;
import lumien.randomthings.tileentity.RuneBaseTileEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

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
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (world.isClientSide() || context.getClickedFace() != Direction.UP) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        BlockState targetState = world.getBlockState(pos);

        RuneBaseTileEntity te;

        if (targetState.getBlock() == ModBlocks.RUNE_BASE) {
            te = (RuneBaseTileEntity) world.getBlockEntity(pos);
        } else {
            te = null;

            if (net.minecraft.world.level.block.Block.canSupportCenter(world, pos, Direction.UP)) {
                BlockPos abovePos = pos.above();
                BlockState toReplace = world.getBlockState(abovePos);

                if (toReplace.isAir() || toReplace.canBeReplaced()) {
                    world.setBlockAndUpdate(abovePos, ModBlocks.RUNE_BASE.defaultBlockState());

                    BlockEntity placedTe = world.getBlockEntity(abovePos);

                    if (placedTe instanceof RuneBaseTileEntity) {
                        te = (RuneBaseTileEntity) placedTe;
                        pos = abovePos;
                    }
                }
            }
        }

        if (te == null) {
            return InteractionResult.FAIL;
        }

        Vec3 hitVec = context.getClickLocation();
        int x = (int) Math.floor((hitVec.x - pos.getX()) * 4);
        int y = (int) Math.floor((hitVec.z - pos.getZ()) * 4);
        x = net.minecraft.util.Mth.clamp(x, 0, 3);
        y = net.minecraft.util.Mth.clamp(y, 0, 3);

        if (te.getRuneData()[x][y] == null) {
            te.getRuneData()[x][y] = color;
            te.syncTE();

            if (player != null && !player.getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public int getColorFromItemstack(ItemStack stack, int tintIndex) {
        return lumien.randomthings.util.DyeUtil.rgb(color);
    }
}
