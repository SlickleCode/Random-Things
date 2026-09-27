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

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
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
