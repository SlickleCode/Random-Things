package lumien.randomthings.item;

import lumien.randomthings.util.StackTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

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
public abstract class CompassItemBase extends Item implements IClientItemProperties{
	public CompassItemBase(Item.Properties properties)
	{
		super(properties);

	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void registerClientProperties()
	{
		net.minecraft.client.renderer.item.ItemProperties.register(this, ResourceLocation.parse("angle"), new ItemPropertyFunction()
		{
			private double rotation;
			private double rota;
			private long lastUpdateTick;

			@Override
			public float call(ItemStack stack, net.minecraft.client.multiplayer.ClientLevel worldIn, LivingEntity entityIn, int seed)
			{
				if (entityIn == null && !stack.isFramed())
				{
					return 0.0F;
				}

				boolean heldByEntity = entityIn != null;
				Entity entity = heldByEntity ? entityIn : stack.getFrame();

				Level world = worldIn;

				if (world == null)
				{
					world = entity.level();
				}

				double angle;

				CompoundTag compound = StackTag.get(stack);
				boolean hasTarget = compound != null && compound.contains("targetX");

				if (hasTarget)
				{
					double facing = heldByEntity ? entity.getYRot() : getFrameRotation((ItemFrame) entity);
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
					angle = wobble(world, angle);
				}

				float f = (float) (angle / (Math.PI * 2D));
				return Mth.positiveModulo(f, 1.0F);
			}

			private double wobble(Level world, double target)
			{
				if (world.getGameTime() != this.lastUpdateTick)
				{
					this.lastUpdateTick = world.getGameTime();

					double delta = target - this.rotation;
					delta = delta % (Math.PI * 2D);
					delta = Mth.clamp(delta, -1.0D, 1.0D);
					this.rota += delta * 0.1D;
					this.rota *= 0.8D;
					this.rotation += this.rota;
				}

				return this.rotation;
			}

			private double getFrameRotation(ItemFrame itemFrame)
			{
				return Mth.wrapDegrees(180 + itemFrame.getDirection().get2DDataValue() * 90);
			}

			/**
			 * Aims at the block's center (+0.5 on each horizontal axis), not its
			 * corner - {@code targetX}/{@code targetZ} are stored as the raw
			 * {@link BlockPos} the {@link PositionFilterItem} recorded, so the
			 * offset belongs here, not in what gets saved.
			 */
			private double getAngleToPos(Entity entity, BlockPos pos)
			{
				return Math.atan2((pos.getZ() + 0.5D) - entity.getZ(), (pos.getX() + 0.5D) - entity.getX());
			}
		});
	}
}
