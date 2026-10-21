package lumien.randomthings.block;

import lumien.randomthings.util.StackTag;
import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.FluidDisplayTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

/**
 * A glass-like block showing whatever fluid was last poured into it with a
 * filled fluid container (right-click). Sneak-right-click cycles a 4-step
 * display rotation; a plain right-click toggles between the still/flowing
 * texture. Direct port of 1.12.2's {@code BlockFluidDisplay}; the fluid
 * texture/tint/rotation itself, driven there by {@code ExtendedBlockState} +
 * a custom baked model, moved to {@link
 * lumien.randomthings.client.renderer.FluidDisplayTileEntityRenderer} -
 * this port's established substitute for exactly that kind of per-instance
 * dynamic render data (see {@code RuneBaseBlock}/{@code RuneBaseTileEntityRenderer}).
 */
public class FluidDisplayBlock extends Block implements EntityBlock {
    public FluidDisplayBlock() {
        super(Block.Properties.of().mapColor(net.minecraft.world.level.material.MapColor.NONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.HAT).noOcclusion().sound(SoundType.GLASS).strength(0.3F));
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity te = worldIn.getBlockEntity(pos);
        if (!(te instanceof FluidDisplayTileEntity)) {
            return InteractionResult.PASS;
        }

        FluidDisplayTileEntity display = (FluidDisplayTileEntity) te;
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);

        IFluidHandlerItem handler = heldItem.isEmpty() ? null : heldItem.getCapability(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.ITEM);

        if (handler != null) {
            FluidStack contents = handler.getTanks() > 0 ? handler.getFluidInTank(0) : null;

            if (contents != null && !contents.isEmpty()) {
                display.setFluidStack(new FluidStack(contents.getFluid(), 1000));
                display.syncTE();
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            display.cycleRotation();
        } else {
            display.toggleFlowing();
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * The real rendering happens in {@link lumien.randomthings.client.renderer.FluidDisplayTileEntityRenderer}
     * (same reasoning as {@code RuneBaseBlock}). The blockstate/model JSON that does exist draws nothing
     * (empty elements) - it's only there so {@code ModelBakery} doesn't log a spurious "missing model"
     * warning, and so breaking this block shows a themed particle effect instead of the default one.
     */
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /**
     * 1.16.5: the equivalent of the 1.14.4 {@code getRenderLayer() == CUTOUT} fix below is {@code Block.Properties.notSolid()} in the constructor -
     * 1.16 culls a neighbour's face against any block whose {@code BlockState.isSolid()} is true (the property's default) regardless of the render layer
     * registered in {@code RenderLayerSetup}, so every block that had a non-SOLID layer in 1.14.4 now sets {@code notSolid()}.
     * <p>
     * Real bug, found 2026-09-28: a neighboring block (e.g. sand) was culling its
     * own face against this block entirely, showing a hole through to whatever's
     * behind instead of the neighbor's texture. Ground-truthed {@code Block
     * #shouldSideBeRendered}/{@code #isSolid}: in this version, face culling
     * against a neighbor is gated on that neighbor's {@code isSolid()}, which is
     * {@code blocksMovement && getRenderLayer() == SOLID} - and {@code
     * getRenderLayer()} defaults to {@code SOLID} unless overridden, regardless of
     * this block's own {@code BlockRenderType.INVISIBLE}/TESR-only rendering (a
     * separate, unrelated method). Vanilla's own {@code GlassBlock} avoids exactly
     * this by overriding {@code getRenderLayer()} to {@code CUTOUT}; 1.12.2 never
     * needed an equivalent override since its culling was keyed off {@code
     * Material#isOpaque()} instead, not render layer - this is a real 1.14.4 API
     * coupling with no 1.12.2 counterpart, not a disclosed simplification.
     */
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new FluidDisplayTileEntity(pos, state);
    }
}
