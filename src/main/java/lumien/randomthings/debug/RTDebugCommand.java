package lumien.randomthings.debug;

import net.minecraft.core.registries.Registries;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Debug commands (only registered when the debug flag is on, see {@link RTDebug}):
 * <pre>
 *   /rtdebug gui &lt;id&gt; [sneak]
 *   /rtdebug allguis [delayTicks]
 *   /rtdebug test [name filter]
 * </pre>
 * {@code test} runs the in-game test suite (see {@code debug.test.RTTestRunner}).
 * {@code gui} opens the GUI of any Random Things block or item exactly as a right-click would. For
 * a block it is placed three blocks in front of you (the GUI's container needs the block to stay
 * put, so it is left there) and activated; for an item it is put in your main hand (your previous
 * hand item goes back into the inventory) and used. {@code sneak} makes the action happen while
 * sneaking, e.g. the Redstone Remote's edit screen.
 * <p>
 * {@code allguis} walks through every GUI in the mod one after another ({@code delayTicks} apart,
 * default 30), cleaning up the placed block each time and restoring your hand item at the end. With
 * the GUI screenshot option on, this produces one screenshot per GUI with no clicking.
 */
public class RTDebugCommand
{
	private static boolean tickRegistered;

	/** Set when an {@code allguis} run completes (read by the client auto-test to exit). */
	public static volatile boolean allGuisFinished;

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
	{
		if (!RTDebug.isLogging())
		{
			return;
		}

		if (!tickRegistered)
		{
			tickRegistered = true;
			MinecraftForge.EVENT_BUS.addListener(RTDebugCommand::onServerTick);
		}

		dispatcher.register(Commands.literal("rtdebug").requires(source -> source.hasPermission(2))
				.then(Commands.literal("allguis")
						.executes(context -> startAllGuis(context, 30))
						.then(Commands.argument("delayTicks", IntegerArgumentType.integer(5, 400))
								.executes(context -> startAllGuis(context, IntegerArgumentType.getInteger(context, "delayTicks")))))
				.then(Commands.literal("scene")
						.then(Commands.argument("id", com.mojang.brigadier.arguments.StringArgumentType.word())
								.suggests((context, builder) -> SharedSuggestionProvider.suggest(RenderScenes.ids(), builder))
								.executes(context -> {
									String error = RenderScenes.build(context.getSource().getPlayerOrException(), com.mojang.brigadier.arguments.StringArgumentType.getString(context, "id"));

									if (error != null)
									{
										context.getSource().sendFailure(Component.literal("[rtdebug] " + error));
										return 0;
									}

									return 1;
								})))
				.then(Commands.literal("fixture")
						.then(Commands.literal("build").executes(context -> {
							for (String line : FixtureWorld.build(context.getSource().getPlayerOrException()))
							{
								lumien.randomthings.debug.RTDebug.log("fixture: {}", line);
								context.getSource().sendSuccess(() -> Component.literal("[rtdebug fixture] " + line), false);
							}
							return 1;
						}))
						.then(Commands.literal("verify").executes(context -> {
							java.util.List<String> lines = FixtureWorld.verify(context.getSource().getLevel());
							try
							{
								java.nio.file.Files.writeString(java.nio.file.Path.of("randomthings_fixture.txt"), String.join("\n", lines) + "\n");
							}
							catch (java.io.IOException e)
							{
								lumien.randomthings.debug.RTDebug.log("fixture: cannot write the report: {}", e);
							}
							for (String line : lines)
							{
								lumien.randomthings.debug.RTDebug.log("fixture: {}", line);
								context.getSource().sendSuccess(() -> Component.literal("[rtdebug fixture] " + line), false);
							}
							return 1;
						})))
				.then(Commands.literal("manual")
						.executes(context -> {
							net.minecraft.server.level.ServerPlayer player = context.getSource().getPlayerOrException();
							for (String line : ManualTestWorld.build(player))
							{
								context.getSource().sendSuccess(() -> Component.literal("[rtdebug manual] " + line), false);
							}
							return 1;
						}))
				.then(Commands.literal("test")
						.executes(context -> startTests(context, null))
						.then(Commands.argument("filter", com.mojang.brigadier.arguments.StringArgumentType.greedyString())
								.executes(context -> startTests(context, com.mojang.brigadier.arguments.StringArgumentType.getString(context, "filter")))))
				.then(Commands.literal("gui")
						.then(Commands.argument("id", ResourceLocationArgument.id())
								.suggests((context, builder) -> SharedSuggestionProvider.suggestResource(allIds(), builder))
								.executes(context -> open(context, false))
								.then(Commands.literal("sneak").executes(context -> open(context, true))))));
	}

