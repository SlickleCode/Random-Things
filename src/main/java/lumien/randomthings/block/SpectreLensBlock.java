package lumien.randomthings.block;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import lumien.randomthings.tileentity.SpectreLensTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

/**
 * Ported from 1.12.2's {@code BlockSpectreLens} - a thin cap that can only be placed directly on top of
 * a vanilla Beacon (see {@link SpectreLensTileEntity}). Unlike this session's earlier Coil/Injector
 * blocks, the original model here is already a single simple thin box (no elaborate Cubik Studio mesh,
 * no now-obsolete Forge multi-layer blockstate trick), so it's ported faithfully rather than simplified
 * - same 1/16-tall shape, same real texture.
 */
public class SpectreLensBlock extends Block {
    private static final VoxelShape SHAPE = Block.makeCuboidShape(0, 0, 0, 16, 1, 16);

    public SpectreLensBlock() {
        super(Block.Properties.create(Material.GLASS).hardnessAndResistance(0.3F).sound(SoundType.GLASS).variableOpacity());
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SpectreLensTileEntity();
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return isOnBeacon(worldIn, pos);
    }

    private static boolean isOnBeacon(IWorldReader world, BlockPos pos) {
        return world.getBlockState(pos.down()).getBlock() == Blocks.BEACON;
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote && !isOnBeacon(worldIn, pos)) {
            worldIn.destroyBlock(pos, true);
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!worldIn.isRemote && placer instanceof PlayerEntity) {
            GameProfile profile = ((PlayerEntity) placer).getGameProfile();

            if (profile != null) {
                UUID id = profile.getId();
                TileEntity te = worldIn.getTileEntity(pos);

                if (te instanceof SpectreLensTileEntity) {
                    ((SpectreLensTileEntity) te).setOwner(id);
                }
            }
        }
    }
}
