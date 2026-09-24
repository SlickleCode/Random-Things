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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.util.BlockRenderLayer;

/**
 * The horizontal-friction math this depends on: {@code LivingEntity.travel}
 * multiplies X/Z motion by {@code (onGround ? blockSlipperiness * 0.91F : 0.91F)}
 * every single tick, on or off the ground doesn't matter here since this
 * block is always stood on - confirmed via `javap -c` disassembly of
 * {@code LivingEntity.travel}/{@code func_213335_r}, since neither method is
 * otherwise documented. That 0.91F multiplier is a hardcoded vanilla
 * constant, not something {@link Block.Properties#slipperiness} can bypass
 * directly - but slipperiness IS a free multiplicand against it, so setting
 * it to exactly {@code 1F / 0.91F} makes the product exactly {@code 1.0F}:
 * perfect momentum retention, i.e. true zero friction, entirely through
 * vanilla's own formula. No Mixin needed - the earlier {@code 1F / 0.98F}
 * value (matching vanilla ice's raw slipperiness field, inverted) came up
 * short of that by design intent, not necessity.
 * <p>
 * One real consequence worth knowing: true zero friction means the
 * acceleration-while-holding-a-direction math (also slipperiness-scaled)
 * has no opposing decay to reach a steady-state top speed against anymore -
 * holding a movement key on this platform now accelerates without bound for
 * as long as you hold it, not just "stops decelerating once you let go".
 * That's an inherent property of true zero friction, not a bug.
 * <p>
 * <b>Speed cap is a deliberate deviation from 1.12.2, not a port of it.</b>
 * Traced the original's ASM patch ({@code ClassTransformer.patchEntityLivingBase}
 * -&gt; {@code AsmHandler.slipFix}) in full: it forces the exact same friction
 * factor to {@code 1.0F} and does nothing else - 1.12.2 had no max-speed
 * clamp on this block either, so unbounded acceleration was already the
 * original's actual (if perhaps unintended) behavior. {@link #onEntityCollision}
 * below adds a new horizontal-speed clamp per the user's explicit request,
 * independent of anything in the original mod.
 */
public class SuperLubricentPlatformBlock extends Block
{
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(0, 14, 0, 16, 16, 16);

	/** Blocks/tick (20 m/s, ~72 km/h) - fast, but bounded. Tune freely. */
	private static final double MAX_HORIZONTAL_SPEED = 1.0D;

	public SuperLubricentPlatformBlock()
	{
		super(Block.Properties.create(Material.ICE, MaterialColor.ICE).hardnessAndResistance(0.5F).slipperiness(1F / 0.91F));
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

	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn)
	{
		super.onEntityCollision(state, worldIn, pos, entityIn);

		Vec3d motion = entityIn.getMotion();
		double horizontalSpeedSq = motion.x * motion.x + motion.z * motion.z;

		if (horizontalSpeedSq > MAX_HORIZONTAL_SPEED * MAX_HORIZONTAL_SPEED)
		{
			double scale = MAX_HORIZONTAL_SPEED / Math.sqrt(horizontalSpeedSq);
			entityIn.setMotion(motion.x * scale, motion.y, motion.z * scale);
		}
	}
}
