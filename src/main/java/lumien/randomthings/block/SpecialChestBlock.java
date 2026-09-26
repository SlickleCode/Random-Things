package lumien.randomthings.block;

import lumien.randomthings.tileentity.SpecialChestTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.material.Material;
import net.minecraft.state.properties.ChestType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * A re-skinned, non-merging (always single, never becomes a double chest)
 * vanilla-style chest. Deliberately does NOT extend vanilla
 * {@code ChestBlock} - that class bakes in adjacency-merging logic
 * (`updatePostPlacement`) that 1.12.2's original never had; this keeps that
 * same simpler single-chest-only behavior instead of picking up double-chest
 * merging as an unintended bonus feature.
 * <p>
 * It does still need to carry {@link ChestBlock#TYPE} itself, always fixed to
 * {@link ChestType#SINGLE} and never read or written beyond that: vanilla's
 * {@code ChestTileEntity} (which {@link SpecialChestTileEntity} extends
 * directly for its lid-animation/open-sound logic) unconditionally calls
 * {@code getBlockState().get(ChestBlock.TYPE)} in a few places - e.g. the
 * open/close sound - with no {@code instanceof ChestBlock} guard, so a block
 * that doesn't register this property crashes the moment the chest is opened
 * (`IllegalArgumentException: Cannot get property ... TYPE ... as it does
 * not exist`). Registering it - without ever implementing the merging logic
 * that would let it become anything other than SINGLE - satisfies that
 * assumption while keeping this block's own always-single behavior exactly.
 */
public class SpecialChestBlock extends Block
{
	private static final VoxelShape SHAPE = Block.makeCuboidShape(1, 0, 1, 15, 14, 15);

	private final int chestType;

	public SpecialChestBlock(int chestType)
	{
		super(Block.Properties.create(Material.WOOD).hardnessAndResistance(2.5F));

		this.chestType = chestType;
		this.setDefaultState(this.stateContainer.getBaseState().with(HorizontalBlock.HORIZONTAL_FACING, Direction.NORTH).with(ChestBlock.TYPE, ChestType.SINGLE));
	}

	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder)
	{
		builder.add(HorizontalBlock.HORIZONTAL_FACING, ChestBlock.TYPE);
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state)
	{
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world)
	{
		return new SpecialChestTileEntity(chestType);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context)
	{
		return this.getDefaultState().with(HorizontalBlock.HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);

		if (stack.hasDisplayName())
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof SpecialChestTileEntity)
			{
				((SpecialChestTileEntity) te).setCustomName(stack.getDisplayName());
			}
		}
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if (state.getBlock() != newState.getBlock())
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof SpecialChestTileEntity)
			{
				InventoryHelper.dropInventoryItems(worldIn, pos, (SpecialChestTileEntity) te);
			}
		}

		super.onReplaced(state, worldIn, pos, newState, isMoving);
	}

	@Override
	public boolean hasComparatorInputOverride(BlockState state)
	{
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos)
	{
		TileEntity te = worldIn.getTileEntity(pos);
		return te instanceof SpecialChestTileEntity ? Container.calcRedstoneFromInventory((SpecialChestTileEntity) te) : 0;
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
	{
		if (!worldIn.isRemote)
		{
			TileEntity te = worldIn.getTileEntity(pos);

			if (te instanceof SpecialChestTileEntity)
			{
				NetworkHooks.openGui((ServerPlayerEntity) player, (SpecialChestTileEntity) te);
			}
		}

		return true;
	}
}
