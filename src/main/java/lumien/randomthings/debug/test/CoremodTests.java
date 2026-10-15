package lumien.randomthings.debug.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.tileentity.PeaceCandleTileEntity;
import lumien.randomthings.tileentity.RainShieldTileEntity;
import lumien.randomthings.tileentity.SlimeCubeTileEntity;
import lumien.randomthings.tileentity.redstoneinterface.BasicRedstoneInterfaceTileEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.nbt.ByteTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.Difficulty;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

/**
 * Tests for features that exist only because of a coremod transformer or an unusual hook. These
 * drive the real patched vanilla code paths (spawn dispatch, rain check, redstone power query,
 * entity movement friction, death-drop routine) and assert on the observable result, so a
 * transformer that stops applying - or never applied - fails here.
 */
final class CoremodTests
{
	private CoremodTests()
	{
	}

	private static boolean canSpawn(TestCtx c, EntityType<?> type, BlockPos pos)
	{
		return SpawnPlacements.checkSpawnRules(type, c.world, MobSpawnType.NATURAL, pos, c.world.random);
	}

	/** Vanilla's light check is randomised against the sky light, so "can spawn" is true if any of several tries is. */
	private static boolean canSpawnSometimes(TestCtx c, EntityType<?> type, BlockPos pos)
	{
		for (int i = 0; i < 40; i++)
		{
			if (canSpawn(c, type, pos))
			{
				return true;
			}
		}

		return false;
	}

	private static void resetTrackers()
	{
		RainShieldTileEntity.shields.clear();
		PeaceCandleTileEntity.candles.clear();
		SlimeCubeTileEntity.cubes.clear();
	}

	private static FakePlayer fakePlayer(TestCtx c)
	{
		FakePlayer fake = FakePlayerFactory.get(c.world, new GameProfile(UUID.nameUUIDFromBytes("RTCoremodTest".getBytes()), "RTCoremodTest"));
		fake.getInventory().clearContent();
		fake.setDeltaMovement(Vec3.ZERO);
		fake.setShiftKeyDown(false);
		return fake;
	}

	static List<RTTest> create()
	{
		List<RTTest> tests = new ArrayList<>();

		// ---- Spawn dispatch coremod (SpawnPlacementTransformer) ---------------------------------
		tests.add(RTTest.named("coremod.spawn.lapis_lamp_forces_monster_spawns_allowed_even_when_lit").run(c -> {
			if (c.world.getDifficulty() == Difficulty.PEACEFUL)
			{
				throw new TestCtx.Skip("world is Peaceful (Lapis Lamp deliberately does not force spawns there)");
			}

			resetTrackers();
			c.floor();
			c.set(c.at(0, 1, 0), Blocks.GLOWSTONE);
		}).waitTicks(60).run(c -> {
			c.check(!canSpawn(c, EntityType.ZOMBIE, c.at(1, 1, 0)), "baseline: vanilla must reject a zombie spawn next to a glowstone block");
			c.set(c.at(0, 1, 0), ModBlocks.LAPIS_LAMP);
			c.check(canSpawn(c, EntityType.ZOMBIE, c.at(1, 1, 0)), "a zombie spawn within 4 blocks of a Lapis Lamp must be allowed");
		}));

		tests.add(RTTest.named("coremod.spawn.peace_candle_denies_monster_spawns_in_range").run(c -> {
			if (c.world.getDifficulty() == Difficulty.PEACEFUL)
			{
				throw new TestCtx.Skip("world is Peaceful (vanilla already rejects monsters)");
			}

			resetTrackers();
			c.floor();

			// a sealed dark room so vanilla would accept a zombie spawn
			for (int x = -2; x <= 2; x++)
			{
				for (int z = -2; z <= 2; z++)
				{
					c.setQuiet(c.at(x, 4, z), Blocks.STONE.defaultBlockState());

					if (Math.abs(x) == 2 || Math.abs(z) == 2)
					{
						for (int y = 1; y <= 3; y++)
						{
							c.setQuiet(c.at(x, y, z), Blocks.STONE.defaultBlockState());
						}
					}
				}
			}
		}).waitTicks(200).run(c -> {
			c.check(canSpawnSometimes(c, EntityType.ZOMBIE, c.at(0, 1, 0)), "baseline: vanilla must accept a zombie spawn in a sealed dark room");
			c.set(c.at(5, 1, 5), ModBlocks.PEACE_CANDLE);
			c.check(!canSpawnSometimes(c, EntityType.ZOMBIE, c.at(0, 1, 0)), "a Peace Candle within 3 chunks must deny monster spawns");
			c.remove(c.at(5, 1, 5));
		}).waitTicks(2).run(c -> {
			resetTrackers();
			c.check(canSpawnSometimes(c, EntityType.ZOMBIE, c.at(0, 1, 0)), "spawns must be allowed again once the candle is gone");
		}));

		tests.add(RTTest.named("coremod.spawn.slime_cube_allows_slimes_unpowered_and_denies_them_when_powered").run(c -> {
			resetTrackers();
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.SLIME_CUBE);
		}).waitTicks(2).run(c -> {
			c.check(canSpawn(c, EntityType.SLIME, c.at(0, 2, 0)), "an unpowered Slime Cube must allow slime spawns in its chunk");
			c.set(c.at(1, 1, 0), Blocks.REDSTONE_BLOCK);
		}).waitTicks(2).run(c -> {
			c.check(!canSpawn(c, EntityType.SLIME, c.at(0, 2, 0)), "a powered Slime Cube must deny slime spawns in its chunk");
		}));

