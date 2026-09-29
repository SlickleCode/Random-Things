package lumien.randomthings.block;

import lumien.randomthings.handler.ModDimensions;
import lumien.randomthings.handler.spectre.SpectreCube;
import lumien.randomthings.handler.spectre.SpectreHandler;
import lumien.randomthings.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

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
    public enum CoreOrientation implements IStringSerializable {
        NW, NE, ES, SW;

        @Override
        public String getName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public static final EnumProperty<CoreOrientation> ORIENTATION = EnumProperty.create("orientation", CoreOrientation.class);

    public SpectreCoreBlock() {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(-1.0F, 3600000.0F).sound(net.minecraft.block.SoundType.GLASS).noDrops());

        this.setDefaultState(this.stateContainer.getBaseState().with(ORIENTATION, CoreOrientation.NW));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION);
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, net.minecraft.util.math.BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
        if (worldIn.getDimension().getType() != ModDimensions.SPECTRE_TYPE) {
            return false;
        }

        ItemStack holding = playerIn.getHeldItem(hand);

        if (!holding.isEmpty() && holding.getItem() == ModItems.ECTOPLASM) {
            if (!worldIn.isRemote) {
                SpectreCube cube = SpectreHandler.getInstance(((ServerPlayerEntity) playerIn).getServer()).getSpectreCubeFromPos(worldIn, pos.up());

                if (cube != null) {
                    holding.shrink(cube.increaseHeight(holding.getCount(), worldIn));
                }
            }

            return true;
        } else if (holding.isEmpty()) {
            if (!worldIn.isRemote) {
                SpectreHandler.getInstance(((ServerPlayerEntity) playerIn).getServer()).teleportPlayerBack((ServerPlayerEntity) playerIn);
            }

            return true;
        }

        return false;
    }
}
