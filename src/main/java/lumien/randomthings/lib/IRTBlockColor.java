package lumien.randomthings.lib;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

/**
 * Implemented by blocks that need a runtime tint (biome color, dye color,
 * etc.) applied to one or more of their model's tinted quads. Wired up
 * generically to Forge's {@code IBlockColor}/{@code BlockColors} system by a
 * single {@code ColorHandlerEvent.Block} listener in {@code RandomThings}
 * rather than needing a bespoke registration per block.
 */
public interface IRTBlockColor {
    int colorMultiplier(BlockState state, BlockAndTintGetter worldIn, BlockPos pos, int tintIndex);
}
