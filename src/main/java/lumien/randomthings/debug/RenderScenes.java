package lumien.randomthings.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.entity.EclipsedClockEntity;
import lumien.randomthings.entity.GoldenChickenEntity;
import lumien.randomthings.entity.ModEntityTypes;
import lumien.randomthings.entity.SpectreIlluminatorEntity;
import lumien.randomthings.entity.SpiritEntity;
import lumien.randomthings.entity.TimeAcceleratorEntity;
import lumien.randomthings.entity.WeatherCloudEntity;
import lumien.randomthings.entity.ArtificialEndPortalEntity;
import lumien.randomthings.handler.spectre.SpectreHandler;
import lumien.randomthings.item.BiomeCrystalItem;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.tileentity.BiomeRadarTileEntity;
import lumien.randomthings.tileentity.DiaphanousBlockTileEntity;
import lumien.randomthings.tileentity.FluidDisplayTileEntity;
import lumien.randomthings.tileentity.RuneBaseTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Developer-only: builds small, repeatable scenes (a gallery of every block, the special block entities, the entities,
 * dyed items and armor, the Spectre dimension, rain with and without a Rain Shield) so the client can screenshot what
 * the renderers actually draw ({@code client/SceneShots}, started by the autotest with {@code RT_AUTOTEST_SCENES=true}).
 * Driven by {@code /rtdebug scene <id>}. Everything happens on a platform at a fixed place in the overworld.
 */
public final class RenderScenes
{
	private static final int PER_ROW = 10;
	private static final int ROWS = 3;

	private static BlockPos origin;
	private static boolean prepared;

	private RenderScenes()
	{
	}

	private static List<Block> galleryBlocks()
	{
		List<Block> blocks = new ArrayList<>();

		for (Block block : ForgeRegistries.BLOCKS.getValues())
		{
			if (ModConstants.MOD_ID.equals(block.getRegistryName().getNamespace()))
			{
				blocks.add(block);
			}
		}

		Collections.sort(blocks, Comparator.comparing(b -> b.getRegistryName().getPath()));
		return blocks;
	}

	/** All scene ids in the order they are shot. */
	public static List<String> ids()
	{
		List<String> ids = new ArrayList<>();
		int pages = (galleryBlocks().size() + PER_ROW * ROWS - 1) / (PER_ROW * ROWS);

		for (int i = 0; i < pages; i++)
		{
			ids.add("blocks_" + i);
		}

		Collections.addAll(ids, "rune_base", "fluid_display", "biome_radar", "diaphanous_near", "entities_a", "entities_b", "time_wheel", "time_wheel_close", "entities_c", "armor_and_items", "light_redirector", "magic_hood", "blazing_fire", "rain_with_shield", "rain_without_shield", "spectre", "spectre_back");
		return ids;
	}

	/** Ticks the client waits after the scene command before shooting. */
	public static int waitTicks(String id)
	{
		switch (id)
		{
			case "entities_c":
				return 150; // the end portal only shows after its own 85 ticks
			case "entities_b":
				return 20; // the Time Accelerator effect is short-lived // the end portal only shows after its own 85 ticks
			case "rain_with_shield":
			case "rain_without_shield":
				return 140;
			case "spectre":
			case "spectre_back":
				return 100;
			default:
				return 60;
		}
	}

