package lumien.randomthings.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.EntityBlock;

import java.util.Random;

import lumien.randomthings.tileentity.AdvancedRedstoneRepeaterTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.ticks.TickPriority;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;

/**
 * A repeater whose on/off delay is a TileEntity-backed 2-10000 tick value
 * instead of vanilla's fixed 1-4 tick DELAY property. Extends vanilla
 * RepeaterBlock/RedstoneDiodeBlock directly to reuse its powering logic.
 * {@code RedstoneDiodeBlock.getDelay(BlockState)} has no pos/world parameter
 * to look the per-instance TE value up from, so both real call sites
 * ({@code updateState}, reached via neighborChanged - the actual scheduling
 * decision - and the edge-case reschedule inside {@code tick} itself) are
 * overridden instead, ground-truthed via bytecode disassembly of
 * RedstoneDiodeBlock since both methods are otherwise unavailable to inspect
 * any other way.
 */
public class AdvancedRedstoneRepeaterBlock extends RepeaterBlock implements EntityBlock {
	public AdvancedRedstoneRepeaterBlock()
	{
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.NONE).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).noCollision().strength(0.0F));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
	    return new AdvancedRedstoneRepeaterTileEntity(pos, state);
	}

	@Override
	public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
		if (!worldIn.isClientSide())
		{
			BlockEntity te = worldIn.getBlockEntity(pos);

			if (te instanceof AdvancedRedstoneRepeaterTileEntity)
			{
				((ServerPlayer) player).openMenu((AdvancedRedstoneRepeaterTileEntity) te);
			}
		}

		return InteractionResult.SUCCESS;
	}

	private static AdvancedRedstoneRepeaterTileEntity getTE(Level world, BlockPos pos)
	{
		BlockEntity te = world.getBlockEntity(pos);
		return te instanceof AdvancedRedstoneRepeaterTileEntity ? (AdvancedRedstoneRepeaterTileEntity) te : null;
	}

	/**
	 * Reached from neighborChanged whenever an adjacent block changes; this is
	 * where the real "wait N ticks, then flip POWERED" scheduling decision
	 * happens (tick() itself only applies the flip and re-schedules in one
	 * edge case - see below).
	 */
	@Override
	protected void checkTickOnNeighbor(Level world, BlockPos pos, BlockState state)
	{
		if (this.isLocked(world, pos, state))
		{
			return;
		}

		AdvancedRedstoneRepeaterTileEntity arr = getTE(world, pos);

		if (arr == null)
		{
			return;
		}

		boolean wasPowered = state.getValue(POWERED);
		boolean shouldPower = this.shouldTurnOn(world, pos, state);

		if (wasPowered != shouldPower && !world.getBlockTicks().willTickThisTick(pos, this))
		{
			TickPriority priority = TickPriority.HIGH;

			if (this.shouldPrioritize(world, pos, state))
			{
				priority = TickPriority.EXTREMELY_HIGH;
			}
			else if (wasPowered)
			{
				priority = TickPriority.VERY_HIGH;
			}

			int delay = shouldPower ? arr.turnOnDelay() : arr.turnOffDelay();

			world.scheduleTick(pos, this, delay, priority);
		}
	}

	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand)
	{
		if (this.isLocked(worldIn, pos, state))
		{
			return;
		}

		AdvancedRedstoneRepeaterTileEntity arr = getTE(worldIn, pos);

		if (arr == null)
		{
			return;
		}

		boolean wasPowered = state.getValue(POWERED);
		boolean shouldPower = this.shouldTurnOn(worldIn, pos, state);

		if (wasPowered && !shouldPower)
		{
			worldIn.setBlock(pos, state.setValue(POWERED, false), 2);
		}
		else if (!wasPowered)
		{
			worldIn.setBlock(pos, state.setValue(POWERED, true), 2);

			if (!shouldPower)
			{
				worldIn.scheduleTick(pos, this, arr.turnOffDelay(), TickPriority.HIGH);
			}
		}
	}
}