		// ---- Rain Shield (RainShieldTransformer on World.isRainingAt) ---------------------------
		tests.add(RTTest.named("coremod.rain_shield.suppresses_rain_within_its_range_only_while_present").run(c -> {
			resetTrackers();
			c.floor();
			c.world.getLevelData().setRaining(true);
			((net.minecraft.world.level.storage.ServerLevelData) c.world.getLevelData()).setRainTime(6000);
		}).waitTicks(40).run(c -> {
			if (!c.world.isRainingAt(c.at(0, 1, 0)))
			{
				c.world.getLevelData().setRaining(false);
				c.world.rainLevel = 0;
				c.world.oRainLevel = 0;
				throw new TestCtx.Skip("could not make it rain at the test position (biome/sky)");
			}

			c.set(c.at(0, 1, 0), ModBlocks.RAIN_SHIELD);
		}).waitTicks(2).run(c -> {
			boolean shielded = !c.world.isRainingAt(c.at(3, 1, 3));
			c.remove(c.at(0, 1, 0));
			c.check(shielded, "rain must be suppressed near an active Rain Shield");
		}).waitTicks(2).run(c -> {
			boolean raining = c.world.isRainingAt(c.at(3, 1, 3));
			c.world.getLevelData().setRaining(false);
			c.world.rainLevel = 0;
				c.world.oRainLevel = 0;
			c.check(raining, "rain must come back once the Rain Shield is removed");
		}));

