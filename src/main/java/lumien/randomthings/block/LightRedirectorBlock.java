package lumien.randomthings.block;

import net.minecraft.world.level.block.EntityBlock;

import lumien.randomthings.tileentity.LightRedirectorTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;

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
public class LightRedirectorBlock extends Block implements EntityBlock {
    public LightRedirectorBlock() {
        super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.WOOD).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.BASS).ignitedByLava().sound(SoundType.WOOD).strength(2.0F));

        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(BlockStateProperties.NORTH, true)
                .setValue(BlockStateProperties.EAST, true)
                .setValue(BlockStateProperties.SOUTH, true)
                .setValue(BlockStateProperties.WEST, true)
                .setValue(BlockStateProperties.UP, true)
                .setValue(BlockStateProperties.DOWN, true));
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.NORTH, BlockStateProperties.EAST, BlockStateProperties.SOUTH, BlockStateProperties.WEST, BlockStateProperties.UP, BlockStateProperties.DOWN);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
        if (worldIn.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity te = worldIn.getBlockEntity(pos);

        if (!(te instanceof LightRedirectorTileEntity)) {
            return InteractionResult.PASS;
        }

        Direction facing = hit.getDirection();
        LightRedirectorTileEntity redirector = (LightRedirectorTileEntity) te;
        redirector.toggleSide(facing);

        worldIn.setBlock(pos, state.setValue(propertyFor(facing), redirector.isEnabled(facing)), 3);

        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new LightRedirectorTileEntity(pos, state);
    }
}
