package lumien.randomthings.block;

import lumien.randomthings.tileentity.LightRedirectorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * A full-cube "periscope": whichever faces are left open (right-click a face
 * to toggle it, matching 1.12.2 exactly) show whatever's on the *opposite*
 * side of the block instead of this block's own texture - the actual "light
 * redirecting" trick, entirely a client-side rendering swap with no real
 * effect on block light propagation itself (ground-truthed from 1.12.2's own
 * {@code AsmHandler#renderBlock}/{@code getSwitchedPosition}, reached via a
 * {@code BlockRendererDispatcher.func_175018_a} ASM patch - see {@link
 * lumien.randomthings.asm.AsmHandler#renderBlock} and {@code
 * BlockRendererDispatcherTransformer.js} for this port's equivalent, hooking
 * this version's own {@code BlockRendererDispatcher.renderBlock} the same
 * "early conditional-IRETURN at method entry" way {@code TeleporterTransformer}
 * already does).
 * <p>
 * The per-face open/closed *texture* swap itself (the {@code #disabled}
 * texture) is unrelated to that coremod - 1.12.2 drove it through a Forge-only
 * {@code forge_marker} blockstate shorthand that doesn't exist in this
 * version at all. Real, non-deprecated {@code BlockState} properties (reusing
 * vanilla's own {@link BlockStateProperties#NORTH}/{@code EAST}/{@code SOUTH}/
 * {@code WEST}/{@code UP}/{@code DOWN} - the same 6 {@code BooleanProperty}
 * constants fences/glass panes use) do the same job the modern way: 64 real
 * registered states, kept in sync with {@link LightRedirectorTileEntity}'s
 * own {@code enabledMap} on every toggle via {@link World#setBlockState}, and
 * a 64-entry blockstate JSON (generated, not hand-written) picks the plain-vs-
 * {@code #disabled} texture per face per combination. Disclosed divergence:
 * 1.12.2 overrode {@code shouldSideBeRendered} to always render every face
 * regardless of neighbor occlusion, which reads like a workaround for that
 * old per-property-shorthand system rather than a real gameplay need - this
 * is a normal solid full cube now, so standard face culling against solid
 * neighbors applies (strictly a performance win, no visual difference for a
 * full cube).
 */
public class LightRedirectorBlock extends Block {
    public LightRedirectorBlock() {
        super(Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(2.0F));

        this.setDefaultState(this.getStateContainer().getBaseState()
                .with(BlockStateProperties.NORTH, true)
                .with(BlockStateProperties.EAST, true)
                .with(BlockStateProperties.SOUTH, true)
                .with(BlockStateProperties.WEST, true)
                .with(BlockStateProperties.UP, true)
                .with(BlockStateProperties.DOWN, true));
    }

    public static BooleanProperty propertyFor(Direction facing) {
        switch (facing) {
            case NORTH:
                return BlockStateProperties.NORTH;
            case EAST:
                return BlockStateProperties.EAST;
            case SOUTH:
                return BlockStateProperties.SOUTH;
            case WEST:
                return BlockStateProperties.WEST;
            case UP:
                return BlockStateProperties.UP;
            case DOWN:
            default:
                return BlockStateProperties.DOWN;
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.NORTH, BlockStateProperties.EAST, BlockStateProperties.SOUTH, BlockStateProperties.WEST, BlockStateProperties.UP, BlockStateProperties.DOWN);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            return true;
        }

        TileEntity te = worldIn.getTileEntity(pos);

        if (!(te instanceof LightRedirectorTileEntity)) {
            return false;
        }

        Direction facing = hit.getFace();
        LightRedirectorTileEntity redirector = (LightRedirectorTileEntity) te;
        redirector.toggleSide(facing);

        worldIn.setBlockState(pos, state.with(propertyFor(facing), redirector.isEnabled(facing)), 3);

        return true;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new LightRedirectorTileEntity();
    }
}
