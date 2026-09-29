package lumien.randomthings.block;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import lumien.randomthings.tileentity.SpectreCoilTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;

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
public class SpectreCoilBlock extends Block {
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

    public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

    private final CoilType coilType;

    public SpectreCoilBlock(CoilType coilType) {
        super(Block.Properties.create(Material.ROCK).hardnessAndResistance(0.3F).doesNotBlockMovement());

        this.coilType = coilType;
        this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.UP));
    }

    public CoilType getCoilType() {
        return coilType;
    }

    @Override
    protected void fillStateContainer(Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SpectreCoilTileEntity();
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getFace());
    }

    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        return hasEnergyCapableSupport(worldIn, pos, state.get(FACING));
    }

    private static boolean hasEnergyCapableSupport(IWorldReader world, BlockPos pos, Direction facing) {
        TileEntity supportTe = world.getTileEntity(pos.offset(facing.getOpposite()));
        return supportTe != null && supportTe.getCapability(CapabilityEnergy.ENERGY, facing).isPresent();
    }

    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (!worldIn.isRemote && !hasEnergyCapableSupport(worldIn, pos, state.get(FACING))) {
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

                if (te instanceof SpectreCoilTileEntity) {
                    ((SpectreCoilTileEntity) te).setOwner(id);
                }
            }
        }
    }
}
