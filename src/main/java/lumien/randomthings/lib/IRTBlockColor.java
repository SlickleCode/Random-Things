package lumien.randomthings.lib;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;

/**
 * Implemented by blocks that need a runtime tint (biome color, dye color,
 * etc.) applied to one or more of their model's tinted quads. Wired up
 * generically to Forge's {@code IBlockColor}/{@code BlockColors} system by a
 * single {@code ColorHandlerEvent.Block} listener in {@code RandomThings}
 * rather than needing a bespoke registration per block.
 */
public interface IRTBlockColor {
    int colorMultiplier(BlockState state, IEnviromentBlockReader worldIn, BlockPos pos, int tintIndex);
}
