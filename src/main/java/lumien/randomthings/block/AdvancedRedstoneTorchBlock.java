package lumien.randomthings.block;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.EntityBlock;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import com.google.common.collect.Lists;

import lumien.randomthings.tileentity.AdvancedRedstoneTorchTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.world.InteractionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;

public class AdvancedRedstoneTorchBlock extends TorchBlock implements EntityBlock {
	public static enum COLOR implements StringRepresentable
	{
		GREEN, RED;

		@Override
		public String getSerializedName()
		{
			return this == GREEN ? "green" : "red";
		}
	}

	public static final EnumProperty<COLOR> COLOR_PROPERTY = EnumProperty.create("color", COLOR.class);
	private static final Map<BlockGetter, List<AdvancedRedstoneTorchBlock.Toggle>> BURNED_TORCHES = java.util.Collections.synchronizedMap(new WeakHashMap<>());

	protected AdvancedRedstoneTorchBlock()
	{
		super(ParticleTypes.FLAME, lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.NONE).pushReaction(net.minecraft.world.level.material.PushReaction.DESTROY).noCollission().strength(0.0F).lightLevel(state -> 7).sound(SoundType.WOOD));
		this.registerDefaultState(this.stateDefinition.any().setValue(COLOR_PROPERTY, COLOR.RED));
	}

	protected AdvancedRedstoneTorchBlock(Block.Properties properties)
	{
		super(ParticleTypes.FLAME, properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
	    return new AdvancedRedstoneTorchTileEntity(pos, state);
	}

	public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
		if (worldIn.isClientSide)
		{
			return InteractionResult.SUCCESS;
		}
		else
		{
			BlockEntity tileentity = worldIn.getBlockEntity(pos);
			if (tileentity instanceof AdvancedRedstoneTorchTileEntity)
			{
				AdvancedRedstoneTorchTileEntity art = (AdvancedRedstoneTorchTileEntity) tileentity;
				((ServerPlayer) player).openMenu(art);
			}

			return InteractionResult.SUCCESS;
		}
	}

	/**
	 * How many world ticks before ticking
	 */
	public int tickRate(LevelReader worldIn)
	{
		return 2;
	}

	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving)
	{
		for (Direction direction : Direction.values())
		{
			worldIn.updateNeighborsAt(pos.relative(direction), this);
		}

	}

	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		// Found by /rtdebug allguis (2026-10-03): without this the tile entity was never removed when the
		// torch was broken/replaced, so it lingered at that position and a different tile-entity block
		// placed there (e.g. an Analog Emitter) got this stale torch tile entity instead of its own.
		super.onRemove(state, worldIn, pos, newState, isMoving);

		if (!isMoving)
		{
			for (Direction direction : Direction.values())
			{
				worldIn.updateNeighborsAt(pos.relative(direction), this);
			}

		}
	}

	/**
	 * @deprecated call via {@link IBlockState#getWeakPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible. Implementing/overriding is fine.
	 */
	public int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		AdvancedRedstoneTorchTileEntity te = (AdvancedRedstoneTorchTileEntity) blockAccess.getBlockEntity(pos);

		int strength = blockState.getValue(COLOR_PROPERTY) == COLOR.RED ? te.signalStrengthRed() : te.signalStrengthGreen();

		return Direction.UP != side ? strength : 0;
	}

	protected boolean shouldBeGreen(Level worldIn, BlockPos pos, BlockState state)
	{
		return worldIn.hasSignal(pos.below(), Direction.DOWN);
	}

	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random)
	{
		update(state, worldIn, pos, random, this.shouldBeGreen(worldIn, pos, state));
	}

	public static void update(BlockState state, Level worldIn, BlockPos pos, RandomSource p_196527_3_, boolean p_196527_4_)
	{
		List<AdvancedRedstoneTorchBlock.Toggle> list = BURNED_TORCHES.get(worldIn);

		while (list != null && !list.isEmpty() && worldIn.getGameTime() - (list.get(0)).time > 60L)
		{
			list.remove(0);
		}

		if (state.getValue(COLOR_PROPERTY) == COLOR.RED)
		{
			if (p_196527_4_)
			{
				worldIn.setBlock(pos, state.setValue(COLOR_PROPERTY, COLOR.GREEN), 3);
				if (isBurnedOut(worldIn, pos, true))
				{
					worldIn.levelEvent(1502, pos, 0);
					worldIn.scheduleTick(pos, worldIn.getBlockState(pos).getBlock(), 160);
				}
			}
		}
		else if (!p_196527_4_ && !isBurnedOut(worldIn, pos, false))
		{
			worldIn.setBlock(pos, state.setValue(COLOR_PROPERTY, COLOR.RED), 3);
		}

	}

	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, net.minecraft.world.level.redstone.Orientation fromPos, boolean isMoving)
	{
		if (state.getValue(COLOR_PROPERTY) == COLOR.RED == this.shouldBeGreen(worldIn, pos, state) && !worldIn.getBlockTicks().willTickThisTick(pos, this))
		{
			worldIn.scheduleTick(pos, this, this.tickRate(worldIn));
		}

	}

	/**
	 * @deprecated call via {@link IBlockState#getStrongPower(IBlockAccess,BlockPos,EnumFacing)} whenever possible. Implementing/overriding is fine.
	 */
	public int getDirectSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
	{
		return side == Direction.DOWN ? blockState.getSignal(blockAccess, pos, side) : 0;
	}

	/**
	 * Can this block provide power. Only wire currently seems to have this change based on its state.
	 * 
	 * @deprecated call via {@link IBlockState#canProvidePower()} whenever possible. Implementing/overriding is fine.
	 */
	public boolean isSignalSource(BlockState state)
	{
		return true;
	}

	protected DustParticleOptions GREEN_DUST = new DustParticleOptions(net.minecraft.util.ARGB.colorFromFloat(1F, 0.0F, 1.0F, 0.0F), 1.0F);

	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand)
	{
		if (stateIn.getValue(COLOR_PROPERTY) == COLOR.RED)
		{
			double d0 = (double) pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d1 = (double) pos.getY() + 0.7D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d2 = (double) pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			worldIn.addParticle(DustParticleOptions.REDSTONE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
		else
		{
			double d0 = (double) pos.getX() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d1 = (double) pos.getY() + 0.7D + (rand.nextDouble() - 0.5D) * 0.2D;
			double d2 = (double) pos.getZ() + 0.5D + (rand.nextDouble() - 0.5D) * 0.2D;
			worldIn.addParticle(GREEN_DUST, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		}
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(COLOR_PROPERTY);
	}

	private static boolean isBurnedOut(Level p_176598_0_, BlockPos worldIn, boolean pos)
	{
		List<AdvancedRedstoneTorchBlock.Toggle> list = BURNED_TORCHES.computeIfAbsent(p_176598_0_, (p_220288_0_) -> {
			return Lists.newArrayList();
		});
		if (pos)
		{
			list.add(new AdvancedRedstoneTorchBlock.Toggle(worldIn.immutable(), p_176598_0_.getGameTime()));
		}

		int i = 0;

		for (int j = 0; j < list.size(); ++j)
		{
			AdvancedRedstoneTorchBlock.Toggle redstonetorchblock$toggle = list.get(j);
			if (redstonetorchblock$toggle.pos.equals(worldIn))
			{
				++i;
				if (i >= 8)
				{
					return true;
				}
			}
		}

		return false;
	}

	public static class Toggle
	{
		private final BlockPos pos;
		private final long time;

		public Toggle(BlockPos pos, long time)
		{
			this.pos = pos;
			this.time = time;
		}
	}
}