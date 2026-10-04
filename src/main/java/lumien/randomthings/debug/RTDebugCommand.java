package lumien.randomthings.debug;

import java.util.ArrayList;
import java.util.List;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import lumien.randomthings.lib.ModConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ResourceLocationArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Debug commands (only registered when the debug flag is on, see {@link RTDebug}):
 * <pre>
 *   /rtdebug gui &lt;id&gt; [sneak]
 *   /rtdebug allguis [delayTicks]
 * </pre>
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

	public static void register(CommandDispatcher<CommandSource> dispatcher)
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

		dispatcher.register(Commands.literal("rtdebug").requires(source -> source.hasPermissionLevel(2))
				.then(Commands.literal("allguis")
						.executes(context -> startAllGuis(context, 30))
						.then(Commands.argument("delayTicks", IntegerArgumentType.integer(5, 400))
								.executes(context -> startAllGuis(context, IntegerArgumentType.getInteger(context, "delayTicks")))))
				.then(Commands.literal("gui")
						.then(Commands.argument("id", ResourceLocationArgument.resourceLocation())
								.suggests((context, builder) -> ISuggestionProvider.suggestIterable(allIds(), builder))
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

	private static int open(CommandContext<CommandSource> context, boolean sneak) throws CommandSyntaxException
	{
		CommandSource source = context.getSource();
		ServerPlayerEntity player = source.asPlayer();

		ResourceLocation id = ResourceLocationArgument.getResourceLocation(context, "id");

		// a bare "igniter" parses as minecraft:igniter - assume this mod
		if ("minecraft".equals(id.getNamespace()) && !ForgeRegistries.BLOCKS.containsKey(id) && !ForgeRegistries.ITEMS.containsKey(id))
		{
			id = new ResourceLocation(ModConstants.MOD_ID, id.getPath());
		}

		String[] how = new String[1];
		boolean opened = openGui(player, id, sneak, placePos(player), how);

		if (opened)
		{
			source.sendFeedback(new StringTextComponent("[rtdebug] " + id + ": GUI opened (" + how[0] + ")"), false);
			return 1;
		}

		source.sendErrorMessage(new StringTextComponent("[rtdebug] " + id + ": no GUI opened (" + how[0] + ")"));
		return 0;
	}

	private static BlockPos placePos(ServerPlayerEntity player)
	{
		return player.getPosition().offset(player.getHorizontalFacing(), 3);
	}

	/** Opens the GUI of the given block/item as a right-click would; {@code how[0]} receives a short description. */
	private static boolean openGui(ServerPlayerEntity player, ResourceLocation id, boolean sneak, BlockPos blockPos, String[] how)
	{
		ServerWorld world = player.getServerWorld();
		boolean wasSneaking = player.isSneaking();
		player.setSneaking(sneak);

		try
		{
			boolean isBlock = ModConstants.MOD_ID.equals(id.getNamespace()) && ForgeRegistries.BLOCKS.containsKey(id);
			boolean isItem = ModConstants.MOD_ID.equals(id.getNamespace()) && ForgeRegistries.ITEMS.containsKey(id);

			if (!isBlock && !isItem)
			{
				how[0] = "no Random Things block or item '" + id + "'";
				return false;
			}

			Container before = player.openContainer;

			if (isBlock)
			{
				Block block = ForgeRegistries.BLOCKS.getValue(id);

				world.setBlockState(blockPos, block.getDefaultState(), 3);

				BlockState state = world.getBlockState(blockPos);
				// run the placement hooks too (e.g. the Ender Mailbox only opens for its owner, set on placement)
				state.getBlock().onBlockPlacedBy(world, blockPos, state, player, new ItemStack(block));
				BlockRayTraceResult hit = new BlockRayTraceResult(new Vec3d(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D), player.getHorizontalFacing().getOpposite(), blockPos, false);
				boolean handled = state.onBlockActivated(world, player, Hand.MAIN_HAND, hit);

				net.minecraft.tileentity.TileEntity te = world.getTileEntity(blockPos);

				how[0] = "placed block at " + blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ() + ", state " + state + ", tile entity " + (te == null ? "null" : te.getClass().getSimpleName()) + ", activate " + (handled ? "handled" : "not handled");
			}
			else
			{
				Item item = ForgeRegistries.ITEMS.getValue(id);
				ItemStack previous = player.getHeldItemMainhand();

				ItemStack stack = new ItemStack(item);
				player.setHeldItem(Hand.MAIN_HAND, stack);

				if (!previous.isEmpty() && previous.getItem() != item)
				{
					player.inventory.placeItemBackInInventory(world, previous);
				}

				ActionResult<ItemStack> result = item.onItemRightClick(world, player, Hand.MAIN_HAND);

				how[0] = "used item, result " + result.getType();
			}

			boolean opened = player.openContainer != before && player.openContainer != player.container;

			RTDebug.log("rtdebug gui {} (sneak={}): {} -> GUI {}", id, sneak, how[0], opened ? "opened" : "NOT opened");

			return opened;
		}
		finally
		{
			player.setSneaking(wasSneaking);
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

	private static class AllGuisRun
	{
		ServerPlayerEntity player;
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

	private static AllGuisRun run;

	private static int startAllGuis(CommandContext<CommandSource> context, int delay) throws CommandSyntaxException
	{
		if (run != null)
		{
			context.getSource().sendErrorMessage(new StringTextComponent("[rtdebug] allguis already running"));
			return 0;
		}

		AllGuisRun r = new AllGuisRun();
		r.player = context.getSource().asPlayer();
		r.delay = delay;
		r.pos = placePos(r.player);
		r.originalHand = r.player.getHeldItemMainhand().copy();
		r.wait = 10;
		allGuisFinished = false;
		run = r;

		context.getSource().sendFeedback(new StringTextComponent("[rtdebug] opening " + ALL_GUIS.length + " GUIs, " + delay + " ticks apart - screenshots go to screenshots/randomthings_debug"), false);
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
			r.player.closeContainer();

			if (r.placedBlock)
			{
				r.player.getServerWorld().setBlockState(r.pos, Blocks.AIR.getDefaultState(), 3);
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
			r.player.setHeldItem(Hand.MAIN_HAND, r.originalHand);
			String summary = "[rtdebug] allguis finished: " + r.opened + "/" + ALL_GUIS.length + " opened" + (r.failed.isEmpty() ? "" : ", NOT opened: " + r.failed);
			RTDebug.log(summary);
			r.player.sendMessage(new StringTextComponent(summary));
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
