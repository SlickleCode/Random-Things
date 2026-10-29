package lumien.randomthings.debug.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.block.PotionVaporizerBlock;
import lumien.randomthings.handler.EnderLetterHandler;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.tileentity.EntityDetectorTileEntity;
import lumien.randomthings.tileentity.PotionVaporizerTileEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.items.IItemHandler;

/**
 * Third batch of gameplay tests (backlog Q8): Entity Detector, Ender Mailbox delivery, Portkey priming and
 * pickup teleport, Potion Vaporizer. A fake player stands in for the real one.
 */
final class UtilityBehaviorTests
{
	private UtilityBehaviorTests()
	{
	}

	static List<RTTest> create()
	{
		List<RTTest> tests = new ArrayList<>();

		// ---- Entity Detector ------------------------------------------------------------------
		tests.add(RTTest.named("behavior.entity_detector.powers_while_an_entity_is_in_range_and_ignores_far_ones").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.ENTITY_DETECTOR);
			c.spawnItem(2.5, 1.5, 0.5, new ItemStack(Items.COBBLESTONE));
		}).waitTicks(4).run(c -> {
			EntityDetectorTileEntity te = c.tile(c.at(0, 1, 0), EntityDetectorTileEntity.class);
			c.check(te.isPowered(), "an item within range must power the detector (default filter ALL)");
			c.eq(15, ModBlocks.ENTITY_DETECTOR.getWeakPower(c.world.getBlockState(c.at(0, 1, 0)), c.world, c.at(0, 1, 0), Direction.UP), "weak power output");

			for (ItemEntity e : c.itemEntities())
			{
				e.remove();
			}

			c.spawnItem(9.5, 1.5, 0.5, new ItemStack(Items.COBBLESTONE));
		}).waitTicks(4).run(c -> {
			EntityDetectorTileEntity te = c.tile(c.at(0, 1, 0), EntityDetectorTileEntity.class);
			c.check(!te.isPowered(), "an item outside the 5 block range must not power it");
		}));

		tests.add(RTTest.named("behavior.entity_detector.monster_filter_and_invert").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.ENTITY_DETECTOR);
			EntityDetectorTileEntity te = c.tile(c.at(0, 1, 0), EntityDetectorTileEntity.class);
			te.cycleFilter();
			te.cycleFilter();
			te.cycleFilter();
			c.eq(EntityDetectorTileEntity.FILTER.MONSTER, te.getFilter(), "filter after 3 cycles");
			c.spawnItem(2.5, 1.5, 0.5, new ItemStack(Items.COBBLESTONE));
		}).waitTicks(4).run(c -> {
			EntityDetectorTileEntity te = c.tile(c.at(0, 1, 0), EntityDetectorTileEntity.class);
			c.check(!te.isPowered(), "an item is not a monster");
			te.toggleInvert();
		}).waitTicks(4).run(c -> {
			EntityDetectorTileEntity te = c.tile(c.at(0, 1, 0), EntityDetectorTileEntity.class);
			c.check(te.isPowered(), "inverted: no monster in range means powered");
		}));

		tests.add(RTTest.named("behavior.entity_detector.empty_custom_filter_matches_nothing_and_invert_flips_it_like_1_12_2").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.ENTITY_DETECTOR);
			EntityDetectorTileEntity te = c.tile(c.at(0, 1, 0), EntityDetectorTileEntity.class);

			for (int i = 0; i < 6; i++)
			{
				te.cycleFilter();
			}

			c.eq(EntityDetectorTileEntity.FILTER.CUSTOM, te.getFilter(), "filter after 6 cycles");
			c.spawnItem(2.5, 1.5, 0.5, new ItemStack(Items.COBBLESTONE));
		}).waitTicks(4).run(c -> {
			EntityDetectorTileEntity te = c.tile(c.at(0, 1, 0), EntityDetectorTileEntity.class);
			c.check(!te.isPowered(), "empty custom filter matches nothing, so it is off");
			te.toggleInvert();
		}).waitTicks(4).run(c -> {
			EntityDetectorTileEntity te = c.tile(c.at(0, 1, 0), EntityDetectorTileEntity.class);
			c.check(te.isPowered(), "empty custom filter + invert is on (1.12.2 truth table: powered == invert)");
		}));

		// ---- Ender Mailbox --------------------------------------------------------------------
		tests.add(RTTest.named("behavior.loot.rolled_chest_biome_crystals_carry_a_biome").run(c -> {
			net.minecraft.loot.LootTable table = c.world.getServer().getLootTableManager().getLootTableFromLocation(lumien.randomthings.handler.LootHandler.SIMPLE_DUNGEON);
			int crystals = 0;

			for (int i = 0; i < 600; i++)
			{
				for (ItemStack rolled : table.generate(new net.minecraft.loot.LootContext.Builder(c.world).build(net.minecraft.loot.LootParameterSets.EMPTY)))
				{
					if (rolled.getItem() == ModItems.BIOME_CRYSTAL)
					{
						crystals++;
						c.check(lumien.randomthings.item.BiomeCrystalItem.getBiome(rolled) != null, "a Biome Crystal from chest loot has a biome attached (tag " + rolled.getTag() + ")");
					}
				}
			}

			c.check(crystals > 40, "about 20% of 600 rolls give a Biome Crystal (got " + crystals + ")");
		}));

		tests.add(RTTest.named("behavior.loot.vanilla_chest_tables_get_the_mod_pools_injected_on_load").run(c -> {
			java.util.function.BiPredicate<net.minecraft.util.ResourceLocation, String> hasPool = (tableName, poolName) -> c.world.getServer().getLootTableManager().getLootTableFromLocation(tableName).getPool(poolName) != null;

			c.check(hasPool.test(lumien.randomthings.handler.LootHandler.SIMPLE_DUNGEON, "randomthings:summoning_pendulum"), "the simple dungeon chest carries the Summoning Pendulum pool");
			c.check(hasPool.test(lumien.randomthings.handler.LootHandler.VILLAGE_WEAPONSMITH, "randomthings:magic_hood"), "the weaponsmith chest carries the Magic Hood pool");
			c.check(hasPool.test(lumien.randomthings.handler.LootHandler.STRONGHOLD_CORRIDOR, "randomthings:biome_crystal"), "every vanilla chest table gets the Biome Crystal pool");
			c.check(hasPool.test(lumien.randomthings.handler.LootHandler.END_CITY_TREASURE, "randomthings:spectre_coil_number"), "the end city treasure chest carries the Numbered Spectre Coil pool");
		}));

		tests.add(RTTest.named("behavior.biome_radar.right_click_inserts_the_crystal_and_the_client_update_tag_carries_it").run(c -> {
			c.floor();
			BlockPos radar = c.at(0, 1, 0);
			c.set(radar, ModBlocks.BIOME_RADAR);
			ItemStack crystal = new ItemStack(ModItems.BIOME_CRYSTAL);
			lumien.randomthings.item.BiomeCrystalItem.setBiome(crystal, net.minecraftforge.registries.ForgeRegistries.BIOMES.getValue(new net.minecraft.util.ResourceLocation("minecraft:plains")));
			c.player.setHeldItem(Hand.MAIN_HAND, crystal);
			ModBlocks.BIOME_RADAR.onBlockActivated(c.world.getBlockState(radar), c.world, radar, c.player, Hand.MAIN_HAND, new BlockRayTraceResult(Vector3d.copy(radar), Direction.UP, radar, false));
			lumien.randomthings.tileentity.BiomeRadarTileEntity te = c.tile(radar, lumien.randomthings.tileentity.BiomeRadarTileEntity.class);
			c.check(te.getCurrentCrystal().getItem() == ModItems.BIOME_CRYSTAL, "the right-clicked crystal is inside the radar");
			c.check(c.player.getHeldItemMainhand().isEmpty(), "and it left the player's hand");
			// what the client receives (getUpdatePacket carries getUpdateTag; the packet class is client-only on the dedicated server) must hold the crystal, or the radar shows nothing
			lumien.randomthings.tileentity.BiomeRadarTileEntity client = new lumien.randomthings.tileentity.BiomeRadarTileEntity();
			client.read(c.world.getBlockState(radar), te.getUpdateTag());
			c.check(client.getCurrentCrystal().getItem() == ModItems.BIOME_CRYSTAL, "the update tag carries the crystal to the client");
			c.player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
		}));

		tests.add(RTTest.named("behavior.ender_mailbox.sneak_click_with_a_letter_delivers_it_to_the_named_receivers_inbox").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.ENDER_MAILBOX);
			GameProfile profile = c.player.getGameProfile();
			c.world.getServer().getPlayerProfileCache().addEntry(profile);

			ItemStack letter = new ItemStack(ModItems.ENDER_LETTER);
			letter.getOrCreateTag().putString("receiver", profile.getName());
			c.player.setHeldItem(Hand.MAIN_HAND, letter);
			c.player.setSneaking(true);

			BlockPos pos = c.at(0, 1, 0);
			ModBlocks.ENDER_MAILBOX.onBlockActivated(c.world.getBlockState(pos), c.world, pos, c.player, Hand.MAIN_HAND,
					new BlockRayTraceResult(Vector3d.copy(pos), Direction.UP, pos, false));
			c.player.setSneaking(false);

			IItemHandler inbox = EnderLetterHandler.get(c.world).getOrCreateInventoryForPlayer(profile.getId());
			c.eq(1, TestCtx.count(inbox, ModItems.ENDER_LETTER), "letters in the receiver's inbox");
			ItemStack delivered = inbox.getStackInSlot(0);
			c.check(delivered.getTag() != null && delivered.getTag().getBoolean("received"), "delivered letter is marked received");
			c.check(c.player.getHeldItemMainhand().isEmpty(), "the sent letter is consumed from the hand");
			EnderLetterHandler.get(c.world).getOrCreateInventoryForPlayer(profile.getId()).extractItem(0, 1, false);
		}));

		tests.add(RTTest.named("behavior.ender_mailbox.unknown_receiver_keeps_the_letter").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.ENDER_MAILBOX);
			String unknown = "NobodyHasThisName_" + UUID.randomUUID().toString().substring(0, 6);

			if (c.world.getServer().getPlayerProfileCache().getGameProfileForUsername(unknown) != null)
			{
				// An integrated (offline) server resolves any name to an offline profile, so there is no "unknown" receiver here.
				throw new TestCtx.Skip("this server resolves arbitrary names to profiles");
			}

			ItemStack letter = new ItemStack(ModItems.ENDER_LETTER);
			letter.getOrCreateTag().putString("receiver", unknown);
			c.player.setHeldItem(Hand.MAIN_HAND, letter);
			c.player.setSneaking(true);

			BlockPos pos = c.at(0, 1, 0);
			ModBlocks.ENDER_MAILBOX.onBlockActivated(c.world.getBlockState(pos), c.world, pos, c.player, Hand.MAIN_HAND,
					new BlockRayTraceResult(Vector3d.copy(pos), Direction.UP, pos, false));
			c.player.setSneaking(false);

			c.check(!c.player.getHeldItemMainhand().isEmpty(), "the letter must stay in the hand when the receiver is unknown");
			c.player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
		}));

		// ---- Neighbour face culling (1.16: BlockState.isSolid decides whether a neighbour's face is culled against a block) -------------
		tests.add(RTTest.named("behavior.render.see_through_blocks_are_not_solid_so_neighbours_keep_their_faces").run(c -> {
			String[] seeThrough = { "fluid_display", "lapis_glass", "quartz_glass", "trigger_glass", "diaphanous_block", "spectre_block", "super_lubricent_ice", "slime_cube", "biome_radar", "rain_shield", "item_collector" };

			for (String name : seeThrough)
			{
				net.minecraft.block.Block block = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(new net.minecraft.util.ResourceLocation("randomthings", name));

				if (block == null || block == Blocks.AIR)
				{
					continue;
				}

				c.check(!block.getDefaultState().isSolid(), name + " must not be solid: a solid neighbour state makes the next block cull its touching face (a hole)");
			}
		}));

		// ---- Biome Radar search (1.16: world biomes are data-pack instances, not the ForgeRegistries ones; the search used to throw an NPE) ----
		tests.add(RTTest.named("behavior.biome_radar.search_finds_the_crystals_biome_and_a_missing_biome_keeps_searching").run(c -> {
			c.floor();
			BlockPos radar = c.at(0, 1, 0);
			c.set(radar, ModBlocks.BIOME_RADAR);
			int[][] bars = { { 0, 2, 0 }, { 0, 3, 0 }, { 1, 3, 0 }, { -1, 3, 0 }, { 1, 4, 0 }, { -1, 4, 0 }, { 0, 3, 1 }, { 0, 3, -1 }, { 0, 4, 1 }, { 0, 4, -1 } };

			for (int[] b : bars)
			{
				c.set(c.at(b[0], b[1], b[2]), Blocks.IRON_BARS);
			}

			ItemStack crystal = new ItemStack(ModItems.BIOME_CRYSTAL);
			lumien.randomthings.item.BiomeCrystalItem.setBiome(crystal, net.minecraftforge.registries.ForgeRegistries.BIOMES.getValue(new net.minecraft.util.ResourceLocation("minecraft:plains")));
			c.tile(radar, lumien.randomthings.tileentity.BiomeRadarTileEntity.class).setCrystal(crystal);
			c.set(c.at(1, 1, 0), Blocks.REDSTONE_BLOCK);
		}).waitTicks(40).run(c -> {
			lumien.randomthings.tileentity.BiomeRadarTileEntity te = c.tile(c.at(0, 1, 0), lumien.randomthings.tileentity.BiomeRadarTileEntity.class);
			c.eq(lumien.randomthings.tileentity.BiomeRadarTileEntity.STATE.FINISHED, te.getState(), "a plains crystal in a plains world finishes at once");
			c.set(c.at(1, 1, 0), Blocks.AIR);
			ItemStack desert = new ItemStack(ModItems.BIOME_CRYSTAL);
			lumien.randomthings.item.BiomeCrystalItem.setBiome(desert, net.minecraftforge.registries.ForgeRegistries.BIOMES.getValue(new net.minecraft.util.ResourceLocation("minecraft:desert")));
			te.setCrystal(desert);
			c.set(c.at(1, 1, 0), Blocks.REDSTONE_BLOCK);
		}).waitTicks(40).run(c -> {
			lumien.randomthings.tileentity.BiomeRadarTileEntity te = c.tile(c.at(0, 1, 0), lumien.randomthings.tileentity.BiomeRadarTileEntity.class);
			c.eq(lumien.randomthings.tileentity.BiomeRadarTileEntity.STATE.SEARCHING, te.getState(), "a biome that does not exist here keeps the radar searching (no crash)");
		}));

		// ---- Biome Radar: an empty hand takes the crystal back out of a finished radar too (user request 2026-10-23) ----
		tests.add(RTTest.named("behavior.biome_radar.an_empty_hand_takes_the_crystal_back_out_of_a_radar_that_finished_searching").run(c -> {
			c.floor();
			BlockPos radar = c.at(0, 1, 0);
			c.set(radar, ModBlocks.BIOME_RADAR);
			int[][] bars = { { 0, 2, 0 }, { 0, 3, 0 }, { 1, 3, 0 }, { -1, 3, 0 }, { 1, 4, 0 }, { -1, 4, 0 }, { 0, 3, 1 }, { 0, 3, -1 }, { 0, 4, 1 }, { 0, 4, -1 } };

			for (int[] b : bars)
			{
				c.set(c.at(b[0], b[1], b[2]), Blocks.IRON_BARS);
			}

			ItemStack crystal = new ItemStack(ModItems.BIOME_CRYSTAL);
			lumien.randomthings.item.BiomeCrystalItem.setBiome(crystal, net.minecraftforge.registries.ForgeRegistries.BIOMES.getValue(new net.minecraft.util.ResourceLocation("minecraft:plains")));
			c.tile(radar, lumien.randomthings.tileentity.BiomeRadarTileEntity.class).setCrystal(crystal);
			c.set(c.at(1, 1, 0), Blocks.REDSTONE_BLOCK);
		}).waitUntil("the radar to finish", c -> c.tile(c.at(0, 1, 0), lumien.randomthings.tileentity.BiomeRadarTileEntity.class).getState() == lumien.randomthings.tileentity.BiomeRadarTileEntity.STATE.FINISHED, 200).run(c -> {
			BlockPos radar = c.at(0, 1, 0);
			c.player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
			ModBlocks.BIOME_RADAR.onBlockActivated(c.world.getBlockState(radar), c.world, radar, c.player, Hand.MAIN_HAND, new BlockRayTraceResult(Vector3d.copy(radar), Direction.UP, radar, false));
			c.check(c.player.getHeldItemMainhand().getItem() == ModItems.BIOME_CRYSTAL, "the crystal is in the player's hand");
			lumien.randomthings.tileentity.BiomeRadarTileEntity te = c.tile(radar, lumien.randomthings.tileentity.BiomeRadarTileEntity.class);
			c.check(te.getCurrentCrystal().isEmpty(), "the radar is empty");
			c.eq(lumien.randomthings.tileentity.BiomeRadarTileEntity.STATE.IDLE, te.getState(), "and back to idle");
			c.player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
		}));

		// ---- Enchant glint colour (Dyeing Machine glint result and the per-item tints) ---------------------------
		tests.add(RTTest.named("behavior.glint.colour_priority_matches_1_12_2").run(c -> {
			int vanilla = lumien.randomthings.asm.GlintColors.VANILLA;
			net.minecraft.item.Item tool = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.util.ResourceLocation("randomthings", "redstone_tool"));
			net.minecraft.item.Item rope = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.util.ResourceLocation("randomthings", "escape_rope"));
			c.eq(vanilla, lumien.randomthings.asm.GlintColors.colorFor(ItemStack.EMPTY, vanilla), "empty stack keeps vanilla");
			c.eq(vanilla, lumien.randomthings.asm.GlintColors.colorFor(new ItemStack(Items.DIAMOND_SWORD), vanilla), "an ordinary item keeps vanilla");
			ItemStack tagged = new ItemStack(Items.IRON_HELMET);
			tagged.getOrCreateTag().putInt("enchantmentColor", 0x3C44AA);
			c.eq(0xFF3C44AA, lumien.randomthings.asm.GlintColors.colorFor(tagged, vanilla), "enchantmentColor tag becomes an opaque colour");
			c.eq(0xFFB20000, lumien.randomthings.asm.GlintColors.colorFor(new ItemStack(tool), vanilla), "Redstone Tool: dark red");
			c.eq(0xFF00B2B2, lumien.randomthings.asm.GlintColors.colorFor(new ItemStack(ModItems.SPECTRE_KEY), vanilla), "Spectre Key: dark cyan");
			c.eq(0xFFB200B2, lumien.randomthings.asm.GlintColors.colorFor(new ItemStack(ModItems.PORTKEY), vanilla), "Portkey: dark magenta");
			c.eq(0xFF7D7D7D, lumien.randomthings.asm.GlintColors.colorFor(new ItemStack(ModItems.SPECTRE_SWORD), vanilla), "Spectre Sword: grey");
			c.eq(0xFFB2B200, lumien.randomthings.asm.GlintColors.colorFor(new ItemStack(rope), vanilla), "Escape Rope: dark yellow");
			ItemStack taggedTool = new ItemStack(tool);
			taggedTool.getOrCreateTag().putInt("enchantmentColor", 0x3C44AA);
			c.eq(0xFFB20000, lumien.randomthings.asm.GlintColors.colorFor(taggedTool, vanilla), "the Redstone Tool's fixed tint beats the tag");
			ItemStack taggedSword = new ItemStack(ModItems.SPECTRE_SWORD);
			taggedSword.getOrCreateTag().putInt("enchantmentColor", 0x3C44AA);
			c.eq(0xFF3C44AA, lumien.randomthings.asm.GlintColors.colorFor(taggedSword, vanilla), "the tag beats the Spectre Sword's tint");
		}));

		// ---- Portkey --------------------------------------------------------------------------
		Vector3d[] portkeyHome = new Vector3d[1];
		tests.add(RTTest.named("behavior.portkey.binds_primes_after_100_ticks_and_teleports_the_picker_upper").run(c -> {
			c.floor();
			ItemStack key = new ItemStack(ModItems.PORTKEY);
			c.player.setHeldItem(Hand.MAIN_HAND, key);
			BlockPos target = c.at(8, 0, 0);
			ModItems.PORTKEY.onItemUse(new ItemUseContext(c.player, Hand.MAIN_HAND, new BlockRayTraceResult(Vector3d.copy(target), Direction.UP, target, false)));
			CompoundNBT tag = key.getTag();
			c.check(tag != null && tag.getBoolean("hasTarget"), "right-click must bind a target");
			c.eq(target.getX(), tag.getInt("targetX"), "bound x");
			c.check(((lumien.randomthings.item.PortkeyItem) ModItems.PORTKEY).hasEffect(key), "a freshly bound Portkey still glows (priming counter 0)");
			c.check(((lumien.randomthings.item.PortkeyItem) ModItems.PORTKEY).hasEffect(new ItemStack(ModItems.PORTKEY)), "an unbound Portkey always glows");
			c.player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
			portkeyHome[0] = c.player.getPositionVec();
			ItemEntity dropped = c.spawnItem(0.5, 2.5, 0.5, key.copy());
			dropped.setPickupDelay(32767);
		}).waitTicks(60).run(c -> {
			ItemEntity drop = c.itemEntities().get(0);
			c.check(drop.getItem().getTag().getInt("dropCounter") < 100, "not primed yet after about 60 ticks");
			c.check(((lumien.randomthings.item.PortkeyItem) ModItems.PORTKEY).hasEffect(drop.getItem()), "still glowing while the counter is below 100");
			c.player.setPosition(c.origin.getX() + 0.5, c.origin.getY() + 1, c.origin.getZ() + 0.5);
			MinecraftForge.EVENT_BUS.post(new EntityItemPickupEvent(c.player, drop));
			c.check(c.player.getPosX() < c.origin.getX() + 3, "an unprimed Portkey must not teleport the picker-upper (x=" + c.player.getPosX() + ")");
		}).waitTicks(60).run(c -> {
			ItemEntity drop = c.itemEntities().get(0);
			c.check(drop.getItem().getTag().getInt("dropCounter") > 100, "primed after more than 100 ticks");
			c.check(!((lumien.randomthings.item.PortkeyItem) ModItems.PORTKEY).hasEffect(drop.getItem()), "the glow stops once the counter has passed 100 (primed and bound)");

			if (c.player.connection == null || c.player.connection.getClass() != ServerPlayNetHandler.class)
			{
				// The teleport goes through the player's network handler; a fake player has none unless we give it a detached one.
				new ServerPlayNetHandler(c.world.getServer(), new NetworkManager(PacketDirection.SERVERBOUND), c.player);
			}

			c.player.setPosition(c.origin.getX() + 0.5, c.origin.getY() + 1, c.origin.getZ() + 0.5);
			MinecraftForge.EVENT_BUS.post(new EntityItemPickupEvent(c.player, drop));
			c.check(Math.abs(c.player.getPosX() - (c.origin.getX() + 8)) <= 3.0, "picker-upper must land near the bound target (x=" + c.player.getPosX() + ")");
			boolean nearSurface = Math.abs(c.player.getPosY() - (c.origin.getY() + 1)) <= 1.5;
			double landedY = c.player.getPosY();
			c.player.setPosition(portkeyHome[0].x, portkeyHome[0].y, portkeyHome[0].z);
			c.check(nearSurface, "and on the surface (y=" + landedY + ")");
		}));

		// ---- Potion Vaporizer -----------------------------------------------------------------
		tests.add(RTTest.named("behavior.potion_vaporizer.applies_its_potion_to_mobs_in_the_sealed_room_and_returns_a_bottle").run(c -> {
			c.floor();

			// 3x3x3 sealed room (interior x,z in -1..1, y in 1..3), vaporizer built into the west wall facing in.
			for (int x = -2; x <= 2; x++)
			{
				for (int z = -2; z <= 2; z++)
				{
					for (int y = 1; y <= 4; y++)
					{
						boolean wall = Math.abs(x) == 2 || Math.abs(z) == 2 || y == 4;

						if (wall)
						{
							c.setQuiet(c.at(x, y, z), Blocks.STONE.getDefaultState());
						}
					}
				}
			}

			c.setQuiet(c.at(-2, 2, 0), ModBlocks.POTION_VAPORIZER.getDefaultState().with(PotionVaporizerBlock.FACING, Direction.EAST));
			PotionVaporizerTileEntity te = c.tile(c.at(-2, 2, 0), PotionVaporizerTileEntity.class);
			te.itemHandler().setStackInSlot(0, new ItemStack(Items.COAL, 4));
			te.itemHandler().setStackInSlot(1, PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.SWIFTNESS));

			PigEntity pig = EntityType.PIG.create(c.world);
			pig.setNoAI(true);
			pig.setPosition(c.origin.getX() + 0.5, c.origin.getY() + 1, c.origin.getZ() + 0.5);
			c.world.addEntity(pig);
		}).waitTicks(160).run(c -> {
			PotionVaporizerTileEntity te = c.tile(c.at(-2, 2, 0), PotionVaporizerTileEntity.class);
			PigEntity pig = c.world.getEntitiesWithinAABB(PigEntity.class, c.area()).get(0);
			c.check(pig.getActivePotionEffect(Effects.SPEED) != null, "a mob in the room must receive the potion effect");
			c.eq(Items.GLASS_BOTTLE, te.itemHandler().getStackInSlot(2).getItem(), "an empty bottle is returned in the output slot");
			c.check(te.itemHandler().getStackInSlot(1).isEmpty(), "the potion was loaded into the tank");
		}));

		// ---- Imbue potions --------------------------------------------------------------------
		tests.add(RTTest.named("behavior.imbue.drinking_applies_a_20_minute_effect_with_icon_and_no_particles_and_replaces_the_previous_one").run(c -> {
			c.player.clearActivePotions();
			ModItems.IMBUE_FIRE.onItemUseFinish(new ItemStack(ModItems.IMBUE_FIRE), c.world, c.player);
			net.minecraft.potion.EffectInstance fire = c.player.getActivePotionEffect(lumien.randomthings.potion.ModEffects.IMBUE_FIRE);
			c.check(fire != null, "drinking Fire Imbue must give the Fire Imbue effect");
			c.check(fire.getDuration() >= 24000 - 5, "the effect lasts 20 minutes (24000 ticks), got " + fire.getDuration());
			c.check(fire.isShowIcon(), "the effect must show its HUD/inventory icon (1.12.2 did; the 5-argument constructor hid it)");
			c.check(!fire.doesShowParticles(), "the effect must not show particles (like 1.12.2)");
			ModItems.IMBUE_POISON.onItemUseFinish(new ItemStack(ModItems.IMBUE_POISON), c.world, c.player);
			c.check(c.player.getActivePotionEffect(lumien.randomthings.potion.ModEffects.IMBUE_FIRE) == null, "drinking a second kind replaces the first");
			c.check(c.player.getActivePotionEffect(lumien.randomthings.potion.ModEffects.IMBUE_POISON) != null, "the second kind is active");
			c.player.clearActivePotions();
		}));

		// ---- Static registries (thread safety) --------------------------------------------------
		tests.add(RTTest.named("registry.weak_sets_survive_concurrent_add_remove_and_iteration").run(c -> {
			java.util.Set<Object> set = lumien.randomthings.util.WeakSets.create();
			Object[] keep = new Object[200];

			for (int i = 0; i < keep.length; i++)
			{
				keep[i] = new Object();
			}

			Throwable[] failure = new Throwable[1];
			Thread writer = new Thread(() -> {
				try
				{
					for (int round = 0; round < 2000; round++)
					{
						for (Object o : keep)
						{
							set.add(o);
						}

						for (Object o : keep)
						{
							set.remove(o);
						}
					}
				}
				catch (Throwable t)
				{
					failure[0] = t;
				}
			});
			writer.start();

			while (writer.isAlive())
			{
				for (Object o : lumien.randomthings.util.WeakSets.snapshot(set))
				{
					o.hashCode();
				}
			}

			c.check(failure[0] == null, "writer thread failed: " + failure[0]);
		}));

		return tests;
	}
}
