package lumien.randomthings.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Shared needle-angle logic for {@link GoldenCompassItem} and
 * {@link EmeraldCompassItem} - a direct port of vanilla's own compass
 * "wobble towards a target" math (from {@code ItemGoldenCompass}/
 * {@code ItemEmeraldCompass} in 1.12.2, themselves near-identical copies of
 * vanilla's {@code ItemCompass}), reading a custom {@code targetX}/
 * {@code targetZ} NBT pair instead of vanilla's lodestone/spawn position.
 * Registered under the same {@code minecraft:angle} property key vanilla's
 * own compass uses, so the item model JSON's {@code overrides} predicates
 * work exactly like vanilla's 32-frame compass model.
 */
public abstract class CompassItemBase extends Item
{
	public CompassItemBase(Item.Properties properties)
	{
		super(properties);

		this.addPropertyOverride(new ResourceLocation("angle"), new IItemPropertyGetter()
		{
			private double rotation;
			private double rota;
			private long lastUpdateTick;

			@Override
			public float call(ItemStack stack, World worldIn, LivingEntity entityIn)
			{
				if (entityIn == null && !stack.isOnItemFrame())
				{
					return 0.0F;
				}

				boolean heldByEntity = entityIn != null;
				Entity entity = heldByEntity ? entityIn : stack.getItemFrame();

				if (worldIn == null)
				{
					worldIn = entity.world;
				}

				double angle;

				CompoundNBT compound = stack.getTag();
				boolean hasTarget = compound != null && compound.contains("targetX");

				if (hasTarget)
				{
					double facing = heldByEntity ? entity.rotationYaw : getFrameRotation((ItemFrameEntity) entity);
					facing = facing % 360.0D;
					double angleToTarget = getAngleToPos(entity, new BlockPos(compound.getInt("targetX"), 0, compound.getInt("targetZ")));
					angle = Math.PI - ((facing - 90.0D) * 0.01745329238474369D - angleToTarget);
				}
				else
				{
					angle = Math.random() * (Math.PI * 2D);
				}

				if (heldByEntity && !hasTarget)
				{
					angle = wobble(worldIn, angle);
				}

				float f = (float) (angle / (Math.PI * 2D));
				return MathHelper.positiveModulo(f, 1.0F);
			}

			private double wobble(World world, double target)
			{
				if (world.getGameTime() != this.lastUpdateTick)
				{
					this.lastUpdateTick = world.getGameTime();

					double delta = target - this.rotation;
					delta = delta % (Math.PI * 2D);
					delta = MathHelper.clamp(delta, -1.0D, 1.0D);
					this.rota += delta * 0.1D;
					this.rota *= 0.8D;
					this.rotation += this.rota;
				}

				return this.rotation;
			}

			private double getFrameRotation(ItemFrameEntity itemFrame)
			{
				return MathHelper.wrapDegrees(180 + itemFrame.getHorizontalFacing().getHorizontalIndex() * 90);
			}

			/**
			 * Aims at the block's center (+0.5 on each horizontal axis), not its
			 * corner - {@code targetX}/{@code targetZ} are stored as the raw
			 * {@link BlockPos} the {@link PositionFilterItem} recorded, so the
			 * offset belongs here, not in what gets saved.
			 */
			private double getAngleToPos(Entity entity, BlockPos pos)
			{
				return Math.atan2((pos.getZ() + 0.5D) - entity.posZ, (pos.getX() + 0.5D) - entity.posX);
			}
		});
	}
}
