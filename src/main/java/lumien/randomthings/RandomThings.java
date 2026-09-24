package lumien.randomthings;

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lumien.randomthings.asm.AsmHandler;
import lumien.randomthings.block.CompressedSlimeBlock;
import lumien.randomthings.block.ContactButtonBlock;
import lumien.randomthings.block.ContactLeverBlock;
import lumien.randomthings.block.FertilizedDirtBlock;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.block.SuperLubricentIceBlock;
import lumien.randomthings.block.SuperLubricentPhysics;
import lumien.randomthings.block.SuperLubricentPlatformBlock;
import lumien.randomthings.block.SuperLubricentStoneBlock;
import lumien.randomthings.client.renderer.DiviningRodRenderer;
import lumien.randomthings.client.renderer.RedstoneObserverLineRenderer;
import lumien.randomthings.client.renderer.SpecialChestTileEntityRenderer;
import lumien.randomthings.client.screen.ModScreens;
import lumien.randomthings.client.vfx.VFXHandler;
import lumien.randomthings.container.ModContainerTypes;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.StableEnderpearlItem;
import lumien.randomthings.lib.IRTBlockColor;
import lumien.randomthings.lib.IRTItemColor;
import lumien.randomthings.lib.ModConstants;
import lumien.randomthings.network.RTPacketHandler;
import lumien.randomthings.tileentity.ChatDetectorTileEntity;
import lumien.randomthings.tileentity.GlobalChatDetectorTileEntity;
import lumien.randomthings.tileentity.ModTileEntityTypes;
import lumien.randomthings.tileentity.RedstoneObserverTileEntity;
import lumien.randomthings.tileentity.SlimeCubeTileEntity;
import lumien.randomthings.tileentity.SpecialChestTileEntity;
import lumien.randomthings.util.InventoryUtil;
import lumien.randomthings.worldgen.BloodRoseFeature;
import lumien.randomthings.worldgen.ModFeatures;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.ShovelItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.placement.FrequencyConfig;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.UseHoeEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;


