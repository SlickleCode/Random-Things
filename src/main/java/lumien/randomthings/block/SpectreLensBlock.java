package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import lumien.randomthings.tileentity.SpectreLensTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

/**
 * Ported from 1.12.2's {@code BlockSpectreLens} - a thin cap that can only be placed directly on top of
 * a vanilla Beacon (see {@link SpectreLensTileEntity}). Unlike this session's earlier Coil/Injector
 * blocks, the original model here is already a single simple thin box (no elaborate Cubik Studio mesh,
 * no now-obsolete Forge multi-layer blockstate trick), so it's ported faithfully rather than simplified
 * - same 1/16-tall shape, same real texture.
 */
public class SpectreLensBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 1, 16);

    public SpectreLensBlock() {
        super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.NONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.HAT).strength(0.3F).sound(SoundType.GLASS).dynamicShape());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new SpectreLensTileEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return Tickers.of(type, ModTileEntityTypes.SPECTRE_LENS, (l, p, s2, be) -> be.tick());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return isOnBeacon(worldIn, pos);
    }

    private static boolean isOnBeacon(LevelReader world, BlockPos pos) {
        return world.getBlockState(pos.below()).getBlock() == Blocks.BEACON;
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, net.minecraft.world.level.redstone.Orientation fromPos, boolean isMoving) {
        if (!worldIn.isClientSide() && !isOnBeacon(worldIn, pos)) {
            worldIn.destroyBlock(pos, true);
        }
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!worldIn.isClientSide() && placer instanceof Player) {
            GameProfile profile = ((Player) placer).getGameProfile();

            if (profile != null) {
                UUID id = profile.id();
                BlockEntity te = worldIn.getBlockEntity(pos);

                if (te instanceof SpectreLensTileEntity) {
                    ((SpectreLensTileEntity) te).setOwner(id);
                }
            }
        }
    }
}
