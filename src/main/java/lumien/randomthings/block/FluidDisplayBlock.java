package lumien.randomthings.block;

import lumien.randomthings.tileentity.FluidDisplayTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

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
public class FluidDisplayBlock extends Block {
    public FluidDisplayBlock() {
        super(Block.Properties.create(Material.GLASS).sound(SoundType.GLASS).hardnessAndResistance(0.3F));
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            return true;
        }

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof FluidDisplayTileEntity)) {
            return false;
        }

        FluidDisplayTileEntity display = (FluidDisplayTileEntity) te;
        ItemStack heldItem = player.getHeldItem(Hand.MAIN_HAND);

        IFluidHandlerItem handler = heldItem.isEmpty() ? null : heldItem.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).orElse(null);

        if (handler != null) {
            FluidStack contents = handler.getTanks() > 0 ? handler.getFluidInTank(0) : null;

            if (contents != null && !contents.isEmpty()) {
                display.setFluidStack(new FluidStack(contents.getFluid(), 1000, contents.getTag()));
                display.syncTE();
                return true;
            }
            return false;
        }

        if (player.isSneaking()) {
            display.cycleRotation();
        } else {
            display.toggleFlowing();
        }
        return true;
    }

    /**
     * The real rendering happens in {@link lumien.randomthings.client.renderer.FluidDisplayTileEntityRenderer}
     * (same reasoning as {@code RuneBaseBlock}). The blockstate/model JSON that does exist draws nothing
     * (empty elements) - it's only there so {@code ModelBakery} doesn't log a spurious "missing model"
     * warning, and so breaking this block shows a themed particle effect instead of the default one.
     */
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }

    /**
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
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new FluidDisplayTileEntity();
    }
}
