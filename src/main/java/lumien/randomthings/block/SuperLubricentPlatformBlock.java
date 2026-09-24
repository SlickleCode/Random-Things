package lumien.randomthings.block;

import lumien.randomthings.item.SuperLubricentBootsItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.util.BlockRenderLayer;

public class SuperLubricentPlatformBlock extends Block
{
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(0, 14, 0, 16, 16, 16);

	public SuperLubricentPlatformBlock()
	{
		super(Block.Properties.create(Material.ICE, MaterialColor.ICE).hardnessAndResistance(0.5F).slipperiness(1F / 0.98F));
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		return SHAPE;
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context)
	{
		Entity entity = context.getEntity();

		if (entity == null)
		{
			return VoxelShapes.empty();
		}

		if (entity.posY < pos.getY() + 14F / 16F)
		{
			return VoxelShapes.empty();
		}

		if (entity instanceof PlayerEntity && ((PlayerEntity) entity).isSneaking() && entity.getMotion().y <= 0)
		{
			return VoxelShapes.empty();
		}

		return super.getCollisionShape(state, worldIn, pos, context);
	}

	@Override
	public BlockRenderLayer getRenderLayer()
	{
		return BlockRenderLayer.TRANSLUCENT;
	}

	@Override
	public float getSlipperiness(BlockState state, IWorldReader worldIn, BlockPos pos, Entity entity)
	{
		if (entity instanceof LivingEntity && !entity.isSneaking() && ((LivingEntity) entity).getItemStackFromSlot(EquipmentSlotType.FEET).getItem() instanceof SuperLubricentBootsItem)
		{
			return 0.6F;
		}

		return super.getSlipperiness(state, worldIn, pos, entity);
	}
}