	private static List<ResourceLocation> allIds()
	{
		List<ResourceLocation> ids = new ArrayList<>();

		for (ResourceLocation id : ForgeRegistries.BLOCKS.getKeys())
		{
			if (ModConstants.MOD_ID.equals(id.getNamespace()))
			{
				ids.add(id);
			}
		}

		for (ResourceLocation id : ForgeRegistries.ITEMS.getKeys())
		{
			if (ModConstants.MOD_ID.equals(id.getNamespace()) && !ids.contains(id))
			{
				ids.add(id);
			}
		}

		return ids;
	}

	private static int open(CommandContext<CommandSourceStack> context, boolean sneak) throws CommandSyntaxException
	{
		CommandSourceStack source = context.getSource();
		ServerPlayer player = source.getPlayerOrException();

		ResourceLocation id = ResourceLocationArgument.getId(context, "id");

		// a bare "igniter" parses as minecraft:igniter - assume this mod
		if ("minecraft".equals(id.getNamespace()) && !ForgeRegistries.BLOCKS.containsKey(id) && !ForgeRegistries.ITEMS.containsKey(id))
		{
			id = new ResourceLocation(ModConstants.MOD_ID, id.getPath());
		}

		String[] how = new String[1];
		boolean opened = openGui(player, id, sneak, placePos(player), how);
		final ResourceLocation openedId = id;

		if (opened)
		{
			source.sendSuccess(() -> Component.literal("[rtdebug] " + openedId + ": GUI opened (" + how[0] + ")"), false);
			return 1;
		}

		source.sendFailure(Component.literal("[rtdebug] " + id + ": no GUI opened (" + how[0] + ")"));
		return 0;
	}

	private static BlockPos placePos(ServerPlayer player)
	{
		return player.blockPosition().relative(player.getDirection(), 3);
	}

	/** Opens the GUI of the given block/item as a right-click would; {@code how[0]} receives a short description. */
	private static boolean openGui(ServerPlayer player, ResourceLocation id, boolean sneak, BlockPos blockPos, String[] how)
	{
		ServerLevel world = player.serverLevel();
		boolean wasSneaking = player.isShiftKeyDown();
		player.setShiftKeyDown(sneak);

		try
		{
			boolean isBlock = ModConstants.MOD_ID.equals(id.getNamespace()) && ForgeRegistries.BLOCKS.containsKey(id);
			boolean isItem = ModConstants.MOD_ID.equals(id.getNamespace()) && ForgeRegistries.ITEMS.containsKey(id);

			if (!isBlock && !isItem)
			{
				how[0] = "no Random Things block or item '" + id + "'";
				return false;
			}

			AbstractContainerMenu before = player.containerMenu;

			if (isBlock)
			{
				Block block = ForgeRegistries.BLOCKS.getValue(id);

				world.setBlock(blockPos, block.defaultBlockState(), 3);

				BlockState state = world.getBlockState(blockPos);
				// run the placement hooks too (e.g. the Ender Mailbox only opens for its owner, set on placement)
				state.getBlock().setPlacedBy(world, blockPos, state, player, new ItemStack(block));
				BlockHitResult hit = new BlockHitResult(new Vec3(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D), player.getDirection().getOpposite(), blockPos, false);
				boolean handled = state.use(world, player, InteractionHand.MAIN_HAND, hit).consumesAction();

				net.minecraft.world.level.block.entity.BlockEntity te = world.getBlockEntity(blockPos);

				how[0] = "placed block at " + blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ() + ", state " + state + ", tile entity " + (te == null ? "null" : te.getClass().getSimpleName()) + ", activate " + (handled ? "handled" : "not handled");
			}
			else
			{
				Item item = ForgeRegistries.ITEMS.getValue(id);
				ItemStack previous = player.getMainHandItem();

				ItemStack stack = new ItemStack(item);
				player.setItemInHand(InteractionHand.MAIN_HAND, stack);

				if (!previous.isEmpty() && previous.getItem() != item)
				{
					player.getInventory().placeItemBackInInventory(previous);
				}

				InteractionResultHolder<ItemStack> result = item.use(world, player, InteractionHand.MAIN_HAND);

				how[0] = "used item, result " + result.getResult();
			}

			boolean opened = player.containerMenu != before && player.containerMenu != player.inventoryMenu;

			RTDebug.log("rtdebug gui {} (sneak={}): {} -> GUI {}", id, sneak, how[0], opened ? "opened" : "NOT opened");

			return opened;
		}
		finally
		{
			player.setShiftKeyDown(wasSneaking);
		}
	}

	// ---- /rtdebug allguis -------------------------------------------------------------------

