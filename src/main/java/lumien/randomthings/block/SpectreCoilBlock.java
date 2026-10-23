package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.ModTileEntityTypes;

import lumien.randomthings.util.Tickers;

import net.minecraft.world.level.block.entity.BlockEntityType;

import net.minecraft.world.level.block.entity.BlockEntityTicker;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import lumien.randomthings.tileentity.SpectreCoilTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

/**
 * Ported from 1.12.2's {@code BlockSpectreCoil} - a small block that attaches to a face of any
 * energy-capable neighbor and taps the placing player's Spectre energy pool into it (see
 * {@link SpectreCoilTileEntity}). One instance per {@link CoilType} tier, matching the original's 5
 * separate registered blocks sharing one class (this port skips {@code NUMBER} - see
 * {@link SpectreCoilTileEntity}'s javadoc).
 *
 * <p>Disclosed simplification: 1.12.2 rendered this as a tiny (0.09375-thick) directional nub with a
 * custom per-face bounding box. This port uses a plain full-cube shape instead, matching the
 * simplification this port's own {@code ContactButtonBlock}/{@code ContactLeverBlock} already
 * established for the same class of "small attached block" - purely cosmetic/collision precision, not
 * a functional change. The real mechanic (must attach to a face that exposes an Energy capability,
 * auto-drops otherwise) is preserved.
 */
public class SpectreCoilBlock extends Block implements EntityBlock {
    public enum CoilType {
        NORMAL("normal", 1024, 0x00FFFF),
        REDSTONE("redstone", 4096, 0xFF0000),
        ENDER("ender", 20480, 0xC800D2),
        GENESIS("genesis", 0, 0xFFA500);

        public final String suffix;
        public final int rate;
        public final int color;

        CoilType(String suffix, int rate, int color) {
            this.suffix = suffix;
            this.rate = rate;
            this.color = color;
        }
    }

    public static final EnumProperty<Direction> FACING = EnumProperty.create("facing", Direction.class, Direction.values());

    private final CoilType coilType;

    public SpectreCoilBlock(CoilType coilType) {
        super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.STONE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASEDRUM).strength(0.3F).noCollission());

        this.coilType = coilType;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public CoilType getCoilType() {
        return coilType;
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new SpectreCoilTileEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return Tickers.of(type, ModTileEntityTypes.SPECTRE_COIL, (l, p, s2, be) -> be.tick());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return hasEnergyCapableSupport(worldIn, pos, state.getValue(FACING));
    }

    private static boolean hasEnergyCapableSupport(LevelReader world, BlockPos pos, Direction facing) {
        BlockEntity supportTe = world.getBlockEntity(pos.relative(facing.getOpposite()));
        return supportTe != null && lumien.randomthings.util.CapUtil.energy(supportTe, facing) != null;
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, net.minecraft.world.level.redstone.Orientation fromPos, boolean isMoving) {
        if (!worldIn.isClientSide && !hasEnergyCapableSupport(worldIn, pos, state.getValue(FACING))) {
            worldIn.destroyBlock(pos, true);
        }
    }

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!worldIn.isClientSide && placer instanceof Player) {
            GameProfile profile = ((Player) placer).getGameProfile();

            if (profile != null) {
                UUID id = profile.getId();
                BlockEntity te = worldIn.getBlockEntity(pos);

                if (te instanceof SpectreCoilTileEntity) {
                    ((SpectreCoilTileEntity) te).setOwner(id);
                }
            }
        }
    }
}
