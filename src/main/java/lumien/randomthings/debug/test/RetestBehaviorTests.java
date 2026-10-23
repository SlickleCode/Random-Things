package lumien.randomthings.debug.test;

import java.util.ArrayList;
import java.util.List;

import lumien.randomthings.entity.ModEntityTypes;
import lumien.randomthings.entity.SpiritEntity;
import lumien.randomthings.item.ModItems;
import lumien.randomthings.util.StackTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Tests for the rows of {@code TESTING_CHECKLIST.md} that no other suite covered: Spirit (#325-328), Summoning Pendulum (#225-229), Magic Hood
 * durability (#255). Each test names the checklist rows it stands in for.
 */
final class RetestBehaviorTests
{
	private RetestBehaviorTests()
	{
	}

	private static void ensureConnection(TestCtx c)
	{
		if (c.player.connection == null || c.player.connection.getClass() != ServerGamePacketListenerImpl.class)
		{
			new ServerGamePacketListenerImpl(c.world.getServer(), new Connection(PacketFlow.SERVERBOUND), c.player, net.minecraft.server.network.CommonListenerCookie.createInitial(c.player.getGameProfile(), false));
		}
	}

	private static void standAt(TestCtx c, int x, int y, int z)
	{
		BlockPos p = c.at(x, y, z);
		c.player.setPos(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
		c.player.setDeltaMovement(Vec3.ZERO);
	}

	private static void hold(TestCtx c, ItemStack stack)
	{
		c.player.stopUsingItem();
		c.player.setItemInHand(InteractionHand.MAIN_HAND, stack);
	}

	private static void release(TestCtx c)
	{
		c.player.stopUsingItem();
		c.player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
	}

	private static SpiritEntity spirit(TestCtx c, int x, int y, int z)
	{
		SpiritEntity spirit = ModEntityTypes.SPIRIT.create(c.world, EntitySpawnReason.COMMAND);
		BlockPos p = c.at(x, y, z);
		spirit.setPos(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
		c.world.addFreshEntity(spirit);
		return spirit;
	}

	private static Cow cow(TestCtx c, int x, int z)
	{
		Cow cow = EntityType.COW.create(c.world, EntitySpawnReason.COMMAND);
		BlockPos p = c.at(x, 1, z);
		cow.setPos(p.getX() + 0.5, p.getY(), p.getZ() + 0.5);
		c.world.addFreshEntity(cow);
		return cow;
	}

	private static int captured(ItemStack pendulum)
	{
		return StackTag.has(pendulum) ? StackTag.get(pendulum).getList("entitys", 10).size() : 0;
	}

	private static net.minecraft.world.item.Item item(String path)
	{
		return net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("randomthings", path));
	}

	private static InteractionResult useOn(TestCtx c, BlockPos clicked, Direction face)
	{
		ItemStack held = c.player.getMainHandItem();
		return held.getItem().useOn(new UseOnContext(c.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(clicked), face, clicked, false)));
	}

	private static InteractionResult useOnAt(TestCtx c, BlockPos clicked, Direction face, double dx, double dy, double dz)
	{
		ItemStack held = c.player.getMainHandItem();
		return held.getItem().useOn(new UseOnContext(c.player, InteractionHand.MAIN_HAND, new BlockHitResult(new Vec3(clicked.getX() + dx, clicked.getY() + dy, clicked.getZ() + dz), face, clicked, false)));
	}

	private static int count(TestCtx c, net.minecraft.world.item.Item item)
	{
		int n = 0;

		for (net.minecraft.world.entity.item.ItemEntity drop : c.itemEntities())
		{
			if (drop.getItem().getItem() == item)
			{
				n += drop.getItem().getCount();
			}
		}

		return n;
	}

	private static net.minecraft.world.level.block.state.BlockState bridge(Direction facing)
	{
		return lumien.randomthings.block.ModBlocks.ENDER_BRIDGE.defaultBlockState().setValue(lumien.randomthings.block.EnderBridgeBlockBase.FACING, facing);
	}

	private static boolean bridgeActive(TestCtx c, BlockPos pos)
	{
		return c.world.getBlockState(pos).getValue(lumien.randomthings.block.EnderBridgeBlockBase.ACTIVE);
	}

	private static net.minecraft.world.entity.item.ItemEntity firstItem(TestCtx c, net.minecraft.world.item.Item item)
	{
		for (net.minecraft.world.entity.item.ItemEntity drop : c.itemEntities())
		{
			if (drop.getItem().getItem() == item)
			{
				return drop;
			}
		}

		return null;
	}

	private static net.minecraft.world.item.crafting.CraftingInput grid(ItemStack... stacks)
	{
		return net.minecraft.world.item.crafting.CraftingInput.of(3, 3, java.util.Arrays.asList(java.util.Arrays.copyOf(stacks, 9)).stream().map(st -> st == null ? ItemStack.EMPTY : st).toList());
	}

	/** A dark air block high above the test spot, in the chunk the Spectre Illuminator is placed in. */
	private static BlockPos illuminatorProbe(TestCtx c)
	{
		net.minecraft.world.level.ChunkPos chunk = new net.minecraft.world.level.ChunkPos(c.origin);
		return new BlockPos(chunk.getMinBlockX() + 3, c.origin.getY() + 6, chunk.getMinBlockZ() + 3);
	}

	static List<RTTest> create()
	{
		List<RTTest> tests = new ArrayList<>();

		// ---- Spirit (#327, #328) ----------------------------------------------------------------
		tests.add(RTTest.named("retest.spirit.ignores_ordinary_damage_and_dies_in_one_hit_from_a_spectre_sword").run(c -> {
			c.floor();
			ensureConnection(c);
			standAt(c, 3, 1, 3);
			SpiritEntity spirit = spirit(c, 0, 2, 0);

			c.check(!spirit.hurtServer(c.world, c.world.damageSources().generic(), 5.0F), "generic damage must be refused");
			c.check(!spirit.hurtServer(c.world, c.world.damageSources().inFire(), 5.0F), "fire must be refused");
			c.check(!spirit.hurtServer(c.world, c.world.damageSources().arrow(null, null), 5.0F), "an arrow must be refused");
			c.check(spirit.isAlive(), "the spirit survived all of that");

			hold(c, new ItemStack(Items.DIAMOND_SWORD));
			spirit.hurtServer(c.world, c.world.damageSources().playerAttack(c.player), 8.0F);
			c.check(spirit.isAlive(), "a diamond sword must not hurt a spirit");

			hold(c, new ItemStack(ModItems.SPECTRE_SWORD));
			spirit.hurtServer(c.world, c.world.damageSources().playerAttack(c.player), 1.0F);
			c.check(!spirit.isAlive(), "a Spectre Sword kills it in one hit");
			release(c);
		}));

		tests.add(RTTest.named("retest.spirit.killed_with_a_spectre_sword_drops_one_or_two_ectoplasm").run(c -> {
			c.floor();
			ensureConnection(c);
			standAt(c, 3, 1, 3);
			hold(c, new ItemStack(ModItems.SPECTRE_SWORD));
			int kills = 40;
			int minDrop = Integer.MAX_VALUE;
			int maxDrop = 0;

			for (int i = 0; i < kills; i++)
			{
				SpiritEntity spirit = spirit(c, 0, 2, 0);
				spirit.hurtServer(c.world, c.world.damageSources().playerAttack(c.player), 1.0F);
				int count = 0;

				for (net.minecraft.world.entity.item.ItemEntity item : c.itemEntities())
				{
					if (item.getItem().getItem() == ModItems.ECTOPLASM)
					{
						count += item.getItem().getCount();
						item.discard();
					}
				}

				minDrop = Math.min(minDrop, count);
				maxDrop = Math.max(maxDrop, count);
			}

			release(c);
			c.check(minDrop == 1, "every kill drops at least one ectoplasm, smallest was " + minDrop);
			c.check(maxDrop == 2, "some kills drop two (1 in 5), largest over " + kills + " kills was " + maxDrop);
		}));

		// ---- Summoning Pendulum (#225-229) --------------------------------------------------------
		tests.add(RTTest.named("retest.summoning_pendulum.captures_passive_mobs_up_to_five_and_refuses_hostile_mobs_and_a_sixth").run(c -> {
			c.floor();
			ensureConnection(c);
			ItemStack pendulum = new ItemStack(ModItems.SUMMONING_PENDULUM);
			hold(c, pendulum);

			Zombie zombie = EntityType.ZOMBIE.create(c.world, EntitySpawnReason.COMMAND);
			zombie.setPos(c.at(4, 1, 4).getX() + 0.5, c.at(4, 1, 4).getY(), c.at(4, 1, 4).getZ() + 0.5);
			zombie.setNoAi(true);
			c.world.addFreshEntity(zombie);
			pendulum.getItem().interactLivingEntity(pendulum, c.player, zombie, InteractionHand.MAIN_HAND);
			c.check(zombie.isAlive() && captured(c.player.getMainHandItem()) == 0, "a hostile mob must not be captured");

			List<Cow> cows = new ArrayList<>();

			for (int i = 0; i < 6; i++)
			{
				Cow cow = cow(c, i - 3, -3);
				cows.add(cow);
				c.player.getMainHandItem().getItem().interactLivingEntity(c.player.getMainHandItem(), c.player, cow, InteractionHand.MAIN_HAND);
			}

			int alive = 0;

			for (Cow cow : cows)
			{
				if (cow.isAlive())
				{
					alive++;
				}
			}

			c.eq(5, captured(c.player.getMainHandItem()), "captured mobs");
			c.eq(1, alive, "the sixth cow stays in the world");
			c.check(c.player.getMainHandItem().hasFoil(), "a full pendulum shimmers");
			c.check(!c.player.getMainHandItem().getItem().isBarVisible(c.player.getMainHandItem()), "a full pendulum hides its bar");
			release(c);
		}));

		tests.add(RTTest.named("retest.summoning_pendulum.releases_the_oldest_captured_mob_first").run(c -> {
			c.floor();
			ensureConnection(c);
			ItemStack pendulum = new ItemStack(ModItems.SUMMONING_PENDULUM);
			hold(c, pendulum);

			Cow first = cow(c, -2, -3);
			first.setCustomName(net.minecraft.network.chat.Component.literal("first"));
			Cow second = cow(c, 2, -3);
			second.setCustomName(net.minecraft.network.chat.Component.literal("second"));
			pendulum.getItem().interactLivingEntity(pendulum, c.player, first, InteractionHand.MAIN_HAND);
			pendulum.getItem().interactLivingEntity(pendulum, c.player, second, InteractionHand.MAIN_HAND);
			c.eq(2, captured(c.player.getMainHandItem()), "captured");
			c.check(!first.isAlive() && !second.isAlive(), "both cows left the world");

			BlockPos floor = c.at(0, 0, 0);
			InteractionResult result = c.player.getMainHandItem().getItem().useOn(new UseOnContext(c.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(floor), Direction.UP, floor, false)));
			c.eq(InteractionResult.SUCCESS, result, "use result");
			c.eq(1, captured(c.player.getMainHandItem()), "one left in the pendulum");

			List<Cow> released = c.world.getEntitiesOfClass(Cow.class, c.area());
			c.eq(1, released.size(), "released cows");
			c.eq("first", released.get(0).getCustomName() == null ? "" : released.get(0).getCustomName().getString(), "the first captured cow comes out first");
			release(c);
		}));

		// ---- Magic Hood (#255) --------------------------------------------------------------------
		tests.add(RTTest.named("retest.magic_hood.has_the_durability_of_a_leather_helmet").run(c -> {
			c.eq(new ItemStack(Items.LEATHER_HELMET).getMaxDamage(), new ItemStack(ModItems.MAGIC_HOOD).getMaxDamage(), "max damage");
		}));

		// ---- ID Card, Position Filter, Stable Ender Pearl, Redstone Activator, Sound Pattern, Entity Filter, Redstone Tool (#115, #136, #124, #286, #156, #108, #94) ----
		tests.add(RTTest.named("retest.id_card.right_click_binds_it_to_the_user").run(c -> {
			ItemStack card = new ItemStack(ModItems.ID_CARD);
			hold(c, card);
			c.eq(InteractionResult.SUCCESS, card.getItem().use(c.world, c.player, InteractionHand.MAIN_HAND), "use result");
			c.eq(c.player.getGameProfile().getId(), lumien.randomthings.item.IdCardItem.getUUID(c.player.getMainHandItem()), "bound uuid");
			c.check(lumien.randomthings.item.IdCardItem.getUUID(c.player.getMainHandItem()) != null, "the card keeps its binding");
			release(c);
		}));

		tests.add(RTTest.named("retest.position_filter.right_click_stores_the_clicked_position_and_dimension").run(c -> {
			c.floor();
			hold(c, new ItemStack(ModItems.POSITION_FILTER));
			BlockPos clicked = c.at(2, 0, 3);
			c.eq(InteractionResult.SUCCESS, useOn(c, clicked, Direction.UP), "use result");
			c.eq(clicked, lumien.randomthings.item.PositionFilterItem.getPosition(c.player.getMainHandItem()), "stored position");
			c.eq(c.world.dimension().location().toString(), StackTag.get(c.player.getMainHandItem()).getString("dimension"), "stored dimension");
			release(c);
		}));

		tests.add(RTTest.named("retest.stable_ender_pearl.right_click_binds_it_to_the_user").run(c -> {
			ItemStack pearl = new ItemStack(ModItems.STABLE_ENDERPEARL);
			hold(c, pearl);
			pearl.getItem().use(c.world, c.player, InteractionHand.MAIN_HAND);
			c.eq(c.player.getGameProfile().getName(), StackTag.get(c.player.getMainHandItem()).getString("player-name"), "bound name");
			c.eq(c.player.getGameProfile().getId().toString(), StackTag.get(c.player.getMainHandItem()).getString("player-uuid"), "bound uuid");
			release(c);
		}));

		tests.add(RTTest.named("retest.redstone_activator.right_click_cycles_2_20_100_and_sneak_goes_backwards").run(c -> {
			ItemStack activator = new ItemStack(ModItems.REDSTONE_ACTIVATOR);
			hold(c, activator);
			c.eq(1, lumien.randomthings.item.RedstoneActivatorItem.getDurationIndex(activator), "starts at 20 ticks");
			c.player.setShiftKeyDown(false);
			activator.getItem().use(c.world, c.player, InteractionHand.MAIN_HAND);
			c.eq(2, lumien.randomthings.item.RedstoneActivatorItem.getDurationIndex(c.player.getMainHandItem()), "forward to 100");
			c.player.getMainHandItem().getItem().use(c.world, c.player, InteractionHand.MAIN_HAND);
			c.eq(0, lumien.randomthings.item.RedstoneActivatorItem.getDurationIndex(c.player.getMainHandItem()), "wraps around to 2");
			c.player.setShiftKeyDown(true);
			c.player.getMainHandItem().getItem().use(c.world, c.player, InteractionHand.MAIN_HAND);
			c.player.setShiftKeyDown(false);
			c.eq(2, lumien.randomthings.item.RedstoneActivatorItem.getDurationIndex(c.player.getMainHandItem()), "sneak wraps backwards to 100");
			release(c);
		}));

		tests.add(RTTest.named("retest.sound_pattern.sneak_right_click_clears_a_stamped_pattern").run(c -> {
			ItemStack pattern = new ItemStack(ModItems.SOUND_PATTERN);
			lumien.randomthings.item.SoundPatternItem.setSoundLocation(pattern, "minecraft:block.note_block.bell");
			hold(c, pattern);
			c.check(lumien.randomthings.item.SoundPatternItem.getSoundLocation(c.player.getMainHandItem()) != null, "stamped");
			c.player.setShiftKeyDown(false);
			c.eq(InteractionResult.PASS, c.player.getMainHandItem().getItem().use(c.world, c.player, InteractionHand.MAIN_HAND), "plain right-click does nothing");
			c.player.setShiftKeyDown(true);
			c.eq(InteractionResult.SUCCESS, c.player.getMainHandItem().getItem().use(c.world, c.player, InteractionHand.MAIN_HAND), "sneak right-click clears");
			c.player.setShiftKeyDown(false);
			c.check(lumien.randomthings.item.SoundPatternItem.getSoundLocation(c.player.getMainHandItem()) == null, "the pattern is blank again");
			release(c);
		}));

		tests.add(RTTest.named("retest.entity_filter.right_click_on_an_entity_captures_its_type_and_the_filter_matches_only_that_type").run(c -> {
			c.floor();
			ItemStack filter = new ItemStack(item("entity_filter"));
			hold(c, filter);
			Cow cow = cow(c, 0, 0);
			filter.getItem().interactLivingEntity(filter, c.player, cow, InteractionHand.MAIN_HAND);
			c.eq("minecraft:cow", StackTag.get(c.player.getMainHandItem()).getString("entityType"), "captured type");

			lumien.randomthings.lib.IEntityFilterItem logic = (lumien.randomthings.lib.IEntityFilterItem) c.player.getMainHandItem().getItem();
			c.check(logic.apply(c.player.getMainHandItem(), cow), "matches a cow");
			Zombie zombie = EntityType.ZOMBIE.create(c.world, EntitySpawnReason.COMMAND);
			c.check(!logic.apply(c.player.getMainHandItem(), zombie), "does not match a zombie");
			release(c);
		}));

		tests.add(RTTest.named("retest.redstone_tool.two_clicks_link_an_observer_to_its_target").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), lumien.randomthings.block.ModBlocks.REDSTONE_OBSERVER);
			c.setQuiet(c.at(3, 1, 0), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
			hold(c, new ItemStack(item("redstone_tool")));

			c.eq(InteractionResult.FAIL, useOn(c, c.at(3, 1, 0), Direction.UP), "a plain block cannot start a link");
			c.eq(InteractionResult.SUCCESS, useOn(c, c.at(0, 1, 0), Direction.UP), "first click on the observer starts linking");
			c.check(c.player.getMainHandItem().getItem().isFoil(c.player.getMainHandItem()), "the tool glows while linking");
			c.eq(InteractionResult.SUCCESS, useOn(c, c.at(3, 1, 0), Direction.UP), "second click sets the target");
			c.check(!c.player.getMainHandItem().getItem().isFoil(c.player.getMainHandItem()), "the glow ends after linking");
			c.eq(c.at(3, 1, 0), c.tile(c.at(0, 1, 0), lumien.randomthings.tileentity.RedstoneObserverTileEntity.class).getTarget(), "observer target");
			release(c);
		}));

		// ---- Eclipsed Clock (#194, #197) -----------------------------------------------------------
		tests.add(RTTest.named("retest.eclipsed_clock.right_click_adds_30_seconds_and_sneak_right_click_takes_them_away").run(c -> {
			c.floor();
			c.setQuiet(c.at(0, 2, 0), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
			lumien.randomthings.entity.EclipsedClockEntity clock = new lumien.randomthings.entity.EclipsedClockEntity(c.world, c.at(0, 2, 1), Direction.SOUTH);
			c.world.addFreshEntity(clock);
			int start = clock.getTargetTime();

			c.player.setShiftKeyDown(false);
			clock.interact(c.player, InteractionHand.MAIN_HAND);
			c.eq((start + 600) % 24000, clock.getTargetTime(), "after one click");
			clock.interact(c.player, InteractionHand.MAIN_HAND);
			c.eq((start + 1200) % 24000, clock.getTargetTime(), "after two clicks");

			c.player.setShiftKeyDown(true);
			clock.interact(c.player, InteractionHand.MAIN_HAND);
			c.player.setShiftKeyDown(false);
			c.eq((start + 600) % 24000, clock.getTargetTime(), "sneak goes back 30 s");
		}));

		tests.add(RTTest.named("retest.eclipsed_clock.breaking_drops_a_clock_except_for_a_creative_player").run(c -> {
			c.floor();
			c.setQuiet(c.at(0, 2, 0), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
			lumien.randomthings.entity.EclipsedClockEntity clock = new lumien.randomthings.entity.EclipsedClockEntity(c.world, c.at(0, 2, 1), Direction.SOUTH);
			c.world.addFreshEntity(clock);

			c.player.getAbilities().instabuild = true;
			clock.dropItem(c.world, c.player);
			c.player.getAbilities().instabuild = false;
			c.eq(0, c.itemEntities().size(), "creative breaking drops nothing");

			clock.dropItem(c.world, c.player);
			int clocks = 0;

			for (net.minecraft.world.entity.item.ItemEntity drop : c.itemEntities())
			{
				if (drop.getItem().getItem() == ModItems.ECLIPSED_CLOCK)
				{
					clocks += drop.getItem().getCount();
				}
			}

			c.eq(1, clocks, "survival breaking drops one fresh clock");
		}));

		// ---- Ender Bucket (#219-221) ---------------------------------------------------------------
		tests.add(RTTest.named("retest.ender_bucket.picking_up_water_from_a_stack_fills_one_bucket_and_leaves_the_rest").run(c -> {
			c.floor();
			c.setQuiet(c.at(0, 0, 0), net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
			standAt(c, 0, 1, 0);
			c.player.setXRot(90.0F);
			hold(c, new ItemStack(ModItems.ENDER_BUCKET, 2));

			InteractionResult result = c.player.getMainHandItem().getItem().use(c.world, c.player, InteractionHand.MAIN_HAND);
			c.check(result.consumesAction(), "the pickup succeeded, got " + result);
			c.check(c.world.getBlockState(c.at(0, 0, 0)).getFluidState().isEmpty(), "the water source was taken");

			ItemStack held = result instanceof InteractionResult.Success success && success.heldItemTransformedTo() != null ? success.heldItemTransformedTo() : c.player.getMainHandItem();
			int empties = held.getCount();
			int filled = 0;

			for (int i = 0; i < c.player.getInventory().getContainerSize(); i++)
			{
				ItemStack inv = c.player.getInventory().getItem(i);

				if (inv.getItem() == ModItems.ENDER_BUCKET && inv.getMaxStackSize() == 1)
				{
					filled += inv.getCount();
				}
			}

			c.eq(1, filled, "one filled bucket in the inventory");
			c.eq(1, empties, "the held stack of 2 lost exactly one empty bucket");
			c.player.getInventory().clearContent();
			release(c);
		}));

		tests.add(RTTest.named("retest.ender_bucket.a_full_bucket_places_its_fluid_on_the_clicked_face_and_empties").run(c -> {
			c.floor();
			standAt(c, 0, 1, 0);
			c.player.setXRot(90.0F);
			hold(c, new ItemStack(ModItems.ENDER_BUCKET));
			c.setQuiet(c.at(0, 0, 0), net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
			InteractionResult pickedUp = c.player.getMainHandItem().getItem().use(c.world, c.player, InteractionHand.MAIN_HAND);
			ItemStack full = pickedUp instanceof InteractionResult.Success success && success.heldItemTransformedTo() != null ? success.heldItemTransformedTo() : c.player.getMainHandItem();
			c.setQuiet(c.at(0, 0, 0), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
			hold(c, full);
			c.eq(1, full.getMaxStackSize(), "a full bucket does not stack");

			InteractionResult placed = full.getItem().use(c.world, c.player, InteractionHand.MAIN_HAND);
			c.check(placed.consumesAction(), "placing succeeded, got " + placed);
			c.check(c.world.getFluidState(c.at(0, 1, 0)).is(net.minecraft.world.level.material.Fluids.WATER), "water was placed above the clicked floor");
			release(c);
		}));

		// ---- Block loot tables (#119, #329-333, #339) ------------------------------------------------
		tests.add(RTTest.named("retest.loot.every_mod_block_drops_itself_except_the_documented_exceptions").run(c -> {
			c.floor();
			BlockPos pos = c.at(0, 1, 0);
			java.util.Set<String> dropNothing = java.util.Set.of("spectre_block", "spectre_core", "spectre_leaf", "bean_stalk", "lesser_bean_stalk", "blazing_fire", "rune_base", "floo_brick", "diaphanous_block");
			java.util.Map<String, String> otherDrop = java.util.Map.of("compressed_slime_block", "slime_block", "bean_sprout", "beans", "bean_pod", "iron_ingot", "lotus", "lotus_seeds", "colored_grass", "dirt");
			List<String> problems = new ArrayList<>();

			for (net.minecraft.world.level.block.Block block : net.minecraft.core.registries.BuiltInRegistries.BLOCK)
			{
				net.minecraft.resources.ResourceLocation id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block);

				if (!"randomthings".equals(id.getNamespace()))
				{
					continue;
				}

				c.setQuiet(pos, block.defaultBlockState());
				List<ItemStack> drops = net.minecraft.world.level.block.Block.getDrops(c.world.getBlockState(pos), c.world, pos, c.world.getBlockEntity(pos), c.player, new ItemStack(Items.STICK));
				String path = id.getPath();

				if (dropNothing.contains(path))
				{
					if (!drops.isEmpty())
					{
						problems.add(path + " should drop nothing but dropped " + drops);
					}
				}
				else if (otherDrop.containsKey(path))
				{
					boolean found = false;

					for (ItemStack drop : drops)
					{
						found |= net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(drop.getItem()).getPath().equals(otherDrop.get(path));
					}

					if (!found)
					{
						problems.add(path + " should drop " + otherDrop.get(path) + " but dropped " + drops);
					}
				}
				else if (drops.size() != 1 || drops.get(0).getItem() != block.asItem())
				{
					problems.add(path + " should drop itself but dropped " + drops);
				}

				c.setQuiet(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
				c.world.removeBlockEntity(pos);
			}

			c.check(problems.isEmpty(), "loot problems: " + problems);
		}));

		tests.add(RTTest.named("retest.loot.colored_grass_drops_dirt_and_only_itself_with_silk_touch").run(c -> {
			c.floor();
			BlockPos pos = c.at(0, 1, 0);
			net.minecraft.world.level.block.state.BlockState red = lumien.randomthings.block.ModBlocks.COLORED_GRASS.defaultBlockState().setValue(lumien.randomthings.block.ColoredGrassBlock.COLOR, net.minecraft.world.item.DyeColor.RED);
			c.setQuiet(pos, red);
			List<ItemStack> plain = net.minecraft.world.level.block.Block.getDrops(red, c.world, pos, null, c.player, new ItemStack(Items.DIAMOND_SHOVEL));
			c.check(plain.size() == 1 && plain.get(0).getItem() == Items.DIRT, "without silk touch: plain dirt, got " + plain);

			ItemStack silk = new ItemStack(Items.DIAMOND_SHOVEL);
			silk.enchant(c.world.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.SILK_TOUCH), 1);
			List<ItemStack> silked = net.minecraft.world.level.block.Block.getDrops(red, c.world, pos, null, c.player, silk);
			c.check(silked.size() == 1 && silked.get(0).getItem() == lumien.randomthings.block.ModBlocks.COLORED_GRASS.asItem(), "with silk touch: itself, got " + silked);
		}));

		tests.add(RTTest.named("retest.loot.bean_sprout_drops_one_bean_when_young_and_three_to_four_when_fully_grown").run(c -> {
			c.floor();
			BlockPos pos = c.at(0, 1, 0);
			net.minecraft.world.level.block.state.BlockState young = lumien.randomthings.block.ModBlocks.BEAN_SPROUT.defaultBlockState();
			net.minecraft.world.level.block.state.BlockState grown = young.setValue(net.minecraft.world.level.block.CropBlock.AGE, 7);

			for (int i = 0; i < 30; i++)
			{
				int youngBeans = 0;
				int grownBeans = 0;

				for (ItemStack drop : net.minecraft.world.level.block.Block.getDrops(young, c.world, pos, null))
				{
					youngBeans += drop.getCount();
				}

				for (ItemStack drop : net.minecraft.world.level.block.Block.getDrops(grown, c.world, pos, null))
				{
					grownBeans += drop.getCount();
				}

				c.eq(1, youngBeans, "young sprout beans");
				c.check(grownBeans >= 3 && grownBeans <= 4, "a grown sprout drops 3-4 beans, got " + grownBeans);
			}
		}));

		tests.add(RTTest.named("retest.loot.bean_pod_drops_ores_beans_and_a_golden_egg_within_the_documented_ranges").run(c -> {
			c.floor();
			BlockPos pos = c.at(0, 1, 0);
			net.minecraft.world.level.block.state.BlockState pod = lumien.randomthings.block.ModBlocks.BEAN_POD.defaultBlockState();

			for (int i = 0; i < 40; i++)
			{
				java.util.Map<String, Integer> counts = new java.util.HashMap<>();

				for (ItemStack drop : net.minecraft.world.level.block.Block.getDrops(pod, c.world, pos, null))
				{
					counts.merge(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(drop.getItem()).getPath(), drop.getCount(), Integer::sum);
				}

				c.check(counts.getOrDefault("iron_ingot", 0) >= 8 && counts.getOrDefault("iron_ingot", 0) <= 20, "iron 8-20, got " + counts);
				c.check(counts.getOrDefault("gold_ingot", 0) >= 4 && counts.getOrDefault("gold_ingot", 0) <= 15, "gold 4-15, got " + counts);
				c.check(counts.getOrDefault("diamond", 0) >= 1 && counts.getOrDefault("diamond", 0) <= 5, "diamonds 1-5, got " + counts);
				c.check(counts.getOrDefault("emerald", 0) <= 2, "emeralds 0-2, got " + counts);
				c.check(counts.getOrDefault("beans", 0) >= 4 && counts.getOrDefault("beans", 0) <= 8, "beans 4-8, got " + counts);
				c.eq(1, counts.getOrDefault("golden_egg", 0), "golden egg");
			}
		}));

		tests.add(RTTest.named("retest.loot.spectre_leaf_drops_ectoplasm_only_and_rarely").run(c -> {
			c.floor();
			BlockPos pos = c.at(0, 1, 0);
			net.minecraft.world.level.block.state.BlockState leaf = lumien.randomthings.block.ModBlocks.SPECTRE_LEAF.defaultBlockState();
			int ectoplasm = 0;
			int other = 0;
			int breaks = 20000;

			for (int i = 0; i < breaks; i++)
			{
				for (ItemStack drop : net.minecraft.world.level.block.Block.getDrops(leaf, c.world, pos, null))
				{
					if (drop.getItem() == ModItems.ECTOPLASM)
					{
						ectoplasm += drop.getCount();
					}
					else
					{
						other++;
					}
				}
			}

			c.eq(0, other, "nothing but ectoplasm");
			c.check(ectoplasm > breaks / 100 && ectoplasm < breaks / 30, "about 1 in 55 leaves drops ectoplasm, got " + ectoplasm + " of " + breaks);
		}));

		// ---- Blocks that need ground (#257, #268) ----------------------------------------------------
		for (String[] spec : new String[][] { { "rain_shield", "RAIN_SHIELD" }, { "peace_candle", "PEACE_CANDLE" } })
		{
			final String name = spec[0];
			final String field = spec[1];

			tests.add(RTTest.named("retest." + name + ".breaks_and_drops_itself_when_the_block_below_is_removed").run(c -> {
				c.floor();
				net.minecraft.world.level.block.Block block = (net.minecraft.world.level.block.Block) lumien.randomthings.block.ModBlocks.class.getField(field).get(null);
				c.setQuiet(c.at(0, 1, 0), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
				c.set(c.at(0, 2, 0), block);
				c.check(c.world.getBlockState(c.at(0, 2, 0)).is(block), "placed on stone");
				c.remove(c.at(0, 1, 0));
			}).waitTicks(5).run(c -> {
				net.minecraft.world.level.block.Block block = (net.minecraft.world.level.block.Block) lumien.randomthings.block.ModBlocks.class.getField(field).get(null);
				c.check(!c.world.getBlockState(c.at(0, 2, 0)).is(block), "the block broke when its support went away");
				int drops = 0;

				for (net.minecraft.world.entity.item.ItemEntity drop : c.itemEntities())
				{
					if (drop.getItem().getItem() == block.asItem())
					{
						drops += drop.getItem().getCount();
					}
				}

				c.eq(1, drops, "it dropped itself");
			}));
		}

		// ---- Ancient Furnace (#263, #264) ------------------------------------------------------------
		tests.add(RTTest.named("retest.ancient_furnace.a_nether_star_starts_the_heat_up_and_nothing_else_does").run(c -> {
			c.floor();
			BlockPos pos = c.at(0, 1, 0);
			c.set(pos, lumien.randomthings.block.ModBlocks.ANCIENT_FURNACE);
			net.minecraft.world.level.block.state.BlockState state = c.world.getBlockState(pos);
			BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);

			ItemStack stick = new ItemStack(Items.STICK);
			hold(c, stick);
			c.check(!state.useItemOn(stick, c.world, c.player, InteractionHand.MAIN_HAND, hit).consumesAction(), "a stick does nothing");
			c.check(!c.world.getBlockState(pos).getValue(lumien.randomthings.block.AncientFurnaceBlock.HEATING), "still cold");

			c.player.getAbilities().instabuild = false;
			ItemStack stars = new ItemStack(Items.NETHER_STAR, 2);
			hold(c, stars);
			c.check(state.useItemOn(stars, c.world, c.player, InteractionHand.MAIN_HAND, hit).consumesAction(), "a nether star works");
			c.eq(1, stars.getCount(), "one star consumed");
			c.check(c.world.getBlockState(pos).getValue(lumien.randomthings.block.AncientFurnaceBlock.HEATING), "heating");

			c.check(!c.world.getBlockState(pos).useItemOn(stars, c.world, c.player, InteractionHand.MAIN_HAND, hit).consumesAction(), "a second star on a heating furnace does nothing");
			c.eq(1, stars.getCount(), "no second star consumed");
			release(c);
		}));

		tests.add(RTTest.named("retest.ancient_furnace.is_very_hard_to_break").run(c -> {
			c.floor();
			BlockPos pos = c.at(0, 1, 0);
			c.set(pos, lumien.randomthings.block.ModBlocks.ANCIENT_FURNACE);
			float hardness = c.world.getBlockState(pos).getDestroySpeed(c.world, pos);
			c.check(hardness >= 50.0F, "hardness like obsidian or harder, got " + hardness);
		}));

		// ---- Golden Chicken (#232) -------------------------------------------------------------------
		tests.add(RTTest.named("retest.golden_chicken.gold_ore_starts_a_timer_and_then_three_gold_ingots_drop").run(c -> {
			c.floor();
			lumien.randomthings.entity.GoldenChickenEntity chicken = ModEntityTypes.GOLDEN_CHICKEN.create(c.world, EntitySpawnReason.COMMAND);
			chicken.setPos(c.at(0, 1, 0).getX() + 0.5, c.at(0, 1, 0).getY(), c.at(0, 1, 0).getZ() + 0.5);
			chicken.setNoAi(true);
			c.world.addFreshEntity(chicken);
			c.spawnItem(0.5, 1.2, 0.5, new ItemStack(Items.GOLD_ORE, 2));
		}).waitTicks(3).run(c -> {
			lumien.randomthings.entity.GoldenChickenEntity chicken = c.world.getEntitiesOfClass(lumien.randomthings.entity.GoldenChickenEntity.class, c.area()).get(0);
			c.check(chicken.ingotDropTimer >= 600 && chicken.ingotDropTimer < 1200, "a timer of 30-60 s started, got " + chicken.ingotDropTimer);
			int ore = 0;

			for (net.minecraft.world.entity.item.ItemEntity item : c.itemEntities())
			{
				if (item.getItem().getItem() == Items.GOLD_ORE)
				{
					ore += item.getItem().getCount();
				}
			}

			c.eq(1, ore, "one ore was eaten");
			chicken.ingotDropTimer = 2;
		}).waitTicks(6).run(c -> {
			int ingots = 0;

			for (net.minecraft.world.entity.item.ItemEntity item : c.itemEntities())
			{
				if (item.getItem().getItem() == Items.GOLD_INGOT)
				{
					ingots += item.getItem().getCount();
				}
			}

			c.eq(3, ingots, "three gold ingots dropped");
		}));

		// ---- Spectre Illuminator (#272-274) -----------------------------------------------------------
		tests.add(RTTest.named("retest.spectre_illuminator.placing_it_centers_it_in_the_chunk_and_picking_it_up_removes_it").run(c -> {
			c.floor();
			standAt(c, 0, 1, 0);
			hold(c, new ItemStack(ModItems.SPECTRE_ILLUMINATOR));
			c.eq(InteractionResult.SUCCESS, useOn(c, c.at(0, 0, 0), Direction.UP), "placing");
			c.eq(1, c.world.getEntitiesOfClass(lumien.randomthings.entity.SpectreIlluminatorEntity.class, c.area().inflate(32)).size(), "one orb");
			release(c);
		}).waitUntil("the orb to centre itself and light the chunk", c -> {
			List<lumien.randomthings.entity.SpectreIlluminatorEntity> orbs = c.world.getEntitiesOfClass(lumien.randomthings.entity.SpectreIlluminatorEntity.class, c.area().inflate(32));
			return !orbs.isEmpty() && orbs.get(0).isIlluminated();
		}, 600).run(c -> {
			List<lumien.randomthings.entity.SpectreIlluminatorEntity> orbs = c.world.getEntitiesOfClass(lumien.randomthings.entity.SpectreIlluminatorEntity.class, c.area().inflate(32));
			net.minecraft.world.level.ChunkPos chunk = new net.minecraft.world.level.ChunkPos(orbs.get(0).blockPosition());
			c.check(orbs.get(0).isInChunk(c.world, chunk), "it lights its own chunk");
			c.check(!orbs.get(0).isInChunk(c.world, new net.minecraft.world.level.ChunkPos(chunk.x + 1, chunk.z)), "but not the chunk next to it");
		}).waitUntil("the light engine to light a dark air block in the chunk", c -> {
			return c.world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, illuminatorProbe(c)) >= 12;
		}, 600).run(c -> {
			List<lumien.randomthings.entity.SpectreIlluminatorEntity> orbs = c.world.getEntitiesOfClass(lumien.randomthings.entity.SpectreIlluminatorEntity.class, c.area().inflate(32));
			c.check(orbs.get(0).interact(c.player, InteractionHand.MAIN_HAND).consumesAction(), "right-click collects it");
		}).waitTicks(3).run(c -> {
			c.eq(0, c.world.getEntitiesOfClass(lumien.randomthings.entity.SpectreIlluminatorEntity.class, c.area().inflate(32)).size(), "the orb is gone");
			int items = 0;

			// the orb hovers at the middle of its chunk, up to 8 blocks from the test spot
			for (net.minecraft.world.entity.item.ItemEntity item : c.world.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, c.area().inflate(32)))
			{
				if (item.getItem().getItem() == ModItems.SPECTRE_ILLUMINATOR)
				{
					items++;
				}
			}

			c.check(items == 1 || c.player.getInventory().contains(new ItemStack(ModItems.SPECTRE_ILLUMINATOR)), "the item came back");
		}).waitUntil("the chunk to go dark again", c -> c.world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, illuminatorProbe(c)) < 6, 2400));

		// ---- Rune Dust and Rune Base (#161, #163-165) --------------------------------------------------
		tests.add(RTTest.named("retest.rune_dust.right_click_on_a_solid_top_places_a_rune_base_with_one_pixel_in_the_clicked_corner").run(c -> {
			c.floor();
			c.player.getAbilities().instabuild = false;
			net.minecraft.world.item.Item red = lumien.randomthings.item.RuneDustItems.BY_COLOR.get(net.minecraft.world.item.DyeColor.RED);
			hold(c, new ItemStack(red, 3));

			c.eq(InteractionResult.SUCCESS, useOnAt(c, c.at(2, 0, 2), Direction.UP, 0.1, 1.0, 0.1), "first pixel (south-west... corner 0,0)");
			c.eq(2, c.player.getMainHandItem().getCount(), "one dust used");
			lumien.randomthings.tileentity.RuneBaseTileEntity te = c.tile(c.at(2, 1, 2), lumien.randomthings.tileentity.RuneBaseTileEntity.class);
			c.eq(net.minecraft.world.item.DyeColor.RED, te.getRuneData()[0][0], "pixel 0,0");

			c.eq(InteractionResult.FAIL, useOnAt(c, c.at(2, 1, 2), Direction.UP, 0.1, 0.1, 0.1), "the same pixel again is refused");
			c.eq(2, c.player.getMainHandItem().getCount(), "no dust used for the refused click");
			c.eq(InteractionResult.SUCCESS, useOnAt(c, c.at(2, 1, 2), Direction.UP, 0.9, 0.1, 0.9), "the opposite corner works");
			c.eq(net.minecraft.world.item.DyeColor.RED, te.getRuneData()[3][3], "pixel 3,3");
			release(c);
		}));

		tests.add(RTTest.named("retest.rune_dust.refuses_side_faces_and_blocks_without_a_solid_top").run(c -> {
			c.floor();
			net.minecraft.world.item.Item blue = lumien.randomthings.item.RuneDustItems.BY_COLOR.get(net.minecraft.world.item.DyeColor.BLUE);
			hold(c, new ItemStack(blue, 3));
			c.check(!useOnAt(c, c.at(2, 0, 2), Direction.NORTH, 0.5, 0.5, 0.0).consumesAction(), "the side of a block does nothing");
			c.check(c.world.getBlockState(c.at(2, 1, 2)).isAir(), "no rune base appeared");
			release(c);
		}));

		tests.add(RTTest.named("retest.rune_base.breaking_it_drops_every_pixel_as_its_own_dust").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), lumien.randomthings.block.ModBlocks.RUNE_BASE);
			lumien.randomthings.tileentity.RuneBaseTileEntity te = c.tile(c.at(0, 1, 0), lumien.randomthings.tileentity.RuneBaseTileEntity.class);
			te.getRuneData()[0][0] = net.minecraft.world.item.DyeColor.RED;
			te.getRuneData()[1][2] = net.minecraft.world.item.DyeColor.RED;
			te.getRuneData()[3][3] = net.minecraft.world.item.DyeColor.GREEN;
			c.world.destroyBlock(c.at(0, 1, 0), false);
		}).waitTicks(2).run(c -> {
			c.eq(2, count(c, lumien.randomthings.item.RuneDustItems.BY_COLOR.get(net.minecraft.world.item.DyeColor.RED)), "red dust");
			c.eq(1, count(c, lumien.randomthings.item.RuneDustItems.BY_COLOR.get(net.minecraft.world.item.DyeColor.GREEN)), "green dust");
			c.check(c.world.getBlockState(c.at(0, 1, 0)).isAir(), "the base is gone");
		}));

		tests.add(RTTest.named("retest.rune_base.breaks_and_drops_its_pixels_when_the_block_below_is_removed").run(c -> {
			c.floor();
			c.setQuiet(c.at(0, 1, 0), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
			c.set(c.at(0, 2, 0), lumien.randomthings.block.ModBlocks.RUNE_BASE);
			c.tile(c.at(0, 2, 0), lumien.randomthings.tileentity.RuneBaseTileEntity.class).getRuneData()[2][1] = net.minecraft.world.item.DyeColor.YELLOW;
			c.remove(c.at(0, 1, 0));
		}).waitTicks(3).run(c -> {
			c.check(c.world.getBlockState(c.at(0, 2, 0)).isAir(), "the base broke with its support");
			c.eq(1, count(c, lumien.randomthings.item.RuneDustItems.BY_COLOR.get(net.minecraft.world.item.DyeColor.YELLOW)), "yellow dust");
		}));

		// ---- Ender Bridge (#247-250) ---------------------------------------------------------------------
		tests.add(RTTest.named("retest.ender_bridge.powered_it_scans_along_its_facing_and_sends_nearby_items_above_an_ender_anchor").run(c -> {
			c.floor();
			c.set(c.at(-3, 1, 0), bridge(Direction.EAST));
			c.set(c.at(3, 1, 0), lumien.randomthings.block.ModBlocks.ENDER_ANCHOR);
			c.spawnItem(-2.5, 1.2, 0.5, new ItemStack(Items.DIAMOND));
			c.set(c.at(-3, 1, 1), net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK);
			c.check(bridgeActive(c, c.at(-3, 1, 0)), "power starts the scan");
		}).waitUntil("the diamond to arrive above the anchor", c -> {
			net.minecraft.world.entity.item.ItemEntity d = firstItem(c, Items.DIAMOND);
			return d != null && Math.abs(d.getX() - (c.at(3, 2, 0).getX() + 0.5)) < 1.5;
		}, 80).run(c -> {
			c.check(!bridgeActive(c, c.at(-3, 1, 0)), "the bridge goes idle after teleporting");
			c.remove(c.at(-3, 1, 1));
		}).waitTicks(2).run(c -> {
			c.spawnItem(-2.5, 1.2, 0.5, new ItemStack(Items.EMERALD));
			c.set(c.at(-3, 1, 1), net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK);
			c.check(bridgeActive(c, c.at(-3, 1, 0)), "cutting the power and applying it again triggers it once more");
		}).waitUntil("the emerald to arrive above the anchor", c -> {
			net.minecraft.world.entity.item.ItemEntity e = firstItem(c, Items.EMERALD);
			return e != null && Math.abs(e.getX() - (c.at(3, 2, 0).getX() + 0.5)) < 1.5;
		}, 80));

		tests.add(RTTest.named("retest.ender_bridge.hitting_another_solid_block_first_just_goes_dark_without_teleporting").run(c -> {
			c.floor();
			c.set(c.at(-3, 1, 0), bridge(Direction.EAST));
			c.set(c.at(0, 1, 0), net.minecraft.world.level.block.Blocks.STONE);
			c.set(c.at(3, 1, 0), lumien.randomthings.block.ModBlocks.ENDER_ANCHOR);
			c.spawnItem(-2.5, 1.2, 0.5, new ItemStack(Items.DIAMOND));
			c.set(c.at(-3, 1, 1), net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK);
		}).waitTicks(40).run(c -> {
			net.minecraft.world.entity.item.ItemEntity d = firstItem(c, Items.DIAMOND);
			c.check(d != null && d.getX() < c.at(0, 0, 0).getX(), "the diamond stayed on the bridge side");
			c.check(!bridgeActive(c, c.at(-3, 1, 0)), "the bridge went dark");
		}));

		// ---- Floo fireplaces (#184-186, #188) -------------------------------------------------------------
		tests.add(RTTest.named("retest.floo.a_sign_converts_the_whole_brick_group_and_breaking_any_brick_reverts_all_of_them").run(c -> {
			c.floor();

			for (int x = 0; x < 3; x++)
			{
				c.setQuiet(c.at(x, 1, 0), net.minecraft.world.level.block.Blocks.BRICKS.defaultBlockState());
			}

			hold(c, new ItemStack(ModItems.FLOO_SIGN));
			c.eq(InteractionResult.SUCCESS, useOnAt(c, c.at(0, 1, 0), Direction.NORTH, 0.5, 0.5, 0.0), "sign on the first brick");

			for (int x = 0; x < 3; x++)
			{
				c.check(c.world.getBlockState(c.at(x, 1, 0)).getBlock() == lumien.randomthings.block.ModBlocks.FLOO_BRICK, "brick " + x + " became a Floo brick");
			}

			c.check(!useOnAt(c, c.at(1, 1, 0), Direction.NORTH, 0.5, 0.5, 0.0).consumesAction(), "a brick that is already part of a fireplace is refused");
			c.world.destroyBlock(c.at(1, 1, 0), false);

			for (int x = 0; x < 3; x++)
			{
				c.check(c.world.getBlockState(c.at(x, 1, 0)).getBlock() != lumien.randomthings.block.ModBlocks.FLOO_BRICK, "brick " + x + " reverted");
			}

			release(c);
		}));

		tests.add(RTTest.named("retest.floo.a_group_of_more_than_twenty_one_bricks_is_refused").run(c -> {
			c.floor();

			for (int x = -6; x <= 6; x++)
			{
				for (int z = 0; z < 2; z++)
				{
					c.setQuiet(c.at(x, 1, z), net.minecraft.world.level.block.Blocks.BRICKS.defaultBlockState());
				}
			}

			hold(c, new ItemStack(ModItems.FLOO_SIGN));
			c.check(!useOnAt(c, c.at(0, 1, 0), Direction.NORTH, 0.5, 0.5, 0.0).consumesAction(), "26 touching bricks are too many");
			c.check(c.world.getBlockState(c.at(0, 1, 0)).is(net.minecraft.world.level.block.Blocks.BRICKS), "nothing was converted");
			release(c);
		}));

		// ---- Time in a Bottle (#199) and the Eclipsed Clock (#196) ---------------------------------------------
		tests.add(RTTest.named("retest.time_in_a_bottle.carrying_it_stores_a_second_per_second_and_the_one_with_less_time_is_drained_when_two_are_carried").run(c -> {
			// the bottle acts when the game time is a multiple of 20 (storing) and of 60 (draining the lesser of two): move the clock forward to the next multiple of 60
			long now = c.world.getGameTime();
			((net.minecraft.world.level.storage.ServerLevelData) c.world.getLevelData()).setGameTime(now - (now % 60) + 60);
			ItemStack a = new ItemStack(ModItems.TIME_IN_A_BOTTLE);
			ItemStack b = new ItemStack(ModItems.TIME_IN_A_BOTTLE);
			lumien.randomthings.item.TimeInABottleItem.setStoredTime(a, 100);
			lumien.randomthings.item.TimeInABottleItem.setStoredTime(b, 5000);
			c.player.getInventory().setItem(0, a);
			c.player.getInventory().setItem(1, b);
			a.getItem().inventoryTick(a, c.world, c.player, 0, false);
			b.getItem().inventoryTick(b, c.world, c.player, 1, false);
			c.eq(0, lumien.randomthings.item.TimeInABottleItem.getStoredTime(a), "the bottle with less time was drained");
			c.eq(5020, lumien.randomthings.item.TimeInABottleItem.getStoredTime(b), "the fuller bottle gained a second (20 ticks)");
			c.player.getInventory().clearContent();
		}));

		final long[] startDayTime = new long[1];

		tests.add(RTTest.named("retest.eclipsed_clock.a_charged_time_in_a_bottle_fast_forwards_the_world_to_the_clocks_target_time").run(c -> {
			c.floor();
			c.setQuiet(c.at(0, 2, 0), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState());
			lumien.randomthings.entity.EclipsedClockEntity clock = new lumien.randomthings.entity.EclipsedClockEntity(c.world, c.at(0, 2, 1), Direction.SOUTH);
			c.world.addFreshEntity(clock);
			startDayTime[0] = c.world.getDayTime();
			clock.setTargetTime((int) ((startDayTime[0] + 1000) % 24000));
			ItemStack bottle = new ItemStack(ModItems.TIME_IN_A_BOTTLE);
			lumien.randomthings.item.TimeInABottleItem.setStoredTime(bottle, 5000);
			hold(c, bottle);
			c.player.getAbilities().instabuild = false;
			c.check(clock.interact(c.player, InteractionHand.MAIN_HAND).consumesAction(), "right-click with the bottle");
			int left = lumien.randomthings.item.TimeInABottleItem.getStoredTime(c.player.getMainHandItem());
			c.check(left <= 4020 && left >= 3900, "about 1000 ticks of stored time were used, left " + left);
			release(c);
		}).waitUntil("the day time to jump forward by about 1000 ticks", c -> c.world.getDayTime() >= startDayTime[0] + 1000, 400));

		// ---- Special crafting recipes (#138, #140, #279, #315-316, Spectre Anchor) -------------------------------------
		tests.add(RTTest.named("retest.recipes.every_special_crafting_recipe_is_registered_and_not_hidden_behind_a_missing_serializer").run(c -> {
			java.util.Set<Class<?>> wanted = new java.util.HashSet<>(List.of(lumien.randomthings.recipes.GoldenCompassSetPositionRecipe.class, lumien.randomthings.recipes.EmeraldCompassSetTargetRecipe.class, lumien.randomthings.recipes.PortkeyCamoRecipe.class,
					lumien.randomthings.recipes.DiaphanousSetRecipe.class, lumien.randomthings.recipes.DiaphanousInvertRecipe.class, lumien.randomthings.recipes.SpectreAnchorCombineRecipe.class));

			for (net.minecraft.world.item.crafting.RecipeHolder<?> holder : c.world.getServer().getRecipeManager().getRecipes())
			{
				wanted.remove(holder.value().getClass());
			}

			c.check(wanted.isEmpty(), "recipes missing from the recipe manager: " + wanted);
		}));

		tests.add(RTTest.named("retest.recipes.golden_compass_takes_the_position_of_a_position_filter").run(c -> {
			lumien.randomthings.recipes.GoldenCompassSetPositionRecipe recipe = new lumien.randomthings.recipes.GoldenCompassSetPositionRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
			ItemStack filter = new ItemStack(ModItems.POSITION_FILTER);
			ItemStack compass = new ItemStack(ModItems.GOLDEN_COMPASS);

			c.check(!recipe.matches(grid(compass, filter), c.world), "an unset filter does not match");
			lumien.randomthings.item.PositionFilterItem.setPosition(filter, "minecraft:overworld", new BlockPos(120, 64, -33));
			c.check(recipe.matches(grid(compass, filter), c.world), "a set filter matches");
			c.check(!recipe.matches(grid(compass, filter, new ItemStack(Items.STICK)), c.world), "anything else in the grid does not match");
			ItemStack result = recipe.assemble(grid(compass, filter), c.world.registryAccess());
			c.eq(120, StackTag.get(result).getInt("targetX"), "target x");
			c.eq(-33, StackTag.get(result).getInt("targetZ"), "target z");
			c.check(StackTag.get(compass) == null || !StackTag.get(compass).contains("targetX"), "the original compass was not modified");
		}));

		tests.add(RTTest.named("retest.recipes.emerald_compass_takes_the_player_of_a_bound_id_card").run(c -> {
			lumien.randomthings.recipes.EmeraldCompassSetTargetRecipe recipe = new lumien.randomthings.recipes.EmeraldCompassSetTargetRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
			ItemStack card = new ItemStack(ModItems.ID_CARD);
			ItemStack compass = new ItemStack(ModItems.EMERALD_COMPASS);

			c.check(!recipe.matches(grid(compass, card), c.world), "an unbound card does not match");
			hold(c, card);
			card.getItem().use(c.world, c.player, InteractionHand.MAIN_HAND);
			card = c.player.getMainHandItem();
			release(c);
			c.check(recipe.matches(grid(compass, card), c.world), "a bound card matches");
			ItemStack result = recipe.assemble(grid(compass, card), c.world.registryAccess());
			c.eq(c.player.getGameProfile().getId().toString(), StackTag.get(result).getString("uuid"), "target uuid");
		}));

		tests.add(RTTest.named("retest.recipes.portkey_camouflage_keeps_the_disguise_item_and_refuses_two_portkeys").run(c -> {
			lumien.randomthings.recipes.PortkeyCamoRecipe recipe = new lumien.randomthings.recipes.PortkeyCamoRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
			ItemStack portkey = new ItemStack(ModItems.PORTKEY);
			ItemStack diamond = new ItemStack(Items.DIAMOND, 5);

			c.check(recipe.matches(grid(portkey, diamond), c.world), "portkey + one other item");
			c.check(!recipe.matches(grid(portkey, new ItemStack(ModItems.PORTKEY)), c.world), "two portkeys do not match");
			c.check(!recipe.matches(grid(portkey, diamond, new ItemStack(Items.STICK)), c.world), "two other items do not match");
			ItemStack result = recipe.assemble(grid(portkey, diamond), c.world.registryAccess());
			c.check(StackTag.get(result).contains("camo"), "the result carries the camo data");
			c.eq(5, diamond.getCount(), "the input stack was not modified");
			c.eq(1, recipe.getRemainingItems(grid(portkey, diamond)).get(1).getCount(), "one disguise item is handed back");
		}));

		tests.add(RTTest.named("retest.recipes.diaphanous_block_takes_a_block_and_toggles_inverted_and_refuses_tile_entity_blocks").run(c -> {
			lumien.randomthings.recipes.DiaphanousSetRecipe set = new lumien.randomthings.recipes.DiaphanousSetRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
			lumien.randomthings.recipes.DiaphanousInvertRecipe invert = new lumien.randomthings.recipes.DiaphanousInvertRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
			ItemStack diaphanous = new ItemStack(lumien.randomthings.block.ModBlocks.DIAPHANOUS_BLOCK.asItem(), 3);

			c.check(set.matches(grid(diaphanous, new ItemStack(Items.STONE)), c.world), "diaphanous + stone");
			c.check(!set.matches(grid(diaphanous, new ItemStack(Items.CHEST)), c.world), "a chest (tile entity) is refused");
			c.check(!set.matches(grid(diaphanous, new ItemStack(Items.STICK)), c.world), "a non-block item is refused");
			ItemStack shown = set.assemble(grid(diaphanous, new ItemStack(Items.STONE)), c.world.registryAccess());
			c.eq("minecraft:stone", StackTag.get(shown).getString("block"), "displayed block");
			c.eq(1, shown.getCount(), "one block per craft");

			c.check(invert.matches(grid(shown), c.world), "a lone diaphanous block can be inverted");
			c.check(!invert.matches(grid(shown, new ItemStack(Items.STONE)), c.world), "not with anything else");
			ItemStack inverted = invert.assemble(grid(shown), c.world.registryAccess());
			c.check(StackTag.get(inverted).getBoolean("inverted"), "inverted once");
			c.check(!StackTag.get(invert.assemble(grid(inverted), c.world.registryAccess())).getBoolean("inverted"), "and back");
			c.eq("minecraft:stone", StackTag.get(inverted).getString("block"), "the displayed block survives inverting");
		}));

		tests.add(RTTest.named("retest.recipes.spectre_anchor_combines_with_one_unstackable_item_once").run(c -> {
			lumien.randomthings.recipes.SpectreAnchorCombineRecipe recipe = new lumien.randomthings.recipes.SpectreAnchorCombineRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
			ItemStack anchor = new ItemStack(ModItems.SPECTRE_ANCHOR);
			ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);

			c.check(recipe.matches(grid(anchor, sword), c.world), "anchor + sword");
			c.check(!recipe.matches(grid(anchor, new ItemStack(Items.DIAMOND, 4)), c.world), "stackable items are refused");
			ItemStack result = recipe.assemble(grid(anchor, sword), c.world.registryAccess());
			c.check(StackTag.get(result).contains("spectreAnchor"), "the result is anchored");
			c.check(!recipe.matches(grid(anchor, result), c.world), "an already anchored item is refused");
		}));

		// ---- Lang entries (#118) ---------------------------------------------------------------------------
		tests.add(RTTest.named("retest.lang.every_mod_item_and_block_has_a_name_in_en_us_json").run(c -> {
			com.google.gson.JsonObject lang;

			try (java.io.InputStream in = RetestBehaviorTests.class.getResourceAsStream("/assets/randomthings/lang/en_us.json"))
			{
				lang = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
			}

			List<String> missing = new ArrayList<>();

			for (net.minecraft.world.item.Item item : net.minecraft.core.registries.BuiltInRegistries.ITEM)
			{
				if ("randomthings".equals(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getNamespace()) && !lang.has(item.getDescriptionId()))
				{
					missing.add(item.getDescriptionId());
				}
			}

			for (net.minecraft.world.level.block.Block block : net.minecraft.core.registries.BuiltInRegistries.BLOCK)
			{
				if ("randomthings".equals(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(block).getNamespace()) && !lang.has(block.getDescriptionId()))
				{
					missing.add(block.getDescriptionId());
				}
			}

			c.check(missing.isEmpty(), missing.size() + " names missing from en_us.json: " + missing);
		}));

		// ---- Biome Radar: an empty hand takes the crystal back (idle and finished) ------------------------------------
		tests.add(RTTest.named("retest.biome_radar.an_empty_hand_takes_the_crystal_back_out_of_an_idle_radar").run(c -> {
			c.floor();
			BlockPos radar = c.at(0, 1, 0);
			c.set(radar, lumien.randomthings.block.ModBlocks.BIOME_RADAR);
			ItemStack crystal = new ItemStack(ModItems.BIOME_CRYSTAL);
			lumien.randomthings.item.BiomeCrystalItem.setBiome(crystal, lumien.randomthings.util.BiomeUtil.byId(net.minecraft.resources.ResourceLocation.parse("minecraft:snowy_plains")));
			c.tile(radar, lumien.randomthings.tileentity.BiomeRadarTileEntity.class).setCrystal(crystal);
			hold(c, ItemStack.EMPTY);
			lumien.randomthings.debug.BlockUse.use(c.world.getBlockState(radar), c.world, c.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atLowerCornerOf(radar), Direction.UP, radar, false));
			c.check(c.player.getMainHandItem().getItem() == ModItems.BIOME_CRYSTAL, "the crystal is in the player's hand");
			c.check(c.tile(radar, lumien.randomthings.tileentity.BiomeRadarTileEntity.class).getCurrentCrystal().isEmpty(), "the radar is empty");
			release(c);
		}));

		tests.add(RTTest.named("retest.biome_radar.an_empty_hand_takes_the_crystal_back_out_of_a_radar_that_finished_searching").run(c -> {
			c.floor();
			BlockPos radar = c.at(0, 1, 0);
			c.set(radar, lumien.randomthings.block.ModBlocks.BIOME_RADAR);
			int[][] bars = { { 0, 2, 0 }, { 0, 3, 0 }, { 1, 3, 0 }, { -1, 3, 0 }, { 1, 4, 0 }, { -1, 4, 0 }, { 0, 3, 1 }, { 0, 3, -1 }, { 0, 4, 1 }, { 0, 4, -1 } };

			for (int[] b : bars)
			{
				c.set(c.at(b[0], b[1], b[2]), net.minecraft.world.level.block.Blocks.IRON_BARS);
			}

			ItemStack crystal = new ItemStack(ModItems.BIOME_CRYSTAL);
			lumien.randomthings.item.BiomeCrystalItem.setBiome(crystal, lumien.randomthings.util.BiomeUtil.byId(net.minecraft.resources.ResourceLocation.parse("minecraft:plains")));
			c.tile(radar, lumien.randomthings.tileentity.BiomeRadarTileEntity.class).setCrystal(crystal);
			c.set(c.at(1, 1, 0), net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK);
		}).waitUntil("the radar to finish", c -> c.tile(c.at(0, 1, 0), lumien.randomthings.tileentity.BiomeRadarTileEntity.class).getState() == lumien.randomthings.tileentity.BiomeRadarTileEntity.STATE.FINISHED, 200).run(c -> {
			BlockPos radar = c.at(0, 1, 0);
			hold(c, ItemStack.EMPTY);
			lumien.randomthings.debug.BlockUse.use(c.world.getBlockState(radar), c.world, c.player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atLowerCornerOf(radar), Direction.UP, radar, false));
			c.check(c.player.getMainHandItem().getItem() == ModItems.BIOME_CRYSTAL, "the crystal is in the player's hand");
			lumien.randomthings.tileentity.BiomeRadarTileEntity te = c.tile(radar, lumien.randomthings.tileentity.BiomeRadarTileEntity.class);
			c.check(te.getCurrentCrystal().isEmpty(), "the radar is empty");
			c.eq(lumien.randomthings.tileentity.BiomeRadarTileEntity.STATE.IDLE, te.getState(), "and back to idle");
			release(c);
		}));

		return tests;
	}
}