		// ---- Wireless Redstone Interface (power query coremods) ---------------------------------
		tests.add(RTTest.named("coremod.redstone_interface.mirrors_its_input_power_onto_the_bound_target").run(c -> {
			resetTrackers();
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.BASIC_REDSTONE_INTERFACE);
			c.set(c.at(4, 1, 0), Blocks.REDSTONE_LAMP);
			c.set(c.at(4, 1, 3), Blocks.REDSTONE_LAMP);
			c.tile(c.at(0, 1, 0), BasicRedstoneInterfaceTileEntity.class).setTarget(c.at(4, 1, 0));
			c.set(c.at(0, 2, 0), Blocks.REDSTONE_BLOCK);
		}).waitTicks(4).run(c -> {
			BlockPos t = c.at(4, 1, 0);
			c.check(c.world.hasNeighborSignal(t), "target must read as powered (interface stored UP power="
					+ lumien.randomthings.tileentity.redstoneinterface.RedstoneInterfaceTileEntity.getWeakPower(c.world, t.above(), net.minecraft.core.Direction.UP)
					+ ", world.getRedstonePower(target.up, UP)=" + c.world.getSignal(t.above(), net.minecraft.core.Direction.UP)
					+ ", redstone block's own power seen from the interface=" + c.world.getSignal(c.at(0, 2, 0), net.minecraft.core.Direction.UP) + ")");
			c.check(c.world.getBlockState(c.at(4, 1, 0)).getValue(RedstoneLampBlock.LIT), "target lamp must light");
			c.check(!c.world.getBlockState(c.at(4, 1, 3)).getValue(RedstoneLampBlock.LIT), "an unrelated lamp must stay dark");
			c.remove(c.at(0, 2, 0));
		}).waitTicks(8).run(c -> {
			c.check(!c.world.hasNeighborSignal(c.at(4, 1, 0)), "target must lose power when the interface input is removed");
			c.check(!c.world.getBlockState(c.at(4, 1, 0)).getValue(RedstoneLampBlock.LIT), "target lamp must turn off again");
		}));

		// ---- Spectre Anchor (PlayerEntityTransformer on the death-drop routine) -----------------
		tests.add(RTTest.named("coremod.spectre_anchor.anchored_items_survive_the_death_drop_routine").run(c -> {
			FakePlayer fake = fakePlayer(c);
			fake.setPos(c.origin.getX() + 0.5, c.origin.getY() + 2, c.origin.getZ() + 0.5);

			ItemStack anchored = new ItemStack(Items.IRON_SWORD);
			anchored.getOrCreateTag().put("spectreAnchor", ByteTag.valueOf((byte) 0));
			fake.getInventory().setItem(0, anchored);
			fake.getInventory().setItem(1, new ItemStack(Items.COBBLESTONE, 5));

			// func_213337_cE = PlayerEntity.dropInventory (SRG name, resolves in dev and in a production jar)
			Method drop = net.minecraftforge.fml.util.ObfuscationReflectionHelper.findMethod(Player.class, "m_5907_");
			drop.invoke(fake);

			c.eq(Items.IRON_SWORD, fake.getInventory().getItem(0).getItem(), "anchored item must stay in its slot");
			c.check(fake.getInventory().getItem(1).isEmpty(), "an ordinary item must be removed from the inventory");

			int dropped = 0;

			for (ItemEntity entity : c.itemEntities())
			{
				if (entity.getItem().getItem() == Items.COBBLESTONE)
				{
					dropped += entity.getItem().getCount();
				}
				else
				{
					throw new TestCtx.Failure("unexpected dropped item " + entity.getItem());
				}
			}

			c.eq(5, dropped, "cobblestone dropped into the world");
			fake.getInventory().clearContent();
		}));

		// ---- Super Lubricent Boots (SuperLubricentBootsTransformer on LivingEntity.travel) -------
		tests.add(RTTest.named("coremod.super_lubricent_boots.remove_friction_on_ordinary_ground_unless_sneaking").run(c -> {
			c.floor();
			FakePlayer fake = fakePlayer(c);

			double barefoot = slide(c, fake, false, false);
			double booted = slide(c, fake, true, false);
			double bootedSneaking = slide(c, fake, true, true);

			fake.getInventory().clearContent();
			fake.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);

			c.check(barefoot < 0.7, "barefoot horizontal speed after one tick of friction should be ~0.55, was " + barefoot);
			c.check(booted > 0.9, "boots must keep nearly all horizontal speed on stone, was " + booted);
			c.check(bootedSneaking < 0.7, "sneaking must cancel the boots' effect, was " + bootedSneaking);
		}));

		return tests;
	}

	/** Horizontal speed left after one tick of ground friction, starting at 1.0 block/tick. */
	private static double slide(TestCtx c, FakePlayer fake, boolean boots, boolean sneaking)
	{
		fake.setItemSlot(EquipmentSlot.FEET, boots ? new ItemStack(ModItems.SUPER_LUBRICENT_BOOTS) : ItemStack.EMPTY);
		fake.setShiftKeyDown(sneaking);
		fake.setPos(c.origin.getX() + 0.5, c.origin.getY() + 1, c.origin.getZ() + 0.5);
		fake.setOnGround(true);
		fake.setDeltaMovement(new Vec3(1.0, 0.0, 0.0));
		fake.travel(Vec3.ZERO);
		return fake.getDeltaMovement().x;
	}
}
