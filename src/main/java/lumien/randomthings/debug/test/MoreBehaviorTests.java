package lumien.randomthings.debug.test;

import java.util.ArrayList;
import java.util.List;

import lumien.randomthings.block.BlockDestabilizerBlock;
import lumien.randomthings.block.ContactButtonBlock;
import lumien.randomthings.block.ContactLeverBlock;
import lumien.randomthings.block.ModBlocks;
import lumien.randomthings.tileentity.ImbuingStationTileEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneLampBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * Second batch of gameplay tests (backlog Q8): Contact Button/Lever, Imbuing Station, Block Destabilizer.
 * Same conventions as {@link BehaviorTests}.
 */
final class MoreBehaviorTests
{
	private MoreBehaviorTests()
	{
	}

	/** What a real right click on {@code pos} does as far as the mod is concerned (the contact blocks listen for this event). */
	private static void rightClick(TestCtx c, BlockPos pos)
	{
		MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(c.player, Hand.MAIN_HAND, pos, Direction.UP));
	}

	static List<RTTest> create()
	{
		List<RTTest> tests = new ArrayList<>();

		// ---- Block Destabilizer ---------------------------------------------------------------
		tests.add(RTTest.named("behavior.block_destabilizer.drops_the_connected_group_it_faces_and_leaves_the_rest").run(c -> {
			c.floor();
			c.set(c.at(0, 2, 0), ModBlocks.BLOCK_DESTABILIZER.getDefaultState().with(BlockDestabilizerBlock.FACING, Direction.EAST));
			// A floating pair of stone directly in front of it (connected), and a separate floating stone further away.
			c.set(c.at(1, 2, 0), Blocks.STONE);
			c.set(c.at(2, 2, 0), Blocks.STONE);
			c.set(c.at(6, 2, 0), Blocks.STONE);
			c.set(c.at(0, 2, 1), Blocks.REDSTONE_BLOCK);
		}).waitTicks(80).run(c -> {
			c.check(c.world.getBlockState(c.at(1, 2, 0)).isAir(c.world, c.at(1, 2, 0)), "targeted stone must have fallen from its place");
			c.check(c.world.getBlockState(c.at(2, 2, 0)).isAir(c.world, c.at(2, 2, 0)), "the stone connected to it must have fallen too");
			c.eq(Blocks.STONE, c.world.getBlockState(c.at(1, 1, 0)).getBlock(), "fallen stone lands on the floor (1)");
			c.eq(Blocks.STONE, c.world.getBlockState(c.at(2, 1, 0)).getBlock(), "fallen stone lands on the floor (2)");
			c.eq(Blocks.STONE, c.world.getBlockState(c.at(6, 2, 0)).getBlock(), "an unconnected stone must stay where it was");
		}));

		// ---- Contact Button -------------------------------------------------------------------
		tests.add(RTTest.named("behavior.contact_button.right_clicking_the_block_it_faces_pulses_power_then_resets").run(c -> {
			c.floor();
			// Button east of the stone, facing back at it (west): the stone is what gets clicked.
			c.set(c.at(0, 1, 0), Blocks.STONE);
			c.set(c.at(1, 1, 0), ModBlocks.CONTACT_BUTTON.getDefaultState().with(ContactButtonBlock.FACING, Direction.WEST));
			c.set(c.at(1, 2, 0), Blocks.REDSTONE_LAMP);
			// Decoy: a button that faces somewhere else must not react to a click on its neighbour.
			c.set(c.at(0, 1, 6), Blocks.STONE);
			c.set(c.at(1, 1, 6), ModBlocks.CONTACT_BUTTON.getDefaultState().with(ContactButtonBlock.FACING, Direction.NORTH));
			rightClick(c, c.at(0, 1, 6));
			c.check(!c.world.getBlockState(c.at(1, 1, 6)).get(ContactButtonBlock.POWERED), "a button facing another way must not activate");
			rightClick(c, c.at(0, 1, 0));
			c.check(c.world.getBlockState(c.at(1, 1, 0)).get(ContactButtonBlock.POWERED), "button must be powered right after the click");
		}).waitTicks(3).run(c -> {
			c.check(c.world.getBlockState(c.at(1, 2, 0)).get(RedstoneLampBlock.LIT), "lamp next to the powered button must light");
		}).waitTicks(30).run(c -> {
			c.check(!c.world.getBlockState(c.at(1, 1, 0)).get(ContactButtonBlock.POWERED), "button must release by itself");
			c.check(!c.world.getBlockState(c.at(1, 2, 0)).get(RedstoneLampBlock.LIT), "lamp must go dark again after the button releases");
		}));

		// ---- Contact Lever --------------------------------------------------------------------
		tests.add(RTTest.named("behavior.contact_lever.right_click_toggles_and_stays").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), Blocks.STONE);
			c.set(c.at(1, 1, 0), ModBlocks.CONTACT_LEVER.getDefaultState().with(ContactLeverBlock.FACING, Direction.WEST));
			rightClick(c, c.at(0, 1, 0));
			c.check(c.world.getBlockState(c.at(1, 1, 0)).get(ContactLeverBlock.POWERED), "first click turns the lever on");
		}).waitTicks(30).run(c -> {
			c.check(c.world.getBlockState(c.at(1, 1, 0)).get(ContactLeverBlock.POWERED), "a lever must stay on until clicked again");
			rightClick(c, c.at(0, 1, 0));
			c.check(!c.world.getBlockState(c.at(1, 1, 0)).get(ContactLeverBlock.POWERED), "second click turns it off");
		}));

		// ---- Imbuing Station ------------------------------------------------------------------
		tests.add(RTTest.named("behavior.imbuing_station.brews_mossy_cobblestone_from_unordered_ingredients_after_200_ticks").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.IMBUING_STATION);
			ImbuingStationTileEntity station = c.tile(c.at(0, 1, 0), ImbuingStationTileEntity.class);
			// The three ingredients may sit in any of the first three slots.
			station.itemHandler().setStackInSlot(0, new ItemStack(Items.BONE_MEAL, 2));
			station.itemHandler().setStackInSlot(1, PotionUtils.addPotionToItemStack(new ItemStack(Items.POTION), Potions.WATER));
			station.itemHandler().setStackInSlot(2, new ItemStack(Items.VINE, 2));
			// Slot 3 is the item being imbued (the original recipe imbues the cobblestone with bottle + vine + bone meal).
			station.itemHandler().setStackInSlot(3, new ItemStack(Items.COBBLESTONE, 2));

			for (int i = 0; i < ImbuingStationTileEntity.IMBUING_LENGTH - 1; i++)
			{
				station.tick();
			}

			c.check(station.itemHandler().getStackInSlot(4).isEmpty(), "nothing must be produced before 200 ticks");
			c.check(station.getImbuingProgress() > 0, "progress must be running");
			station.tick();
			c.eq(1, TestCtx.count(station.itemHandler(), Items.MOSSY_COBBLESTONE), "mossy cobblestone produced");
			c.eq(1, station.itemHandler().getStackInSlot(0).getCount(), "one bone meal consumed");
			c.check(station.itemHandler().getStackInSlot(1).isEmpty(), "the single water bottle consumed");
			c.eq(1, station.itemHandler().getStackInSlot(2).getCount(), "one vine consumed");
			c.eq(1, station.itemHandler().getStackInSlot(3).getCount(), "one cobblestone consumed");
		}));

		tests.add(RTTest.named("behavior.imbuing_station.a_wrong_ingredient_never_produces_anything").run(c -> {
			c.floor();
			c.set(c.at(0, 1, 0), ModBlocks.IMBUING_STATION);
			ImbuingStationTileEntity station = c.tile(c.at(0, 1, 0), ImbuingStationTileEntity.class);
			station.itemHandler().setStackInSlot(0, new ItemStack(Items.BONE_MEAL, 1));
			station.itemHandler().setStackInSlot(1, new ItemStack(Items.DIRT, 1));
			station.itemHandler().setStackInSlot(2, new ItemStack(Items.VINE, 1));
			station.itemHandler().setStackInSlot(3, new ItemStack(Items.COBBLESTONE, 1));

			for (int i = 0; i < 3 * ImbuingStationTileEntity.IMBUING_LENGTH; i++)
			{
				station.tick();
			}

			c.check(station.itemHandler().getStackInSlot(4).isEmpty(), "no output expected");
			c.eq(0, station.getImbuingProgress(), "progress");
			c.eq(1, station.itemHandler().getStackInSlot(1).getCount(), "ingredients must be untouched");
		}));

		return tests;
	}
}
