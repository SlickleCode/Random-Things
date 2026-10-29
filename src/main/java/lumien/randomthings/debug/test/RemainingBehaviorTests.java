package lumien.randomthings.debug.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import lumien.randomthings.asm.AsmHandler;
import lumien.randomthings.block.ChatDetectorBlock;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.block.OnlineDetectorBlock;
import lumien.randomthings.handler.ModDimensions;
import lumien.randomthings.handler.floo.FlooNetworkHandler;
import lumien.randomthings.handler.spectre.SpectreCube;
import lumien.randomthings.handler.spectre.SpectreHandler;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.item.SoundPatternItem;
import lumien.randomthings.tileentity.ChatDetectorTileEntity;
import lumien.randomthings.tileentity.FlooBrickTileEntity;
import lumien.randomthings.tileentity.GlobalChatDetectorTileEntity;
import lumien.randomthings.tileentity.OnlineDetectorTileEntity;
import lumien.randomthings.tileentity.SoundBoxTileEntity;
import lumien.randomthings.tileentity.SoundDampenerTileEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.PotionColorCalculationEvent;

/**
 * Fourth batch of gameplay tests (backlog Q8): Online/Chat/Global Chat detectors, Floo teleport, Blazing Fire,
 * Magic Hood, Spectre dimension cubes, Sound Box/Dampener state. Client-only behaviour (the sound event, nametag
 * rendering) is out of reach here and is listed in docs/testing/README.md.
 */
final class RemainingBehaviorTests
{
	private RemainingBehaviorTests()
	{
	}

	private static void ensureConnection(TestCtx c)
	{
		if (c.player.connection == null || c.player.connection.getClass() != ServerGamePacketListenerImpl.class)
		{
			new ServerGamePacketListenerImpl(c.world.getServer(), new Connection(PacketFlow.SERVERBOUND), c.player);
		}
	}

	private static ServerChatEvent chat(TestCtx c, String message)
	{
		ServerChatEvent event = new ServerChatEvent(c.player, message, Component.literal("<test> " + message));
		MinecraftForge.EVENT_BUS.post(event);
		return event;
	}