	/** id + whether to sneak, in the order of GUI_TEST_LIST.md */
	private static final String[][] ALL_GUIS = {
			{ "advanced_redstone_torch", "" }, { "analog_emitter", "" }, { "igniter", "" }, { "online_detector", "" }, { "advanced_redstone_repeater", "" },
			{ "iron_dropper", "" }, { "inventory_tester", "" }, { "chat_detector", "" }, { "redstone_observer", "" }, { "basic_redstone_interface", "" },
			{ "advanced_redstone_interface", "" }, { "potion_vaporizer", "" }, { "entity_detector", "" }, { "advanced_item_collector", "" },
			{ "filtered_super_lubricent_platform", "" }, { "notification_interface", "" }, { "global_chat_detector", "" }, { "sound_dampener", "" },
			{ "imbuing_station", "" }, { "ender_mailbox", "" }, { "block_destabilizer", "" }, { "dyeing_machine", "" },
			{ "chunk_analyzer", "" }, { "sound_recorder", "" }, { "portable_sound_dampener", "" }, { "ender_letter", "" },
			{ "redstone_remote", "sneak" }, { "redstone_remote", "" } };

	/** The (block/item id, "sneak" or "") list shared with the client click test. */
	public static String[][] guiList()
	{
		return ALL_GUIS;
	}

	private static class AllGuisRun
	{
		ServerPlayer player;
		int delay;
		int index;
		int wait;
		BlockPos pos;
		ItemStack originalHand;
		boolean placedBlock;
		boolean cleaned = true;
		int opened;
		final List<String> failed = new ArrayList<>();
	}

	private static int startTests(CommandContext<CommandSourceStack> context, String filter) throws CommandSyntaxException
	{
		String error = lumien.randomthings.debug.test.RTTestRunner.start(context.getSource().getPlayerOrException(), filter == null || filter.trim().isEmpty() ? null : filter.trim());

		if (error != null)
		{
			context.getSource().sendFailure(Component.literal("[rtdebug] " + error));
			return 0;
		}

		context.getSource().sendSuccess(() -> Component.literal("[rtdebug] running " + lumien.randomthings.debug.test.RTTestRunner.queued() + " tests - results in randomthings_tests.txt"), false);
		return 1;
	}

	private static AllGuisRun run;

	private static int startAllGuis(CommandContext<CommandSourceStack> context, int delay) throws CommandSyntaxException
	{
		if (run != null)
		{
			context.getSource().sendFailure(Component.literal("[rtdebug] allguis already running"));
			return 0;
		}

		AllGuisRun r = new AllGuisRun();
		r.player = context.getSource().getPlayerOrException();
		r.delay = delay;
		r.pos = placePos(r.player);
		r.originalHand = r.player.getMainHandItem().copy();
		r.wait = 10;
		allGuisFinished = false;
		run = r;

		context.getSource().sendSuccess(() -> Component.literal("[rtdebug] opening " + ALL_GUIS.length + " GUIs, " + delay + " ticks apart - screenshots go to screenshots/randomthings_debug"), false);
		return 1;
	}

	private static void onServerTick(TickEvent.ServerTickEvent event)
	{
		AllGuisRun r = run;

		if (r == null || event.phase != TickEvent.Phase.END)
		{
			return;
		}

		if (r.wait-- > 0)
		{
			return;
		}

		if (r.cleaned)
		{
			// phase 2 (a few ticks after the cleanup): open the next GUI
		}
		else
		{
			// phase 1: tidy up the previous step. Removing the old block and placing the next one in
			// the same tick leaves the old tile entity pending removal, and blocks placed right after
			// a tile-entity block (Analog Emitter, Ender Mailbox) then had no tile entity to open a
			// GUI with - so wait a few ticks between the two.
			r.player.doCloseContainer();

			if (r.placedBlock)
			{
				r.player.level().setBlock(r.pos, Blocks.AIR.defaultBlockState(), 3);
				r.placedBlock = false;
			}

			r.cleaned = true;

			if (r.index < ALL_GUIS.length)
			{
				r.wait = 5;
				return;
			}
		}

		if (r.index >= ALL_GUIS.length)
		{
			r.player.setItemInHand(InteractionHand.MAIN_HAND, r.originalHand);
			String summary = "[rtdebug] allguis finished: " + r.opened + "/" + ALL_GUIS.length + " opened" + (r.failed.isEmpty() ? "" : ", NOT opened: " + r.failed);
			RTDebug.log(summary);
			r.player.sendSystemMessage(Component.literal(summary));
			run = null;
			allGuisFinished = true;
			return;
		}

		String[] entry = ALL_GUIS[r.index++];
		ResourceLocation id = new ResourceLocation(ModConstants.MOD_ID, entry[0]);
		String[] how = new String[1];
		boolean sneak = "sneak".equals(entry[1]);
		boolean opened = openGui(r.player, id, sneak, r.pos, how);
		r.placedBlock = ForgeRegistries.BLOCKS.containsKey(id);

		if (opened)
		{
			r.opened++;
		}
		else
		{
			r.failed.add(entry[0] + (sneak ? "(sneak)" : "") + ": " + how[0]);
		}

		r.cleaned = false;
		r.wait = r.delay;
	}
}
