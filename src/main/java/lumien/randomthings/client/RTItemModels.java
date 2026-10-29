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
import net.minecraft.resources.Identifier;
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
	private static Identifier id(String path)
	{
		return Identifier.fromNamespaceAndPath(ModConstants.MOD_ID, path);
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
			event.register(id("fluid_filled"), FluidFilled.CODEC);
			event.register(id("recording"), Recording.CODEC);
		});
		modBus.addListener((RegisterColorHandlersEvent.ItemTintSources event) -> event.register(id("tint"), Tint.CODEC));
		modBus.addListener((RegisterColorHandlersEvent.ItemTintSources event) -> event.register(id("fluid"), FluidTint.CODEC));
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
		public float get(ItemStack stack, ClientLevel worldIn, net.minecraft.world.entity.ItemOwner owner, int seed)
		{
			// 1.21.5+: the owner is the entity holding the item (or the item frame it hangs in); anything else (ground, gui) has no needle
			boolean heldByEntity = owner != null && owner.asLivingEntity() != null;

			if (!heldByEntity && !(owner instanceof ItemFrame))
			{
				return 0.0F;
			}

			Level world = worldIn;

			if (world == null)
			{
				world = owner.level();
			}

			double angle;

			CompoundTag compound = StackTag.get(stack);
			boolean hasTarget = compound != null && compound.contains("targetX");

			if (hasTarget)
			{
				double facing = heldByEntity ? owner.getVisualRotationYInDegrees() : getFrameRotation((ItemFrame) owner);
				facing = facing % 360.0D;
				double angleToTarget = getAngleToPos(owner.position(), new BlockPos(compound.getIntOr("targetX", 0), 0, compound.getIntOr("targetZ", 0)));
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
		private double getAngleToPos(net.minecraft.world.phys.Vec3 from, BlockPos pos)
		{
			return Math.atan2((pos.getZ() + 0.5D) - from.z, (pos.getX() + 0.5D) - from.x);
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
		public float get(ItemStack stack, ClientLevel world, net.minecraft.world.entity.ItemOwner owner, int seed)
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
		public float get(ItemStack stack, ClientLevel world, net.minecraft.world.entity.ItemOwner owner, int seed)
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

	/** True while an Ender Bucket holds a fluid: the item definition then switches to the model with the fluid layer. */
	public static final class FluidFilled implements ConditionalItemModelProperty
	{
		public static final MapCodec<FluidFilled> CODEC = MapCodec.unit(FluidFilled::new);

		@Override
		public boolean get(ItemStack stack, ClientLevel world, LivingEntity entity, int seed, ItemDisplayContext context)
		{
			net.neoforged.neoforge.fluids.SimpleFluidContent content = stack.get(lumien.randomthings.lib.ModDataComponents.FLUID);
			return content != null && !content.isEmpty();
		}

		@Override
		public MapCodec<FluidFilled> type()
		{
			return CODEC;
		}
	}

	/**
	 * Colours the fluid layer of a filled Ender Bucket: the average colour of the fluid's still texture times the fluid's own tint, so water, lava and any modded
	 * fluid show up in their own colour without a model per fluid.
	 */
	public static final class FluidTint implements ItemTintSource
	{
		public static final MapCodec<FluidTint> CODEC = MapCodec.unit(FluidTint::new);

		private static final java.util.Map<Identifier, Integer> AVERAGES = new java.util.HashMap<>();

		@Override
		public int calculate(ItemStack stack, ClientLevel level, LivingEntity entity)
		{
			net.neoforged.neoforge.fluids.SimpleFluidContent content = stack.get(lumien.randomthings.lib.ModDataComponents.FLUID);

			if (content == null || content.isEmpty())
			{
				return 0xFFFFFFFF;
			}

			net.neoforged.neoforge.fluids.FluidStack fluid = content.copy();
			net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions extensions = net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions.of(fluid.getFluid());
			int average = AVERAGES.computeIfAbsent(extensions.getStillTexture(fluid), FluidTint::average);
			int tint = extensions.getTintColor(fluid);
			int r = (tint >> 16 & 0xFF) * (average >> 16 & 0xFF) / 255;
			int g = (tint >> 8 & 0xFF) * (average >> 8 & 0xFF) / 255;
			int b = (tint & 0xFF) * (average & 0xFF) / 255;
			return 0xFF000000 | r << 16 | g << 8 | b;
		}

		/** Mean colour of the texture's first animation frame (the atlas keeps the whole strip). */
		private static int average(Identifier texture)
		{
			net.minecraft.client.renderer.texture.SpriteContents contents = Minecraft.getInstance().getAtlasManager().getAtlasOrThrow(net.minecraft.data.AtlasIds.BLOCKS).getSprite(texture).contents();
			com.mojang.blaze3d.platform.NativeImage image = contents.getOriginalImage();
			long r = 0, g = 0, b = 0, n = 0;

			for (int y = 0; y < contents.height(); y++)
			{
				for (int x = 0; x < contents.width(); x++)
				{
					int pixel = image.getPixel(x, y);

					if ((pixel >>> 24) == 0)
					{
						continue;
					}

					r += pixel >> 16 & 0xFF;
					g += pixel >> 8 & 0xFF;
					b += pixel & 0xFF;
					n++;
				}
			}

			return n == 0 ? 0xFFFFFF : (int) (r / n) << 16 | (int) (g / n) << 8 | (int) (b / n);
		}

		@Override
		public MapCodec<FluidTint> type()
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

			return (ItemStackRenderState state, ItemStack stack, ItemModelResolver resolver, ItemDisplayContext displayContext, ClientLevel level, net.minecraft.world.entity.ItemOwner entity, int seed) -> {
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
