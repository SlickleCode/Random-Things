package lumien.randomthings.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import lumien.randomthings.item.PortkeyItem;
import lumien.randomthings.item.RedstoneActivatorItem;
import lumien.randomthings.item.SoundPatternItem;
import lumien.randomthings.item.SoundRecorderItem;
import lumien.randomthings.lib.IRTBlockColor;
import lumien.randomthings.lib.IRTItemColor;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.util.StackTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;

/**
 * Minecraft 1.21.4 replaced the model-JSON {@code overrides} predicates ({@code ItemProperties.register}), item colour handlers and the
 * {@code BlockEntityWithoutLevelRenderer} by item definitions ({@code assets/randomthings/items/*.json}). This class registers the building
 * blocks those definitions refer to: the compass/clock needles ({@code range_dispatch}), the on/off textures ({@code condition}), the tint
 * source ({@code tints}) and the Portkey's camouflage model.
 */
public final class RTItemModels
{
	private static ResourceLocation id(String path)
	{
		return ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, path);
	}

	public static void init(IEventBus modBus)
	{
		modBus.addListener((RegisterRangeSelectItemModelPropertyEvent event) -> {
			event.register(id("angle"), Angle.CODEC);
			event.register(id("time"), Time.CODEC);
			event.register(id("duration_index"), DurationIndex.CODEC);
		});
		modBus.addListener((RegisterConditionalItemModelPropertyEvent event) -> {
			event.register(id("filled"), Filled.CODEC);
			event.register(id("recording"), Recording.CODEC);
		});
		modBus.addListener((RegisterColorHandlersEvent.ItemTintSources event) -> event.register(id("tint"), Tint.CODEC));
		modBus.addListener((RegisterItemModelsEvent event) -> event.register(id("portkey"), PortkeyModel.CODEC));
	}

	/** The compass needle of the Golden and Emerald Compass (same wobble math as vanilla's compass). */
	public static final class Angle implements RangeSelectItemModelProperty
	{
		public static final MapCodec<Angle> CODEC = MapCodec.unit(Angle::new);

		private double rotation;
		private double rota;
		private long lastUpdateTick;

		@Override
		public float get(ItemStack stack, ClientLevel worldIn, LivingEntity entityIn, int seed)
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

		/** Aims at the block's center (+0.5 on each horizontal axis), not its corner - the stored target is the raw block position. */
		private double getAngleToPos(Entity entity, BlockPos pos)
		{
			return Math.atan2((pos.getZ() + 0.5D) - entity.getZ(), (pos.getX() + 0.5D) - entity.getX());
		}

		@Override
		public MapCodec<Angle> type()
		{
			return CODEC;
		}
	}

	/** Eclipsed Clock hand: the time the clock is set to. */
	public static final class Time implements RangeSelectItemModelProperty
	{
		public static final MapCodec<Time> CODEC = MapCodec.unit(Time::new);

		@Override
		public float get(ItemStack stack, ClientLevel world, LivingEntity entity, int seed)
		{
			int time = 6000;

			if (StackTag.contains(stack, "targetTime"))
			{
				time = StackTag.getInt(stack, "targetTime");
			}

			int i = time % 24000;
			float f = ((float) i) / 24000.0F - 0.25F;

			if (f < 0.0F)
			{
				++f;
			}

			if (f > 1.0F)
			{
				--f;
			}

			float f1 = 1.0F - (float) ((Math.cos((double) f * Math.PI) + 1.0D) / 2.0D);
			return f + (f1 - f) / 3.0F;
		}

		@Override
		public MapCodec<Time> type()
		{
			return CODEC;
		}
	}

	public static final class DurationIndex implements RangeSelectItemModelProperty
	{
		public static final MapCodec<DurationIndex> CODEC = MapCodec.unit(DurationIndex::new);

		@Override
		public float get(ItemStack stack, ClientLevel world, LivingEntity entity, int seed)
		{
			return RedstoneActivatorItem.getDurationIndex(stack);
		}

		@Override
		public MapCodec<DurationIndex> type()
		{
			return CODEC;
		}
	}

	public static final class Filled implements ConditionalItemModelProperty
	{
		public static final MapCodec<Filled> CODEC = MapCodec.unit(Filled::new);

		@Override
		public boolean get(ItemStack stack, ClientLevel world, LivingEntity entity, int seed, ItemDisplayContext context)
		{
			return SoundPatternItem.getSoundLocation(stack) != null;
		}

		@Override
		public MapCodec<Filled> type()
		{
			return CODEC;
		}
	}

	public static final class Recording implements ConditionalItemModelProperty
	{
		public static final MapCodec<Recording> CODEC = MapCodec.unit(Recording::new);

		@Override
		public boolean get(ItemStack stack, ClientLevel world, LivingEntity entity, int seed, ItemDisplayContext context)
		{
			return SoundRecorderItem.isRecording(stack);
		}

		@Override
		public MapCodec<Recording> type()
		{
			return CODEC;
		}
	}

	/**
	 * The bridge from {@link IRTItemColor} / {@link IRTBlockColor} to the item definition's {@code tints}: any item whose JSON lists
	 * {@code {"type": "randomthings:tint", "index": n}} is tinted by the item itself, or by the block of a {@link BlockItem} using the client
	 * player's current position (as 1.12.2's {@code ItemBlockColored} did).
	 */
	public record Tint(int index) implements ItemTintSource
	{
		public static final MapCodec<Tint> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(Codec.INT.optionalFieldOf("index", 0).forGetter(Tint::index)).apply(inst, Tint::new));

		@Override
		public int calculate(ItemStack stack, ClientLevel level, LivingEntity entity)
		{
			int rgb = 0xFFFFFF;

			if (stack.getItem() instanceof IRTItemColor color)
			{
				rgb = color.getColorFromItemstack(stack, index);
			}
			else if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof IRTBlockColor blockColor)
			{
				Block block = blockItem.getBlock();

				if (block instanceof lumien.randomthings.block.ColoredGrassBlock)
				{
					rgb = lumien.randomthings.block.ColoredGrassBlock.getTint(lumien.randomthings.block.ColoredGrassBlock.getColor(stack));
				}
				else
				{
					Minecraft mc = Minecraft.getInstance();

					if (mc.level != null && mc.player != null)
					{
						rgb = blockColor.colorMultiplier(block.defaultBlockState(), mc.level, mc.player.blockPosition(), index);
					}
				}
			}

			return 0xFF000000 | rgb;
		}

		@Override
		public MapCodec<Tint> type()
		{
			return CODEC;
		}
	}

	/**
	 * The Portkey takes the looks of the item it is camouflaged as ({@link PortkeyItem#getCamoStack}); without one it shows its plain model.
	 * Replaces 1.20's {@code PortkeyItemRenderer} ({@code BlockEntityWithoutLevelRenderer}).
	 */
	public record PortkeyModel(ItemModel.Unbaked base) implements ItemModel.Unbaked
	{
		public static final MapCodec<PortkeyModel> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(ItemModels.CODEC.fieldOf("base").forGetter(PortkeyModel::base)).apply(inst, PortkeyModel::new));

		@Override
		public MapCodec<PortkeyModel> type()
		{
			return CODEC;
		}

		@Override
		public void resolveDependencies(ResolvableModel.Resolver resolver)
		{
			this.base.resolveDependencies(resolver);
		}

		@Override
		public ItemModel bake(ItemModel.BakingContext context)
		{
			ItemModel baked = this.base.bake(context);

			return (ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, ClientLevel level, LivingEntity entity, int seed) -> {
				ItemStack camo = PortkeyItem.getCamoStack(stack);

				if (camo.isEmpty())
				{
					baked.update(state, stack, resolver, displayContext, level, entity, seed);
				}
				else
				{
					resolver.appendItemLayers(state, camo, displayContext, level, entity, seed);
				}
			};
		}
	}

	private RTItemModels()
	{
	}
}