@Mod(ModConstants.MOD_ID)
public class RandomThings
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Random RNG = new Random();

	public static RandomThings INSTANCE;

	public RandomThings()
	{
		INSTANCE = this;

		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupCommon);
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);

		MinecraftForge.EVENT_BUS.register(this);

		MinecraftForge.EVENT_BUS.addListener((UseHoeEvent event) -> {
			ItemUseContext context = event.getContext();

			World world = context.getWorld();
			BlockPos pos = context.getPos();
			BlockState state = world.getBlockState(pos);

			if (state.getBlock() == ModBlocks.FERTILIZED_DIRT && !state.get(FertilizedDirtBlock.TILLED))
			{
				event.setResult(Result.ALLOW);
				world.setBlockState(pos, state.with(FertilizedDirtBlock.TILLED, true));
				PlayerEntity playerentity = context.getPlayer();
				world.playSound(playerentity, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
			}
		});

		// A shovel right-clicked on a Slime Block compacts it into a Compressed Slime
		// Block (compression 0); right-clicking an existing Compressed Slime Block
		// compacts it further, up to compression 2. There's no generic Forge event
		// for "tool X used on block Y" the way UseHoeEvent covers hoes, so this
		// mirrors 1.12.2's own approach of hooking the raw right-click event directly.
		MinecraftForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
			ItemStack equipped = event.getItemStack();

			if (equipped.isEmpty() || !(equipped.getItem() instanceof ShovelItem))
			{
				return;
			}

			World world = event.getWorld();
			BlockPos pos = event.getPos();
			BlockState targetState = world.getBlockState(pos);
			PlayerEntity player = event.getPlayer();

			if (targetState.getBlock() == Blocks.SLIME_BLOCK)
			{
				player.swingArm(event.getHand());

				if (!world.isRemote)
				{
					world.setBlockState(pos, ModBlocks.COMPRESSED_SLIME_BLOCK.getDefaultState());
					world.playSound(null, pos, Blocks.SLIME_BLOCK.getSoundType(targetState).getPlaceSound(), SoundCategory.PLAYERS, 1.0F, 0.8F);
					equipped.damageItem(1, player, (p) -> p.sendBreakAnimation(event.getHand()));
				}
			}
			else if (targetState.getBlock() == ModBlocks.COMPRESSED_SLIME_BLOCK)
			{
				int currentCompression = targetState.get(CompressedSlimeBlock.COMPRESSION);

				if (currentCompression < 2)
				{
					player.swingArm(event.getHand());

					if (!world.isRemote)
					{
						world.setBlockState(pos, targetState.with(CompressedSlimeBlock.COMPRESSION, currentCompression + 1));
						world.playSound(null, pos, Blocks.SLIME_BLOCK.getSoundType(targetState).getPlaceSound(), SoundCategory.PLAYERS, 1.0F, 0.8F - ((currentCompression + 1) * 0.2F));
						equipped.damageItem(1, player, (p) -> p.sendBreakAnimation(event.getHand()));
					}
				}
			}
		});

		// Contact Button/Lever aren't triggered by clicking them directly - they're
		// mounted facing another block, and right-clicking THAT block is what
		// activates them (like a hidden pressure sensor). Ported straight from
		// 1.12.2's RTEventHandler.playerInteract: right-clicking any block scans
		// its 6 neighbors for a Contact Button/Lever whose FACING points back at
		// the clicked block, and activates the first match found (button checked
		// before lever, matching the original's ordering).
		MinecraftForge.EVENT_BUS.addListener((PlayerInteractEvent.RightClickBlock event) -> {
			World world = event.getWorld();

			if (world.isRemote || event.getHand() != Hand.MAIN_HAND)
			{
				return;
			}

			BlockPos clickedPos = event.getPos();

			for (Direction facing : Direction.values())
			{
				BlockPos neighborPos = clickedPos.offset(facing);
				BlockState neighborState = world.getBlockState(neighborPos);
				Block neighborBlock = neighborState.getBlock();

				if (neighborBlock instanceof ContactButtonBlock && neighborState.get(ContactButtonBlock.FACING) == facing.getOpposite())
				{
					((ContactButtonBlock) neighborBlock).activate(world, neighborPos, facing.getOpposite());
					break;
				}
				else if (neighborBlock instanceof ContactLeverBlock && neighborState.get(ContactLeverBlock.FACING) == facing.getOpposite())
				{
					((ContactLeverBlock) neighborBlock).activate(world, neighborPos, facing.getOpposite());
					break;
				}
			}
		});

		// Fire/lava protection: Obsidian Skull (carried anywhere in the inventory),
		// Obsidian Water Walking Boots and Lava Wader (worn as boots) all give a
		// chance to shrug off any fire-type damage entirely, scaling with how much
		// damage would've been dealt (chance = amount^3 / 100 - small burns are
		// usually blocked, a big single hit usually isn't). Lava Wader additionally
		// fully cancels LAVA damage specifically by spending its own charge meter
		// (see LavaWaderItem.onArmorTick), independent of the chance-based roll.
		MinecraftForge.EVENT_BUS.addListener((LivingAttackEvent event) -> {
			if (event.getEntityLiving().world.isRemote || event.isCanceled() || event.getAmount() <= 0 || !(event.getEntityLiving() instanceof PlayerEntity))
			{
				return;
			}

			PlayerEntity player = (PlayerEntity) event.getEntityLiving();

			if (event.getSource() == DamageSource.LAVA)
			{
				ItemStack lavaProtector = ItemStack.EMPTY;
				ItemStack lavaCharm = InventoryUtil.getPlayerInventoryItem(ModItems.LAVA_CHARM, player);

				if (!lavaCharm.isEmpty())
				{
					lavaProtector = lavaCharm;
				}

				ItemStack boots = player.getItemStackFromSlot(EquipmentSlotType.FEET);

				if (!boots.isEmpty() && boots.getItem() == ModItems.LAVA_WADER)
				{
					lavaProtector = boots;
				}

				if (!lavaProtector.isEmpty() && lavaProtector.hasTag())
				{
					CompoundNBT compound = lavaProtector.getTag();
					int charge = compound.getInt("charge");

					if (charge > 0)
					{
						compound.putInt("charge", charge - 1);
						compound.putInt("chargeCooldown", 40);
						event.setCanceled(true);
						return;
					}
				}
			}

			if (event.getSource().isFireDamage() && event.getSource() != DamageSource.LAVA)
			{
				ItemStack inventorySkull = InventoryUtil.getPlayerInventoryItem(ModItems.OBSIDIAN_SKULL, player);
				ItemStack obsidianBoots = player.getItemStackFromSlot(EquipmentSlotType.FEET);

				if (!obsidianBoots.isEmpty() && !(obsidianBoots.getItem() == ModItems.OBSIDIAN_WATER_WALKING_BOOTS || obsidianBoots.getItem() == ModItems.LAVA_WADER))
				{
					obsidianBoots = ItemStack.EMPTY;
				}

				ItemStack skull = inventorySkull.isEmpty() ? obsidianBoots : inventorySkull;

				if (!skull.isEmpty())
				{
					float amount = event.getAmount();
					float chance = amount / 100 * amount * amount;

					if (RNG.nextFloat() > chance)
					{
						event.setCanceled(true);
					}
				}
			}
		});

		// Water Walking Boots, Obsidian Water Walking Boots, and Lava Wader
		// (lava too, for the latter): while standing in the liquid with clear air
		// above, nudge the wearer up each tick instead of letting them sink - a
		// repeated small hop rather than true buoyancy. Passive by default (per
		// user direction, deviating from 1.12.2's hold-jump-to-float original) -
		// sneaking opts back out and lets the wearer sink normally. Client-side
		// only (this is a movement-prediction nicety, not physics the server needs
		// to arbitrate).
		MinecraftForge.EVENT_BUS.addListener((LivingEvent.LivingUpdateEvent event) -> {
			if (!event.getEntityLiving().world.isRemote || !(event.getEntityLiving() instanceof PlayerEntity))
			{
				return;
			}

			PlayerEntity player = (PlayerEntity) event.getEntityLiving();

			if (player.isSneaking())
			{
				return;
			}

			ItemStack boots = player.getItemStackFromSlot(EquipmentSlotType.FEET);

			if (boots.isEmpty() || !(boots.getItem() == ModItems.WATER_WALKING_BOOTS || boots.getItem() == ModItems.OBSIDIAN_WATER_WALKING_BOOTS || boots.getItem() == ModItems.LAVA_WADER))
			{
				return;
			}

			BlockPos liquidPos = new BlockPos(Math.floor(player.posX), Math.floor(player.posY), Math.floor(player.posZ));
			BlockPos airPos = new BlockPos(player.posX, player.posY + player.getHeight(), player.posZ);
			BlockState liquidState = player.world.getBlockState(liquidPos);
			Material liquidMaterial = liquidState.getMaterial();

			boolean overLiquid = liquidMaterial == Material.WATER || (boots.getItem() == ModItems.LAVA_WADER && liquidMaterial == Material.LAVA);

			if (overLiquid && player.world.getBlockState(airPos).getBlock().isAir(player.world.getBlockState(airPos), player.world, airPos))
			{
				player.move(MoverType.SELF, new Vec3d(0, 0.22, 0));
			}
		});

		// Super Lubricent Ice/Platform/Stone speed cap - see SuperLubricentPhysics's
		// javadoc. onEntityCollision doesn't fire for an entity merely resting on
		// top of a block (only for actual hitbox overlap), so the cap is enforced
		// here instead, via the same underfoot-block lookup vanilla's own friction
		// code uses in LivingEntity.travel: one full block below the entity's
		// bounding box, confirmed via javap -c disassembly. Registered
		// unconditionally (not gated on isRemote like the water-walking listener
		// above) since this is physics both sides need to agree on, matching how
		// the blocks' own slipperiness applies identically on client and server.
		MinecraftForge.EVENT_BUS.addListener((LivingEvent.LivingUpdateEvent event) -> {
			LivingEntity entity = event.getEntityLiving();

			if (!entity.onGround)
			{
				return;
			}

			BlockPos underfoot = new BlockPos(entity.posX, entity.getBoundingBox().minY - 1.0D, entity.posZ);
			Block block = entity.world.getBlockState(underfoot).getBlock();

			if (block instanceof SuperLubricentIceBlock || block instanceof SuperLubricentPlatformBlock || block instanceof SuperLubricentStoneBlock)
			{
				SuperLubricentPhysics.capHorizontalSpeed(entity);
			}
		});

		MinecraftForge.EVENT_BUS.addListener((ClientTickEvent event) -> {
			if (event.phase == TickEvent.Phase.END)
			{
				DiviningRodRenderer.get().tick();
				VFXHandler.INSTANCE.tick();
			}
		});

		// Drives StableEnderpearlItem's dropped-pearl countdown - see the javadoc on
		// StableEnderpearlItem.tickDroppedPearl for why this lives here instead of
		// on the item itself (Item.onEntityItemUpdate doesn't exist in this Forge
		// build).
		MinecraftForge.EVENT_BUS.addListener((TickEvent.WorldTickEvent event) -> {
			if (event.phase != TickEvent.Phase.END || event.world.isRemote)
			{
				return;
			}

			((ServerWorld) event.world).getEntities().filter(e -> e instanceof ItemEntity).map(e -> (ItemEntity) e).filter(e -> !e.getItem().isEmpty() && e.getItem().getItem() instanceof StableEnderpearlItem).collect(java.util.stream.Collectors.toList()).forEach(e -> ((StableEnderpearlItem) e.getItem().getItem()).tickDroppedPearl(e));
		});

		MinecraftForge.EVENT_BUS.addListener((ServerChatEvent event) -> {
			boolean consumed = false;

			for (ChatDetectorTileEntity detector : ChatDetectorTileEntity.detectors)
			{
				if (detector.checkMessage(event.getPlayer(), event.getMessage()))
				{
					consumed = true;
				}
			}

			for (GlobalChatDetectorTileEntity detector : GlobalChatDetectorTileEntity.detectors)
			{
				if (detector.checkMessage(event.getPlayer(), event.getMessage()))
				{
					consumed = true;
				}
			}

			if (consumed)
			{
				event.setCanceled(true);
			}
		});

		MinecraftForge.EVENT_BUS.addListener(RedstoneObserverTileEntity::notifyNeighbor);

		MinecraftForge.EVENT_BUS.addListener((LivingSpawnEvent.CheckSpawn event) -> {
			if (!(event.getEntityLiving() instanceof SlimeEntity))
			{
				return;
			}

			ChunkPos chunkPos = new ChunkPos(new BlockPos(event.getX(), event.getY(), event.getZ()));

			for (SlimeCubeTileEntity cube : SlimeCubeTileEntity.cubes)
			{
				if (cube.isInChunk((World) event.getWorld(), chunkPos))
				{
					event.setResult(cube.isPowered() ? Result.DENY : Result.ALLOW);
					return;
				}
			}
		});

		// Lapis Lamp (bright to the player, but shouldn't block hostile spawns) and
		// Quartz Lamp (dark-looking, but should) both used 1.12.2's ASM patch to make
		// Block.getLightValue return a different value per logical side - bright on
		// the client, dark on the server, or vice versa - since spawn-checks run
		// server-side and rendering reads the client's own value. That trick has no
		// equivalent in 1.14.4: confirmed via `javap -c` that BlockState.getLightValue()
		// reads a single cached field baked in once from Block.Properties, and the
		// light engine's propagation (which both rendering brightness AND the
		// mob-spawn light check read from) always uses that one cached value - there's
		// no side-branching point left to hook. Replaced with the same event-based
		// approach already used for Slime Cube above: scan a small radius around the
		// spawn attempt for either lamp and force the result, independent of the
		// block's actual (now perfectly normal, single-value) light emission.
		// Disclosed simplification: 1.12.2's version affected anywhere actual light
		// propagation reached (up to 15 blocks in the open); this uses a fixed
		// 4-block proximity radius instead - matches the spirit for a placed
		// decorative light source without an expensive per-spawn light-propagation
		// recomputation.
		MinecraftForge.EVENT_BUS.addListener((LivingSpawnEvent.CheckSpawn event) -> {
			if (!(event.getEntityLiving() instanceof MonsterEntity))
			{
				return;
			}

			World world = (World) event.getWorld();
			BlockPos spawnPos = new BlockPos(event.getX(), event.getY(), event.getZ());

			for (BlockPos p : BlockPos.getAllInBoxMutable(spawnPos.add(-4, -4, -4), spawnPos.add(4, 4, 4)))
			{
				Block block = world.getBlockState(p).getBlock();

				if (block == ModBlocks.LAPIS_LAMP)
				{
					event.setResult(Result.ALLOW);
					return;
				}

				if (block == ModBlocks.QUARTZ_LAMP)
				{
					event.setResult(Result.DENY);
					return;
				}
			}
		});

		// Was ASM patch #12 (WorldGenAbstractTree.setDirtAt): natural tree growth used
		// to hard-revert the soil block under a sapling to plain Dirt. Forge fires a
		// BlockEvent for each block a world-gen feature places, so we can veto that
		// specific replacement without any bytecode patching.
		MinecraftForge.EVENT_BUS.addListener((BlockEvent.EntityPlaceEvent event) -> {
			if (event.getPlacedBlock().getBlock() == net.minecraft.block.Blocks.DIRT && event.getBlockSnapshot().getReplacedBlock().getBlock() == ModBlocks.FERTILIZED_DIRT)
			{
				event.setCanceled(true);
			}
		});
	}

	private void setupCommon(final FMLCommonSetupEvent event)
	{
		AsmHandler.modBlockLight(0F, 1);
		RTPacketHandler.register();
		
		ForgeRegistries.BIOMES.forEach(b -> {
			b.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, Biome.createDecoratedFeature(ModFeatures.BLOOD_ROSES, IFeatureConfig.NO_FEATURE_CONFIG, Placement.COUNT_HEIGHTMAP_32, new FrequencyConfig(1)));
		});
	}

	private void setupClient(final FMLClientSetupEvent event)
	{
		ModScreens.register();

		ClientRegistry.bindTileEntitySpecialRenderer(SpecialChestTileEntity.class, new SpecialChestTileEntityRenderer());

		MinecraftForge.EVENT_BUS.addListener((RenderWorldLastEvent rwl) -> {
			DiviningRodRenderer.get().render();
			VFXHandler.INSTANCE.render(rwl.getPartialTicks());
			RedstoneObserverLineRenderer.render();
		});
	}



	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents
	{
		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent)
		{
			ModBlocks.registerBlocks(blockRegistryEvent);
		}

		@SubscribeEvent
		public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent)
		{
			ModItems.initItemGroup();

			ModItems.registerItems(itemRegistryEvent);
		}

		@SubscribeEvent
		public static void onTileEntityTypesRegistry(final RegistryEvent.Register<TileEntityType<?>> tileEntityTypeRegistryEvent)
		{
			ModTileEntityTypes.registerTypes(tileEntityTypeRegistryEvent);
		}

		@SubscribeEvent
		public static void onContainerTypesRegistry(final RegistryEvent.Register<ContainerType<?>> containerTypeRegistryEvent)
		{
			ModContainerTypes.registerContainerTypes(containerTypeRegistryEvent);
		}
		
		@SubscribeEvent
		public static void onFeaturesRegistry(final RegistryEvent.Register<Feature<?>> featureRegistryEvent) {
			ModFeatures.registerFeatures(featureRegistryEvent);
		}

		/**
		 * Generic bridge from the mod's own {@link IRTBlockColor} interface to
		 * Forge's {@code BlockColors} system: any registered randomthings block
		 * implementing it gets wired up here automatically, so individual blocks
		 * never need their own {@code ColorHandlerEvent} listener.
		 */
		@SubscribeEvent
		public static void onBlockColorHandler(final ColorHandlerEvent.Block event)
		{
			for (Block block : ForgeRegistries.BLOCKS.getValues())
			{
				if (block instanceof IRTBlockColor && block.getRegistryName() != null && ModConstants.MOD_ID.equals(block.getRegistryName().getNamespace()))
				{
					event.getBlockColors().register((state, worldIn, pos, tintIndex) -> ((IRTBlockColor) block).colorMultiplier(state, worldIn, pos, tintIndex), block);
				}
			}
		}

		/**
		 * Same bridge for item icon tinting: any randomthings {@link BlockItem}
		 * whose block implements {@link IRTBlockColor} gets its inventory icon
		 * tinted using the client player's current world/position, matching the
		 * 1.12.2 behavior of {@code ItemBlockColored}/{@code ItemBlockBiomeStone}.
		 */
		@SubscribeEvent
		public static void onItemColorHandler(final ColorHandlerEvent.Item event)
		{
			for (Item item : ForgeRegistries.ITEMS.getValues())
			{
				if (item instanceof BlockItem && item.getRegistryName() != null && ModConstants.MOD_ID.equals(item.getRegistryName().getNamespace()))
				{
					Block block = ((BlockItem) item).getBlock();

					if (block instanceof IRTBlockColor)
					{
						event.getItemColors().register((stack, tintIndex) -> {
							Minecraft mc = Minecraft.getInstance();

							if (mc.world == null || mc.player == null)
							{
								return 0xFFFFFF;
							}

							return ((IRTBlockColor) block).colorMultiplier(block.getDefaultState(), mc.world, mc.player.getPosition(), tintIndex);
						}, item);
					}
				}
			}
		}
	}
}
