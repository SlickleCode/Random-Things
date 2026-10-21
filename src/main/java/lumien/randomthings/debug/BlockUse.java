package lumien.randomthings.debug;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/** Dev-only: what a right click on a block does since 1.20.5 split {@code Block.use} in two (held item first, then the bare interaction). */
public final class BlockUse
{
	private BlockUse()
	{
	}

	public static InteractionResult use(BlockState state, Level level, Player player, InteractionHand hand, BlockHitResult hit)
	{
		ItemInteractionResult itemResult = state.useItemOn(player.getItemInHand(hand), level, player, hand, hit);

		if (itemResult.consumesAction())
		{
			return itemResult.result();
		}

		if (itemResult == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION && hand == InteractionHand.MAIN_HAND)
		{
			return state.useWithoutItem(level, player, hit);
		}

		return itemResult.result();
	}
}
