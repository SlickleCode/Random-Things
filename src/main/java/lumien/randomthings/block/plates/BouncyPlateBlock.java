package lumien.randomthings.block.plates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BouncyPlateBlock extends PlateBlock
{
	public BouncyPlateBlock()
	{
		super(Block.Properties.create(Material.EARTH, MaterialColor.STONE).hardnessAndResistance(0.3F).sound(SoundType.STONE));
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
	{
		super.onEntityCollision(state, worldIn, pos, entityIn);

		if (entityIn.getMotion().y < 1)
		{
			entityIn.onGround = false;
			entityIn.fallDistance = 0;
			entityIn.setMotion(entityIn.getMotion().x, 1, entityIn.getMotion().z);
		}
	}
}
