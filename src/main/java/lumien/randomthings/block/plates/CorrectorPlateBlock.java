package lumien.randomthings.block.plates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CorrectorPlateBlock extends PlateBlock
{
	public CorrectorPlateBlock()
	{
		super(Block.Properties.create(Material.EARTH, MaterialColor.STONE).hardnessAndResistance(0.3F).sound(SoundType.STONE));
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
	{
		super.onEntityCollision(state, worldIn, pos, entityIn);

		if (Math.abs(entityIn.getMotion().x) < Math.abs(entityIn.getMotion().z))
		{
			if (entityIn.posX != pos.getX() + 0.5)
			{
				entityIn.setPositionAndUpdate(pos.getX() + 0.5, entityIn.posY, entityIn.posZ);
			}

			if (entityIn.getMotion().x != 0)
			{
				entityIn.setMotion(0, entityIn.getMotion().y, entityIn.getMotion().z);
			}
		}
		else if (Math.abs(entityIn.getMotion().x) > Math.abs(entityIn.getMotion().z))
		{
			if (entityIn.posZ != pos.getZ() + 0.5)
			{
				entityIn.setPositionAndUpdate(entityIn.posX, entityIn.posY, pos.getZ() + 0.5);
			}

			if (entityIn.getMotion().z != 0)
			{
				entityIn.setMotion(entityIn.getMotion().x, entityIn.getMotion().y, 0);
			}
		}
	}
}