	private static void prepare(ServerPlayerEntity player)
	{
		ServerWorld world = player.getServerWorld();

		if (origin == null)
		{
			origin = new BlockPos(player.getPosition().getX(), 100, player.getPosition().getZ());
		}

		if (!prepared)
		{
			prepared = true;
			world.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, world.getServer());
			world.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(false, world.getServer());
			world.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, world.getServer());
		}

		world.setDayTime(6000);
		WorldInfo info = world.getWorldInfo();
		info.setRaining(false);
		info.setThundering(false);
		world.setRainStrength(0);
		world.setThunderStrength(0);

		for (Entity entity : world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(origin.add(-20, -4, -12), origin.add(20, 14, 34)), e -> !(e instanceof ServerPlayerEntity)))
		{
			entity.remove();
		}

		for (int x = -16; x <= 16; x++)
		{
			for (int z = -12; z <= 30; z++)
			{
				for (int y = 0; y <= 12; y++)
				{
					BlockPos pos = origin.add(x, y, z);

					if (!world.getBlockState(pos).isAir())
					{
						world.removeTileEntity(pos);
						world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2 | 16);
					}
				}

				world.setBlockState(origin.add(x, -1, z), Blocks.LIGHT_GRAY_CONCRETE.getDefaultState(), 2 | 16);
			}
		}
	}

	private static void camera(ServerPlayerEntity player, double dx, double dy, double dz, float yaw, float pitch)
	{
		player.setGameType(GameType.CREATIVE);
		player.abilities.isFlying = true;
		player.abilities.allowFlying = true;
		player.sendPlayerAbilities();
		player.setMotion(0, 0, 0);

		if (player.connection != null)
		{
			player.connection.setPlayerLocation(origin.getX() + 0.5 + dx, origin.getY() + dy, origin.getZ() + 0.5 + dz, yaw, pitch);
		}
	}

	private static void place(ServerWorld world, BlockPos pos, Block block)
	{
		world.setBlockState(pos, block.getDefaultState(), 2 | 16);
	}

	private static <T extends net.minecraft.tileentity.TileEntity> T tile(ServerWorld world, BlockPos pos, Class<T> type)
	{
		return type.cast(world.getTileEntity(pos));
	}

	/** @return an error message, or null when the scene was built */
	public static String build(ServerPlayerEntity player, String id)
	{
		prepare(player);
		ServerWorld world = player.getServerWorld();
		int x0 = origin.getX();
		int y0 = origin.getY();
		int z0 = origin.getZ();

		if (id.startsWith("blocks_"))
		{
			List<Block> blocks = galleryBlocks();
			int page = Integer.parseInt(id.substring("blocks_".length()));
			int start = page * PER_ROW * ROWS;

			for (int i = 0; i < PER_ROW * ROWS && start + i < blocks.size(); i++)
			{
				int col = i % PER_ROW;
				int row = i / PER_ROW;
				BlockPos pos = origin.add(col * 2 - PER_ROW, 0, row * 3);
				Block block = blocks.get(start + i);

				place(world, pos, block);
				// a dark post below every block so floating plants/torches read against the floor
				world.setBlockState(pos.down(), Blocks.BLACK_CONCRETE.getDefaultState(), 2 | 16);
			}

			camera(player, -1, 7, -5.5, 0, 45);
			return null;
		}

		switch (id)
		{
			case "rune_base":
			{
				for (int i = 0; i < 2; i++)
				{
					BlockPos pos = origin.add(i * 2, 0, 0);
					place(world, pos, ModBlocks.RUNE_BASE);
					DyeColor[][] data = new DyeColor[4][4];

					for (int x = 0; x < 4; x++)
					{
						for (int y = 0; y < 4; y++)
						{
							if ((x + y + i) % 2 == 0 || x == 1 || y == 2)
							{
								data[x][y] = DyeColor.values()[(x * 4 + y + i * 3) % 16];
							}
						}
					}

					tile(world, pos, RuneBaseTileEntity.class).setRuneData(data);
					tile(world, pos, RuneBaseTileEntity.class).syncTE();
				}

				camera(player, 1, 2.2, -1.2, 0, 70);
				return null;
			}
			case "fluid_display":
			{
				place(world, origin, ModBlocks.FLUID_DISPLAY);
				place(world, origin.add(2, 0, 0), ModBlocks.FLUID_DISPLAY);
				place(world, origin.add(4, 0, 0), ModBlocks.FLUID_DISPLAY);
				tile(world, origin, FluidDisplayTileEntity.class).setFluidStack(new FluidStack(Fluids.WATER, 1000));
				tile(world, origin.add(2, 0, 0), FluidDisplayTileEntity.class).setFluidStack(new FluidStack(Fluids.LAVA, 1000));

				for (int i = 0; i < 3; i++)
				{
					tile(world, origin.add(i * 2, 0, 0), FluidDisplayTileEntity.class).syncTE();
				}

				camera(player, 2, 3, -4, 0, 30);
				return null;
			}
			case "biome_radar":
			{
				String[] biomes = { "minecraft:plains", "minecraft:desert", "minecraft:snowy_tundra" };

				for (int i = 0; i < biomes.length; i++)
				{
					BlockPos pos = origin.add(i * 2, 0, 0);
					place(world, pos, ModBlocks.BIOME_RADAR);
					ItemStack crystal = new ItemStack(ModItems.BIOME_CRYSTAL);
					BiomeCrystalItem.setBiome(crystal, ForgeRegistries.BIOMES.getValue(new ResourceLocation(biomes[i])));
					tile(world, pos, BiomeRadarTileEntity.class).setCrystal(crystal);
				}

				camera(player, 2, 2.6, -3.2, 0, 14);
				return null;
			}
			case "diaphanous_near":
			{
				Block[] shown = { Blocks.OAK_PLANKS, Blocks.GLASS, Blocks.GOLD_BLOCK };

				for (int i = 0; i < shown.length; i++)
				{
					BlockPos pos = origin.add(i * 2, 0, 0);
					place(world, pos, ModBlocks.DIAPHANOUS_BLOCK);
					tile(world, pos, DiaphanousBlockTileEntity.class).setDisplayState(shown[i].getDefaultState(), i == 2);
				}

				camera(player, 2, 1.8, -4, 0, 10);
				return null;
			}
			case "entities_a":
			{
				double y = y0;
				world.addEntity(withPos(new SpiritEntity(world, x0 - 3, y + 1, z0 + 3), x0 - 3, y + 1, z0 + 3));

				GoldenChickenEntity chicken = new GoldenChickenEntity(ModEntityTypes.GOLDEN_CHICKEN, world);
				chicken.setNoAI(true);
				world.addEntity(withPos(chicken, x0 - 1, y, z0 + 3));

				world.addEntity(withPos(new SpectreIlluminatorEntity(world, x0 + 1, y + 1, z0 + 3), x0 + 1, y + 1, z0 + 3));
				world.addEntity(withPos(new WeatherCloudEntity(world, x0 + 3, y + 2, z0 + 3, ModItems.WEATHER_EGG_SUN), x0 + 3, y + 2, z0 + 3));

				camera(player, 0, 2.2, -2.5, 0, 10);
				return null;
			}
			case "entities_b":
			{
				BlockPos target = origin.add(-3, 0, 3);
				place(world, target, Blocks.STONE);
				world.addEntity(new TimeAcceleratorEntity(world, target, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5));

				// a wall with a hanging Eclipsed Clock in front of it
				for (int x = 0; x <= 3; x++)
				{
					for (int yy = 0; yy <= 3; yy++)
					{
						place(world, origin.add(x, yy, 6), Blocks.STONE_BRICKS);
					}
				}

				world.addEntity(new EclipsedClockEntity(world, origin.add(1, 2, 5), Direction.NORTH));
				camera(player, 1, 3, -3, 0, 22);
				return null;
			}
			case "time_wheel":
			{
				// one accelerated block per time rate (1, 2, 4, 8, 16, 32): the wheel gets a ring more at 2, 4, 8, 16 and 32
				int[] rates = { 1, 2, 4, 8, 16, 32 };

				for (int i = 0; i < rates.length; i++)
				{
					BlockPos target = origin.add(-5 + 2 * i, 0, 4);
					place(world, target, Blocks.STONE);
					TimeAcceleratorEntity accelerator = new TimeAcceleratorEntity(world, target, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
					accelerator.setTimeRate(rates[i]);
					accelerator.setRemainingTime(100000);
					world.addEntity(accelerator);
				}

				camera(player, 0, 2.6, -2, 0, 24);
				return null;
			}
			case "time_wheel_close":
			{
				// the wheel of the highest time rate on the face turned towards the camera, seen from about half a block
				BlockPos target = origin.add(0, 0, 3);
				place(world, target, Blocks.STONE);
				TimeAcceleratorEntity accelerator = new TimeAcceleratorEntity(world, target, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5);
				accelerator.setTimeRate(32);
				accelerator.setRemainingTime(100000);
				world.addEntity(accelerator);
				camera(player, 0, -1.12, 2.0, 0, 0);
				return null;
			}
			case "entities_c":
			{
				// the portal removes itself unless its frame exists: end stone floor, obsidian ring, end rod, end stone above it
				for (int dx = -1; dx <= 1; dx++)
				{
					for (int dz = -1; dz <= 1; dz++)
					{
						place(world, origin.add(dx, -1, 4 + dz), Blocks.END_STONE);
					}
				}

				for (int dx = -2; dx <= 2; dx++)
				{
					for (int dz = -2; dz <= 2; dz++)
					{
						if (Math.abs(dx) == 2 || Math.abs(dz) == 2)
						{
							place(world, origin.add(dx, 0, 4 + dz), Blocks.OBSIDIAN);
						}
					}
				}

				place(world, origin.add(0, 3, 4), Blocks.END_ROD);
				place(world, origin.add(0, 4, 4), Blocks.END_STONE);
				world.addEntity(withPos(new ArtificialEndPortalEntity(world, x0, y0, z0 + 4), x0, y0, z0 + 4));
				camera(player, 0, 4.5, -1, 0, 40);
				return null;
			}
			case "armor_and_items":
			{
				ArmorStandEntity leather = new ArmorStandEntity(world, x0 - 4 + 0.5, y0, z0 + 4.5);
				leather.setItemStackToSlot(EquipmentSlotType.CHEST, dyedLeather(0x3355FF));
				leather.setItemStackToSlot(EquipmentSlotType.LEGS, rtDye(new ItemStack(Items.LEATHER_LEGGINGS), 0xFF3322));
				world.addEntity(leather);

				ArmorStandEntity iron = new ArmorStandEntity(world, x0 - 1.5, y0, z0 + 4.5);
				iron.setItemStackToSlot(EquipmentSlotType.CHEST, rtDye(new ItemStack(Items.IRON_CHESTPLATE), 0x22CC44));
				iron.setItemStackToSlot(EquipmentSlotType.HEAD, rtDye(new ItemStack(Items.IRON_HELMET), 0xFFCC00));
				world.addEntity(iron);

				ArmorStandEntity plain = new ArmorStandEntity(world, x0 + 1.5, y0, z0 + 4.5);
				plain.setItemStackToSlot(EquipmentSlotType.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
				plain.setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.LEATHER_HELMET));
				world.addEntity(plain);

				groundItem(world, new ItemStack(Items.DIAMOND), x0 + 4, y0 + 0.3, z0 + 4.5);
				groundItem(world, rtDye(new ItemStack(Items.DIAMOND), 0xFF2222), x0 + 5, y0 + 0.3, z0 + 4.5);
				groundItem(world, rtDye(new ItemStack(Items.IRON_SWORD), 0x2222FF), x0 + 6, y0 + 0.3, z0 + 4.5);
				groundItem(world, new ItemStack(ModItems.PORTKEY), x0 + 7, y0 + 0.3, z0 + 4.5);
				ItemStack camo = new ItemStack(ModItems.PORTKEY);
				camo.getOrCreateTag().put("camo", new ItemStack(Items.DIAMOND_PICKAXE).write(new net.minecraft.nbt.CompoundNBT()));
				groundItem(world, camo, x0 + 8, y0 + 0.3, z0 + 4.5);

				for (int i = 0; i < 3; i++)
				{
					ItemStack crystal = new ItemStack(ModItems.BIOME_CRYSTAL);
					BiomeCrystalItem.setBiome(crystal, ForgeRegistries.BIOMES.getValue(new ResourceLocation(i == 0 ? "minecraft:plains" : (i == 1 ? "minecraft:desert" : "minecraft:snowy_tundra"))));
					groundItem(world, crystal, x0 + 9 + i, y0 + 0.3, z0 + 4.5);
				}

				camera(player, 4, 2.5, -1, 0, 12);
				return null;
			}
			case "light_redirector":
			{
				// Redirector R: the block east of it must draw as the block WEST of it and vice versa (client-only render trick,
				// BlockRendererDispatcherTransformer). Back row = the same blocks around plain stone, as the control.
				place(world, new BlockPos(x0 - 5, y0, z0 + 8), Blocks.GOLD_BLOCK);
				place(world, new BlockPos(x0 - 4, y0, z0 + 8), Blocks.STONE);
				place(world, new BlockPos(x0 - 3, y0, z0 + 8), Blocks.DIAMOND_BLOCK);

				place(world, new BlockPos(x0 + 2, y0, z0 + 4), Blocks.GOLD_BLOCK);
				place(world, new BlockPos(x0 + 4, y0, z0 + 4), Blocks.DIAMOND_BLOCK);
				place(world, new BlockPos(x0 + 3, y0, z0 + 4), ModBlocks.LIGHT_REDIRECTOR);
				camera(player, 1, 3, -2, 0, 14);
				return null;
			}
			case "magic_hood":
				// the two remote players (one wearing a Magic Hood) are added on the client by SceneShots
				camera(player, 0, 2, -1, 0, 6);
				return null;
			case "blazing_fire":
			{
				place(world, origin, Blocks.NETHERRACK);
				place(world, origin.up(), ModBlocks.BLAZING_FIRE);
				place(world, origin.add(2, 0, 0), Blocks.NETHERRACK);
				place(world, origin.add(2, 1, 0), Blocks.FIRE);
				camera(player, 1, 3, -4, 0, 28);
				return null;
			}
			case "rain_with_shield":
			case "rain_without_shield":
			{
				if (id.equals("rain_with_shield"))
				{
					place(world, origin.add(0, 0, 3), ModBlocks.RAIN_SHIELD);
				}

				for (int i = -9; i <= 9; i += 3)
				{
					if (i != 0)
					{
						place(world, origin.add(i, 0, 3), Blocks.OAK_FENCE);
					}
				}

				WorldInfo info = world.getWorldInfo();
				info.setRaining(true);
				info.setRainTime(100000);
				camera(player, 0, 3, -2, 0, 12);
				return null;
			}
			case "spectre":
			{
				SpectreHandler handler = SpectreHandler.getInstance(player.getServer());

				if (handler == null)
				{
					return "no spectre dimension in this world";
				}

				handler.teleportPlayerToSpectreCube(player);
				player.abilities.isFlying = true;
				player.sendPlayerAbilities();
				return null;
			}
			case "spectre_back":
			{
				SpectreHandler handler = SpectreHandler.getInstance(player.getServer());

				if (handler != null)
				{
					handler.teleportPlayerBack(player);
				}

				return null;
			}
			default:
				return "unknown scene " + id;
		}
	}

	private static <T extends Entity> T withPos(T entity, double x, double y, double z)
	{
		entity.setPosition(x + 0.5, y, z + 0.5);
		return entity;
	}

	private static ItemStack rtDye(ItemStack stack, int rgb)
	{
		stack.getOrCreateTag().putInt("rtDye", rgb);
		return stack;
	}

	private static ItemStack dyedLeather(int rgb)
	{
		ItemStack stack = new ItemStack(Items.LEATHER_CHESTPLATE);
		stack.getOrCreateChildTag("display").putInt("color", rgb);
		return stack;
	}

	private static void groundItem(ServerWorld world, ItemStack stack, double x, double y, double z)
	{
		ItemEntity entity = new ItemEntity(world, x, y, z, stack);
		entity.setNoGravity(true);
		entity.setMotion(0, 0, 0);
		entity.setPickupDelay(32767);
		world.addEntity(entity);
	}
}
