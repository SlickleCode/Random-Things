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
import net.minecraft.util.math.Vec3d;
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

		// ---- Ender Mailbox --------------------------------------------------------------------
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
					new BlockRayTraceResult(new Vec3d(pos), Direction.UP, pos, false));
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
					new BlockRayTraceResult(new Vec3d(pos), Direction.UP, pos, false));
			c.player.setSneaking(false);

			c.check(!c.player.getHeldItemMainhand().isEmpty(), "the letter must stay in the hand when the receiver is unknown");
			c.player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
		}));

		// ---- Portkey --------------------------------------------------------------------------
		Vec3d[] portkeyHome = new Vec3d[1];
		tests.add(RTTest.named("behavior.portkey.binds_primes_after_100_ticks_and_teleports_the_picker_upper").run(c -> {
			c.floor();
			ItemStack key = new ItemStack(ModItems.PORTKEY);
			c.player.setHeldItem(Hand.MAIN_HAND, key);
			BlockPos target = c.at(8, 0, 0);
			ModItems.PORTKEY.onItemUse(new ItemUseContext(c.player, Hand.MAIN_HAND, new BlockRayTraceResult(new Vec3d(target), Direction.UP, target, false)));
			CompoundNBT tag = key.getTag();
			c.check(tag != null && tag.getBoolean("hasTarget"), "right-click must bind a target");
			c.eq(target.getX(), tag.getInt("targetX"), "bound x");
			c.player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
			portkeyHome[0] = c.player.getPositionVec();
			ItemEntity dropped = c.spawnItem(0.5, 2.5, 0.5, key.copy());
			dropped.setPickupDelay(32767);
		}).waitTicks(60).run(c -> {
			ItemEntity drop = c.itemEntities().get(0);
			c.check(drop.getItem().getTag().getInt("dropCounter") < 100, "not primed yet after about 60 ticks");
			c.player.setPosition(c.origin.getX() + 0.5, c.origin.getY() + 1, c.origin.getZ() + 0.5);
			MinecraftForge.EVENT_BUS.post(new EntityItemPickupEvent(c.player, drop));
			c.check(c.player.posX < c.origin.getX() + 3, "an unprimed Portkey must not teleport the picker-upper (x=" + c.player.posX + ")");
		}).waitTicks(60).run(c -> {
			ItemEntity drop = c.itemEntities().get(0);
			c.check(drop.getItem().getTag().getInt("dropCounter") > 100, "primed after more than 100 ticks");

			if (c.player.connection == null)
			{
				// The teleport goes through the player's network handler; a fake player has none unless we give it a detached one.
				new ServerPlayNetHandler(c.world.getServer(), new NetworkManager(PacketDirection.SERVERBOUND), c.player);
			}

			c.player.setPosition(c.origin.getX() + 0.5, c.origin.getY() + 1, c.origin.getZ() + 0.5);
			MinecraftForge.EVENT_BUS.post(new EntityItemPickupEvent(c.player, drop));
			c.check(Math.abs(c.player.posX - (c.origin.getX() + 8)) <= 3.0, "picker-upper must land near the bound target (x=" + c.player.posX + ")");
			boolean nearSurface = Math.abs(c.player.posY - (c.origin.getY() + 1)) <= 1.5;
			double landedY = c.player.posY;
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

		return tests;
	}
}
