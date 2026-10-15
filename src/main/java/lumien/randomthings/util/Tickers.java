package lumien.randomthings.util;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Helper for {@code EntityBlock.getTicker}: returns the ticker only when the requested type is the expected one. */
public final class Tickers
{
	private Tickers()
	{
	}

	@SuppressWarnings("unchecked")
	public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> of(BlockEntityType<A> given, BlockEntityType<E> expected, BlockEntityTicker<? super E> ticker)
	{
		return expected == given ? (BlockEntityTicker<A>) ticker : null;
	}
}
