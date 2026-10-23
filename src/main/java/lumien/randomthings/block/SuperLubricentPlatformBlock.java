package lumien.randomthings.block;



import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;

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
 * The speed/turn-rate cap is new behavior beyond 1.12.2, not a port of it -
 * see {@link SuperLubricentPhysics}, which also backs {@link SuperLubricentIceBlock}
 * and {@link SuperLubricentStoneBlock} so all three Super Lubricent blocks
 * behave identically. A boat gets a different, exact slipperiness value than
 * everyone else (see the {@code getSlipperiness} override below and {@link
 * SuperLubricentPhysics}'s javadoc for why), with an external cap on top of
 * that either way (a {@code LivingUpdateEvent} listener for living entities,
 * paired {@code WorldTickEvent}/{@code ClientTickEvent} listeners for boats)
 * in {@code RandomThings}, not here. Boots-negation doesn't live here at
 * all -
 * {@code SuperLubricentBootsTransformer.js} (a coremod, see
 * {@code lumien.randomthings.asm.AsmHandler#bootsMaxSlip}) intercepts
 * friction globally instead.
 */
public class SuperLubricentPlatformBlock extends Block
{
	protected static final VoxelShape SHAPE = Block.box(0, 14, 0, 16, 16, 16);

	public SuperLubricentPlatformBlock()
	{
		super(lumien.randomthings.lib.RTIds.block().mapColor(net.minecraft.world.level.material.MapColor.ICE).instrument(net.minecraft.world.level.block.state.properties.NoteBlockInstrument.HAT).noOcclusion().strength(0.5F).friction(1F / 0.91F));
	}

	/**
	 * See {@link SuperLubricentPhysics}'s class javadoc for why a boat needs
	 * exact {@code 1.0F} here instead of this block's normal zero-friction
	 * value.
	 */
	@Override
	public float getFriction(BlockState state, LevelReader world, BlockPos pos, Entity entity)
	{
		return SuperLubricentPhysics.slipperinessFor(entity, super.getFriction(state, world, pos, entity));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		return SHAPE;
	}

	@Override
	@SuppressWarnings("deprecation")
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context)
	{
		Entity entity = context instanceof net.minecraft.world.phys.shapes.EntityCollisionContext ecc ? ecc.getEntity() : null;

		if (entity == null)
		{
			return Shapes.empty();
		}

		if (entity.getY() < pos.getY() + 14F / 16F)
		{
			return Shapes.empty();
		}

		if (entity instanceof Player && ((Player) entity).isShiftKeyDown() && entity.getDeltaMovement().y <= 0)
		{
			return Shapes.empty();
		}

		return super.getCollisionShape(state, worldIn, pos, context);
	}
}
