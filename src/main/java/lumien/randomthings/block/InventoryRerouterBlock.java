package lumien.randomthings.block;

import java.util.HashSet;

import lumien.randomthings.tileentity.InventoryRerouterTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.text.TranslationTextComponent;

/**
 * A hopper-shaped block that redirects capability access from its non-facing
 * sides to its facing side's neighbor, per-side remappable by right-clicking
 * a non-facing face (see {@link InventoryRerouterTileEntity}).
 * <p>
 * Real bug, found 2026-09-28 (reported by user): "the side does not change
 * texture when right clicked so I cannot determine what behavior the side is
 * expected to do." Checked 1.12.2's real source first: the original genuinely
 * did draw a per-face overlay decal (a small directional arrow icon, one of 6
 * new textures) baked via a custom {@code IBakedModel}/{@code
 * IExtendedBlockState} unlisted property - this port's own class javadoc had
 * already disclosed dropping that as a simplification, citing this project's
 * now-deleted Plate family as precedent (which no longer exists to actually
 * compare against). Rather than reintroduce a full custom baked-model/texture
 * system for 6 new directional icons this project has no art assets for,
 * fixed the actual reported problem more directly: right-clicking a face now
 * tells the player in an action-bar message exactly what that face is set to
 * (a specific direction, or disabled) every time it's cycled - solves "I
 * can't tell what this does" without needing new rendering infrastructure.
 */
public class InventoryRerouterBlock extends Block
{
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());

	private final HashSet<BlockPos> circleGuard = new HashSet<>();

	public InventoryRerouterBlock()
	{
		super(Block.Properties.create(Material.ROCK).hardnessAndResistance(1.5F));

		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.DOWN));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		builder.add(FACING);
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new InventoryRerouterTileEntity();
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		return this.getDefaultState().with(FACING, context.getNearestLookingDirection().getOpposite());
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (worldIn.isRemote)
		{
			return true;
		}

		Direction clickedFace = hit.getFace();
		Direction myFacing = state.get(FACING);

		if (clickedFace == myFacing)
		{
			return false;
		}

		TileEntity te = worldIn.getTileEntity(pos);

		if (te instanceof InventoryRerouterTileEntity)
		{
			InventoryRerouterTileEntity rerouter = (InventoryRerouterTileEntity) te;
			rerouter.rotateFacing(clickedFace);

			Direction newTarget = rerouter.getFacingMap().get(clickedFace);

			if (newTarget == null)
			{
				player.sendStatusMessage(new TranslationTextComponent("gui.randomthings.inventory_rerouter.disabled", clickedFace.getName()), true);
			}
			else
			{
				player.sendStatusMessage(new TranslationTextComponent("gui.randomthings.inventory_rerouter.redirects", clickedFace.getName(), newTarget.getName()), true);
			}
		}

		return true;
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving)
	{
		if (circleGuard.contains(pos))
		{
			return;
		}

		Direction facing = state.get(FACING);
		BlockPos offset = pos.offset(facing);

		if (offset.equals(fromPos))
		{
			circleGuard.add(pos);
			worldIn.notifyNeighborsOfStateChange(pos, this);
			circleGuard.remove(pos);
		}
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.CUTOUT;
	}
}
