package lumien.randomthings.block;

import java.util.Random;

import lumien.randomthings.tileentity.AdvancedRedstoneRepeaterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RepeaterBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.TickPriority;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

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
public class AdvancedRedstoneRepeaterBlock extends RepeaterBlock
{
	public AdvancedRedstoneRepeaterBlock()
	{
		super(Block.Properties.create(net.minecraft.block.material.Material.MISCELLANEOUS).doesNotBlockMovement().hardnessAndResistance(0.0F));
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new AdvancedRedstoneRepeaterTileEntity();
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (!worldIn.isRemote)
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof AdvancedRedstoneRepeaterTileEntity)
			{
				NetworkHooks.openGui((ServerPlayerEntity) player, (AdvancedRedstoneRepeaterTileEntity) te);
			}
		}

		return true;
	}

	private static AdvancedRedstoneRepeaterTileEntity getTE(World world, BlockPos pos)
	{
		TileEntity te = world.getTileEntity(pos);
		return te instanceof AdvancedRedstoneRepeaterTileEntity ? (AdvancedRedstoneRepeaterTileEntity) te : null;
	}

	/**
	 * Reached from neighborChanged whenever an adjacent block changes; this is
	 * where the real "wait N ticks, then flip POWERED" scheduling decision
	 * happens (tick() itself only applies the flip and re-schedules in one
	 * edge case - see below).
	 */
	@Override
	protected void updateState(World world, BlockPos pos, BlockState state)
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

		boolean wasPowered = state.get(POWERED);
		boolean shouldPower = this.shouldBePowered(world, pos, state);

		if (wasPowered != shouldPower && !world.getPendingBlockTicks().isTickPending(pos, this))
		{
			TickPriority priority = TickPriority.HIGH;

			if (this.isFacingTowardsRepeater(world, pos, state))
			{
				priority = TickPriority.EXTREMELY_HIGH;
			}
			else if (wasPowered)
			{
				priority = TickPriority.VERY_HIGH;
			}

			int delay = shouldPower ? arr.turnOnDelay() : arr.turnOffDelay();

			world.getPendingBlockTicks().scheduleTick(pos, this, delay, priority);
		}
	}

	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random rand)
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

		boolean wasPowered = state.get(POWERED);
		boolean shouldPower = this.shouldBePowered(worldIn, pos, state);

		if (wasPowered && !shouldPower)
		{
			worldIn.setBlockState(pos, state.with(POWERED, false), 2);
		}
		else if (!wasPowered)
		{
			worldIn.setBlockState(pos, state.with(POWERED, true), 2);

			if (!shouldPower)
			{
				worldIn.getPendingBlockTicks().scheduleTick(pos, this, arr.turnOffDelay(), TickPriority.HIGH);
			}
		}
	}
}
