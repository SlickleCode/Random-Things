package lumien.randomthings.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
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
 * The speed cap is new behavior beyond 1.12.2, not a port of it - see
 * {@link SuperLubricentPhysics}, which also backs {@link SuperLubricentIceBlock}
 * and {@link SuperLubricentStoneBlock} so all three Super Lubricent blocks
 * behave identically, and is enforced by a {@code LivingUpdateEvent} listener
 * in {@code RandomThings}, not here - see {@link SuperLubricentPhysics}'s
 * javadoc for why. Boots-negation doesn't live here at all -
 * {@code SuperLubricentBootsMixin} intercepts friction globally instead.
 */
public class SuperLubricentPlatformBlock extends Block
{
	protected static final VoxelShape SHAPE = Block.makeCuboidShape(0, 14, 0, 16, 16, 16);

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
}