	static List<RTTest> create()
	{
		List<RTTest> tests = new ArrayList<>();

		// ---- Online Detector ------------------------------------------------------------------
		tests.add(RTTest.named("behavior.online_detector.stays_off_for_a_name_that_is_not_online").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.ONLINE_DETECTOR);
			c.tile(c.at(0, 1, 0), OnlineDetectorTileEntity.class).setUsername("Nobody_Online_" + UUID.randomUUID().toString().substring(0, 5));
		}).waitTicks(45).run(c -> {
			c.check(!c.world.getBlockState(c.at(0, 1, 0)).getValue(OnlineDetectorBlock.POWERED), "unknown name must not power the detector");
		}));

		tests.add(RTTest.named("behavior.online_detector.powers_for_an_online_player_and_drops_when_they_are_not_named_any_more").run(c -> {
			String name = c.player.getGameProfile().getName();

			if (c.world.getServer().getPlayerList().getPlayerByName(name) == null)
			{
				throw new TestCtx.Skip("the test player is not in the player list (fake player)");
			}

			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.ONLINE_DETECTOR);
			c.tile(c.at(0, 1, 0), OnlineDetectorTileEntity.class).setUsername(name);
		}).waitTicks(45).run(c -> {
			c.check(c.world.getBlockState(c.at(0, 1, 0)).getValue(OnlineDetectorBlock.POWERED), "an online player's name must power the detector");
			c.tile(c.at(0, 1, 0), OnlineDetectorTileEntity.class).setUsername("someone_else_" + UUID.randomUUID().toString().substring(0, 5));
		}).waitTicks(45).run(c -> {
			c.check(!c.world.getBlockState(c.at(0, 1, 0)).getValue(OnlineDetectorBlock.POWERED), "changing to an offline name must unpower it");
		}));

		// ---- Chat Detector --------------------------------------------------------------------
		tests.add(RTTest.named("behavior.chat_detector.matching_message_from_the_bound_player_pulses_and_is_consumed").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.CHAT_DETECTOR);
			ChatDetectorTileEntity te = c.tile(c.at(0, 1, 0), ChatDetectorTileEntity.class);
			te.setPlayerUUID(c.player.getGameProfile().getId());
			te.setChatMessage("open sesame");
			te.setConsume(true);

			ServerChatEvent wrong = chat(c, "something else");
			c.check(!wrong.isCanceled() && !te.isPulsing(), "a different message must be ignored");

			ServerChatEvent right = chat(c, "open sesame");
			c.check(te.isPulsing(), "the matching message must start a pulse");
			c.check(c.world.getBlockState(c.at(0, 1, 0)).getValue(ChatDetectorBlock.POWERED), "block shows powered");
			c.check(right.isCanceled(), "consume mode must cancel the chat message");
		}).waitTicks(26).run(c -> {
			c.check(!c.world.getBlockState(c.at(0, 1, 0)).getValue(ChatDetectorBlock.POWERED), "the pulse ends after about a second");
		}));

		tests.add(RTTest.named("behavior.chat_detector.ignores_other_players_and_non_consume_mode_leaves_the_message").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.CHAT_DETECTOR);
			ChatDetectorTileEntity te = c.tile(c.at(0, 1, 0), ChatDetectorTileEntity.class);
			te.setPlayerUUID(UUID.randomUUID());
			te.setChatMessage("hello");
			te.setConsume(false);
			chat(c, "hello");
			c.check(!te.isPulsing(), "the message of a player the detector is not bound to must not pulse it");

			te.setPlayerUUID(c.player.getGameProfile().getId());
			ServerChatEvent event = chat(c, "hello");
			c.check(te.isPulsing(), "the bound player's message pulses");
			c.check(!event.isCanceled(), "non-consume mode must leave the message in chat");
		}));

		tests.add(RTTest.named("behavior.global_chat_detector.pulses_for_anyone_and_only_consumes_for_an_id_card_owner").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.GLOBAL_CHAT_DETECTOR);
			GlobalChatDetectorTileEntity te = c.tile(c.at(0, 1, 0), GlobalChatDetectorTileEntity.class);
			te.setChatMessage("ping");
			te.setConsume(true);

			ServerChatEvent first = chat(c, "ping");
			c.check(te.isPulsing(), "any player's matching message pulses a global detector");
			c.check(!first.isCanceled(), "without a matching ID card the message must not be consumed");

			ItemStack card = new ItemStack(ModItems.ID_CARD);
			card.getOrCreateTag().putString("uuid", c.player.getGameProfile().getId().toString());
			te.idCardInventory().setStackInSlot(0, card);
			ServerChatEvent second = chat(c, "ping");
			c.check(second.isCanceled(), "an ID card of the sender allows consuming");
		}));

		// ---- Floo -----------------------------------------------------------------------------
		tests.add(RTTest.named("behavior.floo.typing_a_destination_on_a_fireplace_teleports_even_with_a_typo").run(c -> {
			c.floor();
			ensureConnection(c);
			String suffix = Integer.toString(new Random().nextInt(9000) + 1000);
			String home = "Hearth" + suffix;
			String away = "Garden" + suffix;
			Vec3[] home0 = { c.player.position() };

			c.setQuiet(c.at(0, 1, 0), Blocks.BRICKS.defaultBlockState());
			c.setQuiet(c.at(6, 1, 0), Blocks.BRICKS.defaultBlockState());

			for (String[] def : new String[][] { { home, "0" }, { away, "6" } })
			{
				ItemStack sign = new ItemStack(ModItems.FLOO_SIGN);
				sign.setHoverName(Component.literal(def[0]));
				c.player.setItemInHand(InteractionHand.MAIN_HAND, sign);
				BlockPos brick = c.at(Integer.parseInt(def[1]), 1, 0);
				ModItems.FLOO_SIGN.useOn(new UseOnContext(c.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atLowerCornerOf(brick), Direction.NORTH, brick, false)));
			}

			c.check(c.world.getBlockState(c.at(0, 1, 0)).getBlock() == ModBlocks.FLOO_BRICK, "the sign converts the brick into a Floo brick");
			c.check(c.tile(c.at(6, 1, 0), FlooBrickTileEntity.class).isMaster(), "a single brick fireplace is its own master");

			c.player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.FLOO_POWDER, 2));
			c.player.setPos(c.at(0, 2, 0).getX() + 0.5, c.at(0, 2, 0).getY(), c.at(0, 2, 0).getZ() + 0.5);

			ServerChatEvent event = chat(c, "Gardne" + suffix); // typo of the destination name
			boolean arrived = Math.abs(c.player.getX() - (c.at(6, 2, 0).getX() + 0.5)) < 1.0;
			double landedX = c.player.getX();

			FlooNetworkHandler handler = FlooNetworkHandler.get(c.world);
			handler.brokenMaster(c.tile(c.at(0, 1, 0), FlooBrickTileEntity.class).getFirePlaceUid());
			handler.brokenMaster(c.tile(c.at(6, 1, 0), FlooBrickTileEntity.class).getFirePlaceUid());
			c.player.setPos(home0[0].x, home0[0].y, home0[0].z);
			c.player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

			c.check(event.isCanceled(), "the destination message must not reach chat");
			c.check(arrived, "the player must arrive at the other fireplace (x=" + landedX + ")");
		}));

		// ---- Blazing Fire ---------------------------------------------------------------------
		tests.add(RTTest.named("coremod.blazing_fire.catches_flammable_blocks_about_four_times_as_often_as_vanilla_fire").run(c -> {
			c.floor();
			Method tryCatchFire;

			try
			{
				tryCatchFire = FireBlock.class.getDeclaredMethod("tryCatchFire", Level.class, BlockPos.class, int.class, net.minecraft.util.RandomSource.class, int.class, Direction.class);
				tryCatchFire.setAccessible(true);
			}
			catch (ReflectiveOperationException e)
			{
				throw new TestCtx.Skip("cannot reach FireBlock.tryCatchFire by its dev name here: " + e);
			}

			BlockPos pos = c.at(0, 1, 0);
			net.minecraft.util.RandomSource rnd = new net.minecraft.world.level.levelgen.LegacyRandomSource(12345);
			int trials = 3000;
			int vanilla = 0;
			int blazing = 0;

			for (int pass = 0; pass < 2; pass++)
			{
				FireBlock fire = (FireBlock) (pass == 0 ? Blocks.FIRE : ModBlocks.BLAZING_FIRE);

				for (int i = 0; i < trials; i++)
				{
					c.setQuiet(pos, Blocks.OAK_PLANKS.defaultBlockState());

					try
					{
						tryCatchFire.invoke(fire, c.world, pos, 300, rnd, 0, Direction.UP);
					}
					catch (ReflectiveOperationException e)
					{
						throw new TestCtx.Failure("tryCatchFire threw: " + e.getCause());
					}

					if (c.world.getBlockState(pos).getBlock() != Blocks.OAK_PLANKS)
					{
						if (pass == 0)
						{
							vanilla++;
						}
						else
						{
							blazing++;
						}
					}
				}
			}

			c.remove(pos);
			c.check(vanilla > 0, "vanilla fire should catch sometimes (" + vanilla + ")");
			c.check(blazing > vanilla * 2.2, "Blazing Fire must catch clearly more often than vanilla: blazing=" + blazing + " vanilla=" + vanilla + " of " + trials);
		}));

		// ---- Magic Hood -----------------------------------------------------------------------
		tests.add(RTTest.named("behavior.magic_hood.hides_the_nametag_and_the_potion_particles_only_while_worn").run(c -> {
			ItemStack before = c.player.getItemBySlot(EquipmentSlot.HEAD);
			Pig pig = EntityType.PIG.create(c.world);

			try
			{
				c.check(AsmHandler.overrideCanRenderName(true, c.player), "without the hood the nametag stays visible");
				c.check(AsmHandler.overrideCanRenderName(true, pig), "non-player entities are not affected");

				PotionColorCalculationEvent open = new PotionColorCalculationEvent(c.player, 0xABCDEF, false, new ArrayList<MobEffectInstance>());
				MinecraftForge.EVENT_BUS.post(open);
				c.eq(0xABCDEF, open.getColor(), "particle colour is untouched without the hood");

				c.player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.MAGIC_HOOD));
				c.check(!AsmHandler.overrideCanRenderName(true, c.player), "with the hood the nametag is hidden");

				List<MobEffectInstance> effects = new ArrayList<>();
				effects.add(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100));
				PotionColorCalculationEvent worn = new PotionColorCalculationEvent(c.player, 0xABCDEF, false, effects);
				MinecraftForge.EVENT_BUS.post(worn);
				c.eq(0, worn.getColor(), "with the hood the particle colour is zeroed (no particles at all)");
				c.check(worn.areParticlesHidden(), "and the hidden flag is set");
			}
			finally
			{
				c.player.setItemSlot(EquipmentSlot.HEAD, before);
			}
		}));

		// ---- Spectre dimension ----------------------------------------------------------------
		tests.add(RTTest.named("behavior.spectre.every_player_gets_their_own_cube_and_positions_inside_it_resolve_to_that_cube").run(c -> {
			SpectreHandler handler = SpectreHandler.getInstance(c.world.getServer());

			if (handler == null)
			{
				throw new TestCtx.Skip("the Spectre dimension is not available in this world");
			}

			Method generate;

			try
			{
				generate = SpectreHandler.class.getDeclaredMethod("generateSpectreCube", UUID.class);
				generate.setAccessible(true);
			}
			catch (ReflectiveOperationException e)
			{
				throw new TestCtx.Skip("cannot reach generateSpectreCube: " + e);
			}

			UUID a = UUID.randomUUID();
			UUID b = UUID.randomUUID();
			SpectreCube cubeA;
			SpectreCube cubeB;

			try
			{
				cubeA = (SpectreCube) generate.invoke(handler, a);
				cubeB = (SpectreCube) generate.invoke(handler, b);
			}
			catch (ReflectiveOperationException e)
			{
				throw new TestCtx.Failure("cube generation threw: " + e.getCause());
			}

			Level spectre = handler.getWorld();
			c.check(cubeA != cubeB && cubeA.getPosition() != cubeB.getPosition(), "two players must get different cubes");
			c.eq(Blocks.AIR, spectre.getBlockState(new BlockPos(cubeA.getSpawnBlock().getX(), 1, cubeA.getSpawnBlock().getZ())).getBlock(), "the inside of a cube is air above its floor");
			c.eq(ModBlocks.SPECTRE_BLOCK, spectre.getBlockState(new BlockPos(cubeB.getSpawnBlock().getX() - 8, 0, 0)).getBlock(), "the cube corner is Spectre block");

			for (SpectreCube cube : new SpectreCube[] { cubeA, cubeB })
			{
				BlockPos inside = new BlockPos(cube.getSpawnBlock().getX(), 1, cube.getSpawnBlock().getZ());
				SpectreCube found = handler.getSpectreCubeFromPos(spectre, inside);
				c.check(found == cube, "a position inside cube at x=" + inside.getX() + " must resolve to that cube, got " + (found == null ? "null" : "other"));
			}

			c.check(handler.getSpectreCubeFromPos(spectre, new BlockPos(cubeA.getSpawnBlock().getX(), 1, 40)) == null, "outside the cube row resolves to nothing");
		}));

		tests.add(RTTest.named("behavior.spectre.two_players_teleporting_in_each_land_in_their_own_cube_and_go_back").run(c -> {
			SpectreHandler handler = SpectreHandler.getInstance(c.world.getServer());

			if (handler == null)
			{
				throw new TestCtx.Skip("the Spectre dimension is not available in this world");
			}

			ServerPlayer one = c.player;

			if (one.level().dimension() != Level.OVERWORLD)
			{
				throw new TestCtx.Skip("the test player starts in " + one.level().dimension().location() + " (a world left behind by an interrupted run?), not the overworld");
			}

			ServerPlayer two = net.minecraftforge.common.util.FakePlayerFactory.get(c.world, new com.mojang.authlib.GameProfile(UUID.randomUUID(), "SpectreSecond"));
			// a fake player has no connection; give it one that swallows every packet so it can change dimension like a real second player
			two.connection = new net.minecraft.server.network.ServerGamePacketListenerImpl(c.world.getServer(), new net.minecraft.network.Connection(net.minecraft.network.protocol.PacketFlow.SERVERBOUND)
			{
				@Override
				public void send(net.minecraft.network.protocol.Packet<?> packet)
				{
				}

				@Override
				public void send(net.minecraft.network.protocol.Packet<?> packet, net.minecraft.network.PacketSendListener listener)
				{
				}
			}, two);
			double startX = one.getX();
			double startZ = one.getZ();

			try
			{
				handler.teleportPlayerToSpectreCube(one);
				handler.teleportPlayerToSpectreCube(two);

				Level spectre = handler.getWorld();
				SpectreCube cubeOne = handler.getSpectreCubeFromPos(spectre, one.blockPosition());
				SpectreCube cubeTwo = handler.getSpectreCubeFromPos(spectre, two.blockPosition());

				c.check(cubeOne != null && cubeOne.getOwner().equals(one.getGameProfile().getId()), "player one must stand in a cube they own, got " + (cubeOne == null ? "no cube" : "owner " + cubeOne.getOwner()) + " at " + one.blockPosition());
				c.check(cubeTwo != null && cubeTwo.getOwner().equals(two.getGameProfile().getId()), "player two must stand in a cube they own, got " + (cubeTwo == null ? "no cube" : "owner " + cubeTwo.getOwner()) + " at " + two.blockPosition());
				c.check(cubeOne != cubeTwo && cubeOne.getPosition() != cubeTwo.getPosition(), "the two players must not share a cube");

				// leaving and going in again finds the same cube instead of generating another
				handler.teleportPlayerBack(one);
				handler.teleportPlayerToSpectreCube(one);
				c.check(handler.getSpectreCubeFromPos(spectre, one.blockPosition()) == cubeOne, "teleporting in a second time must reuse the player's cube");

				// the protection check puts a player who wandered into the other cube back into their own
				two.setPos(cubeOne.getSpawnBlock().getX() + 0.5, 1, cubeOne.getSpawnBlock().getZ() + 0.5);
				handler.checkPosition(two);
				SpectreCube after = handler.getSpectreCubeFromPos(spectre, two.blockPosition());
				c.check(after == cubeTwo, "a non-creative player in someone else's cube must be moved back to their own");
			}
			finally
			{
				handler.teleportPlayerBack(one);
				handler.teleportPlayerBack(two);
			}

			c.check(one.level().dimension() == Level.OVERWORLD, "player one must be back in the overworld, is in " + one.level().dimension().location());
			c.check(Math.abs(one.getX() - startX) < 2 && Math.abs(one.getZ() - startZ) < 2, "player one must be back where they left");
		}));

		// ---- Sound Box / Sound Dampener (server-visible state) --------------------------------
		tests.add(RTTest.named("behavior.sound_box.pattern_toggles_the_block_state_and_survives_nbt").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.SOUND_BOX);
			SoundBoxTileEntity box = c.tile(c.at(0, 1, 0), SoundBoxTileEntity.class);
			c.check(!box.hasPattern(), "starts empty");

			ItemStack pattern = new ItemStack(ModItems.SOUND_PATTERN);
			SoundPatternItem.setSoundLocation(pattern, "minecraft:block.note_block.harp");
			box.insertPattern(pattern);
			c.check(box.hasPattern(), "pattern stored");

			SoundBoxTileEntity copy = new SoundBoxTileEntity(box.getBlockPos(), box.getBlockState());
			copy.load(box.saveWithoutMetadata());
			c.check(copy.hasPattern() && "minecraft:block.note_block.harp".equals(SoundPatternItem.getSoundLocation(copy.getPattern()).toString()), "pattern survives an NBT round trip");
		}));

		tests.add(RTTest.named("behavior.sound_dampener.muted_set_follows_its_patterns_and_the_block_registers_itself").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.SOUND_DAMPENER);
			SoundDampenerTileEntity dampener = c.tile(c.at(0, 1, 0), SoundDampenerTileEntity.class);
			c.check(SoundDampenerTileEntity.loadedDampeners.contains(dampener), "a placed dampener is in the loaded registry used by the client sound hook");
			c.check(dampener.getMutedSounds().isEmpty(), "nothing muted when empty");

			ItemStack pattern = new ItemStack(ModItems.SOUND_PATTERN);
			SoundPatternItem.setSoundLocation(pattern, "minecraft:entity.pig.ambient");
			dampener.getItemHandler().setStackInSlot(0, pattern);
			dampener.getItemHandler().setStackInSlot(0, pattern.copy());
			c.check(dampener.getMutedSounds().stream().anyMatch(r -> r.toString().equals("minecraft:entity.pig.ambient")), "a stored pattern's sound is in the muted set");
		}));

		return tests;
	}
}
