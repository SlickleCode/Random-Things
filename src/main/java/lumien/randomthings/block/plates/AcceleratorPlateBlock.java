package lumien.randomthings.block.plates;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class AcceleratorPlateBlock extends PlateBlock
{
	public AcceleratorPlateBlock()
	{
		super(Block.Properties.create(Material.EARTH, MaterialColor.STONE).hardnessAndResistance(0.3F).sound(SoundType.STONE));
	}

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
	{
		super.onEntityCollision(state, worldIn, pos, entityIn);

		Vec3d motionVec = entityIn.getMotion().scale(1.2);

		if (motionVec.lengthSquared() > 0.5 * 0.5)
		{
			motionVec = motionVec.normalize().scale(0.5);
		}

		entityIn.setMotion(motionVec);
	}
}
