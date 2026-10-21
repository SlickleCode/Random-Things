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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.Items;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
			c.eq(15, ModBlocks.ENTITY_DETECTOR.getSignal(c.world.getBlockState(c.at(0, 1, 0)), c.world, c.at(0, 1, 0), Direction.UP), "weak power output");

			for (ItemEntity e : c.itemEntities())
			{
				e.discard();
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

		// ---- Ender Mailbox --------------------------------------------------------------------
		tests.add(RTTest.named("behavior.loot.rolled_chest_biome_crystals_carry_a_biome").run(c -> {
			net.minecraft.world.level.storage.loot.LootTable table = c.world.getServer().getLootTables().get(lumien.randomthings.handler.LootHandler.SIMPLE_DUNGEON);
			int crystals = 0;

			for (int i = 0; i < 600; i++)
			{
				for (ItemStack rolled : table.getRandomItems(new net.minecraft.world.level.storage.loot.LootContext.Builder(c.world).create(net.minecraft.world.level.storage.loot.parameters.LootContextParamSets.EMPTY)))
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
			java.util.function.BiPredicate<net.minecraft.resources.ResourceLocation, String> hasPool = (tableName, poolName) -> c.world.getServer().getLootTables().get(tableName).getPool(poolName) != null;

			c.check(hasPool.test(lumien.randomthings.handler.LootHandler.SIMPLE_DUNGEON, "randomthings:summoning_pendulum"), "the simple dungeon chest carries the Summoning Pendulum pool");
			c.check(hasPool.test(lumien.randomthings.handler.LootHandler.VILLAGE_WEAPONSMITH, "randomthings:magic_hood"), "the weaponsmith chest carries the Magic Hood pool");
			c.check(hasPool.test(lumien.randomthings.handler.LootHandler.STRONGHOLD_CORRIDOR, "randomthings:biome_crystal"), "every vanilla chest table gets the Biome Crystal pool");
		}));

		tests.add(RTTest.named("behavior.biome_radar.right_click_inserts_the_crystal_and_the_client_update_tag_carries_it").run(c -> {
			c.floor();
			BlockPos radar = c.at(0, 1, 0);
			c.set(radar, ModBlocks.BIOME_RADAR);
			ItemStack crystal = new ItemStack(ModItems.BIOME_CRYSTAL);
			lumien.randomthings.item.BiomeCrystalItem.setBiome(crystal, net.minecraftforge.registries.ForgeRegistries.BIOMES.getValue(new net.minecraft.resources.ResourceLocation("minecraft:plains")));
			c.player.setItemInHand(InteractionHand.MAIN_HAND, crystal);
			ModBlocks.BIOME_RADAR.use(c.world.getBlockState(radar), c.world, radar, c.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atLowerCornerOf(radar), Direction.UP, radar, false));
			lumien.randomthings.tileentity.BiomeRadarTileEntity te = c.tile(radar, lumien.randomthings.tileentity.BiomeRadarTileEntity.class);
			c.check(te.getCurrentCrystal().getItem() == ModItems.BIOME_CRYSTAL, "the right-clicked crystal is inside the radar");
			c.check(c.player.getMainHandItem().isEmpty(), "and it left the player's hand");
			// what the client receives (getUpdatePacket) must hold the crystal, or the radar shows nothing
			lumien.randomthings.tileentity.BiomeRadarTileEntity client = new lumien.randomthings.tileentity.BiomeRadarTileEntity(radar, c.world.getBlockState(radar));
			client.onDataPacket(null, te.getUpdatePacket());
			c.check(client.getCurrentCrystal().getItem() == ModItems.BIOME_CRYSTAL, "the update packet carries the crystal to the client");
			c.player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		}));

		tests.add(RTTest.named("behavior.ender_mailbox.sneak_click_with_a_letter_delivers_it_to_the_named_receivers_inbox").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.ENDER_MAILBOX);
			GameProfile profile = c.player.getGameProfile();
			c.world.getServer().getProfileCache().add(profile);

			ItemStack letter = new ItemStack(ModItems.ENDER_LETTER);
			letter.getOrCreateTag().putString("receiver", profile.getName());
			c.player.setItemInHand(InteractionHand.MAIN_HAND, letter);
			c.player.setShiftKeyDown(true);

			BlockPos pos = c.at(0, 1, 0);
			ModBlocks.ENDER_MAILBOX.use(c.world.getBlockState(pos), c.world, pos, c.player, InteractionHand.MAIN_HAND,
					new BlockHitResult(Vec3.atLowerCornerOf(pos), Direction.UP, pos, false));
			c.player.setShiftKeyDown(false);

			IItemHandler inbox = EnderLetterHandler.get(c.world).getOrCreateInventoryForPlayer(profile.getId());
			c.eq(1, TestCtx.count(inbox, ModItems.ENDER_LETTER), "letters in the receiver's inbox");
			ItemStack delivered = inbox.getStackInSlot(0);
			c.check(delivered.getTag() != null && delivered.getTag().getBoolean("received"), "delivered letter is marked received");
			c.check(c.player.getMainHandItem().isEmpty(), "the sent letter is consumed from the hand");
			EnderLetterHandler.get(c.world).getOrCreateInventoryForPlayer(profile.getId()).extractItem(0, 1, false);
		}));

		tests.add(RTTest.named("behavior.ender_mailbox.unknown_receiver_keeps_the_letter").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.ENDER_MAILBOX);
			String unknown = "NobodyHasThisName_" + UUID.randomUUID().toString().substring(0, 6);

			if (c.world.getServer().getProfileCache().get(unknown) != null)
			{
				// An integrated (offline) server resolves any name to an offline profile, so there is no "unknown" receiver here.
				throw new TestCtx.Skip("this server resolves arbitrary names to profiles");
			}

			ItemStack letter = new ItemStack(ModItems.ENDER_LETTER);
			letter.getOrCreateTag().putString("receiver", unknown);
			c.player.setItemInHand(InteractionHand.MAIN_HAND, letter);
			c.player.setShiftKeyDown(true);

			BlockPos pos = c.at(0, 1, 0);
			ModBlocks.ENDER_MAILBOX.use(c.world.getBlockState(pos), c.world, pos, c.player, InteractionHand.MAIN_HAND,
					new BlockHitResult(Vec3.atLowerCornerOf(pos), Direction.UP, pos, false));
			c.player.setShiftKeyDown(false);

			c.check(!c.player.getMainHandItem().isEmpty(), "the letter must stay in the hand when the receiver is unknown");
			c.player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
		}));

		// ---- Neighbour face culling (1.16: BlockState.isSolid decides whether a neighbour's face is culled against a block) -------------
		tests.add(RTTest.named("behavior.render.see_through_blocks_are_not_solid_so_neighbours_keep_their_faces").run(c -> {
			String[] seeThrough = { "fluid_display", "lapis_glass", "quartz_glass", "trigger_glass", "diaphanous_block", "spectre_block", "super_lubricent_ice", "slime_cube", "biome_radar", "rain_shield", "item_collector" };

			for (String name : seeThrough)
			{
				net.minecraft.world.level.block.Block block = net.minecraftforge.registries.ForgeRegistries.BLOCKS.getValue(new net.minecraft.resources.ResourceLocation("randomthings", name));

				if (block == null || block == Blocks.AIR)
				{
					continue;
				}

				c.check(!block.defaultBlockState().canOcclude(), name + " must not be solid: a solid neighbour state makes the next block cull its touching face (a hole)");
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
			lumien.randomthings.item.BiomeCrystalItem.setBiome(crystal, net.minecraftforge.registries.ForgeRegistries.BIOMES.getValue(new net.minecraft.resources.ResourceLocation("minecraft:plains")));
			c.tile(radar, lumien.randomthings.tileentity.BiomeRadarTileEntity.class).setCrystal(crystal);
			c.set(c.at(1, 1, 0), Blocks.REDSTONE_BLOCK);
		}).waitTicks(40).run(c -> {
			lumien.randomthings.tileentity.BiomeRadarTileEntity te = c.tile(c.at(0, 1, 0), lumien.randomthings.tileentity.BiomeRadarTileEntity.class);
			c.eq(lumien.randomthings.tileentity.BiomeRadarTileEntity.STATE.FINISHED, te.getState(), "a plains crystal in a plains world finishes at once");
			c.set(c.at(1, 1, 0), Blocks.AIR);
			ItemStack desert = new ItemStack(ModItems.BIOME_CRYSTAL);
			lumien.randomthings.item.BiomeCrystalItem.setBiome(desert, net.minecraftforge.registries.ForgeRegistries.BIOMES.getValue(new net.minecraft.resources.ResourceLocation("minecraft:desert")));
			te.setCrystal(desert);
			c.set(c.at(1, 1, 0), Blocks.REDSTONE_BLOCK);
		}).waitTicks(40).run(c -> {
			lumien.randomthings.tileentity.BiomeRadarTileEntity te = c.tile(c.at(0, 1, 0), lumien.randomthings.tileentity.BiomeRadarTileEntity.class);
			c.eq(lumien.randomthings.tileentity.BiomeRadarTileEntity.STATE.SEARCHING, te.getState(), "a biome that does not exist here keeps the radar searching (no crash)");
		}));

		// ---- Enchant glint colour (Dyeing Machine glint result and the per-item tints) ---------------------------
		tests.add(RTTest.named("behavior.glint.colour_priority_matches_1_12_2").run(c -> {
			int vanilla = lumien.randomthings.asm.GlintColors.VANILLA;
			net.minecraft.world.item.Item tool = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation("randomthings", "redstone_tool"));
			net.minecraft.world.item.Item rope = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation("randomthings", "escape_rope"));
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
		Vec3[] portkeyHome = new Vec3[1];
		tests.add(RTTest.named("behavior.portkey.binds_primes_after_100_ticks_and_teleports_the_picker_upper").run(c -> {
			c.floor();
			ItemStack key = new ItemStack(ModItems.PORTKEY);
			c.player.setItemInHand(InteractionHand.MAIN_HAND, key);
			BlockPos target = c.at(8, 0, 0);
			ModItems.PORTKEY.useOn(new UseOnContext(c.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atLowerCornerOf(target), Direction.UP, target, false)));
			CompoundTag tag = key.getTag();
			c.check(tag != null && tag.getBoolean("hasTarget"), "right-click must bind a target");
			c.eq(target.getX(), tag.getInt("targetX"), "bound x");
			c.check(((lumien.randomthings.item.PortkeyItem) ModItems.PORTKEY).isFoil(key), "a freshly bound Portkey still glows (priming counter 0)");
			c.check(((lumien.randomthings.item.PortkeyItem) ModItems.PORTKEY).isFoil(new ItemStack(ModItems.PORTKEY)), "an unbound Portkey always glows");
			c.player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
			portkeyHome[0] = c.player.position();
			ItemEntity dropped = c.spawnItem(0.5, 2.5, 0.5, key.copy());
			dropped.setPickUpDelay(32767);
		}).waitTicks(60).run(c -> {
			ItemEntity drop = c.itemEntities().get(0);
			c.check(drop.getItem().getTag().getInt("dropCounter") < 100, "not primed yet after about 60 ticks");
			c.check(((lumien.randomthings.item.PortkeyItem) ModItems.PORTKEY).isFoil(drop.getItem()), "still glowing while the counter is below 100");
			c.player.setPos(c.origin.getX() + 0.5, c.origin.getY() + 1, c.origin.getZ() + 0.5);
			MinecraftForge.EVENT_BUS.post(new EntityItemPickupEvent(c.player, drop));
			c.check(c.player.getX() < c.origin.getX() + 3, "an unprimed Portkey must not teleport the picker-upper (x=" + c.player.getX() + ")");
		}).waitTicks(60).run(c -> {
			ItemEntity drop = c.itemEntities().get(0);
			c.check(drop.getItem().getTag().getInt("dropCounter") > 100, "primed after more than 100 ticks");
			c.check(!((lumien.randomthings.item.PortkeyItem) ModItems.PORTKEY).isFoil(drop.getItem()), "the glow stops once the counter has passed 100 (primed and bound)");

			if (c.player.connection == null || c.player.connection.getClass() != ServerGamePacketListenerImpl.class)
			{
				// The teleport goes through the player's network handler; a fake player has none unless we give it a detached one.
				new ServerGamePacketListenerImpl(c.world.getServer(), new Connection(PacketFlow.SERVERBOUND), c.player);
			}

			c.player.setPos(c.origin.getX() + 0.5, c.origin.getY() + 1, c.origin.getZ() + 0.5);
			MinecraftForge.EVENT_BUS.post(new EntityItemPickupEvent(c.player, drop));
			c.check(Math.abs(c.player.getX() - (c.origin.getX() + 8)) <= 3.0, "picker-upper must land near the bound target (x=" + c.player.getX() + ")");
			boolean nearSurface = Math.abs(c.player.getY() - (c.origin.getY() + 1)) <= 1.5;
			double landedY = c.player.getY();
			c.player.setPos(portkeyHome[0].x, portkeyHome[0].y, portkeyHome[0].z);
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
							c.setQuiet(c.at(x, y, z), Blocks.STONE.defaultBlockState());
						}
					}
				}
			}

			c.setQuiet(c.at(-2, 2, 0), ModBlocks.POTION_VAPORIZER.defaultBlockState().setValue(PotionVaporizerBlock.FACING, Direction.EAST));
			PotionVaporizerTileEntity te = c.tile(c.at(-2, 2, 0), PotionVaporizerTileEntity.class);
			te.itemHandler().setStackInSlot(0, new ItemStack(Items.COAL, 4));
			te.itemHandler().setStackInSlot(1, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.SWIFTNESS));

			Pig pig = EntityType.PIG.create(c.world);
			pig.setNoAi(true);
			pig.setPos(c.origin.getX() + 0.5, c.origin.getY() + 1, c.origin.getZ() + 0.5);
			c.world.addFreshEntity(pig);
		}).waitTicks(160).run(c -> {
			PotionVaporizerTileEntity te = c.tile(c.at(-2, 2, 0), PotionVaporizerTileEntity.class);
			Pig pig = c.world.getEntitiesOfClass(Pig.class, c.area()).get(0);
			c.check(pig.getEffect(MobEffects.MOVEMENT_SPEED) != null, "a mob in the room must receive the potion effect");
			c.eq(Items.GLASS_BOTTLE, te.itemHandler().getStackInSlot(2).getItem(), "an empty bottle is returned in the output slot");
			c.check(te.itemHandler().getStackInSlot(1).isEmpty(), "the potion was loaded into the tank");
		}));

		// ---- Imbue potions --------------------------------------------------------------------
		tests.add(RTTest.named("behavior.imbue.drinking_applies_a_20_minute_effect_with_icon_and_no_particles_and_replaces_the_previous_one").run(c -> {
			c.player.removeAllEffects();
			ModItems.IMBUE_FIRE.finishUsingItem(new ItemStack(ModItems.IMBUE_FIRE), c.world, c.player);
			net.minecraft.world.effect.MobEffectInstance fire = c.player.getEffect(lumien.randomthings.potion.ModEffects.IMBUE_FIRE);
			c.check(fire != null, "drinking Fire Imbue must give the Fire Imbue effect");
			c.check(fire.getDuration() >= 24000 - 5, "the effect lasts 20 minutes (24000 ticks), got " + fire.getDuration());
			c.check(fire.showIcon(), "the effect must show its HUD/inventory icon (1.12.2 did; the 5-argument constructor hid it)");
			c.check(!fire.isVisible(), "the effect must not show particles (like 1.12.2)");
			ModItems.IMBUE_POISON.finishUsingItem(new ItemStack(ModItems.IMBUE_POISON), c.world, c.player);
			c.check(c.player.getEffect(lumien.randomthings.potion.ModEffects.IMBUE_FIRE) == null, "drinking a second kind replaces the first");
			c.check(c.player.getEffect(lumien.randomthings.potion.ModEffects.IMBUE_POISON) != null, "the second kind is active");
			c.player.removeAllEffects();
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
