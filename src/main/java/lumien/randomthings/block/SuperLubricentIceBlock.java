package lumien.randomthings.block;

import lumien.randomthings.item.SuperLubricentBootsItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class SuperLubricentIceBlock extends Block
{
	public SuperLubricentIceBlock()
	{
		super(Block.Properties.create(Material.ICE, MaterialColor.ICE).hardnessAndResistance(0.5F).slipperiness(1F / 0.98F));
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
