package lumien.randomthings.block;

import lumien.randomthings.handler.ModDimensions;
import lumien.randomthings.handler.spectre.SpectreCube;
import lumien.randomthings.handler.spectre.SpectreHandler;
import lumien.randomthings.item.ModItems;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;

/**
 * Direct port of 1.12.2's {@code BlockSpectreCore}: the 2x2 unbreakable
 * pedestal at the center of every {@link SpectreCube} room. Right-click while
 * holding Ectoplasm to raise the room's ceiling (consuming the whole held
 * stack, up to however much height is left before Y 255); right-click
 * empty-handed to teleport back where you came from - same interactions as
 * 1.12.2, minus its {@code Baubles}-only "Spectre Anchor in a bauble slot"
 * checks (this port dropped Baubles entirely, matching every other
 * already-ported feature).
 * <p>
 * The {@code orientation} property (which of the 2x2 pedestal's 4 corners
 * this particular block is) is set directly by {@link SpectreCube#generate}
 * at placement time rather than detected dynamically from neighbors the way
 * 1.12.2's {@code getActualState} did - see that method's javadoc.
 */
public class SpectreCoreBlock extends Block {
    public enum CoreOrientation implements StringRepresentable {
        NW, NE, ES, SW;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public static final EnumProperty<CoreOrientation> ORIENTATION = EnumProperty.create("orientation", CoreOrientation.class);

    public SpectreCoreBlock() {
        super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).noOcclusion().strength(-1.0F, 3600000.0F).sound(net.minecraft.world.level.block.SoundType.GLASS).noLootTable());

        this.registerDefaultState(this.stateDefinition.any().setValue(ORIENTATION, CoreOrientation.NW));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION);
    }

    @Override
    public net.minecraft.world.InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level worldIn, net.minecraft.core.BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = legacyUse(state, worldIn, pos, playerIn, hand, hit);
        if (result == InteractionResult.SUCCESS) {
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        if (result == InteractionResult.CONSUME) {
            return net.minecraft.world.InteractionResult.CONSUME;
        }
        if (result == InteractionResult.FAIL) {
            return net.minecraft.world.InteractionResult.FAIL;
        }
        return net.minecraft.world.InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    private InteractionResult legacyUse(BlockState state, Level worldIn, net.minecraft.core.BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
        if (!lumien.randomthings.handler.ModDimensions.isSpectre(worldIn)) {
            return InteractionResult.PASS;
        }

        ItemStack holding = playerIn.getItemInHand(hand);

        if (!holding.isEmpty() && holding.getItem() == ModItems.ECTOPLASM) {
            if (!worldIn.isClientSide) {
                SpectreCube cube = SpectreHandler.getInstance(((ServerPlayer) playerIn).getServer()).getSpectreCubeFromPos(worldIn, pos.above());

                if (cube != null) {
                    holding.shrink(cube.increaseHeight(holding.getCount(), worldIn));
                }
            }

            return InteractionResult.SUCCESS;
        } else if (holding.isEmpty()) {
            if (!worldIn.isClientSide) {
                SpectreHandler.getInstance(((ServerPlayer) playerIn).getServer()).teleportPlayerBack((ServerPlayer) playerIn);
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
